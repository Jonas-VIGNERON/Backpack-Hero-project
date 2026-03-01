package affichage.gameController;

import com.github.forax.zen.ScreenInfo;

import affichage.gameModel.SimpleGameData;
import affichage.gameView.SimpleGameView;
import game.dungeon.Room;
import game.pos.Pos;

/**
 * Controller responsible for handling interactions with UI elements (Buttons,
 * Overlays, Special Interaction Modes).
 */
public class UIController {

  /**
   * Main entry point for blocking UI interactions. 
   * If a blocking mode is active (Curse, Unlock), this handles the input.
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param data Game data.
   * @param info Screen info.
   * @return true if the event was consumed by a UI element.
   */
  public static boolean handleBlockingUI(int mx, int my, SimpleGameData data, ScreenInfo info) {
    if (data.isCurseMode()) {
      return handleClickCurse(mx, my, data, info);
    }
    if (data.isUnlockingSlots()) {
      return handleUnlockMode(mx, my, data, info);
    }
    return false;
  }

  /**
   * Handles clicking the "Show Map" / "Show Backpack" toggle button.
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param data Game data.
   * @param info Screen info.
   * @return true if the button was clicked.
   */
  public static boolean clickSwitchMap(int mx, int my, SimpleGameData data, ScreenInfo info) {
    if (!data.getCurrentMonsters().isEmpty())
      return false;
    var x = info.width() - SimpleGameView.MAP_BUTTON_W - 50;
    var y = SimpleGameView.MAP_BUTTON_Y;
    if (mx >= x && mx <= x + SimpleGameView.MAP_BUTTON_W && my >= y && my <= y + SimpleGameView.MAP_BUTTON_H) {
      data.toggleMap();
      return true;
    }
    return false;
  }

  /**
   * Handles clicks on the Curse Overlay (Accept / Refuse buttons).
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param data Game data.
   * @param info Screen info.
   * @return true if an option was selected.
   */
  public static boolean handleClickCurse(int mx, int my, SimpleGameData data, ScreenInfo info) {
    var cx = info.width() / 2;
    var cy = info.height() / 2;
    if (mx >= cx - 250 && mx <= cx - 50 && my >= cy + 50 && my <= cy + 110) {
      data.resolveCurse(true);
      return true;
    }
    if (mx >= cx + 50 && mx <= cx + 250 && my >= cy + 50 && my <= cy + 110) {
      data.resolveCurse(false);
      return true;
    }
    return true;
  }

  /**
   * Manages clicks during the "Unlock Slot" phase (level up).
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param data Game data.
   * @param info Screen info.
   * @return true if a slot was clicked or validation occurred.
   */
  public static boolean handleUnlockMode(int mx, int my, SimpleGameData data, ScreenInfo info) {
    handleClickUnlockSlot(mx, my, data, info);
    if (clickValidateUnlock(mx, my, data, info)) {
      return true;
    }
    return true;
  }

  /**
   * Processes clicking on a grid slot to toggle its selection for unlocking.
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param data Game data.
   * @param info Screen info.
   */
  private static void handleClickUnlockSlot(int mx, int my, SimpleGameData data, ScreenInfo info) {
    var cols = data.getBackpackCols();
    var rows = data.getBackpackRows();
    var bpX = (info.width() - (cols * 64)) / 2;
    int bpY = 100;
    if (mx < bpX || my < bpY)
      return;
    var gx = (mx - bpX) / 64;
    var gy = (my - bpY) / 64;
    if (gx >= 0 && gx < cols && gy >= 0 && gy < rows) {
      processUnlockSelection(data, new Pos(gx, gy));
    }
  }

  /**
   * Updates the selection state of a slot during unlocking mode.
   *
   * @param data Game data.
   * @param pos  Grid position clicked.
   */
  private static void processUnlockSelection(SimpleGameData data, Pos pos) {
    var selected = data.getSelectedUnlockSlots();
    if (selected.contains(pos)) {
      var newRemaining = data.getBackpack().removeSlot(data.getRemainingUnlocks(), selected, pos);
      while (data.getRemainingUnlocks() < newRemaining) {
        data.incrementRemainingUnlocks();
      }
    } else if (data.getRemainingUnlocks() > 0 && data.getUnlockableSlots().contains(pos)) {
      selected.add(pos);
      data.decrementRemainingUnlocks();
    }
    data.getUnlockableSlots().clear();
    data.getUnlockableSlots().addAll(data.getBackpack().listOfUnlockableSlots(selected));
  }

  /**
   * Handles clicking the "Validate" button to confirm slot unlocks.
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param data Game data.
   * @param info Screen info.
   * @return true if validated.
   */
  private static boolean clickValidateUnlock(int mx, int my, SimpleGameData data, ScreenInfo info) {
    var btnWidth = 200;
    var btnHeight = 60;
    var x = (info.width() - btnWidth) / 2;
    var y = info.height() - btnHeight - 50;

    if (mx >= x && mx <= x + btnWidth && my >= y && my <= y + btnHeight) {
      data.validateUnlockSlots();
      return true;
    }
    return false;
  }

  /**
   * Handles interaction with the Healer interface buttons.
   *
   * @param mx   Mouse X.
   * @param my   Mouse Y.
   * @param data Game data.
   * @param info Screen info.
   * @param room The Healer room.
   * @return true if a heal option was purchased.
   */
  public static boolean clickHealer(int mx, int my, SimpleGameData data, ScreenInfo info, Room room) {
    var btnW = 350;
    var btnH = 60;
    var startX = (info.width() - btnW) / 2;
    var startY = 100 + (data.getBackpackRows() * 64) + 150;
    var gap = 80;
    for (int i = 0; i < 2; i++) {
      var y = startY + i * gap;
      if (mx >= startX && mx <= startX + btnW && my >= y && my <= y + btnH) {
        room.getHealer().purchaseOne(i, data);
        return true;
      }
    }
    return false;
  }
}