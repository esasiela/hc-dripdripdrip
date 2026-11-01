package com.hedgecourt.drip;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.drip.sim.Grid;

/** Draws land tiles top-down as solid squares, only over the visible area. */
public class MapRenderer {
  private final Texture pixel;

  public MapRenderer() {
    Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pm.setColor(Color.WHITE);
    pm.fill();
    pixel = new Texture(pm);
    pm.dispose();
  }

  /** Draws the visible tiles; the batch must be begun with a screen-pixel projection. */
  public void render(
      SpriteBatch batch, Grid grid, Tileset tileset, MapCamera camera, float viewHeight) {
    int x0 = camera.firstVisibleX();
    int x1 = camera.lastVisibleX();
    int y0 = camera.firstVisibleY();
    int y1 = camera.lastVisibleY();
    for (int y = y0; y <= y1; y++) {
      // Round tile edges to whole pixels so neighbors never leave seams.
      float top = Math.round(camera.screenY(y));
      float bottom = Math.round(camera.screenY(y + 1));
      for (int x = x0; x <= x1; x++) {
        float left = Math.round(camera.screenX(x));
        float right = Math.round(camera.screenX(x + 1));
        batch.setColor(tileset.land(grid.land[y * grid.width + x]));
        // Screen y is down; GL y is up.
        batch.draw(pixel, left, viewHeight - bottom, right - left, bottom - top);
      }
    }
    batch.setColor(Color.WHITE);
  }

  public void dispose() {
    pixel.dispose();
  }
}
