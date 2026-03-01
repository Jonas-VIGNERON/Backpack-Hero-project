package game.fight;

import static game.entity.monsters.ListOfMonster.setMonstersNextMoves;
import static game.entity.monsters.moves.Move.doMove;

import java.util.List;
import java.util.Objects;
import affichage.gameModel.SimpleGameData;
import game.effect.Effect;
import game.entity.Target;
import game.entity.monsters.ListOfMonster;
import game.pos.Pos;
import game.utils.UtilsFunctions;

/**
 * Contains the fight's implementation.
 */
public class Fight {

	/**
	 * Applies effects, set monsters' moves and set items' stats before the hero
	 * turn.
	 * 
	 * @param h            is the hero
	 * @param monstersTeam represents the monsters' team
	 * @param bp           is the backpack
	 */
	public static void beforeHeroTurn(SimpleGameData data) {
		Objects.requireNonNull(data);
		var h = data.getHero();
		var monstersTeam = data.getCurrentMonsters();
		Target.applyBeforeTurnEffects(h);
		ifMonstersDie(data);
		h.heroStatsBeforeTurn();
		setMonstersNextMoves(monstersTeam);
		data.getBackpack().setItemStatsBeforeTurn(h, monstersTeam);
	}

	/**
	 * Do moves from the click position (used an item) and allow to choose the
	 * monsters that we want to attack
	 * 
	 * @param pos          represents the click position
	 * @param h            represents the hero
	 * @param monstersTeam is the monsters' team
	 * @param bp           is the backpack
	 */
	public static void doMovesFromPos(Pos pos, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(data, pos));
		var bp = data.getBackpack();
		var monstersTeam = data.getCurrentMonsters();
		var choice = bp.getItemFromPos(pos);
		if (!choice.isAnItem()) {
		} else {
			var target = data.getSelectedMonster();
			int targetIndex = monstersTeam.indexOf(target);
			if (targetIndex == -1 && !monstersTeam.isEmpty()) {
				targetIndex = 0;
			}
			if (target != null && targetIndex >= 0) {
				choice.onUse(data.getHero(), monstersTeam, targetIndex, choice, bp);
			}
		}
	}

	/**
	 * Does monsters' moves
	 * 
	 * @param monstersTeam represents the team of monsters
	 * @param h            is the hero
	 * @param bp           represents the backpack
	 */
	public static void doMonstersMoves(SimpleGameData data) {
		Objects.requireNonNull(data);
		var h = data.getHero();
		var bp = data.getBackpack();
		var monstersTeam = data.getCurrentMonsters();
		// I didn't use a stream here because the list could be modified if a monster
		// summon another
		for (var i = 0; i < monstersTeam.size(); i++) {
			var monster = monstersTeam.get(i);
			var moves = monster.getNextMoves();
			monster.initBeforeTurn();
			for (var move : moves) {
				doMove(h, bp, monster, move, monstersTeam, data);
			}
		}
	}

	/**
	 * Function that increases the hero's xp, and leveled him up if at least one
	 * monster is dead
	 * 
	 * @param h
	 * @param monstersTeam
	 * @param bp
	 */
	public static void ifMonstersDie(SimpleGameData data) {
		Objects.requireNonNull(data);
		var h = data.getHero();
		int xp = ListOfMonster.xpGetByKillingMonster(data.getCurrentMonsters());
		h.addXp(xp);
		if (h.increaseLevelIfPossible()) {
			data.enterUnlockMode();
		}
	}

	/**
	 * Applies effects after the hero turn.
	 * 
	 * @param h            is the hero
	 * @param monstersTeam represents the monsters' team
	 * @param bp           is the backpack
	 */
	public static void afterHeroTurn(SimpleGameData data) {
		Objects.requireNonNull(data);
		var monstersTeam = data.getCurrentMonsters();
		var h = data.getHero();
		var bp = data.getBackpack();
		UtilsFunctions.checkIfNonNull(List.of(monstersTeam, h, bp));
		// Applies effects
		Target.applyEndTurnEffects(h);
		Effect.reduceEffectsStacksAfterTurn(h);
		monstersTeam.forEach(m -> Target.applyBeforeTurnEffects(m));
		ifMonstersDie(data);
		// Does the monsters' moves
		doMonstersMoves(data);
		// Applies effects
		monstersTeam.forEach(m -> Target.applyEndTurnEffects(m));
		monstersTeam.forEach(m -> Effect.reduceEffectsStacksAfterTurn(m));
	}

}
