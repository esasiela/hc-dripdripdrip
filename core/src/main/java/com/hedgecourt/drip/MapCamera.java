package com.hedgecourt.drip;

/**
 * Pure camera math for a top-down tile map; no libGDX types so it can be unit tested. World
 * coordinates are in tiles with y pointing down (tile {@code (x, y)} covers {@code [x, x+1) x [y,
 * y+1)}), matching map row order. Screen coordinates are pixels with y down, origin top-left.
 */
public class MapCamera {
  /** Maximum zoom, in screen pixels per tile. */
  public static final float MAX_PIXELS_PER_TILE = 64f;

  private int mapWidth = 1;
  private int mapHeight = 1;
  private float viewWidth = 1f;
  private float viewHeight = 1f;
  private float centerX;
  private float centerY;
  private float pixelsPerTile = 1f;

  /** Sets the viewport size in pixels, keeping the zoom and position valid. */
  public void setViewport(float width, float height) {
    this.viewWidth = Math.max(1f, width);
    this.viewHeight = Math.max(1f, height);
    constrain();
  }

  /** Switches to a new map, zoomed out so the whole map fits and centered on it. */
  public void setMap(int width, int height) {
    this.mapWidth = width;
    this.mapHeight = height;
    this.pixelsPerTile = minPixelsPerTile();
    this.centerX = width / 2f;
    this.centerY = height / 2f;
    constrain();
  }

  /** Smallest allowed zoom: the whole map fits on screen (capped by the max zoom). */
  public float minPixelsPerTile() {
    return Math.min(MAX_PIXELS_PER_TILE, Math.min(viewWidth / mapWidth, viewHeight / mapHeight));
  }

  public float pixelsPerTile() {
    return pixelsPerTile;
  }

  public float centerX() {
    return centerX;
  }

  public float centerY() {
    return centerY;
  }

  /** Pans by a screen-space delta, as when dragging the map with the mouse. */
  public void panPixels(float dx, float dy) {
    centerX -= dx / pixelsPerTile;
    centerY -= dy / pixelsPerTile;
    constrain();
  }

  /** Multiplies the zoom by {@code factor}, keeping the world point under the cursor fixed. */
  public void zoomAt(float factor, float screenX, float screenY) {
    float wx = worldX(screenX);
    float wy = worldY(screenY);
    pixelsPerTile = clampZoom(pixelsPerTile * factor);
    centerX = wx - (screenX - viewWidth / 2f) / pixelsPerTile;
    centerY = wy - (screenY - viewHeight / 2f) / pixelsPerTile;
    constrain();
  }

  public float worldX(float screenX) {
    return centerX + (screenX - viewWidth / 2f) / pixelsPerTile;
  }

  public float worldY(float screenY) {
    return centerY + (screenY - viewHeight / 2f) / pixelsPerTile;
  }

  public float screenX(float worldX) {
    return (worldX - centerX) * pixelsPerTile + viewWidth / 2f;
  }

  public float screenY(float worldY) {
    return (worldY - centerY) * pixelsPerTile + viewHeight / 2f;
  }

  /** Tile column under a screen x, which may be outside the map. */
  public int tileX(float screenX) {
    return (int) Math.floor(worldX(screenX));
  }

  /** Tile row under a screen y, which may be outside the map. */
  public int tileY(float screenY) {
    return (int) Math.floor(worldY(screenY));
  }

  /** First visible tile column, clamped to the map. */
  public int firstVisibleX() {
    return clamp((int) Math.floor(worldX(0f)), 0, mapWidth - 1);
  }

  /** Last visible tile column, clamped to the map. */
  public int lastVisibleX() {
    return clamp((int) Math.floor(worldX(viewWidth)), 0, mapWidth - 1);
  }

  /** First visible tile row, clamped to the map. */
  public int firstVisibleY() {
    return clamp((int) Math.floor(worldY(0f)), 0, mapHeight - 1);
  }

  /** Last visible tile row, clamped to the map. */
  public int lastVisibleY() {
    return clamp((int) Math.floor(worldY(viewHeight)), 0, mapHeight - 1);
  }

  private float clampZoom(float ppt) {
    return Math.max(minPixelsPerTile(), Math.min(MAX_PIXELS_PER_TILE, ppt));
  }

  private void constrain() {
    pixelsPerTile = clampZoom(pixelsPerTile);
    centerX = clampCenter(centerX, mapWidth, viewWidth / pixelsPerTile);
    centerY = clampCenter(centerY, mapHeight, viewHeight / pixelsPerTile);
  }

  /** Keeps the view inside the map; a map smaller than the view is centered. */
  private static float clampCenter(float center, float mapSize, float viewSize) {
    if (viewSize >= mapSize) {
      return mapSize / 2f;
    }
    return Math.max(viewSize / 2f, Math.min(mapSize - viewSize / 2f, center));
  }

  private static int clamp(int v, int lo, int hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
