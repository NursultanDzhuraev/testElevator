package com.axelor.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.erp.db.AccessRights;
import com.axelor.apps.erp.db.repo.AccessRightsRepository;
import com.axelor.broker.producer.AcsPermissionProducer;
import com.axelor.dto.AccessRightDtoRequest;
import com.axelor.mapper.AccessRightsMapper;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AccessRightsServiceImplTest {

  private AccessRightsMapper mapper;
  private AcsPermissionProducer producer;
  private AccessRightsRepository repository;
  private AccessRightsServiceImpl service;

  @BeforeEach
  void setup() {
    mapper = mock(AccessRightsMapper.class);
    producer = mock(AcsPermissionProducer.class);
    repository = mock(AccessRightsRepository.class);
    service = new AccessRightsServiceImpl(mock(GeneratorId.class), mapper, producer, repository);
    when(mapper.toDto(any(AccessRights.class))).thenReturn(mock(AccessRightDtoRequest.class));
  }

  @Test
  void shouldSendCreateForNewAccessRights() {
    AccessRights rights = new AccessRights();
    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(AccessRights.class)).thenReturn(rights);

    service.accessRightsSaveAndUpdate(request, response);

    verify(producer).sendAccessCreate(any(AccessRightDtoRequest.class));
    verify(producer, never()).sendAccessUpdate(any(), any());
    verify(response).setNotify("AccessRights created successfully");
  }

  @Test
  void shouldSendUpdateForExistingAccessRights() {
    AccessRights rights = new AccessRights();
    rights.setId(11L);
    rights.setUuid("acc-11");
    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(AccessRights.class)).thenReturn(rights);

    service.accessRightsSaveAndUpdate(request, response);

    verify(producer).sendAccessUpdate(eq("acc-11"), any(AccessRightDtoRequest.class));
    verify(producer, never()).sendAccessCreate(any());
    verify(response).setNotify("AccessRights updated successfully");
  }

  @Test
  void shouldDeleteSingleAccessRights() {
    AccessRights rights = new AccessRights();
    rights.setUuid("acc-del");

    ActionRequest request = mock(ActionRequest.class);
    ActionResponse response = mock(ActionResponse.class);
    Context context = mock(Context.class);
    when(request.getContext()).thenReturn(context);
    when(context.get("id")).thenReturn(5);
    when(repository.find(5L)).thenReturn(rights);

    service.accessDelete(request, response);

    verify(producer).sendAccessDelete("acc-del");
  }

  @Test
  void shouldDeleteListAccessRights() {
    AccessRights one = new AccessRights();
    one.setUuid("a1");
    AccessRights two = new AccessRights();
    two.setUuid("a2");

    ActionRequest request = mock(ActionRequest.class);
    ActionResponse response = mock(ActionResponse.class);
    Context context = mock(Context.class);
    when(request.getContext()).thenReturn(context);
    when(context.get("_ids")).thenReturn(List.of(1, 2));
    when(repository.find(1L)).thenReturn(one);
    when(repository.find(2L)).thenReturn(two);

    service.accessListDelete(request, response);

    verify(producer, times(2)).sendAccessDelete(any());
    verify(producer).sendAccessDelete("a1");
    verify(producer).sendAccessDelete("a2");
  }
}
