package com.axelor.broker.producer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class BaseRabbitMQProducer {
    protected final ObjectMapper objectMapper;
    protected final RabbitMQConnectionManager connectionManager;
    private static final String EXCHANGE_NAME = RabbitMQConfig.ACS_EXCHANGE;
    private static final int CONFIRM_TIMEOUT = 5000;

    public BaseRabbitMQProducer(RabbitMQConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    protected abstract String getProducerName();

    protected void sendMessage(String routingKey, Object body) throws Exception {
        Channel channel = null;
        try {
            channel = connectionManager.createChannel();


            Map<String, Object> messageWrapper = new HashMap<>();
            messageWrapper.put("data", body);
            messageWrapper.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(messageWrapper);
            log.debug("{} Sending message to: {} -> {}", getProducerName(), routingKey, json);

            Map<String, Object> headers = new HashMap<>();
            headers.put("x-retry-count", 0);
            headers.put("producer", getProducerName());

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .deliveryMode(1)
                    .headers(headers)
                    .build();

            channel.basicPublish(
                    EXCHANGE_NAME,
                    routingKey,
                    props,
                    json.getBytes(StandardCharsets.UTF_8)
            );

            boolean confirmed = channel.waitForConfirms(CONFIRM_TIMEOUT);

            if (!confirmed) {
                throw new Exception("RabbitMQ did not confirm message delivery within " + CONFIRM_TIMEOUT + "ms");
            }
        } catch (Exception e) {
            log.error("{} - Failed to send message: {}",
                    getProducerName(), e.getMessage(), e);
            if (e.getMessage().contains("connection") || e.getMessage().contains("closed")) {
                log.warn("Connection issue detected, will reinitialize on next attempt");
            }
            throw new Exception("RabbitMQ message error: " + e.getMessage(), e);

        } finally {

            closeChannel(channel);
        }
    }

    protected void closeChannel(Channel channel) {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (Exception e) {
                log.warn("Error closing channel: {}", e.getMessage());
            }
        }
    }

    protected void sendCreate(String routingKey, Object body) throws Exception {
        sendMessage(routingKey, body);
    }

    protected void sendUpdate(String routingKey, String id, Object updates) throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", id);
        updateData.put("updates", updates);

        sendMessage(routingKey, updateData);
    }

    protected void sendDelete(String routingKey, String id) throws Exception {
        sendMessage(routingKey, id);
    }

    protected void sendUpdatePermission(String routingKey, String id, Object updates) throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", id);
        updateData.put("updated", updates);

        sendMessage(routingKey, updateData);
    }
}
