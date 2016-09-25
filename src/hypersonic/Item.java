package hypersonic;

public class Item {
  public static final int ITEM_TYPE_NONE = 0;
  public static final int ITEM_TYPE_POWER = 1;
  public static final int ITEM_TYPE_AMOUNT = 2;

  private int type;
  private Point position;

  Item(Point position, int type) {
    this.position = position;
    this.type = type;
  }

  public int type() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Point position() {
    return position;
  }
}
