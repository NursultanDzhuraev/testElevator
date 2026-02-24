package com.axelor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessRightDtoRequest {
    @JsonProperty("permission_id")
    private String permissionId;

    @JsonProperty("permission_name")
    private String permissionName;

    @JsonProperty("permission_type")
    private String permissionType; // full_access, custom_access, empty_access

    @JsonProperty("custom_permission")
    private Map<String, Object> customPermission;

    @JsonProperty("valid_from")
    private ZonedDateTime validFrom;

    @JsonProperty("valid_until")
    private ZonedDateTime validUntil;
}
