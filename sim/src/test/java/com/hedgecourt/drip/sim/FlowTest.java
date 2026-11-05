package com.hedgecourt.drip.sim;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class FlowTest {
  private static final float EPS = 1e-4f;

  /** Land 1 everywhere inside a one-tile void border. Interior is (w-2) x (h-2). */
  private static Grid bordered(int w, int h) {
    Grid g = new Grid(w, h);
    for (int y = 1; y < h - 1; y++) {
      for (int x = 1; x < w - 1; x++) {
        g.land[y * w + x] = 1;
      }
    }
    return g;
  }

  /** Flat land of the given height everywhere, no void. */
  private static Grid flat(int w, int h, int land) {
    Grid g = new Grid(w, h);
    Arrays.fill(g.land, land);
    return g;
  }

  private static float total(Grid g) {
    float sum = 0f;
    for (float w : g.water) {
      sum += w;
    }
    return sum;
  }

  private static float at(Grid g, int x, int y) {
    return g.water[g.index(x, y)];
  }

  private static void run(Flow flow, Grid g, int ticks) {
    for (int i = 0; i < ticks; i++) {
      flow.tick(g);
    }
  }

  /** A bumpy map with a void hole, for the invariant tests. */
  private static Grid bumpy() {
    Grid g = new Grid(6, 5);
    for (int i = 0; i < g.land.length; i++) {
      g.land[i] = 1 + (i * 3) % 7;
    }
    g.land[g.index(2, 2)] = 0;
    g.land[g.index(3, 1)] = 0;
    g.water[g.index(1, 1)] = 3f;
    g.water[g.index(4, 3)] = 1.5f;
    g.water[g.index(3, 2)] = 0.7f;
    return g;
  }

  @Test
  void conservesMassOverOneAndManyTicks() {
    Grid g = bumpy();
    float before = total(g);
    Flow flow = new Flow();
    flow.tick(g);
    assertEquals(before, total(g), EPS);
    run(flow, g, 2000);
    assertEquals(before, total(g), EPS);
  }

  @Test
  void neverNegativeAndVoidStaysEmpty() {
    Grid g = bumpy();
    Flow flow = new Flow(100f, Flow.MIN_DIFFERENTIAL);
    for (int t = 0; t < 500; t++) {
      flow.tick(g);
      for (int i = 0; i < g.water.length; i++) {
        assertTrue(g.water[i] >= 0f, "negative water at " + i + " tick " + t);
        if (g.land[i] == 0) {
          assertEquals(0f, g.water[i], "water in void at " + i + " tick " + t);
        }
      }
    }
  }

  @Test
  void isDeterministic() {
    Grid a = bumpy();
    Grid b = bumpy();
    run(new Flow(), a, 300);
    run(new Flow(), b, 300);
    assertArrayEquals(a.water, b.water);
  }

  @Test
  void stillWaterDoesNotMove() {
    Grid g = bordered(5, 4);
    for (int y = 1; y < 3; y++) {
      for (int x = 1; x < 4; x++) {
        g.water[g.index(x, y)] = 0.5f;
      }
    }
    float[] before = g.water.clone();
    new Flow().tick(g);
    assertArrayEquals(before, g.water);
  }

  @Test
  void spreadsFromOneWetTile() {
    Grid g = bordered(7, 7);
    g.water[g.index(3, 3)] = 1f;
    new Flow().tick(g);
    float share = at(g, 3, 2);
    assertTrue(at(g, 3, 3) < 1f);
    assertTrue(share > 0f);
    assertEquals(share, at(g, 4, 3), 1e-9f);
    assertEquals(share, at(g, 3, 4), 1e-9f);
    assertEquals(share, at(g, 2, 3), 1e-9f);
    assertEquals(1f, at(g, 3, 3) + 4 * share, EPS);
    for (int y = 0; y < 7; y++) {
      for (int x = 0; x < 7; x++) {
        boolean near = Math.abs(x - 3) + Math.abs(y - 3) <= 1;
        if (!near) {
          assertEquals(0f, at(g, x, y), "tile " + x + "," + y);
        }
      }
    }
  }

  @Test
  void staysMirrorSymmetric() {
    Grid g = bordered(9, 7);
    g.water[g.index(4, 3)] = 3f;
    Flow flow = new Flow();
    for (int t = 0; t < 400; t++) {
      flow.tick(g);
      for (int y = 0; y < 7; y++) {
        for (int x = 0; x < 9; x++) {
          assertEquals(at(g, x, y), at(g, 8 - x, y), 1e-5f, "left/right " + x + "," + y);
          assertEquals(at(g, x, y), at(g, x, 6 - y), 1e-5f, "up/down " + x + "," + y);
        }
      }
    }
  }
}
