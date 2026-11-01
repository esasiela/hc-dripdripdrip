package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MapRendererTest {
  @Test
  void gridHiddenBelowThreshold() {
    assertFalse(MapRenderer.gridVisible(MapRenderer.MIN_GRID_PIXELS_PER_TILE - 0.1f));
    assertTrue(MapRenderer.gridVisible(MapRenderer.MIN_GRID_PIXELS_PER_TILE));
    assertTrue(MapRenderer.gridVisible(MapCamera.MAX_PIXELS_PER_TILE));
  }
}
