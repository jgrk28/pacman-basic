package de.amr.games.pacman.ui.swing.rendering;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;

/**
 * Interface used to render game scenes.
 * 
 * @author Armin Reichert
 */
public interface GameRendering {

	void signalGameState(Graphics2D g, PacManGameModel game);

	void drawScore(Graphics2D g, PacManGameModel game, int x, int y);

	void drawHiScore(Graphics2D g, PacManGameModel game, int x, int y);

	void drawLivesCounter(Graphics2D g, PacManGameModel game, int x, int y);

	void drawLevelCounter(Graphics2D g, PacManGameModel game, int rightX, int y);

	void drawMaze(Graphics2D g, PacManGameModel game, int x, int y);

	void drawEmptyMaze(Graphics2D g, PacManGameModel game, int mazeNumber, int x, int y);

	void drawFullMaze(Graphics2D g, PacManGameModel game, int mazeNumber, int x, int y);

	void drawPac(Graphics2D g, Pac pac, PacManGameModel game);

	void drawGhost(Graphics2D g, Ghost ghost, PacManGameModel game);

	void drawBonus(Graphics2D g, Bonus bonus, PacManGameModel game);

	default Graphics2D smoothGC(Graphics2D g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return g2;
	}

	default void drawImage(Graphics2D g, BufferedImage image, float x, float y, boolean smooth) {
		if (smooth) {
			Graphics2D gc = smoothGC(g);
			gc.drawImage(image, (int) x, (int) y, null);
			gc.dispose();
		} else {
			g.drawImage(image, (int) x, (int) y, null);
		}
	}
}