package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;

public abstract class HuntingStrategy {
  protected AbstractGameModel gameModel;

  HuntingStrategy(AbstractGameModel gameModel) {
    this.gameModel = gameModel;
  }

  abstract V2i ghostHuntingTarget(int ghostID);
}
