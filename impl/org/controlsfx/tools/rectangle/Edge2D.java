package impl.org.controlsfx.tools.rectangle;

import java.util.Objects;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;

public class Edge2D
{
  private final Point2D centerPoint;
  private final Orientation orientation;
  private final double length;
  
  public Edge2D(Point2D centerPoint, Orientation orientation, double length)
  {
    Objects.requireNonNull(centerPoint, "The specified center point must not be null.");
    Objects.requireNonNull(orientation, "The specified orientation must not be null.");
    if (length < 0.0D) {
      throw new IllegalArgumentException("The length must not be negative, i.e. zero or a positive value is alowed.");
    }
    this.centerPoint = centerPoint;
    this.orientation = orientation;
    this.length = length;
  }
  
  public Point2D getUpperLeft()
  {
    if (isHorizontal())
    {
      double cornersX = this.centerPoint.getX() - this.length / 2.0D;
      double edgesY = this.centerPoint.getY();
      return new Point2D(cornersX, edgesY);
    }
    double edgesX = this.centerPoint.getX();
    double cornersY = this.centerPoint.getY() - this.length / 2.0D;
    return new Point2D(edgesX, cornersY);
  }
  
  public Point2D getLowerRight()
  {
    if (isHorizontal())
    {
      double cornersX = this.centerPoint.getX() + this.length / 2.0D;
      double edgesY = this.centerPoint.getY();
      return new Point2D(cornersX, edgesY);
    }
    double edgesX = this.centerPoint.getX();
    double cornersY = this.centerPoint.getY() + this.length / 2.0D;
    return new Point2D(edgesX, cornersY);
  }
  
  public double getOrthogonalDifference(Point2D otherPoint)
  {
    Objects.requireNonNull(otherPoint, "The other point must nt be null.");
    if (isHorizontal()) {
      return otherPoint.getY() - this.centerPoint.getY();
    }
    return otherPoint.getX() - this.centerPoint.getX();
  }
  
  public Point2D getCenterPoint()
  {
    return this.centerPoint;
  }
  
  public Orientation getOrientation()
  {
    return this.orientation;
  }
  
  public boolean isHorizontal()
  {
    return this.orientation == Orientation.HORIZONTAL;
  }
  
  public boolean isVertical()
  {
    return this.orientation == Orientation.VERTICAL;
  }
  
  public double getLength()
  {
    return this.length;
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (this.centerPoint == null ? 0 : this.centerPoint.hashCode());
    
    long temp = Double.doubleToLongBits(this.length);
    result = 31 * result + (int)(temp ^ temp >>> 32);
    result = 31 * result + (this.orientation == null ? 0 : this.orientation.hashCode());
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Edge2D other = (Edge2D)obj;
    if (this.centerPoint == null)
    {
      if (other.centerPoint != null) {
        return false;
      }
    }
    else if (!this.centerPoint.equals(other.centerPoint)) {
      return false;
    }
    if (Double.doubleToLongBits(this.length) != Double.doubleToLongBits(other.length)) {
      return false;
    }
    if (this.orientation != other.orientation) {
      return false;
    }
    return true;
  }
  
  public String toString()
  {
    return "Edge2D [centerX = " + this.centerPoint.getX() + ", centerY = " + this.centerPoint.getY() + ", orientation = " + this.orientation + ", length = " + this.length + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\Edge2D.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */