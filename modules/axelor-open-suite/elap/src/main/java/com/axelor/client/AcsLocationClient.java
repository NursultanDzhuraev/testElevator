package com.axelor.client;

import com.axelor.dto.BuildingDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;


import java.io.IOException;

import static com.axelor.client.ACSConfig.API_LOCATION_URL;

@Slf4j
@Singleton
public class AcsLocationClient extends BaseAcsClient {
    @Inject
    public AcsLocationClient(AcsAuthClient authClient, AcsErrorResponseParser errorParse) {
        super(authClient, errorParse);
    }

    public void createLocation(BuildingDtoRequest buildingRequest) {
        try {
            Response response = executePost(API_LOCATION_URL, buildingRequest);
            validateResponse(response, "create location");
            getResponseBody(response);
            log.info("Location created successful: {}", buildingRequest.getLocationId());
        } catch (IOException | AcsApiException e) {
            log.error("Error create location : {}", e.getMessage(), e);
            throw new RuntimeException("Location creation failed " + buildingRequest.getName(),e);
        }
    }

    public void updateLocation(String locationId, BuildingDtoRequest buildingRequest) {
        try {
            Response response = executePatch(API_LOCATION_URL + locationId + "/", buildingRequest);
            validateResponse(response, "update location");
            getResponseBody(response);
            log.info("Location updated successful: {}", buildingRequest.getLocationId());
        } catch (IOException | AcsApiException e) {
            log.error("Error updated location : {}", e.getMessage(), e);
            throw new RuntimeException("Location is not mistaken " + buildingRequest.getName(),e);
        }
    }

    public void deleteLocation(String locationId) {
        try {
            Response response = executeDelete(API_LOCATION_URL + locationId + "/");
            validateResponse(response, "delete location");
            getResponseBody(response);
            log.info("Location delete successful: {}", locationId);

        } catch (IOException | AcsApiException e) {
            log.error("Error delete location : {}", e.getMessage(), e);
            throw new RuntimeException("Location not deleted " + locationId,e);
        }
    }
}
