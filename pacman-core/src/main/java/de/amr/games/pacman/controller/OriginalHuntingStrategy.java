package de.amr.games.pacman.controller;

import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.CLYDE;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;

public class OriginalHuntingStrategy extends HuntingStrategy {

  OriginalHuntingStrategy(AbstractGameModel gameModel) {
    super(gameModel);
  }

  @Override
  public V2i ghostHuntingTarget(int ghostID) {
    V2i playerTile = gameModel.player.tile();
    switch (ghostID) {

      case BLINKY:
        return playerTile;

      case PINKY: {
        V2i target = playerTile.plus(gameModel.player.dir.vec.scaled(4));
        if (gameModel.player.dir == Direction.UP) {
          // simulate overflow bug
          target = target.plus(-4, 0);
        }
        return target;
      }

      case INKY: {
        V2i twoAheadPlayer = playerTile.plus(gameModel.player.dir.vec.scaled(2));
        if (gameModel.player.dir == Direction.UP) {
          // simulate overflow bug
          twoAheadPlayer = twoAheadPlayer.plus(-2, 0);
        }
        return twoAheadPlayer.scaled(2).minus(gameModel.ghosts[BLINKY].tile());
      }

      case CLYDE: /* A Boy Named Sue */
        return gameModel.ghosts[CLYDE].tile().euclideanDistance(playerTile) < 8
            ? gameModel.currentLevel.world.ghostScatterTile(CLYDE)
            : playerTile;

      default:
        throw new IllegalArgumentException("Unknown ghost, id: " + ghostID);
    }
  }
}
