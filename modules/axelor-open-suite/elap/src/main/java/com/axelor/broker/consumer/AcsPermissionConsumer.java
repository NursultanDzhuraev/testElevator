package com.axelor.broker.consumer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.client.AcsPermissionClient;
import com.axelor.dto.AccessRightDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.axelor.broker.RabbitMQConfig.*;

@Slf4j
@Singleton
public class AcsPermissionConsumer extends BaseRabbitMQConsumer {
    private final AcsPermissionClient permissionClient;
    private static final String QUEUE_NAME = RabbitMQConfig.ACS_ACCESS_QUEUE;
    private static final String CONSUMER_NAME = "ACS-Permission-Consumer";

    @Inject
    public AcsPermissionConsumer(RabbitMQConnectionManager connectionManager, AcsPermissionClient permissionClient) {
        super(connectionManager);
        this.permissionClient = permissionClient;
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
            case ACS_ACCESS_CREATED:
                handleCreate(data);
                break;
            case ACS_ACCESS_UPDATED:
                handleUpdate(data);
                break;
            case ACS_ACCESS_DELETED:
                handleDelete(data);
                break;
            default:
                log.warn("Unknown action: {}", routingKey);
                throw new IllegalArgumentException("Unknown action: " + routingKey);
        }
    }

    private void handleCreate(Object data) throws Exception {
        try {
            AccessRightDtoRequest request = objectMapper.convertValue(data, AccessRightDtoRequest.class);
            permissionClient.createPermission(request);
        } catch (Exception e) {
            log.error("Error create permission: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleUpdate(Object data) throws Exception {
        try {
            Map<String, Object> updateData = objectMapper.convertValue(data, Map.class);
            String permissionId = (String) updateData.get("id");

            Map<String, Object> updates = (Map<String, Object>) updateData.get("updates");
            AccessRightDtoRequest request = objectMapper.convertValue(updates, AccessRightDtoRequest.class);

            permissionClient.updatePermission(permissionId, request);
        } catch (Exception e) {
            log.error("Error updated permission: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleDelete(Object data) throws Exception {
        try {
            String permissionId = valueAsString(data);
            if (permissionId == null || permissionId.isBlank()) {
                throw new IllegalArgumentException("id not found");
            }
            permissionClient.deletePermission(permissionId);
        } catch (Exception e) {
            log.error("Error delete permission: {}", e.getMessage(), e);
            throw e;
        }
    }
}
