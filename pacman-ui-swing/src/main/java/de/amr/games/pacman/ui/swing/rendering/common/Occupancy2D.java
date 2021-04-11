package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

public class Occupancy2D {

  public final PacManGameController controller;

  public Occupancy2D(PacManGameController controller) {
    this.controller = controller;
  }

  public void render(Graphics2D g) {
    HashMap<V2i, Double> occupancyMap = controller.getOccupancy();
    if (occupancyMap != null) {
      for (Map.Entry<V2i, Double> tileOccupancy : occupancyMap.entrySet()) {
        int opacity = (int) (tileOccupancy.getValue() * 255);
        Color myColour = new Color(255, 0, 0, opacity);
        g.setColor(myColour);
        int right = TS * tileOccupancy.getKey().x;
        int top = TS * tileOccupancy.getKey().y;
        g.fillRect(right, top, TS, TS);
      }
    }
  }
}
