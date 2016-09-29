package hypersonic;

import java.util.*;

public class World {

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
