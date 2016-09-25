import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.List;

class Player { public static void main(String[] args) { new Engine().start();}}
class Bomb {

  private Point position;
  private int rounds;
  private int power = 3;

  Bomb(Point position, int rounds, int power) {
    set(position, rounds, power);
  }

  public void set(Point position, int rounds, int power) {
    this.position = position;
    this.rounds = rounds;
    this.power = power;
  }

  public Point position() {
    return position;
  }

  public int rounds() {
    return rounds;
  }

  public int power() {
    return power;
  }
}

class Bomber {

  private Point position;
  private int amount;
  private int power = 3;

  Bomber(Point position, int amount, int power) {
    set(position, amount, power);
  }

  public void set(Point position, int amount, int power) {
    this.position = position;
    this.amount = amount;
    this.power = power;
  }

  public Point position() {
    return position;
  }

  public int amount() {
    return amount;
  }

  public int power() {
    return power;
  }

  public boolean hasBomb() {
    return amount > 0;
  }
}

class Cell {
  public static final int CELL_TYPE_FLOOR = 0;
  public static final int CELL_TYPE_BOX = 1;

  private int type;
  private int heat;
  private Item item = new Item(new Point(-100,-100), Item.ITEM_TYPE_NONE);

  Cell(int type) {
    this.type = type;
  }

  public int type() {
    return type;
  }

  public void setItemType(int type) {
    item.setType(type);
  }

  public int itemType() {
    return item.type();
  }

  public void setHeat(int heat) {
    this.heat = heat;
  }

  public int heat() {
    return heat;
  }

  public void incrementHeat(int value) {
    this.heat += value;
  }
}

class DecisionMaker {

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

class Engine {

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

class EntityHandler {

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

class GameParameters {

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

class Heats {

  public static final int BOX_EMPTY = 2;
  public static final int BOX_POWER = 10;
  public static final int BOX_AMOUNT = 5;

  public static final int ITEM_POWER = 8;
  public static final int ITEM_AMOUNT = 4;

  public static final int RIVAL = -3;
  public static final int BOMB = -1000;
}

class Item {
  public static final int ITEM_TYPE_NONE = 0;
  public static final int ITEM_TYPE_POWER = 1;
  public static final int ITEM_TYPE_AMOUNT = 2;

  private int type;
  private Point position;

  Item(Point position, int type) {
    this.position = position;
    this.type = type;
  }

  public int type() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Point position() {
    return position;
  }
}

class Map {

  GameParameters params;
  Cell[][] map;
  private List<Point> boxes = new ArrayList<>();

  Map(GameParameters params) {
    this.params = params;
    this.map = new Cell[params.width()][params.height()];
  }

  public void updateMap(Scanner scanner) {
    boxes.clear();
    for (int y = 0; y < params.height(); y++) {
      char[] row = scanner.nextLine().toCharArray();
      for (int x = 0; x < row.length; x++) {
        char cell = row[x];
        switch (cell) {
          case '.':
            map[x][y] = new Cell(Cell.CELL_TYPE_FLOOR);
            break;
          case '0':
          case '1':
          case '2':
            boxes.add(new Point(x, y));
            map[x][y] = new Cell(Cell.CELL_TYPE_BOX);
            if (cell == '1') map[x][y].setItemType(Item.ITEM_TYPE_POWER);
            else if (cell == '2') map[x][y].setItemType(Item.ITEM_TYPE_AMOUNT);
            break;
          default:
            throw new RuntimeException("Unknown cell type: " + cell);
        }
      }
    }
  }

  public void applyEntityHeats(EntityHandler entities) {
    boxes.forEach(point -> applyBoxHeats(point.x(), point.y(), entities.hero().power()));
    //we can't place bomb on box
    boxes.forEach(point -> map[point.x()][point.y()].setHeat(0));
    applyItemHeats(entities.items());
    applyRivalsHeats(entities.rivals());
    applyBombHeats(entities.bombs());
  }

  //add heat for every cell which can explode box
  private void applyBoxHeats(int x, int y, int heroPower) {
    int heat;
    switch (map[x][y].itemType()) {
      case Item.ITEM_TYPE_NONE:
        heat = Heats.BOX_EMPTY;
        break;
      case Item.ITEM_TYPE_POWER:
        heat = Heats.BOX_POWER;
        break;
      case Item.ITEM_TYPE_AMOUNT:
        heat = Heats.BOX_AMOUNT;
        break;
      default:
        throw new RuntimeException("Unknown item type:" + map[x][y].itemType());
    }
    for (int xIndex = Math.max(0, x - heroPower); xIndex < Math.min(params.width(), x + heroPower); xIndex++) {
      map[xIndex][y].incrementHeat(heat);
    }
    for (int yIndex = Math.max(0, y - heroPower); yIndex < Math.min(params.height(), y + heroPower); yIndex++) {
      map[x][yIndex].incrementHeat(heat);
    }
  }

  private void applyItemHeats(List<Item> items) {
    items.forEach(item -> {
      switch (item.type()) {
        case Item.ITEM_TYPE_POWER:
          map[item.position().x()][item.position().y()].incrementHeat(Heats.ITEM_POWER);
          break;
        case Item.ITEM_TYPE_AMOUNT:
          map[item.position().x()][item.position().y()].incrementHeat(Heats.ITEM_AMOUNT);
          break;
      }
    });
  }

  private void applyRivalsHeats(List<Bomber> rivals) {
    int rivalInfluence = 2;
    rivals.forEach(rival -> {
      for (int xIndex = Math.max(0, rival.position().x() - rivalInfluence); xIndex < Math.min(params.width(), rival.position().x() + rivalInfluence); xIndex++) {
        for (int yIndex = Math.max(0, rival.position().y() - rivalInfluence); yIndex < Math.min(params.height(), rival.position().y() + rivalInfluence); yIndex++) {
          map[xIndex][yIndex].incrementHeat(Heats.RIVAL);
        }
      }
    });
  }

  private void applyBombHeats(List<Bomb> bombs) {
    bombs.forEach(bomb -> {
      for (int xIndex = Math.max(0, bomb.position().x() - bomb.power()); xIndex < Math.min(params.width(), bomb.position().x() + bomb.power()); xIndex++) {
        map[xIndex][bomb.position().y()].incrementHeat(Heats.BOMB);
      }
      for (int yIndex = Math.max(0, bomb.position().y() - bomb.power()); yIndex < Math.min(params.height(), bomb.position().y() + bomb.power()); yIndex++) {
        map[bomb.position().x()][yIndex].incrementHeat(Heats.BOMB);
      }
    });
  }

  //2*heat - dist?
  public Point bestBombTarget(Point from) {
    Point target = new Point(0,0);
    int points = Integer.MIN_VALUE;
    for (int x = 0; x < params.width(); x++) {
      for (int y = 0; y < params.height(); y++) {
        int distToCell = Math.abs(from.x() - x) + Math.abs(from.y() - y);
        int pts = 2 * map[x][y].heat() - distToCell;
        if (pts > points) {
          points = pts;
          target.setPoint(x, y);
        }
      }
    }
    return target;
  }
}

class Point {

  private int x;
  private int y;

  Point(int x, int y) {
    setPoint(x, y);
  }

  Point(Scanner scanner) {
    this(scanner.nextInt(), scanner.nextInt());
  }

  public void setPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point point = (Point) o;

    if (x != point.x) return false;
    return y == point.y;

  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }
}

