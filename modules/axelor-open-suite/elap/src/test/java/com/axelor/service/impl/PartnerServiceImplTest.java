package com.axelor.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.erp.db.AccessCredential;
import com.axelor.apps.erp.db.AccessRights;
import com.axelor.apps.erp.db.Apartment;
import com.axelor.apps.erp.db.Device;
import com.axelor.apps.erp.db.Entrance;
import com.axelor.apps.erp.db.repo.AccessCredentialRepository;
import com.axelor.apps.erp.db.repo.AccessRightsRepository;
import com.axelor.broker.producer.AcsUserProducer;
import com.axelor.dto.PartnerDtoRequest;
import com.axelor.mapper.ChangePermissionMapper;
import com.axelor.mapper.PartnerMapper;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PartnerServiceImplTest {

  private AccessRightsRepository accessRightsRepository;
  private AccessCredentialRepository accessCredentialRepository;
  private PartnerMapper partnerMapper;
  private ChangePermissionMapper changePermissionMapper;
  private AcsUserProducer acsUserProducer;
  private PartnerServiceImpl service;

  @BeforeEach
  void setup() {
    accessRightsRepository = mock(AccessRightsRepository.class);
    accessCredentialRepository = mock(AccessCredentialRepository.class);
    partnerMapper = mock(PartnerMapper.class);
    changePermissionMapper = mock(ChangePermissionMapper.class);
    acsUserProducer = mock(AcsUserProducer.class);

    service =
        new PartnerServiceImpl(
            accessRightsRepository,
            accessCredentialRepository,
            partnerMapper,
            changePermissionMapper,
            acsUserProducer);

    AccessRights fullAccess = new AccessRights();
    fullAccess.setPermissionType("full_access");
    fullAccess.setUuid("perm-full");
    when(accessRightsRepository.findByType("full_access")).thenReturn(fullAccess);
    when(accessCredentialRepository.save(any(AccessCredential.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(partnerMapper.toDto(any(Partner.class))).thenReturn(mock(PartnerDtoRequest.class));
  }

  @Test
  void shouldCreateCredentialsOnlyForUniqueDevicesAcrossApartments() throws Exception {
    Partner partner = new Partner();
    partner.setName("Test User");
    partner.setApartment(
        setOf(
            apartmentWithDevices(device(1L, "dev-1"), device(2L, "dev-2")),
            apartmentWithDevices(device(2L, "dev-2"), device(3L, "dev-3"))));

    ActionRequest request = mockRequestWithPartner(partner);
    ActionResponse response = mock(ActionResponse.class);

    service.partnerSaveDevice(response, request);

    assertEquals(3, partner.getAccessCredentials().size());
    verify(accessCredentialRepository, times(3)).save(any(AccessCredential.class));
    verify(acsUserProducer).sendUserCreate(any(PartnerDtoRequest.class));
    verify(acsUserProducer, never()).sendUserUpdate(any(String.class), any(PartnerDtoRequest.class));
  }

  @Test
  void shouldNotDuplicateExistingCredentialOnPartnerUpdate() throws Exception {
    Partner partner = new Partner();
    partner.setId(77L);
    partner.setUuid("partner-uuid");

    Device existingDevice = device(10L, "dev-10");
    Device newDevice = device(20L, "dev-20");

    AccessCredential existingCredential = new AccessCredential();
    existingCredential.setDevice(existingDevice);
    AccessRights fullAccess = accessRightsRepository.findByType("full_access");
    existingCredential.setAccessRights(fullAccess);

    partner.setAccessCredentials(new ArrayList<>(List.of(existingCredential)));
    partner.setApartment(setOf(apartmentWithDevices(existingDevice, newDevice)));

    ActionRequest request = mockRequestWithPartner(partner);
    ActionResponse response = mock(ActionResponse.class);

    service.partnerSaveDevice(response, request);

    assertEquals(2, partner.getAccessCredentials().size());
    verify(accessCredentialRepository, times(1)).save(any(AccessCredential.class));
    verify(acsUserProducer).sendUserUpdate(eq("partner-uuid"), any(PartnerDtoRequest.class));
    verify(acsUserProducer, never()).sendUserCreate(any(PartnerDtoRequest.class));
  }

  @Test
  void shouldKeepCredentialsEmptyWhenPartnerHasNoApartments() throws Exception {
    Partner partner = new Partner();
    partner.setApartment(new HashSet<>());

    ActionRequest request = mockRequestWithPartner(partner);
    ActionResponse response = mock(ActionResponse.class);

    service.partnerSaveDevice(response, request);

    assertEquals(0, partner.getAccessCredentials().size());
    verify(accessCredentialRepository, never()).save(any(AccessCredential.class));
    verify(acsUserProducer).sendUserCreate(any(PartnerDtoRequest.class));
  }

  private ActionRequest mockRequestWithPartner(Partner partner) {
    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    when(request.getContext().asType(Partner.class)).thenReturn(partner);
    return request;
  }

  private Apartment apartmentWithDevices(Device... devices) {
    Entrance entrance = new Entrance();
    entrance.setDevice(new ArrayList<>(List.of(devices)));
    Apartment apartment = new Apartment();
    apartment.setEntrance(entrance);
    return apartment;
  }

  private Device device(Long id, String uuid) {
    Device device = new Device();
    device.setId(id);
    device.setUuid(uuid);
    return device;
  }

  @SafeVarargs
  private final <T> Set<T> setOf(T... values) {
    return new HashSet<>(List.of(values));
  }
}
