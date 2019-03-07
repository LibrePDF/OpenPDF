package com.lowagie.text.factories;

import static com.lowagie.text.factories.RomanNumberFactory.getString;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RomanNumberFactoryTest {

  @Test
  void shouldGetRomanNumberString() {
    for (int i = 1; i < 2000; i++) {
      assertNotNull(getString(i));
    }
  }

  /**
   * Tests a few random roman numerals
   */
  @Test
  void shouldGetRomanNumeralRepresentation() {
    assertEquals("iv", getString(4));
    assertEquals("lvi", getString(56));
    assertEquals("cd", getString(400));
    assertEquals("mmcmxcix", getString(2999));
    assertEquals("mmm", getString(3000));
  }
}
