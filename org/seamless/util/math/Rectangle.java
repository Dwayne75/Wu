package org.seamless.util.math;

public class Rectangle
{
  private Point position;
  private int width;
  private int height;
  
  public Rectangle() {}
  
  public Rectangle(Point position, int width, int height)
  {
    this.position = position;
    this.width = width;
    this.height = height;
  }
  
  public void reset()
  {
    this.position = new Point(0, 0);
    this.width = 0;
    this.height = 0;
  }
  
  public Point getPosition()
  {
    return this.position;
  }
  
  public void setPosition(Point position)
  {
    this.position = position;
  }
  
  public int getWidth()
  {
    return this.width;
  }
  
  public void setWidth(int width)
  {
    this.width = width;
  }
  
  public int getHeight()
  {
    return this.height;
  }
  
  public void setHeight(int height)
  {
    this.height = height;
  }
  
  public Rectangle intersection(Rectangle that)
  {
    int tx1 = this.position.getX();
    int ty1 = this.position.getY();
    int rx1 = that.position.getX();
    int ry1 = that.position.getY();
    long tx2 = tx1;
    tx2 += this.width;
    long ty2 = ty1;
    ty2 += this.height;
    long rx2 = rx1;
    rx2 += that.width;
    long ry2 = ry1;
    ry2 += that.height;
    if (tx1 < rx1) {
      tx1 = rx1;
    }
    if (ty1 < ry1) {
      ty1 = ry1;
    }
    if (tx2 > rx2) {
      tx2 = rx2;
    }
    if (ty2 > ry2) {
      ty2 = ry2;
    }
    tx2 -= tx1;
    ty2 -= ty1;
    if (tx2 < -2147483648L) {
      tx2 = -2147483648L;
    }
    if (ty2 < -2147483648L) {
      ty2 = -2147483648L;
    }
    return new Rectangle(new Point(tx1, ty1), (int)tx2, (int)ty2);
  }
  
  public boolean isOverlapping(Rectangle that)
  {
    Rectangle intersection = intersection(that);
    return (intersection.getWidth() > 0) && (intersection.getHeight() > 0);
  }
  
  public String toString()
  {
    return "Rectangle(" + this.position + " - " + this.width + "x" + this.height + ")";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\math\Rectangle.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */