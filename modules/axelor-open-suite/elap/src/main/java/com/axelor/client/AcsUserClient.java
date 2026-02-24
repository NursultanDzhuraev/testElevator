package com.axelor.client;

import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.dto.PartnerChangePermissionDto;
import com.axelor.dto.PartnerDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.io.IOException;

import static com.axelor.client.ACSConfig.API_USER_URL;

@Slf4j
@Singleton
public class AcsUserClient extends BaseAcsClient {

    @Inject
    public AcsUserClient(AcsAuthClient authClient, AcsErrorResponseParser errorParser) {
        super(authClient, errorParser);
    }

    public String getUserSync(String id) {
        try {
            Response executedGet = executeGet(API_USER_URL + id + "/sync-status/");
            validateResponse(executedGet,"user-sync-status");
            return getResponseBody(executedGet);
        } catch (IOException | AcsApiException e) {
            log.error("Error user get sync {}", e.getMessage(), e);
            return null;
        }
    }

    public void createUser(PartnerDtoRequest userRequest) {
        try {
            Response response = executePost(API_USER_URL, userRequest);
            validateResponse(response, "create user");
            getResponseBody(response);
            log.info("User created successful: {}", userRequest.getUserId());
        } catch (IOException | AcsApiException e) {
            log.error("Error create user: {}", e.getMessage(), e);
            throw new RuntimeException("User creation failed: " + userRequest.getFullName(), e);
        }
    }

    public void updateUser(String userId, PartnerDtoRequest userRequest) {
        try {
            Response response = executePost(API_USER_URL + userId + "/", userRequest);
            validateResponse(response, "update user");
            getResponseBody(response);
            log.info("User updated successful: {}", userId);
        } catch (IOException | AcsApiException e) {
            log.error("Error updated user: {}", e.getMessage(), e);
            throw new RuntimeException("User is not mistaken: " + userId, e);
        }
    }

    public void updateUserPermission(String userId, PartnerChangePermissionDto userRequest) {
        try {
            Response response = executePatch(API_USER_URL + userId + "/", userRequest);
            validateResponse(response, "updated user");
            getResponseBody(response);
            log.info("User updated permission successful: {}", userId);
        } catch (IOException | AcsApiException e) {
            log.error("Error updated user: {}", e.getMessage(), e);
            throw new RuntimeException("User is not mistaken: " + userId, e);
        }
    }

    public void deleteUser(String userId) {
        try {
            Response response = executeDelete(API_USER_URL + userId + "/");
            validateResponse(response, "delete user");
            getResponseBody(response);

            log.info("User delete successful: {}", userId);

        } catch (IOException | AcsApiException e) {
            log.error("Error delete user: {}", e.getMessage(), e);
            throw new RuntimeException("User not deleted: " + userId, e);
        }
    }
}
