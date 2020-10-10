package org.controlsfx.control;

import impl.org.controlsfx.skin.GridCellSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;

public class GridCell<T>
  extends IndexedCell<T>
{
  public GridCell()
  {
    getStyleClass().add("grid-cell");
    
    indexProperty().addListener(new InvalidationListener()
    {
      public void invalidated(Observable observable)
      {
        GridView<T> gridView = GridCell.this.getGridView();
        if (gridView == null) {
          return;
        }
        if (GridCell.this.getIndex() < 0)
        {
          GridCell.this.updateItem(null, true);
          return;
        }
        T item = gridView.getItems().get(GridCell.this.getIndex());
        
        GridCell.this.updateItem(item, item == null);
      }
    });
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new GridCellSkin(this);
  }
  
  public SimpleObjectProperty<GridView<T>> gridViewProperty()
  {
    return this.gridView;
  }
  
  private final SimpleObjectProperty<GridView<T>> gridView = new SimpleObjectProperty(this, "gridView");
  
  public final void updateGridView(GridView<T> gridView)
  {
    this.gridView.set(gridView);
  }
  
  public GridView<T> getGridView()
  {
    return (GridView)this.gridView.get();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\GridCell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */