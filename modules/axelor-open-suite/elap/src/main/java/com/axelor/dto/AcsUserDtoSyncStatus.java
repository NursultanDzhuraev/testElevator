package com.axelor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcsUserDtoSyncStatus {
    @JsonProperty("device_name")
    private String deviceName;
    @JsonProperty("permission_name")
    private String permissionName;
    @JsonProperty("synchronized_at")
    private LocalDateTime synchronizedAt;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}



