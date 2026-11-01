package com.hedgecourt.drip;

import com.badlogic.gdx.graphics.Color;

/** Maps land levels to something drawable, plus the water color. */
public interface Tileset {
  /** Color for a land level: 0 is void, 1..7 are heights. */
  Color land(int level);

  /** Base water color; alpha is scaled by depth when drawn. */
  Color water();
}
