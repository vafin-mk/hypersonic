package hypersonic;

public class Bomb {

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
