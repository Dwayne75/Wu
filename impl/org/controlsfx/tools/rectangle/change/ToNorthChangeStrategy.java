package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.rectangle.Edge2D;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class ToNorthChangeStrategy
  extends AbstractFixedEdgeChangeStrategy
{
  private final Edge2D southernEdge;
  
  public ToNorthChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, Rectangle2D bounds)
  {
    super(ratioFixed, ratio, bounds);
    Point2D edgeCenterPoint = new Point2D((original.getMinX() + original.getMaxX()) / 2.0D, original.getMaxY());
    this.southernEdge = new Edge2D(edgeCenterPoint, Orientation.HORIZONTAL, original.getMaxX() - original.getMinX());
  }
  
  public ToNorthChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, double maxX, double maxY)
  {
    this(original, ratioFixed, ratio, new Rectangle2D(0.0D, 0.0D, maxX, maxY));
  }
  
  protected Edge2D getFixedEdge()
  {
    return this.southernEdge;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\tools\rectangle\change\ToNorthChangeStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */