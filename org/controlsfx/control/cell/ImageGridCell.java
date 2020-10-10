package org.controlsfx.control.cell;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.GridCell;

public class ImageGridCell
  extends GridCell<Image>
{
  private final ImageView imageView;
  private final boolean preserveImageProperties;
  
  public ImageGridCell()
  {
    this(true);
  }
  
  public ImageGridCell(boolean preserveImageProperties)
  {
    getStyleClass().add("image-grid-cell");
    
    this.preserveImageProperties = preserveImageProperties;
    this.imageView = new ImageView();
    this.imageView.fitHeightProperty().bind(heightProperty());
    this.imageView.fitWidthProperty().bind(widthProperty());
  }
  
  protected void updateItem(Image item, boolean empty)
  {
    super.updateItem(item, empty);
    if (empty)
    {
      setGraphic(null);
    }
    else
    {
      if (this.preserveImageProperties)
      {
        this.imageView.setPreserveRatio(item.isPreserveRatio());
        this.imageView.setSmooth(item.isSmooth());
      }
      this.imageView.setImage(item);
      setGraphic(this.imageView);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\cell\ImageGridCell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */