package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;

public class OccupancyHuntingStrategy extends HuntingStrategy {

  OccupancyHuntingStrategy(AbstractGameModel gameModel) {
    super(gameModel);
  }

  @Override
  V2i ghostHuntingTarget(int ghostID) {
    return null;
  }
}
