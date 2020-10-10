package org.seamless.util.math;

public class Point
{
  private int x;
  private int y;
  
  public Point(int x, int y)
  {
    this.x = x;
    this.y = y;
  }
  
  public int getX()
  {
    return this.x;
  }
  
  public int getY()
  {
    return this.y;
  }
  
  public Point multiply(double by)
  {
    return new Point(this.x != 0 ? (int)(this.x * by) : 0, this.y != 0 ? (int)(this.y * by) : 0);
  }
  
  public Point divide(double by)
  {
    return new Point(this.x != 0 ? (int)(this.x / by) : 0, this.y != 0 ? (int)(this.y / by) : 0);
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Point point = (Point)o;
    if (this.x != point.x) {
      return false;
    }
    if (this.y != point.y) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.x;
    result = 31 * result + this.y;
    return result;
  }
  
  public String toString()
  {
    return "Point(" + this.x + "/" + this.y + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\math\Point.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */