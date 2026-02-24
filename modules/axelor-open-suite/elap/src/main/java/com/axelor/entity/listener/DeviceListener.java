package com.axelor.entity.listener;

import com.axelor.apps.erp.db.Device;
import com.axelor.broker.producer.AcsDeviceProducer;
import com.axelor.dto.DeviceDtoRequest;
import com.axelor.inject.Beans;
import com.axelor.mapper.DeviceMapper;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import java.util.UUID;

public class DeviceListener {

    @PrePersist
    public void generateUUID(Device device) {
        if(device.getUuid() == null)
            device.setUuid(UUID.randomUUID().toString());
    }

    @PostPersist
    public void sendDeviceCreate(Device device) throws Exception {
        DeviceMapper mapper = Beans.get(DeviceMapper.class);
        AcsDeviceProducer producer = Beans.get(AcsDeviceProducer.class);
        DeviceDtoRequest dto = mapper.toDto(device);
        producer.sendDeviceCreate(dto);
    }

    @PostUpdate
    public void sendDeviceUpdate(Device device) throws Exception {
        DeviceMapper mapper = Beans.get(DeviceMapper.class);
        AcsDeviceProducer producer = Beans.get(AcsDeviceProducer.class);
        DeviceDtoRequest dto = mapper.toDto(device);
        producer.sendDeviceUpdate(dto.getDeviceId(),dto);

    }

    @PostRemove
    public void sendDeviceDelete(Device device) throws Exception {
        AcsDeviceProducer producer = Beans.get(AcsDeviceProducer.class);
       producer.sendDeviceDelete(device.getUuid());
    }
}
