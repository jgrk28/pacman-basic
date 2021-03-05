package de.amr.games.pacman.ui.swing.app;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static java.awt.EventQueue.invokeLater;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.swing.PacManGameUI_Swing;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppSwing {

	/**
	 * Starts the Pac-Man game application.
	 * 
	 * @param args command-line arguments
	 *             <ul>
	 *             <li><code>-height</code> <em>value</em>: height of UI, default is 600</li>
	 *             <li><code>-pacman</code>: start in Pac-Man mode</li>
	 *             <li><code>-mspacman</code>: start in Ms. Pac-Man mode</li>
	 *             </ul>
	 */
	public static void main(String[] args) {
		new PacManGameAppSwing(new CommandLineArgs(args)).launch();
	}

	private CommandLineArgs options;
	private Thread gameLoopThread;
	private volatile boolean gameLoopRunning;
	private PacManGameController controller;

	public PacManGameAppSwing(CommandLineArgs options) {
		this.options = options;
		controller = new PacManGameController();
		if (options.pacman) {
			controller.play(GameType.PACMAN);
		} else {
			controller.play(GameType.MS_PACMAN);
		}
	}

	public void launch() {
		invokeLater(() -> {
			PacManGameUI_Swing ui = new PacManGameUI_Swing(controller, options.height);
			ui.addWindowClosingHandler(this::endGameLoop);
			controller.addView(ui);
			controller.showViews();
			startGameLoop();
		});
	}

	private void gameLoop() {
		while (gameLoopRunning) {
			clock.tick(controller::step);
		}
	}

	private void startGameLoop() {
		if (gameLoopRunning) {
			log("Game loop is already started");
			return;
		}
		gameLoopThread = new Thread(this::gameLoop, "PacManGameLoop");
		gameLoopThread.start();
		gameLoopRunning = true;
	}

	private void endGameLoop() {
		gameLoopRunning = false;
		try {
			gameLoopThread.join();
		} catch (Exception x) {
			x.printStackTrace();
		}
		log("Exit game and terminate VM");
		System.exit(0);
	}
}