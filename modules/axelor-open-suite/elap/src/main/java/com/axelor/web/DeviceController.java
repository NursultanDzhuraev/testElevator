package com.axelor.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.DeviceService;
import com.google.inject.Inject;

public class DeviceController {
    private final DeviceService deviceService;

    @Inject
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public void saveDevice(ActionRequest request, ActionResponse response) {
        deviceService.saveAndUpdateDevice(request, response);
    }

    public void deleteListDevice(ActionRequest request, ActionResponse response){
        deviceService.deleteListDevices(request,response);
    }

    public void deleteDevice(ActionRequest request, ActionResponse response){
        deviceService.deleteDevice(request,response);
    }
}
