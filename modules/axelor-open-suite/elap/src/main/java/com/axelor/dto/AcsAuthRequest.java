package com.axelor.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcsAuthRequest {
  @NotBlank private String username;
  @NotBlank private String password;
}
