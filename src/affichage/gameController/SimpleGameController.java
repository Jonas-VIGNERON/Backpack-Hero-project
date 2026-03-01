package affichage.gameController;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import com.github.forax.zen.ScreenInfo;

import affichage.ImageLoader;
import affichage.gameModel.SimpleGameData;
import affichage.gameView.SimpleGameView;
import game.hallOfFame.HallOfFame;

/**
 * Main Controller class acting as the entry point of the application. It
 * initializes the game components, manages the main game loop, and dispatches
 * input events (keyboard/mouse) to specialized sub-controllers.
 */
public class SimpleGameController {

	/**
	 * Main method to launch the application.
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		Application.run(Color.BLACK, SimpleGameController::backpackGame);
	}

	/**
	 * Initializes the game context, data, view, and starts the game loop. Runs
	 * until the window is closed or the game loop returns false.
	 *
	 * @param context The application context provided by the Zen library.
	 */
	private static void backpackGame(ApplicationContext context) {
		var images = ImageLoader.loadImages();
		var data = new SimpleGameData();
		var view = new SimpleGameView();

		render(context, data, view, images);

		while (true) {
			if (!gameLoop(context, data, view, images)) {
				context.dispose();
				HallOfFame.enterHallOfFame(data.getBackpack(), data.getHero());
				return;
			}
		}
	}

	/**
	 * The core game loop processing events and updating the display.
	 *
	 * @param context The application context.
	 * @param data    The game data model.
	 * @param view    The game view renderer.
	 * @param images  The loaded image resources.
	 * @return true to continue the loop, false to exit.
	 */
	private static boolean gameLoop(ApplicationContext context, SimpleGameData data, SimpleGameView view,
			Map<String, BufferedImage> images) {
		var event = context.pollOrWaitEvent(10);
		while (event != null) {
			if (!handleEvent(event, context, data)) {
				return false;
			}
			event = context.pollEvent();
		}
		render(context, data, view, images);
		return true;
	}

	/**
	 * Renders a single frame of the game.
	 *
	 * @param context The application context.
	 * @param data    The game data model.
	 * @param view    The game view renderer.
	 * @param images  The loaded image resources.
	 */
	private static void render(ApplicationContext context, SimpleGameData data, SimpleGameView view,
			Map<String, BufferedImage> images) {
		context.renderFrame(g -> {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
			view.draw(g, context.getScreenInfo(), data, images);
		});
	}

	/**
	 * Dispatches raw Zen events to specific handlers based on event type.
	 *
	 * @param event   The event to handle.
	 * @param context The application context.
	 * @param data    The game data model.
	 * @return true to keep running, false to quit
	 */
	private static boolean handleEvent(Event event, ApplicationContext context, SimpleGameData data) {
		return switch (event) {
		case KeyboardEvent ke -> handleKeyboard(ke, data);
		case PointerEvent pe -> handlePointer(pe, context.getScreenInfo(), data);
		default -> true;
		};
	}

	/**
	 * Handles keyboard input events. 'R' rotates the item being moved. 'Q' quits
	 * the game.
	 *
	 * @param ke   The keyboard event.
	 * @param data The game data model.
	 * @return true if the game should continue, false if 'Q' was pressed.
	 */
	private static boolean handleKeyboard(KeyboardEvent ke, SimpleGameData data) {
		if (ke.action() == KeyboardEvent.Action.KEY_PRESSED && ke.key() == KeyboardEvent.Key.R) {
			ItemController.rotateItemIfMoving(data);
		}
		return ke.key() != KeyboardEvent.Key.Q;
	}

	/**
	 * Handles mouse pointer events (movement, clicks).
	 *
	 * @param pe   The pointer event.
	 * @param info Screen information.
	 * @param data The game data model.
	 * @return always true (game continues on mouse action).
	 */
	private static boolean handlePointer(PointerEvent pe, ScreenInfo info, SimpleGameData data) {
		var mx = pe.location().x();
		var my = pe.location().y();
		data.updateMouse(mx, my);

		if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
			return dispatchPointerDown(mx, my, data, info);
		} else if (pe.action() == PointerEvent.Action.POINTER_UP) {
			ItemController.handleItemDrop(mx, my, data, info);
		}
		return true;
	}

	/**
	 * Dispatches mouse click events to specific controllers based on priority and
	 * game state.
	 *
	 * @param mx   Mouse X coordinate.
	 * @param my   Mouse Y coordinate.
	 * @param data The game data model.
	 * @param info Screen information.
	 * @return true if the click was handled.
	 */
	private static boolean dispatchPointerDown(int mx, int my, SimpleGameData data, ScreenInfo info) {
    if (data.isPlacingCurse()) {
      return true; 
    }
    if (UIController.handleBlockingUI(mx, my, data, info)) {
      return true;
    }
    if (UIController.clickSwitchMap(mx, my, data, info)) {
      return true;
    }
    if (data.isShowMap()) {
      MapController.clickMap(mx, my, data, info);
      return true;
    }
    return handleGameplayClicks(mx, my, data, info);
  }

	/**
	 * Handles clicks related to the main gameplay loop (Combat, Item usage, Room
	 * interactions).
	 *
	 * @param mx   Mouse X coordinate.
	 * @param my   Mouse Y coordinate.
	 * @param data The game data model.
	 * @param info Screen information.
	 * @return true if an action was performed.
	 */
	private static boolean handleGameplayClicks(int mx, int my, SimpleGameData data, ScreenInfo info) {
		if (MapController.handleSpecialRoomClick(mx, my, data, info))
			return true;
		if (!data.getCurrentMonsters().isEmpty()) {
			if (CombatController.isClickOnEndTurn(mx, my, info)) {
				return CombatController.processEndTurn(data);
			}
			if (CombatController.clickSelectMonster(mx, my, data, info)) {
				return true;
			}
			ItemController.clickItem(mx, my, data, info);
			CombatController.checkMonstersDeath(data);
		} else {
			ItemController.tryPickUpItem(mx, my, data, info);
		}
		return true;
	}
}