package game.context;

import java.util.List;

import game.backpack.Backpack;
import game.entity.Hero;
import game.entity.Monster;
import game.item.Item;
import game.utils.UtilsFunctions;

public record Context(Hero hero, List<Monster> monsters, int index, Backpack backpack, Item itemSource) {

	public Context{
		UtilsFunctions.checkIfNonNull(List.of(hero, monsters, backpack, itemSource));
	}
	
	/**
	 * Targeted monster's getter
	 * 
	 * @return Monster the targeted monster
	 */
	public Monster getTargetedMonster() {
		return monsters.get(index);
	}
	
	
	/**
	 * Add damage
	 * 
	 * @param value is the additional damage.
	 */
	public void addBonusDamage(int value) {
	    itemSource.addBonusDamage(value);
	}
	
	/**
	 * Add shield
	 * 
	 * @param value is the additional shield.
	 */
	public void addBonusShield(int value) {
	    itemSource.addBonusShield(value);
	}
}
