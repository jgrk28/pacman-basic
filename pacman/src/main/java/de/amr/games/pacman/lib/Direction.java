package de.amr.games.pacman.lib;

/**
 * The possible move directions inside the world.
 * 
 * @author Armin Reichert
 *
 */
public enum Direction {

	LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1);

	public final V2i vec;

	public Direction opposite() {
		switch (this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default:
			throw new IllegalStateException();
		}
	}

	private Direction(int x, int y) {
		vec = new V2i(x, y);
	}
}