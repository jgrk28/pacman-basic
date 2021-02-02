package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.stream.IntStream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.ui.api.PacManGameAnimations;
import de.amr.games.pacman.ui.api.PacManGameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Intro presenting the ghosts and showing the chasing animations.
 * 
 * @author Armin Reichert
 */
public class PacManClassicIntroScene implements PacManGameScene {

	private static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	private final V2i size;
	private final PacManClassicRendering rendering;
	private final PacManGame game;
	private final SoundManager sounds;

	private int lastKilledGhostID;

	public PacManClassicIntroScene(V2i size, PacManClassicRendering rendering, SoundManager sounds, PacManGame game) {
		this.size = size;
		this.game = game;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.of(rendering);
	}

	@Override
	public V2i size() {
		return size;
	}

	@Override
	public void start() {
		game.pac.position = new V2f(size.x, t(22));
		game.pac.dir = LEFT;
		game.pac.speed = 0.8f;
		for (Ghost ghost : game.ghosts) {
			ghost.position = new V2f(game.pac.position.x + 24 + 16 * ghost.id, t(22));
			ghost.wishDir = ghost.dir = LEFT;
			ghost.speed = 0.8f;
			ghost.visible = true;
		}
		lastKilledGhostID = -1;
	}

	@Override
	public void end() {
		for (Ghost ghost : game.ghosts) {
			for (Direction dir : Direction.values()) {
				rendering.ghostWalking(ghost, dir).restart();
			}
		}
		for (Direction dir : Direction.values()) {
			rendering.assets.pacMunching.get(dir).restart();
		}
		game.state.resetTimer();
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		game.state.runAfter(clock.sec(1), () -> {
			drawHCenteredImage(g, rendering.assets.gameLogo, 3);
		});

		game.state.runAfter(clock.sec(2), () -> {
			g.setColor(Color.WHITE);
			g.setFont(rendering.assets.scoreFont);
			drawHCenteredText(g, rendering.translator.apply("CHARACTER_NICKNAME"), t(8));
		});

		IntStream.rangeClosed(0, 3).forEach(ghost -> {
			int ghostStart = 3 + 2 * ghost;
			int y = t(10 + 3 * ghost);
			game.state.runAt(clock.sec(ghostStart), () -> {
				sounds.playSound(PacManGameSound.CREDIT);
			});
			game.state.runAfter(clock.sec(ghostStart), () -> {
				g.drawImage(rendering.assets.ghostWalking.get(ghost).get(RIGHT).thing(0), t(2) - 3, y - 2, null);
			});
			game.state.runAfter(clock.sec(ghostStart + 0.5), () -> {
				drawGhostCharacterAndName(g, ghost, y, false);
			});
			game.state.runAfter(clock.sec(ghostStart + 1), () -> {
				drawGhostCharacterAndName(g, ghost, y, true);
			});
		});

		game.state.runAfter(clock.sec(12), () -> {
			drawPointsAnimation(g);
		});

		game.state.runAt(clock.sec(13), () -> {
			sounds.loopSound(PacManGameSound.GHOST_SIREN_1);
		});

		game.state.runAfter(clock.sec(13), () -> {
			if (game.pac.dir == LEFT) {
				drawGhostsChasingPacMan(g);
			} else {
				drawPacManChasingGhosts(g);
			}
		});

		game.state.runAt(clock.sec(24), () -> {
			sounds.stopSound(PacManGameSound.PACMAN_POWER);
		});

		game.state.runAfter(clock.sec(24), () -> {
			drawPressKeyToStart(g);
		});

		game.state.runAt(clock.sec(30), () -> {
			end();
			start();
		});
	}

	private void drawPressKeyToStart(Graphics2D g) {
		g.setColor(Color.ORANGE);
		g.setFont(rendering.assets.scoreFont);
		if (game.state.ticksRun() % 40 < 20) {
			drawHCenteredText(g, rendering.translator.apply("PRESS_KEY_TO_PLAY"), size.y - 20);
		}
	}

	private void drawPointsAnimation(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect(t(9) + 6, t(27) + 2, 2, 2);
		if (game.state.ticksRun() % 40 < 20) {
			g.fillOval(t(9), t(29) - 2, 10, 10);
		}
		g.setColor(Color.WHITE);
		g.setFont(rendering.assets.scoreFont);
		g.drawString("10", t(12), t(28));
		g.drawString("50", t(12), t(30));
		g.setFont(rendering.assets.scoreFont.deriveFont(6f));
		g.drawString(rendering.translator.apply("POINTS"), t(15), t(28));
		g.drawString(rendering.translator.apply("POINTS"), t(15), t(30));
	}

	private void drawGhostCharacterAndName(Graphics2D g, int ghostID, int y, boolean both) {
		String character = rendering.translator.apply("CLASSIC.GHOST." + ghostID + ".CHARACTER");
		String nickname = "\"" + rendering.translator.apply("CLASSIC.GHOST." + ghostID + ".NICKNAME") + "\"";
		Color color = GHOST_COLORS[ghostID];
		g.setColor(color);
		g.setFont(rendering.assets.scoreFont);
		g.drawString("-" + character, t(4), y + 11);
		if (both) {
			g.drawString(nickname, t(15), y + 11);
		}
	}

	private void drawGhostsChasingPacMan(Graphics2D g) {
		if (game.state.ticksRun() % 40 < 20) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), t(22) + 2, 10, 10);
		}
		g.drawImage(pacSprite(), (int) game.pac.position.x, (int) game.pac.position.y, null);
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			g.drawImage(rendering.assets.ghostWalking.get(ghostID).get(LEFT).currentFrameThenAdvance(),
					(int) game.ghosts[0].position.x + 16 * ghostID, (int) game.pac.position.y, null);
		}
		if (game.pac.position.x > t(2)) {
			V2f velocity = new V2f(game.pac.dir.vec).scaled(game.pac.speed);
			game.pac.position = game.pac.position.sum(velocity);
			V2f ghostVelocity = new V2f(game.ghosts[0].dir.vec).scaled(game.ghosts[0].speed);
			game.ghosts[0].position = game.ghosts[0].position.sum(ghostVelocity);
		} else {
			game.pac.dir = RIGHT;
			for (int i = 0; i < 4; ++i) {
				game.ghosts[i].dir = RIGHT;
				game.ghosts[i].wishDir = RIGHT;
				game.ghosts[i].speed = 0.4f;
			}
			sounds.stopSound(PacManGameSound.GHOST_SIREN_1);
			sounds.loopSound(PacManGameSound.PACMAN_POWER);
		}
	}

	private void drawPacManChasingGhosts(Graphics2D g) {
		int y = t(22);
		for (int ghostID = 0; ghostID < 4; ++ghostID) {
			int x = (int) game.ghosts[0].position.x + 16 * ghostID;
			if (game.pac.position.x < x) {
				g.drawImage(rendering.assets.ghostBlue.currentFrameThenAdvance(), x, y, null);
			} else if (game.pac.position.x > x && game.pac.position.x <= x + 16) {
				int bounty = (int) (Math.pow(2, ghostID) * 200);
				g.drawImage(rendering.assets.numbers.get(bounty), x, y, null);
				if (lastKilledGhostID != ghostID) {
					lastKilledGhostID++;
					sounds.playSound(PacManGameSound.GHOST_EATEN);
				}
			}
		}
		g.drawImage(pacSprite(), (int) game.pac.position.x, (int) game.pac.position.y, null);
		if (game.pac.position.x < size.x) {
			V2f velocity = new V2f(game.pac.dir.vec).scaled(game.pac.speed);
			game.pac.position = game.pac.position.sum(velocity);
			V2f ghostVelocity = new V2f(game.ghosts[0].dir.vec).scaled(game.ghosts[0].speed);
			game.ghosts[0].position = game.ghosts[0].position.sum(ghostVelocity);
		}
	}

	private BufferedImage pacSprite() {
		return game.pac.speed != 0 ? rendering.assets.pacMunching.get(game.pac.dir).currentFrameThenAdvance()
				: rendering.assets.pacMouthOpen.get(game.pac.dir);
	}

}