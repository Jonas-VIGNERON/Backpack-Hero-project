package affichage.gameController;

import com.github.forax.zen.ScreenInfo;

import affichage.FloatingItems;
import affichage.gameModel.SimpleGameData;
import affichage.gameView.SimpleGameView;
import game.dungeon.Room;
import game.dungeon.RoomType;
import game.event.Merchant;
import game.fight.Fight;
import game.item.Item;
import game.item.ItemType;
import game.item.ItemsCreation;
import game.pos.Pos;

/**
 * Controller for item interactions: pickup, drop, move, rotate, use.
 */
public class ItemController {

  /**
   * Tries to pick up an item (floating or in backpack).
   * 
   * @param mx   mouse x
   * @param my   mouse y
   * @param data game data
   * @param info screen info
   */
  public static void tryPickUpItem(int mx, int my, SimpleGameData data, ScreenInfo info) {
    if (tryPickUpFloating(mx, my, data))
      return;
    tryPickUpBackpack(mx, my, data, info);
  }

  /**
   * Checks and picks up a floating item.
   * 
   * @param mx   mouse x
   * @param my   mouse y
   * @param data game data
   * @return true if picked up
   */
  private static boolean tryPickUpFloating(int mx, int my, SimpleGameData data) {
    var floatingList = data.getFloatingItems();
    for (var i = floatingList.size() - 1; i >= 0; i--) {
      FloatingItems fItem = floatingList.get(i);
      if (isMouseOverItem(mx, my, fItem)) {
        data.startMoveFloatingItem(fItem);
        return true;
      }
    }
    return false;
  }

  /**
   * Checks and picks up an item from the backpack grid.
   * 
   * @param mx   mouse x
   * @param my   mouse y
   * @param data game data
   * @param info screen info
   */
  private static void tryPickUpBackpack(int mx, int my, SimpleGameData data, ScreenInfo info) {
    var cellSize = 64;
    var bpX = (info.width() - (data.getBackpackCols() * cellSize)) / 2;
    var bpY = 100;
    if (mx < bpX || my < bpY)
      return;
    var gx = (mx - bpX) / cellSize;
    var gy = (my - bpY) / cellSize;
    var pos = new Pos(gx, gy);
    var item = data.getBackpack().getItemFromPos(pos);

    if (isValidItemToMove(item)) {
      data.startMoveItem(item, pos);
    }
  }

  /**
   * Validates if an item can be moved (not null, not curse, etc.).
   * 
   * @param item the item to check
   * @return true if movable
   */
  private static boolean isValidItemToMove(Item item) {
    return item != null && item.getInfos().type() != ItemType.CURSE && item.getInfos().type() != ItemType.VOID
        && item.getInfos().type() != ItemType.BLOCKED;
  }

  /**
   * Checks if mouse is within a floating item's bounds.
   * 
   * @param mx    mouse x
   * @param my    mouse y
   * @param fItem floating item
   * @return true if hovering
   */
  private static boolean isMouseOverItem(int mx, int my, FloatingItems fItem) {
    var item = fItem.item();
    var w = item.getShape().columns() * 64;
    var h = item.getShape().lines() * 64;
    var left = fItem.x() - (w / 2);
    var top = fItem.y() - (h / 2);
    return mx >= left && mx <= left + w && my >= top && my <= top + h;
  }

  /**
   * Handles dropping the currently moved item. Dispatcher method.
   * 
   * @param mx   mouse x
   * @param my   mouse y
   * @param data game data
   * @param info screen info
   */
  public static void handleItemDrop(int mx, int my, SimpleGameData data, ScreenInfo info) {
    var item = data.getMovedItem();
    if (item == null)
      return;
    var cellSize = 64;
    var bpX = (info.width() - (data.getBackpackCols() * cellSize)) / 2;
    var bpY = 100;
    if (data.isPlacingCurse()) {
      handleCurseDrop(mx, my, data, item, bpX, bpY);
    }
    if (trySellItem(mx, my, data, item, info)) {
      return;
    }
    handleNormalDrop(mx, my, data, item, bpX, bpY);
  }

  /**
   * Manages the mandatory placement of a curse item.
   * 
   * @param mx   mouse x
   * @param my   mouse y
   * @param data game data
   * @param item curse item
   * @param bpX  backpack x origin
   * @param bpY  backpack y origin
   */
  private static void handleCurseDrop(int mx, int my, SimpleGameData data, Item item, int bpX, int bpY) {
    Pos gridPos = getGridPosition(mx, my, data, item, bpX, bpY);
    if (gridPos != null) {
      if (data.getBackpack().addCurse(item, gridPos)) {
        data.cursePlacedSuccessfully();
      }
    }
  }

  /**
   * Manages the drop of a standard item (inside or outside backpack).
   * 
   * @param mx   mouse x
   * @param my   mouse y
   * @param data game data
   * @param item item being dropped
   * @param bpX  backpack x origin
   * @param bpY  backpack y origin
   */
  private static void handleNormalDrop(int mx, int my, SimpleGameData data, Item item, int bpX, int bpY) {
    var gridPos = getGridPosition(mx, my, data, item, bpX, bpY);
    if (gridPos != null) {
      handleDropInside(data, item, gridPos.x(), gridPos.y());
    } else {
      var bpWidth = data.getBackpackCols() * 64;
      handleDropOutside(data, item, mx, my, bpX, bpY, bpWidth);
    }
  }

  /**
   * Calculates the grid position based on mouse coordinates. Returns null if the
   * item is dropped outside the grid.
   */
  private static Pos getGridPosition(int mx, int my, SimpleGameData data, Item item, int bpX, int bpY) {
    var cellSize = 64;
    var itemW = item.getShape().columns() * cellSize;
    var itemH = item.getShape().lines() * cellSize;
    var visualX = mx - (itemW / 2);
    var visualY = my - (itemH / 2);
    var gx = (int) Math.round((double) (visualX - bpX) / cellSize);
    var gy = (int) Math.round((double) (visualY - bpY) / cellSize);
    if (gx >= 0 && gx < data.getBackpackCols() && gy >= 0 && gy < data.getBackpackRows()) {
      return new Pos(gx, gy);
    }
    return null;
  }

  /**
   * Logic for dropping an item inside the backpack
   * 
   * @param data game data
   * @param item item dropped
   * @param gx   grid x
   * @param gy   grid y
   */
  private static void handleDropInside(SimpleGameData data, Item item, int gx, int gy) {
    if (!tryPurchaseIfMerchant(data, item)) {
      data.forceCancel();
      return;
    }
    data.stopMoveItem(true, new Pos(gx, gy));
  }

  /**
   * Handles merchant purchase logic.
   * 
   * @param data game data
   * @param item item being dropped
   * @return true if purchase allowed or not relevant
   */
  private static boolean tryPurchaseIfMerchant(SimpleGameData data, Item item) {
    var currentRoom = data.getDungeon().getRoom(data.getHeroMapY(), data.getHeroMapX());
    if (currentRoom.getType() != RoomType.MERCHANT)
      return true;
    if (!currentRoom.getMerchant().getShop().contains(item))
      return true;
    var price = Merchant.definePrice(item);
    if (data.getBackpack().getGolds() >= price) {
      data.getBackpack().reduceGold(price);
      currentRoom.getMerchant().getShop().remove(item);
      return true;
    }
    return false;
  }

  /**
   * Logic for dropping an item outside (cancel or drop). Fixed arguments to match
   * the call.
   * 
   * @param data game data
   * @param item item dropped
   * @param mx   mouse x (center)
   * @param my   mouse y (center)
   * @param bpX  backpack x origin
   * @param bpY  backpack y origin
   * @param bpW  backpack width
   */
  private static void handleDropOutside(SimpleGameData data, Item item, int mx, int my, int bpX, int bpY, int bpW) {
    var itemW = item.getShape().columns() * 64;
    var itemH = item.getShape().lines() * 64;
    var visualLeft = mx - itemW / 2;
    var visualTop = my - itemH / 2;
    var backpackHeight = data.getBackpackRows() * 64;
    var overlaps = visualLeft < bpX + bpW && visualLeft + itemW > bpX && visualTop < bpY + backpackHeight
        && visualTop + itemH > bpY;
    if (overlaps) {
      data.forceCancel();
    } else {
      data.stopMoveItem(false, null);
    }
  }

  /**
   * Rotates the currently held item.
   * 
   * @param data game data
   */
  public static void rotateItemIfMoving(SimpleGameData data) {
    if (data.isMoving()) {
      var item = data.getMovedItem();
      if (item != null && item.getInfos().type() != ItemType.CURSE) {
        item.rotate();
      }
    }
  }

  /**
   * Uses an item in the backpack (click effect).
   * 
   * @param mx   mouse x
   * @param my   mouse y
   * @param data game data
   * @param info screen info
   */
  public static void clickItem(int mx, int my, SimpleGameData data, ScreenInfo info) {
    var cols = data.getBackpackCols();
    var bpWidth = cols * 64;
    var bpX = (info.width() - bpWidth) / 2;
    var bpY = 100;
    if (mx < bpX || my < bpY)
      return;
    var gx = (mx - bpX) / 64;
    var gy = (my - bpY) / 64;
    if (gx >= 0 && gx < cols && gy >= 0 && gy < data.getBackpackRows()) {
      Pos pos = new Pos(gx, gy);
      Fight.doMovesFromPos(pos, data);
      Fight.ifMonstersDie(data);
    }
  }

  /**
   * Tries to sell the item if it is dropped onto the Sell Zone in a Merchant
   * room. If successful, it spawns floating gold rewards and removes the item
   * from hand.
   *
   * @param mx   Mouse X coordinate.
   * @param my   Mouse Y coordinate.
   * @param data Game data.
   * @param item The item being dropped.
   * @param info Screen info.
   * @return true if the item was sold, false otherwise.
   */
  private static boolean trySellItem(int mx, int my, SimpleGameData data, Item item, ScreenInfo info) {
    var room = data.getDungeon().getRoom(data.getHeroMapY(), data.getHeroMapX());
    if (room.getType() != RoomType.MERCHANT || item.getInfos().type() == ItemType.GOLD) {
      return false;
    }
    if (room.getMerchant().getShop().contains(item)) {
      return false;
    }
    if (isOverSellZone(mx, my, info)) {
      var price = Merchant.definePrice(item) / 2;
      var goldReward = ItemsCreation.createGold(price);
      data.getFloatingItems().add(new FloatingItems(goldReward, mx, my));
      data.cursePlacedSuccessfully();
      return true;
    }
    return false;
  }

  /**
   * Checks if the mouse cursor is hovering over the Merchant's Sell Zone.
   * Calculates the zone position dynamically based on screen width.
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param info Screen info.
   * @return true if the mouse is inside the sell button.
   */
  private static boolean isOverSellZone(int mx, int my, ScreenInfo info) {
    var cols = 7;
    var bpWidth = cols * 64;
    var startX = (info.width() - bpWidth) / 2 + bpWidth + 20;
    var startY = 100;

    return mx >= startX && mx <= startX + SimpleGameView.SELL_BTN_W && my >= startY
        && my <= startY + SimpleGameView.SELL_BTN_H;
  }
}