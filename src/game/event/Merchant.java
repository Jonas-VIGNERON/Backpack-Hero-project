package game.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import game.backpack.Backpack;
import game.item.Item;
import game.item.ItemsCreation;
import game.item.ListOfItems;
import game.item.Rarity;

/**
 * Create a Merchant which sells and buys items.
 */
public class Merchant {
	public final ArrayList<Item> shop = createShop();
	public int maxSells = 3;
	
	/**
	 * Define the item's price depending of the rarity
	 * 
	 * @param i the item
	 * @return int the price of the item i
	 */
	public static int definePrice(Item i) {
		Objects.requireNonNull(i);
		return switch (i.getInfos().rarity()) {
		case Rarity.COMMON -> 4;
		case Rarity.UNCOMMON -> 6;
		case Rarity.RARE -> 12;
		case Rarity.LEGENDARY -> 20;
		default -> 0;
		};
	}
	
	/**
	 * Creates the shop
	 * @return ArrayList<Item>
	 */
	private ArrayList<Item> createShop(){
		var l = new ListOfItems();
		return l.getListOfRandomItems(6);
	}
	
	/**
	 * Shop's getter
	 * 
	 * @return List<Item> the shop
	 */
	public List<Item> getShop(){
		return shop;
	}
	
	/**
	 * Purchases an item from the shop it there is enough gold in the backpack.
	 * 
	 * @param index the index of the item
	 * @param bp the backpack
	 * @return Item the item bought
	 */
	public Item buyItem(int index, Backpack bp) {
		Objects.requireNonNull(bp);
		if (index < 0) {
			throw new IllegalArgumentException("index must not be negative");
		}
		if (bp.reduceGold(definePrice(shop.get(index)))) {
			return shop.remove(index);
		}
		return null;
	}
	
	
	/**
	 * Sells an item and get gold if the player sold less than 3 items. 
	 * 
	 * @param i the item
	 * @return Item or null;
	 */
	public Item sellItem(Item i) {
		Objects.requireNonNull(i);
		if (maxSells > 0) {
			maxSells--;
			return ItemsCreation.createGold(definePrice(i) / 2);
		}
		return null;
	}
}
