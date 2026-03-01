package affichage.gameView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.github.forax.zen.ScreenInfo;

import affichage.gameModel.SimpleGameData;
import game.dungeon.Room;
import game.dungeon.RoomType;
import game.pos.Pos;

/**
 * Utility class responsible for rendering the dungeon map. It displays visited
 * rooms, the current path, and the hero's position.
 */
public class MapRenderer {

	private static final int MAP_CELL_SIZE = 80;

	/**
	 * Draws the complete map interface centered on the screen.
	 *
	 * @param g    The graphics context.
	 * @param info The screen information (width/height).
	 * @param data The game data containing the dungeon and history.
	 */
	public static void drawMap(Graphics2D g, ScreenInfo info, SimpleGameData data) {
		var dungeon = data.getDungeon();
		var mapW = dungeon.getCols() * MAP_CELL_SIZE;
		var mapH = dungeon.getRows() * MAP_CELL_SIZE;
		var startX = (info.width() - mapW) / 2;
		var startY = (info.height() - mapH) / 2;

		for (var r = 0; r < dungeon.getRows(); r++) {
			for (var c = 0; c < dungeon.getCols(); c++) {
				drawMapCell(g, dungeon.getRoom(r, c), startX, startY, r, c, data);
			}
		}
		drawPathLine(g, data, startX, startY);
		drawHeroOnMap(g, data, startX, startY);
	}

	/**
	 * Draws a single cell (room) of the map. Hidden rooms are drawn in black.
	 *
	 * @param g      The graphics context.
	 * @param room   The room object to draw.
	 * @param startX The X offset of the map.
	 * @param startY The Y offset of the map.
	 * @param r      The row index.
	 * @param c      The column index.
	 * @param data   The game data used to check visibility.
	 */
	private static void drawMapCell(Graphics2D g, Room room, int startX, int startY, int r, int c,
			SimpleGameData data) {
		var x = startX + c * MAP_CELL_SIZE;
		var y = startY + r * MAP_CELL_SIZE;
		if (room.getType() == RoomType.BLOCKED)
			return;
		if (!data.isRoomVisible(r, c)) {
			g.setColor(Color.BLACK);
			g.fillRect(x, y, MAP_CELL_SIZE, MAP_CELL_SIZE);
			return;
		}

		g.setColor(getRoomColor(room.getType()));
		g.fillRect(x, y, MAP_CELL_SIZE, MAP_CELL_SIZE);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(x, y, MAP_CELL_SIZE, MAP_CELL_SIZE);
	}

	/**
	 * Determines the color of a room based on its type (Enemy, Treasure, Merchant,
	 * etc.).
	 *
	 * @param type The type of the room.
	 * @return The corresponding Color object.
	 */
	private static Color getRoomColor(RoomType type) {
		return switch (type) {
		case EMPTY -> Color.LIGHT_GRAY;
		case ENEMY -> new Color(200, 100, 100);
		case TREASURE -> Color.YELLOW;
		case MERCHANT -> Color.ORANGE;
		case HEALER -> Color.GREEN;
		case GATE -> Color.MAGENTA;
		case EXIT -> Color.WHITE;
		default -> Color.GRAY;
		};
	}

	/**
	 * Draws the path history line connecting visited rooms on the map.
	 *
	 * @param g      The graphics context.
	 * @param data   The game data containing the path history.
	 * @param startX The X offset of the map.
	 * @param startY The Y offset of the map.
	 */
	private static void drawPathLine(Graphics2D g, SimpleGameData data, int startX, int startY) {
		var path = data.getPathHistory();
		if (path.size() < 2)
			return;
		g.setColor(new Color(255, 215, 0, 150));
		g.setStroke(new BasicStroke(4));
		var half = MAP_CELL_SIZE / 2;
		for (var i = 0; i < path.size() - 1; i++) {
			var p1 = path.get(i);
			var p2 = path.get(i + 1);
			g.drawLine(startX + p1.x() * MAP_CELL_SIZE + half, startY + p1.y() * MAP_CELL_SIZE + half,
					startX + p2.x() * MAP_CELL_SIZE + half, startY + p2.y() * MAP_CELL_SIZE + half);
		}
		g.setStroke(new BasicStroke(1));
	}

	/**
	 * Draws the hero indicator (a blue circle) on the current map position.
	 *
	 * @param g      The graphics context.
	 * @param data   The game data containing hero position.
	 * @param startX The X offset of the map.
	 * @param startY The Y offset of the map.
	 */
	private static void drawHeroOnMap(Graphics2D g, SimpleGameData data, int startX, int startY) {
		var hX = startX + data.getHeroMapX() * MAP_CELL_SIZE;
		var hY = startY + data.getHeroMapY() * MAP_CELL_SIZE;
		g.setColor(Color.BLUE);
		g.fillOval(hX + 20, hY + 20, MAP_CELL_SIZE - 40, MAP_CELL_SIZE - 40);
	}
}