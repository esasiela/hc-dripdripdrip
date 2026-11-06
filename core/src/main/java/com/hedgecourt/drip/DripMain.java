package com.hedgecourt.drip;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hedgecourt.drip.sim.Flow;
import com.hedgecourt.drip.sim.Grid;
import com.hedgecourt.drip.sim.MapData;
import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DripMain extends ApplicationAdapter {
  private static final float KEY_PAN_PIXELS_PER_SECOND = 600f;
  private static final float SCROLL_ZOOM_STEP = 1.15f;
  private static final float INSPECTOR_TITLE_SCALE = 1.5f;
  private static final float INSPECTOR_TITLE_HEIGHT = 30f;

  private final String startMap;
  private SpriteBatch batch;
  private BitmapFont font;
  private MapRenderer mapRenderer;
  private SettingsPanel settingsPanel;
  private HelpOverlay helpOverlay;
  private MapCatalog catalog;
  private final TileHud tileHud = new TileHud();
  private final ViewSettings settings = new ViewSettings();
  private final MapCamera camera = new MapCamera();
  private final Matrix4 projection = new Matrix4();
  private int mapIndex;
  private String mapKey = "";
  private MapData map;
  private Tileset tileset;
  private final Flow flow = new Flow();
  private int tickCount;
  private float waterTotal;

  /** Creates the game, optionally starting on the named map (null for the first one). */
  public DripMain(String startMap) {
    this.startMap = startMap;
  }

  @Override
  public void create() {
    batch = new SpriteBatch();
    font = new BitmapFont();
    mapRenderer = new MapRenderer();
    settingsPanel = new SettingsPanel(font, settings);
    helpOverlay = new HelpOverlay(font);
    catalog = new MapCatalog();
    if (catalog.names().isEmpty()) {
      throw new IllegalStateException("No maps found under maps/");
    }

    camera.setViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    int start = startMap == null ? 0 : catalog.indexOf(startMap);
    if (start < 0) {
      Gdx.app.log("Drip", "WARNING: unknown map '" + startMap + "', available: " + catalog.names());
      start = 0;
    }
    loadMap(start);
    Gdx.input.setInputProcessor(new InputMultiplexer(settingsPanel.stage(), new Controls()));
  }

  private void loadMap(int index) {
    mapIndex = Math.floorMod(index, catalog.names().size());
    mapKey = catalog.names().get(mapIndex);
    map = catalog.load(mapKey);
    tileset = Tilesets.byName(map.tileset());
    camera.setMap(map.grid().width, map.grid().height);
    resetCounters();
    Gdx.app.log("Drip", "Loaded map " + mapKey);
  }

  /** Reloads the current map's starting state. The camera is left alone. */
  private void resetMap() {
    map = catalog.load(mapKey);
    resetCounters();
    Gdx.app.log("Drip", "Reset map " + mapKey);
  }

  private void resetCounters() {
    tickCount = 0;
    waterTotal = InspectorText.totalWater(map.grid());
  }

  private void stepFlow() {
    flow.tick(map.grid());
    tickCount++;
    waterTotal = InspectorText.totalWater(map.grid());
  }

  @Override
  public void resize(int width, int height) {
    camera.setViewport(width, height);
    projection.setToOrtho2D(0, 0, width, height);
    settingsPanel.resize(width, height);
    helpOverlay.resize(width, height);
  }

  @Override
  public void render() {
    pollKeys(Gdx.graphics.getDeltaTime());
    float vh = Gdx.graphics.getHeight();
    projection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), vh);

    ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    batch.setProjectionMatrix(projection);
    batch.begin();
    mapRenderer.render(batch, map.grid(), tileset, camera, settings, vh);
    drawHighlight(vh);
    drawTileHud(vh);
    drawInspector(vh);
    batch.end();
    helpOverlay.render(Gdx.graphics.getDeltaTime());
    settingsPanel.render(Gdx.graphics.getDeltaTime());
  }

  private void pollKeys(float delta) {
    float dx = 0;
    float dy = 0;
    if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      dx += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      dx -= 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
      dy += 1;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      dy -= 1;
    }
    if (dx != 0 || dy != 0) {
      float step = KEY_PAN_PIXELS_PER_SECOND * delta;
      camera.panPixels(dx * step, dy * step);
    }
  }

  private void drawHighlight(float vh) {
    int index = tileUnderCursor();
    int x = Gdx.input.getX();
    int y = Gdx.input.getY();
    if (!settings.highlightHovered
        || index < 0
        || settingsPanel.isOver(x, y)
        || helpOverlay.isOver(x, y)) {
      return;
    }
    int width = map.grid().width;
    mapRenderer.drawHighlight(batch, camera, index % width, index / width, vh);
  }

  private void drawTileHud(float vh) {
    int index = tileUnderCursor();
    int x = Gdx.input.getX();
    int y = Gdx.input.getY();
    if (!settings.showTileHud
        || index < 0
        || settingsPanel.isOver(x, y)
        || helpOverlay.isOver(x, y)) {
      return;
    }
    tileHud.draw(batch, font, map.grid(), camera, index, Gdx.graphics.getWidth(), vh);
  }

  /** Grid index of the map tile under the mouse cursor, or -1 if the cursor is off the map. */
  private int tileUnderCursor() {
    Grid g = map.grid();
    int tx = camera.tileX(Gdx.input.getX());
    int ty = camera.tileY(Gdx.input.getY());
    return g.inBounds(tx, ty) ? g.index(tx, ty) : -1;
  }

  private void drawInspector(float vh) {
    Grid g = map.grid();
    List<String> lines = new ArrayList<>();
    lines.add("FPS: " + Gdx.graphics.getFramesPerSecond());
    lines.add("Map: " + map.name() + " [" + mapKey + "] " + g.width + "x" + g.height);
    lines.add(String.format("Zoom: %.1f px/tile", camera.pixelsPerTile()));
    lines.addAll(InspectorText.globalLines(tickCount, waterTotal));
    lines.addAll(InspectorText.tileLines(g, tileUnderCursor()));
    lines.add("F1: settings   ? help");

    font.getData().setScale(INSPECTOR_TITLE_SCALE);
    font.draw(batch, "Inspector", 8, vh - 8);
    font.getData().setScale(1f);
    float top = vh - 8 - INSPECTOR_TITLE_HEIGHT;
    for (int i = 0; i < lines.size(); i++) {
      font.draw(batch, lines.get(i), 8, top - i * 18);
    }
  }

  @Override
  public void dispose() {
    batch.dispose();
    font.dispose();
    mapRenderer.dispose();
    settingsPanel.dispose();
    helpOverlay.dispose();
  }

  /** Mouse drag pans, scroll zooms, N/P switch maps, . steps flow, R resets, ? toggles help. */
  private class Controls extends InputAdapter {
    private int lastX;
    private int lastY;
    private boolean panning;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
      // A press on an empty part of the panel must not start a map drag.
      panning = !settingsPanel.isOver(screenX, screenY);
      if (!panning) {
        return true;
      }
      lastX = screenX;
      lastY = screenY;
      return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
      if (!panning) {
        return true;
      }
      camera.panPixels(screenX - lastX, screenY - lastY);
      lastX = screenX;
      lastY = screenY;
      return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
      if (settingsPanel.isOver(Gdx.input.getX(), Gdx.input.getY())) {
        return true;
      }
      camera.zoomAt(
          (float) Math.pow(SCROLL_ZOOM_STEP, -amountY), Gdx.input.getX(), Gdx.input.getY());
      return true;
    }

    @Override
    public boolean keyTyped(char character) {
      // Typed character, so Shift+/ works regardless of keyboard layout.
      if (character == '?') {
        helpOverlay.toggle();
        return true;
      }
      return false;
    }

    @Override
    public boolean keyDown(int keycode) {
      if (keycode == Input.Keys.F1) {
        settingsPanel.toggle();
        return true;
      }
      if (keycode == Input.Keys.N) {
        loadMap(mapIndex + 1);
        return true;
      }
      if (keycode == Input.Keys.P) {
        loadMap(mapIndex - 1);
        return true;
      }
      if (keycode == Input.Keys.PERIOD) {
        stepFlow();
        return true;
      }
      if (keycode == Input.Keys.R) {
        resetMap();
        return true;
      }
      return false;
    }
  }
}
