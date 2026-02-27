package com.axelor.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class GeneratorIdTest {

  @Test
  void shouldFormatIdWithUpperPrefixAndPadding() throws Exception {
    GeneratorId generatorId = new GeneratorId();
    Method formatId = GeneratorId.class.getDeclaredMethod("formatId", String.class, long.class);
    formatId.setAccessible(true);

    String result = (String) formatId.invoke(generatorId, "device", 123L);

    assertEquals("DEV-000123", result);
  }

  @Test
  void shouldUseWholeKeyWhenShorterThanThreeChars() throws Exception {
    GeneratorId generatorId = new GeneratorId();
    Method formatId = GeneratorId.class.getDeclaredMethod("formatId", String.class, long.class);
    formatId.setAccessible(true);

    String result = (String) formatId.invoke(generatorId, "ac", 5L);

    assertEquals("AC-000005", result);
  }
}
