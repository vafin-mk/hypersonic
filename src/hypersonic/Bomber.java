package hypersonic;

public class Bomber {

  static final int ENTITY_TYPE = 0;

  Coord coord = new Coord(0, 0);
  int bombs;
  int range;
  int points;
  int updateRound;//bomber dead if this < world.round

  void update(Coord coord, int bombs, int range, int updateRound) {
    this.coord.update(coord);
    this.bombs = bombs;
    this.range = range;
    this.updateRound = updateRound;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Bomber bomber = (Bomber) o;

    if (bombs != bomber.bombs) return false;
    if (range != bomber.range) return false;
    if (points != bomber.points) return false;
    return coord != null ? coord.equals(bomber.coord) : bomber.coord == null;

  }

  @Override
  public int hashCode() {
    int result = coord != null ? coord.hashCode() : 0;
    result = 31 * result + bombs;
    result = 31 * result + range;
    result = 31 * result + points;
    return result;
  }
}
