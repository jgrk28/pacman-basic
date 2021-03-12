package de.amr.games.pacman.ui;

import java.util.Optional;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Interface through which the game controller accesses the views. This interface must be
 * implemented, the animation and sound interfaces are optional.
 * 
 * @author Armin Reichert
 */
public interface PacManGameUI {

	void onGameChanged(GameModel game);

	void reset();

	void update();

	void showFlashMessage(String message, double seconds);

	default void showFlashMessage(String message) {
		showFlashMessage(message, 1);
	}

	boolean keyPressed(String keySpec);

	void mute(boolean muted);

	Optional<SoundManager> sound();

	Optional<PacManGameAnimations> animation();
}