package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.world.PacManGameWorld;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

public class OccupancyHuntingStrategy extends HuntingStrategy {
  private PacManGameWorld gameWorld;
  HashMap<V2i, Double> occupancy;

  OccupancyHuntingStrategy(AbstractGameModel gameModel) {
    super(gameModel);
    this.occupancy = new HashMap<>();
  }

  //Set occupancy to know where PacMan is at the start
  private void initOccupancy() {
    gameWorld.tiles().filter(
        tile -> !gameWorld.isWall(tile) && !gameWorld.isGhostHouseDoor(tile)
    ).forEach(tile -> occupancy.put(tile, 0.0));
    V2i pacTile = gameModel.player.tile();
    occupancy.replace(pacTile, 1.0);
  }

  //Distribute occupancy equally
  private void distributeOccupancy() {
    long numTiles = gameWorld.tiles().filter(
        tile -> !gameWorld.isWall(tile) && !gameWorld.isGhostHouseDoor(tile)
    ).count();
    Double startingProb = 1.0 / numTiles;
    gameWorld.tiles().filter(
        tile -> !gameWorld.isWall(tile) && !gameWorld.isGhostHouseDoor(tile)
    ).forEach(tile -> occupancy.put(tile, startingProb));
  }

  @Override
  V2i ghostHuntingTarget(int ghostID) {
    //If the occupancy map has not been made or has been cleared re-initialize
    if (occupancy.size() == 0) {
      this.gameWorld = gameModel.currentLevel.getWorld();
      initOccupancy();
    }

    //Get all tiles seen by the ghosts
    List<V2i> seenTiles = new ArrayList<>();
    V2i pacTile = gameModel.player.tile();
    for (Ghost ghost : gameModel.ghosts) {
      V2i ghostTile = ghost.tile();
      for (int i = 0; i <= 8; i++) {
        V2i aheadGhost = ghostTile.plus(gameModel.ghosts[ghostID].dir.vec.scaled(i));
        //If this tile is a wall seen tiles does not continue
        if (gameWorld.isWall(aheadGhost)) {
          break;
        }
        seenTiles.add(aheadGhost);
      }
    }

    //If a ghost can see PacMan set that position to a 1
    if (seenTiles.contains(pacTile)) {
      for (V2i tile : occupancy.keySet()) {
        if (tile.equals(pacTile)) {
          occupancy.replace(tile, 1.0);
        } else {
          occupancy.replace(tile, 0.0);
        }
      }
    } else {
      //Else disperse current probabilities based on seen tiles
      recalculateOccupancy(seenTiles);
    }

    //Find the most likely tile for PacMan to be on
    Optional<Entry<V2i, Double>> maxEntry = occupancy.entrySet().stream()
        .max((Map.Entry<V2i, Double> e1, Map.Entry<V2i, Double> e2) ->
            e1.getValue().compareTo(e2.getValue())
        );
    V2i target = maxEntry.get().getKey();
    Double value = maxEntry.get().getValue();
    return target;
  }

  private void recalculateOccupancy(List<V2i> seenTiles) {
    Double amountWiped = 0.0;

    //Remove occupancy from seen tiles
    for (V2i seenTile : seenTiles) {
      if (!gameWorld.insideMap(seenTile) || gameWorld.isWall(seenTile) || gameWorld.isGhostHouseDoor(seenTile)) {
        continue;
      }
      Double occupancyValue = occupancy.get(seenTile);
      long numValidNeighbors = gameWorld.neighborTiles(seenTile).filter(
          neighbor -> gameWorld.insideMap(neighbor) && !gameWorld.isWall(neighbor) && !gameWorld.isGhostHouseDoor(neighbor) && !seenTiles.contains(neighbor)
      ).count();
      //If there is a non seen neighbor transfer the probability there
      if (numValidNeighbors > 0) {
        double amountDistributed = occupancyValue / numValidNeighbors;
        Stream<V2i> validNeighbors = gameWorld.neighborTiles(seenTile).filter(neighbor ->
            gameWorld.insideMap(neighbor) && !gameWorld.isWall(neighbor) && !gameWorld.isGhostHouseDoor(neighbor) && !seenTiles.contains(neighbor));
        validNeighbors.forEach(neighbor -> modifyTileOccupancy(neighbor, amountDistributed));
      } else {
        //Else we have to wipe the probability and then disperse it later
        amountWiped += occupancyValue;
      }
      occupancy.replace(seenTile, 0.0);
    }

    //Disperse the current probabilities to their neighbors
    for (Map.Entry<V2i, Double> tileOccupancy : occupancy.entrySet()) {
      V2i tile = tileOccupancy.getKey();
      Double occupancyValue = tileOccupancy.getValue();
      if (seenTiles.contains(tile)) {
        continue;
      }
      Double dispersion = occupancyValue * .1;
      modifyTileOccupancy(tile, dispersion * -1);
      Stream<V2i> validNeighbors = gameWorld.neighborTiles(tile).filter(neighbor ->
          gameWorld.insideMap(neighbor) && !gameWorld.isWall(neighbor) && !gameWorld.isGhostHouseDoor(neighbor) && !seenTiles.contains(neighbor));
      Double amountReceived = dispersion / validNeighbors.count();

      //Reload stream after terminating with count
      validNeighbors = gameWorld.neighborTiles(tile).filter(neighbor ->
          gameWorld.insideMap(neighbor) && !gameWorld.isWall(neighbor) && !gameWorld.isGhostHouseDoor(neighbor) && !seenTiles.contains(neighbor));
      validNeighbors.forEach(neighbor -> modifyTileOccupancy(neighbor, amountReceived));
    }

    //Disperse all the wiped probabilities to everywhere with a non zero probability
    long numNonZero = occupancy.values().stream().filter(value -> !value.equals(0.0)).count();
    if (numNonZero == 0) {
      distributeOccupancy();
    } else {
      Double distributeWiped = amountWiped / numNonZero;
      for (Map.Entry<V2i, Double> tileOccupancy : occupancy.entrySet()) {
        V2i tile = tileOccupancy.getKey();
        Double occupancyValue = tileOccupancy.getValue();
        if (!occupancyValue.equals(0.0)) {
          modifyTileOccupancy(tile, distributeWiped);
        }
      }
    }
  }

  private void modifyTileOccupancy(V2i tile, double amount) {
    double originalOccupancy = occupancy.get(tile);
    double newOccupancy = originalOccupancy + amount;
    occupancy.replace(tile, newOccupancy);
  }
}
