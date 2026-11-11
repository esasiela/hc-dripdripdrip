package com.hedgecourt.drip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedgecourt.drip.sim.Grid;
import java.util.List;
import org.junit.jupiter.api.Test;

class InspectorTextTest {
  private final Grid grid = new Grid(3, 2);

  @Test
  void totalWaterSumsAllTiles() {
    grid.land[0] = 2;
    grid.water[0] = 0.5f;
    grid.land[1] = 1;
    grid.water[1] = 1.0f;
    assertEquals(1.5f, InspectorText.totalWater(grid), 1e-6f);
  }

  @Test
  void globalLinesShowTickTotalAndSimState() {
    assertEquals(
        List.of("Tick: 3", "Water total: 1.50", "Sim: paused"),
        InspectorText.globalLines(3, 1.5f, false));
    assertEquals(
        List.of("Tick: 3", "Water total: 1.50", "Sim: playing"),
        InspectorText.globalLines(3, 1.5f, true));
  }

  @Test
  void offMapShowsDashes() {
    assertEquals(
        List.of("Tile: -", "Land:       -", "Water:     -", "Surface:  -"),
        InspectorText.tileLines(grid, -1));
  }

  @Test
  void voidTileHasNoWaterOrSurface() {
    assertEquals(
        List.of("Tile: (1, 1)", "Land:       void", "Water:     -", "Surface:  -"),
        InspectorText.tileLines(grid, 4));
  }

  @Test
  void dryTileSurfaceHasTwoDecimals() {
    grid.land[2] = 3;
    assertEquals(
        List.of("Tile: (2, 0)", "Land:       3", "Water:     0.00", "Surface:  3.00"),
        InspectorText.tileLines(grid, 2));
  }

  @Test
  void wetTileShowsTwoDecimals() {
    grid.land[0] = 2;
    grid.water[0] = 0.256f;
    assertEquals(
        List.of("Tile: (0, 0)", "Land:       2", "Water:     0.26", "Surface:  2.26"),
        InspectorText.tileLines(grid, 0));
  }
}
