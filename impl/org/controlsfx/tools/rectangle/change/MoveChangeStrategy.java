package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.MathTools;
import java.util.Objects;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class MoveChangeStrategy
  extends AbstractPreviousRectangleChangeStrategy
{
  private final Rectangle2D bounds;
  private Point2D startingPoint;
  
  public MoveChangeStrategy(Rectangle2D previous, Rectangle2D bounds)
  {
    super(previous, false, 0.0D);
    Objects.requireNonNull(bounds, "The specified bounds must not be null.");
    this.bounds = bounds;
  }
  
  public MoveChangeStrategy(Rectangle2D previous, double maxX, double maxY)
  {
    super(previous, false, 0.0D);
    if (maxX < previous.getWidth()) {
      throw new IllegalArgumentException("The specified maximal x-coordinate must be greater than or equal to the previous rectangle's width.");
    }
    if (maxY < previous.getHeight()) {
      throw new IllegalArgumentException("The specified maximal y-coordinate must be greater than or equal to the previous rectangle's height.");
    }
    this.bounds = new Rectangle2D(0.0D, 0.0D, maxX, maxY);
  }
  
  private final Rectangle2D moveRectangleToPoint(Point2D point)
  {
    double xMove = point.getX() - this.startingPoint.getX();
    double yMove = point.getY() - this.startingPoint.getY();
    
    double upperLeftX = getPrevious().getMinX() + xMove;
    double upperLeftY = getPrevious().getMinY() + yMove;
    
    double maxX = this.bounds.getMaxX() - getPrevious().getWidth();
    double maxY = this.bounds.getMaxY() - getPrevious().getHeight();
    
    double correctedUpperLeftX = MathTools.inInterval(this.bounds.getMinX(), upperLeftX, maxX);
    double correctedUpperLeftY = MathTools.inInterval(this.bounds.getMinY(), upperLeftY, maxY);
    
    return new Rectangle2D(correctedUpperLeftX, correctedUpperLeftY, 
    
      getPrevious().getWidth(), getPrevious().getHeight());
  }
  
  protected Rectangle2D doBegin(Point2D point)
  {
    this.startingPoint = point;
    return getPrevious();
  }
  
  protected Rectangle2D doContinue(Point2D point)
  {
    return moveRectangleToPoint(point);
  }
  
  protected Rectangle2D doEnd(Point2D point)
  {
    return moveRectangleToPoint(point);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\MoveChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */