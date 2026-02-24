package com.axelor.client;


import lombok.Getter;

import java.util.Map;
@Getter
public class AcsApiException  extends Exception{
    public final AcsErrorCode errorCode;
    private final String errorMessage;
    private final Map<String,Object> details;
    private final int httpStatus;


    public AcsApiException(AcsErrorCode errorCode, String errorMessage, Map<String, Object> details, int httpStatus) {
        super(String.format("[%s] %s", errorCode, errorMessage));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    public AcsApiException (AcsErrorCode errorCode, String errorMessage){
        this(errorCode, errorMessage, null, 0);
    }

    public boolean isRetryable(){
       return errorCode.isRetryable();
    }
    public boolean shouldSendToDeadLetter(){
      return   errorCode.shouldSendToDeadLetter();
    }
    public boolean isAuthError() {
        return errorCode.isAuthError();
    }
    public boolean isValidationError() {
        return errorCode.isValidationError();
    }
    public boolean isBusinessError() {
        return errorCode.isBusinessError();
    }
    public boolean isSystemError() {
        return errorCode.isSystemError();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AcsApiException{");
        sb.append("code=").append(errorCode);
        sb.append(", message='").append(errorMessage).append('\'');
        sb.append(", httpStatus=").append(httpStatus);
        if (details != null && !details.isEmpty()) {
            sb.append(", details=").append(details);
        }
        sb.append('}');
        return sb.toString();
    }
}