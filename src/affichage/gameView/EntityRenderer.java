package affichage.gameView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import affichage.ImageLoader;
import affichage.gameModel.SimpleGameData;
import game.effect.Effect;
import game.effect.EffectType;
import game.entity.Hero;
import game.entity.Monster;
import game.entity.Target;
import game.entity.monsters.moves.*;

/**
 * Utility class responsible for rendering entities (Hero and Monsters). It
 * handles displaying sprites, statistics (HP, Mana, etc.), next moves, and
 * status effects.
 */
public class EntityRenderer {

	/**
	 * Draws the hero, their stats, and active effects on the screen.
	 *
	 * @param g        The graphics context.
	 * @param data     The game data.
	 * @param bpX      Backpack X start position (used for relative positioning).
	 * @param bpY      Backpack Y start position.
	 * @param cellSize The size of a cell.
	 */
	public static void drawHero(Graphics2D g, SimpleGameData data, int bpX, int bpY, int cellSize) {
		var centerX = bpX - 200;
		var groundY = bpY + (data.getBackpackRows() * cellSize) + 300;
		var h = data.getHero();
		drawEntitySprite(g, "Hero", centerX, groundY, Color.BLUE);
		var statsY = drawHeroStats(g, data, centerX, groundY);
		drawEffects(g, centerX - 50, statsY, h);
	}

	/**
	 * Draws the list of active enemies during combat.
	 *
	 * @param g        The graphics context.
	 * @param data     The game data.
	 * @param bpX      Backpack X start position.
	 * @param bpY      Backpack Y start position.
	 * @param bpWidth  Total width of the backpack.
	 * @param cellSize The size of a cell.
	 */
	public static void drawEnemies(Graphics2D g, SimpleGameData data, int bpX, int bpY, int bpWidth, int cellSize) {
		var monsters = data.getCurrentMonsters();
		if (monsters.isEmpty())
			return;

		var startX = bpX + bpWidth + 100;
		var groundY = bpY + (data.getBackpackRows() * cellSize) + 300;
		var gap = 180;

		for (var i = 0; i < monsters.size(); i++) {
			var m = monsters.get(i);
			var centerX = startX + (i * gap);
			drawSingleMonster(g, m, centerX, groundY);
			drawSelectionArrow(g, data, m, centerX, groundY);
		}
	}

	/**
	 * Draws a single monster, including sprite, stats, and planned moves.
	 *
	 * @param g       The graphics context.
	 * @param m       The monster to draw.
	 * @param centerX The X center position for the monster.
	 * @param groundY The Y position of the ground (feet level).
	 */
	private static void drawSingleMonster(Graphics2D g, Monster m, int centerX, int groundY) {
		drawEntitySprite(g, m.getName(), centerX, groundY, Color.RED);
		drawMonsterInfo(g, m, centerX, groundY);
		drawMonsterMoves(g, m, centerX - 40, groundY + 65);
	}

	/**
	 * Draws the sprite of an entity. Falls back to a colored oval if the image is
	 * missing.
	 *
	 * @param g             The graphics context.
	 * @param imageName     The key name of the image to load.
	 * @param centerX       The X center position.
	 * @param groundY       The Y ground position.
	 * @param fallbackColor The color to use if the image is not found.
	 */
	private static void drawEntitySprite(Graphics2D g, String imageName, int centerX, int groundY,
			Color fallbackColor) {
		var maxHeight = 200;
		var img = ImageLoader.getImage(imageName, null);
		if (img != null) {
			var ratio = (double) img.getWidth() / img.getHeight();
			var drawWidth = (int) (maxHeight * ratio);
			g.drawImage(img, centerX - (drawWidth / 2), groundY - maxHeight, drawWidth, maxHeight, null);
		} else {
			g.setColor(fallbackColor);
			g.fillOval(centerX - 50, groundY - 100, 100, 100);
		}
	}

	/**
	 * Draws the text statistics for the hero (HP, Energy, Mana, etc.).
	 *
	 * @param g       The graphics context.
	 * @param data    The game data.
	 * @param centerX The X center position.
	 * @param groundY The Y reference position.
	 * @return The Y coordinate where the next element should be drawn.
	 */
	private static int drawHeroStats(Graphics2D g, SimpleGameData data, int centerX, int groundY) {
		var h = data.getHero();
		g.setFont(new Font("Arial", Font.BOLD, 20));
		var y = groundY + 30;
		y = UIRenderer.drawCenteredText(g, "HP: " + h.getHp() + " / " + h.getMaxHp(), Color.WHITE, centerX, y);
		y = UIRenderer.drawCenteredText(g, "Energy: " + h.getEnergy(), Color.GREEN, centerX, y);
		y = UIRenderer.drawCenteredText(g, "Mana: " + h.getMana(), Color.BLUE, centerX, y);
		y = UIRenderer.drawCenteredText(g, "Block: " + h.getShield(), Color.CYAN, centerX, y);
		y = UIRenderer.drawCenteredText(g, "Gold: " + data.getBackpack().getGolds(), Color.YELLOW, centerX, y);
		return y;
	}

	/**
	 * Draws the name, HP, and Shield of a monster.
	 *
	 * @param g       The graphics context.
	 * @param m       The monster.
	 * @param centerX The X center position.
	 * @param groundY The Y reference position.
	 */
	private static void drawMonsterInfo(Graphics2D g, Monster m, int centerX, int groundY) {
		g.setFont(new Font("Arial", Font.BOLD, 16));
		g.setColor(Color.WHITE);
		var nameW = g.getFontMetrics().stringWidth(m.getName());
		g.drawString(m.getName(), centerX - (nameW / 2), groundY - 215);
		String hp = m.getHp() + " HP";
		var hpW = g.getFontMetrics().stringWidth(hp);
		g.drawString(hp, centerX - (hpW / 2), groundY + 25);
		if (m.getShield() > 0) {
			g.setColor(Color.CYAN);
			var sh = "Shld: " + m.getShield();
			var shW = g.getFontMetrics().stringWidth(sh);
			g.drawString(sh, centerX - (shW / 2), groundY + 45);
		}
	}

	/**
	 * Draws the planned moves for a monster below its sprite.
	 *
	 * @param g      The graphics context.
	 * @param m      The monster.
	 * @param x      The X position to start drawing.
	 * @param startY The Y position to start drawing.
	 */
	private static void drawMonsterMoves(Graphics2D g, Monster m, int x, int startY) {
		if (m.getNextMoves() == null || m.getNextMoves().isEmpty())
			return;
		var currentY = startY;
		for (var move : m.getNextMoves()) {
			drawSingleMove(g, move, x + 20, currentY);
			currentY += 20;
		}
		drawEffects(g, x + 20, currentY, m);
	}

	/**
	 * Renders a single move description text (e.g., "Attack 7").
	 *
	 * @param g    The graphics context.
	 * @param move The move to render.
	 * @param x    The X coordinate.
	 * @param y    The Y coordinate.
	 */
	private static void drawSingleMove(Graphics2D g, Move move, int x, int y) {
		var txt = "?";
		switch (move) {
		case Attack a -> {
			g.setColor(new Color(255, 100, 100));
			txt = "Attack " + a.damage();
		}
		case Shield s -> {
			g.setColor(Color.CYAN);
			txt = "Block " + s.shield();
		}
		case Poison p -> {
			g.setColor(Color.GRAY);
			txt = "Poison " + p.stacks();
		}
		case Summon s -> {
			g.setColor(Color.PINK);
			txt = "Summon " + s.nameSummon();
		}
		case Dodge _ -> {
			g.setColor(Color.BLUE);
			txt = "Dodge";
		}
		case Curse _ -> {
			g.setColor(Color.BLACK);
			txt = "Curse";
		}
		}
		g.drawString(txt, x, y);
	}

	/**
	 * Draws the active status effects on a target (Poison, Regen, etc.).
	 *
	 * @param g The graphics context.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param t The target (Hero or Monster).
	 */
	private static void drawEffects(Graphics2D g, int x, int y, Target t) {
		g.setFont(new Font("Arial", Font.BOLD, 15));
		var mapEffects = Effect.listToMap(t.getEffects());
		for (var entry : mapEffects.entrySet()) {
			g.setColor(getEffectColor(entry.getKey()));
			g.drawString(entry.getKey() + " : " + entry.getValue(), x, y);
			y += 30;
		}
	}

	/**
	 * Maps an EffectType to a specific Color for rendering.
	 *
	 * @param type The effect type.
	 * @return The corresponding color.
	 */
	private static Color getEffectColor(EffectType type) {
		return switch (type) {
		case BURN -> Color.ORANGE;
		case DODGE -> Color.GRAY;
		case FREEZE -> Color.getHSBColor(200f / 360f, 0.45f, 0.90f);
		case HASTE -> Color.MAGENTA;
		case POISON -> Color.getHSBColor(270f / 360f, 0.55f, 0.80f);
		case RAGE -> Color.RED;
		case REGEN -> Color.getHSBColor(100f / 360f, 0.25f, 0.95f);
		default -> Color.WHITE;
		};
	}

	/**
	 * Draws a red arrow above the selected monster to indicate targeting.
	 *
	 * @param g       The graphics context.
	 * @param data    The game data.
	 * @param m       The monster to check.
	 * @param centerX The X center position of the monster.
	 * @param groundY The Y ground position.
	 */
	private static void drawSelectionArrow(Graphics2D g, SimpleGameData data, Monster m, int centerX, int groundY) {
		if (m == data.getSelectedMonster()) {
			g.setColor(Color.RED);
			var arrowY = groundY - 260;
			int[] xPoints = { centerX - 15, centerX + 15, centerX };
			int[] yPoints = { arrowY, arrowY, arrowY + 20 };
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
}