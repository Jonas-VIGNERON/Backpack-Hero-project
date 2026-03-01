package game.event;

import java.util.ArrayList;
import java.util.Objects;

import game.item.Item;
import game.item.ListOfItems;

/**
 * It is the Chest class.
 */
public class Chest{
	private final ArrayList<Item> listItems;

	/**
	 * It is the Chest's constructor
	 * 
	 * @param l contains all possible items
	 */
	public Chest (ListOfItems l) {
		Objects.requireNonNull(l);
		listItems = new ArrayList<>(l.getListOfRandomItems(7));
	}

	/**
	 * Get the chest's contents
	 * 
	 * @return
	 */
	public ArrayList<Item> getListItems(){
		return listItems;
	}
}
