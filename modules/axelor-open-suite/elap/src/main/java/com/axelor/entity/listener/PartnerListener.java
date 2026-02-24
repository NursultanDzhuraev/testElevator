package com.axelor.entity.listener;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.erp.db.Apartment;
import com.axelor.broker.producer.AcsUserProducer;
import com.axelor.dto.PartnerDtoRequest;
import com.axelor.inject.Beans;
import com.axelor.mapper.PartnerMapper;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import java.util.UUID;

public class PartnerListener {



    @PrePersist
    public void generateUUID(Partner partner) {
        if(partner.getUuid() == null){
            partner.setUuid(UUID.randomUUID().toString().replace("-",""));}
    }


    @PostRemove
    public void sendPartnerDelete(Partner partner) throws Exception {
        AcsUserProducer producer = Beans.get(AcsUserProducer.class);
        producer.sendUserDelete(partner.getUuid());
    }
}
