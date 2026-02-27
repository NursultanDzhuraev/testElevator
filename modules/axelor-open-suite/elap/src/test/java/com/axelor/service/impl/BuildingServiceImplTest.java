package com.axelor.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.erp.db.Apartment;
import com.axelor.apps.erp.db.Building;
import com.axelor.apps.erp.db.Entrance;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BuildingServiceImplTest {

  private MapService mapService;
  private BuildingServiceImpl service;

  @BeforeEach
  void setup() {
    mapService = mock(MapService.class);
    service = new BuildingServiceImpl(mapService, null, null);
  }

  @Test
  void shouldReturnEmptyMapWhenCoordinatesAbsent() {
    Map<String, Object> result = service.displayMap(new Building());
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnMapPayloadWhenCoordinatesPresent() {
    Building building = new Building();
    building.setLatitude(BigDecimal.valueOf(42.1));
    building.setLongitude(BigDecimal.valueOf(74.6));
    when(mapService.getMapUrl(building.getLatitude(), building.getLongitude())).thenReturn("map-url");

    Map<String, Object> result = service.displayMap(building);

    assertEquals("Map", result.get("title"));
    assertEquals("map-url", result.get("resource"));
    assertEquals("html", result.get("viewType"));
  }

  @Test
  void shouldCalculateBuildingStatistics() {
    Apartment a1 = new Apartment();
    a1.setDebtStatus(true);
    a1.setPartner(List.of(resident(), nonResident()));

    Apartment a2 = new Apartment();
    a2.setDebtStatus(false);
    a2.setPartner(List.of(resident()));

    Entrance entrance = new Entrance();
    entrance.setApartment(List.of(a1, a2));

    Building building = new Building();
    building.setEntrance(List.of(entrance));

    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(Building.class)).thenReturn(building);

    service.buildingStatistics(request, response);

    verify(response).setValue("$allApartments", 2);
    verify(response).setValue("$allResidents", 2);
    verify(response).setValue("$numberOfDebtors", 1);
    verify(response).setValue("$totalAmountOfDebt", 200);
  }

  @Test
  void shouldSkipStatisticsWhenBuildingIsNull() {
    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(Building.class)).thenReturn(null);

    service.buildingStatistics(request, response);

    verifyNoInteractions(response);
  }

  private Partner resident() {
    Partner partner = new Partner();
    partner.setPartnerType("resident");
    return partner;
  }

  private Partner nonResident() {
    Partner partner = new Partner();
    partner.setPartnerType("owner");
    return partner;
  }
}
