package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

public class GridViewBehavior
  extends TableViewBehavior<ObservableList<SpreadsheetCell>>
{
  private GridViewSkin skin;
  
  public GridViewBehavior(TableView<ObservableList<SpreadsheetCell>> control)
  {
    super(control);
  }
  
  void setGridViewSkin(GridViewSkin skin)
  {
    this.skin = skin;
  }
  
  protected void updateCellVerticalSelection(int delta, Runnable defaultAction)
  {
    TableViewSpanSelectionModel sm = (TableViewSpanSelectionModel)getSelectionModel();
    if ((sm == null) || (sm.getSelectionMode() == SelectionMode.SINGLE)) {
      return;
    }
    TableFocusModel fm = getFocusModel();
    if (fm == null) {
      return;
    }
    TablePositionBase focusedCell = getFocusedCell();
    if ((this.isShiftDown) && (getAnchor() != null))
    {
      SpreadsheetCell cell = (SpreadsheetCell)focusedCell.getTableColumn().getCellData(focusedCell.getRow());
      sm.direction = new Pair(Integer.valueOf(delta), Integer.valueOf(0));
      int newRow;
      if (delta < 0) {
        newRow = this.skin.getFirstRow(cell, focusedCell.getRow()) + delta;
      } else {
        newRow = focusedCell.getRow() + this.skin.spreadsheetView.getRowSpan(cell, focusedCell.getRow()) - 1 + delta;
      }
      int newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);
      
      TablePositionBase<?> anchor = getAnchor();
      int minRow = Math.min(anchor.getRow(), newRow);
      int maxRow = Math.max(anchor.getRow(), newRow);
      int minColumn = Math.min(anchor.getColumn(), focusedCell.getColumn());
      int maxColumn = Math.max(anchor.getColumn(), focusedCell.getColumn());
      
      sm.clearSelection();
      if ((minColumn != -1) && (maxColumn != -1)) {
        sm.selectRange(minRow, ((TableView)getControl()).getVisibleLeafColumn(minColumn), maxRow, 
          ((TableView)getControl()).getVisibleLeafColumn(maxColumn));
      }
      fm.focus(newRow, focusedCell.getTableColumn());
    }
    else
    {
      int focusIndex = fm.getFocusedIndex();
      if (!sm.isSelected(focusIndex, focusedCell.getTableColumn())) {
        sm.select(focusIndex, focusedCell.getTableColumn());
      }
      defaultAction.run();
    }
  }
  
  protected void updateCellHorizontalSelection(int delta, Runnable defaultAction)
  {
    TableViewSpanSelectionModel sm = (TableViewSpanSelectionModel)getSelectionModel();
    if ((sm == null) || (sm.getSelectionMode() == SelectionMode.SINGLE)) {
      return;
    }
    TableFocusModel fm = getFocusModel();
    if (fm == null) {
      return;
    }
    TablePositionBase focusedCell = getFocusedCell();
    if ((focusedCell == null) || (focusedCell.getTableColumn() == null)) {
      return;
    }
    TableColumnBase adjacentColumn = getColumn(focusedCell.getTableColumn(), delta);
    if (adjacentColumn == null) {
      return;
    }
    int focusedCellRow = focusedCell.getRow();
    if ((this.isShiftDown) && (getAnchor() != null))
    {
      SpreadsheetCell cell = (SpreadsheetCell)focusedCell.getTableColumn().getCellData(focusedCell.getRow());
      
      sm.direction = new Pair(Integer.valueOf(0), Integer.valueOf(delta));
      int newColumn;
      int newColumn;
      if (delta < 0) {
        newColumn = this.skin.spreadsheetView.getViewColumn(cell.getColumn()) + delta;
      } else {
        newColumn = this.skin.spreadsheetView.getViewColumn(cell.getColumn()) + this.skin.spreadsheetView.getColumnSpan(cell) - 1 + delta;
      }
      TablePositionBase<?> anchor = getAnchor();
      int minRow = Math.min(anchor.getRow(), focusedCellRow);
      int maxRow = Math.max(anchor.getRow(), focusedCellRow);
      int minColumn = Math.min(anchor.getColumn(), newColumn);
      int maxColumn = Math.max(anchor.getColumn(), newColumn);
      
      sm.clearSelection();
      if ((minColumn != -1) && (maxColumn != -1)) {
        sm.selectRange(minRow, ((TableView)getControl()).getVisibleLeafColumn(minColumn), maxRow, 
          ((TableView)getControl()).getVisibleLeafColumn(maxColumn));
      }
      fm.focus(focusedCell.getRow(), getColumn(newColumn));
    }
    else
    {
      defaultAction.run();
    }
  }
  
  protected void focusPreviousRow()
  {
    focusVertical(true);
  }
  
  protected void focusNextRow()
  {
    focusVertical(false);
  }
  
  protected void focusLeftCell()
  {
    focusHorizontal(true);
  }
  
  protected void focusRightCell()
  {
    focusHorizontal(false);
  }
  
  protected void discontinuousSelectPreviousRow()
  {
    discontinuousSelectVertical(true);
  }
  
  protected void discontinuousSelectNextRow()
  {
    discontinuousSelectVertical(false);
  }
  
  protected void discontinuousSelectPreviousColumn()
  {
    discontinuousSelectHorizontal(true);
  }
  
  protected void discontinuousSelectNextColumn()
  {
    discontinuousSelectHorizontal(false);
  }
  
  private void focusVertical(boolean previous)
  {
    TableSelectionModel sm = getSelectionModel();
    if ((sm == null) || (sm.getSelectionMode() == SelectionMode.SINGLE)) {
      return;
    }
    TableFocusModel fm = getFocusModel();
    if (fm == null) {
      return;
    }
    TablePositionBase focusedCell = getFocusedCell();
    if ((focusedCell == null) || (focusedCell.getTableColumn() == null)) {
      return;
    }
    SpreadsheetCell cell = (SpreadsheetCell)focusedCell.getTableColumn().getCellData(focusedCell.getRow());
    sm.clearAndSelect(previous ? findPreviousRow(focusedCell, cell) : findNextRow(focusedCell, cell), focusedCell.getTableColumn());
    this.skin.focusScroll();
  }
  
  private void focusHorizontal(boolean previous)
  {
    TableSelectionModel sm = getSelectionModel();
    if (sm == null) {
      return;
    }
    TableFocusModel fm = getFocusModel();
    if (fm == null) {
      return;
    }
    TablePositionBase focusedCell = getFocusedCell();
    if ((focusedCell == null) || (focusedCell.getTableColumn() == null)) {
      return;
    }
    SpreadsheetCell cell = (SpreadsheetCell)focusedCell.getTableColumn().getCellData(focusedCell.getRow());
    
    sm.clearAndSelect(focusedCell.getRow(), ((TableView)getControl()).getVisibleLeafColumn(previous ? findPreviousColumn(focusedCell, cell) : findNextColumn(focusedCell, cell)));
    this.skin.focusScroll();
  }
  
  private int findPreviousRow(TablePositionBase focusedCell, SpreadsheetCell cell)
  {
    ObservableList<ObservableList<SpreadsheetCell>> items = ((TableView)getControl()).getItems();
    if (isEmpty(cell)) {
      for (int row = focusedCell.getRow() - 1; row >= 0; row--)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(row)).get(focusedCell.getColumn());
        if (!isEmpty(temp)) {
          return row;
        }
      }
    } else if ((focusedCell.getRow() - 1 >= 0) && (!isEmpty((SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow() - 1)).get(focusedCell.getColumn())))) {
      for (int row = focusedCell.getRow() - 2; row >= 0; row--)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(row)).get(focusedCell.getColumn());
        if (isEmpty(temp)) {
          return row + 1;
        }
      }
    } else {
      for (int row = focusedCell.getRow() - 2; row >= 0; row--)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(row)).get(focusedCell.getColumn());
        if (!isEmpty(temp)) {
          return row;
        }
      }
    }
    return 0;
  }
  
  public void selectCell(int rowDiff, int columnDiff)
  {
    TableViewSpanSelectionModel sm = (TableViewSpanSelectionModel)getSelectionModel();
    if (sm == null) {
      return;
    }
    sm.direction = new Pair(Integer.valueOf(rowDiff), Integer.valueOf(columnDiff));
    
    TableFocusModel fm = getFocusModel();
    if (fm == null) {
      return;
    }
    TablePositionBase focusedCell = getFocusedCell();
    int currentRow = focusedCell.getRow();
    int currentColumn = getVisibleLeafIndex(focusedCell.getTableColumn());
    if ((rowDiff < 0) && (currentRow <= 0)) {
      return;
    }
    if ((rowDiff > 0) && (currentRow >= getItemCount() - 1)) {
      return;
    }
    if ((columnDiff < 0) && (currentColumn <= 0)) {
      return;
    }
    if ((columnDiff > 0) && (currentColumn >= getVisibleLeafColumns().size() - 1)) {
      return;
    }
    if ((columnDiff > 0) && (currentColumn == -1)) {
      return;
    }
    TableColumnBase tc = focusedCell.getTableColumn();
    tc = getColumn(tc, columnDiff);
    
    int row = focusedCell.getRow() + rowDiff;
    
    sm.clearAndSelect(row, tc);
    setAnchor(row, tc);
  }
  
  private int findNextRow(TablePositionBase focusedCell, SpreadsheetCell cell)
  {
    ObservableList<ObservableList<SpreadsheetCell>> items = ((TableView)getControl()).getItems();
    int itemCount = getItemCount();
    if (isEmpty(cell)) {
      for (int row = focusedCell.getRow() + 1; row < itemCount; row++)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(row)).get(focusedCell.getColumn());
        if (!isEmpty(temp)) {
          return row;
        }
      }
    } else if ((focusedCell.getRow() + 1 < itemCount) && (!isEmpty((SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow() + 1)).get(focusedCell.getColumn())))) {
      for (int row = focusedCell.getRow() + 2; row < getItemCount(); row++)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(row)).get(focusedCell.getColumn());
        if (isEmpty(temp)) {
          return row - 1;
        }
      }
    } else {
      for (int row = focusedCell.getRow() + 2; row < itemCount; row++)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(row)).get(focusedCell.getColumn());
        if (!isEmpty(temp)) {
          return row;
        }
      }
    }
    return itemCount - 1;
  }
  
  private void discontinuousSelectVertical(boolean previous)
  {
    TableSelectionModel sm = getSelectionModel();
    if (sm == null) {
      return;
    }
    TableFocusModel fm = getFocusModel();
    if (fm == null) {
      return;
    }
    TablePositionBase focusedCell = getFocusedCell();
    if ((focusedCell == null) || (focusedCell.getTableColumn() == null)) {
      return;
    }
    SpreadsheetCell cell = (SpreadsheetCell)focusedCell.getTableColumn().getCellData(focusedCell.getRow());
    
    int newRow = previous ? findPreviousRow(focusedCell, cell) : findNextRow(focusedCell, cell);
    
    newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);
    
    TablePositionBase<?> anchor = getAnchor();
    int minRow = Math.min(anchor.getRow(), newRow);
    int maxRow = Math.max(anchor.getRow(), newRow);
    int minColumn = Math.min(anchor.getColumn(), focusedCell.getColumn());
    int maxColumn = Math.max(anchor.getColumn(), focusedCell.getColumn());
    
    sm.clearSelection();
    if ((minColumn != -1) && (maxColumn != -1)) {
      sm.selectRange(minRow, ((TableView)getControl()).getVisibleLeafColumn(minColumn), maxRow, 
        ((TableView)getControl()).getVisibleLeafColumn(maxColumn));
    }
    fm.focus(newRow, focusedCell.getTableColumn());
    this.skin.focusScroll();
  }
  
  private void discontinuousSelectHorizontal(boolean previous)
  {
    TableSelectionModel sm = getSelectionModel();
    if (sm == null) {
      return;
    }
    TableFocusModel fm = getFocusModel();
    if (fm == null) {
      return;
    }
    TablePositionBase focusedCell = getFocusedCell();
    if ((focusedCell == null) || (focusedCell.getTableColumn() == null)) {
      return;
    }
    int columnPos = getVisibleLeafIndex(focusedCell.getTableColumn());
    int focusedCellRow = focusedCell.getRow();
    SpreadsheetCell cell = (SpreadsheetCell)focusedCell.getTableColumn().getCellData(focusedCell.getRow());
    
    int newColumn = previous ? findPreviousColumn(focusedCell, cell) : findNextColumn(focusedCell, cell);
    
    TablePositionBase<?> anchor = getAnchor();
    int minRow = Math.min(anchor.getRow(), focusedCellRow);
    int maxRow = Math.max(anchor.getRow(), focusedCellRow);
    int minColumn = Math.min(anchor.getColumn(), newColumn);
    int maxColumn = Math.max(anchor.getColumn(), newColumn);
    
    sm.clearSelection();
    if ((minColumn != -1) && (maxColumn != -1)) {
      sm.selectRange(minRow, ((TableView)getControl()).getVisibleLeafColumn(minColumn), maxRow, 
        ((TableView)getControl()).getVisibleLeafColumn(maxColumn));
    }
    fm.focus(focusedCell.getRow(), getColumn(newColumn));
    this.skin.focusScroll();
  }
  
  private int findNextColumn(TablePositionBase focusedCell, SpreadsheetCell cell)
  {
    ObservableList<ObservableList<SpreadsheetCell>> items = ((TableView)getControl()).getItems();
    int itemCount = ((TableView)getControl()).getColumns().size();
    if (isEmpty(cell)) {
      for (int column = focusedCell.getColumn() + 1; column < itemCount; column++)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(column);
        if (!isEmpty(temp)) {
          return column;
        }
      }
    } else if ((focusedCell.getColumn() + 1 < itemCount) && (!isEmpty((SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(focusedCell.getColumn() + 1)))) {
      for (int column = focusedCell.getColumn() + 2; column < itemCount; column++)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(column);
        if (isEmpty(temp)) {
          return column - 1;
        }
      }
    } else {
      for (int column = focusedCell.getColumn() + 2; column < itemCount; column++)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(column);
        if (!isEmpty(temp)) {
          return column;
        }
      }
    }
    return itemCount - 1;
  }
  
  private int findPreviousColumn(TablePositionBase focusedCell, SpreadsheetCell cell)
  {
    ObservableList<ObservableList<SpreadsheetCell>> items = ((TableView)getControl()).getItems();
    if (isEmpty(cell)) {
      for (int column = focusedCell.getColumn() - 1; column >= 0; column--)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(column);
        if (!isEmpty(temp)) {
          return column;
        }
      }
    } else if ((focusedCell.getColumn() - 1 >= 0) && (!isEmpty((SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(focusedCell.getColumn() - 1)))) {
      for (int column = focusedCell.getColumn() - 2; column >= 0; column--)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(column);
        if (isEmpty(temp)) {
          return column + 1;
        }
      }
    } else {
      for (int column = focusedCell.getColumn() - 2; column >= 0; column--)
      {
        SpreadsheetCell temp = (SpreadsheetCell)((ObservableList)items.get(focusedCell.getRow())).get(column);
        if (!isEmpty(temp)) {
          return column;
        }
      }
    }
    return 0;
  }
  
  private boolean isEmpty(SpreadsheetCell cell)
  {
    return (cell.getGraphic() == null) && ((cell.getItem() == null) || (
      ((cell.getItem() instanceof Double)) && (((Double)cell.getItem()).isNaN())));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\spreadsheet\GridViewBehavior.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */