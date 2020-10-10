package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.rectangle.Rectangles2D;
import java.util.Objects;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

abstract class AbstractFixedPointChangeStrategy
  extends AbstractRatioRespectingChangeStrategy
{
  private final Rectangle2D bounds;
  private Point2D fixedCorner;
  
  protected AbstractFixedPointChangeStrategy(boolean ratioFixed, double ratio, Rectangle2D bounds)
  {
    super(ratioFixed, ratio);
    Objects.requireNonNull(bounds, "The argument 'bounds' must not be null.");
    
    this.bounds = bounds;
  }
  
  protected abstract Point2D getFixedCorner();
  
  private final Rectangle2D createFromCorners(Point2D point)
  {
    Point2D pointInBounds = Rectangles2D.inRectangle(this.bounds, point);
    if (isRatioFixed()) {
      return Rectangles2D.forDiagonalCornersAndRatio(this.fixedCorner, pointInBounds, getRatio());
    }
    return Rectangles2D.forDiagonalCorners(this.fixedCorner, pointInBounds);
  }
  
  protected final Rectangle2D doBegin(Point2D point)
  {
    boolean startPointNotInBounds = !this.bounds.contains(point);
    if (startPointNotInBounds) {
      throw new IllegalArgumentException("The change's start point (" + point + ") must lie within the bounds (" + this.bounds + ").");
    }
    this.fixedCorner = getFixedCorner();
    return createFromCorners(point);
  }
  
  protected Rectangle2D doContinue(Point2D point)
  {
    return createFromCorners(point);
  }
  
  protected final Rectangle2D doEnd(Point2D point)
  {
    Rectangle2D newRectangle = createFromCorners(point);
    this.fixedCorner = null;
    return newRectangle;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\AbstractFixedPointChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */