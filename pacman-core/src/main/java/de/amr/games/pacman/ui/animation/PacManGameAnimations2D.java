package de.amr.games.pacman.ui.animation;

import de.amr.games.pacman.model.common.GameModel;

/**
 * Animations for a game.
 * 
 * @author Armin Reichert
 */
public interface PacManGameAnimations2D {

	default void resetAllAnimations(GameModel game) {
		mazeAnimations().reset();
		game.ghosts().forEach(ghostAnimations()::reset);
		playerAnimations().reset(game.player);
	}

	PlayerAnimations2D playerAnimations();

	GhostAnimations2D ghostAnimations();

	MazeAnimations2D mazeAnimations();

	TimedSequence<?> storkFlying();

	TimedSequence<?> flapFlapping();
}