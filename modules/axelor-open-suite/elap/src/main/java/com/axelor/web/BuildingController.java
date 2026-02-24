package com.axelor.web;

import com.axelor.apps.erp.db.Building;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.BuildingService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.util.Map;

public class BuildingController {
    private final BuildingService buildingService;

    @Inject
    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    public void viewMap(ActionRequest request, ActionResponse response) {
        Building building = request.getContext().asType(Building.class);
        try {
            Map<String, Object> mapView = buildingService.displayMap(building);
            response.setView(mapView);
        } catch (Exception e) {
            response.setInfo("Address not found");
            response.setReload(true);
        }
    }

    public void geocodeAddress(ActionRequest request, ActionResponse response) {
        buildingService.geocodeAddress(request, response);
    }

    public void applySelectedGeoFromEditor(ActionRequest request, ActionResponse response) {
        buildingService.applySelectedGeoResult(request, response);
    }

    public void applyPendingGeo(ActionRequest request, ActionResponse response) {
        buildingService.applyFromGeoId(request, response);
    }

    public void buildingStatistics(ActionRequest request, ActionResponse response) {
        buildingService.buildingStatistics(request, response);
    }

}
