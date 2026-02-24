package com.axelor.broker.producer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.dto.PartnerChangePermissionDto;
import com.axelor.dto.PartnerDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static com.axelor.broker.RabbitMQConfig.*;


@Slf4j
@Singleton
public class AcsUserProducer extends BaseRabbitMQProducer {
    private static final String PRODUCER_NAME = "User_Producer";

    @Inject
    public AcsUserProducer(RabbitMQConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }

    public void sendUserCreate(PartnerDtoRequest dto) throws Exception {
        sendCreate(ACS_USER_CREATED, dto);
    }

    public void sendUserUpdate(String userId, PartnerDtoRequest dto) throws Exception {
        sendUpdate(ACS_USER_UPDATED, userId, dto);
    }

    public void sendUserDelete(String userId) throws Exception{
        sendDelete(ACS_USER_DELETED, userId);
    }
    public void sendUserChangePermission(String userId, PartnerChangePermissionDto dto) throws Exception {
        sendUpdatePermission(ACS_USER_PERMISSION, userId, dto);
    }
}
