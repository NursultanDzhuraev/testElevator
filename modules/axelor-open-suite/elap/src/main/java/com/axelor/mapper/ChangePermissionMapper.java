package com.axelor.mapper;

import com.axelor.dto.DeviceConfigDto;
import com.axelor.dto.PartnerChangePermissionDto;


import java.util.List;

public class ChangePermissionMapper {
    public PartnerChangePermissionDto changeDebtStatusDto(List<DeviceConfigDto> deviceConfigDtoList) {

        return PartnerChangePermissionDto.builder()
                .devicesConfig(deviceConfigDtoList)
                .build();
    }
}
