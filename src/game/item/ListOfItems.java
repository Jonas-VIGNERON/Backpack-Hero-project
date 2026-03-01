package game.item;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import game.random.Randomizer;

/**
 * Class which contains methods about a list of items
 */
public class ListOfItems {
	private final List<String> proportionality = createListSafely();
	private final Map<String, Supplier<Item>> mapNameAndItem = createMapNameItem();

	/**
	 * Creates a list thanks to a file and return it.
	 * If there is a problem return an empty list.
	 * @return List<String>
	 */
	private static List<String> createListSafely() {
	    try {
	        return createListWithFile();
	    } catch (IOException e) {
	        return new ArrayList<>();
	    }
	}

	/**
	 * Creates a list of String, which contains the items name.
	 * Each name appears a different number of time in that list depending of the rarity.
	 * <p>
	 * <b>10</b> times for each COMMON items
	 * <b>5</b> times for each UNCOMMON items
	 * <b>3</b> times for each RARE items
	 * <b>1</b> time for each LEGENDARY items
	 * <p>
	 * The objective of this implementation is to have a drop rate depending of the rarity.
	 * 
	 * @return List<String>
	 * @throws IOException
	 */
	private static List<String> createListWithFile() throws IOException {
		List<String> result = new ArrayList<>();
		Path fileName = Path.of("files/ListOfItems.txt");
		List<String> lines = Files.readAllLines(fileName);
		for (String line : lines) {
		    if (line.isBlank()) continue;
		    String[] parts = line.split("\\s+"); // Split by space
		            String name = parts[0];
		            int count = Integer.parseInt(parts[1]);
		            for (int i = 0; i < count; i++) {
		                result.add(name);
		            }
		        }
		
		return result;
	}
	
	
	/**
	 * Creates a map with (name, method) as (key, value). This map is used
	 * to get an item by his name.
	 * 
	 * @return Map<String, Supplier<Item>>
	 */
	private static Map<String, Supplier<Item>> createMapNameItem() {
	    return Map.ofEntries(
	    	Map.entry("WoodenSword", ItemsCreation::createWoodenSword), Map.entry("RoughBuckler", ItemsCreation::createRoughBuckler),
	        Map.entry("Sapphire", ItemsCreation::createSapphire), Map.entry("Dart", ItemsCreation::createDart),
	        Map.entry("LeatherCap", ItemsCreation::createLeatherCap),Map.entry("LeatherBoots", ItemsCreation::createLeatherBoots),
	        Map.entry("Manastone", ItemsCreation::createMana), Map.entry("SkullWand", ItemsCreation::createSkullWand),
	        Map.entry("FlameAxe", ItemsCreation::createFlameAxe), Map.entry("SpikyClub", ItemsCreation::createSpikyClub),
	        Map.entry("VampiricAxe", ItemsCreation::createVampiricAxe), Map.entry("Mace", ItemsCreation::createMace),
	        Map.entry("FrozenHammer", ItemsCreation::createFrozenHammer), Map.entry("DamagedKnife", ItemsCreation::createDamagedKnife),
	        Map.entry("ThrowingStar", ItemsCreation::createThrowingStar), Map.entry("PoisonStar", ItemsCreation::createPoisonStar),
	        Map.entry("TowerShield", ItemsCreation::createTowerShield), Map.entry("PlateArmor", ItemsCreation::createPlateArmor),
	        Map.entry("SweatyTowel", ItemsCreation::createSweatyTowel), Map.entry("EarthstoneBlade", ItemsCreation::createEarthstoneBlade),
	        Map.entry("Meal", ItemsCreation::createMeal), Map.entry("JadeAxe", ItemsCreation::createJadeAxe),
	        Map.entry("RareHerb", ItemsCreation::createRareHerb), Map.entry("Tea", ItemsCreation::createTea),
	        Map.entry("RingOfRage", ItemsCreation::createRingOfRage), Map.entry("Smelt", ItemsCreation::createSmelt),
	        Map.entry("Key", ItemsCreation::createKey)
	    );
	}


	/**
	 * Gets an item depending on the name, use the map created by {@code createMapNameItem}
	 * 
	 * @param name the item's name
	 * @return Item
	 */
	public Item getAnItemByName(String name) {
		Objects.requireNonNull(name);
	    var supplier = mapNameAndItem.get(name);
	    if (supplier == null) {
	        throw new IllegalArgumentException("Uknown item : " + name);
	    }
	    return supplier.get();
	}
	
	/**
	 * Gets a random list of {@code numberOfItems} items
	 * 
	 * @param numberOfItems
	 * @return
	 */
	public ArrayList<Item> getListOfRandomItems(int numberOfItems){
		if (numberOfItems <= 0) {
			return new ArrayList<>();
		}
		return Stream.generate(() -> getAnItemByName(proportionality.get(Randomizer.random(0, proportionality.size()))))
				.limit(numberOfItems)
				.collect(Collectors.toCollection(ArrayList::new));
	}
	
}
