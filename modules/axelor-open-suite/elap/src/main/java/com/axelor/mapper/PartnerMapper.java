package com.axelor.mapper;

import com.axelor.apps.base.db.Partner;
import com.axelor.dto.DeviceConfigDto;
import com.axelor.dto.PartnerDtoRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import com.axelor.meta.MetaFiles;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartnerMapper {

    public PartnerDtoRequest toDto(Partner partner) {
        try {

            String base64Image = null;
            if (partner.getPicture() != null) {
                try {
                    Path path = MetaFiles.getPath(partner.getPicture());
                    byte[] imageBase = Files.readAllBytes(path);
                    base64Image = Base64.getEncoder().encodeToString(imageBase);
                } catch (Exception e) {
                    log.error("Error processing image: {}", e.getMessage());
                    base64Image = null;
                }
            }
            return PartnerDtoRequest.builder()
                    .userId(partner.getUuid())
                    .fullName(partner.getName())
                    .userRole(partner.getPartnerType())
                    .devicesConfig(getDeviceConfigForPartner(partner))
                    .imageBase64(base64Image == null ? "" : base64Image)
                    .qrCode("")
                    .cardNumber(partner.getCardNumber())
                    .pinCode(String.valueOf(partner.getPinCode()))
                    .email(partner.getEmail() == null ? "" : partner.getEmail())
                    .phone(partner.getPhone() == null ? "" : partner.getPhone())
                    .status(partner.getStatus())
                    .metadata(null)
                    .build();

        } catch (Exception e) {
            log.error("Failed to convert partner to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert partner to DTO: " + e.getMessage(), e);
        }
    }

    public List<DeviceConfigDto> getDeviceConfigForPartner(Partner partner) {
        if (partner.getAccessCredentials() == null
                || partner.getAccessCredentials().isEmpty()) {
            return new ArrayList<>();
        }

        return partner.getAccessCredentials().stream()
                .filter(adp -> adp.getDevice() != null && adp.getAccessRights() != null)
                .map(adp -> DeviceConfigDto.builder()
                        .deviceId(adp.getDevice().getUuid())
                        .permissionId(adp.getAccessRights().getUuid())
                        .build())
                .collect(Collectors.toList());
    }
}
