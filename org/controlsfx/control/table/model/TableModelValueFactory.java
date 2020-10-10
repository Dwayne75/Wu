package org.controlsfx.control.table.model;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

class TableModelValueFactory<S, T>
  implements Callback<TableColumn.CellDataFeatures<TableModelRow<S>, T>, ObservableValue<T>>
{
  private final JavaFXTableModel<S> _tableModel;
  private final int _columnIndex;
  
  public TableModelValueFactory(JavaFXTableModel<S> tableModel, int columnIndex)
  {
    this._tableModel = tableModel;
    this._columnIndex = columnIndex;
  }
  
  public ObservableValue<T> call(TableColumn.CellDataFeatures<TableModelRow<S>, T> cdf)
  {
    TableModelRow<S> row = (TableModelRow)cdf.getValue();
    T valueAt = row.get(this._columnIndex);
    return (valueAt instanceof ObservableValue) ? (ObservableValue)valueAt : new ReadOnlyObjectWrapper(valueAt);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\table\model\TableModelValueFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */