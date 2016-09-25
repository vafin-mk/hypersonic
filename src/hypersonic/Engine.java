package hypersonic;

import java.util.Scanner;

public class Engine {

  private GameParameters params = new GameParameters();
  private Map map;
  private EntityHandler entityHandler;
  private DecisionMaker decisionMaker;

  public void start() {
    Scanner scanner = new Scanner(System.in);
    params.fromScanner(scanner);
    map = new Map(params);
    entityHandler = new EntityHandler(params.teamId());
    decisionMaker = new DecisionMaker(params, map, entityHandler);
    scanner.nextLine();

    // game loop
    while (true) {
      map.updateMap(scanner);
      entityHandler.updateEntities(scanner);
      map.applyEntityHeats(entityHandler);
      scanner.nextLine();
      decisionMaker.decision();
    }
  }
}
