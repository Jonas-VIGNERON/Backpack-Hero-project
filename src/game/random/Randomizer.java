package game.random;

import java.util.Random;

/**
 * Class which contains a random operation.
 */
public final class Randomizer {
    /**
     * Gets a random number between {@code min}  and {@code max - 1}
     * 
     * @param min the minimum
     * @param max the maximum
     * @return int
     */
    public static int random(int min, int max) {
    	var random = new Random();
        return random.nextInt(min, max);
    }
}
