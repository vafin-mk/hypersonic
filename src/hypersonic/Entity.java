package hypersonic;

public class Entity {

  int type;
  Point position;
  int param1;
  int param2;

  Entity(int type, Point position, int param1, int param2) {
    this.type = type;
    this.position = position;
    this.param1 = param1;
    this.param2 = param2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Entity entity = (Entity) o;

    if (type != entity.type) return false;
    return position.equals(entity.position);

  }

  @Override
  public int hashCode() {
    int result = type;
    result = 31 * result + position.hashCode();
    return result;
  }
}
