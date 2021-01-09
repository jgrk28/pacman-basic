package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.core.PacManGame.APPLE;
import static de.amr.games.pacman.core.PacManGame.BELL;
import static de.amr.games.pacman.core.PacManGame.CHERRIES;
import static de.amr.games.pacman.core.PacManGame.GALAXIAN;
import static de.amr.games.pacman.core.PacManGame.GRAPES;
import static de.amr.games.pacman.core.PacManGame.KEY;
import static de.amr.games.pacman.core.PacManGame.PEACH;
import static de.amr.games.pacman.core.PacManGame.STRAWBERRY;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.Sound;

/**
 * Assets used in Pac-Man game.
 * 
 * @author Armin Reichert
 */
class Assets {

	public static final Map<Direction, Integer> DIR_INDEX;

	static {
		DIR_INDEX = new EnumMap<>(Direction.class);
		DIR_INDEX.put(Direction.RIGHT, 0);
		DIR_INDEX.put(Direction.LEFT, 1);
		DIR_INDEX.put(Direction.UP, 2);
		DIR_INDEX.put(Direction.DOWN, 3);
	}

	public static final Color[] GHOST_COLORS = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	public final BufferedImage imageLogo;
	public final BufferedImage imageMazeFull;
	public final BufferedImage imageMazeEmpty;
	public final BufferedImage imageMazeEmptyWhite;
	public final BufferedImage spriteSheet;
	public final BufferedImage imageLive;
	public final BufferedImage[] symbols;
	public final Map<Short, BufferedImage> numbers;
	public final Font scoreFont;
	public final Map<Sound, String> soundPaths;

	public Assets() {
		spriteSheet = image("/sprites.png");
		imageLogo = image("/logo.png");
		imageMazeFull = image("/maze_full.png");
		imageMazeEmpty = image("/maze_empty.png");
		imageMazeEmptyWhite = image("/maze_empty_white.png");
		imageLive = sheet(8, 1);

		scoreFont = font("/PressStart2P-Regular.ttf", 8);

		//@formatter:off
		symbols = new BufferedImage[8];
		symbols[CHERRIES]   = sheet(2, 3);
		symbols[STRAWBERRY] = sheet(3, 3);
		symbols[PEACH]      = sheet(4, 3);
		symbols[APPLE]      = sheet(5, 3);
		symbols[GRAPES]     = sheet(6, 3);
		symbols[GALAXIAN]   = sheet(7, 3);
		symbols[BELL]       = sheet(8, 3);
		symbols[KEY]        = sheet(9, 3);
	
		numbers = new HashMap<>();
		numbers.put((short)100,  sheet(0, 9));
		numbers.put((short)200,  sheet(0, 8));
		numbers.put((short)300,  sheet(1, 9));
		numbers.put((short)400,  sheet(1, 8));
		numbers.put((short)500,  sheet(2, 9));
		numbers.put((short)700,  sheet(3, 9));
		numbers.put((short)800,  sheet(2, 8));
		numbers.put((short)1000, section(4, 9, 2, 1));
		numbers.put((short)1600, sheet(3, 8));
		numbers.put((short)2000, section(3, 10, 3, 1));
		numbers.put((short)3000, section(3, 11, 3, 1));
		numbers.put((short)5000, section(3, 12, 3, 1));
	
		soundPaths = new EnumMap<>(Sound.class);
		soundPaths.put(Sound.CREDIT,       "/sound/credit.wav");
		soundPaths.put(Sound.EAT_BONUS,    "/sound/eat_fruit.wav");
		soundPaths.put(Sound.EXTRA_LIFE,   "/sound/extend.wav");
		soundPaths.put(Sound.GAME_READY,   "/sound/game_start.wav");
		soundPaths.put(Sound.GHOST_DEATH,  "/sound/eat_ghost.wav");
		soundPaths.put(Sound.MUNCH,        "/sound/munch_1.wav");
		soundPaths.put(Sound.PACMAN_DEATH, "/sound/death_1.wav");
		soundPaths.put(Sound.PACMAN_POWER, "/sound/power_pellet.wav");
		soundPaths.put(Sound.RETREATING,   "/sound/retreating.wav");
		soundPaths.put(Sound.SIREN_1,      "/sound/siren_1.wav");
		soundPaths.put(Sound.SIREN_2,      "/sound/siren_2.wav");
		soundPaths.put(Sound.SIREN_3,      "/sound/siren_3.wav");
		soundPaths.put(Sound.SIREN_4,      "/sound/siren_4.wav");
		soundPaths.put(Sound.SIREN_5,      "/sound/siren_5.wav");
		//@formatter:on
	}

	public BufferedImage section(int x, int y, int w, int h) {
		return spriteSheet.getSubimage(x * 16, y * 16, w * 16, h * 16);
	}

	public BufferedImage sheet(int x, int y) {
		return section(x, y, 1, 1);
	}

	public BufferedImage image(String path) {
		try (InputStream is = getClass().getResourceAsStream(path)) {
			return ImageIO.read(is);
		} catch (Exception x) {
			throw new AssetsException("Could not load image with path '%s'", path);
		}
	}

	public Font font(String fontPath, int size) {
		try (InputStream fontData = getClass().getResourceAsStream(fontPath)) {
			return Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) size);
		} catch (Exception x) {
			throw new AssetsException("Could not load font with path '%s'", fontPath);
		}
	}
}