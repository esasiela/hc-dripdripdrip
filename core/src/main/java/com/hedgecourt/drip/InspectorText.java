package com.hedgecourt.drip;

import com.hedgecourt.drip.sim.Grid;
import java.util.List;
import java.util.Locale;

/** Formats the per-tile lines of the inspector panel. Labels are padded so values roughly align. */
final class InspectorText {
  private InspectorText() {}

  /** Lines describing the tile at {@code index}, or a single "-" line if index is negative. */
  static List<String> tileLines(Grid g, int index) {
    if (index < 0) {
      return List.of("Tile:      -");
    }
    String coords = "Tile:      (" + index % g.width + ", " + index / g.width + ")";
    int land = g.land[index];
    if (land == 0) {
      return List.of(coords, "Land:      void");
    }
    float water = g.water[index];
    String surface =
        water == 0 ? String.valueOf(land) : String.format(Locale.ROOT, "%.2f", land + water);
    return List.of(
        coords,
        "Land:      " + land,
        "Water:     " + String.format(Locale.ROOT, "%.2f", water),
        "Surface:  " + surface);
  }
}
