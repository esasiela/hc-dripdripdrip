package com.hedgecourt.drip;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hedgecourt.drip.sim.Grid;
import com.hedgecourt.drip.sim.MapData;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DripMain extends ApplicationAdapter {
  private static final float KEY_PAN_PIXELS_PER_SECOND = 600f;
  private static final float SCROLL_ZOOM_STEP = 1.15f;

  private final String startMap;
  private SpriteBatch batch;
  private BitmapFont font;
  private MapRenderer mapRenderer;
  private MapCatalog catalog;
  private final MapCamera camera = new MapCamera();
  private final Matrix4 projection = new Matrix4();
  private int mapIndex;
  private String mapKey = "";
  private MapData map;
  private Tileset tileset;

  /** Creates the game, optionally starting on the named map (null for the first one). */
  public DripMain(String startMap) {
    this.startMap = startMap;
  }

  @Override
  public void create() {
    batch = new SpriteBatch();
    font = new BitmapFont();
    mapRenderer = new MapRenderer();
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
    Gdx.input.setInputProcessor(new Controls());
  }

  private void loadMap(int index) {
    mapIndex = Math.floorMod(index, catalog.names().size());
    mapKey = catalog.names().get(mapIndex);
    map = catalog.load(mapKey);
    tileset = Tilesets.byName(map.tileset());
    camera.setMap(map.grid().width, map.grid().height);
    Gdx.app.log("Drip", "Loaded map " + mapKey);
  }

  @Override
  public void resize(int width, int height) {
    camera.setViewport(width, height);
    projection.setToOrtho2D(0, 0, width, height);
  }

  @Override
  public void render() {
    pollKeys(Gdx.graphics.getDeltaTime());
    float vh = Gdx.graphics.getHeight();
    projection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), vh);

    ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    batch.setProjectionMatrix(projection);
    batch.begin();
    mapRenderer.render(batch, map.grid(), tileset, camera, vh);
    drawHud(vh);
    batch.end();
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

  private void drawHud(float vh) {
    Grid g = map.grid();
    int tx = camera.tileX(Gdx.input.getX());
    int ty = camera.tileY(Gdx.input.getY());
    String tile = "-";
    if (g.inBounds(tx, ty)) {
      int land = g.land[g.index(tx, ty)];
      tile = "(" + tx + ", " + ty + ") height " + (land == 0 ? "void" : String.valueOf(land));
    }
    String[] lines = {
      "FPS: " + Gdx.graphics.getFramesPerSecond(),
      "Map: " + map.name() + " [" + mapKey + "] " + g.width + "x" + g.height,
      String.format("Zoom: %.1f px/tile", camera.pixelsPerTile()),
      "Tile: " + tile,
    };
    for (int i = 0; i < lines.length; i++) {
      font.draw(batch, lines[i], 8, vh - 8 - i * 18);
    }
  }

  @Override
  public void dispose() {
    batch.dispose();
    font.dispose();
    mapRenderer.dispose();
  }

  /** Mouse drag pans, scroll zooms, N/P switch maps. */
  private class Controls extends InputAdapter {
    private int lastX;
    private int lastY;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
      lastX = screenX;
      lastY = screenY;
      return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
      camera.panPixels(screenX - lastX, screenY - lastY);
      lastX = screenX;
      lastY = screenY;
      return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
      camera.zoomAt(
          (float) Math.pow(SCROLL_ZOOM_STEP, -amountY), Gdx.input.getX(), Gdx.input.getY());
      return true;
    }

    @Override
    public boolean keyDown(int keycode) {
      if (keycode == Input.Keys.N) {
        loadMap(mapIndex + 1);
        return true;
      }
      if (keycode == Input.Keys.P) {
        loadMap(mapIndex - 1);
        return true;
      }
      return false;
    }
  }
}
