package com.axelor.service;

import java.util.Map;

public interface DahuaAutoRegisterService {

  boolean startListen(String ip, int port);

  boolean stopListen();

  boolean isListening();

  long getListenHandle();

  Map<String, Object> status();
}
