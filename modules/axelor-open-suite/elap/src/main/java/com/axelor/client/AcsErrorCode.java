package com.axelor.client;

import lombok.Getter;

public enum AcsErrorCode {
    ERR_AUTH_MISSING_TOKEN("AUTH", true, false),
    ERR_AUTH_INVALID_TOKEN("AUTH", true, false),
    ERR_ACCESS_DENIED("AUTH", false, true),
    ERR_VALIDATION_ERROR("VALIDATION", false, true),
    ERR_VALIDATION_REQUIRED_FIELD("VALIDATION", false, true),
    ERR_VALIDATION_ENUM_VALUE("VALIDATION", false, true),
    ERR_VALIDATION_IMAGE_FORMAT("VALIDATION", false, true),
    ERR_VALIDATION_IMAGE_SIZE("VALIDATION", false, true),
    ERR_VALIDATION_PASSWORD_DIGITS("VALIDATION", false, true),
    ERR_ACCESS_REVOKED("VALIDATION", false, false),
    ERR_USER_ALREADY_EXISTS("BUSINESS", false, true),
    ERR_CARD_ALREADY_USED("BUSINESS", false, true),
    ERR_USER_NOT_FOUND("BUSINESS", false, true),
    ERR_PERMISSION_NOT_FOUND("BUSINESS", false, true),
    ERR_DEVICE_ALREADY_EXISTS("BUSINESS", false, true),
    ERR_DEVICE_NOT_FOUND("BUSINESS", false, true),
    ERR_INTERNAL_SERVER("SYSTEM", true, false),
    ERR_DEVICE_CONNECTION("SYSTEM", true, false),
    ERR_DEVICE_AUTH_FAILED("SYSTEM", false, false),
    ERR_SYNC_QUEUE_FULL("SYSTEM", true, false),
    UNKNOWN("UNKNOWN", false, true);

    @Getter
    private final String category;
    @Getter
    private final boolean retryable;
    private final boolean sendToDeadLetter;

    AcsErrorCode(String category, boolean retryable, boolean sendToDeadLetter) {
        this.category = category;
        this.retryable = retryable;
        this.sendToDeadLetter = sendToDeadLetter;
    }

    public boolean shouldSendToDeadLetter() {
        return sendToDeadLetter;
    }

    public static AcsErrorCode fromString(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        try {
            return AcsErrorCode.valueOf(code);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public boolean isAuthError() {
        return "AUTH".equals(category);
    }
    public boolean isValidationError() {
        return "VALIDATION".equals(category);
    }

    public boolean isBusinessError() {
        return "BUSINESS".equals(category);
    }

    public boolean isSystemError() {
        return "SYSTEM".equals(category);
    }
}
