package hypersonic;

import java.util.*;

public class TileMap {
  
  private Tile[][] tiles = new Tile[Constants.WIDTH][Constants.HEIGHT];

  private Entity hero;
  private Entity rival;
  private Set<Entity> bombs = new HashSet<>();
  private Set<Entity> items = new HashSet<>();
  private List<Tile> possibleMoves = new ArrayList<>();
  private PossibleMoveComparator moveComparator = new PossibleMoveComparator();

  private int boxCount;
  private int remainingBox;
  private boolean firstRun;

  TileMap() {
    for (int x = 0; x < Constants.WIDTH; x++) {
      for (int y = 0; y < Constants.HEIGHT; y++) {
        tiles[x][y] = new Tile(new Point(x, y));
      }
    }
  }
  
  public void updateMap(Scanner scanner) {
    for (int x = 0; x < Constants.WIDTH; x++) {
      for (int y = 0; y < Constants.HEIGHT; y++) {
        tiles[x][y] = new Tile(new Point(x, y));
      }
    }

    for (int y = 0; y < Constants.HEIGHT; y++) {
      char[] row = scanner.nextLine().toCharArray();
      for (int x = 0; x < row.length; x++) {
        char cell = row[x];
        switch (cell) {
          case '.':
            tiles[x][y].moveable = true;
            tiles[x][y].stopExplosion = false;
            break;
          case '0':
          case '1':
          case '2':
            tiles[x][y].moveable = false;
            tiles[x][y].stopExplosion = true;
            if (cell == '0') {
              tiles[x][y].boxType = Constants.ITEM_EMPTY;
            } else if (cell == '1') {
              tiles[x][y].boxType = Constants.ITEM_POWER;
            } else if (cell == '2') {
              tiles[x][y].boxType = Constants.ITEM_AMOUNT;
            }
            remainingBox++;
            if (firstRun) {
              boxCount++;
            }
            break;
          case 'X':
            tiles[x][y].moveable = false;
            tiles[x][y].stopExplosion = true;
            tiles[x][y].destructible = false;
            break;
          default:
            throw new IllegalStateException();
        }
      }
    }
    
    updateEntities(scanner);
    updatePossibleMoves();
    updateMoveMagnetisms();
    moveComparator.heroPosition = hero.position;
    possibleMoves.sort(moveComparator);

    firstRun = false;
  }
  
  private void updateEntities(Scanner scanner) {
    bombs.clear();
    items.clear();

    int entities = scanner.nextInt();
    for (int i = 0; i < entities; i++) {
      int entityType = scanner.nextInt();
      int owner = scanner.nextInt();
      Point position = new Point(scanner);
      int param1 = scanner.nextInt();
      int param2 = scanner.nextInt();
      Entity entity = new Entity(entityType, position, param1, param2);
      switch (entityType) {
        case Constants.ENTITY_BOMBER:
          if (owner == Constants.TEAM_ID) {
            hero = entity;
          } else {
            rival = entity;
          }
          break;
        case Constants.ENTITY_BOMB:
          addBomb(entity);
          break;
        case Constants.ENTITY_ITEM:
          addItem(entity);
          break;
        default:
          throw new IllegalStateException();
      }
    }
  }

  private void updatePossibleMoves() {
    possibleMoves.clear();
    checkTileAndAdd(hero.position.x, hero.position.y);
    //clear duplicates
//    Set<Tile> set = new HashSet<>(possibleMoves);
//    possibleMoves.clear();
//    possibleMoves.addAll(set);
//    System.err.println("POSSIBLE " + Arrays.toString(possibleMoves.toArray()));
  }

  private void updateMoveMagnetisms() {

    for (Tile move : possibleMoves) {
      int magnetism = 0;
      if (move.dangerous) {
        magnetism -= 1000;
      }
      for (Entity item : items) {
        if (item.position.equals(move.pos)) {
          if (item.param1 == Constants.ITEM_POWER) {
            magnetism += 5;
          } else if (item.param1 == Constants.ITEM_AMOUNT) {
            magnetism += 3;
          }
        }
      }
      //room for algorithm improvement
      for (Tile explode : getAllExplodingTiles(move.pos.x, move.pos.y, hero.param2)) {
        switch (explode.boxType) {
          case Constants.ITEM_EMPTY:
            magnetism += 3;
            break;
          case Constants.ITEM_AMOUNT:
            magnetism += 4;
            break;
          case Constants.ITEM_POWER:
            magnetism += 5;
            break;
        }
      }

      if (getRivalTiles(3).contains(move)) {
        if (remainingBox > 0) {
          magnetism -= 10;
        } else {
          //todo run if my boxcount > rivals count
          //box end -> let's hunt
          magnetism += 1000;
        }
      }

      move.magnetism = magnetism;
    }
  }

  private void checkTileAndAdd(int x, int y) {
    if (x < 0 || y < 0 || x >= Constants.WIDTH || y >= Constants.HEIGHT) {
      return;
    }
    Tile tile = tiles[x][y];
    if (possibleMoves.contains(tiles[x][y]) || (!tile.moveable && !tile.pos.equals(hero.position))) {
      return;
    }
    possibleMoves.add(tile);
    checkTileAndAdd(x - 1, y);
    checkTileAndAdd(x + 1, y);
    checkTileAndAdd(x, y - 1);
    checkTileAndAdd(x, y + 1);
  }

  //add chain reaction handle
  private void addBomb(Entity bomb) {
    tiles[bomb.position.x][bomb.position.y].moveable = false;
    bombs.add(bomb);
    for (Tile tile : getAllExplodingTiles(bomb.position.x, bomb.position.y, bomb.param2)) {
      tile.dangerous = true;
      tile.stopExplosion = true;
      if (bomb.param1 <= 2){
        tile.moveable = false;
      }
    }
  }

  private void addItem(Entity item) {
    items.add(item);
    tiles[item.position.x][item.position.y].stopExplosion = true;
  }

  private Set<Tile> getAllExplodingTiles(int x, int y, int power) {
    Set<Tile> result = new HashSet<>();
    result.add(tiles[x][y]);
    for (int i = x + 1; i < Math.min(x + power, Constants.WIDTH); i++) {//TO RIGHT
      Tile tile = tiles[i][y];
      if (!tile.destructible) {
        break;
      }
      result.add(tile);
      if (tile.stopExplosion) {
        break;
      }
    }
    for (int i = x - 1; i > Math.max(x - power, -1); i--) {//TO LEFT
      Tile tile = tiles[i][y];
      if (!tile.destructible) {
        break;
      }
      result.add(tile);
      if (tile.stopExplosion) {
        break;
      }
    }
    for (int i = y + 1; i < Math.min(y + power, Constants.HEIGHT); i++) {//TO Bottom
      Tile tile = tiles[x][i];
      if (!tile.destructible) {
        break;
      }
      result.add(tile);
      if (tile.stopExplosion) {
        break;
      }
    }
    for (int i = y - 1; i > Math.max(y - power, -1); i--) {//TO UP
      Tile tile = tiles[x][i];
      if (!tile.destructible) {
        break;
      }
      result.add(tile);
      if (tile.stopExplosion) {
        break;
      }
    }
    return result;
  }

  private Set<Tile> getRivalTiles(int radius) {
    Set<Tile> result = new HashSet<>();
    int x = rival.position.x;
    int y = rival.position.y;
    int startX = Math.max(0, x - radius);
    int endX = Math.min(Constants.WIDTH, x + radius);
    int startY = Math.max(0, y - radius);
    int endY = Math.min(Constants.HEIGHT, y + radius);

    for (int x1 = startX; x1 < endX; x1++) {
      for (int y1 = startY; y1 < endY; y1++) {
        result.add(tiles[x1][y1]);
      }
    }

    return result;
  }

  //add check possible moves after bomb
  public boolean canBombNow() {
    List<Tile> predictSave = new ArrayList<>(possibleMoves);
    predictSave.removeAll(getAllExplodingTiles(hero.position.x, hero.position.y, hero.param2));
    List<Tile> bestOf = new ArrayList<>(possibleMoves.subList(0, Math.min(possibleMoves.size(), hero.param1)));
//    System.err.println(hero.param1 + "\n" + Arrays.toString(predictSave.toArray()) + "\n" + Arrays.toString(possibleMoves.toArray()));
    return hero.param1 > 0 && !predictSave.isEmpty() && bestOf.contains(tiles[hero.position.x][hero.position.y]);
  }

  public Point findBestPosition() {
    System.err.println("BEST POS:" + possibleMoves.get(0));
    if (possibleMoves.size() > 1) {
      System.err.println("SECOND BEST:" + possibleMoves.get(1));
    } else {
      System.err.println("NO SECOND BEST");
    }
    if (possibleMoves.get(0).pos.equals(hero.position) && possibleMoves.size() > 1) {
      return possibleMoves.get(1).pos;
    }
    return possibleMoves.get(0).pos;
  }

  public Point findBestPositionWithBomb() {
    System.err.println("WITH BOMB");
    possibleMoves.removeAll(getAllExplodingTiles(hero.position.x, hero.position.y, hero.param2));
    return findBestPosition();
  }
}
