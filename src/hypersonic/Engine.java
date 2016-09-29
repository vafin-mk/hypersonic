package hypersonic;

import java.util.Scanner;

public class Engine {

  public void start() {
    Scanner scanner = new Scanner(System.in);
    World world = new World();
    world.getWorldParameters(scanner);
    AI ai = new AI(world);
    long ns = System.nanoTime();

    while (true) {
      System.err.println("Round " + world.round + " calc in " + (System.nanoTime() - ns) / 1000000 + " ms");
      ns = System.nanoTime();

      world.updateTiles(scanner);
      world.updateEntities(scanner);
      world.updateExplosionMap();
      world.updateTileLinks();

      if (world.round >= 120 || world.remainingBoxes < 5) {
        System.err.println("CHANGE STRATEGY");

        if (world.getLeader().equals(world.hero)) {
          ai.scanRange = 8;
          ai.strategy = AI.Strategy.RUNNER;
        } else {
          ai.scanRange = 16;
          ai.strategy = AI.Strategy.AGGRO_PALADIN;
        }
      }
      ai.makeDecision();
      world.round++;
    }
  }
}
