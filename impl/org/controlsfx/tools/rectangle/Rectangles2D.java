package impl.org.controlsfx.tools.rectangle;

import impl.org.controlsfx.tools.MathTools;
import java.util.Objects;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class Rectangles2D
{
  public static boolean contains(Rectangle2D rectangle, Edge2D edge)
  {
    Objects.requireNonNull(rectangle, "The argument 'rectangle' must not be null.");
    Objects.requireNonNull(edge, "The argument 'edge' must not be null.");
    
    boolean edgeInBounds = (rectangle.contains(edge.getUpperLeft())) && (rectangle.contains(edge.getLowerRight()));
    return edgeInBounds;
  }
  
  public static Point2D inRectangle(Rectangle2D rectangle, Point2D point)
  {
    Objects.requireNonNull(rectangle, "The argument 'rectangle' must not be null.");
    Objects.requireNonNull(point, "The argument 'point' must not be null.");
    if (rectangle.contains(point)) {
      return point;
    }
    double newX = MathTools.inInterval(rectangle.getMinX(), point.getX(), rectangle.getMaxX());
    double newY = MathTools.inInterval(rectangle.getMinY(), point.getY(), rectangle.getMaxY());
    return new Point2D(newX, newY);
  }
  
  public static Point2D getCenterPoint(Rectangle2D rectangle)
  {
    Objects.requireNonNull(rectangle, "The argument 'rectangle' must not be null.");
    
    double centerX = (rectangle.getMinX() + rectangle.getMaxX()) / 2.0D;
    double centerY = (rectangle.getMinY() + rectangle.getMaxY()) / 2.0D;
    return new Point2D(centerX, centerY);
  }
  
  public static Rectangle2D intersection(Rectangle2D a, Rectangle2D b)
  {
    Objects.requireNonNull(a, "The argument 'a' must not be null.");
    Objects.requireNonNull(b, "The argument 'b' must not be null.");
    if (a.intersects(b))
    {
      double intersectionMinX = Math.max(a.getMinX(), b.getMinX());
      double intersectionMaxX = Math.min(a.getMaxX(), b.getMaxX());
      double intersectionWidth = intersectionMaxX - intersectionMinX;
      double intersectionMinY = Math.max(a.getMinY(), b.getMinY());
      double intersectionMaxY = Math.min(a.getMaxY(), b.getMaxY());
      double intersectionHeight = intersectionMaxY - intersectionMinY;
      return new Rectangle2D(intersectionMinX, intersectionMinY, intersectionWidth, intersectionHeight);
    }
    return Rectangle2D.EMPTY;
  }
  
  public static Rectangle2D forDiagonalCorners(Point2D oneCorner, Point2D diagonalCorner)
  {
    Objects.requireNonNull(oneCorner, "The specified corner must not be null.");
    Objects.requireNonNull(diagonalCorner, "The specified diagonal corner must not be null.");
    
    double minX = Math.min(oneCorner.getX(), diagonalCorner.getX());
    double minY = Math.min(oneCorner.getY(), diagonalCorner.getY());
    double width = Math.abs(oneCorner.getX() - diagonalCorner.getX());
    double height = Math.abs(oneCorner.getY() - diagonalCorner.getY());
    
    return new Rectangle2D(minX, minY, width, height);
  }
  
  public static Rectangle2D forUpperLeftCornerAndSize(Point2D upperLeft, double width, double height)
  {
    return new Rectangle2D(upperLeft.getX(), upperLeft.getY(), width, height);
  }
  
  public static Rectangle2D forDiagonalCornersAndRatio(Point2D fixedCorner, Point2D diagonalCorner, double ratio)
  {
    Objects.requireNonNull(fixedCorner, "The specified fixed corner must not be null.");
    Objects.requireNonNull(diagonalCorner, "The specified diagonal corner must not be null.");
    if (ratio < 0.0D) {
      throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");
    }
    double xDifference = diagonalCorner.getX() - fixedCorner.getX();
    double yDifference = diagonalCorner.getY() - fixedCorner.getY();
    
    double xDifferenceByRatio = correctCoordinateDifferenceByRatio(xDifference, yDifference, ratio);
    double yDifferenceByRatio = correctCoordinateDifferenceByRatio(yDifference, xDifference, 1.0D / ratio);
    
    double minX = getMinCoordinate(fixedCorner.getX(), xDifferenceByRatio);
    double minY = getMinCoordinate(fixedCorner.getY(), yDifferenceByRatio);
    
    double width = Math.abs(xDifferenceByRatio);
    double height = Math.abs(yDifferenceByRatio);
    
    return new Rectangle2D(minX, minY, width, height);
  }
  
  private static double correctCoordinateDifferenceByRatio(double difference, double otherDifference, double ratioAsMultiplier)
  {
    double differenceByRatio = otherDifference * ratioAsMultiplier;
    double correctedDistance = Math.min(Math.abs(difference), Math.abs(differenceByRatio));
    
    return correctedDistance * Math.signum(difference);
  }
  
  private static double getMinCoordinate(double fixedCoordinate, double difference)
  {
    if (difference < 0.0D) {
      return fixedCoordinate + difference;
    }
    return fixedCoordinate;
  }
  
  public static Rectangle2D forCenterAndSize(Point2D centerPoint, double width, double height)
  {
    Objects.requireNonNull(centerPoint, "The specified center point must not be null.");
    
    double absoluteWidth = Math.abs(width);
    double absoluteHeight = Math.abs(height);
    double minX = centerPoint.getX() - absoluteWidth / 2.0D;
    double minY = centerPoint.getY() - absoluteHeight / 2.0D;
    
    return new Rectangle2D(minX, minY, width, height);
  }
  
  public static Rectangle2D fixRatio(Rectangle2D original, double ratio)
  {
    Objects.requireNonNull(original, "The specified original rectangle must not be null.");
    if (ratio < 0.0D) {
      throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");
    }
    return createWithFixedRatioWithinBounds(original, ratio, null);
  }
  
  public static Rectangle2D fixRatioWithinBounds(Rectangle2D original, double ratio, Rectangle2D bounds)
  {
    Objects.requireNonNull(original, "The specified original rectangle must not be null.");
    Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null.");
    if (ratio < 0.0D) {
      throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");
    }
    return createWithFixedRatioWithinBounds(original, ratio, bounds);
  }
  
  private static Rectangle2D createWithFixedRatioWithinBounds(Rectangle2D original, double ratio, Rectangle2D bounds)
  {
    Point2D centerPoint = getCenterPoint(original);
    
    boolean centerPointInBounds = (bounds == null) || (bounds.contains(centerPoint));
    if (!centerPointInBounds) {
      throw new IllegalArgumentException("The center point " + centerPoint + " of the original rectangle is out of the specified bounds.");
    }
    double area = original.getWidth() * original.getHeight();
    
    return createForCenterAreaAndRatioWithinBounds(centerPoint, area, ratio, bounds);
  }
  
  public static Rectangle2D forCenterAndAreaAndRatio(Point2D centerPoint, double area, double ratio)
  {
    Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null.");
    if (area < 0.0D) {
      throw new IllegalArgumentException("The specified area " + area + " must be larger than zero.");
    }
    if (ratio < 0.0D) {
      throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");
    }
    return createForCenterAreaAndRatioWithinBounds(centerPoint, area, ratio, null);
  }
  
  public static Rectangle2D forCenterAndAreaAndRatioWithinBounds(Point2D centerPoint, double area, double ratio, Rectangle2D bounds)
  {
    Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null.");
    Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null.");
    boolean centerPointInBounds = bounds.contains(centerPoint);
    if (!centerPointInBounds) {
      throw new IllegalArgumentException("The center point " + centerPoint + " of the original rectangle is out of the specified bounds.");
    }
    if (area < 0.0D) {
      throw new IllegalArgumentException("The specified area " + area + " must be larger than zero.");
    }
    if (ratio < 0.0D) {
      throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");
    }
    return createForCenterAreaAndRatioWithinBounds(centerPoint, area, ratio, bounds);
  }
  
  private static Rectangle2D createForCenterAreaAndRatioWithinBounds(Point2D centerPoint, double area, double ratio, Rectangle2D bounds)
  {
    double newWidth = Math.sqrt(area * ratio);
    double newHeight = area / newWidth;
    
    boolean boundsSpecified = bounds != null;
    if (boundsSpecified)
    {
      double reductionFactor = lengthReductionToStayWithinBounds(centerPoint, newWidth, newHeight, bounds);
      newWidth *= reductionFactor;
      newHeight *= reductionFactor;
    }
    return forCenterAndSize(centerPoint, newWidth, newHeight);
  }
  
  private static double lengthReductionToStayWithinBounds(Point2D centerPoint, double width, double height, Rectangle2D bounds)
  {
    Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null.");
    Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null.");
    boolean centerPointInBounds = bounds.contains(centerPoint);
    if (!centerPointInBounds) {
      throw new IllegalArgumentException("The center point " + centerPoint + " of the original rectangle is out of the specified bounds.");
    }
    if (width < 0.0D) {
      throw new IllegalArgumentException("The specified width " + width + " must be larger than zero.");
    }
    if (height < 0.0D) {
      throw new IllegalArgumentException("The specified height " + height + " must be larger than zero.");
    }
    double distanceToEast = Math.abs(centerPoint.getX() - bounds.getMinX());
    double distanceToWest = Math.abs(centerPoint.getX() - bounds.getMaxX());
    double distanceToNorth = Math.abs(centerPoint.getY() - bounds.getMinY());
    double distanceToSouth = Math.abs(centerPoint.getY() - bounds.getMaxY());
    
    return MathTools.min(new double[] { 1.0D, distanceToEast / width * 2.0D, distanceToWest / width * 2.0D, distanceToNorth / height * 2.0D, distanceToSouth / height * 2.0D });
  }
  
  public static Rectangle2D forEdgeAndOpposingPoint(Edge2D edge, Point2D point)
  {
    double otherDimension = edge.getOrthogonalDifference(point);
    return createForEdgeAndOtherDimension(edge, otherDimension);
  }
  
  public static Rectangle2D forEdgeAndOpposingPointAndRatioWithinBounds(Edge2D edge, Point2D point, double ratio, Rectangle2D bounds)
  {
    Objects.requireNonNull(edge, "The specified edge must not be null.");
    Objects.requireNonNull(point, "The specified point must not be null.");
    Objects.requireNonNull(bounds, "The specified bounds must not be null.");
    
    boolean edgeInBounds = contains(bounds, edge);
    if (!edgeInBounds) {
      throw new IllegalArgumentException("The specified edge " + edge + " is not entirely contained on the specified bounds.");
    }
    if (ratio < 0.0D) {
      throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");
    }
    Point2D boundedPoint = movePointIntoBounds(point, bounds);
    Edge2D unboundedEdge = resizeEdgeForDistanceAndRatio(edge, boundedPoint, ratio);
    Edge2D boundedEdge = resizeEdgeForBounds(unboundedEdge, bounds);
    
    double otherDimension = Math.signum(boundedEdge.getOrthogonalDifference(boundedPoint));
    if (boundedEdge.isHorizontal()) {
      otherDimension *= boundedEdge.getLength() / ratio;
    } else {
      otherDimension *= boundedEdge.getLength() * ratio;
    }
    return createForEdgeAndOtherDimension(boundedEdge, otherDimension);
  }
  
  private static Point2D movePointIntoBounds(Point2D point, Rectangle2D bounds)
  {
    if (bounds.contains(point)) {
      return point;
    }
    double boundedPointX = MathTools.inInterval(bounds.getMinX(), point.getX(), bounds.getMaxX());
    double boundedPointY = MathTools.inInterval(bounds.getMinY(), point.getY(), bounds.getMaxY());
    return new Point2D(boundedPointX, boundedPointY);
  }
  
  private static Edge2D resizeEdgeForDistanceAndRatio(Edge2D edge, Point2D point, double ratio)
  {
    double distance = Math.abs(edge.getOrthogonalDifference(point));
    if (edge.isHorizontal())
    {
      double xLength = distance * ratio;
      return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), xLength);
    }
    double yLength = distance / ratio;
    return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), yLength);
  }
  
  private static Edge2D resizeEdgeForBounds(Edge2D edge, Rectangle2D bounds)
  {
    boolean edgeInBounds = contains(bounds, edge);
    if (edgeInBounds) {
      return edge;
    }
    boolean centerPointInBounds = bounds.contains(edge.getCenterPoint());
    if (!centerPointInBounds) {
      throw new IllegalArgumentException("The specified edge's center point (" + edge + ") is out of the specified bounds (" + bounds + ").");
    }
    if (edge.isHorizontal())
    {
      double leftPartLengthBound = Math.abs(bounds.getMinX() - edge.getCenterPoint().getX());
      double rightPartLengthBound = Math.abs(bounds.getMaxX() - edge.getCenterPoint().getX());
      
      double leftPartLength = MathTools.inInterval(0.0D, edge.getLength() / 2.0D, leftPartLengthBound);
      double rightPartLength = MathTools.inInterval(0.0D, edge.getLength() / 2.0D, rightPartLengthBound);
      
      double horizontalLength = Math.min(leftPartLength, rightPartLength) * 2.0D;
      return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), horizontalLength);
    }
    double lowerPartLengthBound = Math.abs(bounds.getMinY() - edge.getCenterPoint().getY());
    double upperPartLengthBound = Math.abs(bounds.getMaxY() - edge.getCenterPoint().getY());
    
    double lowerPartLength = MathTools.inInterval(0.0D, edge.getLength() / 2.0D, lowerPartLengthBound);
    double upperPartLength = MathTools.inInterval(0.0D, edge.getLength() / 2.0D, upperPartLengthBound);
    
    double verticalLength = Math.min(lowerPartLength, upperPartLength) * 2.0D;
    return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), verticalLength);
  }
  
  private static Rectangle2D createForEdgeAndOtherDimension(Edge2D edge, double otherDimension)
  {
    if (edge.isHorizontal()) {
      return createForHorizontalEdgeAndHeight(edge, otherDimension);
    }
    return createForVerticalEdgeAndWidth(edge, otherDimension);
  }
  
  private static Rectangle2D createForHorizontalEdgeAndHeight(Edge2D horizontalEdge, double height)
  {
    Point2D leftEdgeEndPoint = horizontalEdge.getUpperLeft();
    double upperLeftX = leftEdgeEndPoint.getX();
    
    double upperLeftY = leftEdgeEndPoint.getY() + Math.min(0.0D, height);
    
    double absoluteWidth = Math.abs(horizontalEdge.getLength());
    double absoluteHeight = Math.abs(height);
    
    return new Rectangle2D(upperLeftX, upperLeftY, absoluteWidth, absoluteHeight);
  }
  
  private static Rectangle2D createForVerticalEdgeAndWidth(Edge2D verticalEdge, double width)
  {
    Point2D upperEdgeEndPoint = verticalEdge.getUpperLeft();
    
    double upperLeftX = upperEdgeEndPoint.getX() + Math.min(0.0D, width);
    double upperLeftY = upperEdgeEndPoint.getY();
    
    double absoluteWidth = Math.abs(width);
    double absoluteHeight = Math.abs(verticalEdge.getLength());
    
    return new Rectangle2D(upperLeftX, upperLeftY, absoluteWidth, absoluteHeight);
  }
  
  public static Rectangle2D fromBounds(Bounds bounds)
  {
    return new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\Rectangles2D.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */