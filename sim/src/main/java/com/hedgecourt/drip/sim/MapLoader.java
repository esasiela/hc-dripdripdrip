package com.hedgecourt.drip.sim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

/** Parses the v1 JSON map format described in {@code docs/design.md}. */
public final class MapLoader {
  public static final int MAX_LAND = 7;
  private static final String DEFAULT_TILESET = "roygbiv";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private MapLoader() {}

  public static MapData load(InputStream in) {
    try {
      return build(MAPPER.readTree(in));
    } catch (IOException e) {
      throw new MapFormatException("Malformed map JSON: " + e.getMessage(), e);
    }
  }

  public static MapData load(String json) {
    try {
      return build(MAPPER.readTree(json));
    } catch (JsonProcessingException e) {
      throw new MapFormatException("Malformed map JSON: " + e.getOriginalMessage(), e);
    }
  }

  private static MapData build(JsonNode root) {
    if (root == null || !root.isObject()) {
      throw new MapFormatException("Map must be a JSON object");
    }
    JsonNode versionNode = root.get("version");
    if (versionNode == null || !versionNode.isInt()) {
      throw new MapFormatException("Missing or non-integer 'version'");
    }
    int version = versionNode.intValue();
    if (version != 1) {
      throw new MapFormatException("Unsupported map version " + version + " (expected 1)");
    }

    JsonNode size = root.get("size");
    if (size == null || !size.path("width").isInt() || !size.path("height").isInt()) {
      throw new MapFormatException("Missing or invalid 'size' (need integer width and height)");
    }
    int width = size.get("width").intValue();
    int height = size.get("height").intValue();
    if (width <= 0 || height <= 0) {
      throw new MapFormatException("Invalid size " + width + "x" + height + ": must be > 0");
    }

    JsonNode meta = root.path("meta");
    String name = meta.path("name").asText("unnamed");
    String tileset = meta.path("tileset").asText(DEFAULT_TILESET);

    Grid grid = new Grid(width, height);
    String[] landRows = rows(root.get("land"), "land", height);
    for (int y = 0; y < height; y++) {
      String[] cells = cells(landRows[y], "land", y, width);
      for (int x = 0; x < width; x++) {
        int v;
        try {
          v = Integer.parseInt(cells[x]);
        } catch (NumberFormatException e) {
          throw new MapFormatException(
              "land row " + y + ", column " + x + ": '" + cells[x] + "' is not an integer");
        }
        if (v < 0 || v > MAX_LAND) {
          throw new MapFormatException(
              "land row " + y + ", column " + x + ": value " + v + " out of range 0.." + MAX_LAND);
        }
        grid.land[y * width + x] = v;
      }
    }

    if (root.hasNonNull("water")) {
      String[] waterRows = rows(root.get("water"), "water", height);
      for (int y = 0; y < height; y++) {
        String[] cells = cells(waterRows[y], "water", y, width);
        for (int x = 0; x < width; x++) {
          float w;
          try {
            w = Float.parseFloat(cells[x]);
          } catch (NumberFormatException e) {
            throw new MapFormatException(
                "water row " + y + ", column " + x + ": '" + cells[x] + "' is not a number");
          }
          int i = y * width + x;
          if (Float.isNaN(w) || Float.isInfinite(w) || w < 0f) {
            throw new MapFormatException(
                "water row " + y + ", column " + x + ": value " + w + " must be finite and >= 0");
          }
          if (w > 0f && grid.land[i] == 0) {
            throw new MapFormatException(
                "water row " + y + ", column " + x + ": water " + w + " on a void tile");
          }
          grid.water[i] = w;
        }
      }
    }
    return new MapData(version, name, tileset, grid);
  }

  private static String[] rows(JsonNode node, String key, int height) {
    if (node == null || !node.isArray()) {
      throw new MapFormatException("Missing or non-array '" + key + "'");
    }
    if (node.size() != height) {
      throw new MapFormatException(
          "'" + key + "' has " + node.size() + " rows, expected " + height);
    }
    String[] out = new String[height];
    for (int y = 0; y < height; y++) {
      if (!node.get(y).isTextual()) {
        throw new MapFormatException(key + " row " + y + ": expected a string");
      }
      out[y] = node.get(y).textValue();
    }
    return out;
  }

  private static String[] cells(String row, String key, int y, int width) {
    String trimmed = row.trim();
    String[] cells = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
    if (cells.length != width) {
      throw new MapFormatException(
          key + " row " + y + ": has " + cells.length + " columns, expected " + width);
    }
    return cells;
  }
}
