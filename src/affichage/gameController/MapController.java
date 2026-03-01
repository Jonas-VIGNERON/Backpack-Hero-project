package affichage.gameController;

import java.util.List;
import java.util.stream.IntStream;

import com.github.forax.zen.ScreenInfo;

import affichage.FloatingItems;
import affichage.gameModel.SimpleGameData;
import game.dungeon.Room;
import game.dungeon.RoomType;
import game.item.Item;
import game.pos.Pos;

/**
 * Controller responsible for managing map interactions, hero movement, and room
 * encounter logic.
 */
public class MapController {

	/**
	 * Handles a click on the map screen to move the hero.
	 *
	 * @param x    Mouse X coordinate.
	 * @param y    Mouse Y coordinate.
	 * @param data The game data model.
	 * @param info Screen information.
	 */
	public static void clickMap(int x, int y, SimpleGameData data, ScreenInfo info) {
		var cellSize = 80;
		var mapW = data.getDungeon().getCols() * cellSize;
		var mapH = data.getDungeon().getRows() * cellSize;
		var startX = (info.width() - mapW) / 2;
		var startY = (info.height() - mapH) / 2;
		if (x < startX || x > startX + mapW || y < startY || y > startY + mapH)
			return;
		var targetCol = (x - startX) / cellSize;
		var targetRow = (y - startY) / cellSize;

		moveHeroIfPossible(data, targetRow, targetCol);
	}

	/**
	 * Calculates the path to the target cell and moves the hero step by step. Stops
	 * if a special room is in the way
	 *
	 * @param data      The game data model.
	 * @param targetR   Target row index.
	 * @param targetCol Target column index.
	 */
	private static void moveHeroIfPossible(SimpleGameData data, int targetR, int targetCol) {
		var path = data.getDungeon().getPath(data.getHeroMapY(), data.getHeroMapX(), targetR, targetCol);
		if (path != null && !path.isEmpty()) {
			for (int i = 1; i < path.size(); i++) {
				Pos step = path.get(i);
				if (!processStep(data, step))
					return;
			}
			data.clearFloatingItems();
			handleRoomEncounter(data, data.getHeroMapY(), data.getHeroMapX());
		}
	}

	/**
	 * Processes a single movement step of the hero. Checks for locked doors (Gates)
	 * and ambushes.
	 *
	 * @param data The game data model.
	 * @param step The position the hero is trying to enter.
	 * @return true if the movement was successful, false if blocked or interrupted.
	 */
	private static boolean processStep(SimpleGameData data, Pos step) {
		var room = data.getDungeon().getRoom(step.y(), step.x());
		if (room.getType() == RoomType.GATE) {
			var key = data.getBackpack().getItemByName("Key");
			if (key != null) {
				data.getBackpack().removeItem(key.getPos());
				data.getDungeon().setRoom(step.y(), step.x(), new Room(RoomType.EMPTY));
			} else {
				data.clearFloatingItems();
				return false;
			}
		}
		data.setHeroMapPosition(step.x(), step.y());
		if (room.getType() == RoomType.ENEMY && room.hasLivingMonsters()) {
			data.clearFloatingItems();
			handleRoomEncounter(data, step.y(), step.x());
			return false;
		}
		return true;
	}

	/**
	 * Triggers the specific logic associated with the room the hero just entered.
	 *
	 * @param data The game data model.
	 * @param r    Row index of the room.
	 * @param c    Column index of the room.
	 */
	private static void handleRoomEncounter(SimpleGameData data, int r, int c) {
		var room = data.getDungeon().getRoom(r, c);
		if (data.isShowMap() && room.getType() != RoomType.EMPTY && room.getType() != RoomType.BLOCKED) {
			data.toggleMap();
		}
		switch (room.getType()) {
		case ENEMY -> {
			data.getHero().heroStatsBeforeFight(data.getBackpack());
			data.startCombat(room.getMonsters());
			game.fight.Fight.beforeHeroTurn(data);
		}
		case EXIT -> data.nextLevel();
		case HEALER -> data.clearFloatingItems();
		case TREASURE -> addRewardToListOfFloatingItem(data);
		case MERCHANT -> setupMerchantShop(data, room);
		default -> data.clearFloatingItems();
		}
	}

	/**
	 * Checks if a click corresponds to a special room interaction (e.g. Healer).
	 *
	 * @param mx   Mouse X.
	 * @param my   Mouse Y.
	 * @param data Game data.
	 * @param info Screen info.
	 * @return true if a special room interaction occurred.
	 */
	public static boolean handleSpecialRoomClick(int mx, int my, SimpleGameData data, ScreenInfo info) {
		var currentRoom = data.getDungeon().getRoom(data.getHeroMapY(), data.getHeroMapX());
		if (currentRoom.getType() == RoomType.HEALER) {
			return UIController.clickHealer(mx, my, data, info, currentRoom);
		}
		return false;
	}

	/**
	 * Initializes the Merchant's shop display by converting items to FloatingItems.
	 *
	 * @param data The game data.
	 * @param room The merchant room.
	 */
	private static void setupMerchantShop(SimpleGameData data, Room room) {
		data.clearFloatingItems();
		var merchant = room.getMerchant();
		var shopItems = merchant.getShop();
		var backpackBottom = 100 + (data.getBackpackRows() * 64);
		var startY = backpackBottom + 250;
		var startX = 650;
		var gapX = 130;
		for (int i = 0; i < shopItems.size(); i++) {
			Item item = shopItems.get(i);
			data.getFloatingItems().add(new FloatingItems(item, startX + (i * gapX), startY));
		}
		room.setVisited(true);
	}

	/**
	 * Spawns reward items floating on the screen when entering a Treasure room.
	 *
	 * @param data The game data.
	 */
	public static void addRewardToListOfFloatingItem(SimpleGameData data) {
		var dungeon = data.getDungeon();
		var room = dungeon.getRoom(data.getHeroMapY(), data.getHeroMapX());
		var startY = 100 + (data.getBackpackRows() * 64) + 250;
		var startX = 650;
		var deltaX = 130;
		var list = room.getRewards();
		if (!list.isEmpty()) {
			var floatingItems = IntStream.range(0, list.size())
					.mapToObj(i -> new FloatingItems(list.get(i), startX + i * deltaX, startY)).toList();
			floatingItems.forEach(fi -> data.getFloatingItems().add(fi));
		}
	}
}