package com.hedgecourt.drip;

import com.hedgecourt.drip.sim.Grid;
import java.util.List;
import java.util.Locale;

/** Formats the per-tile lines of the inspector panel. Labels are padded so values roughly align. */
final class InspectorText {
  private InspectorText() {}

  /** Sum of water over all tiles. */
  static float totalWater(Grid g) {
    double sum = 0;
    for (float w : g.water) {
      sum += w;
    }
    return (float) sum;
  }

  /** The global lines: ticks run since the map was loaded or reset, and total water. */
  static List<String> globalLines(int tick, float totalWater) {
    return List.of("Tick: " + tick, String.format(Locale.ROOT, "Water total: %.2f", totalWater));
  }

  /** Always four lines for the tile at {@code index}; "-" stands in for data that doesn't exist. */
  static List<String> tileLines(Grid g, int index) {
    if (index < 0) {
      return lines("-", "-", "-", "-");
    }
    String coords = "(" + index % g.width + ", " + index / g.width + ")";
    int land = g.land[index];
    if (land == 0) {
      return lines(coords, "void", "-", "-");
    }
    float water = g.water[index];
    return lines(
        coords,
        String.valueOf(land),
        String.format(Locale.ROOT, "%.2f", water),
        String.format(Locale.ROOT, "%.2f", land + water));
  }

  private static List<String> lines(String tile, String land, String water, String surface) {
    return List.of(
        "Tile: " + tile, "Land:       " + land, "Water:     " + water, "Surface:  " + surface);
  }
}
