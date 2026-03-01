package game.event;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.BiFunction;

import affichage.gameModel.SimpleGameData;
import game.backpack.Backpack;
import game.entity.Hero;

/**
 * Creates a Healer which heals hp or increase the hero's maxHP for golds.
 */
public class Healer {
	private final LinkedHashMap<String, BiFunction<Hero, Backpack, Boolean>> choices = initChoices();

	/**
	 * Initializes the different choices.
	 * 
	 * @return LinkedHashMap<String, BiFunction<Hero, Backpack, Boolean>>
	 */
	public LinkedHashMap<String, BiFunction<Hero, Backpack, Boolean>> initChoices() {
		var choices = new LinkedHashMap<String, BiFunction<Hero, Backpack, Boolean>>();
		choices.put("Heal the player for 25 HP for 4 gold", (h, bp) -> heal25For4Gold(h, bp));
		choices.put("Increase the player's max HP by 5 for 10 gold", (h, bp) -> increase5MaxHpFor10Gold(h, bp));
		return choices;
	}

	/**
	 * Get the choice of the players.
	 * 
	 * @return LinkedHashMap<String, BiFunction<Hero, Backpack, Boolean>>
	 */
	public LinkedHashMap<String, BiFunction<Hero, Backpack, Boolean>> getChoices() {
		return choices;
	}

	/**
	 * Purchases 25 hp for 4 golds if there is enough gold in the backpack.
	 * 
	 * @param h  the hero
	 * @param bp the backpack
	 * @return {@code true} if there is enough gold {@code false} if there is not
	 *         enough gold
	 */
	private static boolean heal25For4Gold(Hero h, Backpack bp) {
		if (bp.reduceGold(4)) {
			h.addHp(25);
			return true;
		}
		return false;
	}

	/**
	 * Purchases 5 hp max for 10 golds if there is enough gold in the backpack.
	 * 
	 * @param h  the hero
	 * @param bp the backpack
	 * @return {@code true} if there is enough gold {@code false} if there is not
	 *         enough gold
	 */
	private static boolean increase5MaxHpFor10Gold(Hero h, Backpack bp) {
		if (bp.reduceGold(10)) {
			h.increaseMaxHp(5);
			return true;
		}
		return false;
	}

	/**
	 * Function to purchase something from the healer
	 * 
	 * @param index the choice's number
	 * @param data  the data of the game.
	 */
	public void purchaseOne(int index, SimpleGameData data) {
		Objects.requireNonNull(data);
		if (index < 0) {
			throw new IllegalArgumentException("index must not be negative");
		}
		if (index < 0 || index >= choices.size())
			return;
		var h = data.getHero();
		var bp = data.getBackpack();
		var entry = choices.entrySet().stream().skip(index).findFirst().orElse(null);
		if (entry == null)
			return;
		if (entry.getValue().apply(h, bp)) {
			choices.clear();
		}
	}
}
