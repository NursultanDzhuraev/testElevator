package com.axelor.mapper;

import com.axelor.apps.erp.db.Building;
import com.axelor.apps.erp.db.Device;
import com.axelor.apps.erp.db.Entrance;
import com.axelor.dto.DeviceDtoRequest;

import java.util.ArrayList;

public class DeviceMapper {
    public DeviceDtoRequest toDto(Device device) {
        Entrance entrance = device.getEntrance();
        Building building = entrance.getBuilding();
        return DeviceDtoRequest.builder()
                .deviceId(device.getUuid())
                .name(device.getName())
                .ip(device.getIp())
                .login(device.getLogin())
                .password(device.getPassword())
                .usersAccess(new ArrayList<>())
                .locationId(building.getUuid())
                .deviceType(device.getDeviceType())
                .host(device.getHost() == null ? "" : device.getHost())
                .protocol(device.getProtocol() == null ? "" : device.getProtocol())
                .port(device.getPort() == null ? 80 : device.getPort())
                .model(device.getModel() == null ? "" : device.getModel())
                .firmware(device.getFirmware() == null ? "" : device.getFirmware())
                .serialNumber(device.getSerialNumber() == null ? "" : device.getSerialNumber())
                .isActive(device.getIsActive())
                .metadata(null)
                .build();
    }
}
