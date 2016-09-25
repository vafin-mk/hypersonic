package hypersonic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Map {

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
