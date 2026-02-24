package com.axelor.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AcsErrorResponseParser {
    private final ObjectMapper objectMapper;
    @Inject
    public AcsErrorResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AcsApiException parseError(String responseBody, int httpStatus) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            boolean success = root.has("success") && root.get("success").asBoolean();
            if (success) {
                return null;
            }
            if (!root.has("error")) {
                return new AcsApiException(AcsErrorCode.UNKNOWN, "Unknown error" + responseBody, null, httpStatus);
            }
            JsonNode error = root.get("error");
            String codeStr = error.has("code") ? error.get("code").asText() : "UNKNOWN";
            AcsErrorCode errorCode = AcsErrorCode.fromString(codeStr);
            String message = error.has("message") ? error.get("message").asText() : "No message";
            Map<String, Object> details = null;
            if (error.has("details") && !error.get("details").isNull()) {
                details = parseDetails(error.get("details"));
            }
            return new AcsApiException(errorCode, message, details, httpStatus);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> parseDetails(JsonNode detailsNode) {
        Map<String, Object> details = new HashMap<>();

        detailsNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (value.isArray()) {
                if (value.size() > 0) {
                    details.put(key, value.get(0).asText());
                }
            } else {
                details.put(key, value.asText());
            }
        });

        return details;
    }

    public AcsApiException createDefaultError(int httpStatus, String responseBody) {

        AcsErrorCode errorCode;
        String message;

        switch (httpStatus) {
            case 401:
                errorCode = AcsErrorCode.ERR_AUTH_INVALID_TOKEN;
                message = "Authentication failed";
                break;

            case 403:
                errorCode = AcsErrorCode.ERR_ACCESS_DENIED;
                message = "Access denied";
                break;

            case 404:
                errorCode = AcsErrorCode.ERR_USER_NOT_FOUND;
                message = "Resource not found";
                break;

            case 409:
                errorCode = AcsErrorCode.ERR_USER_ALREADY_EXISTS;
                message = "Resource already exists";
                break;

            case 500:
                errorCode = AcsErrorCode.ERR_INTERNAL_SERVER;
                message = "Internal server error";
                break;

            case 503:
                errorCode = AcsErrorCode.ERR_DEVICE_CONNECTION;
                message = "Service unavailable";
                break;

            default:
                errorCode = AcsErrorCode.UNKNOWN;
                message = "Unknown error: HTTP " + httpStatus;
        }

        return new AcsApiException(errorCode, message, null, httpStatus);
    }
}
