package impl.org.controlsfx.tools.rectangle.change;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class ToSouthwestChangeStrategy
  extends AbstractFixedPointChangeStrategy
{
  private final Point2D northeasternCorner;
  
  public ToSouthwestChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, Rectangle2D bounds)
  {
    super(ratioFixed, ratio, bounds);
    this.northeasternCorner = new Point2D(original.getMaxX(), original.getMinY());
  }
  
  protected Point2D getFixedCorner()
  {
    return this.northeasternCorner;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\ToSouthwestChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */