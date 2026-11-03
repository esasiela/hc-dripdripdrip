package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.hedgecourt.drip.sim.Grid;
import org.junit.jupiter.api.Test;

class TileHudTest {
  private static Grid grid() {
    Grid g = new Grid(3, 1);
    g.land[0] = 3;
    g.water[0] = 1.5f;
    g.land[1] = 5; // dry
    g.land[2] = 0; // void
    return g;
  }

  @Test
  void wetTileShowsLandWaterSurface() {
    assertArrayEquals(new String[] {"3", "1.50", "4.50"}, TileHud.parts(grid(), 0));
  }

  @Test
  void dryTileStillShowsFullLabel() {
    assertArrayEquals(new String[] {"5", "0.00", "5.00"}, TileHud.parts(grid(), 1));
  }

  @Test
  void voidAndOutOfRangeShowNothing() {
    assertNull(TileHud.parts(grid(), 2));
    assertNull(TileHud.parts(grid(), -1));
    assertNull(TileHud.parts(grid(), 3));
  }

  @Test
  void centeredAboveTileInTheOpen() {
    // Tile 100..140, label 60 wide x 10 tall: x = 120 - 30, y = 200 - gap - 10.
    float[] p = TileHud.position(100, 200, 140, 60, 10, 0, 0, 500, 500);
    assertArrayEquals(new float[] {90f, 200f - TileHud.GAP - 10f}, p);
  }

  @Test
  void clampedAtLeftRightAndTop() {
    assertEquals(0f, TileHud.position(0, 200, 10, 60, 10, 0, 0, 500, 500)[0]);
    assertEquals(440f, TileHud.position(490, 200, 500, 60, 10, 0, 0, 500, 500)[0]);
    assertEquals(0f, TileHud.position(100, 5, 140, 60, 10, 0, 0, 500, 500)[1]);
  }

  @Test
  void tilesNearACornerShareTheClampedPosition() {
    float[] a = TileHud.position(0, 0, 10, 60, 10, 0, 0, 500, 500);
    float[] b = TileHud.position(10, 0, 20, 60, 10, 0, 0, 500, 500);
    assertArrayEquals(a, b);
  }

  @Test
  void labelWiderThanBoundsPinsToTopLeft() {
    float[] p = TileHud.position(10, 50, 20, 300, 10, 5, 7, 100, 100);
    assertArrayEquals(new float[] {5f, 34f}, p);
    float[] q = TileHud.position(10, 5, 20, 300, 200, 5, 7, 100, 100);
    assertArrayEquals(new float[] {5f, 7f}, q);
  }
}
