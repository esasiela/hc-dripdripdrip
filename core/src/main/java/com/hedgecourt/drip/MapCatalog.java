package com.hedgecourt.drip;

import com.badlogic.gdx.Gdx;
import com.hedgecourt.drip.sim.MapData;
import com.hedgecourt.drip.sim.MapLoader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Finds and loads the maps under {@code maps/}, listed via the build-generated assets.txt. */
public class MapCatalog {
  private static final String PREFIX = "maps/";
  private static final String SUFFIX = ".json";

  private final List<String> names = new ArrayList<>();

  public MapCatalog() {
    for (String line : Gdx.files.internal("assets.txt").readString("UTF-8").split("\\R")) {
      String path = line.trim();
      if (path.startsWith(PREFIX) && path.endsWith(SUFFIX)) {
        names.add(path.substring(PREFIX.length(), path.length() - SUFFIX.length()));
      }
    }
    Collections.sort(names);
  }

  public List<String> names() {
    return names;
  }

  /** Index of the named map, or -1 if absent. */
  public int indexOf(String name) {
    return names.indexOf(name);
  }

  public MapData load(String name) {
    try (InputStream in = Gdx.files.internal(PREFIX + name + SUFFIX).read()) {
      return MapLoader.load(in);
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Cannot read map " + name, e);
    }
  }
}
