package com.axelor.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.service.impl.dahua.DahuaNetSdkBridge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DahuaAutoRegisterServiceImplTest {

  private DahuaNetSdkBridge bridge;
  private DahuaAutoRegisterServiceImpl service;

  @BeforeEach
  void setUp() {
    bridge = mock(DahuaNetSdkBridge.class);
    service = new DahuaAutoRegisterServiceImpl(bridge);
  }

  @Test
  void shouldStartListenWhenInitAndListenSucceed() {
    when(bridge.init()).thenReturn(true);
    when(bridge.listenServer("127.0.0.1", 9500)).thenReturn(1001L);

    boolean started = service.startListen("127.0.0.1", 9500);

    assertTrue(started);
    assertTrue(service.isListening());
    assertEquals(1001L, service.getListenHandle());
  }

  @Test
  void shouldFailStartWhenSdkInitFails() {
    when(bridge.init()).thenReturn(false);

    boolean started = service.startListen("127.0.0.1", 9500);

    assertFalse(started);
    assertFalse(service.isListening());
  }

  @Test
  void shouldStopListenAndCleanup() {
    when(bridge.init()).thenReturn(true);
    when(bridge.listenServer("127.0.0.1", 9500)).thenReturn(888L);
    when(bridge.stopListenServer(888L)).thenReturn(true);
    when(bridge.cleanup()).thenReturn(true);

    service.startListen("127.0.0.1", 9500);
    boolean stopped = service.stopListen();

    assertTrue(stopped);
    assertFalse(service.isListening());
    verify(bridge).stopListenServer(888L);
    verify(bridge).cleanup();
  }
}
