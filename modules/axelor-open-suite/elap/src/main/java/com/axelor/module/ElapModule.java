package com.axelor.module;

import com.axelor.app.AxelorModule;
import com.axelor.broker.RabbitMQConnectionManager;
import com.axelor.broker.consumer.AcsDeviceConsumer;
import com.axelor.broker.consumer.AcsLocationConsumer;
import com.axelor.broker.consumer.AcsPermissionConsumer;
import com.axelor.broker.consumer.AcsUserConsumer;
import com.axelor.broker.producer.AcsDeviceProducer;
import com.axelor.broker.producer.AcsLocationProducer;
import com.axelor.broker.producer.AcsPermissionProducer;
import com.axelor.broker.producer.AcsUserProducer;
import com.axelor.service.*;
import com.axelor.service.impl.*;

public class ElapModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AcsDeviceConsumer.class).asEagerSingleton();
    bind(AcsLocationConsumer.class).asEagerSingleton();
    bind(AcsPermissionConsumer.class).asEagerSingleton();
    bind(AcsUserConsumer.class).asEagerSingleton();
    bind(PartnerService.class).to(PartnerServiceImpl.class);
    bind(RabbitMQConnectionManager.class).asEagerSingleton();
    bind(GeneratorId.class).asEagerSingleton();
    bind(BuildingService.class).to(BuildingServiceImpl.class);
    bind(DeviceService.class).to(DeviceServiceImpl.class);
    bind(AccessRightsService.class).to(AccessRightsServiceImpl.class);
    bind(EntranceService.class).to(EntranceServiceImpl.class);
    bind(DahuaAutoRegisterService.class).to(DahuaAutoRegisterServiceImpl.class);
    bind(AcsDeviceProducer.class).asEagerSingleton();
    bind(AcsLocationProducer.class).asEagerSingleton();
    bind(AcsUserProducer.class).asEagerSingleton();
    bind(AcsPermissionProducer.class).asEagerSingleton();
  }
}
