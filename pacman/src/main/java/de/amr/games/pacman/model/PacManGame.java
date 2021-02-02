package de.amr.games.pacman.model;

import static de.amr.games.pacman.lib.Logging.log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Hiscore;
import de.amr.games.pacman.model.creatures.Bonus;
import de.amr.games.pacman.model.creatures.Ghost;
import de.amr.games.pacman.model.creatures.Pac;

/**
 * Common base class of the game models.
 * 
 * @author Armin Reichert
 */
public abstract class PacManGame {

	public static final byte BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3, SUE = 3;

	public PacManGameState state;
	public boolean started;
	public boolean attractMode;

	public int currentLevelNumber; // counting from 1
	public PacManGameLevel level;
	public Pac pac;
	public Ghost[] ghosts;
	public Bonus bonus;
	public String[] bonusNames;
	public int[] bonusValues;
	public byte lives;
	public int score;
	public int highscoreLevel, highscorePoints;
	public byte huntingPhase;
	public short ghostBounty;
	public List<Byte> levelSymbols;
	public short globalDotCounter;
	public boolean globalDotCounterEnabled;

	public abstract void buildLevel(int levelNumber);

	public abstract String hiscoreFilename();

	public abstract long bonusActivationTicks();

	public void enterLevel(int levelNumber) {
		currentLevelNumber = levelNumber;
		buildLevel(levelNumber);
		ghostBounty = 200;
		huntingPhase = 0;
		bonus.edibleTicksLeft = 0;
		bonus.eatenTicksLeft = 0;
		for (Ghost ghost : ghosts) {
			ghost.dotCounter = 0;
			ghost.elroy = 0;
		}
	}

	public void reset() {
		enterLevel(1);
		Hiscore hiscore = loadHighScore();
		highscoreLevel = hiscore.level;
		highscorePoints = hiscore.points;
		score = 0;
		lives = 3;
		levelSymbols = new ArrayList<>();
		levelSymbols.add(level.bonusSymbol);
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(ghosts);
	}

	public String stateDescription() {
		if (state == PacManGameState.HUNTING) {
			String phaseName = inScatteringPhase() ? "Scattering" : "Chasing";
			int phaseIndex = huntingPhase / 2;
			return String.format("%s-%s (%d of 4)", state, phaseName, phaseIndex + 1);
		}
		return state.name();
	}

	public boolean inScatteringPhase() {
		return huntingPhase % 2 == 0;
	}

	public Hiscore loadHighScore() {
		File dir = new File(System.getProperty("user.home"));
		Hiscore hiscore = new Hiscore(new File(dir, hiscoreFilename()));
		hiscore.load();
		return hiscore;
	}

	public void saveHighscore() {
		Hiscore hiscore = loadHighScore();
		if (highscorePoints > hiscore.points) {
			hiscore.points = highscorePoints;
			hiscore.level = highscoreLevel;
			hiscore.save();
			log("New hiscore saved. %d points in level %d", hiscore.points, hiscore.level);
		}
	}
}