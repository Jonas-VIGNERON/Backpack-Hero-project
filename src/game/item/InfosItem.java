package game.item;

import java.awt.Image;
import java.util.List;

import game.utils.UtilsFunctions;

/**
 * Record that stocks the unmodifiable informations about items.
 */
public record InfosItem(String name, ItemType type, Rarity rarity, boolean usable, boolean unlimitedUses, Image img) {
	
	public InfosItem{
		UtilsFunctions.checkIfNonNull(List.of(name, type, rarity, img));
	}
}
