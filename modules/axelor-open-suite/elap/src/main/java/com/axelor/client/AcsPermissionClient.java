package com.axelor.client;

import com.axelor.dto.AccessRightDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.io.IOException;

import static com.axelor.client.ACSConfig.API_ACCESS_RIGHT_URL;

@Slf4j
@Singleton
public class AcsPermissionClient extends BaseAcsClient {
    @Inject
    public AcsPermissionClient(AcsAuthClient authClient, AcsErrorResponseParser errorParse) {
        super(authClient, errorParse);
    }

    public String createPermission(AccessRightDtoRequest accessRequest) {
        try {
            Response response = executePost(API_ACCESS_RIGHT_URL, accessRequest);
            validateResponse(response, "create Permission");
            String responseBody = getResponseBody(response);
            log.info("Permission created successful: {}", accessRequest.getPermissionId());
            return responseBody;
        } catch (IOException | AcsApiException e) {
            log.error("Error create Permission: {}", e.getMessage(), e);
            throw new RuntimeException("Permission creation failed: " + accessRequest.getPermissionName(), e);
        }
    }

    public void updatePermission(String permissionId, AccessRightDtoRequest accessRequest) {
        try {
            Response response = executePatch(API_ACCESS_RIGHT_URL + permissionId + "/", accessRequest);
            validateResponse(response, "update Permission");
            getResponseBody(response);
            log.info("Permission updated successful: {}", permissionId);
        } catch (IOException | AcsApiException e) {
            log.error("Error updated Permission: {}", e.getMessage(), e);
            throw new RuntimeException("Permission is not mistaken: " + permissionId, e);
        }
    }

    public void deletePermission(String permissionId) {
        try {
            Response response = executeDelete(API_ACCESS_RIGHT_URL + permissionId + "/");
            validateResponse(response, "delete Permission");
            getResponseBody(response);

            log.info("Permission delete successful: {}", permissionId);

        } catch (IOException | AcsApiException e) {
            log.error("Error delete Permission: {}", e.getMessage(), e);
            throw new RuntimeException("Permission not deleted: " + permissionId, e);
        }
    }

}
