package de.amr.games.pacman.ui.swing.scenes.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.DeadGhostCountChangeEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.assets.SoundManager;
import de.amr.games.pacman.ui.swing.rendering.SwingPacManGameRendering2D;

/**
 * Play scene (Pac-Man and Ms. Pac-Man).
 * 
 * @author Armin Reichert
 */
public class PlayScene extends GameScene {

	private TimedSequence<?> mazeFlashing;

	public PlayScene(PacManGameController controller, Dimension size, SwingPacManGameRendering2D rendering, SoundManager sounds) {
		super(controller, size, rendering, sounds);
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		AbstractGameModel game = gameController.game();
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (newState == PacManGameState.READY) {
			rendering.resetAllAnimations(gameController.game());
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				gameController.stateTimer().resetSeconds(4.5);
				sounds.play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}

		// enter HUNTING
		if (newState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().restart();
			rendering.playerAnimations().playerMunching(game.player).forEach(TimedSequence::restart);
			game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::restart);
		}

		// exit HUNTING
		if (oldState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().reset();
		}

		// enter PACMAN_DYING
		if (newState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
			playAnimationPlayerDying(game);
		}

		// enter GHOST_DYING
		if (newState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
			rendering.mazeAnimations().energizerBlinking().restart();
		}

		// exit GHOST_DYING
		if (oldState == PacManGameState.GHOST_DYING) {
			// the dead ghost(s) will return home now
			if (game.ghosts(GhostState.DEAD).count() > 0) {
				sounds.loop(PacManGameSound.GHOST_RETURNING_HOME, Integer.MAX_VALUE);
			}
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			mazeFlashing = rendering.mazeAnimations().mazeFlashing(game.currentLevel.mazeNumber);
			sounds.stopAll();
		}

		// enter GAME_OVER
		if (newState == PacManGameState.GAME_OVER) {
			game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		}
	}

	@Override
	protected void onGameEvent(PacManGameEvent gameEvent) {
		sounds.setMuted(gameController.isAttractMode());

		if (gameEvent instanceof ScatterPhaseStartedEvent) {
			ScatterPhaseStartedEvent e = (ScatterPhaseStartedEvent) gameEvent;
			if (e.scatterPhase > 0) {
				sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
			}
			sounds.loop(PacManGameSound.SIRENS.get(e.scatterPhase), Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManLostPowerEvent) {
			sounds.stop(PacManGameSound.PACMAN_POWER);
		}

		else if (gameEvent instanceof PacManFoundFoodEvent) {
			sounds.play(PacManGameSound.PACMAN_MUNCH);
		}

		else if (gameEvent instanceof PacManGainsPowerEvent) {
			sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
			gameEvent.gameModel.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				rendering.ghostAnimations().ghostFlashing(ghost).reset();
				rendering.ghostAnimations().ghostFrightened(ghost).forEach(TimedSequence::restart);
			});
			gameEvent.gameModel.ghosts(GhostState.HUNTING_PAC).forEach(ghost -> {
				rendering.ghostAnimations().ghostFrightened(ghost).forEach(TimedSequence::restart);
			});
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			sounds.play(PacManGameSound.BONUS_EATEN);
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			sounds.play(PacManGameSound.EXTRA_LIFE);
			gameController.userInterface.showFlashMessage("Extra life!");
		}

		else if (gameEvent instanceof DeadGhostCountChangeEvent) {
			DeadGhostCountChangeEvent e = (DeadGhostCountChangeEvent) gameEvent;
			if (e.oldCount == 0 && e.newCount > 0) {
				sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
			} else if (e.oldCount > 0 && e.newCount == 0) {
				sounds.stop(PacManGameSound.GHOST_RETURNING_HOME);
			}
		}
	}

	private void playAnimationPlayerDying(AbstractGameModel game) {
		game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		rendering.playerAnimations().playerDying().delay(120).onStart(() -> {
			game.ghosts().forEach(ghost -> ghost.visible = false);
			if (gameController.isGameRunning()) {
				sounds.play(PacManGameSound.PACMAN_DEATH);
			}
		}).restart();
	}

	private void runLevelCompleteState(PacManGameState state) {
		AbstractGameModel game = gameController.game();
		if (gameController.stateTimer().isRunningSeconds(2)) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
		}
		if (gameController.stateTimer().isRunningSeconds(3)) {
			mazeFlashing.restart();
		}
		mazeFlashing.animate();
		if (mazeFlashing.isComplete()) {
			gameController.stateTimer().forceExpiration();
		}
	}

	@Override
	public void start() {
		super.start();
		AbstractGameModel game = gameController.game();
		mazeFlashing = rendering.mazeAnimations().mazeFlashing(game.currentLevel.mazeNumber)
				.repetitions(game.currentLevel.numFlashes);
		mazeFlashing.reset();
		game.player.powerTimer.addEventListener(e -> {
			if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
				game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
					TimedSequence<?> flashing = rendering.ghostAnimations().ghostFlashing(ghost);
					long frameTime = e.ticks / (game.currentLevel.numFlashes * flashing.numFrames());
					flashing.frameDuration(frameTime).repetitions(game.currentLevel.numFlashes).restart();
				});
			}
		});
	}

	@Override
	public void end() {
		super.end();
	}

	@Override
	public void update() {
		if (gameController.state == PacManGameState.LEVEL_COMPLETE) {
			runLevelCompleteState(gameController.state);
		} else if (gameController.state == PacManGameState.LEVEL_STARTING) {
			gameController.stateTimer().forceExpiration();
		}
	}

	@Override
	public void render(Graphics2D g) {
		AbstractGameModel game = gameController.game();
		rendering.drawMaze(g, game.currentLevel.mazeNumber, 0, t(3), mazeFlashing.isRunning());
		if (!mazeFlashing.isRunning()) {
			rendering.drawFoodTiles(g, game.currentLevel.world.tiles().filter(game.currentLevel.world::isFoodTile),
					game.currentLevel::containsEatenFood);
			rendering.drawEnergizerTiles(g, game.currentLevel.world.energizerTiles());
		}
		if (gameController.isAttractMode()) {
			rendering.drawGameState(g, game, PacManGameState.GAME_OVER);
		} else {
			rendering.drawGameState(g, game, gameController.state);
		}
		rendering.drawBonus(g, game.bonus);
		rendering.drawPlayer(g, game.player);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game.player.powerTimer.isRunning()));
		if (gameController.isGameRunning()) {
			rendering.drawScore(g, game, false);
			rendering.drawLivesCounter(g, game, t(2), t(34));
		} else {
			rendering.drawScore(g, game, true);
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}
}