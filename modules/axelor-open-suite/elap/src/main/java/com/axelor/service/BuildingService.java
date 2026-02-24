package com.axelor.service;

import com.axelor.apps.erp.db.Building;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface BuildingService {

    Map<String, Object> displayMap(Building building);

    void geocodeAddress(ActionRequest request, ActionResponse response);

    void applySelectedGeoResult(ActionRequest request, ActionResponse response);

    void applyFromGeoId(ActionRequest request, ActionResponse response);

    void buildingStatistics(ActionRequest request, ActionResponse response);
}
