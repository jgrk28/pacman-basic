package de.amr.games.pacman.ui.swing;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.Timer;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.swing.assets.AssetLoader;
import de.amr.games.pacman.ui.swing.common.GameScene;
import de.amr.games.pacman.ui.swing.common.PlayScene;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.swing.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.swing.rendering.Debug;
import de.amr.games.pacman.ui.swing.rendering.SwingRendering;
import de.amr.games.pacman.ui.swing.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.swing.rendering.standard.PacMan_StandardRendering;

/**
 * A Swing implementation of the Pac-Man game UI interface.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_Swing implements PacManGameUI {

	private final PacManGameController controller;
	private final EnumMap<GameType, SwingRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, List<GameScene>> scenes = new EnumMap<>(GameType.class);
	private final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();
	private final Dimension unscaledSize;
	private final V2i scaledSize;
	private final float scaling;
	private final JFrame window;
	private final Timer titleUpdateTimer;
	private final Canvas canvas;
	private final Keyboard keyboard;

	private GameScene currentScene;
	private GameModel game;
	private boolean muted;

	public PacManGameUI_Swing(PacManGameController controller, double scalingFactor) {
		this.controller = controller;
		scaling = (float) scalingFactor;
		unscaledSize = new Dimension(28 * TS, 36 * TS);
		scaledSize = new V2f(unscaledSize.width, unscaledSize.height).scaled(this.scaling).toV2i();

		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		canvas.setSize(scaledSize.x, scaledSize.y);
		canvas.setFocusable(false);

		window = new JFrame();
		window.setTitle("Swing: Pac-Man");
		window.setBackground(Color.BLACK);
		window.setResizable(false);
		window.setFocusable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setIconImage(AssetLoader.image("/pacman/graphics/pacman.png"));
		window.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				handleGlobalKeys(e);
			}
		});
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				controller.endGameLoop();
			}
		});
		window.getContentPane().add(canvas);

		keyboard = new Keyboard(window);

		titleUpdateTimer = new Timer(1000,
				e -> window.setTitle(String.format("Pac-Man / Ms. Pac-Man (%d fps, JFC Swing)", clock.frequency)));

		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		renderings.put(PACMAN, new PacMan_StandardRendering());

		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));

		scenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene(unscaledSize, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN))//
		));

		scenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene(unscaledSize, renderings.get(PACMAN), sounds.get(PACMAN))//
		));

		onGameChanged(controller.getGame());
		log("Swing UI created at clock tick %d", clock.ticksTotal);
	}

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private GameScene currentScene() {
		GameType currentGame = currentGame();
		switch (game.state) {
		case INTRO:
			return scenes.get(currentGame).get(0);
		case INTERMISSION:
			return scenes.get(currentGame).get(game.intermissionNumber);
		default:
			return scenes.get(currentGame).get(4);
		}
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		scenes.get(currentGame()).forEach(scene -> scene.setGame(newGame));
		changeScene(currentScene());
	}

	private void changeScene(GameScene newScene) {
		currentScene = currentScene();
		currentScene.start();
	}

	@Override
	public void show() {
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		canvas.createBufferStrategy(2);
		moveMousePointerOutOfSight();
		titleUpdateTimer.start();
	}

	@Override
	public void update() {
		GameScene newScene = currentScene();
		if (newScene == null) {
			throw new IllegalStateException("No scene found for game state " + game.state);
		}
		if (currentScene != newScene) {
			if (currentScene != null) {
				currentScene.end();
			}
			newScene.start();
			log("Current scene changed from %s to %s", currentScene, newScene);
		}
		currentScene = newScene;
		currentScene.update();

		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			message.timer.run();
			if (message.timer.expired()) {
				flashMessageQ.remove();
			}
		}
	}

	private void renderScreen() {
		BufferStrategy buffers = canvas.getBufferStrategy();
		do {
			do {
				Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				g.scale(scaling, scaling);
				currentScene.render(g);
				drawFlashMessage(g);
				g.dispose();
			} while (buffers.contentsRestored());
			buffers.show();
		} while (buffers.contentsLost());
	}

	@Override
	public void render() {
		EventQueue.invokeLater(this::renderScreen);
	}

	@Override
	public void reset() {
		currentScene.end();
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessageQ.add(new FlashMessage(message, ticks));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public void mute(boolean b) {
		muted = b;
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty(); // TODO that's just a hack, should have real mute functionality
		}
		return Optional.of(sounds.get(currentGame()));
	}

	@Override
	public Optional<PacManGameAnimations> animation() {
		return Optional.of(renderings.get(currentGame()));
	}

	private void handleGlobalKeys(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_S: {
			clock.targetFreq = clock.targetFreq != 30 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KeyEvent.VK_F: {
			clock.targetFreq = clock.targetFreq != 120 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case KeyEvent.VK_D:
			Debug.on = !Debug.on;
			log("UI debug mode is %s", Debug.on ? "on" : "off");
			break;
		default:
			break;
		}
	}

	private void drawFlashMessage(Graphics2D g) {
		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			double alpha = Math.cos(Math.PI * message.timer.running() / (2 * message.timer.getDuration()));
			g.setColor(Color.BLACK);
			g.fillRect(0, unscaledSize.height - 16, unscaledSize.width, 16);
			g.setColor(new Color(1, 1, 0, (float) alpha));
			g.setFont(new Font(Font.SERIF, Font.BOLD, 10));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(message.text, (unscaledSize.width - g.getFontMetrics().stringWidth(message.text)) / 2,
					unscaledSize.height - 3);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}

	private void moveMousePointerOutOfSight() {
		try {
			Robot robot = new Robot();
			robot.mouseMove(window.getX() + 10, window.getY());
		} catch (AWTException x) {
			x.printStackTrace();
		}
	}
}