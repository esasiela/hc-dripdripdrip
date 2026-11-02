package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Test
  void highlightWidthIsConstantButNeverExceedsHalfTile() {
    assertEquals(MapRenderer.HIGHLIGHT_PIXELS, MapRenderer.highlightWidth(64f));
    assertEquals(MapRenderer.HIGHLIGHT_PIXELS, MapRenderer.highlightWidth(6f));
    assertEquals(1f, MapRenderer.highlightWidth(2f));
    assertEquals(0.5f, MapRenderer.highlightWidth(1f));
  }

  @Test
  void gridShownOnlyWhenEnabledAndBigEnough() {
    ViewSettings settings = new ViewSettings();
    assertTrue(MapRenderer.gridShown(settings, MapRenderer.MIN_GRID_PIXELS_PER_TILE));
    assertFalse(MapRenderer.gridShown(settings, MapRenderer.MIN_GRID_PIXELS_PER_TILE - 0.1f));
    settings.showGrid = false;
    assertFalse(MapRenderer.gridShown(settings, MapCamera.MAX_PIXELS_PER_TILE));
  }
}
