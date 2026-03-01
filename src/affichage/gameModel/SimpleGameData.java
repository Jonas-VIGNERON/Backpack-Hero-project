package affichage.gameModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import affichage.Direction;
import affichage.FloatingItems;
import game.backpack.Backpack;
import game.dungeon.DungeonFloor;
import game.dungeon.DungeonGenerator;
import game.dungeon.Room;
import game.dungeon.RoomType;
import game.entity.Hero;
import game.entity.Monster;
import game.item.Item;
import game.item.ItemType;
import game.item.ItemsCreation;
import game.pos.Pos;

/**
 * The Central Model of the game. It holds the state of the Hero, Backpack,
 * Dungeon, and current interactions. This class acts as the "Model" in the MVC
 * architecture.
 */
public class SimpleGameData {

  private final Hero hero;
  private final Backpack backpack;
  private DungeonFloor dungeon;

  // Game State Flags
  private boolean showEnemy = false;
  private boolean showMap = false;
  private int currentLevel = 1;

  // Map & Exploration
  private int posMapX = 0;
  private int posMapY = 2;
  private List<Pos> pathHistory = new ArrayList<>();
  private Set<Pos> accessibleRooms = new HashSet<>();

  // Combat
  private List<Monster> currentMonsters;
  private Monster selectedMonster;

  // Item Interaction (Mouse Drag)
  private Item movedItem;
  private Pos movedItemOrigin;
  private Direction movedItemOriginalDirection;
  private int mouseX;
  private int mouseY;
  private List<Item> droppedItems = new ArrayList<>();
  private List<FloatingItems> floatingItems = new ArrayList<>();
  private Pos originalFloatingPos;

  // UI Modes (Unlock & Curse)
  private boolean unlockingSlots = false;
  private List<Pos> unlockableSlots = new ArrayList<>();
  private ArrayList<Pos> selectedUnlockSlots = new ArrayList<>();
  private int remainingUnlocks = 0;

  private boolean curseMode = false;
  private Item pendingCurseItem;
  private int curseRefusalCount = 0;
  private boolean placingCurse = false;

  /**
   * Initializes the game data with a default hero, an empty backpack, and
   * generates the first dungeon floor. It also gives starting items to the
   * player.
   */
  public SimpleGameData() {
    this.hero = new Hero();
    this.backpack = new Backpack();
    this.dungeon = DungeonGenerator.generate(hero);
    this.currentLevel = 1;
    this.currentMonsters = new ArrayList<>();
    initStartingItems();
    this.setHeroMapPosition(0, 2);
  }

  /**
   * Helper method to populate the backpack with initial gear.
   */
  private void initStartingItems() {
    var sword = ItemsCreation.createWoodenSword();
    var m = ItemsCreation.createMeal();
    var gold = ItemsCreation.createGold(12);
    var lc = ItemsCreation.createLeatherCap();
    backpack.addItem(sword, new Pos(2, 1));
    backpack.addItem(lc, new Pos(3, 1));
    backpack.addItem(gold, new Pos(4, 1));
    backpack.addItem(m, new Pos(3, 2));
  }

  // =========================================================
  // MAP LOGIC
  // =========================================================

  /**
   * Advances the game to the next dungeon level. Regenerates the dungeon and
   * resets player position.
   */
  public void nextLevel() {
    currentLevel++;
    pathHistory.clear();
    this.dungeon = DungeonGenerator.generate(hero);
    this.setHeroMapPosition(0, 2);
    this.showMap = true;
    this.showEnemy = false;
  }

  /**
   * Updates the hero's position on the map and updates exploration history.
   *
   * @param x The new X coordinate (column).
   * @param y The new Y coordinate (row).
   */
  public void setHeroMapPosition(int x, int y) {
    if (x < 0 || x >= dungeon.getCols() || y < 0 || y >= dungeon.getRows()) {
      return;
    }
    this.posMapX = x;
    this.posMapY = y;
    var p = new Pos(x, y);

    if (pathHistory.isEmpty() || !pathHistory.get(pathHistory.size() - 1).equals(p)) {
      pathHistory.add(p);
    }
    updateAccessibleRooms();
  }

  /**
   * Checks if a specific room is visible to the player (visited or adjacent).
   *
   * @param r The row index of the room.
   * @param c The column index of the room.
   * @return true if the room should be drawn, false otherwise.
   */
  public boolean isRoomVisible(int r, int c) {
    if (dungeon.getRoom(r, c).getType() == RoomType.BLOCKED)
      return true;
    var target = new Pos(c, r);
    return pathHistory.contains(target) || accessibleRooms.contains(target);
  }

  /**
   * Recalculates the set of rooms accessible from the current position. Uses a
   * Breadth-First Search (BFS) to find connected transparent rooms.
   */
  private void updateAccessibleRooms() {
    accessibleRooms.clear();
    Queue<Pos> queue = new LinkedList<>();
    var start = new Pos(posMapX, posMapY);
    queue.add(start);
    accessibleRooms.add(start);
    int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };

    while (!queue.isEmpty()) {
      var curr = queue.poll();
      if (isTransparent(curr) || curr.equals(start)) {
        for (var dir : directions) {
          addNeighborIfValid(curr.y() + dir[0], curr.x() + dir[1], queue);
        }
      }
    }
  }

  /**
   * Checks if a room allows vision to pass through (not blocked, not an active
   * enemy room).
   *
   * @param p The position of the room.
   * @return true if transparent.
   */
  private boolean isTransparent(Pos p) {
    var r = dungeon.getRoom(p.y(), p.x());
    return r.getType() != RoomType.BLOCKED && !(r.getType() == RoomType.ENEMY && r.hasLivingMonsters());
  }

  /**
   * Adds a neighbor to the processing queue if valid and not visited.
   *
   * @param nr    The neighbor row.
   * @param nc    The neighbor column.
   * @param queue The BFS queue.
   */
  private void addNeighborIfValid(int nr, int nc, Queue<Pos> queue) {
    if (nr >= 0 && nr < dungeon.getRows() && nc >= 0 && nc < dungeon.getCols()) {
      var neighbor = new Pos(nc, nr);
      if (!accessibleRooms.contains(neighbor)) {
        var room = dungeon.getRoom(nr, nc);
        if (room.getType() != RoomType.BLOCKED) {
          accessibleRooms.add(neighbor);
          queue.add(neighbor);
        }
      }
    }
  }

  /**
   * Toggles the full-screen map view.
   */
  public void toggleMap() {
    this.showMap = !this.showMap;
  }

  // =========================================================
  // ITEM MOVEMENT
  // =========================================================

  /**
   * Initiates the dragging of an item from the backpack.
   *
   * @param item        The item being moved.
   * @param coordOrigin The original position of the item in the backpack.
   */
  public void startMoveItem(Item item, Pos coordOrigin) {
    this.movedItem = item;
    this.movedItemOrigin = item.getPos();
    this.movedItemOriginalDirection = item.getDirection();
    if (coordOrigin != null) {
      backpack.removeItem(coordOrigin);
    }
  }

  /**
   * Finalizes the movement of an item.
   *
   * @param insideBackpack True if the item was dropped inside the grid.
   * @param newPos         The new position (if inside).
   */
  public void stopMoveItem(boolean insideBackpack, Pos newPos) {
    if (placingCurse) {
      return;
    }
    if (movedItem == null) {
      return;
    }
    if (insideBackpack) {
      if (!backpack.addItem(movedItem, newPos)) {
        cancelMoveItem();
      }
    } else {
      dropItemOutside();
    }
    clearMoveState();
  }

  /**
   * Resets the internal state of the moved item.
   */
  private void clearMoveState() {
    this.movedItem = null;
    this.movedItemOrigin = null;
  }

  /**
   * Cancels the current item movement and returns the item to its original place.
   * If the item came from outside (floating), it returns to floating state.
   */
  public void cancelMoveItem() {

    if (placingCurse) {
      return;
    }
    if (movedItem != null && movedItemOriginalDirection != null) {
      movedItem.setDirection(movedItemOriginalDirection);
    }
    if (movedItemOrigin != null) {
      backpack.addItem(movedItem, movedItemOrigin);
    } else {

      if (originalFloatingPos != null) {
        floatingItems.add(new FloatingItems(movedItem, originalFloatingPos.x(), originalFloatingPos.y()));
      }
    }
  }

  /**
   * Forces the cancellation of a move
   */
  public void forceCancel() {
    if (placingCurse) {
      return;
    }
    cancelMoveItem();
    clearMoveState();
  }

  /**
   * Handles dropping an item outside the backpack area. If it's a curse, it
   * cannot be dropped and returns to the bag.
   */
  public void dropItemOutside() {
    if (movedItem != null) {
      if (placingCurse) {
        return;
      }
      if (movedItem.getInfos().type() == ItemType.CURSE) {
        cancelMoveItem();
        return;
      }
      floatingItems.add(new FloatingItems(movedItem, mouseX, mouseY));
      this.movedItem = null;
      this.movedItemOrigin = null;
    }
  }

  /**
   * Starts dragging an item that was floating in the world.
   *
   * @param floating The floating item wrapper.
   */
  public void startMoveFloatingItem(FloatingItems floating) {
    this.movedItem = floating.item();
    this.movedItemOrigin = null;
    this.originalFloatingPos = new Pos(floating.x(), floating.y());
    this.movedItemOriginalDirection = this.movedItem.getDirection();
    floatingItems.remove(floating);
  }

  /**
   * Clears all floating items from the screen.
   */
  public void clearFloatingItems() {
    floatingItems.clear();
  }

  // =========================================================
  // CURSE LOGIC
  // =========================================================

  /**
   * Triggers the curse mode, displaying the curse overlay.
   *
   * @param item The curse item to be placed.
   */
  public void triggerCurse(Item item) {
    this.curseMode = true;
    this.pendingCurseItem = item;
  }

  /**
   * Resolves the player's choice regarding the curse.
   *
   * @param accepted True if the player accepted the curse.
   */
  public void resolveCurse(boolean accepted) {
    if (accepted) {
      this.placingCurse = true;
      this.curseMode = false;
      startMoveItem(pendingCurseItem, null);
      this.pendingCurseItem = null;
    } else {
      curseRefusalCount++;
      var damage = curseRefusalCount * 5;
      hero.takeDamageIgnoringShield(damage);
      this.curseMode = false;
      this.pendingCurseItem = null;
    }
  }

  /**
   * Removes all curses from the backpack. Called after combat.
   */
  public void cleanCursesAfterCombat() {
    backpack.removeAllCurses();
  }

  /**
   * Finalizes the manual placement of a curse. Clears the item currently being
   * dragged and exits the mandatory placement mode.
   */
  public void cursePlacedSuccessfully() {
    this.movedItem = null;
    this.movedItemOrigin = null;
    this.placingCurse = false;
  }

  // =========================================================
  // COMBAT & UNLOCK MODES
  // =========================================================

  /**
   * Starts a combat encounter with a list of monsters.
   *
   * @param monstersFromRoom The list of monsters.
   */
  public void startCombat(List<Monster> monstersFromRoom) {
    this.currentMonsters = monstersFromRoom;
    game.entity.monsters.ListOfMonster.setMonstersNextMoves(this.currentMonsters);
    this.showEnemy = true;
    this.showMap = false;
  }

  /**
   * Enters the mode where the player can unlock new backpack slots.
   */
  public void enterUnlockMode() {
    unlockableSlots = new ArrayList<>(backpack.listOfUnlockableSlots(null));
    remainingUnlocks = Backpack.numberOfMoreUnlockSlots(unlockableSlots);
    selectedUnlockSlots.clear();
    unlockingSlots = true;
  }

  /**
   * Validates the selected slots and unlocks them.
   */
  public void validateUnlockSlots() {
    for (Pos pos : selectedUnlockSlots) {
      backpack.unlockSlot(pos);
    }
    selectedUnlockSlots.clear();
    unlockableSlots = List.of();
    unlockingSlots = false;
  }

  /**
   * Increments the count of remaining unlockable slots.
   */
  public void incrementRemainingUnlocks() {
    remainingUnlocks++;
  }

  /**
   * Decrements the count of remaining unlockable slots.
   */
  public void decrementRemainingUnlocks() {
    remainingUnlocks--;
  }

  // =========================================================
  // GETTERS & SETTERS
  // =========================================================

  /**
   * Getter for the hero
   * 
   * @return the hero
   */
  public Hero getHero() {
    return hero;
  }

  /**
   * Getter for the backpack
   * 
   * @return the backpack
   */
  public Backpack getBackpack() {
    return backpack;
  }

  /**
   * Getter for the current floor
   * 
   * @return the dungeon
   */
  public DungeonFloor getDungeon() {
    return dungeon;
  }

  /**
   * Getter for the list of monster
   * 
   * @return list of monsters
   */
  public List<Monster> getCurrentMonsters() {
    return currentMonsters;
  }

  /**
   * Sets the monster currently targeted by the player.
   * 
   * @param m the selected monster
   */
  public void setSelectedMonster(Monster m) {
    this.selectedMonster = m;
  }

  /**
   * Gets the currently selected monster. Defaults to the first one if none
   * selected.
   * 
   * @return the targeted monster
   */
  public Monster getSelectedMonster() {
    if ((selectedMonster == null || selectedMonster.getHp() <= 0) && !currentMonsters.isEmpty()) {
      return currentMonsters.get(0);
    }
    return selectedMonster;
  }

  /**
   * Getter for the backpack row
   * 
   * @return rows count
   */
  public int getBackpackRows() {
    return backpack.getRows();
  }

  /**
   * Getter for the backpack cols
   * 
   * @return columns count
   */
  public int getBackpackCols() {
    return backpack.getCols();
  }

  /**
   * Checks if the enemy view should be shown.
   * 
   * @return true if showing enemies
   */
  public boolean isShowEnemy() {
    return showEnemy;
  }

  /**
   * Checks if the map view should be shown.
   * 
   * @return true if showing map
   */
  public boolean isShowMap() {
    return showMap;
  }

  /**
   * Gets the Hero's X position on the dungeon map.
   * 
   * @return X coordinate
   */
  public int getHeroMapX() {
    return posMapX;
  }

  /**
   * Gets the Hero's Y position on the dungeon map.
   * 
   * @return Y coordinate
   */
  public int getHeroMapY() {
    return posMapY;
  }

  /**
   * Gets the history of visited positions on the map.
   * 
   * @return list of positions
   */
  public List<Pos> getPathHistory() {
    return pathHistory;
  }

  /**
   * Gets the list of items placed in the backpack for drawing.
   * 
   * @return list of items
   */
  public List<Item> getItemsToDraw() {
    return backpack.getPlacedItems();
  }

  /**
   * Gets the type of item at a specific backpack slot.
   * 
   * @param x Column index
   * @param y Row index
   * @return The ItemType
   */
  public ItemType getBackpackSlotType(int x, int y) {
    return backpack.getItemTypeAt(new Pos(x, y));
  }

  /**
   * Checks if an item is currently being moved/dragged.
   * 
   * @return true if moving
   */
  public boolean isMoving() {
    return movedItem != null;
  }

  /**
   * Gets the item currently being moved.
   * 
   * @return the moved item or null
   */
  public Item getMovedItem() {
    return movedItem;
  }

  /**
   * Gets the list of items dropped outside (unused currently but kept for
   * structure).
   * 
   * @return list of dropped items
   */
  public List<Item> getDroppedItems() {
    return droppedItems;
  }

  /**
   * Gets the list of floating items (loot/shop) to display.
   * 
   * @return list of floating items
   */
  public List<FloatingItems> getFloatingItems() {
    return floatingItems;
  }

  /**
   * Updates the tracked mouse position.
   * 
   * @param x Mouse X
   * @param y Mouse Y
   */
  public void updateMouse(int x, int y) {
    this.mouseX = x;
    this.mouseY = y;
  }

  /**
   * Gets the last known mouse X position.
   * 
   * @return X coordinate
   */
  public int getMouseX() {
    return mouseX;
  }

  /**
   * Gets the last known mouse Y position.
   * 
   * @return Y coordinate
   */
  public int getMouseY() {
    return mouseY;
  }

  /**
   * Checks if the game is in Unlock Slot mode.
   * 
   * @return true if unlocking
   */
  public boolean isUnlockingSlots() {
    return unlockingSlots;
  }

  /**
   * Gets the list of slots available for unlocking.
   * 
   * @return list of positions
   */
  public List<Pos> getUnlockableSlots() {
    return unlockableSlots;
  }

  /**
   * Gets the list of slots currently selected for unlocking.
   * 
   * @return list of positions
   */
  public List<Pos> getSelectedUnlockSlots() {
    return selectedUnlockSlots;
  }

  /**
   * Gets the number of remaining unlocks available.
   * 
   * @return count
   */
  public int getRemainingUnlocks() {
    return remainingUnlocks;
  }

  /**
   * Checks if the game is in Curse Choice mode.
   * 
   * @return true if curse mode
   */
  public boolean isCurseMode() {
    return curseMode;
  }

  /**
   * Gets the curse item pending acceptance or refusal.
   * 
   * @return the curse item
   */
  public Item getPendingCurseItem() {
    return pendingCurseItem;
  }

  /**
   * Gets the number of times the player has refused a curse.
   * 
   * @return count
   */
  public int getCurseRefusalCount() {
    return curseRefusalCount;
  }

  /**
   * check if the player is placing a curse or not
   * 
   * @return
   */
  public boolean isPlacingCurse() {
    return placingCurse;
  }

  /**
   * setter to know if the player is placing a curse or not
   * 
   * @param v true or false
   */
  public void setPlacingCurse(boolean v) {
    this.placingCurse = v;
  }
}