package game.entity.monsters.moves;

import java.util.List;

import affichage.gameModel.SimpleGameData;
import game.backpack.Backpack;
import game.entity.Hero;
import game.entity.Monster;
import game.utils.UtilsFunctions;

/**
 * Record for the Shield move.
 * Add shield to the entity who do this move.
 */
public record Shield(int shield) implements Move{
	public Shield {
		if (shield < 0) {
			throw new IllegalArgumentException("Additional shield must not be negative ! ");
		}
	}
	
	/**
	 * Gives shield to the monster m
	 * 
	 * @param m represents the monster
	 */
	public void apply(Hero h, Backpack bp, Monster monster,
      List<Monster> monsters, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(h, bp, monster, monsters, data));
		monster.addShield(shield);
	}
}
