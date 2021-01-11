package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.worlds.PacManGameWorld.t;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.core.PacManGame;
import de.amr.games.pacman.creatures.Ghost;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;

public abstract class PacManGameScene {

	public static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	public final PacManGame game;
	public final V2i size;

	public PacManGameScene(PacManGame game, V2i size) {
		this.game = game;
		this.size = size;
	}

	public abstract void start();

	public abstract void end();

	public abstract void draw(Graphics2D g);

	public void drawCenteredText(Graphics2D g, String text, int y) {
		g.drawString(text, (size.x - g.getFontMetrics().stringWidth(text)) / 2, y);
	}

	public void drawCenteredImage(Graphics2D g, BufferedImage image, int y) {
		g.drawImage(image, (size.x - image.getWidth()) / 2, y, null);
	}

	// debug drawing stuff

	private static final Polygon TRIANGLE = new Polygon(new int[] { -4, 4, 0 }, new int[] { 0, 0, 4 }, 3);

	public void drawDebugInfo(Graphics2D g) {
		if (game.ui.isDebugMode()) {
			long remaining = game.state.ticksRemaining();
			String ticksText = remaining == Long.MAX_VALUE ? "forever" : remaining + " ticks remaining";
			String stateText = String.format("%s (%s)", game.stateDescription(), ticksText);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.PLAIN, 6));
			g.drawString(stateText, t(1), t(3));
			for (Ghost ghost : game.ghosts) {
				g.setColor(Color.WHITE);
				g.drawRect(round(ghost.position.x), round(ghost.position.y), TS, TS);
				if (ghost.targetTile != null) {
					Color c = GHOST_COLORS[ghost.id];
					g.setColor(c);
					g.fillRect(t(ghost.targetTile.x) + HTS / 2, t(ghost.targetTile.y) + HTS / 2, HTS, HTS);
				}
			}
			if (game.pac.targetTile != null) {
				g.setColor(new Color(255, 255, 0, 200));
				g.fillRect(t(game.pac.targetTile.x), t(game.pac.targetTile.y), TS, TS);
			}
		}
	}

	public void drawMazeStructure(Graphics2D g) {
		Color dark = new Color(80, 80, 80, 200);
		Stroke thin = new BasicStroke(0.1f);
		g.setColor(dark);
		g.setStroke(thin);
		for (int x = 0; x < game.world.sizeInTiles().x; ++x) {
			for (int y = 0; y < game.world.sizeInTiles().y; ++y) {
				if (game.world.isIntersection(x, y)) {
					for (Direction dir : Direction.values()) {
						int nx = x + dir.vec.x, ny = y + dir.vec.y;
						if (game.world.isWall(nx, ny)) {
							continue;
						}
						g.drawLine(t(x) + HTS, t(y) + HTS, t(nx) + HTS, t(ny) + HTS);
					}
				} else if (game.world.isUpwardsBlocked(x, y)) {
					g.translate(t(x) + HTS, t(y));
					g.fillPolygon(TRIANGLE);
					g.translate(-t(x) - HTS, -t(y));
				}
			}
		}
	}
}