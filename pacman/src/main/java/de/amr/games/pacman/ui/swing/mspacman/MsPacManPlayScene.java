package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.HTS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.ui.swing.PacManGameSwingUI.TEXTS;
import static de.amr.games.pacman.ui.swing.mspacman.MsPacManAssets.DIR_INDEX;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.games.pacman.game.core.PacManGame;
import de.amr.games.pacman.game.core.PacManGameState;
import de.amr.games.pacman.game.creatures.Creature;
import de.amr.games.pacman.game.creatures.Ghost;
import de.amr.games.pacman.game.creatures.GhostState;
import de.amr.games.pacman.game.creatures.Pac;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.swing.PacManGameSwingUI;
import de.amr.games.pacman.ui.swing.scene.PacManGamePlayScene;

/**
 * Scene where the game is played.
 * 
 * @author Armin Reichert
 */
public class MsPacManPlayScene extends PacManGamePlayScene {

	private final MsPacManAssets assets;

	public MsPacManPlayScene(PacManGame game, V2i size, MsPacManAssets assets) {
		super(game, size);
		this.assets = assets;
	}

	@Override
	public void draw(Graphics2D g, Graphics2D unscaledGC) {
		drawScore(g);
		drawLivesCounter(g);
		drawLevelCounter(g);
		drawMaze(g);
		drawSprite(g, sprite(game.pac), game.pac);
		for (Ghost ghost : game.ghosts) {
			drawSprite(g, sprite(ghost), ghost);
		}
		drawDebugInfo(g);
	}

	private void drawSprite(Graphics2D g, BufferedImage sprite, Creature guy) {
		if (guy.visible) {
			g.drawImage(sprite, (int) (guy.position.x) - HTS, (int) (guy.position.y) - HTS, null);
		}
	}

	private Color getScoreColor() {
		switch (game.level.mazeNumber) {
		case 1:
			return Color.PINK;
		case 2:
			return Color.CYAN;
		case 3:
			return Color.ORANGE;
		case 4:
			return Color.BLUE;
		case 5:
			return Color.PINK;
		case 6:
			return Color.ORANGE;
		default:
			return Color.WHITE;
		}
	}

	private void drawScore(Graphics2D g) {
		g.setFont(assets.scoreFont);
		g.translate(0, 2);
		g.setColor(Color.WHITE);
		g.drawString(TEXTS.getString("SCORE"), t(1), t(1));
		g.drawString(TEXTS.getString("HI_SCORE"), t(16), t(1));
		g.translate(0, 1);
		g.setColor(getScoreColor());
		g.drawString(String.format("%08d", game.score), t(1), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.levelNumber), t(9), t(2));
		g.setColor(getScoreColor());
		g.drawString(String.format("%08d", game.hiscore.points), t(16), t(2));
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(String.format("L%02d", game.hiscore.level), t(24), t(2));
		g.translate(0, -3);
	}

	private void drawLivesCounter(Graphics2D g) {
		int maxLives = 5;
		int displayedLives = game.started ? game.lives - 1 : game.lives;
		int y = size.y - t(2);
		for (int i = 0; i < Math.min(displayedLives, maxLives); ++i) {
			g.drawImage(assets.life, t(2 * (i + 1)), y, null);
		}
		if (game.lives > maxLives) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
			g.drawString("+" + (game.lives - maxLives), t(12) - 4, y + t(2));
		}
	}

	private void drawLevelCounter(Graphics2D g) {
		int x = t(game.world.sizeInTiles().x - 4);
		for (int levelNumber = 1; levelNumber <= Math.min(game.levelNumber, 7); ++levelNumber) {
			BufferedImage symbol = assets.symbols[game.levelSymbols.get(levelNumber - 1)];
			g.drawImage(symbol, x, size.y - t(2), null);
			x -= t(2);
		}
	}

	private void hideFood(Graphics2D g, int x, int y) {
		g.setColor(Color.BLACK);
		g.fillRect(t(x), t(y), TS, TS);
	}

	private void drawMaze(Graphics2D g) {
		if (game.mazeFlashesRemaining > 0) {
			game.clock.runAlternating(game.clock.sec(0.25), () -> {
				g.drawImage(assets.mazeEmptyDark[game.level.mazeNumber - 1], 0, t(3), null);
			}, () -> {
//				g.drawImage(assets.mazeEmptyBright[mazeIndex], 0, t(3), null);
			}, () -> {
				game.mazeFlashesRemaining--;
			});
			return;
		}
		g.drawImage(assets.mazeFull[game.level.mazeNumber - 1], 0, t(3), null);
		range(0, game.world.sizeInTiles().x).forEach(x -> {
			range(4, game.world.sizeInTiles().y - 3).forEach(y -> {
				if (game.world.isFoodRemoved(x, y)) {
					hideFood(g, x, y);
				} else if (game.state == PacManGameState.HUNTING && game.world.isEnergizerTile(x, y)) {
					game.clock.runOrBeIdle(10, () -> hideFood(g, x, y));
				}
			});
		});
		drawBonus(g);
		if (PacManGameSwingUI.debugMode) {
			drawMazeStructure(g);
		}
	}

	private static final int BONUS_JUMP[] = { -2, 0, 2 };

	private void drawBonus(Graphics2D g) {
		int x = (int) (game.bonus.position.x) - HTS;
		int y = (int) (game.bonus.position.y) - HTS;
		if (game.bonus.edibleTicksLeft > 0) {
			int frame = game.clock.frame(20, BONUS_JUMP.length);
			if (game.bonus.dir == Direction.LEFT || game.bonus.dir == Direction.RIGHT) {
				y += BONUS_JUMP[frame]; // TODO this is not yet correct
			}
			g.drawImage(assets.symbols[game.bonus.symbol], x, y, null);
		} else if (game.bonus.eatenTicksLeft > 0) {
			g.drawImage(assets.numbers.get(game.bonus.points), x, y, null);
		}
	}

	private BufferedImage sprite(Pac pac) {
		int dir = DIR_INDEX.get(pac.dir);
		if (pac.collapsingTicksLeft > 0) {
			// collapsing animation
			return assets.section(0, pac.collapsingTicksLeft > 1 ? game.clock.frame(10, 4) : dir);
		}
		if (pac.speed == 0) {
			// medium open mouth when in READY state, else full face
			return game.state == PacManGameState.READY ? assets.section(1, dir) : assets.section(2, dir);
		}
		if (!pac.couldMove) {
			// medium open mouth
			return assets.section(1, dir);
		}
		// mouth animation
		int frame = game.clock.frame(5, 3);
		return assets.section(frame, dir);
	}

	private BufferedImage sprite(Ghost ghost) {
		if (ghost.bounty > 0) {
			return assets.bountyNumbers.get(ghost.bounty);
		}
		int dir = DIR_INDEX.get(ghost.wishDir);
		int walking = ghost.speed == 0 ? 0 : game.clock.frame(5, 2);
		if (ghost.state == GhostState.DEAD || ghost.state == GhostState.ENTERING_HOUSE) {
			// eyes looking towards *intended* move direction
			return assets.section(8 + dir, 5);
		}
		if (ghost.state == GhostState.FRIGHTENED) {
			if (game.pac.powerTicksLeft <= 20 * game.level.numFlashes && game.state == PacManGameState.HUNTING) {
				// flashing blue/white, walking animation
				int flashing = game.clock.frame(10, 2) == 0 ? 8 : 10;
				return assets.section(walking + flashing, 4);
			}
			// blue, walking animation
			return assets.section(8 + walking, 4);
		}
		if (ghost.state == GhostState.LOCKED && game.pac.powerTicksLeft > 0) {
			// blue, walking animation
			return assets.section(8 + walking, 4);
		}
		// colored, walking animation, looking towards intended move direction
		return assets.section(2 * dir + walking, 4 + ghost.id);
	}

}