package com.axelor.entity.listener;

import com.axelor.apps.erp.db.Building;
import com.axelor.broker.producer.AcsLocationProducer;
import com.axelor.dto.BuildingDtoRequest;
import com.axelor.inject.Beans;
import com.axelor.mapper.BuildingMapper;


import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import java.util.UUID;

public class BuildingListener {

    @PrePersist
    public void generateUUID(Building building) {
        if (building.getUuid() == null)
            building.setUuid(UUID.randomUUID().toString());
    }

    @PostPersist
    public void sendBuildingCreate(Building building) throws Exception {
        BuildingMapper mapper = Beans.get(BuildingMapper.class);
        AcsLocationProducer producer = Beans.get(AcsLocationProducer.class);
        BuildingDtoRequest location = mapper.toDto(building);
        producer.sendBuildingCreate(location);
    }

    @PostUpdate
    public void sendBuildingUpdate(Building building) throws Exception {
        BuildingMapper mapper = Beans.get(BuildingMapper.class);
        AcsLocationProducer producer = Beans.get(AcsLocationProducer.class);
        BuildingDtoRequest location = mapper.toDto(building);
        producer.sendBuildingUpdate(location.getLocationId(), location);
    }

    @PostRemove
    public void sendBuildingDelete(Building building) throws Exception {
        AcsLocationProducer producer = Beans.get(AcsLocationProducer.class);
        producer.sendBuildingDelete(building.getUuid());
    }
}
