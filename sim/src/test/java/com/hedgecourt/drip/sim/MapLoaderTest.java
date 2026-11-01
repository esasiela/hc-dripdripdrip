package com.hedgecourt.drip.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MapLoaderTest {
  private static String map(String land, String water, String extra) {
    return "{\"version\":1,\"meta\":{\"name\":\"T\",\"tileset\":\"roygbiv\"},"
        + "\"size\":{\"width\":3,\"height\":2},"
        + "\"land\":"
        + land
        + (water == null ? "" : ",\"water\":" + water)
        + (extra == null ? "" : "," + extra)
        + "}";
  }

  private static final String LAND = "[\"1 2 3\",\"0 7 1\"]";

  private static String message(String json) {
    return assertThrows(MapFormatException.class, () -> MapLoader.load(json)).getMessage();
  }

  @Test
  void loadsValidMap() {
    MapData m = MapLoader.load(map(LAND, "[\"0 0.5 0\",\"0 2 0\"]", null));
    assertEquals("T", m.name());
    assertEquals("roygbiv", m.tileset());
    Grid g = m.grid();
    assertEquals(3, g.width);
    assertEquals(2, g.height);
    assertEquals(3, g.land[2]);
    assertEquals(0, g.land[3]);
    assertEquals(7, g.land[g.index(1, 1)]);
    assertEquals(0.5f, g.water[1]);
    assertEquals(2f, g.water[g.index(1, 1)]);
  }

  @Test
  void waterIsOptional() {
    MapData m = MapLoader.load(map(LAND, null, null));
    for (float w : m.grid().water) {
      assertEquals(0f, w);
    }
  }

  @Test
  void unknownKeysAreIgnored() {
    MapData m =
        MapLoader.load(
            map(
                LAND,
                null,
                "\"sources\":[{\"x\":1,\"y\":1,\"rate\":2}],\"drains\":[],\"obstacles\":[],"
                    + "\"future\":{\"a\":1}"));
    assertEquals(3, m.grid().width);
  }

  @Test
  void wrongRowCount() {
    String msg = message(map("[\"1 2 3\"]", null, null));
    assertTrue(msg.contains("1 rows, expected 2"), msg);
  }

  @Test
  void wrongColumnCountNamesRow() {
    String msg = message(map("[\"1 2 3\",\"1 2\"]", null, null));
    assertTrue(msg.contains("land row 1"), msg);
    assertTrue(msg.contains("2 columns, expected 3"), msg);
  }

  @Test
  void landOutOfRangeNamesRowAndColumn() {
    String msg = message(map("[\"1 2 3\",\"1 8 1\"]", null, null));
    assertTrue(msg.contains("land row 1, column 1"), msg);
    assertTrue(msg.contains("out of range"), msg);
  }

  @Test
  void negativeWater() {
    String msg = message(map(LAND, "[\"0 0 0\",\"0 -1 0\"]", null));
    assertTrue(msg.contains("water row 1, column 1"), msg);
  }

  @Test
  void waterOnVoidTile() {
    String msg = message(map(LAND, "[\"0 0 0\",\"1 0 0\"]", null));
    assertTrue(msg.contains("water row 1, column 0"), msg);
    assertTrue(msg.contains("void"), msg);
  }

  @Test
  void malformedJson() {
    assertTrue(message("{not json").startsWith("Malformed map JSON"));
  }

  @Test
  void unsupportedVersion() {
    String json = map(LAND, null, null).replace("\"version\":1", "\"version\":2");
    assertTrue(message(json).contains("Unsupported map version 2"));
  }
}
