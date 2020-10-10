package impl.org.controlsfx.tools.rectangle.change;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class ToSoutheastChangeStrategy
  extends AbstractFixedPointChangeStrategy
{
  private final Point2D northwesternCorner;
  
  public ToSoutheastChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, Rectangle2D bounds)
  {
    super(ratioFixed, ratio, bounds);
    this.northwesternCorner = new Point2D(original.getMinX(), original.getMinY());
  }
  
  protected Point2D getFixedCorner()
  {
    return this.northwesternCorner;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\ToSoutheastChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */