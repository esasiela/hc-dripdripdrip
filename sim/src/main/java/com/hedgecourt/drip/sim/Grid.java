package com.hedgecourt.drip.sim;

/**
 * Rectangular tile grid. Land is 0 (void) or 1..7 (height); water is depth. Index is {@code y *
 * width + x}.
 */
public class Grid 
{
  public final int width;
  public final int height;
  public final int[] land;
  public final float[] water;

  public Grid(int width, int height) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException(
          "Grid dimensions must be > 0, got " + width + "x" + height);
    }
    this.width = width;
    this.height = height;
    this.land = new int[width * height];
    this.water = new float[width * height];
  }

  public boolean inBounds(int x, int y) {
    return x >= 0 && x < width && y >= 0 && y < height;
  }

  public int index(int x, int y) {
    if (!inBounds(x, y)) {
      throw new IndexOutOfBoundsException(
          "(" + x + ", " + y + ") out of bounds for grid " + width + "x" + height + "long line long line long line long line");
    }
    return y * width + x;
  }
}
