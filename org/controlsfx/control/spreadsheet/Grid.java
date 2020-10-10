package org.controlsfx.control.spreadsheet;

import java.util.Collection;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;

public abstract interface Grid
{
  public static final double AUTOFIT = -1.0D;
  
  public abstract int getRowCount();
  
  public abstract int getColumnCount();
  
  public abstract ObservableList<ObservableList<SpreadsheetCell>> getRows();
  
  public abstract void setCellValue(int paramInt1, int paramInt2, Object paramObject);
  
  public abstract double getRowHeight(int paramInt);
  
  public abstract boolean isRowResizable(int paramInt);
  
  public abstract ObservableList<String> getRowHeaders();
  
  public abstract ObservableList<String> getColumnHeaders();
  
  public abstract void spanRow(int paramInt1, int paramInt2, int paramInt3);
  
  public abstract void spanColumn(int paramInt1, int paramInt2, int paramInt3);
  
  public abstract void setRows(Collection<ObservableList<SpreadsheetCell>> paramCollection);
  
  public abstract boolean isDisplaySelection();
  
  public abstract void setDisplaySelection(boolean paramBoolean);
  
  public abstract BooleanProperty displaySelectionProperty();
  
  public abstract void setCellDisplaySelection(int paramInt1, int paramInt2, boolean paramBoolean);
  
  public abstract boolean isCellDisplaySelection(int paramInt1, int paramInt2);
  
  public abstract <E extends GridChange> void addEventHandler(EventType<E> paramEventType, EventHandler<E> paramEventHandler);
  
  public abstract <E extends GridChange> void removeEventHandler(EventType<E> paramEventType, EventHandler<E> paramEventHandler);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\spreadsheet\Grid.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */