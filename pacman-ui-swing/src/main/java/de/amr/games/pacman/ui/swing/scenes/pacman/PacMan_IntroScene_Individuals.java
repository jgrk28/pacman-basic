package de.amr.games.pacman.ui.swing.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.TOP_Y;

import de.amr.games.pacman.controller.PacManGameController;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class PacMan_IntroScene_Individuals extends PacMan_IntroScene {

  public PacMan_IntroScene_Individuals(PacManGameController controller,
      Dimension size) {
    super(controller, size);
  }

  protected void drawGallery(Graphics2D g) {
    g.setColor(Color.WHITE);
    g.setFont(rendering.getScoreFont());
    g.drawString("Individuals", t(6), TOP_Y);
  }
}
