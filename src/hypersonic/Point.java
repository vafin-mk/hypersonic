package hypersonic;

import java.util.Scanner;

public class Point {

  private int x;
  private int y;

  Point(int x, int y) {
    setPoint(x, y);
  }

  Point(Scanner scanner) {
    this(scanner.nextInt(), scanner.nextInt());
  }

  public void setPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int x() {
    return x;
  }

  public int y() {
    return y;
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
}
