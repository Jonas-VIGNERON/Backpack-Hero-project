package game.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import game.entity.Monster;
import game.event.Healer;
import game.event.Merchant;
import game.item.Item;
import game.item.ItemsCreation;
import game.item.ListOfItems;

/**
 * Represents a single room within the dungeon floor. It holds state about
 * monsters, rewards, interactive events (Merchant/Healer), and visitation
 * status.
 */
public class Room {
	private final RoomType type;
	private List<Monster> monsters = new ArrayList<>();
	private int gold;
	private List<Item> rewards;
	private Merchant merchant;
	private Healer healer;
	private boolean isVisited = false;

	/**
	 * Creates a new Room with the specified type.
	 *
	 * @param type The type of the room
	 */
	public Room(RoomType type) {
		this.type = Objects.requireNonNull(type);
	}

	/**
	 * Gets the type of this room.
	 *
	 * @return The RoomType.
	 */
	public RoomType getType() {
		return type;
	}

	/**
	 * Generates a Gold item based on the accumulated gold value of the room.
	 *
	 * @return A Gold item.
	 */
	private Item getGold() {
		return ItemsCreation.createGold(gold);
	}

	/**
	 * Retrieves and consumes the rewards in this room. The rewards list is cleared
	 * after calling this method to prevent infinite farming.
	 *
	 * @return The list of items found in the room.
	 */
	public List<Item> getRewards() {
		var res = rewards;
		rewards = new ArrayList<>();
		return res;
	}

	/**
	 * Sets the list of monsters present in this room.
	 *
	 * @param monsters The list of monsters.
	 */
	public void setMonsters(List<Monster> monsters) {
		this.monsters = monsters;
	}

	/**
	 * Gets the list of monsters in this room.
	 *
	 * @return The list of monsters.
	 */
	public List<Monster> getMonsters() {
		return monsters;
	}

	/**
	 * Checks if there are any living monsters remaining in the room.
	 * 
	 * @return true if monsters are present, false otherwise.
	 */
	public boolean hasLivingMonsters() {
		return !monsters.isEmpty();
	}

	/**
	 * Calculates the total gold value of the room based on the monsters present.
	 * Used to generate loot when the room is cleared.
	 */
	private void defineGold() {
		if (!hasLivingMonsters()) {
			gold = 0;
			return;
		}
		gold = monsters.stream().filter(Objects::nonNull).mapToInt(Monster::defineGoldWhenDead).sum();
	}

	/**
	 * Generates random rewards for this room.
	 */
	public void setRewards() {
		var l = new ListOfItems();
		rewards = l.getListOfRandomItems(4);
		defineGold();
		var goldItem = getGold();
		if (goldItem.getNumberOfUses() > 0) {
			rewards.add(goldItem);
		}
	}

	/**
	 * Checks if the room is theoretically accessible
	 *
	 * @return true if accessible, false if it is the Exit.
	 */
	public boolean isAccessible() {
		return type != RoomType.EXIT;
	}

	/**
	 * Gets the Merchant associated with this room. Initializes it if it doesn't
	 * exist yet
	 * 
	 * @return The Merchant instance.
	 */
	public Merchant getMerchant() {
		if (this.type == RoomType.MERCHANT && this.merchant == null) {
			this.merchant = new Merchant();
		}
		return this.merchant;
	}

	/**
	 * Gets the Healer associated with this room. Initializes it if it doesn't exist
	 * yet
	 *
	 * @return The Healer instance.
	 */
	public Healer getHealer() {
		if (this.type == RoomType.HEALER && this.healer == null) {
			this.healer = new Healer();
		}
		return this.healer;
	}

	/**
	 * Checks if the player has visited this room.
	 *
	 * @return true if visited.
	 */
	public boolean isVisited() {
		return isVisited;
	}

	/**
	 * Marks the room as visited or not.
	 *
	 * @param visited The visitation status.
	 */
	public void setVisited(boolean visited) {
		isVisited = visited;
	}
}