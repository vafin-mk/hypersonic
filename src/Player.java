import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.*;
import java.util.List;
import java.util.EnumSet;

class Player { public static void main(String[] args) { new Engine().start();}}
class AI {

  enum Strategy {
    BOX_HUNTER, //normal
    AGGRO_PALADIN, //aggro paladin
    RUNNER //escaper
  }

  World world;

  int scanRange = 6;
  Strategy strategy = Strategy.BOX_HUNTER;

  AI(World world) {
    this.world = world;
  }

  void makeDecision() {
    List<Tile> available = new ArrayList<>();
    world.buildAvailableTiles(scanRange, available);
    world.calculateWeigths(available, strategy);
    if (available.isEmpty()) {
      System.err.println("STAY WE YOU ARE!");
      stay();
      return;
    }
    available.sort((o1, o2) -> {
      int weightDelta = o2.weight - o1.weight;
      if (weightDelta != 0) {
        return weightDelta;
      }
      int distDelta = o1.coord.distance(world.hero.coord) - o2.coord.distance(world.hero.coord);
      if (distDelta != 0) {
        return distDelta;
      }
      if (o1.coord.x != o2.coord.x) {
        return o1.coord.x - o2.coord.x;
      } else {
        return o1.coord.y - o2.coord.y;
      }
    });
    System.err.println("AVAILABLE:" + Arrays.toString(available.toArray()));
    switch (strategy) {
      case BOX_HUNTER:
        if (boxDecision(available)) {
          return;
        }
        break;
      case AGGRO_PALADIN:
        if (aggroDecision(available)) {
          return;
        }
        break;
      case RUNNER:
        if (runnerDecision(available)) {
          return;
        }
        break;
    }
    System.err.println("HOUSTON WE HAVE A PROBLEM; STAY WHERE YOU ARE");
    stay();
  }

  boolean boxDecision(List<Tile> available) {
    for (Tile tile : available) {
      if (world.explosionMap.map[tile.coord.x][tile.coord.y] != ExplosionMap.NO_EXPLODE) {
        continue;
      }
      if (tile.type == Tile.Type.ITEM_RANGE || tile.type == Tile.Type.ITEM_AMOUNT) {
        System.err.println("GOING AFTER ITEM");
        move(tile.coord);
        return true;
      }
      if (world.canPlaceBomb(tile.coord, available)) {
        if (world.hero.bombs > 0) {
          if (tile.coord.equals(world.hero.coord)) {
            System.err.println("BOMBING NOW");
            bomb(tile.coord);//todo move to another poi immediately
            return true;
          } else {
            System.err.println("GOING TO PLACE BOMB AT " + tile.coord);
            move(tile.coord);
            return true;
          }
        }
      }
    }
    System.err.println("LOOKING FOR SAFE PLACE");
    for (Tile tile : available) {
      if (world.explosionMap.map[tile.coord.x][tile.coord.y] == ExplosionMap.NO_EXPLODE) {
        System.err.println("GOING TO SAFE PLACE:" + tile.coord);
        move(tile.coord);
        return true;
      }
    }
    return false;
  }

  boolean aggroDecision(List<Tile> available) {
    for (Tile tile : available) {
      if (world.explosionMap.map[tile.coord.x][tile.coord.y] != ExplosionMap.NO_EXPLODE || !haveEnemyInRange(tile.coord, 6)) {
        continue;
      }
      if (world.canPlaceBomb(tile.coord, available)) {
        if (world.hero.bombs > 0) {
          if (tile.coord.equals(world.hero.coord) || world.hero.bombs > 1 && !haveBombInRange(tile.coord, 2)) {
            System.err.println("BOMBING NOW");
            bomb(tile.coord);//todo move to another poi immediately
            return true;
          } else {
            System.err.println("GOING TO PLACE BOMB AT " + tile.coord);
            move(tile.coord);
            return true;
          }
        }
      }
    }
    return false;
  }

  boolean runnerDecision(List<Tile> available) {
    Tile safest = null;
    int maxDist = 0;
    for (Tile tile : available) {
      if (world.explosionMap.map[tile.coord.x][tile.coord.y] == ExplosionMap.NO_EXPLODE) {
        int distToHero = tile.coord.distance(world.hero.coord);
        if (distToHero > maxDist) {
          maxDist = distToHero;
          safest = tile;
        }
      }
    }

    if (safest != null) {
//      if (world.canPlaceBomb(world.hero.coord, available) && world.hero.bombs > 0 && haveEnemyInRange(world.hero.coord, 6)) {
//        bomb(safest.coord);
//        return true;
//      } else {
        System.err.println("GOING TO SAFE PLACE:" + safest.coord);
        move(safest.coord);
//        return true;
//      }
    }
    return false;
  }

  boolean haveEnemyInRange(Coord lookCoord, int radius) {
    boolean haveEnemyInRange = false;
    for (Bomber rival : world.rivals.values()) {
      if (rival.updateRound == world.round && rival.coord.distance(lookCoord) < radius) {
        haveEnemyInRange = true;
        break;
      }
    }
    return haveEnemyInRange;
  }

  boolean haveBombInRange(Coord lookCoord, int radius) {
    boolean haveBombInRange = false;
    for (Bomb bomb : world.bombs) {
      if (bomb.coord.distance(lookCoord) < radius) {
        haveBombInRange = true;
        break;
      }
    }
    return haveBombInRange;
  }

  private void stay() {
    move(world.hero.coord);
  }

  private void move(Coord coord) {
    System.out.println("MOVE " + coord.x + " " + coord.y);
  }

  private void bomb(Coord coord) {
    System.out.println("BOMB " + coord.x + " " + coord.y);
  }
}

class Bomb {

  static final int ENTITY_TYPE = 1;

  Coord coord = new Coord(0, 0);
  int countdown;
  int range;
  int owner;

  Bomb(Coord coord, int owner, int countdown, int range) {
    this.coord = coord;
    this.owner = owner;
    this.countdown = countdown;
    this.range = range;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Bomb bomb = (Bomb) o;

    return coord != null ? coord.equals(bomb.coord) : bomb.coord == null;

  }

  @Override
  public int hashCode() {
    return coord != null ? coord.hashCode() : 0;
  }
}

class Bomber {

  static final int ENTITY_TYPE = 0;

  Coord coord = new Coord(0, 0);
  int bombs;
  int range;
  int points;
  int updateRound;//bomber dead if this < world.round

  void update(Coord coord, int bombs, int range, int updateRound) {
    this.coord.update(coord);
    this.bombs = bombs;
    this.range = range;
    this.updateRound = updateRound;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Bomber bomber = (Bomber) o;

    if (bombs != bomber.bombs) return false;
    if (range != bomber.range) return false;
    if (points != bomber.points) return false;
    return coord != null ? coord.equals(bomber.coord) : bomber.coord == null;

  }

  @Override
  public int hashCode() {
    int result = coord != null ? coord.hashCode() : 0;
    result = 31 * result + bombs;
    result = 31 * result + range;
    result = 31 * result + points;
    return result;
  }
}

class Coord {
  int x;
  int y;

  Coord(int x, int y) {
    update(x, y);
  }

  void update(int x, int y) {
    this.x = x;
    this.y = y;
  }

  void update(Coord other) {
    this.x = other.x;
    this.y = other.y;
  }

  int distance(Coord other) {
    return Math.abs(x - other.x) + Math.abs(y - other.y);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Coord that = (Coord) o;

    if (x != that.x) return false;
    return y == that.y;

  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return x +"|" + y;
  }
}

class Engine {

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

class ExplosionMap {

  static int NO_EXPLODE = Integer.MAX_VALUE;
  static int EXPLODE_NEXT_TURN = 1;

  enum Direction {
    LEFT, UP, RIGHT, DOWN
  }

  World world;
  //0 - no explosion, >0 rounds to explode
  int[][] map;

  ExplosionMap(World world) {
    this.world = world;
    this.map = new int[world.width][world.height];
  }

  void update(List<Bomb> bombs) {
    for (int x = 0; x < world.width; x++) {
      for (int y = 0; y < world.height; y++) {
        map[x][y] = NO_EXPLODE;
      }
    }

    for (Bomb bomb : bombs) {
      List<Coord> exploders = explodingMap(bomb.coord, bomb.range);
      for (Bomb chained : bombs) {
        if (exploders.contains(chained.coord)) {
          chained.countdown = Math.min(bomb.countdown, chained.countdown);
        }
      }
      for (Coord explode : exploders) {
        map[explode.x][explode.y] = Math.min(bomb.countdown, map[explode.x][explode.y]);
      }
    }
  }

  List<Coord> explodingMap(Coord coord, int bombPower) {
    List<Coord> exploders = new ArrayList<>();
    exploders.addAll(explodingWave(coord, Direction.LEFT, bombPower));
    exploders.addAll(explodingWave(coord, Direction.UP, bombPower));
    exploders.addAll(explodingWave(coord, Direction.RIGHT, bombPower));
    exploders.addAll(explodingWave(coord, Direction.DOWN, bombPower));
    return exploders;
  }

  List<Coord> explodingWave(Coord coord, Direction direction, int bombPower) {
    List<Coord> explodingCoords = new ArrayList<>();
    explodingCoords.add(coord);
    switch (direction) {
      case LEFT:
        for (int x = coord.x - 1; x >= Math.max(0, coord.x - bombPower); x--) {
          Tile tile = world.map[x][coord.y];
          if (Tile.EXPLODE.contains(tile.type)) {
            explodingCoords.add(new Coord(x, coord.y));
            break;
          }
          explodingCoords.add(new Coord(x, coord.y));
        }
        break;
      case UP:
        for (int y = coord.y - 1; y >= Math.max(0, coord.y - bombPower); y--) {
          Tile tile = world.map[coord.x][y];
          if (Tile.EXPLODE.contains(tile.type)) {
            explodingCoords.add(new Coord(coord.x, y));
            break;
          }
          explodingCoords.add(new Coord(coord.x, y));
        }
        break;
      case RIGHT:
        for (int x = coord.x + 1; x < Math.min(world.width, coord.x + bombPower); x++) {
          Tile tile = world.map[x][coord.y];
          if (Tile.EXPLODE.contains(tile.type)) {
            explodingCoords.add(new Coord(x, coord.y));
            break;
          }
          explodingCoords.add(new Coord(x, coord.y));
        }
        break;
      case DOWN:
        for (int y = coord.y + 1; y < Math.min(world.height, coord.y + bombPower); y++) {
          Tile tile = world.map[coord.x][y];
          if (Tile.EXPLODE.contains(tile.type)) {
            explodingCoords.add(new Coord(coord.x, y));
            break;
          }
          explodingCoords.add(new Coord(coord.x, y));
        }
        break;
    }
    return explodingCoords;
  }
}

class Tile {

  enum Type {
    FLOOR, BOX, BOX_RANGE, BOX_AMOUNT, WALL, BOMB, ITEM_RANGE, ITEM_AMOUNT
  }

  static EnumSet<Type> PASSABLE = EnumSet.of(Type.FLOOR, Type.ITEM_RANGE, Type.ITEM_AMOUNT);
  static EnumSet<Type> NON_PASSABLE = EnumSet.of(Type.BOX, Type.BOX_RANGE, Type.BOX_AMOUNT, Type.WALL, Type.BOMB);
  static EnumSet<Type> EXPLODE = EnumSet.of(Type.BOX, Type.BOX_RANGE, Type.BOX_AMOUNT, Type.WALL, Type.BOMB, Type.ITEM_AMOUNT, Type.ITEM_RANGE);
  static EnumSet<Type> BOXES = EnumSet.of(Type.BOX, Type.BOX_RANGE, Type.BOX_AMOUNT);

  Coord coord;
  Type type = Type.FLOOR;
  int weight;
  List<Tile> adjTiles = new ArrayList<>();

  Tile(Coord coord) {
    this.coord = coord;
  }

  void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return String.format("Coord:(%s); type=%s; weight=%s", coord, type, weight);
  }
}

class World {

  int width;
  int height;
  int teamId;
  Tile[][] map;
  ExplosionMap explosionMap;

  Bomber hero = new Bomber();
  Map<Integer, Bomber> rivals = new HashMap<>();
  List<Bomb> bombs = new ArrayList<>();

  int boxesCount = 100000;
  int remainingBoxes = 0;
  int round = 0;

  void getWorldParameters(Scanner scanner) {
    width = scanner.nextInt();
    height = scanner.nextInt();
    teamId = scanner.nextInt();
    map = new Tile[width][height];
    explosionMap = new ExplosionMap(this);
  }

  void updateTiles(Scanner scanner) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        map[x][y] = new Tile(new Coord(x, y));
      }
    }

    int counter = 0;
    for (int y = 0; y < height; y++) {
      char[] row = scanner.next().toCharArray();
      for (int x = 0; x < row.length; x++) {
        char cell = row[x];
        switch (cell) {
          case '.':
            map[x][y].setType(Tile.Type.FLOOR);
            break;
          case '0':
            map[x][y].setType(Tile.Type.BOX);
            counter++;
            break;
          case '1':
            map[x][y].setType(Tile.Type.BOX_RANGE);
            counter++;
            break;
          case '2':
            map[x][y].setType(Tile.Type.BOX_AMOUNT);
            counter++;
            break;
          case 'X':
            map[x][y].setType(Tile.Type.WALL);
            break;
          default:
            System.err.println("UNKNOWN CELL TYPE:" + cell);
        }
      }
    }
    if (boxesCount == 100000) {
      boxesCount = counter;
    }
    remainingBoxes = counter;
  }

  void updateEntities(Scanner scanner) {
    bombs.clear();
    int entities = scanner.nextInt();
    for (int i = 0; i < entities; i++) {
      int entityType = scanner.nextInt();
      int owner = scanner.nextInt();
      int x = scanner.nextInt();
      int y = scanner.nextInt();
      int param1 = scanner.nextInt();
      int param2 = scanner.nextInt();

      switch (entityType) {
        case Bomber.ENTITY_TYPE:
          Coord coord = new Coord(x, y);
          if (owner == teamId) {
            hero.update(coord, param1, param2, round);
          } else {
            rivals.putIfAbsent(owner, new Bomber());
            rivals.get(owner).update(coord, param1, param2, round);
          }
          break;
        case Bomb.ENTITY_TYPE:
          bombs.add(new Bomb(new Coord(x, y), owner, param1, param2));
          map[x][y].setType(Tile.Type.BOMB);
          break;
        case 2://item
          map[x][y].setType(param1 == 1 ? Tile.Type.ITEM_RANGE : Tile.Type.ITEM_AMOUNT);
          break;
        default:
          System.err.println("UNKNOWN ENTITY TYPE:" + entityType);
      }
    }
  }

  void updateExplosionMap() {
    explosionMap.update(bombs);

    for (Bomb bomb : bombs) {
      if (explosionMap.map[bomb.coord.x][bomb.coord.y] != ExplosionMap.EXPLODE_NEXT_TURN) {
        continue;
      }

      explosionMap.explodingMap(bomb.coord, bomb.range).stream().filter(coord -> Tile.BOXES.contains(map[coord.x][coord.y].type)).forEach(coord -> {
        if (bomb.owner == teamId) hero.points++;
        else rivals.get(bomb.owner).points++;
      });
    }
  }

  void updateTileLinks() {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Tile tile = map[x][y];
        tile.adjTiles.clear();
        addAdjacent(tile, x - 1, y);
        addAdjacent(tile, x + 1, y);
        addAdjacent(tile, x, y - 1);
        addAdjacent(tile, x, y + 1);
      }
    }
  }

  void addAdjacent(Tile tile, int adjX, int adjY) {
    if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height) {
      return;
    }
    Tile adjTile = map[adjX][adjY];
    if (Tile.NON_PASSABLE.contains(adjTile.type)) {
      return;
    }
    if (tile.adjTiles.contains(adjTile)) {
      System.err.println("Already have " + adjTile);
    }
    tile.adjTiles.add(adjTile);
  }

  void buildAvailableTiles(int scanRadius, List<Tile> tiles) {
    int turn = 1;
    Tile currTile = map[hero.coord.x][hero.coord.y];
    tiles.add(currTile);
    for (Tile tile : currTile.adjTiles) {
      addIfAvailable(turn+1, scanRadius, tiles, tile);
    }
  }

  void addIfAvailable(int turn, int maxTurn, List<Tile> tiles, Tile tile) {
//    System.err.println(turn + "||" + tile.coord + "||" + explosionMap.map[tile.coord.x][tile.coord.y]);
    if (turn >= maxTurn) {
      return;
    }
    if (tiles.contains(tile)) {
      return;
    }
    if (explosionMap.map[tile.coord.x][tile.coord.y] == turn) {
      return;
    }

    tiles.add(tile);
    for (Tile adj : tile.adjTiles) {
      addIfAvailable(turn+1, maxTurn, tiles, adj);
    }
  }

  void calculateWeigths(List<Tile> tiles, AI.Strategy strategy) {
    for (Tile tile : tiles) {
      switch(tile.type) {
        case ITEM_AMOUNT:
          tile.weight += strategy == AI.Strategy.BOX_HUNTER ? 3 : 1;
          break;
        case ITEM_RANGE:
          tile.weight += strategy == AI.Strategy.BOX_HUNTER ? 4 : 1;
          break;
      }
      if (strategy == AI.Strategy.BOX_HUNTER) {
        tile.weight += bombPlaceWeights(tile.coord);
        tile.weight += tile.adjTiles.size();
        tile.weight -= tile.coord.distance(hero.coord);
      } else if (strategy == AI.Strategy.AGGRO_PALADIN) {
        rivals.values().stream().filter(rival -> rival.updateRound == round).forEach(rival -> {
          switch (rival.coord.distance(tile.coord)){
            case 6:
            case 5:
              tile.weight += 20;
            case 4:
            case 3:
              tile.weight += 20;
            case 2:
            case 1:
              tile.weight += 20;
            case 0:
              tile.weight += 20;

          }
        });
      } else if (strategy == AI.Strategy.RUNNER) {
        rivals.values().stream().filter(rival -> rival.updateRound == round && rival.coord.distance(tile.coord) <= 6).forEach(rival -> {
          switch (rival.coord.distance(tile.coord)){
            case 6:
            case 5:
              tile.weight -= 20;
            case 4:
            case 3:
              tile.weight -= 20;
            case 2:
            case 1:
              tile.weight -= 20;
            case 0:
              tile.weight -= 20;

          }
        });
      }
    }
  }

  int bombPlaceWeights(Coord coord) {
    int weight = 0;
    for (Coord explodeCoord : explosionMap.explodingMap(coord, hero.range)) {
      switch (map[explodeCoord.x][explodeCoord.y].type) {
        case BOX:
          weight += 2;
          break;
        case BOX_AMOUNT:
          weight += 3;
          break;
        case BOX_RANGE:
          weight += 3;
          break;
      }
    }
    return weight;
  }

  boolean canPlaceBomb(Coord placeCoord, List<Tile> tiles) {
    List<Coord> predicter = new ArrayList<>();
    tiles.forEach(tile -> predicter.add(tile.coord));
    predicter.removeAll(explosionMap.explodingMap(placeCoord, hero.range));
    return !predicter.isEmpty();
  }

  Bomber getLeader() {
    Bomber leader = hero;
    for (Bomber rival : rivals.values()) {
      if (rival.updateRound < round) {
        continue;
      }
      if (rival.points > leader.points) {
        leader = rival;
      }
    }
    return leader;
  }

}

