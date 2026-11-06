package com.hedgecourt.drip;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.drip.sim.Grid;
import java.util.Locale;

/**
 * The tile-attached HUD above the hovered tile: a status line ({@code Tick: N Water total: X.XX})
 * over a colored {@code land + water = surface} line. The label has a fixed screen size and is
 * clamped to the visible part of the map.
 */
final class TileHud {
  /** Gap in pixels between the tile's top edge and the label. */
  static final float GAP = 6f;

  /** Minimum distance in pixels between the label and the edge of the visible map. */
  static final float EDGE_PADDING = 6f;

  /** Vertical gap in pixels between the two lines. */
  private static final float LINE_GAP = 4f;

  private static final Color LAND = new Color(0.35f, 0.9f, 0.35f, 1f);
  private static final Color WATER = new Color(0.35f, 0.65f, 1f, 1f);
  private static final Color SURFACE = Color.WHITE;
  private static final Color SEPARATOR = new Color(0.75f, 0.75f, 0.75f, 1f);
  private static final Color SHADOW = new Color(0f, 0f, 0f, 0.8f);

  private final GlyphLayout layout = new GlyphLayout();

  /** Land, water and surface text for a tile; null for void or an out-of-range index. */
  static String[] parts(Grid g, int index) {
    if (index < 0 || index >= g.land.length || g.land[index] == 0) {
      return null;
    }
    float water = g.water[index];
    return new String[] {
      String.valueOf(g.land[index]),
      String.format(Locale.ROOT, "%.2f", water),
      String.format(Locale.ROOT, "%.2f", g.land[index] + water)
    };
  }

  /** The first HUD line: the tick count and the total water. */
  static String statusLine(int tick, float totalWater) {
    return "Tick: " + tick + String.format(Locale.ROOT, "   Water total: %.2f", totalWater);
  }

  /**
   * Top-left of the label in screen coordinates (y down): centered above the tile, then clamped
   * into the bounds. A label larger than the bounds is pinned to the bounds' top-left.
   */
  static float[] position(
      float tileLeft,
      float tileTop,
      float tileRight,
      float labelWidth,
      float labelHeight,
      float minX,
      float minY,
      float maxX,
      float maxY) {
    float x = (tileLeft + tileRight) / 2f - labelWidth / 2f;
    float y = tileTop - GAP - labelHeight;
    x = Math.max(minX, Math.min(maxX - labelWidth, x));
    y = Math.max(minY, Math.min(maxY - labelHeight, y));
    return new float[] {x, y};
  }

  /**
   * Draws the label for tile {@code index}, if it is not void. Batch uses a screen projection. Both
   * lines are centered on the same x.
   */
  void draw(
      SpriteBatch batch,
      BitmapFont font,
      Grid g,
      MapCamera camera,
      int index,
      int tick,
      float totalWater,
      float viewWidth,
      float viewHeight) {
    String[] p = parts(g, index);
    if (p == null) {
      return;
    }
    String[] texts = {p[0], " + ", p[1], " = ", p[2]};
    Color[] colors = {LAND, SEPARATOR, WATER, SEPARATOR, SURFACE};
    float[] widths = new float[texts.length];
    float total = 0f;
    for (int i = 0; i < texts.length; i++) {
      layout.setText(font, texts[i]);
      widths[i] = layout.width;
      total += widths[i];
    }
    String status = statusLine(tick, totalWater);
    layout.setText(font, status);
    float statusWidth = layout.width;
    float lineHeight = font.getCapHeight();
    float height = 2 * lineHeight + LINE_GAP;
    float width = Math.max(total, statusWidth);

    int tx = index % g.width;
    int ty = index / g.width;
    float[] pos =
        position(
            camera.screenX(tx),
            camera.screenY(ty),
            camera.screenX(tx + 1),
            width,
            height,
            Math.max(0f, camera.screenX(0)) + EDGE_PADDING,
            Math.max(0f, camera.screenY(0)) + EDGE_PADDING,
            Math.min(viewWidth, camera.screenX(g.width)) - EDGE_PADDING,
            Math.min(viewHeight, camera.screenY(g.height)) - EDGE_PADDING);

    // Screen y is down; BitmapFont.draw takes the GL y (up) of the top of the text.
    float top = viewHeight - pos[1];
    drawShadowed(batch, font, status, SURFACE, Math.round(pos[0] + (width - statusWidth) / 2), top);
    top -= lineHeight + LINE_GAP;
    float x = Math.round(pos[0] + (width - total) / 2);
    for (int i = 0; i < texts.length; i++) {
      drawShadowed(batch, font, texts[i], colors[i], x, top);
      x += widths[i];
    }
  }

  private static void drawShadowed(
      SpriteBatch batch, BitmapFont font, String text, Color color, float x, float top) {
    font.setColor(SHADOW);
    font.draw(batch, text, x + 1, top - 1);
    font.setColor(color);
    font.draw(batch, text, x, top);
    font.setColor(Color.WHITE);
  }
}
