package com.hedgecourt.drip;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Read-only, non-modal overlay listing {@link HelpMappings}. Its stage is not registered as an
 * input processor, so it never captures input.
 */
public class HelpOverlay implements Disposable {
  private final Stage stage = new Stage(new ScreenViewport());
  private final Table root = new Table();
  private final Texture background = solid(new Color(0f, 0f, 0f, 0.7f));

  /** Builds the (initially closed) overlay; the font is borrowed, not disposed here. */
  public HelpOverlay(BitmapFont font) {
    Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
    Table panel = new Table();
    panel.setBackground(new TextureRegionDrawable(background));
    panel.pad(12);
    panel.defaults().left().pad(2);
    panel.add(new Label("Controls", style)).colspan(2).row();
    for (HelpMappings.Entry e : HelpMappings.entries()) {
      panel.add(new Label(e.keys(), style)).padRight(20);
      panel.add(new Label(e.description(), style)).row();
    }

    root.setFillParent(true);
    root.add(panel);
    root.setVisible(false);
    stage.addActor(root);
  }

  public void toggle() {
    root.setVisible(!root.isVisible());
  }

  public void resize(int width, int height) {
    stage.getViewport().update(width, height, true);
  }

  public void render(float delta) {
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void dispose() {
    stage.dispose();
    background.dispose();
  }

  private static Texture solid(Color color) {
    Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pm.setColor(color);
    pm.fill();
    Texture t = new Texture(pm);
    pm.dispose();
    return t;
  }
}
