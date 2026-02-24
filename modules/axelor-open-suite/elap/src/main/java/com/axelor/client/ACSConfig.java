package com.axelor.client;

import com.axelor.app.AppSettings;

public class ACSConfig {

    private static final AppSettings settings = AppSettings.get();


    public static final String API_BASE_URL = settings.get("axelor.api.baseUrl");
    public static final String API_USERNAME = settings.get("axelor.api.username");
    public static final String API_PASSWORD = settings.get("axelor.api.password");

    public static final String API_LOCATION_URL = "/api/v2/locations/";
    public static final String API_DEVICE_URL = "/api/v2/devices/";
    public static final String API_ACCESS_RIGHT_URL = "/api/v2/access-rights/";
    public static final String API_USER_URL = "/api/v2/users/";

    public static final String API_TOKEN_URL = "/api/token/";
    public static final String API_REFRESH_TOKEN_URL = "/api/token/refresh/";

}
