package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.CellSkinBase;
import java.util.Collections;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

public class GridRowSkin<T>
  extends CellSkinBase<GridRow<T>, BehaviorBase<GridRow<T>>>
{
  public GridRowSkin(GridRow<T> control)
  {
    super(control, new BehaviorBase(control, Collections.emptyList()));
    
    getChildren().clear();
    updateCells();
    
    registerChangeListener(((GridRow)getSkinnable()).indexProperty(), "INDEX");
    registerChangeListener(((GridRow)getSkinnable()).widthProperty(), "WIDTH");
    registerChangeListener(((GridRow)getSkinnable()).heightProperty(), "HEIGHT");
  }
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if ("INDEX".equals(p)) {
      updateCells();
    } else if ("WIDTH".equals(p)) {
      updateCells();
    } else if ("HEIGHT".equals(p)) {
      updateCells();
    }
  }
  
  public GridCell<T> getCellAtIndex(int index)
  {
    if (index < getChildren().size()) {
      return (GridCell)getChildren().get(index);
    }
    return null;
  }
  
  public void updateCells()
  {
    int rowIndex = ((GridRow)getSkinnable()).getIndex();
    if (rowIndex >= 0)
    {
      GridView<T> gridView = ((GridRow)getSkinnable()).getGridView();
      int maxCellsInRow = ((GridViewSkin)gridView.getSkin()).computeMaxCellsInRow();
      int totalCellsInGrid = gridView.getItems().size();
      int startCellIndex = rowIndex * maxCellsInRow;
      int endCellIndex = startCellIndex + maxCellsInRow - 1;
      int cacheIndex = 0;
      for (int cellIndex = startCellIndex; cellIndex <= endCellIndex; cacheIndex++)
      {
        if (cellIndex >= totalCellsInGrid) {
          break;
        }
        GridCell<T> cell = getCellAtIndex(cacheIndex);
        if (cell == null)
        {
          cell = createCell();
          getChildren().add(cell);
        }
        cell.updateIndex(-1);
        cell.updateIndex(cellIndex);cellIndex++;
      }
      getChildren().remove(cacheIndex, getChildren().size());
    }
  }
  
  private GridCell<T> createCell()
  {
    GridView<T> gridView = (GridView)((GridRow)getSkinnable()).gridViewProperty().get();
    GridCell<T> cell;
    GridCell<T> cell;
    if (gridView.getCellFactory() != null) {
      cell = (GridCell)gridView.getCellFactory().call(gridView);
    } else {
      cell = createDefaultCellImpl();
    }
    cell.updateGridView(gridView);
    return cell;
  }
  
  private GridCell<T> createDefaultCellImpl()
  {
    new GridCell()
    {
      protected void updateItem(T item, boolean empty)
      {
        super.updateItem(item, empty);
        if (empty) {
          setText("");
        } else {
          setText(item.toString());
        }
      }
    };
  }
  
  protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
  }
  
  protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    return Double.MAX_VALUE;
  }
  
  protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    GridView<T> gv = (GridView)((GridRow)getSkinnable()).gridViewProperty().get();
    return gv.getCellHeight() + gv.getVerticalCellSpacing() * 2.0D;
  }
  
  protected void layoutChildren(double x, double y, double w, double h)
  {
    double cellWidth = ((GridView)((GridRow)getSkinnable()).gridViewProperty().get()).getCellWidth();
    double cellHeight = ((GridView)((GridRow)getSkinnable()).gridViewProperty().get()).getCellHeight();
    double horizontalCellSpacing = ((GridView)((GridRow)getSkinnable()).gridViewProperty().get()).getHorizontalCellSpacing();
    double verticalCellSpacing = ((GridView)((GridRow)getSkinnable()).gridViewProperty().get()).getVerticalCellSpacing();
    
    double xPos = 0.0D;
    double yPos = 0.0D;
    for (Node child : getChildren())
    {
      child.relocate(xPos + horizontalCellSpacing, yPos + verticalCellSpacing);
      child.resize(cellWidth, cellHeight);
      xPos = xPos + horizontalCellSpacing + cellWidth + horizontalCellSpacing;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\GridRowSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */