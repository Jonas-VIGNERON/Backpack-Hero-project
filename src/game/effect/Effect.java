package game.effect;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import game.entity.Target;
import game.utils.UtilsFunctions;

/**
 * Interface that represents the effects.
 */
public sealed interface Effect permits EffectBuilder{
	public String name();
	public EffectType type();
	public Consumer<Target> onStartTurn();
	public Consumer<Target> onEndTurn();
	public Function<Integer, Integer> modifyDamageDealt();
	public Function<Integer, Integer> modifyDamageTaken();
	public Function<Integer, Integer> modifyShield();
    
    
    /**
     * Adds the effect e to the target(a monster or the hero)
     * 
     * @param t is the Hero or a Monster
     * @param e represents the effect
     */
    public static void addEffect(Target t, EffectType e, int i) {
    	UtilsFunctions.checkIfNonNull(List.of(e, t));
    	t.getEffects().addAll(Collections.nCopies(i, e));
    }
    
    /**
     * Reduces the stack of every effect
     * 
     * @param t represents the hero or a monster.
     */
    public static void reduceEffectsStacksAfterTurn(Target t) {
    	Objects.requireNonNull(t);
        var removed = new HashSet<EffectType>();
        t.getEffects().removeIf(effect -> removed.add(effect));
    }
    
    /**
     * Transforms the list of effect into a Map.
     * By associates the effect with the amount of time he appears in the list.
     * 
     * @param l the list of effects
     * @return Map<EffectType, Integer> the effects' map
     */
    public static Map<EffectType, Integer> listToMap(List<EffectType> l){
    	Objects.requireNonNull(l);
    	return l.stream()
    			.collect(Collectors.groupingBy(e -> e, Collectors.summingInt(_ -> 1)));
    }
}
