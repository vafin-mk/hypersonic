package hypersonic;

public class Tile {
  Point pos;

  boolean moveable = true;
  boolean stopExplosion = false;
  boolean destructible = true;
  boolean dangerous = false;
  int boxType = -1;

  int magnetism;
  Tile(Point pos) {
    this.pos = pos;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Tile tile = (Tile) o;

    return pos.equals(tile.pos);
  }

  @Override
  public int hashCode() {
    return pos.hashCode();
  }

  @Override
  public String toString() {
    return "Tile:" + pos.toString() + "; magnet=" + magnetism;
  }
}
