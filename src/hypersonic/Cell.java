package hypersonic;

public class Cell {
  public static final int CELL_TYPE_FLOOR = 0;
  public static final int CELL_TYPE_BOX = 1;

  private int type;
  private int heat;
  private Item item = new Item(new Point(-100,-100), Item.ITEM_TYPE_NONE);

  Cell(int type) {
    this.type = type;
  }

  public int type() {
    return type;
  }

  public void setItemType(int type) {
    item.setType(type);
  }

  public int itemType() {
    return item.type();
  }

  public void setHeat(int heat) {
    this.heat = heat;
  }

  public int heat() {
    return heat;
  }

  public void incrementHeat(int value) {
    this.heat += value;
  }
}
