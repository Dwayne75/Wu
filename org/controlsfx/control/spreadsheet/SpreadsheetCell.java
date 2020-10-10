package org.controlsfx.control.spreadsheet;

import java.util.List;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public abstract interface SpreadsheetCell
{
  public static final EventType EDITABLE_EVENT_TYPE = new EventType("EditableEventType");
  public static final EventType WRAP_EVENT_TYPE = new EventType("WrapTextEventType");
  public static final EventType CORNER_EVENT_TYPE = new EventType("CornerEventType");
  
  public abstract boolean match(SpreadsheetCell paramSpreadsheetCell);
  
  public abstract void setItem(Object paramObject);
  
  public abstract Object getItem();
  
  public abstract ObjectProperty<Object> itemProperty();
  
  public abstract boolean isEditable();
  
  public abstract void setEditable(boolean paramBoolean);
  
  public abstract boolean isWrapText();
  
  public abstract void setWrapText(boolean paramBoolean);
  
  public abstract boolean hasPopup();
  
  public abstract void setHasPopup(boolean paramBoolean);
  
  public abstract List<MenuItem> getPopupItems();
  
  public abstract void setStyle(String paramString);
  
  public abstract String getStyle();
  
  public abstract StringProperty styleProperty();
  
  public abstract void activateCorner(CornerPosition paramCornerPosition);
  
  public abstract void deactivateCorner(CornerPosition paramCornerPosition);
  
  public abstract boolean isCornerActivated(CornerPosition paramCornerPosition);
  
  public abstract StringProperty formatProperty();
  
  public abstract String getFormat();
  
  public abstract void setFormat(String paramString);
  
  public abstract ReadOnlyStringProperty textProperty();
  
  public abstract String getText();
  
  public abstract SpreadsheetCellType getCellType();
  
  public abstract int getRow();
  
  public abstract int getColumn();
  
  public abstract int getRowSpan();
  
  public abstract void setRowSpan(int paramInt);
  
  public abstract int getColumnSpan();
  
  public abstract void setColumnSpan(int paramInt);
  
  public abstract ObservableSet<String> getStyleClass();
  
  public abstract ObjectProperty<Node> graphicProperty();
  
  public abstract void setGraphic(Node paramNode);
  
  public abstract Node getGraphic();
  
  public abstract Optional<String> getTooltip();
  
  public abstract void addEventHandler(EventType<Event> paramEventType, EventHandler<Event> paramEventHandler);
  
  public abstract void removeEventHandler(EventType<Event> paramEventType, EventHandler<Event> paramEventHandler);
  
  public static enum CornerPosition
  {
    TOP_LEFT,  TOP_RIGHT,  BOTTOM_RIGHT,  BOTTOM_LEFT;
    
    private CornerPosition() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\spreadsheet\SpreadsheetCell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */