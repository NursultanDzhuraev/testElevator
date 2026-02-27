package com.axelor.service.impl.dahua;

public interface DahuaNetSdkBridge {

  boolean init();

  long listenServer(String ip, int port);

  boolean stopListenServer(long handle);

  boolean cleanup();

  boolean isAvailable();
}
