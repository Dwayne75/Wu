package org.controlsfx.control.spreadsheet;

import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.util.Callback;

public class FilterBase
  implements Filter
{
  private final SpreadsheetView spv;
  private final int column;
  private MenuButton menuButton;
  private BitSet hiddenRows;
  private Set<String> stringSet = new HashSet();
  private Set<String> copySet = new HashSet();
  
  public FilterBase(SpreadsheetView spv, int column)
  {
    this.spv = spv;
    this.column = column;
  }
  
  public MenuButton getMenuButton()
  {
    if (this.menuButton == null)
    {
      this.menuButton = new MenuButton();
      this.menuButton.getStyleClass().add("filter-menu-button");
      
      this.menuButton.showingProperty().addListener(new ChangeListener()
      {
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
        {
          if (newValue.booleanValue())
          {
            FilterBase.this.addMenuItems();
            FilterBase.this.hiddenRows = new BitSet(FilterBase.this.spv.getHiddenRows().size());
            FilterBase.this.hiddenRows.or(FilterBase.this.spv.getHiddenRows());
          }
          else
          {
            for (int i = FilterBase.this.spv.getFilteredRow() + 1; i < FilterBase.this.spv.getGrid().getRowCount(); i++) {
              FilterBase.this.hiddenRows.set(i, !FilterBase.this.copySet.contains(((SpreadsheetCell)((ObservableList)FilterBase.this.spv.getGrid().getRows().get(i)).get(FilterBase.this.column)).getText()));
            }
            FilterBase.this.spv.setHiddenRows(FilterBase.this.hiddenRows);
          }
        }
      });
    }
    return this.menuButton;
  }
  
  private void addMenuItems()
  {
    if (this.menuButton.getItems().isEmpty())
    {
      final MenuItem sortItem = new MenuItem("Sort ascending");
      sortItem.setOnAction(new EventHandler()
      {
        public void handle(ActionEvent event)
        {
          if (FilterBase.this.spv.getComparator() == FilterBase.this.ascendingComp)
          {
            FilterBase.this.spv.setComparator(FilterBase.this.descendingComp);
            sortItem.setText("Remove sort");
          }
          else if (FilterBase.this.spv.getComparator() == FilterBase.this.descendingComp)
          {
            FilterBase.this.spv.setComparator(null);
            sortItem.setText("Sort ascending");
          }
          else
          {
            FilterBase.this.spv.setComparator(FilterBase.this.ascendingComp);
            sortItem.setText("Sort descending");
          }
        }
      });
      ListView<String> listView = new ListView();
      listView.setCellFactory(new Callback()
      {
        public ListCell<String> call(ListView<String> param)
        {
          new ListCell()
          {
            public void updateItem(final String item, boolean empty)
            {
              super.updateItem(item, empty);
              setText(item);
              if (item != null)
              {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(FilterBase.this.copySet.contains(item));
                checkBox.selectedProperty().addListener(new ChangeListener()
                {
                  public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                  {
                    if (newValue.booleanValue()) {
                      FilterBase.this.copySet.add(item);
                    } else {
                      FilterBase.this.copySet.remove(item);
                    }
                  }
                });
                setGraphic(checkBox);
              }
            }
          };
        }
      });
      for (int i = this.spv.getFilteredRow() + 1; i < this.spv.getGrid().getRowCount(); i++) {
        this.stringSet.add(((SpreadsheetCell)((ObservableList)this.spv.getGrid().getRows().get(i)).get(this.column)).getText());
      }
      listView.setItems(FXCollections.observableArrayList(this.stringSet));
      
      CustomMenuItem customMenuItem = new CustomMenuItem(listView);
      customMenuItem.setHideOnClick(false);
      this.menuButton.getItems().addAll(new MenuItem[] { sortItem, customMenuItem });
    }
    this.copySet.clear();
    for (int i = this.spv.getFilteredRow() + 1; i < this.spv.getGrid().getRowCount(); i++) {
      if (!this.spv.getHiddenRows().get(i)) {
        this.copySet.add(((SpreadsheetCell)((ObservableList)this.spv.getGrid().getRows().get(i)).get(this.column)).getText());
      }
    }
  }
  
  private final Comparator ascendingComp = new Comparator()
  {
    public int compare(ObservableList<SpreadsheetCell> o1, ObservableList<SpreadsheetCell> o2)
    {
      SpreadsheetCell cell1 = (SpreadsheetCell)o1.get(FilterBase.this.column);
      SpreadsheetCell cell2 = (SpreadsheetCell)o2.get(FilterBase.this.column);
      if (cell1.getRow() <= FilterBase.this.spv.getFilteredRow()) {
        return Integer.compare(cell1.getRow(), cell2.getRow());
      }
      if (cell2.getRow() <= FilterBase.this.spv.getFilteredRow()) {
        return Integer.compare(cell1.getRow(), cell2.getRow());
      }
      if ((cell1.getCellType() == SpreadsheetCellType.INTEGER) && (cell2.getCellType() == SpreadsheetCellType.INTEGER)) {
        return Integer.compare(((Integer)cell1.getItem()).intValue(), ((Integer)cell2.getItem()).intValue());
      }
      if ((cell1.getCellType() == SpreadsheetCellType.DOUBLE) && (cell2.getCellType() == SpreadsheetCellType.DOUBLE)) {
        return Double.compare(((Double)cell1.getItem()).doubleValue(), ((Double)cell2.getItem()).doubleValue());
      }
      return cell1.getText().compareToIgnoreCase(cell2.getText());
    }
  };
  private final Comparator descendingComp = new Comparator()
  {
    public int compare(ObservableList<SpreadsheetCell> o1, ObservableList<SpreadsheetCell> o2)
    {
      SpreadsheetCell cell1 = (SpreadsheetCell)o1.get(FilterBase.this.column);
      SpreadsheetCell cell2 = (SpreadsheetCell)o2.get(FilterBase.this.column);
      if (cell1.getRow() <= FilterBase.this.spv.getFilteredRow()) {
        return Integer.compare(cell1.getRow(), cell2.getRow());
      }
      if (cell2.getRow() <= FilterBase.this.spv.getFilteredRow()) {
        return Integer.compare(cell1.getRow(), cell2.getRow());
      }
      if ((cell1.getCellType() == SpreadsheetCellType.INTEGER) && (cell2.getCellType() == SpreadsheetCellType.INTEGER)) {
        return Integer.compare(((Integer)cell2.getItem()).intValue(), ((Integer)cell1.getItem()).intValue());
      }
      if ((cell1.getCellType() == SpreadsheetCellType.DOUBLE) && (cell2.getCellType() == SpreadsheetCellType.DOUBLE)) {
        return Double.compare(((Double)cell2.getItem()).doubleValue(), ((Double)cell1.getItem()).doubleValue());
      }
      return cell2.getText().compareToIgnoreCase(cell1.getText());
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\spreadsheet\FilterBase.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */