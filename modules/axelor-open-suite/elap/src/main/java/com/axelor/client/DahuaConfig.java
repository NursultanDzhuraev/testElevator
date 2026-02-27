package com.axelor.client;

import com.axelor.app.AppSettings;

public final class DahuaConfig {

  private static final AppSettings SETTINGS = AppSettings.get();

  private DahuaConfig() {}

  public static String listenIp() {
    return SETTINGS.get("axelor.dahua.listen.ip", "0.0.0.0");
  }

  public static int listenPort() {
    return Integer.parseInt(SETTINGS.get("axelor.dahua.listen.port", "9500"));
  }
}
