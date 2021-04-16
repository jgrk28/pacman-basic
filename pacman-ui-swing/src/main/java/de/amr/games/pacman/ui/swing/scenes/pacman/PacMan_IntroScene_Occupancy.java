package de.amr.games.pacman.ui.swing.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.TOP_Y;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.GhostPortrait;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class PacMan_IntroScene_Occupancy extends PacMan_IntroScene {

  public PacMan_IntroScene_Occupancy(PacManGameController controller,
      Dimension size) {
    super(controller, size);
  }

  protected void drawGallery(Graphics2D g) {
    g.setColor(Color.WHITE);
    g.setFont(rendering.getScoreFont());
    g.drawString("Occupancy Map", t(6), TOP_Y);
  }
}
