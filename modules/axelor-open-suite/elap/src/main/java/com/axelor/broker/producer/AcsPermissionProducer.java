package com.axelor.broker.producer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.dto.AccessRightDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static com.axelor.broker.RabbitMQConfig.*;

@Slf4j
@Singleton
public class AcsPermissionProducer extends BaseRabbitMQProducer{
    private static final String PRODUCER_NAME = "Permission_Producer";
    @Inject
    public AcsPermissionProducer(RabbitMQConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }

    public void sendAccessCreate(AccessRightDtoRequest dto) throws Exception {
        sendCreate(ACS_ACCESS_CREATED, dto);
    }

    public void sendAccessUpdate(String permissionId, AccessRightDtoRequest dto) throws Exception {
       sendUpdate(ACS_ACCESS_UPDATED, permissionId, dto);
    }

    public void sendAccessDelete(String permissionId) throws Exception{
        sendDelete(ACS_ACCESS_DELETED, permissionId);
    }
}
