package impl.org.controlsfx.tools.rectangle.change;

import java.util.Objects;
import javafx.geometry.Rectangle2D;

abstract class AbstractPreviousRectangleChangeStrategy
  extends AbstractRatioRespectingChangeStrategy
{
  private final Rectangle2D previous;
  
  protected AbstractPreviousRectangleChangeStrategy(Rectangle2D previous, boolean ratioFixed, double ratio)
  {
    super(ratioFixed, ratio);
    
    Objects.requireNonNull(previous, "The previous rectangle must not be null.");
    this.previous = previous;
  }
  
  protected final Rectangle2D getPrevious()
  {
    return this.previous;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\AbstractPreviousRectangleChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */