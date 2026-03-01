package game.entity.monsters;

import static game.random.Randomizer.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import game.entity.Hero;
import game.entity.Monster;

public class ListOfMonster {
	/**
	 * Gets a random monster from the list of monster
	 * 
	 * @return Monster
	 */
	public static Monster getRandomMonster(Hero hero) {
		Objects.requireNonNull(hero);
		var allMonstersAndProbability = Map.of("RatWolf", 20, "LittleRatWolf", 25, "FrogWizard", 6,
				"QueenBee", 3, "LivingShadow", 2);
	    int totalWeight = allMonstersAndProbability.values()
	            .stream()
	            .mapToInt(Integer::intValue)
	            .sum();

	    int r = random(0, totalWeight);
	    int cumulative = 0;
	    for (var entry : allMonstersAndProbability.entrySet()) {
	        cumulative += entry.getValue();
	        if (r < cumulative) {
	            return Monster.createMonsterByName(entry.getKey(), hero);
	        }
	    }
	    // It is not supposed to happen
	    throw new IllegalStateException("Impossible to select a monster");
	}
	
	
	/**
	 * Gets a random team of monsters
	 * 
	 * @return ArrayList<Monster> 
	 */
	public static ArrayList<Monster> getTeamOfMonsters(Hero hero) {
		Objects.requireNonNull(hero);
	    return Stream.generate(() -> getRandomMonster(hero))
	                 .limit(random(1, 4))
	                 .collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Sets the moves of all the monster in the team
	 * 
	 * @param monstersTeam represents the monsters' team
	 */
	public static void setMonstersNextMoves(List<Monster> monstersTeam) {
		Objects.requireNonNull(monstersTeam);
		monstersTeam.forEach(m -> m.defineMoves());
	}
	
	/**
	 * Removes the monsters dead in there team.
	 * 
	 * @param monsters represents the monsters' team
	 * @return int
	 */
	private static int removeDeadMonster(List<Monster> monsters) {
	    int totalXp = 0;
	    var it = monsters.iterator();
	    while (it.hasNext()) {
	        Monster m = it.next();
	        if (m.getHp() <= 0) {
	            totalXp += m.getInfos().xp();
	            it.remove();
	        }
	    }
	    return totalXp;
	}
	
	/**
	 * Gets the xp of dead monsters
	 * 
	 * @param monsters is the monsters' team
	 * @param h represents the hero
	 * @param bp is the backpack
	 * @return
	 */
	public static int xpGetByKillingMonster(List<Monster> monsters) {
		Objects.requireNonNull(monsters);
		var xp = removeDeadMonster(monsters);
		return xp;
	}
	
	/**
	 * This function permits to change the LivingShadow's move
	 * 
	 * @param monsters the list of monsters 
	 */
	public static void updateIfLivingShadow(List<Monster> monsters) {
		Objects.requireNonNull(monsters);
		for(var monster : monsters) {
			if (monster.getInfos().name().equals("LivingShadow")) {
				monster.defineMoves();
			}
		}
	}
	
}
