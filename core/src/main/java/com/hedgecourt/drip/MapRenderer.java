package com.hedgecourt.drip;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.drip.sim.Grid;

/** Draws land tiles top-down as solid squares, only over the visible area. */
public class MapRenderer {
  /** Grid lines are hidden when tiles are smaller than this many screen pixels. */
  static final float MIN_GRID_PIXELS_PER_TILE = 4f;

  /** Subtle dark line: black at 25% alpha. */
  private static final Color GRID_COLOR = new Color(0f, 0f, 0f, 0.25f);

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
    if (gridVisible(camera.pixelsPerTile())) {
      drawGrid(batch, camera, viewHeight, x0, x1, y0, y1);
    }
    batch.setColor(Color.WHITE);
  }

  /** Whether tiles are big enough on screen for grid lines to be useful. */
  static boolean gridVisible(float pixelsPerTile) {
    return pixelsPerTile >= MIN_GRID_PIXELS_PER_TILE;
  }

  /** Draws 1-pixel lines on the boundaries between visible tiles (not around the outside). */
  private void drawGrid(
      SpriteBatch batch, MapCamera camera, float viewHeight, int x0, int x1, int y0, int y1) {
    float left = Math.round(camera.screenX(x0));
    float right = Math.round(camera.screenX(x1 + 1));
    float top = Math.round(camera.screenY(y0));
    float bottom = Math.round(camera.screenY(y1 + 1));
    batch.setColor(GRID_COLOR);
    for (int x = x0 + 1; x <= x1; x++) {
      batch.draw(pixel, Math.round(camera.screenX(x)), viewHeight - bottom, 1f, bottom - top);
    }
    for (int y = y0 + 1; y <= y1; y++) {
      batch.draw(pixel, left, viewHeight - Math.round(camera.screenY(y)), right - left, 1f);
    }
  }

  public void dispose() {
    pixel.dispose();
  }
}
