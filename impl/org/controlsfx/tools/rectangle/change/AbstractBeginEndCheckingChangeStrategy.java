package impl.org.controlsfx.tools.rectangle.change;

import java.util.Objects;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

abstract class AbstractBeginEndCheckingChangeStrategy
  implements Rectangle2DChangeStrategy
{
  private boolean beforeBegin;
  
  protected AbstractBeginEndCheckingChangeStrategy()
  {
    this.beforeBegin = true;
  }
  
  public final Rectangle2D beginChange(Point2D point)
  {
    Objects.requireNonNull(point, "The specified point must not be null.");
    if (!this.beforeBegin) {
      throw new IllegalStateException("The change already began, so 'beginChange' must not be called again before 'endChange' was called.");
    }
    this.beforeBegin = false;
    
    beforeBeginHook(point);
    return doBegin(point);
  }
  
  public final Rectangle2D continueChange(Point2D point)
  {
    Objects.requireNonNull(point, "The specified point must not be null.");
    if (this.beforeBegin) {
      throw new IllegalStateException("The change did not begin. Call 'beginChange' before 'continueChange'.");
    }
    return doContinue(point);
  }
  
  public final Rectangle2D endChange(Point2D point)
  {
    Objects.requireNonNull(point, "The specified point must not be null.");
    if (this.beforeBegin) {
      throw new IllegalStateException("The change did not begin. Call 'beginChange' before 'endChange'.");
    }
    Rectangle2D finalRectangle = doEnd(point);
    afterEndHook(point);
    this.beforeBegin = true;
    return finalRectangle;
  }
  
  protected void beforeBeginHook(Point2D point) {}
  
  protected abstract Rectangle2D doBegin(Point2D paramPoint2D);
  
  protected abstract Rectangle2D doContinue(Point2D paramPoint2D);
  
  protected abstract Rectangle2D doEnd(Point2D paramPoint2D);
  
  protected void afterEndHook(Point2D point) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\AbstractBeginEndCheckingChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */