package game.effect;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import game.entity.Target;


/**
 * Class to creates all the possible effects.
 */
public class EffectsCreation {
	private final static HashMap<EffectType, Effect> mapOfEffect = initMapEffect();

	/**
	 * Associate the type with an effect in a HashMap
	 * 
	 * @return HashMap<EffectType, Effect>
	 */
	private static HashMap<EffectType, Effect> initMapEffect(){
		var mapOfEffect = new HashMap<EffectType, Effect>();
		mapOfEffect.put(EffectType.BURN, createBurn());
		mapOfEffect.put(EffectType.DODGE, createDodge());
		mapOfEffect.put(EffectType.FREEZE, createFreeze());
		mapOfEffect.put(EffectType.HASTE, createHaste());
		mapOfEffect.put(EffectType.POISON, createPoison());
		mapOfEffect.put(EffectType.RAGE, createRage());
		mapOfEffect.put(EffectType.REGEN, createRegen());
		mapOfEffect.put(EffectType.SLOW, createSlow());
		return mapOfEffect;
	}
	
	/**
	 * Gets the effect depending on the type.
	 * @param type the effect's type.
	 * @return
	 */
	public static Effect getEffectFromType(EffectType type) {
		Objects.requireNonNull(type);
		return mapOfEffect.get(type);
	}
	
	/**
	 * Creates the burn effect.
	 * Burn deals damage to the entity who has it.
	 * 
	 * @return Effect the burn effect
	 */
	public static Effect createBurn() {
		Consumer<Target> onStartTurn = t -> t.takeDamage(1);
		return new EffectBuilder("Burn", EffectType.BURN, onStartTurn, null, null, null, null);
	}
	
	/**
	 * Creates the dodge effect.
	 * Allows the entity who has it to dodge an attack.
	 * 
	 * @return Effect the dodge effect.
	 */
	public static Effect createDodge() {
		return new EffectBuilder("Dodge", EffectType.DODGE, null, null, null, (_ -> 0), null);
	}
	
	/**
	 * Creates the Freeze effect.
	 * Freeze increase the damages that the entity takes.
	 * 
	 * @return Effect the freeze effect
	 */
	public static Effect createFreeze() {
		Function<Integer, Integer> modifyDamageTaken = (damage -> damage + 1);
		return new EffectBuilder("Freeze", EffectType.FREEZE, null, null, null, modifyDamageTaken, null);
	}

	/**
	 * Creates the Haste effect.
	 * Haste increase the entity's shield.
	 * 
	 * @return Effect the haste effect
	 */
	public static Effect createHaste() {
		Function<Integer, Integer> modifyShield = (shield -> shield + 1);
		return new EffectBuilder("Haste", EffectType.HASTE, null, null, null, null, modifyShield);
	}
	
	
	/**
	 * Creates the Poison effect.
	 * Poison deals damage to the entity who has it regardless if he has shield or not.
	 * 
	 * @return Effect the poison effect
	 */
	public static Effect createPoison() {
		Consumer<Target> onEndTurn = (t -> t.takeDamageIgnoringShield(1));
		return new EffectBuilder("Poison", EffectType.POISON, null, onEndTurn, null, null, null);
	}
	
	
	/**
	 * Creates the rage effect.
	 * Rage increases the amount of damage dealt
	 * 
	 * @return Effect the rage effect
	 */
	public static Effect createRage() {
		Function<Integer, Integer> modifyDamageDealt = (damage -> damage + 1);
		return new EffectBuilder("Rage", EffectType.RAGE, null, null, modifyDamageDealt, null, null);
	}
	
	/**
	 * Creates the Regen effect.
	 * Regen allows the entity to gain hp back
	 * 
	 * @return Effect the regen effect
	 */
	public static Effect createRegen() {
		Consumer<Target> onStartTurn = t -> t.addHp(1);
		return new EffectBuilder("Regen", EffectType.REGEN, onStartTurn, null, null, null, null);
	}
	
	
	/**
	 * Creates the Slow effect.
	 * Slow reduce the entity's shield
	 * 
	 * @return Effect the Slow effect
	 */
	public static Effect createSlow() {
		Function<Integer, Integer> modifyShield = shield -> shield - 1;
		return new EffectBuilder("Slow", EffectType.SLOW, null, null, null, null, modifyShield);
	}
}
