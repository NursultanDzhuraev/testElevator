package com.axelor.service.impl.dahua;

import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ReflectiveDahuaNetSdkBridge implements DahuaNetSdkBridge {

  private Object netSdkInstance;
  private Class<?> lLongClass;

  @Override
  public boolean init() {
    try {
      if (netSdkInstance == null && !loadSdk()) {
        return false;
      }
      Method initMethod = netSdkInstance.getClass().getMethod("CLIENT_Init", Object.class, Object.class);
      Object result = initMethod.invoke(netSdkInstance, null, null);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.error("Dahua SDK init failed", e);
      return false;
    }
  }

  @Override
  public long listenServer(String ip, int port) {
    try {
      if (netSdkInstance == null && !loadSdk()) {
        return 0L;
      }
      Method method =
          netSdkInstance
              .getClass()
              .getMethod("CLIENT_ListenServer", String.class, int.class, int.class, Object.class, Object.class);
      Object handle = method.invoke(netSdkInstance, ip, port, 1000, null, null);
      return asLongHandle(handle);
    } catch (NoSuchMethodException e) {
      try {
        Method method =
            netSdkInstance
                .getClass()
                .getMethod("CLIENT_ListenServer", String.class, int.class, int.class, Object.class);
        Object handle = method.invoke(netSdkInstance, ip, port, 1000, null);
        return asLongHandle(handle);
      } catch (Exception inner) {
        log.error("Dahua SDK listen server failed", inner);
        return 0L;
      }
    } catch (Exception e) {
      log.error("Dahua SDK listen server failed", e);
      return 0L;
    }
  }

  @Override
  public boolean stopListenServer(long handle) {
    try {
      if (netSdkInstance == null && !loadSdk()) {
        return false;
      }
      Object lLongHandle = newHandle(handle);
      Method method = netSdkInstance.getClass().getMethod("CLIENT_StopListenServer", lLongClass);
      Object result = method.invoke(netSdkInstance, lLongHandle);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.error("Dahua SDK stop listen failed", e);
      return false;
    }
  }

  @Override
  public boolean cleanup() {
    try {
      if (netSdkInstance == null) {
        return true;
      }
      Method method = netSdkInstance.getClass().getMethod("CLIENT_Cleanup");
      Object result = method.invoke(netSdkInstance);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.error("Dahua SDK cleanup failed", e);
      return false;
    }
  }

  @Override
  public boolean isAvailable() {
    return loadSdk();
  }

  private boolean loadSdk() {
    try {
      if (netSdkInstance != null) {
        return true;
      }
      Class<?> netSdkClass = Class.forName("com.netsdk.lib.NetSDKLib");
      lLongClass = Class.forName("com.netsdk.lib.NetSDKLib$LLong");
      netSdkInstance = netSdkClass.getField("NETSDK_INSTANCE").get(null);
      return netSdkInstance != null;
    } catch (Exception e) {
      log.warn("Dahua NetSDK classes not found in classpath");
      return false;
    }
  }

  private long asLongHandle(Object handleObj) throws Exception {
    if (handleObj == null) {
      return 0L;
    }
    Method longValueMethod = handleObj.getClass().getMethod("longValue");
    Object value = longValueMethod.invoke(handleObj);
    return value instanceof Number ? ((Number) value).longValue() : 0L;
  }

  private Object newHandle(long handle) throws Exception {
    Constructor<?> ctor = lLongClass.getConstructor(long.class);
    return ctor.newInstance(handle);
  }
}
