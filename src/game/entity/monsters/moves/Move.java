package game.entity.monsters.moves;

import java.util.List;

import affichage.gameModel.SimpleGameData;
import game.backpack.Backpack;
import game.entity.Hero;
import game.entity.Monster;
import game.utils.UtilsFunctions;

/**
 * Interface for moves.
 */
public sealed interface Move permits Attack, Shield, Poison, Summon, Dodge, Curse {
	public String toString();
	public void apply(Hero h, Backpack bp, Monster monster,
      List<Monster> monsters, SimpleGameData data);

	/**
	 * Does the move m
	 * 
	 * @param h       is the hero
	 * @param bp      is the backpack
	 * @param monster is the monster
	 * @param m       is the move
	 * @param data    is the game data
	 */
	public static void doMove(Hero h, Backpack bp, Monster monster, Move m,
      List<Monster> monsters, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(h, bp, monster, m, monsters, data));
		m.apply(h, bp, monster, monsters, data);
		}

}
