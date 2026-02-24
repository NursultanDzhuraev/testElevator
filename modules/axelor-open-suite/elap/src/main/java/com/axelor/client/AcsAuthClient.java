package com.axelor.client;

import com.axelor.dto.AcsAuthRequest;
import com.axelor.dto.AcsTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import static com.axelor.client.ACSConfig.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class AcsAuthClient {
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Getter
    private String accessToken;
    @Getter
    private String refreshToken;

    @Inject
    public AcsAuthClient() {
        this.httpClient =
                new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        authenticate();
    }

    private void authenticate() {
        try {
            Map<String, String> body = Map.of("username", API_USERNAME, "password", API_PASSWORD);

            String json = objectMapper.writeValueAsString(body);

            Request request =
                    new Request.Builder()
                            .url(API_BASE_URL + API_TOKEN_URL)
                            .post(RequestBody.create(json, MediaType.parse("application/json")))
                            .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Authorization unsuccessful: " + response.code());
                }

                String responseBody = response.body().string();

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode data = root.get("data");

                this.accessToken = data.get("access").asText();
                this.refreshToken = data.get("refresh").asText();

                log.info("Authorization to ACS successful");
            }
        } catch (Exception e) {
            log.error("Authorization error", e);
            throw new RuntimeException("Authorization error", e);
        }
    }

    public void refreshAccessToken() {

        try {
            String json = objectMapper.writeValueAsString(new AcsAuthRequest("refresh", refreshToken));

            Request request =
                    new Request.Builder()
                            .url(API_BASE_URL + API_REFRESH_TOKEN_URL)
                            .post(RequestBody.create(json, MediaType.parse("application/json")))
                            .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    AcsTokenResponse tokenResponse =
                            objectMapper.readValue(responseBody, AcsTokenResponse.class);

                    this.accessToken = tokenResponse.getAccess_token();
                    this.refreshToken = tokenResponse.getRefresh_token();
                } else {
                    authenticate();
                }
            }
        } catch (IOException e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            authenticate();
        }
    }


    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
