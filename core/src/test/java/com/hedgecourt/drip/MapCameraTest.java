package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MapCameraTest {
  private static MapCamera camera(int mapW, int mapH, float viewW, float viewH) {
    MapCamera c = new MapCamera();
    c.setViewport(viewW, viewH);
    c.setMap(mapW, mapH);
    return c;
  }

  @Test
  void minZoomFitsWholeMap() {
    MapCamera c = camera(128, 64, 1024, 768);
    assertEquals(8f, c.minPixelsPerTile(), 1e-4);
    assertEquals(8f, c.pixelsPerTile(), 1e-4);
    // Wider than tall map: whole width visible, height centered.
    assertEquals(0, c.firstVisibleX());
    assertEquals(127, c.lastVisibleX());
    assertEquals(0, c.firstVisibleY());
    assertEquals(63, c.lastVisibleY());
  }

  @Test
  void zoomIsClampedBetweenFitAndMax() {
    MapCamera c = camera(100, 100, 800, 800);
    c.zoomAt(0.01f, 400, 400);
    assertEquals(8f, c.pixelsPerTile(), 1e-4);
    c.zoomAt(1000f, 400, 400);
    assertEquals(MapCamera.MAX_PIXELS_PER_TILE, c.pixelsPerTile(), 1e-4);
  }

  @Test
  void smallMapNeverZoomsPastMax() {
    MapCamera c = camera(4, 4, 1000, 1000);
    assertEquals(MapCamera.MAX_PIXELS_PER_TILE, c.minPixelsPerTile(), 1e-4);
  }

  @Test
  void panClampsToMapEdges() {
    MapCamera c = camera(100, 100, 800, 800);
    c.zoomAt(4f, 400, 400); // 32 px/tile, view is 25x25 tiles
    c.panPixels(1_000_000f, 1_000_000f);
    assertEquals(12.5f, c.centerX(), 1e-3);
    assertEquals(12.5f, c.centerY(), 1e-3);
    assertEquals(0, c.firstVisibleX());
    c.panPixels(-1_000_000f, -1_000_000f);
    assertEquals(87.5f, c.centerX(), 1e-3);
    assertEquals(87.5f, c.centerY(), 1e-3);
    assertEquals(99, c.lastVisibleX());
  }

  @Test
  void mapSmallerThanViewIsCenteredAndNotPannable() {
    MapCamera c = camera(100, 10, 800, 800); // fits by width, leaving vertical slack
    c.panPixels(0, 500);
    assertEquals(5f, c.centerY(), 1e-4);
  }

  @Test
  void zoomKeepsPointUnderCursorFixed() {
    MapCamera c = camera(100, 100, 800, 800);
    c.zoomAt(4f, 400, 400);
    float before = c.worldX(300);
    c.zoomAt(2f, 300, 200);
    assertEquals(before, c.worldX(300), 1e-3);
  }

  @Test
  void screenToTileRoundTrip() {
    MapCamera c = camera(20, 10, 400, 200);
    assertEquals(0, c.tileX(5));
    assertEquals(19, c.tileX(399));
    assertEquals(0, c.tileY(5));
    assertEquals(9, c.tileY(199));
    assertTrue(c.tileX(-5) < 0);
    assertEquals(c.screenX(3), 60f, 1e-3);
  }

  @Test
  void resizeKeepsViewValid() {
    MapCamera c = camera(100, 100, 800, 800);
    c.zoomAt(4f, 400, 400);
    c.panPixels(-1_000_000f, 0);
    c.setViewport(400, 400);
    assertTrue(c.pixelsPerTile() >= c.minPixelsPerTile());
    assertTrue(c.lastVisibleX() <= 99);
  }
}
