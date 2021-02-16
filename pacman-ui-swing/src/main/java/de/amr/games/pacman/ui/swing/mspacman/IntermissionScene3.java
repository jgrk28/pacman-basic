package de.amr.games.pacman.ui.swing.mspacman;

import static de.amr.games.pacman.ui.swing.mspacman.MsPacManGameScenes.soundManager;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.ui.swing.GameScene;

public class IntermissionScene3 implements GameScene {

	private final Dimension size;
	private final PacManGameModel game;

	public IntermissionScene3(Dimension size, PacManGameModel game) {
		this.size = size;
		this.game = game;
	}

	@Override
	public Dimension sizeInPixel() {
		return size;
	}

	@Override
	public void start() {
		soundManager.loop(PacManGameSound.INTERMISSION_3, 1);
	}

	@Override
	public void update() {
		if (game.state.ticksRun() == God.clock.sec(6)) {
			game.state.duration(0);
		}
	}

	@Override
	public void render(Graphics2D g) {
	}
}