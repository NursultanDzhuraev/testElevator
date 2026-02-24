package com.axelor.broker.producer;

import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.dto.BuildingDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static com.axelor.broker.RabbitMQConfig.*;


@Slf4j
@Singleton
public class AcsLocationProducer extends BaseRabbitMQProducer{
    private static final String PRODUCER_NAME = "Location_Producer";
    @Inject
    public AcsLocationProducer(RabbitMQConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }

    public void sendBuildingCreate(BuildingDtoRequest dto) throws Exception {
        sendCreate(ACS_LOCATION_CREATED, dto);
    }

    public void sendBuildingUpdate(String locationId, BuildingDtoRequest dto) throws Exception {
        sendUpdate(ACS_LOCATION_UPDATED, locationId, dto);
    }

    public void sendBuildingDelete(String locationId) throws Exception {
        sendDelete(ACS_LOCATION_DELETED, locationId);
    }
}
