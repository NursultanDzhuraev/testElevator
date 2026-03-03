package com.axelor.service.impl;

import com.axelor.apps.erp.db.Device;
import com.axelor.apps.erp.db.repo.DeviceRepository;
import com.axelor.broker.producer.AcsDeviceProducer;
import com.axelor.dto.DeviceDtoRequest;
import com.axelor.mapper.DeviceMapper;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.service.DeviceService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class DeviceServiceImpl implements DeviceService {
    private final GeneratorId generatorId;
    private final DeviceMapper mapper;
    private final DeviceRepository repository;
    private final AcsDeviceProducer producer;

    @Inject
    public DeviceServiceImpl(GeneratorId generatorId, DeviceMapper mapper, DeviceRepository repository, AcsDeviceProducer producer) {
        this.generatorId = generatorId;
        this.mapper = mapper;
        this.repository = repository;
        this.producer = producer;
    }

    @Transactional
    @Override
    public void saveAndUpdateDevice(ActionRequest request, ActionResponse response) {
        try {
            Device device = request.getContext().asType(Device.class);
            if (device.getUuid() == null || device.getUuid().isBlank()) {
                device.setUuid(generatorId.generate("device"));
            }
            if (device.getExternalId() == null || device.getExternalId().isBlank()) {
                device.setExternalId(device.getUuid());
            }
            device.setUpdatedAt(LocalDateTime.now());
            device.setVersion(device.getVersion() == null ? 1 : device.getVersion() + 1);

            boolean isNew = (device.getId() == null);

            if (isNew) {
                DeviceDtoRequest dto = mapper.toDto(device);
                producer.sendDeviceCreate(dto);
                response.setValues(device);

                response.setNotify("device created successfully");

            } else {
                DeviceDtoRequest dto = mapper.toDto(device);
                producer.sendDeviceUpdate(device.getUuid(), dto);

                response.setValues(device);
                response.setNotify("device updated successfully");
            }

        } catch (Exception e) {
            log.error(" Error saving device: {}", e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void deleteListDevices(ActionRequest request, ActionResponse response) {
        try {
            Context context = request.getContext();
            List<Integer> ids = (List<Integer>) context.get("_ids");
            for (Integer id : ids) {
                Device device = repository.find(id.longValue());
                producer.sendDeviceDelete(device.getUuid());
            }
        } catch (Exception e) {
            log.error(" Error deleting device: {}", e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void deleteDevice(ActionRequest request, ActionResponse response) {
        try {
            Context context = request.getContext();
            Integer id =(Integer) context.get("id");
            Device device = repository.find(id.longValue());
            producer.sendDeviceDelete(device.getUuid());

        } catch (Exception e) {
            log.error(" Error deleting device: {}", e.getMessage(), e);
        }
    }
}
