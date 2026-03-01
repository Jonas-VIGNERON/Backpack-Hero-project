package affichage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import affichage.Direction;

/**
 * Utility class responsible for loading, storing, and retrieving all image
 * assets using java.nio.
 */
public class ImageLoader {

	/**
	 * Cache storage: maps a unique key (e.g., "WoodenSword_UP") to the loaded
	 * image.
	 */
	private final static Map<String, BufferedImage> mapImages = new HashMap<>();

	/**
	 * List of item names to load. Matches filenames or folder names in /images/.
	 */
	private static final String[] ITEM_NAMES = { "WoodenSword", "JadeAxe", "FlameAxe", "LeatherBoots", "LeatherCap",
			"SkullWand", "Dart", "Gold", "Manastone", "RoughBuckler", "donjon", "blocked", "SpikyClub", "VampiricAxe",
			"EarthstoneBlade", "Mace", "FrozenHammer", "DamagedKnife", "ThrowingStar", "PoisonStar", "SweatyTowel",
			"TowerShield", "PlateArmor", "Sapphire", "Meal", "RareHerb", "Tea", "RingOfRage", "Smelt", "Key", "Curse" };

	/** List of monster names to load. Matches filenames in /images/Monster/. */
	private static final String[] MONSTER_NAMES = { "FrogWizard", "LilBee", "LittleRatWolf", "LivingShadow", "QueenBee",
			"RatWolf" };

	/**
	 * Internal helper to read an image file from the disk using java.nio.
	 * * @param pathString The relative path to the image file (e.g., "images/Hero.png").
	 * @return The loaded {@code BufferedImage}, or {@code null} if error/not found.
	 */
	private static BufferedImage loadImg(String pathString) {
		try {
			if (pathString.startsWith("/") || pathString.startsWith("\\")) {
				pathString = pathString.substring(1);
			}
			var path = Path.of(pathString);
			if (!Files.exists(path)) {
				return null;
			}
			try (InputStream is = Files.newInputStream(path)) {
				return ImageIO.read(is);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Main entry point to load all game assets. This method should be called once
	 * at the start of the application.
	 */
	public static Map<String, BufferedImage> loadImages() {
		for (String name : ITEM_NAMES) {
			registerItem(name);
		}
		registerSimpleImage("Hero", "images/Hero.png");
		registerSimpleImage("Background", "images/Background.png");
		for (String mName : MONSTER_NAMES) {
			registerSimpleImage(mName, "images/Monster/" + mName + ".png");
		}
		return mapImages;
	}

	/**
	 * Helper to load a single, non-rotatable image.
	 */
	private static void registerSimpleImage(String key, String path) {
		var img = loadImg(path);
		if (img != null) {
			mapImages.put(key, img);
		} else {
			System.err.println("Image manquante : " + path);
		}
	}

	/**
	 * Helper to load an Item, handling both static images and directional folders.
	 */
	private static void registerItem(String name) {
		var baseImg = loadImg("images/" + name + ".png");
		for (Direction dir : Direction.values()) {
			var folderPath = "images/" + name + "/" + name + "_" + dir + ".png";
			var rotImg = loadImg(folderPath);
			var key = name + "_" + dir;
			if (rotImg != null) {
				mapImages.put(key, rotImg);
			} else if (baseImg != null) {
				mapImages.put(key, baseImg);
			}
		}
		if (baseImg != null) {
			mapImages.put(name, baseImg);
		}
	}

	/**
	 * Retrieves a stored image from the cache.
	 */
	public static BufferedImage getImage(String name, Direction dir) {
		if (dir != null) {
			var img = mapImages.get(name + "_" + dir);
			if (img != null)
				return img;
		}
		return mapImages.get(name);
	}
}