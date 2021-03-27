package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;

public class PacManFoundFoodEvent extends PacManGameEvent {

	public PacManFoundFoodEvent(GameVariant gameVariant, GameModel gameModel) {
		super(gameVariant, gameModel);
	}

}