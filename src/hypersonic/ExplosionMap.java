package hypersonic;

import java.util.ArrayList;
import java.util.List;

public class ExplosionMap {

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
