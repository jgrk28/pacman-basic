package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.world.PacManGameWorld;
import java.util.HashMap;

public class OccupancyHuntingStrategy extends HuntingStrategy {
  private PacManGameWorld gameWorld;
  HashMap<V2i, Double> occupancy;

  OccupancyHuntingStrategy(AbstractGameModel gameModel) {
    super(gameModel);
    this.gameWorld = gameModel.currentLevel.getWorld();
    this.occupancy = new HashMap<>();
    initOccupancy();
  }

  private void initOccupancy() {
    long numTiles = gameWorld.tiles().count();
    Double startingProb = 1.0 / numTiles;
    gameWorld.tiles().forEach(tile -> occupancy.put(tile, startingProb));
  }

  @Override
  V2i ghostHuntingTarget(int ghostID) {
    return null;
  }
}
