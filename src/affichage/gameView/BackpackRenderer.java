package affichage.gameView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import affichage.gameModel.SimpleGameData;
import game.item.ItemType;
import game.pos.Pos;

/**
 * Utility class responsible for rendering the static backpack grid and its
 * background. It handles drawing the slots, coloring them based on their status
 * (blocked, unlocked, etc.), and drawing the grid lines.
 */
public class BackpackRenderer {

	/**
	 * Draws the entire backpack grid structure. This includes the white background,
	 * the individual slots, and the grid lines.
	 *
	 * @param g        The graphics context used for drawing.
	 * @param data     The current game data containing the backpack state.
	 * @param bpX      The X coordinate of the top-left corner of the backpack.
	 * @param bpY      The Y coordinate of the top-left corner of the backpack.
	 * @param cellSize The size (width and height) of a single cell in pixels.
	 */
	public static void drawGrid(Graphics2D g, SimpleGameData data, int bpX, int bpY, int cellSize) {
		var rows = data.getBackpackRows();
		var cols = data.getBackpackCols();
		g.setColor(Color.WHITE);
		g.fillRect(bpX, bpY, cols * cellSize, rows * cellSize);
		for (var r = 0; r < rows; r++) {
			for (var c = 0; c < cols; c++) {
				drawSingleSlot(g, data, bpX, bpY, r, c, cellSize);
			}
		}
		drawGridLines(g, rows, cols, bpX, bpY, cols * cellSize, rows * cellSize, cellSize);
	}

	/**
	 * Draws a single slot of the backpack at the specified grid position. The color
	 * of the slot changes if it is blocked, normal, or selected for unlocking.
	 *
	 * @param g        The graphics context.
	 * @param data     The game data.
	 * @param bpX      The X origin of the backpack.
	 * @param bpY      The Y origin of the backpack.
	 * @param r        The row index of the slot.
	 * @param c        The column index of the slot.
	 * @param cellSize The pixel size of the cell.
	 */
	private static void drawSingleSlot(Graphics2D g, SimpleGameData data, int bpX, int bpY, int r, int c,
			int cellSize) {
		ItemType type = data.getBackpackSlotType(c, r);
		var x = bpX + c * cellSize;
		var y = bpY + r * cellSize;
		Color baseColor = (type == ItemType.BLOCKED) ? Color.GRAY : Color.WHITE;
		if (data.isUnlockingSlots() && data.getSelectedUnlockSlots().contains(new Pos(c, r))) {
			baseColor = new Color(144, 238, 144);
		}
		g.setColor(baseColor);
		g.fillRect(x, y, cellSize, cellSize);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(x, y, cellSize, cellSize);
	}

	/**
	 * Draws the grid lines overlaying the backpack slots.
	 *
	 * @param g        The graphics context.
	 * @param rows     The number of rows in the backpack.
	 * @param cols     The number of columns in the backpack.
	 * @param bpX      The X origin.
	 * @param bpY      The Y origin.
	 * @param w        The total width of the backpack.
	 * @param h        The total height of the backpack.
	 * @param cellSize The size of a single cell.
	 */
	private static void drawGridLines(Graphics2D g, int rows, int cols, int bpX, int bpY, int w, int h, int cellSize) {
		g.setColor(Color.DARK_GRAY);
		g.setStroke(new BasicStroke(2));
		for (var i = 0; i <= cols; i++) {
			var x = bpX + i * cellSize;
			g.drawLine(x, bpY, x, bpY + h);
		}
		for (var j = 0; j <= rows; j++) {
			var y = bpY + j * cellSize;
			g.drawLine(bpX, y, bpX + w, y);
		}
		g.setStroke(new BasicStroke(1));
	}
}