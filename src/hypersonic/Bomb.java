package hypersonic;

class Bomb {

  static final int EXPLODE_NEXT_TURN = 1;
  static final int NO_EXPLODE = -10000;

  int col;
  int row;
  int owner;
  int time;//to explode
  int range;

  Bomb(int col, int row, int owner, int time, int range) {
    this.col = col;
    this.row = row;
    this.owner = owner;
    this.time = time;
    this.range = range;
  }

  @Override
  public String toString() {
    return String.format("(%s/%s[%s] - (%s, %s))", col, row, owner, time, range);
  }
}
