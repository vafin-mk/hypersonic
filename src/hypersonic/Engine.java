package hypersonic;

import java.util.Scanner;

public class Engine {

  public void start() {
    Scanner scanner = new Scanner(System.in);
    int width = scanner.nextInt();
    int height = scanner.nextInt();
    int myId = scanner.nextInt();
    Grid grid = new Grid(width, height, myId);
    AI ai = new AI(grid);

    long time = System.nanoTime();
    int round = 0;

    while (true) {
      grid.updateMap(scanner);
      grid.updateEntities(scanner);
      grid.updateExplosionMap();
      grid.calculateAvailableTiles();

//      grid.debugMap();
//      grid.debugEntities();
//      grid.debugExplosions();
//      grid.debugPoints();
      grid.debugList(grid.moves, 5, "MOVES");

      ai.decision();

      System.err.println(String.format("=================%s ROUND COMPLETE====================", round));
      System.err.println(round + " round calc time in " + (System.nanoTime() - time) / 1000000 + " ms");
      time = System.nanoTime();
      round++;
      grid.round = round;
    }
  }
}
