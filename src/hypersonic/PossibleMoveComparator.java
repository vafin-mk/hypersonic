package hypersonic;

import java.util.Comparator;

public class PossibleMoveComparator implements Comparator<Tile> {

  Point heroPosition;

  @Override
  public int compare(Tile tile1, Tile tile2) {
    if (tile2.dangerous) {
      return -1;
    }
    if (tile1.dangerous) {
      return 1;
    }
    int distToFirst = tile1.pos.distanceTo(heroPosition);
    int distToSecond = tile2.pos.distanceTo(heroPosition);

    int firstPts = tile1.magnetism - distToFirst;
    int secondPts = tile2.magnetism - distToSecond;
    if (firstPts > secondPts) {
      return -1;
    } else if (secondPts > firstPts) {
      return 1;
    }
    return 0;
  }
}
