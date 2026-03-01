package game.entity.monsters;

import java.util.List;
import java.util.function.Consumer;

import game.entity.Monster;
import game.utils.UtilsFunctions;

/**
 * Record that contains informations about a monster which they are not supposed to be modified.
 */
public record InfoMonster(String name, int maxHp, int xp, Consumer<Monster> moves) {
	
	public InfoMonster{
		UtilsFunctions.checkIfNonNull(List.of(name, moves));
		if (maxHp < 0) {
			throw new IllegalArgumentException("maxHp must not be negative");
		}
		if (xp < 0) {
			throw new IllegalArgumentException("xp must not be negative");
		}
	}
}
