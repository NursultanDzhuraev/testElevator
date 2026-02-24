package com.axelor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDtoRequest {
    @JsonProperty("device_id")
    private String deviceId;

    private String name;

    private String ip;

    private String login;

    private String password;

    @JsonProperty("location_id")
    private String locationId;

    @JsonProperty("device_type")
    private String deviceType; // lift, door, gate, turnstile, intercom, barrier, parking

    @JsonProperty("users_access")
    private List<UserAccessConfig> usersAccess;

    private String host;

    private String protocol; // ssh, http, https, tcp, udp, api

    private Integer port;

    private String model;

    private String firmware;

    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonProperty("is_active")
    private Boolean isActive;

    private Map<String, Object> metadata;
}
