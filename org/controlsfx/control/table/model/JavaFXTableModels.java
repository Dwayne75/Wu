package org.controlsfx.control.table.model;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

class JavaFXTableModels
{
  public static <S> JavaFXTableModel<S> wrap(TableModel tableModel)
  {
    new JavaFXTableModel()
    {
      final TableRowSorter<TableModel> sorter;
      
      public S getValueAt(int rowIndex, int columnIndex)
      {
        return (S)this.val$tableModel.getValueAt(this.sorter.convertRowIndexToView(rowIndex), columnIndex);
      }
      
      public void setValueAt(S value, int rowIndex, int columnIndex)
      {
        this.val$tableModel.setValueAt(value, rowIndex, columnIndex);
      }
      
      public int getRowCount()
      {
        return this.val$tableModel.getRowCount();
      }
      
      public int getColumnCount()
      {
        return this.val$tableModel.getColumnCount();
      }
      
      public String getColumnName(int columnIndex)
      {
        return this.val$tableModel.getColumnName(columnIndex);
      }
      
      public void sort(TableView<TableModelRow<S>> table)
      {
        List<RowSorter.SortKey> sortKeys = new ArrayList();
        for (TableColumn<TableModelRow<S>, ?> column : table.getSortOrder())
        {
          int columnIndex = table.getVisibleLeafIndex(column);
          TableColumn.SortType sortType = column.getSortType();
          SortOrder sortOrder = sortType == TableColumn.SortType.DESCENDING ? SortOrder.DESCENDING : sortType == TableColumn.SortType.ASCENDING ? SortOrder.ASCENDING : SortOrder.UNSORTED;
          
          RowSorter.SortKey sortKey = new RowSorter.SortKey(columnIndex, sortOrder);
          sortKeys.add(sortKey);
          
          this.sorter.setComparator(columnIndex, column.getComparator());
        }
        this.sorter.setSortKeys(sortKeys);
        this.sorter.sort();
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\table\model\JavaFXTableModels.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */