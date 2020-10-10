package impl.org.controlsfx.tools.rectangle;

import java.util.EnumSet;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class CoordinatePositions
{
  public static EnumSet<CoordinatePosition> onRectangleAndEdges(Rectangle2D rectangle, Point2D point, double edgeTolerance)
  {
    EnumSet<CoordinatePosition> positions = EnumSet.noneOf(CoordinatePosition.class);
    positions.add(inRectangle(rectangle, point));
    positions.add(onEdges(rectangle, point, edgeTolerance));
    return positions;
  }
  
  public static CoordinatePosition inRectangle(Rectangle2D rectangle, Point2D point)
  {
    if (rectangle.contains(point)) {
      return CoordinatePosition.IN_RECTANGLE;
    }
    return CoordinatePosition.OUT_OF_RECTANGLE;
  }
  
  public static CoordinatePosition onEdges(Rectangle2D rectangle, Point2D point, double edgeTolerance)
  {
    CoordinatePosition vertical = closeToVertical(rectangle, point, edgeTolerance);
    CoordinatePosition horizontal = closeToHorizontal(rectangle, point, edgeTolerance);
    
    return extractSingleCardinalDirection(vertical, horizontal);
  }
  
  private static CoordinatePosition closeToVertical(Rectangle2D rectangle, Point2D point, double edgeTolerance)
  {
    double xDistanceToLeft = Math.abs(point.getX() - rectangle.getMinX());
    double xDistanceToRight = Math.abs(point.getX() - rectangle.getMaxX());
    boolean xCloseToLeft = (xDistanceToLeft < edgeTolerance) && (xDistanceToLeft < xDistanceToRight);
    boolean xCloseToRight = (xDistanceToRight < edgeTolerance) && (xDistanceToRight < xDistanceToLeft);
    if ((!xCloseToLeft) && (!xCloseToRight)) {
      return null;
    }
    boolean yCloseToVertical = (rectangle.getMinY() - edgeTolerance < point.getY()) && (point.getY() < rectangle.getMaxY() + edgeTolerance);
    if (yCloseToVertical)
    {
      if (xCloseToLeft) {
        return CoordinatePosition.WEST_EDGE;
      }
      if (xCloseToRight) {
        return CoordinatePosition.EAST_EDGE;
      }
    }
    return null;
  }
  
  private static CoordinatePosition closeToHorizontal(Rectangle2D rectangle, Point2D point, double edgeTolerance)
  {
    double yDistanceToUpper = Math.abs(point.getY() - rectangle.getMinY());
    double yDistanceToLower = Math.abs(point.getY() - rectangle.getMaxY());
    boolean yCloseToUpper = (yDistanceToUpper < edgeTolerance) && (yDistanceToUpper < yDistanceToLower);
    boolean yCloseToLower = (yDistanceToLower < edgeTolerance) && (yDistanceToLower < yDistanceToUpper);
    if ((!yCloseToUpper) && (!yCloseToLower)) {
      return null;
    }
    boolean xCloseToHorizontal = (rectangle.getMinX() - edgeTolerance < point.getX()) && (point.getX() < rectangle.getMaxX() + edgeTolerance);
    if (xCloseToHorizontal)
    {
      if (yCloseToUpper) {
        return CoordinatePosition.NORTH_EDGE;
      }
      if (yCloseToLower) {
        return CoordinatePosition.SOUTH_EDGE;
      }
    }
    return null;
  }
  
  private static CoordinatePosition extractSingleCardinalDirection(CoordinatePosition vertical, CoordinatePosition horizontal)
  {
    if (vertical == null) {
      return horizontal;
    }
    if (horizontal == null) {
      return vertical;
    }
    if ((horizontal == CoordinatePosition.NORTH_EDGE) && (vertical == CoordinatePosition.EAST_EDGE)) {
      return CoordinatePosition.NORTHEAST_EDGE;
    }
    if ((horizontal == CoordinatePosition.NORTH_EDGE) && (vertical == CoordinatePosition.WEST_EDGE)) {
      return CoordinatePosition.NORTHWEST_EDGE;
    }
    if ((horizontal == CoordinatePosition.SOUTH_EDGE) && (vertical == CoordinatePosition.EAST_EDGE)) {
      return CoordinatePosition.SOUTHEAST_EDGE;
    }
    if ((horizontal == CoordinatePosition.SOUTH_EDGE) && (vertical == CoordinatePosition.WEST_EDGE)) {
      return CoordinatePosition.SOUTHWEST_EDGE;
    }
    throw new IllegalArgumentException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\CoordinatePositions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */