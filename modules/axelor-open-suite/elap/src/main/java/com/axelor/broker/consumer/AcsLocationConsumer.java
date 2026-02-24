package com.axelor.broker.consumer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.client.AcsLocationClient;
import com.axelor.dto.BuildingDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.axelor.broker.RabbitMQConfig.*;

@Slf4j
@Singleton
public class AcsLocationConsumer extends BaseRabbitMQConsumer {
    private final AcsLocationClient locationClient;
    private static final String QUEUE_NAME = RabbitMQConfig.ACS_LOCATION_QUEUE;
    private static final String CONSUMER_NAME = "ACS-Location-Consumer";

    @Inject
    public AcsLocationConsumer(RabbitMQConnectionManager connectionManager, AcsLocationClient locationClient) {
        super(connectionManager);
        this.locationClient = locationClient;
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
            case ACS_LOCATION_CREATED:
                handleCreate(data);
                break;
            case ACS_LOCATION_UPDATED:
                handleUpdate(data);
                break;
            case ACS_LOCATION_DELETED:
                handleDelete(data);
                break;
            default:
                log.warn("Unknown action: {}", routingKey);
                throw new IllegalArgumentException("Unknown action: " + routingKey);
        }
    }

    private void handleCreate(Object data) throws Exception {
        try {
            BuildingDtoRequest request = objectMapper.convertValue(data, BuildingDtoRequest.class);
            locationClient.createLocation(request);
        } catch (Exception e) {
            log.error("Error create location: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleUpdate(Object data) throws Exception {
        try {
            Map<String, Object> updateData = objectMapper.convertValue(data, Map.class);
            String locationId = (String) updateData.get("id");

            Map<String, Object> updates = (Map<String, Object>) updateData.get("updates");
            BuildingDtoRequest request = objectMapper.convertValue(updates, BuildingDtoRequest.class);

            locationClient.updateLocation(locationId, request);
        } catch (Exception e) {
            log.error("Error updated location: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleDelete(Object data) throws Exception {
        try {
            String locationId = valueAsString(data);

            if (locationId == null || locationId.isBlank()) {
                throw new IllegalArgumentException("id not found");
            }
            locationClient.deleteLocation(locationId);
        } catch (Exception e) {
            log.error("Error delete location: {}", e.getMessage(), e);
            throw e;
        }
    }
}
