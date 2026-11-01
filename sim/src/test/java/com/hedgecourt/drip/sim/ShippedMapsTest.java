package com.hedgecourt.drip.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Every map shipped under {@code assets/maps} must load. */
class ShippedMapsTest {
  private static final Path MAPS = Path.of("..", "assets", "maps");

  private static List<Path> mapFiles() throws IOException {
    try (Stream<Path> s = Files.list(MAPS)) {
      return s.filter(p -> p.toString().endsWith(".json")).sorted().toList();
    }
  }

  @Test
  void allShippedMapsLoad() throws IOException {
    List<Path> files = mapFiles();
    assertFalse(files.isEmpty());
    for (Path p : files) {
      try (InputStream in = Files.newInputStream(p)) {
        MapData m = MapLoader.load(in);
        assertEquals(m.grid().width * m.grid().height, m.grid().land.length, p.toString());
      } catch (MapFormatException e) {
        throw new AssertionError(p + ": " + e.getMessage(), e);
      }
    }
  }

  @Test
  void includesNonSquareAndLargeMaps() throws IOException {
    boolean nonSquare = false;
    boolean large = false;
    for (Path p : mapFiles()) {
      try (InputStream in = Files.newInputStream(p)) {
        Grid g = MapLoader.load(in).grid();
        nonSquare |= g.width != g.height;
        large |= g.width >= 128 && g.height >= 128;
      }
    }
    assertTrue(nonSquare);
    assertTrue(large);
  }
}
