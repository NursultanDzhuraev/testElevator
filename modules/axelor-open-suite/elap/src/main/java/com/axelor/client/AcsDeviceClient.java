package com.axelor.client;

import com.axelor.dto.DeviceDtoRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.io.IOException;

import static com.axelor.client.ACSConfig.API_DEVICE_URL;

@Slf4j
@Singleton
public class AcsDeviceClient extends BaseAcsClient{

    @Inject
    public AcsDeviceClient(AcsAuthClient authClient, AcsErrorResponseParser errorParse) {
        super(authClient, errorParse);
    }

    public void createDevice(DeviceDtoRequest deviceRequest){
        try {
            Response response = executePost(API_DEVICE_URL, deviceRequest);
            validateResponse(response,"create device");
            getResponseBody(response);
            log.info("Device created successful {}", deviceRequest.getDeviceId());

        } catch (IOException | AcsApiException e) {
            log.error("Error create device : {}", e.getMessage(), e);
            throw new RuntimeException("Device creation failed " + deviceRequest.getName(),e);
        }
    }

    public void updateDevice(String deviceId, DeviceDtoRequest deviceRequest) {
        try {
            Response response = executePatch(API_DEVICE_URL + deviceId + "/", deviceRequest);
            validateResponse(response, "update device");
            getResponseBody(response);
            log.info("Device updated successful {}", deviceRequest.getDeviceId());

        } catch (IOException | AcsApiException e) {
            log.error("Error updated device : {}", e.getMessage(), e);
            throw new RuntimeException("Device is not mistaken " + deviceRequest.getName(),e);
        }
    }

    public void deleteDevice(String deviceId) {
        try {
            Response response = executeDelete(API_DEVICE_URL + deviceId + "/");
            validateResponse(response, "delete device");
            getResponseBody(response);
            log.info("Device delete successful: {}", deviceId);

        } catch (IOException | AcsApiException e) {
            log.error("Error delete device : {}", e.getMessage(), e);
            throw new RuntimeException("Device not deleted " + deviceId,e);
        }
    }

}
