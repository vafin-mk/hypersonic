package hypersonic;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;

public class Tile {

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
