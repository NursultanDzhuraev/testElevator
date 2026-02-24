package com.axelor.mapper;

import com.axelor.apps.erp.db.Building;
import com.axelor.apps.erp.db.Device;
import com.axelor.dto.BuildingDtoRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildingMapper {

    public BuildingDtoRequest toDto(Building building) {
        if (building.getName() == null || building.getUuid() == null) {
            throw new IllegalArgumentException("Name and id must not be empty");
        }

        Map<String, Object> metadataMap = null;
        List<String> ids = new ArrayList<>();
        List<Device> device = building.getDevice();
        if (device != null) {
            for (Device device1 : device) {
                ids.add(device1.getUuid());
            }
        }
        return BuildingDtoRequest.builder()
                .locationId(building.getUuid())
                .name(building.getName())
                .address(building.getAddress() == null ? "" : building.getAddress())
                .devicesId(ids)
                .latitude(building.getLatitude())
                .longitude(building.getLongitude())
                .timezone(String.valueOf(building.getTimezone() == null ? 0 : building.getTimezone()))
                .metadata(metadataMap)
                .build();
    }
}
