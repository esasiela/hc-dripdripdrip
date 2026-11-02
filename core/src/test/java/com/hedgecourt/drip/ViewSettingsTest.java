package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ViewSettingsTest {
  @Test
  void gridShownByDefault() {
    assertTrue(new ViewSettings().showGrid);
  }
}
