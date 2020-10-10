package impl.org.controlsfx.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import org.controlsfx.control.GridView;

class GridRow<T>
  extends IndexedCell<T>
{
  public GridRow()
  {
    getStyleClass().add("grid-row");
    
    indexProperty().addListener(new InvalidationListener()
    {
      public void invalidated(Observable observable)
      {
        GridRow.this.updateItem(null, GridRow.this.getIndex() == -1);
      }
    });
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new GridRowSkin(this);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\GridRow.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */