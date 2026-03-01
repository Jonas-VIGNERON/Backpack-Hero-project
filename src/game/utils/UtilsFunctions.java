package game.utils;

import java.util.List;
import java.util.Objects;

public class UtilsFunctions {
	/**
	 * Checks if every object in {@code objects} are not null.
	 * If so do nothing, throw an exception otherwise.
	 * 
	 * @param <T> different kind of objects
	 * @param objects a list of object.
	 */
	public static <T> void checkIfNonNull(List<T> objects) {
		objects.forEach(Objects::requireNonNull);
	}
}
