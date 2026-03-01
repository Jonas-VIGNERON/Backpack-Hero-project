package game.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import game.backpack.Backpack;
import game.effect.EffectType;
import game.entity.monsters.moves.TypeOfMoves;


/**
 * Class that implements the hero.
 */
public final class Hero implements Target{
	private int maxHp = 40;
	private int hp = 40;
	private int energy = 3;
	private int mana = 0;
	private int shield = 0;
	private int level = 1;
	private int xp = 0;
	private TypeOfMoves lastMove = null;
	private final List<Integer> listOfXpNeedToLevelUp = List.of(0, 10, 25, 45, 70, 100, 135, 175, 220, 270);
	private ArrayList<EffectType> effects = new ArrayList<>();
	
	
	/*********Functions below allow to collect hero's properties in other files.***********/

	
	/**
	 * Gets the maximum of HP the hero can have
	 * 
	 * @return int
	 */
	public int getMaxHp() {
		return maxHp;
	}
	
	/**
	 * Gets the current hero's Hp
	 * 
	 * @return int
	 */
	public int getHp() {
		return hp;
	}
	
	
	/**
	 * Gets the hero's energy
	 * 
	 * @return int
	 */
	public int getEnergy() {
		return energy;
	}
	
	/**
	 * Gets the hero's mana
	 * 
	 * @return int
	 */
	public int getMana() {
		return mana;
	}
	
	
	/**
	 * Gets the hero's shield
	 * 
	 * @return int
	 */
	public int getShield() {
		return shield;
	}
	
	
	/**
	 * Gets the hero's level
	 * 
	 * @return int
	 */
	public int getLevel() {
		return level;
	}
	
	
	/**
	 * Gets the hero's xp
	 * 
	 * @return int
	 */
	public int getXp() {
		return xp;
	}
	
	/**
	 * Gets the hero's last move
	 * This one is useful if there is a living shadow in the monsters' team
	 * 
	 * @return TypeOfMoves
	 */
	public TypeOfMoves getLastMove() {
		return lastMove;
	}
	/**
	 * Gets the list which contains the hero's effects.
	 * 
	 * @return ArrayList<String, Effect>
	 */
	public ArrayList<EffectType> getEffects(){
		return effects;
	}
	
	/************* Functions below permits to reduce the hero's statistics. *****************/


	/**
	 * Reduces the hero's energy.
	 * And return true if reducing the energy is possible, else false.
	 * 
	 * @param reduce the amount of energy reduced
	 * @return boolean
	 */
	public boolean reduceEnergy(int reduce) {
		if (reduce < 0) {
			throw new IllegalArgumentException("reduce must not be negative");
		}
	    return Target.reduceStat(reduce, () -> energy, v -> energy = v);
	}

	/**
	 * Reduces the hero's mana.
	 * And return true if reducing the mana is possible, else false.
	 * 
	 * @param reduce the amount of mana reduced
	 * @return boolean
	 */
	public  boolean reduceMana(int reduce) {
		if (reduce < 0) {
			throw new IllegalArgumentException("reduce must not be negative");
		}
	    return Target.reduceStat(reduce, () -> mana, v -> mana = v);
	}

	
	/**
	 * Deals damage to the Hero.
	 * 
	 * @param damage represents the damage dealt to the hero
	 */
	public void takeDamage(int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative");
		}
		damage = Target.damageTakenAfterApplyingEffects(this, damage);
		damage = Target.applyDodge(this, damage);
		Target.takeDamage(damage, () -> shield, v -> shield = v, () -> hp, value -> hp = value);
	}
	
	/**
	 * Deals damage to the hero by ignoring his shield
	 * 
	 * @param damage represents the damage dealt to the hero
	 */
	public void takeDamageIgnoringShield(int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative");
		}
		hp -= damage;
	}

	/********** Functions below permits to increase the hero's statistics. *************/
	
	public void increaseMaxHp(int addHp) {
		if (addHp < 0) {
			throw new IllegalArgumentException("addHp must not be negative");
		}
		maxHp += addHp;
		hp += addHp;
	}
	
	
	/**
	 * Functions used to increase the hero's statistics
	 * 
	 * @param plusStat the additional stat
	 * @param getter contains the statistic that will be increase
	 * @param setter contains the assignment to be performed
	 * @param f contains a binary function
	 */
	private void addStats(int plusStat, IntSupplier getter, IntConsumer setter, IntBinaryOperator f) {
		setter.accept(f.applyAsInt(getter.getAsInt(), plusStat));
	}
	
	
	/**
	 * Adds shield to the hero
	 * 
	 * @param moreShield represents the additional shield
	 */
	public void addShield(int moreShield) {
		if (moreShield < 0) {
			throw new IllegalArgumentException("moreShield must not be negative");
		}
		moreShield = Target.shieldAfterEffects(this, moreShield);
		addStats(moreShield, () -> shield, value -> shield = value, (s, v) -> s + v);
	}
	
	
	/**
	 * Adds hp to the hero
	 * 
	 * @param moreHp represents the additional hp
	 */
	public void addHp(int moreHp) {
		if (moreHp < 0) {
			throw new IllegalArgumentException("moreHp must not be negative");
		}
		addStats(moreHp, () -> hp, value -> hp = value, (h, v) -> (h + v) > maxHp ? maxHp : h + v);
	}
	
	/**
	 * Adds energy to the hero
	 *
	 * @param moreEnergy represents the additional energy
	 */
	public void addEnergy(int moreEnergy) {
		if (moreEnergy < 0) {
			throw new IllegalArgumentException("moreEnergy must not be negative");
		}
		addStats(moreEnergy, () -> energy, value -> energy = value, (e, v) -> e + v);
	}
	
	
	/**
	 * Levels up the hero if he has enough xp
	 * If so return true, else false.
	 * 
	 * @return boolean
	 */
	public boolean increaseLevelIfPossible() {
		var xpToLevelUp = listOfXpNeedToLevelUp.get(level);
		if(xp >= xpToLevelUp) {
			xp -= xpToLevelUp;
			level++;
			return true;
		}
		return false;
	}
	
	
	/**
	 * Adds xp to the hero
	 * 
	 * @param moreXp represents the additional xp
	 */
	public void addXp(int moreXp) {
		if (moreXp < 0) {
			throw new IllegalArgumentException("moreXp must not be negative");
		}
		addStats(moreXp, () -> xp, value -> xp = value, (x, v) -> x + v);
	}
	
	/***************************** Functions below are used to set the heros' statistics ********************************************/
	
	/**
	 * Set the last hero's move
	 * 
	 * @param m the move's type
	 */
	public void setLastMove(TypeOfMoves m) {
		Objects.requireNonNull(m);
		lastMove = m;
	}
	
	/**
	 * Modify the amount of mana
	 * 
	 * @param m the new amount of mana.
	 */
	public void setMana(int m) {
		if (m < 0) {
			throw new IllegalArgumentException("m must not be negative");
		}
		mana = m;
	}
	
	/**
	 * Sets the hero's stats before his turn. 
	 */
	public void heroStatsBeforeTurn() {
		energy = 3;
		shield = 0;
	}
	
	
	/**
	 * Initializes hero's stats before his turn.
	 * 
	 * @param bp the backpack (used to get the amount of mana)
	 */
	public void heroStatsBeforeFight(Backpack bp) {
		Objects.requireNonNull(bp);
		lastMove = null;
		effects = new ArrayList<>();
		bp.countMana(this);
	}
	
	/**
	 * Removes all the effects apply to the hero.
	 */
	public void removeAllEffects() {
		effects = new ArrayList<>();
	}

	/**
	 * Return true if the hero's xp is enough to level up, otherwise false.
	 * 
	 * @param moreXp represents the additional xp.
	 * @return boolean
	 */
	public boolean canLevelUp(int moreXp) {
		if (moreXp < 0) {
			throw new IllegalArgumentException("moreXp must not be negative");
		}
		addXp(moreXp);
		return increaseLevelIfPossible();
	}
}
