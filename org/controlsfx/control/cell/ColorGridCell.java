package org.controlsfx.control.cell;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.GridCell;

public class ColorGridCell
  extends GridCell<Color>
{
  private Rectangle colorRect;
  private static final boolean debug = false;
  
  public ColorGridCell()
  {
    getStyleClass().add("color-grid-cell");
    
    this.colorRect = new Rectangle();
    this.colorRect.setStroke(Color.BLACK);
    this.colorRect.heightProperty().bind(heightProperty());
    this.colorRect.widthProperty().bind(widthProperty());
    setGraphic(this.colorRect);
  }
  
  protected void updateItem(Color item, boolean empty)
  {
    super.updateItem(item, empty);
    if (empty)
    {
      setGraphic(null);
    }
    else
    {
      this.colorRect.setFill(item);
      setGraphic(this.colorRect);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\cell\ColorGridCell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */