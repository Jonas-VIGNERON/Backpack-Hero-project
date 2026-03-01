package game.dungeon;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import game.pos.Pos;

/**
 * Represents a specific floor of the dungeon containing a grid of rooms.
 * Provides methods to access rooms and calculate paths between them
 */
public class DungeonFloor {
	private final int rows = 5;
	private final int cols = 11;
	private final List<List<Room>> grid;

	/**
	 * Constructs a dungeon floor from a generated grid of rooms.
	 *
	 * @param grid The 2D list of rooms (must be 5x11).
	 * @throws IllegalArgumentException if dimensions are incorrect.
	 */
	public DungeonFloor(List<List<Room>> grid) {
		if (grid.size() != rows)
			throw new IllegalArgumentException("Invalid number of row");
		for (var row : grid) {
			if (row.size() != cols)
				throw new IllegalArgumentException("Invalid number of colums in a row");
		}
		this.grid = grid;
	}

	/**
	 * Retrieves the room at specific coordinates.
	 *
	 * @param r The row index.
	 * @param c The column index.
	 * @return The Room object.
	 */
	public Room getRoom(int r, int c) {
		return grid.get(r).get(c);
	}

	/**
	 * @return The total number of rows.
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @return The total number of columns.
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * Updates a room at specific coordinates.
	 *
	 * @param r       Row index.
	 * @param c       Column index.
	 * @param newRoom The new Room object to place.
	 */
	public void setRoom(int r, int c, Room newRoom) {
		if (r >= 0 && r < rows && c >= 0 && c < cols) {
			grid.get(r).set(c, newRoom);
		}
	}

	/**
	 * Finds the shortest path between two points in the dungeon using Dijkstra's
	 * algorithm.
	 *
	 * @param startR  Starting row.
	 * @param startC  Starting column.
	 * @param targetR Target row.
	 * @param targetC Target column.
	 * @return A list of Positions representing the path, or null if no path found.
	 */
	public List<Pos> getPath(int startR, int startC, int targetR, int targetC) {
		var pq = new PriorityQueue<Node>(Comparator.comparingInt(Node::cost));
		var dist = new HashMap<Pos, Integer>();
		var predecessors = new HashMap<Pos, Pos>();

		var start = new Pos(startC, startR);
		var target = new Pos(targetC, targetR);

		initSearch(pq, dist, predecessors, start);

		while (!pq.isEmpty()) {
			var current = pq.poll();
			if (current.cost > dist.getOrDefault(current.pos, Integer.MAX_VALUE))
				continue;
			if (current.pos.equals(target))
				return reconstructPath(predecessors, target);

			processNeighbors(current.pos, dist, predecessors, pq);
		}
		return null;
	}

	/**
	 * Initializes the search structures.
	 */
	private void initSearch(PriorityQueue<Node> pq, Map<Pos, Integer> dist, Map<Pos, Pos> pred, Pos start) {
		pq.add(new Node(start, 0));
		dist.put(start, 0);
		pred.put(start, null);
	}

	/**
	 * Processes all valid neighbors of the current position.
	 */
	private void processNeighbors(Pos curr, Map<Pos, Integer> dist, Map<Pos, Pos> pred, PriorityQueue<Node> pq) {
		int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
		for (var dir : directions) {
			var newR = curr.y() + dir[0];
			var newC = curr.x() + dir[1];
			checkNeighbor(curr, newR, newC, dist, pred, pq);
		}
	}

	/**
	 * Checks if a specific neighbor is valid and offers a shorter path.
	 */
	private void checkNeighbor(Pos curr, int r, int c, Map<Pos, Integer> dist, Map<Pos, Pos> pred,
			PriorityQueue<Node> pq) {
		if (r < 0 || r >= this.rows || c < 0 || c >= this.cols)
			return;
		Room room = this.getRoom(r, c);
		if (room.getType() == RoomType.BLOCKED)
			return;
		int weight = calculateWeight(room.getType());
		int newDist = dist.get(curr) + weight;
		Pos neighbor = new Pos(c, r);
		if (newDist < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
			dist.put(neighbor, newDist);
			pred.put(neighbor, curr);
			pq.add(new Node(neighbor, newDist));
		}
	}

	/**
	 * Determines the movement cost for entering a room. Empty rooms are cheap (1),
	 * others are expensive (10) to prefer safe paths.
	 */
	private int calculateWeight(RoomType type) {
		return (type == RoomType.EMPTY || type == RoomType.EXIT) ? 1 : 10;
	}

	/**
	 * Reconstructs the path from the predecessors map.
	 */
	private List<Pos> reconstructPath(Map<Pos, Pos> predecessors, Pos target) {
		List<Pos> path = new ArrayList<>();
		Pos curr = target;
		while (curr != null) {
			path.add(curr);
			curr = predecessors.get(curr);
		}
		Collections.reverse(path);
		return path;
	}

	/** Internal record for the PriorityQueue. */
	private record Node(Pos pos, int cost) {
	}
}