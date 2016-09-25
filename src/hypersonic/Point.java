package hypersonic;

import java.util.Scanner;

public class Point {
  int x;
  int y;

  Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  Point(Scanner scanner) {
    this(scanner.nextInt(), scanner.nextInt());
  }

  public int distanceTo(Point other) {
    return Math.abs(other.x - x) + Math.abs(other.y - y);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point point = (Point) o;

    if (x != point.x) return false;
    return y == point.y;

  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return "Point("+x+"|"+y+")";
  }
}
