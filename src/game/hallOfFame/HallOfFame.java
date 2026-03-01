package game.hallOfFame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import affichage.EndScreen.EndScreen;
import game.backpack.Backpack;
import game.entity.Hero;
import game.event.Merchant;
import game.utils.UtilsFunctions;

/**
 * Class to manage the Hall Of Fame
 */
public record HallOfFame() {
	
	/**
	 * Reads the Hall Of Fame's file and return a list of String which contains the file's content.
	 * 
	 * @return List<String>
	 */
	private static List<String> readFileHallOfFame() {
		var filePath = "./files/HallOfFame.txt";
		try {
            return Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("File can't be read : " + filePath, e);
        }
	}
	
	/**
	 * Splits a String by a space character like tab, space or others.
	 * 
	 * @param s 
	 * @return
	 */
	private static String[] splitString(String s) {
		return s.split("\\s+");
	}

	/**
	 * Computes the score depending on the hero's max HP and the item's rarity in the backpack.
	 * 
	 * @param bp the backpack
	 * @param h the hero
	 * @return int the player's score.
	 */
	private static int computeScore(Backpack bp, Hero h) {
	    int price = bp.getPlacedItems().stream()
	            .mapToInt(Merchant::definePrice)
	            .sum();
	    return (h.getMaxHp() - 40) * 100 + price * 10;
	}

	/**
	 * Creates an entry composed by {@name} and {@score}.
	 */
	private static String createEntry(String name, int score) {
	    return name + " " + score;
	}

	/**
	 * Inserts the player's score if it is better than at least the third best score.
	 * 
	 * @param lines List of strings which represents the hall of fame's file's content
	 * @param entry the String of the player and his score.
	 * @param score player's score
	 */
	private static void insertScore(List<String> lines, String entry, int score) {
	    for (int i = 0; i < lines.size(); i++) {
	        int oldScore = Integer.parseInt(splitString(lines.get(i))[1]);
	        if (score > oldScore) {
	            lines.add(i, entry);
	            return;
	        }
	    }
	    lines.add(entry);
	}

	/**
	 * Keeps the 3 best score.
	 * @param lines list of strings which represents the hall of fame's file's content
	 */
	private static void keepTopThree(List<String> lines) {
	    while (lines.size() > 3) {
	        lines.remove(lines.size() - 1);
	    }
	}
	
	/**
	 * Writes in the file the hall of fame.
	 * 
	 * @param lines list of strings which represents the hall of fame's file's content
	 */
	private static void writeHallOfFame(List<String> lines) {
		var filePath = "./files/HallOfFame.txt";
	    try {
	        Files.write(Path.of(filePath), lines);
	    } catch (IOException e) {
	        throw new RuntimeException("Impossible d'écrire le Hall of Fame", e);
	    }
	}

	/**
	 * Creates the new Hall of Fame's file after the player's game.
	 * Adds his score if it is in the top three.
	 * 
	 * @param bp the backpack
	 * @param h the hero.
	 */
	public static void enterHallOfFame(Backpack bp, Hero h) {
		UtilsFunctions.checkIfNonNull(List.of(bp, h));
		var name = EndScreen.askPlayerName();
	    int score = computeScore(bp, h);
	    var lines = new ArrayList<>(readFileHallOfFame());
	    String entry = createEntry(name, score);
	    insertScore(lines, entry, score);
	    keepTopThree(lines);

	    writeHallOfFame(lines);
	}

	
}
