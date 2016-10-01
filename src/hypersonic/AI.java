package hypersonic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AI {
  
  Grid grid;

  AI(Grid grid) {
    this.grid = grid;
  }

  boolean decision() {
    Tile item = closestItemTile(4);
    Tile bomb = bestBombTile();
    Tile safe = closestSafeTile();
    System.err.println(String.format("ITEM:%s\nBOMB:%s\nSAFE=%s", item, bomb, safe));
    Hero hero = grid.myHero();

    if (item == null && bomb == null && safe == null) {
      return stay("whoops");
    }
    if (item != null) {
      boolean canGo = true;
      for (Tile tile : grid.tilesIn(grid.tiles[hero.col][hero.row], 2)) {
        if (grid.boomMap[tile.col][tile.row] != Bomb.NO_EXPLODE) {
          canGo = false;
          break;
        }
      }
      if (canGo) {
        return move(item, String.format("item - %s/%s", item.col, item.row));
      }
    }

    if (bomb != null) {
      if (hero.col != bomb.col || hero.row != bomb.row) {
        return move(bomb, String.format("bomb - %s/%s", bomb.col, bomb.row));
      }
      if (hero.bombs > 0) {
        return bomb(bomb, String.format("bomb - %s/%s", bomb.col, bomb.row));
      }
    }

    if (safe != null) {
      return move(safe, String.format("safe - %s/%s", safe.col, safe.row));
    }

    return stay("" + (200 - grid.round));
  }

  Tile closestItemTile(int maxScanRadius) {
    List<Tile> items = new ArrayList<>(grid.moves)
        .stream()
        .filter(tile -> tile.type == Tile.Type.ITEM
            && tile.range < maxScanRadius)
        .collect(Collectors.toList());
    items.sort((tile1,tile2) -> tile1.range - tile2.range);
    if (items.isEmpty()) {
      return null;
    }
//    grid.debugList(items, 5, "ITEMS");
    return items.get(0);
  }

  Tile bestBombTile() {
    List<Tile> tiles = new ArrayList<>(grid.moves)
        .stream()
        .filter(tile -> grid.canBomb(tile))
        .collect(Collectors.toList());
    tiles.sort((tile1, tile2) -> {
      return (2 * grid.findExplodedBoxesCount(tile2) - tile2.range) - (2 * grid.findExplodedBoxesCount(tile1) - tile1.range);
    });
    if (tiles.isEmpty()) {
      return null;
    }
//    grid.debugList(tiles, 5, "BOMB TILES");
    return tiles.get(0);
  }

  Tile closestSafeTile() {
    List<Tile> safe = new ArrayList<>(grid.moves)
        .stream()
        .filter(move -> !grid.haveExplosionDanger(move))
        .collect(Collectors.toList());

    safe.sort((move1, move2) -> {
      int move1Boom = grid.boomMap[move1.col][move1.row];
      int move2Boom = grid.boomMap[move2.col][move2.row];
      if (move1Boom != move2Boom) {
        return move2Boom - move1Boom;
      }
      return move1.range - move2.range;
    });
    if (safe.isEmpty()) {
      return null;
    }
//    grid.debugList(safe, 5, "SAFE TILES");
    return safe.get(0);
  }

  //COMMANDS

  boolean stay(String message) {
    Hero my = grid.myHero();
    return move(my.col, my.row, message);
  }

  boolean stay() {
    return stay("");
  }

  boolean move(int col, int row, String message) {
    System.out.println(String.format("MOVE %s %s %s", col, row, message));
    return true;
  }

  boolean move(int col, int row) {
    return move(col, row, "");
  }

  boolean move(Tile target, String msg) {
    return move(target.col, target.row, msg);
  }

  boolean move(Tile tile) {
    return move(tile, tile.toString());
  }

  boolean bomb(int col, int row, String message) {
    System.out.println(String.format("BOMB %s %s %s", col, row, message));
    return true;
  }

  boolean bomb(int col, int row) {
    return bomb(col, row, "");
  }

  boolean bomb(Tile tile, String msg) {
    return bomb(tile.col, tile.row, msg);
  }

  boolean bomb(Tile tile) {
    return bomb(tile, tile.toString());
  }
}
