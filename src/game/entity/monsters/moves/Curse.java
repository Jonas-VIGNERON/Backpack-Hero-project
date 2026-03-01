package game.entity.monsters.moves;

import java.util.List;
import java.util.Objects;

import affichage.gameModel.SimpleGameData;
import game.backpack.Backpack;
import game.entity.Hero;
import game.entity.Monster;
import game.item.Item;
import game.utils.UtilsFunctions;

/**
 * Record for the Curse Move
 */
public record Curse(Item i) implements Move {
	
	/**
	 * Constructor for Curse
	 * 
	 * @param i the item that represents the curse
	 */
	public Curse{
		Objects.requireNonNull(i);
	}

	/**
	 * Trigger the curse mode in the game data. * @param data The game data to
	 * switch state
	 */
	public void apply(Hero h, Backpack bp, Monster monster,
      List<Monster> monsters, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(h, bp, monster, monsters, data));
		data.triggerCurse(i);
	}
}
