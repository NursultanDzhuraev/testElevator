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
class UserAccessConfig {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("permission_id")
    private String permissionId;
}
