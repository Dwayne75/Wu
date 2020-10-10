package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.rectangle.Edge2D;
import impl.org.controlsfx.tools.rectangle.Rectangles2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

abstract class AbstractFixedEdgeChangeStrategy
  extends AbstractRatioRespectingChangeStrategy
{
  private final Rectangle2D bounds;
  private Edge2D fixedEdge;
  
  protected AbstractFixedEdgeChangeStrategy(boolean ratioFixed, double ratio, Rectangle2D bounds)
  {
    super(ratioFixed, ratio);
    this.bounds = bounds;
  }
  
  protected abstract Edge2D getFixedEdge();
  
  private final Rectangle2D createFromEdges(Point2D point)
  {
    Point2D pointInBounds = Rectangles2D.inRectangle(this.bounds, point);
    if (isRatioFixed()) {
      return Rectangles2D.forEdgeAndOpposingPointAndRatioWithinBounds(this.fixedEdge, pointInBounds, 
        getRatio(), this.bounds);
    }
    return Rectangles2D.forEdgeAndOpposingPoint(this.fixedEdge, pointInBounds);
  }
  
  protected final Rectangle2D doBegin(Point2D point)
  {
    boolean startPointNotInBounds = !this.bounds.contains(point);
    if (startPointNotInBounds) {
      throw new IllegalArgumentException("The change's start point (" + point + ") must lie within the bounds (" + this.bounds + ").");
    }
    this.fixedEdge = getFixedEdge();
    return createFromEdges(point);
  }
  
  protected Rectangle2D doContinue(Point2D point)
  {
    return createFromEdges(point);
  }
  
  protected final Rectangle2D doEnd(Point2D point)
  {
    Rectangle2D newRectangle = createFromEdges(point);
    this.fixedEdge = null;
    return newRectangle;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\AbstractFixedEdgeChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */