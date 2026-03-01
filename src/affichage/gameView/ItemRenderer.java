package affichage.gameView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import affichage.Direction;
import affichage.FloatingItems;
import affichage.ImageLoader;
import affichage.gameModel.SimpleGameData;

import game.dungeon.Room;
import game.dungeon.RoomType;
import game.event.Merchant;
import game.item.Item;
import game.item.ItemType;

/**
 * Utility class responsible for rendering items in various contexts: inside the
 * backpack, floating in the world (loot/shop), or being dragged by the cursor.
 */
public class ItemRenderer {

	/**
	 * Draws all items currently placed inside the backpack grid.
	 *
	 * @param g        The graphics context.
	 * @param data     The game data.
	 * @param bpX      The backpack X origin.
	 * @param bpY      The backpack Y origin.
	 * @param cellSize The size of a cell.
	 * @param images   The map of loaded images.
	 */
	public static void drawItems(Graphics2D g, SimpleGameData data, int bpX, int bpY, int cellSize,
			Map<String, BufferedImage> images) {
		var items = data.getItemsToDraw();
		for (var item : items) {
			var pos = item.getPos();
			var x = bpX + pos.x() * cellSize;
			var y = bpY + pos.y() * cellSize;
			drawGenericItem(g, item, x, y, cellSize);
		}
	}

	/**
	 * Draws items that are floating on the screen (e.g., loot rewards or Merchant
	 * shop items). If the current room is a shop, displays the price tag below the
	 * item.
	 *
	 * @param g        The graphics context.
	 * @param data     The game data.
	 * @param cellSize The size of a cell.
	 * @param images   The map of loaded images.
	 */
	public static void drawFloatingItems(Graphics2D g, SimpleGameData data, int cellSize,
			Map<String, BufferedImage> images) {
		var currentRoom = data.getDungeon().getRoom(data.getHeroMapY(), data.getHeroMapX());
		for (var floating : data.getFloatingItems()) {
			var item = floating.item();
			var shape = item.getShape();
			var startX = floating.x() - (shape.columns() * cellSize) / 2;
			var startY = floating.y() - (shape.lines() * cellSize) / 2;
			drawGenericItem(g, item, startX, startY, cellSize);
			if (currentRoom.getType() == RoomType.MERCHANT && currentRoom.getMerchant().getShop().contains(item)) {
				drawItemPrice(g, item, floating.x(), floating.y(), cellSize);
			}
		}
	}

	/**
	 * Draws the item currently being dragged by the mouse pointer.
	 *
	 * @param g        The graphics context.
	 * @param data     The game data.
	 * @param cellSize The size of a cell.
	 * @param images   The map of loaded images.
	 */
	public static void drawMovingItem(Graphics2D g, SimpleGameData data, int cellSize,
			Map<String, BufferedImage> images) {
		var item = data.getMovedItem();
		if (item == null)
			return;
		var shape = item.getShape();
		var startX = data.getMouseX() - (shape.columns() * cellSize) / 2;
		var startY = data.getMouseY() - (shape.lines() * cellSize) / 2;
		drawGenericItem(g, item, startX, startY, cellSize);
	}

	/**
	 * Internal helper to draw a single generic item at specific coordinates. Checks
	 * if the item is drawable (not VOID or BLOCKED) before rendering.
	 *
	 * @param g        The graphics context.
	 * @param item     The item to draw.
	 * @param x        The X coordinate.
	 * @param y        The Y coordinate.
	 * @param cellSize The size of a cell.
	 */
	private static void drawGenericItem(Graphics2D g, Item item, int x, int y, int cellSize) {
		var name = item.getInfos().name();
		if (name.equals("Void") || name.equals("Blocked"))
			return;
		var dir = item.getShape().dir();
		var img = ImageLoader.getImage(name, dir);
		if (img != null) {
			drawItemShape(g, item, x, y, cellSize, img);
		}
	}

	/**
	 * Renders the item's visual shape using the provided image. Handles specific
	 * rendering logic for Curses (tiled) vs normal items (stretched).
	 *
	 * @param g        The graphics context.
	 * @param item     The item to render.
	 * @param startX   The X start position.
	 * @param startY   The Y start position.
	 * @param cellSize The size of a cell.
	 * @param img      The image to draw.
	 */
	public static void drawItemShape(Graphics2D g, Item item, int startX, int startY, int cellSize, BufferedImage img) {
		var shape = item.getShape();
		if (item.getInfos().type() == ItemType.CURSE) {
			for (var r = 0; r < shape.lines(); r++) {
				for (var c = 0; c < shape.columns(); c++) {
					if (r < shape.shape().length && c < shape.shape()[r].length && shape.shape()[r][c]) {
						g.drawImage(img, startX + c * cellSize, startY + r * cellSize, cellSize, cellSize, null);
					}
				}
			}
		} else {
			var w = shape.columns() * cellSize;
			var h = shape.lines() * cellSize;
			g.drawImage(img, startX, startY, w, h, null);
		}
	}

	/**
	 * Draws the price tag of an item centered below it (used in Merchant rooms).
	 *
	 * @param g        The graphics context.
	 * @param item     The item.
	 * @param x        The X center coordinate.
	 * @param y        The Y center coordinate.
	 * @param cellSize The size of a cell.
	 */
	private static void drawItemPrice(Graphics2D g, Item item, int x, int y, int cellSize) {
		var price = Merchant.definePrice(item);
		g.setColor(Color.YELLOW);
		g.setFont(new Font("Arial", Font.BOLD, 14));
		var h = item.getShape().lines() * cellSize;
		g.drawString(price + " G", x, y - (h / 2) - 5);
	}
}