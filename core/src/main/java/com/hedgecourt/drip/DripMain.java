package com.hedgecourt.drip;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hedgecourt.drip.sim.Grid;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DripMain extends ApplicationAdapter {
  private SpriteBatch batch;
  private BitmapFont font;

  @Override
  public void create() {
    batch = new SpriteBatch();
    font = new BitmapFont();

    Grid grid = new Grid(8, 8);
    Gdx.app.log("Drip", "sim Grid created: " + grid.width + "x" + grid.height);
  }

  @Override
  public void render() {
    ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    batch.begin();
    font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 8, Gdx.graphics.getHeight() - 8);
    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
    font.dispose();
  }
}
