package com.axelor.service;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public interface DeviceService {
    void saveAndUpdateDevice(ActionRequest request, ActionResponse response);

    void deleteListDevices(ActionRequest request, ActionResponse response);

    void deleteDevice(ActionRequest request, ActionResponse response);
}
