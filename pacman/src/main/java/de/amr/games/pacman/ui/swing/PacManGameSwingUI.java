package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.game.worlds.PacManGameWorld.TS;
import static de.amr.games.pacman.game.worlds.PacManGameWorld.t;
import static de.amr.games.pacman.lib.Logging.log;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.game.core.PacManGame;
import de.amr.games.pacman.game.core.PacManGameState;
import de.amr.games.pacman.game.core.PacManGameVariant;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.api.PacManGameSound;
import de.amr.games.pacman.ui.api.PacManGameUI;
import de.amr.games.pacman.ui.swing.classic.PacManClassicAssets;
import de.amr.games.pacman.ui.swing.classic.PacManClassicIntroScene;
import de.amr.games.pacman.ui.swing.classic.PacManClassicPlayScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManAssets;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManIntroScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacManPlayScene;
import de.amr.games.pacman.ui.swing.scene.PacManGameScene;

/**
 * Swing UI for Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameSwingUI implements PacManGameUI {

	public static BufferedImage image(String path) {
		try (InputStream is = PacManGameSwingUI.class.getResourceAsStream(path)) {
			return ImageIO.read(is);
		} catch (Exception x) {
			throw new AssetException("Could not load image with path '%s'", path);
		}
	}

	public static Font font(String fontPath, int size) {
		try (InputStream fontData = PacManGameSwingUI.class.getResourceAsStream(fontPath)) {
			return Font.createFont(Font.TRUETYPE_FONT, fontData).deriveFont((float) size);
		} catch (Exception x) {
			throw new AssetException("Could not load font with path '%s'", fontPath);
		}
	}

	public static boolean debugMode;
	public static final ResourceBundle TEXTS = ResourceBundle.getBundle("localization.translation");

	static final int PAUSE_KEY = KeyEvent.VK_P;
	static final int SLOWMODE_KEY = KeyEvent.VK_S;
	static final int FASTMODE_KEY = KeyEvent.VK_F;
	static final int DEBUGMODE_KEY = KeyEvent.VK_D;

	private final V2i unscaledSizePixels;
	private final V2i scaledSizeInPixels;
	private final float scaling;
	private final JFrame window;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private String messageText;
	private Color messageColor;
	private Font messageFont;
	private Timer titleUpdateTimer;
	private PacManGame game;
	private PacManGameScene currentScene;
	private PacManGameScene introScene;
	private PacManGameScene playScene;
	private PacManGameSoundManager soundManager;

	public PacManGameSwingUI(PacManGame game, float scaling) {
		this.scaling = scaling;
		unscaledSizePixels = game.world.sizeInTiles().scaled(TS);
		scaledSizeInPixels = new V2i((int) (unscaledSizePixels.x * scaling), (int) (unscaledSizePixels.y * scaling));

		window = new JFrame();

		window.setTitle("Pac-Man");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(image("/pacman.png"));

		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				game.exit();
			}
		});
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == PAUSE_KEY) {
					game.paused = !game.paused;
				}
				if (e.getKeyCode() == SLOWMODE_KEY) {
					game.clock.targetFrequency = game.clock.targetFrequency == 60 ? 30 : 60;
					log("Clock frequency changed to %d Hz", game.clock.targetFrequency);
				}
				if (e.getKeyCode() == FASTMODE_KEY) {
					game.clock.targetFrequency = game.clock.targetFrequency == 60 ? 120 : 60;
					log("Clock frequency changed to %d Hz", game.clock.targetFrequency);
				}
				if (e.getKeyCode() == DEBUGMODE_KEY) {
					debugMode = !debugMode;
					log("UI debug mode is %s", debugMode ? "on" : "off");
				}
			}
		});

		keyboard = new Keyboard(window);

		canvas = new Canvas();
		canvas.setBackground(new Color(0, 0, 0));
		canvas.setSize(scaledSizeInPixels.x, scaledSizeInPixels.y);
		canvas.setFocusable(false);
		window.add(canvas);

		messageFont = font("/PressStart2P-Regular.ttf", 8).deriveFont(Font.PLAIN);

		setGame(game);
	}

	@Override
	public void setGame(PacManGame game) {
		this.game = game;
		game.ui = this;
		onGameVariantChanged(game);
	}

	@Override
	public void onGameVariantChanged(PacManGame game) {

		if (soundManager != null) {
			stopAllSounds();
		}

		if (game.variant == PacManGameVariant.CLASSIC) {
			PacManClassicAssets assets = new PacManClassicAssets();
			soundManager = new PacManGameSoundManager(assets.soundPaths::get);
			introScene = new PacManClassicIntroScene(game, unscaledSizePixels, assets);
			playScene = new PacManClassicPlayScene(game, unscaledSizePixels, assets);
		} else {
			MsPacManAssets assets = new MsPacManAssets();
			soundManager = new PacManGameSoundManager(assets.soundPaths::get);
			introScene = new MsPacManIntroScene(game, unscaledSizePixels, assets);
			playScene = new MsPacManPlayScene(game, unscaledSizePixels, assets);
		}

		if (titleUpdateTimer != null) {
			titleUpdateTimer.stop();
		}
		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("%s (%d fps)", game.world.pacName(), game.clock.frequency)));
		titleUpdateTimer.start();
	}

	@Override
	public void openWindow() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		canvas.createBufferStrategy(2);
	}

	@Override
	public void showMessage(String message, boolean important) {
		messageText = message;
		messageColor = important ? Color.RED : Color.yellow;
	}

	@Override
	public void clearMessage() {
		messageText = null;
	}

	@Override
	public void render() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(canvas.getBackground());
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				updateScene();
				drawCurrentScene(g);
				if (game.paused) {
					drawPausedScreen(g);
				}
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	private void drawCurrentScene(Graphics2D g) {
		Graphics2D gScaled = (Graphics2D) g.create();
		gScaled.scale(scaling, scaling);
		currentScene.draw(gScaled, g);
		if (messageText != null) {
			gScaled.setFont(messageFont);
			gScaled.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			gScaled.setColor(messageColor);
			int textWidth = gScaled.getFontMetrics().stringWidth(messageText);
			gScaled.drawString(messageText, (unscaledSizePixels.x - textWidth) / 2, t(21));
			gScaled.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		gScaled.dispose();
	}

	private void drawPausedScreen(Graphics2D g) {
		Font font = new Font(Font.MONOSPACED, Font.BOLD, 24);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(new Color(0, 48, 143, 100));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setColor(Color.GREEN);
		g.setFont(font);
		int y = canvas.getHeight() / 2;
		String text = "PAUSED";
		g.drawString(text, (canvas.getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
		y += font.getSize() * 150 / 100;
		text = "(Press 'P' key to resume)";
		g.drawString(text, (canvas.getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	private void updateScene() {
		PacManGameScene scene = null;
		if (game.state == PacManGameState.INTRO) {
			scene = introScene;
		} else {
			scene = playScene;
		}
		if (scene != currentScene) {
			if (currentScene != null) {
				currentScene.end();
			}
			currentScene = scene;
			currentScene.start();
		}
	}

	@Override
	public void playSound(PacManGameSound sound) {
		soundManager.playSound(sound);
	}

	@Override
	public void loopSound(PacManGameSound sound) {
		soundManager.loopSound(sound);
	}

	@Override
	public void stopSound(PacManGameSound sound) {
		soundManager.stopSound(sound);
	}

	@Override
	public void stopAllSounds() {
		soundManager.stopAllSounds();
	}
}