package hypersonic;

class Tile {

  enum Type {
    FLOOR, BOX, BOMB, WALL, ITEM
  }

  int col;
  int row;
  Type type;

  int range = -1;//in rounds to hero, -1 not available

  Tile(int col, int row) {
    this.col = col;
    this.row = row;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Tile tile = (Tile) o;

    if (col != tile.col) return false;
    if (row != tile.row) return false;
    return type == tile.type;

  }

  @Override
  public int hashCode() {
    int result = col;
    result = 31 * result + row;
    result = 31 * result + type.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("(pos(%s/%s) - %s - range to hero %s", col, row, type, range == -1 ? "XX" : range);
  }
}
