package game.entity.monsters.moves;

import java.util.List;

import affichage.gameModel.SimpleGameData;
import game.backpack.Backpack;
import game.entity.Hero;
import game.entity.Monster;
import game.utils.UtilsFunctions;


/**
 * Record for the Attack move
 */
public record Attack(int damage) implements Move{
	public Attack {
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative !");
		}
	}
	
	/**
	 * Attacks the hero
	 * 
	 * @param h represents the hero
	 */
	public void apply(Hero h, Backpack bp, Monster monster,
      List<Monster> monsters, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(h, bp, monster, monsters, data));
		h.takeDamage(damage);
	}
}
