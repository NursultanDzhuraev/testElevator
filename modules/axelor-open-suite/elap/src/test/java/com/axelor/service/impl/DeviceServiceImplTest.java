package com.axelor.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.erp.db.Device;
import com.axelor.apps.erp.db.repo.DeviceRepository;
import com.axelor.broker.producer.AcsDeviceProducer;
import com.axelor.dto.DeviceDtoRequest;
import com.axelor.mapper.DeviceMapper;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DeviceServiceImplTest {

  private DeviceMapper mapper;
  private DeviceRepository repository;
  private AcsDeviceProducer producer;
  private DeviceServiceImpl service;

  @BeforeEach
  void setup() {
    mapper = mock(DeviceMapper.class);
    repository = mock(DeviceRepository.class);
    producer = mock(AcsDeviceProducer.class);
    service = new DeviceServiceImpl(mock(GeneratorId.class), mapper, repository, producer);
    when(mapper.toDto(any(Device.class))).thenReturn(mock(DeviceDtoRequest.class));
  }

  @Test
  void shouldCreateDeviceWhenIdIsNull() {
    Device device = new Device();
    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(Device.class)).thenReturn(device);

    service.saveAndUpdateDevice(request, response);

    verify(producer).sendDeviceCreate(any(DeviceDtoRequest.class));
    verify(producer, never()).sendDeviceUpdate(any(), any());
  }

  @Test
  void shouldUpdateDeviceWhenIdExists() {
    Device device = new Device();
    device.setId(7L);
    device.setUuid("dev-7");
    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(Device.class)).thenReturn(device);

    service.saveAndUpdateDevice(request, response);

    verify(producer).sendDeviceUpdate(eq("dev-7"), any(DeviceDtoRequest.class));
    verify(producer, never()).sendDeviceCreate(any());
  }

  @Test
  void shouldDeleteDeviceById() {
    Device device = new Device();
    device.setUuid("dev-del");
    ActionRequest request = mock(ActionRequest.class);
    ActionResponse response = mock(ActionResponse.class);
    Context context = mock(Context.class);
    when(request.getContext()).thenReturn(context);
    when(context.get("id")).thenReturn(33);
    when(repository.find(33L)).thenReturn(device);

    service.deleteDevice(request, response);

    verify(producer).sendDeviceDelete("dev-del");
  }

  @Test
  void shouldDeleteDeviceList() {
    Device d1 = new Device();
    d1.setUuid("d1");
    Device d2 = new Device();
    d2.setUuid("d2");
    ActionRequest request = mock(ActionRequest.class);
    ActionResponse response = mock(ActionResponse.class);
    Context context = mock(Context.class);
    when(request.getContext()).thenReturn(context);
    when(context.get("_ids")).thenReturn(List.of(1, 2));
    when(repository.find(1L)).thenReturn(d1);
    when(repository.find(2L)).thenReturn(d2);

    service.deleteListDevices(request, response);

    verify(producer, times(2)).sendDeviceDelete(any());
    verify(producer).sendDeviceDelete("d1");
    verify(producer).sendDeviceDelete("d2");
  }
}
