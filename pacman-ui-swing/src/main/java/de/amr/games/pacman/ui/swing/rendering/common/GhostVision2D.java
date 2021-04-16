package de.amr.games.pacman.ui.swing.rendering.common;

import static de.amr.games.pacman.model.common.Ghost.BLINKY;
import static de.amr.games.pacman.model.common.Ghost.CLYDE;
import static de.amr.games.pacman.model.common.Ghost.INKY;
import static de.amr.games.pacman.model.common.Ghost.PINKY;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

public class GhostVision2D {
  public final Ghost ghost;
  public final PacManGameController controller;

  public GhostVision2D(Ghost ghost, PacManGameController controller) {
    this.controller = controller;
    this.ghost = ghost;
  }

  public void render(Graphics2D g) {
    V2i currTile = ghost.tile();
    switch (ghost.id) {
      case INKY:
        Color inkyColour = new Color(25, 25, 255, 150);
        g.setColor(inkyColour);

        V2i inkyEndSight = currTile.plus(ghost.dir.vec.scaled(8));
        int inkyTop = Math.min(currTile.y, inkyEndSight.y) * TS;
        int inkyHeight = (Math.abs(currTile.y - inkyEndSight.y) + 1) * TS;
        int inkyRight = Math.min(currTile.x, inkyEndSight.x) * TS;
        int inkyWidth = (Math.abs(currTile.x - inkyEndSight.x) + 1) * TS;
        g.fillRect(inkyRight, inkyTop, inkyWidth, inkyHeight);
        break;
      case BLINKY:
        Color blinkyColour = new Color(150, 0, 100, 150);
        g.setColor(blinkyColour);

        V2i blinkyEndSight = currTile;
        for (int i = 0; i <= 4; i++) {
          V2i aheadGhost = currTile.plus(ghost.dir.vec.scaled(i));
          if (controller.game().currentLevel.getWorld().isWall(aheadGhost)) {
            break;
          } else {
            blinkyEndSight = aheadGhost;
          }
        }
        int blinkyTop = Math.min(currTile.y, blinkyEndSight.y) * TS;
        int blinkyHeight = (Math.abs(currTile.y - blinkyEndSight.y) + 1) * TS;
        int blinkyRight = Math.min(currTile.x, blinkyEndSight.x) * TS;
        int blinkyWidth = (Math.abs(currTile.x - blinkyEndSight.x) + 1) * TS;
        g.fillRect(blinkyRight, blinkyTop, blinkyWidth, blinkyHeight);
        break;
      case PINKY:
        Color pinkyColour = new Color(255, 100, 150, 200);
        g.setColor(pinkyColour);

        V2i pinkyEndSight = currTile;
        for (int i = 0; i <= 4; i++) {
          V2i aheadGhost = currTile.plus(ghost.dir.vec.scaled(i));
          if (controller.game().currentLevel.getWorld().isWall(aheadGhost)) {
            break;
          } else {
            pinkyEndSight = aheadGhost;
          }
        }
        int pinkyTop = Math.min(currTile.y, pinkyEndSight.y) * TS;
        int pinkyHeight = (Math.abs(currTile.y - pinkyEndSight.y) + 1) * TS;
        int pinkyRight = Math.min(currTile.x, pinkyEndSight.x) * TS;
        int pinkyWidth = (Math.abs(currTile.x - pinkyEndSight.x) + 1) * TS;
        g.fillRect(pinkyRight, pinkyTop, pinkyWidth, pinkyHeight);
        break;
      case CLYDE:
        Color clydeColour = new Color(255, 150, 0, 50);
        g.setColor(clydeColour);
        int circRight = (currTile.x - 6) * TS;
        int circTop = (currTile.y - 6) * TS;
        g.fillOval(circRight, circTop, 13 * TS, 13 * TS);
        break;
    }
  }
}
