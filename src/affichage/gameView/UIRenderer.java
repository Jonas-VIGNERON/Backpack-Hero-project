package affichage.gameView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.github.forax.zen.ScreenInfo;

import affichage.ImageLoader;
import affichage.gameModel.SimpleGameData;
import game.dungeon.Room;
import game.dungeon.RoomType;
import game.item.Item;

/**
 * Utility class responsible for rendering general UI elements such as buttons,
 * text overlays, and specific interface screens (Curse, Healer).
 */
public class UIRenderer {

  /**
   * Draws global buttons like "End Turn" or "Validate Unlock" depending on the
   * game state.
   *
   * @param g    The graphics context.
   * @param info The screen info.
   * @param data The game data.
   */
  public static void drawGlobalButtons(Graphics2D g, ScreenInfo info, SimpleGameData data) {
    if (!data.getCurrentMonsters().isEmpty()) {
      var x = info.width() - SimpleGameView.BUTTON_WIDTH - 50;
      var y = info.height() - SimpleGameView.BUTTON_HEIGHT - 50;
      drawButton(g, x, y, SimpleGameView.BUTTON_WIDTH, SimpleGameView.BUTTON_HEIGHT, "FIN DE TOUR", Color.RED);
    }
    if (data.isUnlockingSlots()) {
      drawValidateButton(g, info, data);
    }
  }

  /**
   * Draws the button to toggle between Map view and Backpack view.
   *
   * @param g    The graphics context.
   * @param data The game data.
   * @param info The screen info.
   */
  public static void drawMapToggleButton(Graphics2D g, SimpleGameData data, ScreenInfo info) {
    if (!data.getCurrentMonsters().isEmpty())
      return;
    var x = info.width() - SimpleGameView.MAP_BUTTON_W - 50;
    var text = data.isShowMap() ? "Back to Bag" : "Show Map";
    drawButton(g, x, SimpleGameView.MAP_BUTTON_Y, SimpleGameView.MAP_BUTTON_W, SimpleGameView.MAP_BUTTON_H, text,
        Color.GRAY);
  }

  /**
   * Draws the Curse Overlay screen, forcing the player to accept or refuse a
   * curse. Delegates drawing tasks to sub-methods to keep code clean.
   *
   * @param g      The graphics context.
   * @param info   The screen info.
   * @param data   The game data containing the pending curse.
   * @param images The loaded images.
   */
  public static void drawCurseOverlay(Graphics2D g, ScreenInfo info, SimpleGameData data,
      Map<String, BufferedImage> images) {
    drawOverlayBackgroundAndTitle(g, info);
    drawPendingCurseItem(g, info, data);
    drawCurseButtons(g, info, data);
  }

  /**
   * Draws the semi-transparent background and the title of the overlay. * @param
   * g The graphics context.
   * 
   * @param info The screen info.
   */
  private static void drawOverlayBackgroundAndTitle(Graphics2D g, ScreenInfo info) {
    g.setColor(new Color(0, 0, 0, 220));
    g.fillRect(0, 0, info.width(), info.height());
    int cx = info.width() / 2;
    int cy = info.height() / 2;
    var title = "CURSE !";
    g.setColor(new Color(148, 0, 211));
    g.setFont(new Font("Arial", Font.BOLD, 40));
    g.drawString(title, cx - g.getFontMetrics().stringWidth(title) / 2, cy - 150);
  }

  /**
   * Draws the shape of the curse item currently pending. * @param g The graphics
   * context.
   * 
   * @param info The screen info.
   * @param data The game data.
   */
  private static void drawPendingCurseItem(Graphics2D g, ScreenInfo info, SimpleGameData data) {
    var item = data.getPendingCurseItem();
    if (item == null)
      return;
    var img = ImageLoader.getImage(item.getInfos().name(), null);
    if (img != null) {
      var cx = info.width() / 2;
      var cy = info.height() / 2;
      var startX = cx - (item.getShape().columns() * 64) / 2;
      var startY = cy - (item.getShape().lines() * 64) / 2 - 50;
      ItemRenderer.drawItemShape(g, item, startX, startY, 64, img);
    }
  }

  /**
   * Draws the "Accept" and "Refuse" buttons with dynamic damage calculation.
   * * @param g The graphics context.
   * 
   * @param info The screen info.
   * @param data The game data.
   */
  private static void drawCurseButtons(Graphics2D g, ScreenInfo info, SimpleGameData data) {
    var cx = info.width() / 2;
    var cy = info.height() / 2;
    drawButton(g, cx - 250, cy + 50, 200, 60, "ACCEPT", new Color(34, 139, 34));
    var dmg = (data.getCurseRefusalCount() + 1) * 5;
    drawButton(g, cx + 50, cy + 50, 200, 60, "REFUSE (-" + dmg + ")", new Color(178, 34, 34));
  }

  /**
   * Renders room-specific interfaces, such as the Healer's choices.
   *
   * @param g    The graphics context.
   * @param info The screen info.
   * @param data The game data.
   */
  public static void drawRoomSpecificUI(Graphics2D g, ScreenInfo info, SimpleGameData data) {
    var currentRoom = data.getDungeon().getRoom(data.getHeroMapY(), data.getHeroMapX());
    if (currentRoom.getType() == RoomType.HEALER) {
      drawHealerInterface(g, info, currentRoom);
    } else if (currentRoom.getType() == RoomType.MERCHANT) {
      drawMerchantSellZone(g, info, data);
    }
  }

  /**
   * Helper to draw the Healer interface with available healing options.
   *
   * @param g    The graphics context.
   * @param info The screen info.
   * @param room The healer room containing the choices.
   */
  private static void drawHealerInterface(Graphics2D g, ScreenInfo info, Room room) {
    var btnW = 420;
    var btnH = 60;
    var startX = (info.width() - btnW) / 2;
    var startY = 100 + (5 * 64) + 150;
    var gap = 80;
    var choices = room.getHealer().getChoices();
    var i = 0;
    for (var text : choices.keySet()) {
      drawButton(g, startX, startY + i * gap, btnW, btnH, text, new Color(50, 200, 50));
      i++;
    }
  }

  /**
   * Draws the "Validate" button when the player is unlocking slots.
   *
   * @param g    The graphics context.
   * @param info The screen info.
   * @param data The game data.
   */
  private static void drawValidateButton(Graphics2D g, ScreenInfo info, SimpleGameData data) {
    var btnW = 200;
    var btnH = 60;
    var x = (info.width() - btnW) / 2;
    var y = info.height() - btnH - 50;

    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.PLAIN, 18));
    var txt = "You can unlock " + data.getRemainingUnlocks() + " more slot(s)";
    drawCenteredText(g, txt, Color.WHITE, x + btnW / 2, y - 10);
    drawButton(g, x, y, btnW, btnH, "Validate", new Color(0, 200, 0));
  }

  /**
   * Generic helper to draw a styled rectangular button with centered text.
   *
   * @param g    The graphics context.
   * @param x    The X coordinate.
   * @param y    The Y coordinate.
   * @param w    The width of the button.
   * @param h    The height of the button.
   * @param text The text to display.
   * @param bg   The background color.
   */
  public static void drawButton(Graphics2D g, int x, int y, int w, int h, String text, Color bg) {
    g.setColor(bg);
    g.fillRect(x, y, w, h);
    g.setColor(Color.WHITE);
    g.setStroke(new BasicStroke(2));
    g.drawRect(x, y, w, h);
    g.setFont(new Font("Arial", Font.BOLD, 18));
    var fm = g.getFontMetrics();
    g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h - fm.getHeight()) / 2 + fm.getAscent());
  }

  /**
   * Helper to draw centered text at a specific Y coordinate.
   *
   * @param g       The graphics context.
   * @param text    The text to draw.
   * @param color   The text color.
   * @param centerX The X center position.
   * @param y       The Y coordinate.
   * @return The Y coordinate where the next line of text should be drawn (useful
   *         for lists).
   */
  public static int drawCenteredText(Graphics2D g, String text, Color color, int centerX, int y) {
    g.setColor(color);
    var width = g.getFontMetrics().stringWidth(text);
    g.drawString(text, centerX - width / 2, y);
    return y + 30;
  }

  /**
   * Draws the "Sell Item" zone in the Merchant room. Displays a golden
   * rectangular area where players can drop items to sell them.
   *
   * @param g    The graphics context.
   * @param info The screen info.
   * @param data The game data.
   */
  private static void drawMerchantSellZone(Graphics2D g, ScreenInfo info, SimpleGameData data) {
    var cols = data.getBackpackCols();
    var bpWidth = cols * 64;
    var x = (info.width() - bpWidth) / 2 + bpWidth + 20;
    var y = 100;
    var w = SimpleGameView.SELL_BTN_W;
    var h = SimpleGameView.SELL_BTN_H;
    g.setColor(new Color(218, 165, 32));
    g.fillRect(x, y, w, h);
    g.setColor(Color.WHITE);
    g.setStroke(new BasicStroke(3));
    g.drawRect(x, y, w, h);
    g.setFont(new Font("Arial", Font.BOLD, 20));
    drawCenteredText(g, "SELL", Color.WHITE, x + w / 2, y + h / 2 - 10);
    g.setFont(new Font("Arial", Font.PLAIN, 14));
    drawCenteredText(g, "(Drop Here)", Color.LIGHT_GRAY, x + w / 2, y + h / 2 + 20);
  }
}