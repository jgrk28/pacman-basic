package de.amr.games.pacman.ui.swing.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.swing.GameScene;
import de.amr.games.pacman.ui.swing.rendering.DebugRendering;
import de.amr.games.pacman.ui.swing.rendering.GameRendering;

/**
 * Play scene for Pac-Man and Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PlayScene implements GameScene {

	private final Dimension size;
	private final GameRendering rendering;
	private final PacManGameModel game;

	public PlayScene(Dimension size, GameRendering rendering, PacManGameModel game) {
		this.size = size;
		this.rendering = rendering;
		this.game = game;
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	@Override
	public void update() {
	}

	@Override
	public void render(Graphics2D g) {
		rendering.drawScore(g, game, t(1), t(0));
		rendering.drawHiScore(g, game, t(15), t(0));
		rendering.drawMaze(g, game, 0, t(3));
		if (DebugRendering.on) {
			DebugRendering.drawMazeStructure(g, game);
		}
		rendering.signalGameState(g, game);
		rendering.drawPac(g, game.pac, game);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game));
		if (DebugRendering.on) {
			DebugRendering.drawPlaySceneDebugInfo(g, game);
		}
		if (!game.attractMode) {
			rendering.drawLivesCounter(g, game, t(2), size.height - t(2));
		}
		rendering.drawLevelCounter(g, game, t(game.level.world.xTiles() - 4), size.height - t(2));
	}
}