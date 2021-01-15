package de.amr.games.pacman.core;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.creatures.Ghost;
import de.amr.games.pacman.creatures.Ghost.GhostState;
import de.amr.games.pacman.creatures.Pac;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

/**
 * Controls Pac-Man movement.
 * 
 * @author Armin Reichert
 */
public class Autopilot {

	private static final int MAX_GHOST_AHEAD_DETECTION_DIST = 4; // tiles
	private static final int MAX_GHOST_BEHIND_DETECTION_DIST = 2; // tiles
	private static final int MAX_GHOST_CHASE_DIST = 10; // tiles
	private static final int MAX_BONUS_HARVEST_DIST = 20; // tiles

	private PacManGame game;
	private Pac pacMan;
	private Ghost[] ghosts;

	public void controlPac(PacManGame game) {
		this.game = game;
		this.pacMan = game.pac;
		this.ghosts = game.ghosts;
		V2i pacManTile = pacMan.tile();

		if (pacMan.couldMove && !pacMan.changedTile) {
			return;
		}

		if (pacMan.forcedDirection) {
			pacMan.forcedDirection = false;
			return;
		}

		pacMan.targetTile = null;

		Ghost hunterAhead = findHuntingGhostAhead(); // Where is Hunter?
		if (hunterAhead != null) {
			Direction escapeDir = null;
			Ghost hunterBehind = findHuntingGhostBehind();
			if (hunterBehind != null) {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(pacMan.dir, pacMan.dir.opposite()));
				log("Detected ghost %s behind, escape direction is %s", hunterAhead.name, escapeDir);
			} else {
				escapeDir = findEscapeDirectionExcluding(EnumSet.of(pacMan.dir));
				log("Detected ghost %s ahead, escape direction is %s", hunterAhead.name, escapeDir);
			}
			if (escapeDir != null) {
				pacMan.wishDir = escapeDir;
			}
			pacMan.forcedDirection = true;
			return;
		}

		// when not escaping ghost, keep move direction at least until next intersection
		if (pacMan.couldMove && !game.world.isIntersection(pacManTile))
			return;

		Ghost prey = findFrightenedGhostInReach();
		if (prey != null && pacMan.powerTicksLeft >= game.clock.sec(1)) {
			log("Detected frightened ghost %s %.0g tiles away", prey.name, prey.tile().manhattanDistance(pacManTile));
			pacMan.targetTile = prey.tile();
		} else if (game.bonus.availableTicks > 0
				&& game.bonus.tile().manhattanDistance(pacManTile) <= MAX_BONUS_HARVEST_DIST) {
			log("Detected active bonus");
			pacMan.targetTile = game.bonus.tile();
		} else {
			V2i foodTile = findTileFarestFromGhosts(findNearestFoodTiles());
			pacMan.targetTile = foodTile;
		}
		approachTarget();
	}

	private void approachTarget() {
		if (pacMan.targetTile == null) {
			return;
		}
		double minDist = Double.MAX_VALUE;
		Direction minDistDir = null;
		for (Direction dir : Direction.shuffled()) {
			if (dir == pacMan.dir.opposite()) {
				continue;
				/*
				 * TODO sometimes reversing direction can be useful but in most cases, it leads to bouncing.
				 */
			}
			V2i neighbor = pacMan.tile().sum(dir.vec);
			if (!game.canAccessTile(pacMan, neighbor.x, neighbor.y)) {
				continue;
			}
			double dist = neighbor.euclideanDistance(pacMan.targetTile);
			if (dist < minDist) {
				minDist = dist;
				minDistDir = dir;
			}
		}
		if (minDistDir != null) {
			pacMan.wishDir = minDistDir;
			log("Approach target tile %s from tile %s by turning %s", pacMan.tile(), pacMan.targetTile, pacMan.wishDir);
		}
	}

	private Ghost findFrightenedGhostInReach() {
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.FRIGHTENED
					&& ghost.tile().manhattanDistance(pacMan.tile()) < MAX_GHOST_CHASE_DIST) {
				return ghost;
			}
		}
		return null;
	}

	private Ghost findHuntingGhostAhead() {
		V2i pacManTile = pacMan.tile();
		boolean energizerFound = false;
		for (int i = 1; i <= MAX_GHOST_AHEAD_DETECTION_DIST; ++i) {
			V2i ahead = pacManTile.sum(pacMan.dir.vec.scaled(i));
			if (!game.canAccessTile(pacMan, ahead.x, ahead.y)) {
				break;
			}
			if (game.world.isEnergizerTile(ahead) && !game.world.isFoodRemoved(ahead)) {
				energizerFound = true;
			}
			V2i aheadLeft = ahead.sum(pacMan.dir.turnLeft().vec), aheadRight = ahead.sum(pacMan.dir.turnRight().vec);
			for (Ghost ghost : ghosts) {
				if (!game.isGhostHunting(ghost)) {
					continue;
				}
				if (ghost.tile().equals(ahead) || ghost.tile().equals(aheadLeft) || ghost.tile().equals(aheadRight)) {
					if (energizerFound) {
						log("Ignore hunting ghost ahead, energizer comes first!");
						return null;
					}
					return ghost;
				}
			}
		}
		return null;
	}

	private Ghost findHuntingGhostBehind() {
		V2i pacManTile = pacMan.tile();
		for (int i = 1; i <= MAX_GHOST_BEHIND_DETECTION_DIST; ++i) {
			V2i behind = pacManTile.sum(pacMan.dir.opposite().vec.scaled(i));
			if (!game.canAccessTile(pacMan, behind.x, behind.y)) {
				break;
			}
			for (Ghost ghost : ghosts) {
				if (!game.isGhostHunting(ghost)) {
					continue;
				}
				if (ghost.tile().equals(behind)) {
					return ghost;
				}
			}
		}
		return null;
	}

	private Direction findEscapeDirectionExcluding(Collection<Direction> forbidden) {
		V2i pacManTile = pacMan.tile();
		List<Direction> escapes = new ArrayList<>(4);
		for (Direction dir : Direction.shuffled()) {
			if (forbidden.contains(dir)) {
				continue;
			}
			V2i neighbor = pacManTile.sum(dir.vec);
			if (game.canAccessTile(pacMan, neighbor.x, neighbor.y)) {
				escapes.add(dir);
			}
		}
		for (Direction escape : escapes) {
			V2i escapeTile = pacManTile.sum(escape.vec);
			if (game.world.isTunnel(escapeTile)) {
				return escape;
			}
		}
		return escapes.isEmpty() ? null : escapes.get(0);
	}

	private List<V2i> findNearestFoodTiles() {
		long time = System.nanoTime();
		List<V2i> foodTiles = new ArrayList<>();
		V2i pacManTile = pacMan.tile();
		double minDist = Double.MAX_VALUE;
		for (int x = 0; x < game.world.sizeInTiles().x; ++x) {
			for (int y = 0; y < game.world.sizeInTiles().y; ++y) {
				if (!game.world.isFoodTile(x, y) || game.world.isFoodRemoved(x, y)) {
					continue;
				}
				V2i foodTile = new V2i(x, y);
				if (game.world.isEnergizerTile(foodTile) && pacMan.powerTicksLeft > game.clock.sec(1)
						&& game.world.foodRemaining() > 1) {
					continue;
				}
				double dist = pacManTile.manhattanDistance(foodTile);
				if (dist < minDist) {
					minDist = dist;
					foodTiles.clear();
					foodTiles.add(foodTile);
				} else if (dist == minDist) {
					foodTiles.add(foodTile);
				}
			}
		}
		time = System.nanoTime() - time;
		log("Nearest food tiles from Pac-Man location %s: (time %.2f millis)", pacManTile, time / 1_000_000f);
		for (V2i t : foodTiles) {
			log("\t%s (%.2g tiles away from Pac-Man, %.2g tiles away from ghosts)", t, t.manhattanDistance(pacManTile),
					minDistanceFromGhosts(t));
		}
		return foodTiles;
	}

	private V2i findTileFarestFromGhosts(List<V2i> tiles) {
		V2i farestTile = null;
		double maxDist = -1;
		for (V2i tile : tiles) {
			double dist = minDistanceFromGhosts(tile);
			if (dist > maxDist) {
				maxDist = dist;
				farestTile = tile;
			}
		}
		return farestTile;
	}

	private double minDistanceFromGhosts(V2i tile) {
		return Stream.of(ghosts).map(Ghost::tile).mapToDouble(tile::manhattanDistance).min().getAsDouble();
	}
}