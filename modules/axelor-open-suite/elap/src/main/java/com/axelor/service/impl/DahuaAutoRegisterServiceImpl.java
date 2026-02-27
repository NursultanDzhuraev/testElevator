package com.axelor.service.impl;

import com.axelor.client.DahuaConfig;
import com.axelor.service.DahuaAutoRegisterService;
import com.axelor.service.impl.dahua.DahuaNetSdkBridge;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DahuaAutoRegisterServiceImpl implements DahuaAutoRegisterService {

  private final DahuaNetSdkBridge dahuaBridge;
  private volatile long listenHandle;
  private volatile String listenIp;
  private volatile int listenPort;
  private volatile LocalDateTime startedAt;

  @Inject
  public DahuaAutoRegisterServiceImpl(DahuaNetSdkBridge dahuaBridge) {
    this.dahuaBridge = dahuaBridge;
  }

  @Override
  public synchronized boolean startListen(String ip, int port) {
    if (isListening()) {
      return true;
    }
    String bindIp = (ip == null || ip.isBlank()) ? DahuaConfig.listenIp() : ip;
    int bindPort = port <= 0 ? DahuaConfig.listenPort() : port;

    if (!dahuaBridge.init()) {
      log.error("Failed to initialize Dahua SDK");
      return false;
    }

    long handle = dahuaBridge.listenServer(bindIp, bindPort);
    if (handle == 0L) {
      log.error("Failed to start Dahua auto-register server on {}:{}", bindIp, bindPort);
      dahuaBridge.cleanup();
      return false;
    }

    listenHandle = handle;
    listenIp = bindIp;
    listenPort = bindPort;
    startedAt = LocalDateTime.now();
    log.info("Dahua auto-register listen started on {}:{} (handle={})", bindIp, bindPort, handle);
    return true;
  }

  @Override
  public synchronized boolean stopListen() {
    if (!isListening()) {
      return true;
    }

    boolean stopOk = dahuaBridge.stopListenServer(listenHandle);
    boolean cleanupOk = dahuaBridge.cleanup();

    if (stopOk) {
      log.info("Dahua auto-register listen stopped (handle={})", listenHandle);
    }

    listenHandle = 0L;
    listenIp = null;
    listenPort = 0;
    startedAt = null;

    return stopOk && cleanupOk;
  }

  @Override
  public boolean isListening() {
    return listenHandle > 0L;
  }

  @Override
  public long getListenHandle() {
    return listenHandle;
  }

  @Override
  public Map<String, Object> status() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("sdkAvailable", dahuaBridge.isAvailable());
    payload.put("isListening", isListening());
    payload.put("listenHandle", listenHandle);
    payload.put("listenIp", listenIp);
    payload.put("listenPort", listenPort);
    payload.put("startedAt", startedAt != null ? startedAt.toString() : null);
    return payload;
  }
}
