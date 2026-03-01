package game.item;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.ImageIcon;

import affichage.Direction;
import game.backpack.Backpack;
import game.context.Context;
import game.entity.Hero;
import game.entity.Monster;
import game.pos.Pos;
import game.utils.UtilsFunctions;

/**
 * Class for item informations.
 */
public final class Item {
	private final InfosItem infos;
	private Shape shape;
	private int numberOfUses; // For Gold, it references to the amount of gold.

	private int bonusDamage = 0;
	private int bonusShield = 0;
	private Pos pos = new Pos(-1, -1);

	private Consumer<Context> onUse;
	private Consumer<Context> beforeTurn; // Mostly to add effects on items.

	/**
	 * The builder
	 * 
	 * @param n the name
	 * @param t the type
	 * @param r the rarity
	 * @param s the shape
	 * @param u true if the item is usable, false otherwise
	 * @param uN true if there is a utilization limit of this item, otherwise false
	 * @param nOU the number of used (-1  if uN is true)
	 * @param oU the consumer for when the item is used (can be {@code null})
	 * @param bT the consumer for the turn's start  (can be {@code null})
	 */
	public Item(String n, ItemType t, Rarity r, Shape s, boolean u, boolean uN, int nOU, Consumer<Context> oU,
			Consumer<Context> bT) {
		UtilsFunctions.checkIfNonNull(List.of(n, t, r, s));
		var img = new ImageIcon("images/" + n + ".png").getImage();
		infos = new InfosItem(n, t, r, u, uN, img);
		shape = s;
		numberOfUses = nOU;
		onUse = oU;
		beforeTurn = bT;
	}

	/**
	 * Gets the item's Pos
	 * 
	 * @return Pos
	 */
	public Pos getPos() {
		return pos;
	}

	/**
	 * Gets the item's unmodifiable informations.
	 */
	public InfosItem getInfos() {
		return infos;
	}

	/**
	 * Sets the item's pos to newPos
	 * 
	 * @param newPos represents the new position of the item
	 */
	public void setPos(Pos newPos) {
		Objects.requireNonNull(newPos);
		pos = newPos;
	}

	/**
	 * Gets the item's shape
	 * 
	 * @return Shape
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * Gets the item's numeber of uses
	 * 
	 * @return
	 */
	public int getNumberOfUses() {
		return numberOfUses;
	}

	/**
	 * Gets the item's bonus damage
	 * 
	 * @return int
	 */
	public int getBonusDamage() {
		return bonusDamage;
	}

	/**
	 * Gets the item's bonus shield
	 * 
	 * @return int
	 */
	public int getBonusShield() {
		return bonusShield;
	}

	/**
	 * Adds bonus damage
	 * 
	 * @param value is the additional damage
	 */
	public void addBonusDamage(int value) {
		bonusDamage += value;
	}

	/**
	 * Adds bonus shield
	 * 
	 * @param value is the additional shield
	 */
	public void addBonusShield(int value) {
		bonusShield += value;
	}

	/**
	 * Do the action of the chosen item if possible.
	 * 
	 * @param h the hero
	 * @param m the list of monsters
	 * @param index the index of the targeted monster in {@code m}
	 * @param i the item
	 * @param bp the backpack
	 */
	public void onUse(Hero h, List<Monster> m, int index, Item i, Backpack bp) {
		UtilsFunctions.checkIfNonNull(List.of(h, m, i, bp));
		if (index < 0){
			throw new IllegalArgumentException("index must not be negative");
		}
		if (onUse != null) {
			onUse.accept(new Context(h, m, index, bp, i));
			var infosItem = i.getInfos();
			if (!infosItem.unlimitedUses() && i.getNumberOfUses() == 0) {
				bp.removeItem(i.getPos());
			}
		}
	}

	/**
	 * Applies the consumer of the item at the turn's start
	 *
	 * @param h the hero
	 * @param m the list of monsters
	 * @param index the index of the targeted monster in {@code m}
	 * @param i the item
	 */
	public void beforeTurn(Hero h, List<Monster> m, Backpack bp, Item i) {
		UtilsFunctions.checkIfNonNull(List.of(bp, i, m, h));
		if (beforeTurn != null) {
			beforeTurn.accept(new Context(h, m, 0, bp, i));
		}
	}

	/**
	 * Modify the item's shape
	 * 
	 * @param s
	 */
	private void setShape(Shape s) {
		shape = s;
	}

	/**
	 * Rotates the item, and so modify the shape.
	 */
	public void rotate() {
		setShape(shape.rotate());
	}

	/**
	 * Sets the bonus when the hero's turn starts
	 */
	public void setBonusBeforeTurn() {
		bonusDamage = 0;
		bonusShield = 0;
	}

	/**
	 * Modifies the numberOfUses and sets it to {@code newVal}
	 * 
	 * @param newVal the new value of numberOfUses
	 */
	public void setNumberOfUses(int newVal) {
		numberOfUses = newVal;
	}

	/**
	 * Reduces the numberOfUses by 1.
	 */
	public void reduceNumberOfUsesBy1() {
		numberOfUses--;
	}
	
	/**
	 * Checks if it is an item, if so return true, false otherwise.
	 * 
	 * @return boolean.
	 */
	public boolean isAnItem() {
		var type = infos.type();
		return switch (type) {
		case ItemType.VOID -> false;
		case ItemType.BLOCKED -> false;
		default -> true;
		};
	}

	/**
	 * Gets the direction of the item's shape.
	 * 
	 * @return Direction
	 */
	public Direction getDirection() {
		return this.shape.dir();
	}

	/**
	 * Modifies the direction of the item's shape and sets it to {@targetDir}
	 * 
	 * @param targetDir the new Direction of the item's Shape
	 */
	public void setDirection(Direction targetDir) {
		Objects.requireNonNull(targetDir);
		while (this.getDirection() != targetDir) {
			this.rotate();
		}
	}
}
