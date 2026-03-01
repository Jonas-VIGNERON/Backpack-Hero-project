package affichage.gameView;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Objects;

import com.github.forax.zen.ScreenInfo;

import affichage.ImageLoader;
import affichage.gameModel.SimpleGameData;

/**
 * Main View class acting as a Facade. It delegates the rendering of specific
 * components (Map, Entities, Items, UI) to specialized renderer classes.
 */
public record SimpleGameView() {
  public static final int BUTTON_WIDTH = 200;
  public static final int BUTTON_HEIGHT = 60;

  public static final int MAP_BUTTON_W = 150;
  public static final int MAP_BUTTON_H = 50;
  public static final int MAP_BUTTON_X = 50;
  public static final int MAP_BUTTON_Y = 50;

  public static final int SELL_BTN_W = 120;
  public static final int SELL_BTN_H = 120;

  /**
   * Main entry point to draw the entire game frame. * @param g The graphics
   * context used for drawing.
   * 
   * @param screenInfo Information about the screen dimensions.
   * @param data       The current game data (model).
   * @param images     The map of loaded images.
   */
  public void draw(Graphics2D g, ScreenInfo screenInfo, SimpleGameData data, Map<String, BufferedImage> images) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(screenInfo);
    Objects.requireNonNull(data);
    Objects.requireNonNull(images);
    drawBackground(g, screenInfo, images);
    UIRenderer.drawMapToggleButton(g, data, screenInfo);
    if (data.isShowMap()) {
      MapRenderer.drawMap(g, screenInfo, data);
    } else {
      drawBackpackScene(g, screenInfo, data, images);
    }
    if (data.isCurseMode()) {
      UIRenderer.drawCurseOverlay(g, screenInfo, data, images);
    }
  }

  /**
   * Orchestrates the drawing of the main backpack view (Grid, Entities, Items,
   * UI). * @param g The graphics context.
   * 
   * @param info   The screen info.
   * @param data   The game data.
   * @param images The images map.
   */
  private void drawBackpackScene(Graphics2D g, ScreenInfo info, SimpleGameData data,
      Map<String, BufferedImage> images) {
    var cols = data.getBackpackCols();
    var cellSize = 64;
    var bpWidth = cols * cellSize;
    var bpX = (info.width() - bpWidth) / 2;
    var bpY = 100;
    BackpackRenderer.drawGrid(g, data, bpX, bpY, cellSize);
    EntityRenderer.drawHero(g, data, bpX, bpY, cellSize);
    EntityRenderer.drawEnemies(g, data, bpX, bpY, bpWidth, cellSize);
    UIRenderer.drawRoomSpecificUI(g, info, data);
    ItemRenderer.drawItems(g, data, bpX, bpY, cellSize, images);
    ItemRenderer.drawFloatingItems(g, data, cellSize, images);
    ItemRenderer.drawMovingItem(g, data, cellSize, images);

    UIRenderer.drawGlobalButtons(g, info, data);
  }

  /**
   * Draws the background image or a black screen if not found.
   * 
   * @param g      The graphics context.
   * @param info   The screen info.
   * @param images The image map.
   */
  private void drawBackground(Graphics2D g, ScreenInfo info, Map<String, BufferedImage> images) {
    var bg = ImageLoader.getImage("Background", null);
    if (bg != null) {
      g.drawImage(bg, 0, 0, info.width(), info.height(), null);
    } else {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, info.width(), info.height());
    }
  }
}