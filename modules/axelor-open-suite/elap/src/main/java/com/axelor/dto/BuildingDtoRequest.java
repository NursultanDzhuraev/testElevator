package com.axelor.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildingDtoRequest {
  @JsonProperty("location_id")
  private String locationId;

  private String name;

  private String address;

  @JsonProperty("devices_id")
  private List<String> devicesId;

  private BigDecimal latitude;

  private BigDecimal longitude;

  private String timezone;

  private Map<String, Object> metadata;
}
