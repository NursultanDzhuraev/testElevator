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
public class PartnerDtoRequest {
  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("full_name")
  private String fullName;

  @JsonProperty("user_role")
  private String userRole; // resident, employee, guest, courier, service, security, admin

  @JsonProperty("devices_config")
  private List<DeviceConfigDto> devicesConfig;

  @JsonProperty("pin_code")
  private String pinCode;

  private String email;

  private String phone;

  @JsonProperty("image_base64")
  private String imageBase64;

  @JsonProperty("card_number")
  private String cardNumber;

  @JsonProperty("qr_code")
  private String qrCode;

  private String status; // active, inactive, expired

  private Map<String, Object> metadata;
}
