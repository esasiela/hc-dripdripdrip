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
  void waterAlphaIsZeroWhenDry() {
    assertEquals(0f, MapRenderer.waterAlpha(0f));
  }

  @Test
  void waterAlphaHasMinimumForAnyWater() {
    assertTrue(MapRenderer.waterAlpha(0.0001f) >= MapRenderer.WATER_MIN_ALPHA);
  }

  @Test
  void waterAlphaNeverDecreasesAndIsCapped() {
    float previous = 0f;
    for (float depth = 0f; depth <= 10f; depth += 0.01f) {
      float alpha = MapRenderer.waterAlpha(depth);
      assertTrue(alpha >= previous);
      assertTrue(alpha <= MapRenderer.WATER_MAX_ALPHA);
      previous = alpha;
    }
    assertEquals(MapRenderer.WATER_MAX_ALPHA, MapRenderer.waterAlpha(100f));
    assertEquals(
        MapRenderer.WATER_MAX_ALPHA, MapRenderer.waterAlpha(MapRenderer.WATER_FULL_ALPHA_DEPTH));
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
