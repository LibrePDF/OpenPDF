package com.lowagie.text.factories;

import org.junit.jupiter.api.Test;

import static com.lowagie.text.factories.GreekAlphabetFactory.getString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GreekAlphabetFactoryTest {

  @Test
  void shouldGetGreekString() {
    for (int i = 1; i < 1000; i++) {
      assertNotNull(getString(i));
    }
  }

}
