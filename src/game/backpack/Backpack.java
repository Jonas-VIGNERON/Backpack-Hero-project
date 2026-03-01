package game.backpack;

import static game.random.Randomizer.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import affichage.Direction;
import game.entity.Hero;
import game.entity.Monster;
import game.item.Item;
import game.item.ItemType;
import game.item.ItemsCreation;
import game.item.Shape;
import game.pos.Pos;
import game.utils.UtilsFunctions;

/**
 * Useful class to manage the backpack. Stores gold and mana, can add and remove
 * items.
 */
public class Backpack {
  private final Item[][] matrix;
  private int golds = 0;

  /**
   * Initializes the matrix which represents the backpack
   */
  public Backpack() {
    this.matrix = initItemsMatrix();
  }

  /**
   * Creates a matrix, 5 by 7, which will contains items.
   * 
   * @return Item[][] : a matrix size 5x7
   */
  private Item[][] initItemsMatrix() {
    var matrix = new Item[5][7];
    for (int r = 0; r < 5; r++) {
      for (int c = 0; c < 7; c++) {
        if (r >= 1 && r <= 3 && c >= 2 && c <= 4) {
          matrix[r][c] = ItemsCreation.createVoid();
        } else {
          matrix[r][c] = ItemsCreation.createBlocked();
        }
      }
    }
    return matrix;
  }

  /**
   * Golds' getter
   * 
   * @return int : amount of gold
   */
  public int getGolds() {
    return golds;
  }

  /**
   * Get an item from a position in the bp.
   * 
   * @param pos represents a position in the backpack
   * @return Item
   */
  public Item getItemFromPos(Pos pos) {
    Objects.requireNonNull(pos);
    if (outOfTheBackpack(pos)) {
      return null;
    }
    return matrix[pos.y()][pos.x()];
  }

  /**
   * Counts and modifies the number of golds by checking every backpack's slots.
   */
  public void countGolds() {
    int golds = Arrays.stream(matrix).flatMap(Arrays::stream).filter(item -> item.getInfos().type() == ItemType.GOLD)
        .mapToInt(Item::getNumberOfUses).sum();
    this.golds = golds;
  }

  /**
   * Counts and modifies the number of mana by checking every backpack's slots.
   * 
   * @param h the hero
   */
  public void countMana(Hero h) {
    Objects.requireNonNull(h);
    int mana = Arrays.stream(matrix).flatMap(Arrays::stream)
        .filter(item -> item.getInfos().type() == ItemType.MANASTONE).mapToInt(_ -> 1).sum();
    h.setMana(mana);
  }

  /**
   * Gets the number of row in the bp
   * 
   * @return int
   */
  public int getRows() {
    return matrix.length;
  }

  /**
   * Gets the number of column in the bp
   * 
   * @return int
   */
  public int getCols() {
    if (matrix.length == 0) {
      return 0;
    }
    return matrix[0].length;
  }

  /**
   * Gets the number of slots not blocked in a row under a pos
   * 
   * @param pos represents a position in the backpack
   * @return int
   */
  private int countRowBelow(Pos pos) {
    var posBelow = new Pos(pos.x(), pos.y() + 1);
    if (outOfTheBackpack(posBelow) || isBlocked(posBelow)) {
      return 0;
    }
    return 1 + countRowBelow(posBelow);
  }

  /**
   * Computes the minimum number of free rows in a vertical direction (above or
   * below) for a given shape placed at a specific position.
   *
   * @param pos       the top-left position of the shape
   * @param shape     the shape to evaluate
   * @param direction the vertical direction (ABOVE or BELOW)
   * @return the minimum number of rows the shape can move in the given direction
   */
  public int countRowInDirection(Pos pos, Shape shape, Direction direction) {
    UtilsFunctions.checkIfNonNull(List.of(shape, direction, pos));
    int min = Integer.MAX_VALUE;
    var s = shape.shape();
    for (int row = 0; row < shape.lines(); row++) {
      for (int col = 0; col < shape.columns(); col++) {
        if (s[row][col]) {
          Pos cellPos = new Pos(pos.x() + col, pos.y() + row);
          int res = switch (direction) {
          case Direction.UP -> countRowAbove(cellPos);
          case Direction.DOWN -> countRowBelow(cellPos);
          default -> Integer.MAX_VALUE;
          };
          min = Math.min(min, res);
        }
      }
    }
    return min == Integer.MAX_VALUE ? 0 : min;
  }

  /**
   * Gets the number of slots not blocked in a row above a pos
   * 
   * @param pos represents a position in the backpack
   * @return int
   */
  private int countRowAbove(Pos pos) {
    var posAbove = new Pos(pos.x(), pos.y() - 1);
    if (outOfTheBackpack(posAbove) || isBlocked(posAbove)) {
      return 0;
    }
    return 1 + countRowAbove(posAbove);
  }

  /**
   * Gets type of an item at position pos.
   * 
   * @param pos the position of the item
   * @return ItemType the item's type
   */
  public ItemType getItemTypeAt(Pos pos) {
    Objects.requireNonNull(pos);
    if (outOfTheBackpack(pos)) {
      return null;
    }
    return getItemFromPos(pos).getInfos().type();
  }

  /***
   * Collects all the placed items in the backpack
   * 
   * @return List<Item> contains every items stocked in the backpack
   */
  public List<Item> getPlacedItems() {
    return Arrays.stream(matrix).flatMap(Arrays::stream)
        .filter(item -> item.getInfos().type() != ItemType.VOID && item.getInfos().type() != ItemType.BLOCKED)
        .distinct().toList();
  }

  /**
   * Returns a key Item if there is one in the backpack, null otherwise.
   * 
   * @return Item
   */
  public Item getAKey() {
    for (var row = 0; row < 5; row++) {
      for (var col = 0; col < 7; col++) {
        if (matrix[row][col].getInfos().type() == ItemType.KEY) {
          return removeItem(new Pos(col, row));
        }
      }
    }
    return null;
  }

  /**
   * Return true if pos is in the bp, else otherwise.
   * 
   * @param pos represents a position in the backpack
   * @return boolean
   */
  public boolean outOfTheBackpack(Pos pos) {
    Objects.requireNonNull(pos);
    return pos.x() >= 7 || pos.y() >= 5 || pos.x() < 0 || pos.y() < 0;
  }

  /**
   * Verify if the item at pos is a Blocked item. If so return true, else false.
   * 
   * @param pos represents a position in the backpack
   * @return boolean
   */
  private boolean isBlocked(Pos pos) {
    if (!outOfTheBackpack(pos)) {
      if (getItemFromPos(pos).getInfos().type() == ItemType.BLOCKED) {
        return true;
      }
    }
    return false;
  }

  /**
   * é Check if there is an item at the position pos. If so return true, else
   * false.
   * 
   * @param pos represents a position in the backpack
   * @return boolean
   */
  private boolean alreadyAnItem(Pos pos) {
    if (outOfTheBackpack(pos)) {
      return false;
    }
    var i = getItemFromPos(pos);
    return switch (i.getInfos().type()) {
    case ItemType.VOID -> false;
    case ItemType.BLOCKED -> false;
    default -> true;
    };
  }

  /**
   * Verify if the Item i can be place in the backpack at the position pos, which
   * reprensent the top left corner of its shape. Return true if the item can be
   * placed, otherwise false.
   * 
   * @param i   contains an Item
   * @param pos represents a position in the backpack
   * @return boolean
   */
  private boolean checkPos(Item i, Pos pos) {
    var shape = i.getShape();
    var x = pos.x();
    var y = pos.y();
    for (int l = 0; l < shape.lines(); l++) {
      for (int c = 0; c < shape.columns(); c++) {
        var p = new Pos(x + c, y + l);
        if (l < shape.shape().length && c < shape.shape()[l].length) {
          if (shape.shape()[l][c]) {
            if (outOfTheBackpack(p) || alreadyAnItem(p) || isBlocked(p)) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  /**
   * Adds gold(s) to the backpack if possible. If possible return true, false
   * otherwise.
   * 
   * @param i   is an item which contains gold(s)
   * @param pos the wanted position to place the gold
   * @return boolean
   */
  public boolean addGold(Item i, Pos pos) {
    UtilsFunctions.checkIfNonNull(List.of(i, pos));
    if (isBlocked(pos)) {
      return false;
    }
    var current = getItemFromPos(pos);
    var type = current.getInfos().type();
    // If the player wants to add golds to an existing one
    if (type == ItemType.GOLD) {
      removeItem(pos);
      i.setNumberOfUses(current.getNumberOfUses() + i.getNumberOfUses());
    } else if (type != ItemType.VOID) {
      return false;
    }

    matrix[pos.y()][pos.x()] = i;
    i.setPos(pos);
    return true;
  }

  /**
   * Consumes gold from a specific backpack slot. If the slot does not contain
   * gold, the amount is unchanged. If the gold in the slot is less than or equal
   * to the required amount, the item is removed. Otherwise, only part of the gold
   * is consumed.
   *
   * @param r      the row index of the slot
   * @param c      the column index of the slot
   * @param amount the remaining amount of gold to withdraw
   * @return the updated remaining amount of gold to withdraw
   */
  private int consumeGoldAt(int r, int c, int amount) {
    var item = matrix[r][c];
    if (item.getInfos().type() != ItemType.GOLD)
      return amount;

    int gold = item.getNumberOfUses();
    if (gold <= amount) {
      removeItem(new Pos(c, r));
      return amount - gold;
    }

    item.setNumberOfUses(gold - amount);
    return 0;
  }

  /**
   * Removes gold from the backpack until the specified amount is reached. The
   * method iterates over the backpack matrix and consumes gold items slot by slot
   * until the remaining amount becomes zero.
   *
   * @param amount the amount of gold to withdraw
   */
  private void withdrawGold(int amount) {
    for (int r = 0; r < 5 && amount > 0; r++) {
      for (int c = 0; c < 7 && amount > 0; c++) {
        amount = consumeGoldAt(r, c, amount);
      }
    }
  }

  /**
   * Withdraws a given amount of gold from the backpack. This method first checks
   * whether the backpack contains enough gold. If so, it removes gold items slot
   * by slot until the requested amount is fully withdrawn, then updates the total
   * gold count.
   *
   * @param amount the amount of gold to withdraw
   * @return {@code true} if the gold was successfully withdrawn, {@code false} if
   *         there was not enough gold
   */
  public boolean reduceGold(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount must not be negative");
    }
    if (amount > golds) {
      return false;
    }

    withdrawGold(amount);
    countGolds();
    return true;
  }

  /**
   * Removes any items located beneath the future curse position.
   *
   * @param i The curse item.
   * @param p The position.
   */
  public void removeItemToAddCurse(Item i, Pos p) {
    var shape = i.getShape();
    boolean[][] grid = shape.shape();
    for (int l = 0; l < shape.lines(); l++) {
      for (int c = 0; c < shape.columns(); c++) {
        if (l < grid.length && c < grid[l].length && grid[l][c]) {
          removeItem(new Pos(p.x() + c, p.y() + l));
        }
      }
    }
  }

  /**
   * Checks if the curse can be placed at the given position without hitting a
   * BLOCKED slot.
   *
   * @param i   The curse item.
   * @param pos The top-left position.
   * @return true if valid.
   */
  private boolean canPlaceCurse(Item i, Pos pos) {
    var shape = i.getShape();
    boolean[][] grid = shape.shape();
    for (int l = 0; l < shape.lines(); l++) {
      for (int c = 0; c < shape.columns(); c++) {
        if (l < grid.length && c < grid[l].length && grid[l][c]) {
          if (isBlocked(new Pos(pos.x() + c, pos.y() + l))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Tries to add a curse at the specified position. Overwrites existing items
   * unless the slot is blocked.
   *
   * @param i   The curse item.
   * @param pos The target position.
   * @return true if placed successfully, false if blocked.
   */
  public boolean addCurse(Item i, Pos pos) {
    if (!canPlaceCurse(i, pos)) {
      return false;
    }
    removeItemToAddCurse(i, pos);
    placeCurseOnGrid(i, pos);
    i.setPos(pos);
    countGolds();
    return true;
  }

  /**
   * Writes the curse item into the backpack matrix.
   *
   * @param i   The curse item.
   * @param pos The position.
   */
  private void placeCurseOnGrid(Item i, Pos pos) {
    var shape = i.getShape();
    boolean[][] grid = shape.shape();
    for (int l = 0; l < shape.lines(); l++) {
      for (int c = 0; c < shape.columns(); c++) {
        if (l < grid.length && c < grid[l].length && grid[l][c]) {
          matrix[pos.y() + l][pos.x() + c] = i;
        }
      }
    }
  }

  /**
   * Checks whether a cell of the item's shape is valid and occupied.
   *
   * @param i the item
   * @param l the line index in the shape
   * @param c the column index in the shape
   * @return true if the shape cell exists and is filled
   */
  private boolean isShapeCell(Item i, int l, int c) {
    return l < i.getShape().shape().length && c < i.getShape().shape()[l].length && i.getShape().shape()[l][c];
  }

  /**
   * Places a shaped item into the backpack matrix and updates its position.
   *
   * @param i   the item to place
   * @param pos the top-left position of the item
   */
  private void placeItem(Item i, Pos pos) {
    var shape = i.getShape();
    for (int l = 0; l < shape.lines(); l++) {
      for (int c = 0; c < shape.columns(); c++) {
        if (isShapeCell(i, l, c)) {
          matrix[pos.y() + l][pos.x() + c] = i;
        }
      }
    }
    i.setPos(pos);
    countGolds();
  }

  /**
   * Adds gold to the backpack and updates the total gold count.
   *
   * @param i   the gold item to add
   * @param pos the position where the gold is added
   * @return true if the gold was added, false otherwise
   */
  private boolean addGoldAndUpdate(Item i, Pos pos) {
    var added = addGold(i, pos);
    countGolds();
    return added;
  }

  /**
   * Add an item in the bp at pos (the top left corner of the shape) if possible.
   * Return true if the item was add, else false.
   * 
   * @param i   represents an Item
   * @param pos represents a position in the backpack
   * @return boolean
   */
  public boolean addItem(Item i, Pos pos) {
    if (i == null || pos == null) {
      return false;
    }
    UtilsFunctions.checkIfNonNull(List.of(i, pos));
    if (i.getInfos().type() == ItemType.GOLD) {
      return addGoldAndUpdate(i, pos);
    }
    if (!checkPos(i, pos)) {
      return false;
    }
    placeItem(i, pos);
    return true;
  }

  /**
   * Return true if there is at least 1 slot unlocked among the adjacent slots,
   * otherwise return false. This function is useful to unlock new slots when the
   * hero level up.
   * 
   * @param pos represents a position in the backpack
   * @return boolean
   */
  private boolean nextToAnUnlockedSlot(Pos pos) {
    Objects.requireNonNull(pos);
    int x = pos.x();
    int y = pos.y();
    Pos[] directions = { new Pos(x, y - 1), new Pos(x, y + 1), new Pos(x - 1, y), new Pos(x + 1, y) };
    for (Pos p : directions) {
      if (!outOfTheBackpack(p) && !isBlocked(p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Unlocked a new slot at pos if possible. Return true if a new slot get
   * unlocked, else false.
   * 
   * @param pos represents a position in the backpack
   * @return boolean
   */
  public boolean unlockSlot(Pos pos) {
    Objects.requireNonNull(pos);
    if (isBlocked(pos) && nextToAnUnlockedSlot(pos)) {
      matrix[pos.y()][pos.x()] = ItemsCreation.createVoid();
      return true;
    }
    return false;
  }

  /**
   * 
   * Return true if the slot at pos is next to a pos in slotsThatWillBeUnlocked,
   * otherwise false
   * 
   * @param pos                     represents a position in the backpack
   * @param slotsThatWillBeUnlocked contains pos that are selected by the player
   *                                to be unlocked
   * @return boolean
   */
  private boolean nextToASlotThatWillBeUnlocked(Pos pos, List<Pos> slotsThatWillBeUnlocked) {
    if (slotsThatWillBeUnlocked == null) {
      return false;
    }
    int x = pos.x();
    int y = pos.y();
    Pos[] directions = { new Pos(x, y - 1), new Pos(x, y + 1), new Pos(x - 1, y), new Pos(x + 1, y) };
    for (Pos p : directions) {
      if (!outOfTheBackpack(p) && slotsThatWillBeUnlocked.contains(p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return a list of Pos items which contains the slots' pos that can be
   * unlocked.
   * 
   * @param slotsThatWillBeUnlocked contains pos that are selected by the player
   *                                to be unlocked
   * @return List<Pos>
   */
  public List<Pos> listOfUnlockableSlots(List<Pos> slotsThatWillBeUnlocked) {
    var unlockableSlots = new ArrayList<Pos>();
    for (int l = 0; l < 5; l++) {
      for (int c = 0; c < 7; c++) {
        var pos = new Pos(c, l);
        if (isBlocked(pos)
            && (nextToAnUnlockedSlot(new Pos(c, l)) || nextToASlotThatWillBeUnlocked(pos, slotsThatWillBeUnlocked))) {
          unlockableSlots.add(new Pos(c, l));
        }
      }
    }
    return List.copyOf(unlockableSlots);
  }

  /**
   * Function that return an integer that represents the number of slots the
   * player will be able to unlock.
   * 
   * @param unlockableSlots list that contains all the slots' coordinates which
   *                        can be unlocked.
   * @return int
   */
  public static int numberOfMoreUnlockSlots(List<Pos> unlockableSlots) {
    Objects.requireNonNull(unlockableSlots);
    if (unlockableSlots.size() < 4) {
      return unlockableSlots.size();
    } else {
      return random(3, 5);
    }
  }

  /**
   * Check if the selected slots can still be unlocked when one slot is removed
   * from this list. Return true if so, else false. It is a recursive function.
   * 
   * @param pos            represents the coordinates of the slot we check
   * @param alreadyVisited is a list of Pos which contains nothing the first time
   *                       and contains all the Pos already visited.
   * @param unlockedSlots  is a list of Pos which contains the slots the player
   *                       wants to unlock
   * @return boolean
   */
  private boolean slotCanStillBeUnlocked(Pos pos, List<Pos> alreadyVisited, List<Pos> unlockedSlots) {
    if (alreadyVisited.contains(pos)) {
      return false;
    }
    alreadyVisited.add(pos);
    var unlockableSlots = listOfUnlockableSlots(null);
    if (unlockableSlots.contains(pos)) {
      return true;
    }
    var x = pos.x();
    var y = pos.y();
    var neighbors = List.of(new Pos(x, y - 1), new Pos(x, y + 1), new Pos(x - 1, y), new Pos(x + 1, y));
    for (Pos n : neighbors) {
      if (unlockedSlots.contains(n) && slotCanStillBeUnlocked(n, alreadyVisited, unlockedSlots)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks the slots in unlockedSlots to see if there still can be unlocked and
   * return the number of remaining slots unlockable.
   *
   * @param unlockedSlots    contains pos which represent slots that will be
   *                         unlocked
   * @param numberOfNewSlots represents the number of remaining slots unlockable.
   * @return int
   */
  private int checkOtherSlots(List<Pos> unlockedSlots, int numberOfNewSlots) {
    if (unlockedSlots == null) {
      return numberOfNewSlots;
    }
    var it = unlockedSlots.iterator();
    while (it.hasNext()) {
      if (!slotCanStillBeUnlocked(it.next(), new ArrayList<Pos>(), unlockedSlots)) {
        it.remove();
        numberOfNewSlots++;
      }
    }
    return numberOfNewSlots;
  }

  /**
   * Removes slots from unlockedSlots that are not unlockable anymore and return
   * the number of remaining slots unlockable.
   * 
   * @param numberOfNewSlots represents the number of remaining slots unlockable.
   * @param unlockedSlots    contains pos which represent slots that will be
   *                         unlocked
   * @param pos              represents a position in the backpack
   * @return int
   */
  public int removeSlot(int numberOfNewSlots, List<Pos> unlockedSlots, Pos pos) {
    UtilsFunctions.checkIfNonNull(List.of(unlockedSlots, pos));
    if (numberOfNewSlots < 0) {
      throw new IllegalArgumentException("numberOfNewSlots must not be negative.");
    }
    unlockedSlots.remove(pos);
    numberOfNewSlots++;
    numberOfNewSlots = checkOtherSlots(unlockedSlots, numberOfNewSlots);
    return numberOfNewSlots;
  }

  /**
   * Set the bonus stats to 0 and then add the bonus from items which provide
   * buffs.
   * 
   * @param h is the Hero
   * @param m is the list of Monster
   */
  public void setItemStatsBeforeTurn(Hero h, List<Monster> m) {
    UtilsFunctions.checkIfNonNull(List.of(h, m));
    var items = getPlacedItems();
    for (var item : items) {
      item.setBonusBeforeTurn();
    }
    for (var item : items) {
      item.beforeTurn(h, m, this, item);
    }
  }

  /**
   * Check if it is possible to remove the item, if so return true, else false.
   * 
   * @param pos represents the coordinates in the list
   * @param s   is the item's shape
   * @return boolean
   */
  private boolean isPossibleRemovingItem(Pos pos, Shape s) {
    UtilsFunctions.checkIfNonNull(List.of(pos, s));
    if (outOfTheBackpack(new Pos(pos.x() + s.columns() - 1, pos.y() + s.lines() - 1))) {
      return false;
    }
    return true;
  }

  /**
   * Replaces the cells occupied by a shape with void items.
   *
   * @param topLeft top-left position of the shape
   * @param shape   the shape to clear
   */
  private void clearShapeCells(Pos topLeft, Shape shape) {
    for (int l = 0; l < shape.lines(); l++) {
      for (int c = 0; c < shape.columns(); c++) {
        if (l < shape.shape().length && c < shape.shape()[l].length && shape.shape()[l][c]) {
          matrix[topLeft.y() + l][topLeft.x() + c] = ItemsCreation.createVoid();
        }
      }
    }
  }

  /**
   * Function that return an item, or null if there is none and remove it from the
   * backpack
   * 
   * @param pos represents a position in the backpack
   * @return Item
   */
  public Item removeItem(Pos pos) {
    Objects.requireNonNull(pos);
    if (outOfTheBackpack(pos))
      return null;

    Item item = getItemFromPos(pos);
    if (item.getInfos().type() == ItemType.VOID)
      return null;

    Pos topLeft = item.getPos();
    if (!isPossibleRemovingItem(topLeft, item.getShape()))
      return null;

    clearShapeCells(topLeft, item.getShape());
    item.setPos(new Pos(-1, -1));
    countGolds();
    return item;
  }

  /**
   * look for the name of an item in the backpack
   * 
   * @param name of the item we looking for
   * @return the item or null if not found
   */
  public Item getItemByName(String name) {
    Objects.requireNonNull(name);
    for (Item item : this.getPlacedItems()) {
      if (item.getInfos().name().equalsIgnoreCase(name)) {
        return item;
      }
    }
    return null;
  }

  /**
   * Removes all items of type CURSE from the backpack. Useful for temporary
   * curses that disappear after combat.
   */
  public void removeAllCurses() {
    List<Item> items = getPlacedItems();
    for (Item item : items) {
      if (item.getInfos().type() == ItemType.CURSE) {
        removeItem(item.getPos());
      }
    }
  }

}
