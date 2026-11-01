package com.hedgecourt.drip;

import com.badlogic.gdx.Gdx;

/** Registry of tilesets by the name used in a map's {@code meta.tileset}. */
public final class Tilesets {
  private Tilesets() {}

  /** Returns the named tileset; unknown names log a warning and fall back to roygbiv. */
  public static Tileset byName(String name) {
    if (RoygbivTileset.NAME.equals(name)) {
      return new RoygbivTileset();
    }
    Gdx.app.log("Drip", "WARNING: unknown tileset '" + name + "', using " + RoygbivTileset.NAME);
    return new RoygbivTileset();
  }
}
