package com.axelor.service.impl;

import com.axelor.apps.erp.db.AccessRights;
import com.axelor.apps.erp.db.repo.AccessRightsRepository;
import com.axelor.broker.producer.AcsPermissionProducer;
import com.axelor.dto.AccessRightDtoRequest;
import com.axelor.mapper.AccessRightsMapper;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.service.AccessRightsService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class AccessRightsServiceImpl implements AccessRightsService {
    private final GeneratorId generatorId;
    private final AccessRightsMapper mapper;
    private final AcsPermissionProducer producer;
    private final AccessRightsRepository repository;

    @Inject
    public AccessRightsServiceImpl(GeneratorId generatorId, AccessRightsMapper mapper, AcsPermissionProducer producer, AccessRightsRepository repository) {
        this.generatorId = generatorId;
        this.mapper = mapper;
        this.producer = producer;
        this.repository = repository;
    }

    @Transactional
    @Override
    public void accessRightsSaveAndUpdate(ActionRequest request, ActionResponse response) {
        try {
            AccessRights accessRights = request.getContext().asType(AccessRights.class);
            boolean isNew = (accessRights.getId() == null);
            if (isNew) {
                AccessRightDtoRequest dto = mapper.toDto(accessRights);
                producer.sendAccessCreate(dto);
                response.setValues(accessRights);
                response.setNotify("AccessRights created successfully");
            } else {
                AccessRightDtoRequest dto = mapper.toDto(accessRights);
                producer.sendAccessUpdate(accessRights.getUuid(), dto);
                response.setValues(accessRights);
                response.setNotify("AccessRights updated successfully");
            }
        } catch (Exception e) {
            log.error(" Error saving accessRights: {}", e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void accessDelete(ActionRequest request, ActionResponse response) {
        try {
            Context context = request.getContext();
            Integer id = (Integer) context.get("id");
            AccessRights accessRights = repository.find(id.longValue());
            producer.sendAccessDelete(accessRights.getUuid());

        } catch (Exception e) {
            log.error(" Error deleting accessRights: {}", e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void accessListDelete(ActionRequest request, ActionResponse response) {
        try {
            Context context = request.getContext();
            List<Integer> ids = (List<Integer>) context.get("_ids");
            for (Integer id : ids) {
                AccessRights accessRights = repository.find(id.longValue());
                producer.sendAccessDelete(accessRights.getUuid());
            }
        } catch (Exception e) {
            log.error(" Error deleting accessRights: {}", e.getMessage(), e);
        }
    }
}

