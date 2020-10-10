package impl.org.controlsfx.tools.rectangle.change;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public abstract interface Rectangle2DChangeStrategy
{
  public abstract Rectangle2D beginChange(Point2D paramPoint2D);
  
  public abstract Rectangle2D continueChange(Point2D paramPoint2D);
  
  public abstract Rectangle2D endChange(Point2D paramPoint2D);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\Rectangle2DChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */