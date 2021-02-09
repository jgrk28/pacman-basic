package de.amr.games.pacman.ui.fx.app;

import java.io.IOException;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man game app.
 * 
 * @author Armin Reichert
 */
public class PacManGameApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		PacManGameController controller = new PacManGameController(true);
		controller.setUI(new PacManGameFXUI(stage, controller.game, 2));
		controller.showUI();
		new Thread(controller::gameLoop, "PacManGame").start();
	}
}