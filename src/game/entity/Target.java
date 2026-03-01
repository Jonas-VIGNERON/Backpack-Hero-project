package game.entity;

import static game.effect.EffectsCreation.getEffectFromType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import game.effect.EffectType;
import game.utils.UtilsFunctions;

public sealed interface Target permits Monster, Hero{
	public void takeDamageIgnoringShield(int damage);
	public void takeDamage(int damage);
	public void addHp(int hpPlus);
	public ArrayList<EffectType> getEffects();
	public void removeAllEffects();
	
	
	/**
	 * Return the attack damage after applying the effects.
	 * 
	 * @param t hero/monster
	 * @param damage represents the initial damage
	 * @return int
	 */
	public static int damageDealsWithEffects(Target t, int damage) {
		Objects.requireNonNull(t);
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative");
		}
		var effectsType = t.getEffects();
		for (var type : effectsType) {
			damage = getEffectFromType(type).modifyDamageDealt().apply(damage);
		}
		return damage;
	}
	
	/**
	 * Return the remaining damage dealt to the target after applying his effects.
	 * 
	 * @param t is the target of the attack
	 * @param baseDamage represents the initial damage dealt
	 * @return int
	 */
	public static int damageTakenAfterApplyingEffects(Target t, int damage) {
		Objects.requireNonNull(t);
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative");
		}
		var effectsType = t.getEffects();
		for (var type : effectsType) {
			if (type != EffectType.DODGE) {				
				damage = getEffectFromType(type).modifyDamageTaken().apply(damage);
			}
		}
		return damage >= 0 ? damage : 0;
	}

	/**
	 * Return the shield after effects are applied
	 * 
	 * @param moreShield represents the additional shield
	 * @return int
	 */
	public static int shieldAfterEffects(Target t, int moreShield) {
		Objects.requireNonNull(t);
		if (moreShield < 0) {
			throw new IllegalArgumentException("moreShield must not be negative");
		}
		var effectsType = t.getEffects();
		for (var type : effectsType) {
			moreShield = getEffectFromType(type).modifyShield().apply(moreShield);
		}
		return moreShield >= 0 ? moreShield: 0;
	}
	
	/**
	 * Applies the hero's effect before his turn
	 */
	public static void applyBeforeTurnEffects(Target t) {
		Objects.requireNonNull(t);
		var effectsType = t.getEffects();
		for (var type : effectsType) {
			getEffectFromType(type).onStartTurn().accept(t);
		}
	}

	/**
	 * Applies the hero's effect after his turn
	 */
	public static void applyEndTurnEffects(Target t) {
		Objects.requireNonNull(t);
		var effectsType = t.getEffects();
		for (var type : effectsType) {
			getEffectFromType(type).onEndTurn().accept(t);
		}
	}
	
	
	/**
	 * If there is a dodge in the map that contains the target's effects we dodge an attack and return 0.
	 * Is there isn't return damage.
	 * 
	 * @param t is the target (hero or monster)
	 * @param damage represent the initial damage
	 * @return int
	 */
	public static int applyDodge(Target t, int damage) {
		Objects.requireNonNull(t);
		if (damage < 0) {
			throw new IllegalArgumentException("damage must not be negative");
		}
		var effectsType = t.getEffects();
		if (effectsType.remove(EffectType.DODGE)) {
			return 0;
		}
		return damage;
	}
	
	/**
	 * Checks if a statistic can be reduced. If so return {@code true} otherwise {@code false}
	 * 
	 * @param reduce the amount needed to reduce
	 * @param statToReduce the amount of statistic 
	 * @return boolean
	 */
	private static boolean canReduce(int reduce, int statToReduce) {
		return statToReduce >= reduce;
	}
	
	/**
	 * Reduces a statistic by a given amount if possible.
	 * The current value is obtained using {@code getter}.
	 * If the value can be reduced by {@code reduce} (i.e. it does not go below zero),
	 * the updated value is applied using {@code setter}.
	 *
	 * @param reduce the amount to reduce the statistic by
	 * @param getter supplies the current value of the statistic
	 * @param setter updates the statistic with the reduced value
	 *
	 * @return {@code true} if the statistic was successfully reduced,
	 *         {@code false} if the reduction was not possible
	 */
	public static boolean reduceStat(int reduce, IntSupplier getter, IntConsumer setter) {
		UtilsFunctions.checkIfNonNull(List.of(getter, setter));
		if (reduce < 0) {
			throw new IllegalArgumentException("reduce must not be negative");
		}
        int current = getter.getAsInt();
        if (canReduce(reduce, current)) {
            setter.accept(current - reduce);
            return true;
        }
        return false;
    }

	/**
	 * Reduces a shield value and returns any remaining damage.
	 * If the shield is sufficient, it absorbs the entire {@code reduce} amount and no damage remains.
	 * If the shield is insufficient, it is reduced to zero and the leftover damage is returned.
	 *
	 * @param reduce the amount of damage to apply to the shield
	 * @param shieldGetter supplies the current shield value
	 * @param shieldSetter updates the shield value
	 *
	 * @return the remaining damage that could not be absorbed by the shield
	 */
    private static int reduceShield(int reduce, IntSupplier shieldGetter, IntConsumer shieldSetter) {
        int shield = shieldGetter.getAsInt();
        if (canReduce(reduce, shield)) {
        	shieldSetter.accept(shield - reduce);
        	return 0;
        }
        int leftover = reduce - shield;
        shieldSetter.accept(0);
        return leftover;
    }

    /**
     * Reduces hit points by a given amount, clamping the value to zero.
     * If the hit points cannot be reduced by {@code reduce} without going below zero,
     * the hit points are set to zero.
     *
     * @param reduce
     *        the amount of damage to apply
     * @param hpGetter supplies the current hit points
     * @param hpSetter updates the hit points
     */
    private static void reduceHp(int reduce, IntSupplier hpGetter, IntConsumer hpSetter) {
        if (!reduceStat(reduce, hpGetter, hpSetter)) {
            hpSetter.accept(0);
        }
    }

    /**
     * Applies damage to a target by reducing its shield first, then its hp.
     * The damage is absorbed by the shield as a priority.
     * Any remaining damage after the shield is depleted is then applied to hp.
     *
     * @param damage the total amount of damage to apply
     * @param shieldGetter supplies the current shield value
     * @param shieldSetter updates the shield value
     * @param hpGetter supplies the current hit points
     * @param hpSetter updates the hit points
     */
    public static void takeDamage(int damage, IntSupplier shieldGetter, IntConsumer shieldSetter,
                                  IntSupplier hpGetter, IntConsumer hpSetter) {
    	UtilsFunctions.checkIfNonNull(List.of(shieldGetter, shieldSetter, hpGetter, hpSetter));
    	if (damage < 0) {
    		throw new IllegalArgumentException("damage must not be negative");
    	}
        int leftover = reduceShield(damage, shieldGetter, shieldSetter);
        reduceHp(leftover, hpGetter, hpSetter);
    }
	
}
