package com.axelor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceConfigDto {
    @JsonProperty("device_id")
    private String deviceId;
    @JsonProperty("permission_id")
    private String permissionId;
}
