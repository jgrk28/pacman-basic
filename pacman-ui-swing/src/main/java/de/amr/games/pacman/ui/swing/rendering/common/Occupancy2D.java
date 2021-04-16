package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
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
        double probability = tileOccupancy.getValue();
        int adjustedOpacity = (int) (logFunc(logFunc(logFunc(probability))) * 255);
        Color myColour = new Color(255, 0, 0, adjustedOpacity);
        g.setColor(myColour);
        int right = TS * tileOccupancy.getKey().x;
        int top = TS * tileOccupancy.getKey().y;
        g.fillRect(right, top, TS, TS);
      }
    }
  }

  //This log function will take a number between 0 and 1 and
  //will output a number between 0 and 1. Lower number are scaled
  //up while higher numbers stay the same
  private double logFunc(Double probability) {
    //scale to number between 1 and 10
    double oneToTen = (probability * 9) + 1;
    //take log base ten to get back to a number between 0 and 1
    return Math.log10(oneToTen);
  }
}
