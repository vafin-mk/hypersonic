package hypersonic;

import java.util.Scanner;

public class GameParameters {

  private int mapWidth;
  private int mapHeight;
  private int myTeamid;

  public void fromScanner(Scanner scanner) {
    mapWidth = scanner.nextInt();
    mapHeight = scanner.nextInt();
    myTeamid = scanner.nextInt();
  }

  public int width() {
    return mapWidth;
  }

  public int height() {
    return mapHeight;
  }

  public int teamId() {
    return myTeamid;
  }
}
