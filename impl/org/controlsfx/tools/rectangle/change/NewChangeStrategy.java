package impl.org.controlsfx.tools.rectangle.change;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class NewChangeStrategy
  extends AbstractFixedPointChangeStrategy
{
  private Point2D startingPoint;
  
  public NewChangeStrategy(boolean ratioFixed, double ratio, Rectangle2D bounds)
  {
    super(ratioFixed, ratio, bounds);
  }
  
  protected void beforeBeginHook(Point2D point)
  {
    this.startingPoint = point;
  }
  
  protected Point2D getFixedCorner()
  {
    return this.startingPoint;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\NewChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */