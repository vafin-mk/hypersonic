package hypersonic;

public class Bomber {

  private Point position;
  private int amount;
  private int power = 3;

  Bomber(Point position, int amount, int power) {
    set(position, amount, power);
  }

  public void set(Point position, int amount, int power) {
    this.position = position;
    this.amount = amount;
    this.power = power;
  }

  public Point position() {
    return position;
  }

  public int amount() {
    return amount;
  }

  public int power() {
    return power;
  }

  public boolean hasBomb() {
    return amount > 0;
  }
}
