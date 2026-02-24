package com.axelor.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

import static com.axelor.client.ACSConfig.API_BASE_URL;

@Slf4j
public abstract class BaseAcsClient {

    protected final AcsAuthClient authClient;
    protected final OkHttpClient httpClient;
    protected final ObjectMapper objectMapper;
    private final AcsErrorResponseParser errorParser;

    @Inject
    public BaseAcsClient(AcsAuthClient authClient, AcsErrorResponseParser errorParser) {
        this.authClient = authClient;
        this.httpClient = authClient.getHttpClient();
        this.objectMapper = authClient.getObjectMapper();
        this.errorParser = errorParser;
    }

    protected Response executePost(String endpoint, Object body) throws AcsApiException, IOException {
        return executeRequest(endpoint, "POST", body, 0);
    }

    protected Response executePatch(String endpoint, Object body) throws AcsApiException, IOException {
        return executeRequest(endpoint, "PATCH", body, 0);
    }

    protected Response executeDelete(String endpoint) throws AcsApiException, IOException {
        return executeRequest(endpoint, "DELETE", null, 0);
    }

    protected Response executeGet(String endpoint) throws AcsApiException, IOException {
        return executeRequest(endpoint, "GET", null, 0);
    }


    private Response executeRequest(String endpoint, String method, Object body, int retryCount) throws AcsApiException, IOException {
        try {
            Request request = buildRequest(endpoint, method, body);
            Response response = httpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                log.debug("{} {} success", method, endpoint);
                return response;
            }
            return handleErrorResponse(response, endpoint, method, body, retryCount);
        } catch (IOException e) {
            log.error("{} {} IOException: {}", method, endpoint, e.getMessage());
            throw e;
        }
    }

    private Request buildRequest(String endpoint, String method, Object body) throws AcsApiException, IOException {
        Request.Builder builder = new Request.Builder()
                .url(API_BASE_URL + endpoint)
                .header("Authorization", "Bearer " + authClient.getAccessToken())
                .header("Content-Type", "application/json");
        switch (method) {
            case "GET":
                builder.get();
                break;

            case "POST":
                builder.post(createRequestBody(body));
                break;

            case "PUT":
                builder.put(createRequestBody(body));
                break;

            case "PATCH":
                builder.patch(createRequestBody(body));
                break;

            case "DELETE":
                builder.delete();
                break;

            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
        return builder.build();
    }

    private RequestBody createRequestBody(Object body) throws IOException {
        if (body == null) {
            return RequestBody.create("", MediaType.parse("application/json"));
        }
        String json = objectMapper.writeValueAsString(body);
        return RequestBody.create(json, MediaType.parse("application/json"));
    }

    private Response handleErrorResponse(Response response, String endpoint, String method,
                                         Object body, int retryCount) throws  AcsApiException, IOException {
        int httpStatus = response.code();
        String responseBody = getResponseBody(response);
        AcsApiException error = errorParser.parseError(responseBody, httpStatus);

        if (error == null) {
            error = errorParser.createDefaultError(httpStatus, responseBody);
        }
        AcsErrorCode errorCode = error.getErrorCode();

        if (errorCode == AcsErrorCode.ERR_AUTH_INVALID_TOKEN) {
            authClient.refreshAccessToken();
            if (retryCount == 0) {
                return executeRequest(endpoint, method, body, retryCount + 1);
            } else {
                throw error;
            }
        }
        if (error.isRetryable()){
            return executeRequest(endpoint, method, body, retryCount + 1);
        }
        throw error;
    }


    protected String getResponseBody(Response response) throws IOException {
        try {
            return response.body().string();
        } finally {
            response.close();
        }
    }

    protected void validateResponse(Response response, String operation) throws IOException {
        if (!response.isSuccessful()) {
            String errorBody = getResponseBody(response);
            log.error("{} failed: {} - {}", operation, response.code(), errorBody);
            throw new RuntimeException(operation + " failed: " + response.code() + " - " + errorBody);
        }
    }
}
