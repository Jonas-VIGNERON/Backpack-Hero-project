package game.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import game.entity.Hero;
import game.entity.monsters.ListOfMonster;
import game.pos.Pos;

/**
 * Utility class responsible for the procedural generation of dungeon floors. It
 * creates a random path, adds extra rooms, and populates them with enemies,
 * treasures, etc.
 */
public class DungeonGenerator {

	private static final int ROWS = 5;
	private static final int COLS = 11;
	private static final Random random = new Random();

	/**
	 * Generates a new random dungeon floor.
	 * 
	 * @param hero The hero instance
	 * @return A fully generated DungeonFloor.
	 */
	public static DungeonFloor generate(Hero hero) {
		Room[][] tempGrid = initBlockedGrid();
		var openPositions = new ArrayList<Pos>();
		generateMainPath(tempGrid, openPositions);
		addExtraRooms(tempGrid, openPositions);
		populateRooms(tempGrid, openPositions, hero);
		return convertToFloor(tempGrid);
	}

	/**
	 * Initializes a 5x11 grid filled with BLOCKED rooms.
	 * 
	 * @return The initialized blocked grid.
	 */
	private static Room[][] initBlockedGrid() {
		Room[][] grid = new Room[ROWS][COLS];
		for (var r = 0; r < ROWS; r++) {
			for (var c = 0; c < COLS; c++) {
				grid[r][c] = new Room(RoomType.BLOCKED);
			}
		}
		return grid;
	}

	/**
	 * Generates a random path from left (2,0) to right.
	 * 
	 * @param grid          The dungeon grid.
	 * @param openPositions List to track open rooms.
	 * @return The position of the Exit.
	 */
	private static Pos generateMainPath(Room[][] grid, List<Pos> openPositions) {
		var currR = 2;
		var currC = 0;
		grid[currR][currC] = new Room(RoomType.EMPTY);
		while (currC < COLS - 1) {
			var rdm = random.nextDouble();
			if (rdm < 0.6 || (currR == 0 && rdm < 0.8) || (currR == ROWS - 1 && rdm < 0.8)) {
				currC++;
			} else if (rdm < 0.8 && currR > 0) {
				currR--;
			} else if (currR < ROWS - 1) {
				currR++;
			}
			if (grid[currR][currC].getType() == RoomType.BLOCKED) {
				grid[currR][currC] = new Room(RoomType.EMPTY);
				openPositions.add(new Pos(currC, currR));
			}
		}
		grid[currR][currC] = new Room(RoomType.EXIT);
		openPositions.remove(new Pos(currC, currR));
		return new Pos(currC, currR);
	}

	/**
	 * Adds extra rooms branching from the main path to create non-linear dungeons.
	 * 
	 * @param grid          The dungeon grid.
	 * @param openPositions List of available positions.
	 */
	private static void addExtraRooms(Room[][] grid, List<Pos> openPositions) {
		var extraRooms = 10 + random.nextInt(5);
		var attempts = 0;
		while (extraRooms > 0 && attempts < 100) {
			var origin = new Pos(0, 2);
			if (!openPositions.isEmpty()) {
				origin = openPositions.get(random.nextInt(openPositions.size()));
			}
			var neighbor = getRandomNeighbor(origin);
			if (isValid(neighbor) && grid[neighbor.y()][neighbor.x()].getType() == RoomType.BLOCKED) {
				grid[neighbor.y()][neighbor.x()] = new Room(RoomType.EMPTY);
				openPositions.add(neighbor);
				extraRooms--;
			}
			attempts++;
		}
	}

	/**
	 * Assigns types (Enemy, Treasure, etc.) to the generated empty rooms. *
	 * 
	 * @param grid          The dungeon grid.
	 * @param openPositions The list of empty room positions.
	 * @param hero          The hero (for scaling).
	 */
	private static void populateRooms(Room[][] grid, List<Pos> openPositions, Hero hero) {
		Collections.shuffle(openPositions);
		var minEnemies = 2;
		for (int i = 0; i < minEnemies && !openPositions.isEmpty(); i++) {
			var p = openPositions.remove(0);
			grid[p.y()][p.x()] = createEnemyRoom(hero);
		}
		for (var p : openPositions) {
			assignRoomType(grid, p, hero);
		}
	}

	/**
	 * Determines the type of a single room based on random probabilities. *
	 * 
	 * @param grid The dungeon grid.
	 * @param p    The position to assign.
	 * @param hero The hero.
	 */
	private static void assignRoomType(Room[][] grid, Pos p, Hero hero) {
		var roll = random.nextDouble();
		if (roll < 0.10)
			grid[p.y()][p.x()] = createEnemyRoom(hero);
		else if (roll < 0.20)
			grid[p.y()][p.x()] = createTreasureRoom();
		else if (roll < 0.30)
			grid[p.y()][p.x()] = new Room(RoomType.MERCHANT);
		else if (roll < 0.40)
			grid[p.y()][p.x()] = new Room(RoomType.HEALER);
		else if (roll < 0.50)
			grid[p.y()][p.x()] = new Room(RoomType.GATE);
	}

	/**
	 * Converts the temporary array to a List of Lists. *
	 * 
	 * @param grid The 2D array of rooms.
	 * @return The dungeon floor object.
	 */
	private static DungeonFloor convertToFloor(Room[][] grid) {
		List<List<Room>> finalMap = new ArrayList<>();
		for (int r = 0; r < ROWS; r++) {
			List<Room> row = new ArrayList<>();
			for (var c = 0; c < COLS; c++) {
				row.add(grid[r][c]);
			}
			finalMap.add(row);
		}
		return new DungeonFloor(finalMap);
	}

	/**
	 * Helper to create an Enemy Room.
	 * 
	 * @param hero The hero instance.
	 * @return A room populated with monsters.
	 */
	private static Room createEnemyRoom(Hero hero) {
		var room = new Room(RoomType.ENEMY);
		room.setMonsters(ListOfMonster.getTeamOfMonsters(hero));
		room.setRewards();
		return room;
	}

	/**
	 * Helper to create a Treasure Room.
	 * 
	 * @return A room populated with rewards.
	 */
	private static Room createTreasureRoom() {
		var room = new Room(RoomType.TREASURE);
		room.setRewards();
		return room;
	}

	/**
	 * Returns a random neighbor position (Up, Down, Left, Right).
	 * 
	 * @param p The origin position.
	 * @return A random adjacent position.
	 */
	private static Pos getRandomNeighbor(Pos p) {
		var dir = random.nextInt(4);
		return switch (dir) {
		case 0 -> new Pos(p.x() + 1, p.y());
		case 1 -> new Pos(p.x() - 1, p.y());
		case 2 -> new Pos(p.x(), p.y() + 1);
		default -> new Pos(p.x(), p.y() - 1);
		};
	}

	/**
	 * Checks if a position is within the grid bounds. *
	 * 
	 * @param p The position to check.
	 * @return true if valid, false otherwise.
	 */
	private static boolean isValid(Pos p) {
		return p.x() >= 0 && p.x() < COLS && p.y() >= 0 && p.y() < ROWS;
	}
}