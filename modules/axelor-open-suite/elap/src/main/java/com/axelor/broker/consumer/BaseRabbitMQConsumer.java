package com.axelor.broker.consumer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.client.AcsApiException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class BaseRabbitMQConsumer {

    protected final ObjectMapper objectMapper;
    protected final RabbitMQConnectionManager connectionManager;

    protected Channel channel;

    private static final int MAX_RETRY_COUNT = 3;

    protected BaseRabbitMQConsumer(RabbitMQConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        try {
            this.channel = connectionManager.createChannel();
            this.channel.basicQos(1);

            setupQueuesWithDLX();
            log.info("{} consumer connected to RabbitMQ", getConsumerName());
        } catch (Exception e) {
            log.error("{} error connecting consumer", getConsumerName(), e);
            throw new RuntimeException("Consumer initialization error", e);
        }
    }

    protected abstract String getConsumerName();

    protected abstract String getQueueName();

    protected abstract void processMessage(String message, String routingKey) throws Exception;

    private void setupQueuesWithDLX() throws IOException {
        String queueName = getQueueName();
        String dlqName = queueName + ".dlq";

        channel.exchangeDeclare(RabbitMQConfig.ACS_EXCHANGE, BuiltinExchangeType.TOPIC, true);
        channel.exchangeDeclare(RabbitMQConfig.DEAD_LETTER_EXCHANGE, BuiltinExchangeType.DIRECT, true);

        Map<String, Object> mainQueueArgs = new HashMap<>();
        mainQueueArgs.put("x-dead-letter-exchange", RabbitMQConfig.DEAD_LETTER_EXCHANGE);
        mainQueueArgs.put("x-dead-letter-routing-key", queueName + ".dead");

        channel.queueDeclare(queueName, true, false, false, mainQueueArgs);
        channel.queueDeclare(dlqName, true, false, false, null);

        channel.queueBind(RabbitMQConfig.ACS_USER_QUEUE, RabbitMQConfig.ACS_EXCHANGE, "acs.user.*");
        channel.queueBind(RabbitMQConfig.ACS_LOCATION_QUEUE, RabbitMQConfig.ACS_EXCHANGE, "acs.location.*");
        channel.queueBind(RabbitMQConfig.ACS_DEVICE_QUEUE, RabbitMQConfig.ACS_EXCHANGE, "acs.device.*");
        channel.queueBind(RabbitMQConfig.ACS_ACCESS_QUEUE, RabbitMQConfig.ACS_EXCHANGE, "acs.access.*");

        channel.queueBind(dlqName, RabbitMQConfig.DEAD_LETTER_EXCHANGE, queueName + ".dead");

        log.info("Queue + DLQ ready: {} , {}", queueName, dlqName);
    }

    public void startConsuming() {
        try {
            String queueName = getQueueName();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                long deliveryTag = delivery.getEnvelope().getDeliveryTag();

                Map<String, Object> headers = delivery.getProperties().getHeaders();
                Integer retryCount = getRetryCount(headers);

                try {
                    processMessage(message, delivery.getEnvelope().getRoutingKey());
                    channel.basicAck(deliveryTag, false);
                    log.info("{} - Message processed", getConsumerName());

                } catch (Exception e) {
                    log.error("{} - Error processing message (retry: {}): {}",
                            getConsumerName(), retryCount, e.getMessage(), e);

                    handleMessageError(delivery, deliveryTag, retryCount, e);
                }
            };

            CancelCallback cancelCallback = consumerTag ->
                    log.warn("{} - Consumer cancelled/suspended", getConsumerName());

            channel.basicConsume(queueName, false, deliverCallback, cancelCallback);

            log.info("{} - Consumer started, queue={}", getConsumerName(), queueName);

        } catch (IOException e) {
            log.error("{} - Error starting consumer", getConsumerName(), e);
            throw new RuntimeException("Consumer start error", e);
        }
    }

    private void
    handleMessageError(Delivery delivery, long deliveryTag, Integer retryCount, Exception error) {
        try {
            if (error instanceof AcsApiException) {
                AcsApiException acsError = (AcsApiException) error;
                log.error("ACS API Error: code={}, status={}, message={}",
                        acsError.getErrorCode(), acsError.getHttpStatus(), acsError.getErrorMessage());

                if (acsError.shouldSendToDeadLetter()) {
                    log.error("Validation/Business error -> send to DLQ. details={}", acsError.getDetails());
                    sendToDeadLetterQueue(delivery, deliveryTag, acsError);
                    return;
                }

                if (acsError.isRetryable()) {
                    log.warn("Retryable ACS error. Retry {} / {}", retryCount + 1, MAX_RETRY_COUNT);
                    handleRetryLogic(delivery, deliveryTag, retryCount, error);
                    return;
                }
                log.error("ACS Error - DLQ send");
                sendToDeadLetterQueue(delivery, deliveryTag, error);
                return;
            }

            handleRetryLogic(delivery, deliveryTag, retryCount, error);

        } catch (IOException e) {
            log.error("Error handling : {}", e.getMessage());
        }
    }

    private void handleRetryLogic(Delivery delivery, long deliveryTag, Integer retryCount, Exception error) throws IOException {
        if (retryCount < MAX_RETRY_COUNT) {
            Map<String, Object> newHeaders = new HashMap<>();
            if (delivery.getProperties().getHeaders() != null) {
                newHeaders.putAll(delivery.getProperties().getHeaders());
            }
            newHeaders.put("x-retry-count", retryCount + 1);

            AMQP.BasicProperties newProps = delivery.getProperties().builder()
                    .headers(newHeaders)
                    .build();

            channel.basicAck(deliveryTag, false);
            channel.basicPublish(
                    RabbitMQConfig.ACS_EXCHANGE,
                    delivery.getEnvelope().getRoutingKey(),
                    newProps,
                    delivery.getBody()
            );

            log.info("Re-published message for retry {}", retryCount + 1);
        } else {
            sendToDeadLetterQueue(delivery, deliveryTag, error);
        }
    }

    private void sendToDeadLetterQueue(Delivery delivery, long deliveryTag, Exception error) throws IOException {
        channel.basicReject(deliveryTag, false);
        logFailedMessage(delivery, error);
    }

    private void logFailedMessage(Delivery delivery, Exception error) {
        try {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            log.error("========= FAILED MESSAGE =========");
            log.error("Queue: {}", getQueueName());
            log.error("Routing Key: {}", delivery.getEnvelope().getRoutingKey());
            log.error("Message: {}", message);
            log.error("Error: {}", error.getMessage(), error);
            log.error("==================================");
        } catch (Exception e) {
            log.error("Failed to log message: {}", e.getMessage(), e);
        }
    }

    private Integer getRetryCount(Map<String, Object> headers) {
        if (headers == null) {
            return 0;
        }
        Object retryObj = headers.get("x-retry-count");
        if (retryObj instanceof Integer) {
            return (Integer) retryObj;
        }
        if (retryObj instanceof Long) {
            Long l = (Long) retryObj;
            return l.intValue();
        }
        return 0;
    }

    protected <T> T parseMessage(String message, Class<T> clazz) throws IOException {
        return objectMapper.readValue(message, clazz);
    }

    protected Envelope parseEnvelope(String message) throws IOException {
        return objectMapper.readValue(message, Envelope.class);
    }

    protected UpdateEnvelope parseUpdateEnvelope(Object data) {
        Map<String, Object> updateData = objectMapper.convertValue(data, Map.class);

        String id = valueAsString(updateData.get("id"));
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id not found in update payload");
        }

        Object updates = updateData.get("updates");
        if (updates == null) {
            throw new IllegalArgumentException("updates not found in update payload");
        }

        return new UpdateEnvelope(id, updates);
    }

    protected String valueAsString(Object value) {
        return value instanceof String ? (String) value : null;
    }

    public void shutdown() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                log.info("{} - Channel closed", getConsumerName());
            }
            // Connection is owned by connectionManager; do not close it here.
        } catch (Exception e) {
            log.error("{} - Shutdown error", getConsumerName(), e);
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Envelope {
        public Object data;
        public Long timestamp;
    }

    @Value
    public static class UpdateEnvelope {
        String id;
        Object updates;
    }
}
