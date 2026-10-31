package com.hedgecourt.drip.sim;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GridTest {
  @Test
  void indexIsRowMajor() {
    Grid g = new Grid(5, 3);
    assertEquals(0, g.index(0, 0));
    assertEquals(4, g.index(4, 0));
    assertEquals(5, g.index(0, 1));
    assertEquals(7, g.index(2, 1));
    assertEquals(14, g.index(4, 2));
  }

  @Test
  void outOfBoundsThrows() {
    Grid g = new Grid(5, 3);
    assertThrows(IndexOutOfBoundsException.class, () -> g.index(-1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> g.index(0, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> g.index(5, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> g.index(0, 3));
  }

  @Test
  void inBounds() {
    Grid g = new Grid(5, 3);
    assertTrue(g.inBounds(0, 0));
    assertTrue(g.inBounds(4, 2));
    assertFalse(g.inBounds(5, 2));
    assertFalse(g.inBounds(4, 3));
    assertFalse(g.inBounds(-1, 0));
  }

  @Test
  void constructorRejectsNonPositiveDimensions() {
    assertThrows(IllegalArgumentException.class, () -> new Grid(0, 3));
    assertThrows(IllegalArgumentException.class, () -> new Grid(3, 0));
    assertThrows(IllegalArgumentException.class, () -> new Grid(-1, 3));
    assertThrows(IllegalArgumentException.class, () -> new Grid(3, -1));
  }

  @Test
  void arraysSizedAndZeroed() {
    Grid g = new Grid(7, 2);
    assertEquals(7, g.width);
    assertEquals(2, g.height);
    assertEquals(14, g.land.length);
    assertEquals(14, g.water.length);
    for (int i = 0; i < 14; i++) {
      assertEquals(0, g.land[i]);
      assertEquals(0f, g.water[i]);
    }
  }
}
