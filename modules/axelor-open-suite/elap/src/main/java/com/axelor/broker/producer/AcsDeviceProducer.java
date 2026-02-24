package com.axelor.broker.producer;

import com.axelor.broker.RabbitMQConfig;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.dto.DeviceDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static com.axelor.broker.RabbitMQConfig.*;

@Slf4j
@Singleton
public class AcsDeviceProducer extends BaseRabbitMQProducer{
    private static final String PRODUCER_NAME = "Device_Producer";

    @Inject
    public AcsDeviceProducer(RabbitMQConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }
    public void sendDeviceCreate(DeviceDtoRequest dto) throws Exception {
        sendCreate(ACS_DEVICE_CREATED, dto);
    }

    public void sendDeviceUpdate(String deviceId, DeviceDtoRequest dto) throws Exception {
        sendUpdate(ACS_DEVICE_UPDATED, deviceId, dto);
    }

    public void sendDeviceDelete(String deviceId) throws Exception {
        sendDelete(ACS_DEVICE_DELETED, deviceId);
    }
}
