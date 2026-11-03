package com.hedgecourt.drip;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Mouse-operable settings panel (Scene2D). Styles are generated in code, so there are no skin
 * assets. To add a boolean setting, add a field to {@link ViewSettings} and one {@code addCheckbox}
 * call in the constructor.
 */
public class SettingsPanel implements Disposable {
  private static final int BOX_SIZE = 14;

  private final Stage stage = new Stage(new ScreenViewport());
  private final Table root = new Table();
  private final Table panel = new Table();
  private final Texture background = solid(new Color(0f, 0f, 0f, 0.7f), 1);
  private final Texture boxOff = box(false);
  private final Texture boxOn = box(true);
  private final CheckBox.CheckBoxStyle checkStyle;

  /** Builds the (initially closed) panel; the font is borrowed, not disposed here. */
  public SettingsPanel(BitmapFont font, ViewSettings settings) {
    checkStyle =
        new CheckBox.CheckBoxStyle(
            new TextureRegionDrawable(boxOff), new TextureRegionDrawable(boxOn), font, Color.WHITE);
    panel.setBackground(new TextureRegionDrawable(background));
    panel.pad(10);
    panel.defaults().left().pad(2);

    addCheckbox("Show grid", () -> settings.showGrid, v -> settings.showGrid = v);
    addCheckbox(
        "Highlight hovered tile",
        () -> settings.highlightHovered,
        v -> settings.highlightHovered = v);
    addCheckbox("Show water", () -> settings.showWater, v -> settings.showWater = v);

    root.setFillParent(true);
    root.top().right().pad(8);
    root.add(panel);
    root.setVisible(false);
    stage.addActor(root);
  }

  private void addCheckbox(String label, BooleanSupplier get, Consumer<Boolean> set) {
    CheckBox box = new CheckBox(" " + label, checkStyle);
    box.setChecked(get.getAsBoolean());
    box.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            set.accept(box.isChecked());
          }
        });
    panel.add(box).row();
  }

  public void toggle() {
    root.setVisible(!root.isVisible());
  }

  public boolean isOpen() {
    return root.isVisible();
  }

  /** Whether the panel is open and covers the given screen position (y down, in pixels). */
  public boolean isOver(int screenX, int screenY) {
    if (!isOpen()) {
      return false;
    }
    Vector2 p = stage.screenToStageCoordinates(new Vector2(screenX, screenY));
    return stage.hit(p.x, p.y, true) != null;
  }

  /** The stage, to be registered as an input processor ahead of the game controls. */
  public Stage stage() {
    return stage;
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
    boxOff.dispose();
    boxOn.dispose();
  }

  private static Texture solid(Color color, int size) {
    Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pm.setColor(color);
    pm.fill();
    Texture t = new Texture(pm);
    pm.dispose();
    return t;
  }

  /** A white outlined square, filled with a smaller square when checked. */
  private static Texture box(boolean checked) {
    Pixmap pm = new Pixmap(BOX_SIZE, BOX_SIZE, Pixmap.Format.RGBA8888);
    pm.setColor(Color.WHITE);
    pm.drawRectangle(0, 0, BOX_SIZE, BOX_SIZE);
    if (checked) {
      pm.fillRectangle(3, 3, BOX_SIZE - 6, BOX_SIZE - 6);
    }
    Texture t = new Texture(pm);
    pm.dispose();
    return t;
  }
}
