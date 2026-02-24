package com.axelor.entity.listener;

import com.axelor.apps.erp.db.AccessRights;
import com.axelor.broker.producer.AcsPermissionProducer;
import com.axelor.dto.AccessRightDtoRequest;
import com.axelor.inject.Beans;
import com.axelor.mapper.AccessRightsMapper;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import java.util.UUID;

public class AccessRightsListener {


    @PrePersist
    public void generateUUID(AccessRights accessRights) {
        if (accessRights.getUuid() == null)
            accessRights.setUuid(UUID.randomUUID().toString());
    }

    @PostPersist
    public void sendAccessCreate(AccessRights accessRights) throws Exception {
        AccessRightsMapper mapper = Beans.get(AccessRightsMapper.class);
        AcsPermissionProducer producer = Beans.get(AcsPermissionProducer.class);
        AccessRightDtoRequest dto = mapper.toDto(accessRights);
        producer.sendAccessCreate(dto);
    }

    @PostUpdate
    public void sendAccessUpdate(AccessRights accessRights) throws Exception {
        AccessRightsMapper mapper = Beans.get(AccessRightsMapper.class);
        AcsPermissionProducer producer = Beans.get(AcsPermissionProducer.class);
        AccessRightDtoRequest dto = mapper.toDto(accessRights);
        producer.sendAccessUpdate(dto.getPermissionId(), dto);
    }

    @PostRemove
    public void sendAccessDelete(AccessRights accessRights) throws Exception {
        AcsPermissionProducer producer = Beans.get(AcsPermissionProducer.class);
        producer.sendAccessDelete(accessRights.getUuid());
    }
}
