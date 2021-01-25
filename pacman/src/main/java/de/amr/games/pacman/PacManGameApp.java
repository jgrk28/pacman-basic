package de.amr.games.pacman;

import static java.awt.EventQueue.invokeLater;
import static java.lang.Float.parseFloat;

import de.amr.games.pacman.game.core.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.KeyboardPacController;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp {

	public static void main(String[] args) {
		float scaling = args.length > 1 ? parseFloat(args[1]) : 2;
		invokeLater(() -> {
			PacManGameController gameController = new PacManGameController();
			gameController.newMsPacManGame();
			PacManGameUI ui = new PacManGameSwingUI(new V2i(28, 36), scaling);
			ui.setGameController(gameController);
			gameController.pacController = new KeyboardPacController(ui);
			ui.openWindow();
			new Thread(gameController, "PacManGame").start();
		});
	}
}