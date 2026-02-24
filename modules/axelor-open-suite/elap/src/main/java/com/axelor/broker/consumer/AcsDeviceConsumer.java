package com.axelor.broker.consumer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.client.AcsDeviceClient;
import com.axelor.dto.DeviceDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.axelor.broker.RabbitMQConfig.*;

@Slf4j
@Singleton
public class AcsDeviceConsumer extends BaseRabbitMQConsumer {
    private final AcsDeviceClient deviceClient;
    private static final String QUEUE_NAME = RabbitMQConfig.ACS_DEVICE_QUEUE;
    private static final String CONSUMER_NAME = "ACS-Device-Consumer";

    @Inject
    public AcsDeviceConsumer(RabbitMQConnectionManager connectionManager, AcsDeviceClient deviceClient) {
        super(connectionManager);
        this.deviceClient = deviceClient;
        startConsuming();
    }

    @Override
    protected String getConsumerName() {
        return CONSUMER_NAME;
    }

    @Override
    protected String getQueueName() {
        return QUEUE_NAME;
    }

    @Override
    protected void processMessage(String message, String routingKey) throws Exception {
        Envelope envelope = parseEnvelope(message);
        Object data = envelope.data;

        switch (routingKey) {
            case ACS_DEVICE_CREATED:
                handleCreate(data);
                break;
            case ACS_DEVICE_UPDATED:
                handleUpdate(data);
                break;
            case ACS_DEVICE_DELETED:
                handleDelete(data);
                break;
            default:
                log.warn("Unknown action: {}", routingKey);
                throw new IllegalArgumentException("Unknown action: " + routingKey);
        }
    }

    private void handleCreate(Object data) throws Exception {
        try {
            DeviceDtoRequest request = objectMapper.convertValue(data, DeviceDtoRequest.class);
            deviceClient.createDevice(request);
        } catch (Exception e) {
            log.error("Error create device: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleUpdate(Object data) throws Exception {
        Map<String, Object> updateData = objectMapper.convertValue(data, Map.class);
        String deviceId = (String) updateData.get("id");

        Map<String, Object> updates = (Map<String, Object>) updateData.get("updates");
        DeviceDtoRequest request = objectMapper.convertValue(updates, DeviceDtoRequest.class);

        deviceClient.updateDevice(deviceId, request);

    }

    private void handleDelete(Object data) throws Exception {
        String deviceId = valueAsString(data);
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("id not found");
        }
        deviceClient.deleteDevice(deviceId);

    }
}
