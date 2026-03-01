package affichage.gameController;

import java.util.List;

import com.github.forax.zen.ScreenInfo;

import affichage.gameModel.SimpleGameData;
import affichage.gameView.SimpleGameView;
import game.entity.Monster;
import game.fight.Fight;

/**
 * Controller responsible for combat mechanics: ending turns, managing monster
 * deaths, and selecting targets.
 */
public class CombatController {

	/**
	 * Checks if the user clicked on the "END TURN" button.
	 *
	 * @param mx   Mouse X.
	 * @param my   Mouse Y.
	 * @param info Screen info.
	 * @return true if the click is within button bounds.
	 */
	public static boolean isClickOnEndTurn(int mx, int my, ScreenInfo info) {
		var btnW = SimpleGameView.BUTTON_WIDTH;
		var btnH = SimpleGameView.BUTTON_HEIGHT;
		var btnX = info.width() - btnW - 50;
		var btnY = info.height() - btnH - 50;
		return mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
	}

	/**
	 * Processes the logic for ending a turn. Executes post-turn hero effects,
	 * monster turns, and pre-turn hero effects. Checks for game over or victory
	 * conditions at each step.
	 *
	 * @param data Game data.
	 * @return true if the game continues, false if game over logic interrupts.
	 */
	public static boolean processEndTurn(SimpleGameData data) {
		Fight.afterHeroTurn(data);
		if (!checkGameOver(data))
			return false;
		if (checkMonstersDeath(data))
			return true;
		Fight.beforeHeroTurn(data);
		if (!checkGameOver(data))
			return false;
		if (checkMonstersDeath(data))
			return true;

		return true;
	}

	/**
	 * Checks if all monsters are dead. If so, cleans up combat state and spawns
	 * rewards.
	 *
	 * @param data Game data.
	 * @return true if all monsters are dead.
	 */
	public static boolean checkMonstersDeath(SimpleGameData data) {
		if (data.getCurrentMonsters().isEmpty()) {
			data.cleanCursesAfterCombat();
			MapController.addRewardToListOfFloatingItem(data);
			return true;
		}
		return false;
	}

	/**
	 * Checks if the hero is dead.
	 *
	 * @param data Game data.
	 * @return true if the hero is alive, false otherwise.
	 */
	private static boolean checkGameOver(SimpleGameData data) {
		if (data.getHero().getHp() <= 0) {
			System.out.println("GAME OVER");
			return false;
		}
		return true;
	}

	/**
	 * Handles selecting a specific monster as a target when clicked.
	 *
	 * @param mx   Mouse X.
	 * @param my   Mouse Y.
	 * @param data Game data.
	 * @param info Screen info.
	 * @return true if a monster was selected.
	 */
	public static boolean clickSelectMonster(int mx, int my, SimpleGameData data, ScreenInfo info) {
		List<Monster> monsters = data.getCurrentMonsters();
		if (monsters.isEmpty())
			return false;
		var cols = data.getBackpackCols();
		var bpWidth = cols * 64;
		var bpX = (info.width() - bpWidth) / 2;
		var bpY = 100;
		var startX = bpX + bpWidth + 100;
		var groundY = bpY + (data.getBackpackRows() * 64) + 300;
		var gap = 180;
		for (var i = 0; i < monsters.size(); i++) {
			int monsterCenterX = startX + (i * gap);
			if (mx >= monsterCenterX - 75 && mx <= monsterCenterX + 75 && my >= groundY - 200 && my <= groundY) {
				data.setSelectedMonster(monsters.get(i));
				return true;
			}
		}
		return false;
	}
}