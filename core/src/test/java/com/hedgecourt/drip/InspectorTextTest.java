package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedgecourt.drip.sim.Grid;
import java.util.List;
import org.junit.jupiter.api.Test;

class InspectorTextTest {
  private final Grid grid = new Grid(3, 2);

  @Test
  void offMapShowsDash() {
    assertEquals(List.of("Tile:      -"), InspectorText.tileLines(grid, -1));
  }

  @Test
  void voidTileHasNoWaterOrSurface() {
    assertEquals(List.of("Tile:      (1, 1)", "Land:      void"), InspectorText.tileLines(grid, 4));
  }

  @Test
  void dryTileSurfaceIsInteger() {
    grid.land[2] = 3;
    assertEquals(
        List.of("Tile:      (2, 0)", "Land:      3", "Water:     0.00", "Surface:  3"),
        InspectorText.tileLines(grid, 2));
  }

  @Test
  void wetTileShowsTwoDecimals() {
    grid.land[0] = 2;
    grid.water[0] = 0.256f;
    assertEquals(
        List.of("Tile:      (0, 0)", "Land:      2", "Water:     0.26", "Surface:  2.26"),
        InspectorText.tileLines(grid, 0));
  }
}
