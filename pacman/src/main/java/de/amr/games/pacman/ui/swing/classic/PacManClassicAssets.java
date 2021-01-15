package de.amr.games.pacman.ui.swing.classic;

import static de.amr.games.pacman.ui.swing.Assets.font;
import static de.amr.games.pacman.ui.swing.Assets.image;
import static de.amr.games.pacman.world.PacManClassicWorld.APPLE;
import static de.amr.games.pacman.world.PacManClassicWorld.BELL;
import static de.amr.games.pacman.world.PacManClassicWorld.CHERRIES;
import static de.amr.games.pacman.world.PacManClassicWorld.GALAXIAN;
import static de.amr.games.pacman.world.PacManClassicWorld.GRAPES;
import static de.amr.games.pacman.world.PacManClassicWorld.KEY;
import static de.amr.games.pacman.world.PacManClassicWorld.PEACH;
import static de.amr.games.pacman.world.PacManClassicWorld.STRAWBERRY;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.swing.SoundAssets;

/**
 * Assets used in Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManClassicAssets implements SoundAssets {

	/** Sprite sheet order of directions. */
	public static final Map<Direction, Integer> DIR_INDEX = new EnumMap<>(Direction.class);

	static {
		DIR_INDEX.put(Direction.RIGHT, 0);
		DIR_INDEX.put(Direction.LEFT, 1);
		DIR_INDEX.put(Direction.UP, 2);
		DIR_INDEX.put(Direction.DOWN, 3);
	}

	public final BufferedImage spriteSheet;
	public final BufferedImage gameLogo;
	public final BufferedImage mazeFull;
	public final BufferedImage mazeEmptyDark;
	public final BufferedImage mazeEmptyBright;
	public final BufferedImage life;
	public final BufferedImage[] symbols = new BufferedImage[8];
	public final Map<Integer, BufferedImage> numbers = new HashMap<>();
	public final Map<PacManGameSound, String> soundPaths = new EnumMap<>(PacManGameSound.class);
	public final Font scoreFont;

	public PacManClassicAssets() {
		//@formatter:off
		gameLogo            = image("/worlds/classic/logo.png");
		spriteSheet         = image("/worlds/classic/sprites.png");
		mazeFull            = image("/worlds/classic/maze_full.png");
		mazeEmptyDark       = image("/worlds/classic/maze_empty.png");
		mazeEmptyBright     = image("/worlds/classic/maze_empty_white.png");

		life                = section(8, 1);

		symbols[CHERRIES]   = section(2, 3);
		symbols[STRAWBERRY] = section(3, 3);
		symbols[PEACH]      = section(4, 3);
		symbols[APPLE]      = section(5, 3);
		symbols[GRAPES]     = section(6, 3);
		symbols[GALAXIAN]   = section(7, 3);
		symbols[BELL]       = section(8, 3);
		symbols[KEY]        = section(9, 3);
	
		numbers.put(200,  section(0, 8));
		numbers.put(400,  section(1, 8));
		numbers.put(800,  section(2, 8));
		numbers.put(1600, section(3, 8));
		
		numbers.put(100,  section(0, 9));
		numbers.put(300,  section(1, 9));
		numbers.put(500,  section(2, 9));
		numbers.put(700,  section(3, 9));
		
		numbers.put(1000, section(4, 9, 2, 1)); // left-aligned 
		
		numbers.put(2000, section(3, 10, 3, 1));
		numbers.put(3000, section(3, 11, 3, 1));
		numbers.put(5000, section(3, 12, 3, 1));
	
		soundPaths.put(PacManGameSound.CREDIT,       "/sound/credit.wav");
		soundPaths.put(PacManGameSound.EAT_BONUS,    "/sound/eat_fruit.wav");
		soundPaths.put(PacManGameSound.EXTRA_LIFE,   "/sound/extend.wav");
		soundPaths.put(PacManGameSound.GAME_READY,   "/sound/game_start.wav");
		soundPaths.put(PacManGameSound.GHOST_DEATH,  "/sound/eat_ghost.wav");
		soundPaths.put(PacManGameSound.MUNCH,        "/sound/munch_1.wav");
		soundPaths.put(PacManGameSound.PACMAN_DEATH, "/sound/death_1.wav");
		soundPaths.put(PacManGameSound.PACMAN_POWER, "/sound/power_pellet.wav");
		soundPaths.put(PacManGameSound.RETREATING,   "/sound/retreating.wav");
		soundPaths.put(PacManGameSound.SIREN_1,      "/sound/siren_1.wav");
		soundPaths.put(PacManGameSound.SIREN_2,      "/sound/siren_2.wav");
		soundPaths.put(PacManGameSound.SIREN_3,      "/sound/siren_3.wav");
		soundPaths.put(PacManGameSound.SIREN_4,      "/sound/siren_4.wav");
		soundPaths.put(PacManGameSound.SIREN_5,      "/sound/siren_5.wav");
		//@formatter:on

		scoreFont = font("/PressStart2P-Regular.ttf", 8);
	}

	public BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage section(int x, int y) {
		return section(x, y, 1, 1);
	}

	@Override
	public String getSoundPath(PacManGameSound sound) {
		return soundPaths.get(sound);
	}
}