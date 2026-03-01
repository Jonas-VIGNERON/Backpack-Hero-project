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
 * Record for the Dodge move.
 * Dodge allow the entity to dodge an attack
 */
public record Dodge() implements Move{
	/**
	 * Add a dodge effect on the monster who used Dodge
	 * 
	 * @param m
	 */
	public void apply(Hero h, Backpack bp, Monster monster,
      List<Monster> monsters, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(h, bp, monster, monsters, data));
		Effect.addEffect(monster, EffectType.DODGE, 1);
	}
}
