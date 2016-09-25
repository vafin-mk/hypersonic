package hypersonic;

public class Bomb {

  private Point position;
  private int rounds;
  private int power = 3;

  Bomb(Point position, int rounds, int power) {
    set(position, rounds, power);
  }

  public void set(Point position, int rounds, int power) {
    this.position = position;
    this.rounds = rounds;
    this.power = power;
  }

  public Point position() {
    return position;
  }

  public int rounds() {
    return rounds;
  }

  public int power() {
    return power;
  }
}
