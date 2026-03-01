package game.item;

import java.util.List;

import affichage.Direction;
import game.utils.UtilsFunctions;

/**
 * Record that represents the shape of an item.
 * @param shape is a table of boolean. Where the value is true it means the shape of the item fill that slots, false mean it not.
 * @param columns represents the amount of columns this shape has
 * @param lines represents the amount of lines this shape has
 * @param dir is the direction of the item.
 */
public record Shape(boolean[][] shape, int columns, int lines, Direction dir) {
	
	
	public Shape {
		UtilsFunctions.checkIfNonNull(List.of(shape, dir));
		if (columns <= 0) {
			throw new IllegalArgumentException("columns must be positive");
		}
		if (lines <= 0) {
			throw new IllegalArgumentException("lines must be positive");
		}
	}
	/**
	 * Rotates the item's shape clockwise
	 * 
	 * @return Shape the new Shape
	 */
	public Shape rotate() {
	    boolean[][] newShape = new boolean[columns][lines];
	    for (int l = 0; l < lines; l++) {
	        for (int c = 0; c < columns; c++) {
	            newShape[c][lines - 1 - l] = shape[l][c];
	        }
	    }
	    return new Shape(newShape, lines, columns, dir.next());
	}

	/**
	 * Create a 1x1 shape.
	 * 
	 * @return Shape
	 */
	public static Shape getA1By1Shape() {
		var s = new boolean[1][1];
		s[0][0] = true;
		return new Shape(s, 1, 1, Direction.UP);
	}
}
