package hypersonic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EntityHandler {

  private static final int ENTITY_TYPE_BOMBER = 0;
  private static final int ENTITY_TYPE_BOMB = 1;
  private static final int ENTITY_TYPE_ITEM = 2;

  private List<Bomb> bombs = new ArrayList<>();
  private List<Item> items = new ArrayList<>();
  private List<Bomber> rivals = new ArrayList<>();
  private Bomber hero = new Bomber(new Point(0, 0), 1, 3);

  private int heroId;

  EntityHandler(int heroId) {
    this.heroId = heroId;
  }

  public void updateEntities(Scanner scanner) {
    bombs.clear();
    items.clear();
    rivals.clear();

    int entities = scanner.nextInt();
    for (int i = 0; i < entities; i++) {
      int entityType = scanner.nextInt();
      int owner = scanner.nextInt();
      Point point = new Point(scanner);
      int param1 = scanner.nextInt();
      int param2 = scanner.nextInt();

      switch (entityType) {
        case ENTITY_TYPE_BOMBER:
          if (owner == heroId) {
            hero.set(point, param1, param2);
          } else {
            rivals.add(new Bomber(point, param1, param2));
          }
          break;
        case ENTITY_TYPE_BOMB:
          bombs.add(new Bomb(point, param1, param2));
          break;
        case ENTITY_TYPE_ITEM:
          items.add(new Item(point, param1));
          break;
        default:
          throw new RuntimeException("Unknown entity type:" + entityType);
      }
    }
  }

  public List<Bomb> bombs() {
    return bombs;
  }

  public List<Item> items() {
    return items;
  }

  public List<Bomber> rivals() {
    return rivals;
  }

  public Bomber hero() {
    return hero;
  }

  public Point closestItemPosition() {
    Point result = new Point(6,6);
    int closestDist = Integer.MAX_VALUE;
    for (Item item : items) {
      int distToItem = Math.abs(hero.position().x() - item.position().x()) + Math.abs(hero.position().y() - item.position().y());
      if (distToItem < closestDist) {
        closestDist = distToItem;
        result = item.position();
      }
    }
    return result;
  }
}
