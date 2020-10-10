package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.VirtualContainerBase;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.util.Collections;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.util.Callback;
import org.controlsfx.control.GridView;

public class GridViewSkin<T>
  extends VirtualContainerBase<GridView<T>, BehaviorBase<GridView<T>>, GridRow<T>>
{
  private final ListChangeListener<T> gridViewItemsListener = new ListChangeListener()
  {
    public void onChanged(ListChangeListener.Change<? extends T> change)
    {
      GridViewSkin.this.updateRowCount();
      ((GridView)GridViewSkin.this.getSkinnable()).requestLayout();
    }
  };
  private final WeakListChangeListener<T> weakGridViewItemsListener = new WeakListChangeListener(this.gridViewItemsListener);
  
  public GridViewSkin(GridView<T> control)
  {
    super(control, new BehaviorBase(control, Collections.emptyList()));
    
    updateGridViewItems();
    
    this.flow.setId("virtual-flow");
    this.flow.setPannable(false);
    this.flow.setVertical(true);
    this.flow.setFocusTraversable(((GridView)getSkinnable()).isFocusTraversable());
    this.flow.setCreateCell(new Callback()
    {
      public GridRow<T> call(VirtualFlow flow)
      {
        return GridViewSkin.this.createCell();
      }
    });
    getChildren().add(this.flow);
    
    updateRowCount();
    
    registerChangeListener(control.itemsProperty(), "ITEMS");
    registerChangeListener(control.cellFactoryProperty(), "CELL_FACTORY");
    registerChangeListener(control.parentProperty(), "PARENT");
    registerChangeListener(control.cellHeightProperty(), "CELL_HEIGHT");
    registerChangeListener(control.cellWidthProperty(), "CELL_WIDTH");
    registerChangeListener(control.horizontalCellSpacingProperty(), "HORIZONZAL_CELL_SPACING");
    registerChangeListener(control.verticalCellSpacingProperty(), "VERTICAL_CELL_SPACING");
    registerChangeListener(control.widthProperty(), "WIDTH_PROPERTY");
    registerChangeListener(control.heightProperty(), "HEIGHT_PROPERTY");
  }
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if (p == "ITEMS")
    {
      updateGridViewItems();
    }
    else if (p == "CELL_FACTORY")
    {
      this.flow.recreateCells();
    }
    else if (p == "CELL_HEIGHT")
    {
      this.flow.recreateCells();
    }
    else if (p == "CELL_WIDTH")
    {
      updateRowCount();
      this.flow.recreateCells();
    }
    else if (p == "HORIZONZAL_CELL_SPACING")
    {
      updateRowCount();
      this.flow.recreateCells();
    }
    else if (p == "VERTICAL_CELL_SPACING")
    {
      this.flow.recreateCells();
    }
    else if (p == "PARENT")
    {
      if ((((GridView)getSkinnable()).getParent() != null) && (((GridView)getSkinnable()).isVisible())) {
        ((GridView)getSkinnable()).requestLayout();
      }
    }
    else if ((p == "WIDTH_PROPERTY") || (p == "HEIGHT_PROPERTY"))
    {
      updateRowCount();
    }
  }
  
  public void updateGridViewItems()
  {
    if (((GridView)getSkinnable()).getItems() != null) {
      ((GridView)getSkinnable()).getItems().removeListener(this.weakGridViewItemsListener);
    }
    if (((GridView)getSkinnable()).getItems() != null) {
      ((GridView)getSkinnable()).getItems().addListener(this.weakGridViewItemsListener);
    }
    updateRowCount();
    this.flow.recreateCells();
    ((GridView)getSkinnable()).requestLayout();
  }
  
  protected void updateRowCount()
  {
    if (this.flow == null) {
      return;
    }
    int oldCount = this.flow.getCellCount();
    int newCount = getItemCount();
    if (newCount != oldCount)
    {
      this.flow.setCellCount(newCount);
      this.flow.rebuildCells();
    }
    else
    {
      this.flow.reconfigureCells();
    }
    updateRows(newCount);
  }
  
  protected void layoutChildren(double x, double y, double w, double h)
  {
    double x1 = ((GridView)getSkinnable()).getInsets().getLeft();
    double y1 = ((GridView)getSkinnable()).getInsets().getTop();
    double w1 = ((GridView)getSkinnable()).getWidth() - (((GridView)getSkinnable()).getInsets().getLeft() + ((GridView)getSkinnable()).getInsets().getRight());
    double h1 = ((GridView)getSkinnable()).getHeight() - (((GridView)getSkinnable()).getInsets().getTop() + ((GridView)getSkinnable()).getInsets().getBottom());
    
    this.flow.resizeRelocate(x1, y1, w1, h1);
  }
  
  public GridRow<T> createCell()
  {
    GridRow<T> row = new GridRow();
    row.updateGridView((GridView)getSkinnable());
    return row;
  }
  
  public int getItemCount()
  {
    ObservableList<?> items = ((GridView)getSkinnable()).getItems();
    
    return items == null ? 0 : (int)Math.ceil(items.size() / computeMaxCellsInRow());
  }
  
  public int computeMaxCellsInRow()
  {
    return Math.max((int)Math.floor(computeRowWidth() / computeCellWidth()), 1);
  }
  
  protected double computeRowWidth()
  {
    return ((GridView)getSkinnable()).getWidth() - 18.0D;
  }
  
  protected double computeCellWidth()
  {
    return ((GridView)getSkinnable()).cellWidthProperty().doubleValue() + ((GridView)getSkinnable()).horizontalCellSpacingProperty().doubleValue() * 2.0D;
  }
  
  protected void updateRows(int rowCount)
  {
    for (int i = 0; i < rowCount; i++)
    {
      GridRow<T> row = (GridRow)this.flow.getVisibleCell(i);
      if (row != null)
      {
        row.updateIndex(-1);
        row.updateIndex(i);
      }
    }
  }
  
  protected boolean areRowsVisible()
  {
    if (this.flow == null) {
      return false;
    }
    if (this.flow.getFirstVisibleCell() == null) {
      return false;
    }
    if (this.flow.getLastVisibleCell() == null) {
      return false;
    }
    return true;
  }
  
  protected double computeMinHeight(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return 0.0D;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\GridViewSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */