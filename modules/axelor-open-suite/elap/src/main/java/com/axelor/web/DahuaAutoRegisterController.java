package com.axelor.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.DahuaAutoRegisterService;
import com.google.inject.Inject;
import java.util.Map;

public class DahuaAutoRegisterController {

  private final DahuaAutoRegisterService dahuaAutoRegisterService;

  @Inject
  public DahuaAutoRegisterController(DahuaAutoRegisterService dahuaAutoRegisterService) {
    this.dahuaAutoRegisterService = dahuaAutoRegisterService;
  }

  public void startListen(ActionRequest request, ActionResponse response) {
    String ip = request.getContext().get("listenIp") != null ? request.getContext().get("listenIp").toString() : null;
    int port = parsePort(request.getContext().get("listenPort"));

    boolean started = dahuaAutoRegisterService.startListen(ip, port);
    response.setValue("$dahuaStatus", dahuaAutoRegisterService.status());
    response.setNotify(started ? "Dahua auto-register listening started" : "Failed to start Dahua auto-register listening");
  }

  public void stopListen(ActionRequest request, ActionResponse response) {
    boolean stopped = dahuaAutoRegisterService.stopListen();
    response.setValue("$dahuaStatus", dahuaAutoRegisterService.status());
    response.setNotify(stopped ? "Dahua auto-register listening stopped" : "Failed to stop Dahua auto-register listening");
  }

  public void status(ActionRequest request, ActionResponse response) {
    Map<String, Object> status = dahuaAutoRegisterService.status();
    response.setValue("$dahuaStatus", status);
  }

  private int parsePort(Object portObj) {
    if (portObj == null) {
      return 0;
    }
    try {
      return Integer.parseInt(portObj.toString());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
