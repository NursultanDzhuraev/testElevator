package com.axelor.broker.consumer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.client.AcsUserClient;
import com.axelor.dto.PartnerChangePermissionDto;
import com.axelor.dto.PartnerDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.axelor.broker.RabbitMQConfig.*;

@Slf4j
@Singleton
public class AcsUserConsumer extends BaseRabbitMQConsumer {
    private final AcsUserClient userClient;
    private static final String QUEUE_NAME = RabbitMQConfig.ACS_USER_QUEUE;
    private static final String CONSUMER_NAME = "ACS-User-Consumer";

    @Inject
    public AcsUserConsumer(RabbitMQConnectionManager connectionManager, AcsUserClient userClient) {
        super(connectionManager);
        this.userClient = userClient;
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
            case ACS_USER_CREATED:
                handleCreate(data);
                break;
            case ACS_USER_UPDATED:
                handleUpdate(data);
                break;
            case ACS_USER_PERMISSION:
                handleUpdatePermission(data);
                break;
            case ACS_USER_DELETED:
                handleDelete(data);
                break;
            default:
                log.warn("Unknown action: {}", routingKey);
                throw new IllegalArgumentException("Unknown action: " + routingKey);
        }
    }

    private void handleCreate(Object data) throws Exception {
        try {
            PartnerDtoRequest userRequest = objectMapper.convertValue(data, PartnerDtoRequest.class);
            userClient.createUser(userRequest);
        } catch (Exception e) {
            log.error("Error create user: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleUpdate(Object data) throws Exception {
        try {
            Map<String, Object> updateData = objectMapper.convertValue(data, Map.class);
            String userId = (String) updateData.get("id");

            Map<String, Object> updates = (Map<String, Object>) updateData.get("updates");
            PartnerDtoRequest userRequest = objectMapper.convertValue(updates, PartnerDtoRequest.class);

            userClient.updateUser(userId, userRequest);

        } catch (Exception e) {
            log.error("Error updated user: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleUpdatePermission(Object data) throws Exception {
        try {
            Map<String, Object> updatedData = objectMapper.convertValue(data, Map.class);
            String userId = (String) updatedData.get("id");

            Map<String, Object> updated = (Map<String, Object>) updatedData.get("updated");
            PartnerChangePermissionDto userChangePermissionRequest = objectMapper.convertValue(updated, PartnerChangePermissionDto.class);

            userClient.updateUserPermission(userId, userChangePermissionRequest);

        } catch (Exception e) {
            log.error("Error updated user: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleDelete(Object data) throws Exception {
        try {
            String userId = valueAsString(data);

            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("user_id not found");
            }
            userClient.deleteUser(userId);

        } catch (Exception e) {
            log.error("Error delete user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
