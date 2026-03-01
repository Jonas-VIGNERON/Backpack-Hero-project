package affichage;

public enum Direction {
	UP, RIGHT, DOWN, LEFT;

	public Direction next() {
		Direction[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}
}