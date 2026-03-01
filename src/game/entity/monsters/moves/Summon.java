package game.entity.monsters.moves;

import java.util.List;
import java.util.Objects;

import affichage.gameModel.SimpleGameData;
import game.backpack.Backpack;
import game.entity.Hero;
import game.entity.Monster;
import game.utils.UtilsFunctions;

/**
 * Record for the Summon move.
 * Summon move creates another monster depending on the name.
 */
public record Summon(String nameSummon) implements Move{
	
	/**
	 * Constructor for Summon
	 * @param nameSummon the summon's name
	 */
	public Summon {
		Objects.requireNonNull(nameSummon);
	}

	/**
	 * Summon a new monster, and add it to the monsters' list.
	 * @param monsters the list of monsters
	 * @param hero the hero.
	 */
	public void apply(Hero h, Backpack bp, Monster monster,
      List<Monster> monsters, SimpleGameData data) {
		UtilsFunctions.checkIfNonNull(List.of(h, bp, monster, monsters, data));
		monsters.add(Monster.createMonsterByName(nameSummon, h));
	}
}
