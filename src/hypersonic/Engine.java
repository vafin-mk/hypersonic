package hypersonic;

import java.util.Scanner;

public class Engine {

  public void start() {
    Scanner in = new Scanner(System.in);
    Constants.WIDTH = in.nextInt();
    Constants.HEIGHT = in.nextInt();
    Constants.TEAM_ID = in.nextInt();
    in.nextLine();

    TileMap tileMap = new TileMap();

    // game loop
    while (true) {
      tileMap.updateMap(in);
      in.nextLine();

      if (tileMap.canBombNow()) {
        command(true, tileMap.findBestPositionWithBomb());
      } else {
        command(false, tileMap.findBestPosition());
      }
    }
  }

  private void command(boolean bomb, Point position) {
    System.out.println(String.format("%s %s %s", bomb ? "BOMB" : "MOVE", position.x, position.y));
  }
}
