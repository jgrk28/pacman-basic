package de.amr.games.pacman.ui.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his
 * dress apart.
 * 
 * @author Armin Reichert
 */
public abstract class PacMan_IntermissionScene2_Controller {

	public enum Phase {

		WALKING, GETTING_STUCK, STUCK;
	}

	public static final int groundTileY = 20;

	public final TickTimer timer = new TickTimer();
	public final PacManGameController gameController;

	public Ghost blinky;
	public Pac pac;
	public GameEntity nail;
	public Phase phase;

	public PacMan_IntermissionScene2_Controller(PacManGameController gameController) {
		this.gameController = gameController;
	}

	public abstract void playIntermissionSound();

	public void init() {
		pac = new Pac("Pac-Man", Direction.LEFT);
		pac.setTilePosition(30, groundTileY);
		pac.visible = true;
		pac.speed = 1;

		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		blinky.setPositionRelativeTo(pac, t(14), 0);
		blinky.visible = true;
		blinky.state = GhostState.HUNTING_PAC;
		blinky.speed = 1;

		nail = new GameEntity();
		nail.visible = true;
		nail.setPosition(t(14), t(groundTileY) - 1);

		playIntermissionSound();

		enter(Phase.WALKING);
	}

	public void enter(Phase nextPhase) {
		phase = nextPhase;
		timer.reset();
		timer.start();
	}

	public int nailDistance() {
		return (int) (nail.position.x - blinky.position.x);
	}

	public void update() {
		switch (phase) {
		case WALKING:
			if (nailDistance() == 0) {
				enter(Phase.GETTING_STUCK);
			}
			timer.tick();
			break;
		case GETTING_STUCK:
			int stretching = nailDistance() / 4;
			blinky.speed = 0.3f - 0.1f * stretching;
			if (stretching == 3) {
				blinky.speed = 0;
				blinky.dir = Direction.UP;
				enter(Phase.STUCK);
			}
			timer.tick();
			break;
		case STUCK:
			if (timer.isRunningSeconds(3)) {
				blinky.dir = Direction.RIGHT;
			}
			if (timer.isRunningSeconds(6)) {
				gameController.stateTimer().forceExpiration();
				return;
			}
			timer.tick();
			break;
		default:
			throw new IllegalStateException("Illegal phase: " + phase);
		}
		blinky.move();
		pac.move();
	}
}