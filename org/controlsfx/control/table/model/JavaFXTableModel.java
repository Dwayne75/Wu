package org.controlsfx.control.table.model;

import javafx.scene.control.TableView;

abstract interface JavaFXTableModel<T>
{
  public abstract T getValueAt(int paramInt1, int paramInt2);
  
  public abstract void setValueAt(T paramT, int paramInt1, int paramInt2);
  
  public abstract int getRowCount();
  
  public abstract int getColumnCount();
  
  public abstract String getColumnName(int paramInt);
  
  public abstract void sort(TableView<TableModelRow<T>> paramTableView);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\table\model\JavaFXTableModel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */