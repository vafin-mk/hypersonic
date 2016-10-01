package hypersonic;

class Hero {

  int col;//x-axis
  int row;//y-axis
  int owner;
  int bombs;
  int range;

  Hero(int col, int row, int owner, int bombs, int range) {
    this.col = col;
    this.row = row;
    this.owner = owner;
    this.bombs = bombs;
    this.range = range;
  }

  @Override
  public String toString() {
    return String.format("(pos(%s/%s) - owner[%s] - (%s, %s))", col, row, owner, bombs, range);
  }
}
