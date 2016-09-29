package hypersonic;

import java.util.Scanner;

public class Coord {
  int x;
  int y;

  Coord(int x, int y) {
    update(x, y);
  }

  void update(int x, int y) {
    this.x = x;
    this.y = y;
  }

  void update(Coord other) {
    this.x = other.x;
    this.y = other.y;
  }

  int distance(Coord other) {
    return Math.abs(x - other.x) + Math.abs(y - other.y);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Coord that = (Coord) o;

    if (x != that.x) return false;
    return y == that.y;

  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return x +"|" + y;
  }
}
