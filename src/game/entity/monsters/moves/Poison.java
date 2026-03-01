package game.entity.monsters.moves;

import java.util.List;

import affichage.gameModel.SimpleGameData;
import game.backpack.Backpack;
import game.effect.Effect;
import game.effect.EffectType;
import game.entity.Hero;
import game.entity.Monster;
import game.utils.UtilsFunctions;

/**
 * Record for the Poison move.
 */
public record Poison(int stacks) implements Move{
	
	/**
	 * Poison's constructor
	 * 
	 * @param stacks the amount of poison
	 */
	public Poison {
		if (stacks < 0) {
			throw new IllegalArgumentException("stacks must not be negative");
		}
	}
	
	/**
	 * Add poison effect to the hero.
	 * 
	 * @param h
	 */
	public void apply(Hero h, Backpack bp, Monster monster,
      List<Monster> monsters, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(h, bp, monster, monsters, data));
		Effect.addEffect(h, EffectType.POISON, stacks);
	}
}
