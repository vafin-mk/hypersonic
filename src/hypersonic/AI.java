package hypersonic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AI {

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
      System.err.println("STAY WHERE YOU ARE");
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
