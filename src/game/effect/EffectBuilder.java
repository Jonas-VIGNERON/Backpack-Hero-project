package game.effect;

import java.util.function.Consumer;
import java.util.function.Function;

import game.entity.Target;

/**
 * Record to creates effects.
 */
public record EffectBuilder(
		String name,
		EffectType type,
		Consumer<Target> onStartTurn,
		Consumer<Target> onEndTurn,
		Function<Integer, Integer> modifyDamageDealt,
		Function<Integer, Integer> modifyDamageTaken,
		Function<Integer, Integer> modifyShield
) implements Effect {
	/**
	 * Effect's builder
	 * 
	 * @param name the effect's name
	 * @param type the effect's type
	 * @param onStartTurn the effect application on start of the entity's turn.
	 * @param onEndTurn the effect application on the end of the entity's turn.
	 * @param modifyDamageDealt is the function which modifies or not the damage dealt by the entity
	 * @param modifyDamageTaken is the function which modifies or not the damage taken by the entity
	 * @param modifyShield is the function which modifies or not the entity's shield
	 */
    public EffectBuilder {
        onStartTurn = (onStartTurn != null) ? onStartTurn : (_ -> {});
        onEndTurn = (onEndTurn != null) ? onEndTurn : (_ -> {});
        modifyDamageDealt = (modifyDamageDealt != null) ? modifyDamageDealt : (v -> v);
        modifyDamageTaken = (modifyDamageTaken != null) ? modifyDamageTaken : (v -> v);
        modifyShield = (modifyShield != null) ? modifyShield : (v -> v);
    }
}
