package hypersonic;

public class DecisionMaker {

  private GameParameters params;
  private Map map;
  private EntityHandler entities;

  public DecisionMaker(GameParameters params, Map map, EntityHandler entities) {
    this.params = params;
    this.map = map;
    this.entities = entities;
  }

  public void decision() {
    if (entities.hero().hasBomb()) {
      Point target = map.bestBombTarget(entities.hero().position());
      if (entities.hero().position().equals(target)) {
        bomb(entities.closestItemPosition());
      } else {
        move(target);
      }
    } else {
      move(entities.closestItemPosition());
    }
  }

  private void move(Point point) {
    System.out.println("MOVE " + point.x() + " " + point.y());
  }

  private void bomb(Point point) {
    System.out.println("BOMB " + point.x() + " " + point.y());
  }
}
