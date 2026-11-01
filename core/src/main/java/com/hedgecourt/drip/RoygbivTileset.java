package com.hedgecourt.drip;

import com.badlogic.gdx.graphics.Color;

/** Solid-color tiles in rainbow order, generated in code. Void is black. */
public class RoygbivTileset implements Tileset {
  public static final String NAME = "roygbiv";

  private static final Color[] LAND = {
    Color.BLACK,
    new Color(0.90f, 0.10f, 0.10f, 1f),
    new Color(1.00f, 0.55f, 0.05f, 1f),
    new Color(1.00f, 0.90f, 0.10f, 1f),
    new Color(0.10f, 0.70f, 0.20f, 1f),
    new Color(0.15f, 0.40f, 0.95f, 1f),
    new Color(0.30f, 0.10f, 0.60f, 1f),
    new Color(0.65f, 0.35f, 0.85f, 1f),
  };
  private static final Color WATER = new Color(0.10f, 0.35f, 1.00f, 0.6f);

  @Override
  public Color land(int level) {
    return LAND[Math.max(0, Math.min(LAND.length - 1, level))];
  }

  @Override
  public Color water() {
    return WATER;
  }
}
