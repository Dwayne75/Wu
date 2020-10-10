package org.controlsfx.control.spreadsheet;

import com.sun.javafx.event.EventHandlerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public class SpreadsheetCellBase
  implements SpreadsheetCell, EventTarget
{
  private static final int EDITABLE_BIT_POSITION = 4;
  private static final int WRAP_BIT_POSITION = 5;
  private static final int POPUP_BIT_POSITION = 6;
  private final SpreadsheetCellType type;
  private final int row;
  private final int column;
  private int rowSpan;
  private int columnSpan;
  private final StringProperty format;
  private final StringProperty text;
  private final StringProperty styleProperty;
  private final ObjectProperty<Node> graphic;
  private String tooltip;
  private int propertyContainer = 0;
  private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
  private ObservableSet<String> styleClass;
  private List<MenuItem> actionsList;
  
  public SpreadsheetCellBase(int row, int column, int rowSpan, int columnSpan)
  {
    this(row, column, rowSpan, columnSpan, SpreadsheetCellType.OBJECT);
  }
  
  public SpreadsheetCellBase(int row, int column, int rowSpan, int columnSpan, SpreadsheetCellType<?> type)
  {
    this.row = row;
    this.column = column;
    this.rowSpan = rowSpan;
    this.columnSpan = columnSpan;
    this.type = type;
    this.text = new SimpleStringProperty("");
    this.format = new SimpleStringProperty("");
    this.graphic = new SimpleObjectProperty();
    this.format.addListener(new ChangeListener()
    {
      public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2)
      {
        SpreadsheetCellBase.this.updateText();
      }
    });
    setEditable(true);
    getStyleClass().add("spreadsheet-cell");
    this.styleProperty = new SimpleStringProperty();
  }
  
  public boolean match(SpreadsheetCell cell)
  {
    return this.type.match(cell);
  }
  
  private final ObjectProperty<Object> item = new SimpleObjectProperty(this, "item")
  {
    protected void invalidated()
    {
      SpreadsheetCellBase.this.updateText();
    }
  };
  
  public final void setItem(Object value)
  {
    if (isEditable()) {
      this.item.set(value);
    }
  }
  
  public final Object getItem()
  {
    return this.item.get();
  }
  
  public final ObjectProperty<Object> itemProperty()
  {
    return this.item;
  }
  
  public final boolean isEditable()
  {
    return isSet(4);
  }
  
  public final void setEditable(boolean editable)
  {
    if (setMask(editable, 4)) {
      Event.fireEvent(this, new Event(EDITABLE_EVENT_TYPE));
    }
  }
  
  public boolean isWrapText()
  {
    return isSet(5);
  }
  
  public void setWrapText(boolean wrapText)
  {
    if (setMask(wrapText, 5)) {
      Event.fireEvent(this, new Event(WRAP_EVENT_TYPE));
    }
  }
  
  public boolean hasPopup()
  {
    return isSet(6);
  }
  
  public void setHasPopup(boolean value)
  {
    setMask(value, 6);
    
    Event.fireEvent(this, new Event(CORNER_EVENT_TYPE));
  }
  
  public List<MenuItem> getPopupItems()
  {
    if (this.actionsList == null) {
      this.actionsList = new ArrayList();
    }
    return this.actionsList;
  }
  
  public final StringProperty formatProperty()
  {
    return this.format;
  }
  
  public final String getFormat()
  {
    return (String)this.format.get();
  }
  
  public final void setFormat(String format)
  {
    formatProperty().set(format);
    updateText();
  }
  
  public final ReadOnlyStringProperty textProperty()
  {
    return this.text;
  }
  
  public final String getText()
  {
    return (String)this.text.get();
  }
  
  public final SpreadsheetCellType getCellType()
  {
    return this.type;
  }
  
  public final int getRow()
  {
    return this.row;
  }
  
  public final int getColumn()
  {
    return this.column;
  }
  
  public final int getRowSpan()
  {
    return this.rowSpan;
  }
  
  public final void setRowSpan(int rowSpan)
  {
    this.rowSpan = rowSpan;
  }
  
  public final int getColumnSpan()
  {
    return this.columnSpan;
  }
  
  public final void setColumnSpan(int columnSpan)
  {
    this.columnSpan = columnSpan;
  }
  
  public final ObservableSet<String> getStyleClass()
  {
    if (this.styleClass == null) {
      this.styleClass = FXCollections.observableSet(new String[0]);
    }
    return this.styleClass;
  }
  
  public void setStyle(String style)
  {
    this.styleProperty.set(style);
  }
  
  public String getStyle()
  {
    return (String)this.styleProperty.get();
  }
  
  public StringProperty styleProperty()
  {
    return this.styleProperty;
  }
  
  public ObjectProperty<Node> graphicProperty()
  {
    return this.graphic;
  }
  
  public void setGraphic(Node graphic)
  {
    this.graphic.set(graphic);
  }
  
  public Node getGraphic()
  {
    return (Node)this.graphic.get();
  }
  
  public Optional<String> getTooltip()
  {
    return Optional.ofNullable(this.tooltip);
  }
  
  public void setTooltip(String tooltip)
  {
    this.tooltip = tooltip;
  }
  
  public void activateCorner(SpreadsheetCell.CornerPosition position)
  {
    if (setMask(true, getCornerBitNumber(position))) {
      Event.fireEvent(this, new Event(CORNER_EVENT_TYPE));
    }
  }
  
  public void deactivateCorner(SpreadsheetCell.CornerPosition position)
  {
    if (setMask(false, getCornerBitNumber(position))) {
      Event.fireEvent(this, new Event(CORNER_EVENT_TYPE));
    }
  }
  
  public boolean isCornerActivated(SpreadsheetCell.CornerPosition position)
  {
    return isSet(getCornerBitNumber(position));
  }
  
  public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
  {
    return tail.append(this.eventHandlerManager);
  }
  
  public String toString()
  {
    return "cell[" + this.row + "][" + this.column + "]" + this.rowSpan + "-" + this.columnSpan;
  }
  
  public final boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SpreadsheetCell)) {
      return false;
    }
    SpreadsheetCell otherCell = (SpreadsheetCell)obj;
    return (otherCell.getRow() == this.row) && (otherCell.getColumn() == this.column) && 
      (Objects.equals(otherCell.getText(), getText())) && 
      (this.rowSpan == otherCell.getRowSpan()) && 
      (this.columnSpan == otherCell.getColumnSpan()) && 
      (Objects.equals(getStyleClass(), otherCell.getStyleClass()));
  }
  
  public final int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + this.column;
    result = 31 * result + this.row;
    result = 31 * result + this.rowSpan;
    result = 31 * result + this.columnSpan;
    result = 31 * result + Objects.hashCode(getText());
    result = 31 * result + Objects.hashCode(getStyleClass());
    return result;
  }
  
  public void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler)
  {
    this.eventHandlerManager.addEventHandler(eventType, eventHandler);
  }
  
  public void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler)
  {
    this.eventHandlerManager.removeEventHandler(eventType, eventHandler);
  }
  
  private void updateText()
  {
    if (getItem() == null) {
      this.text.setValue("");
    } else if (!"".equals(getFormat())) {
      this.text.setValue(this.type.toString(getItem(), getFormat()));
    } else {
      this.text.setValue(this.type.toString(getItem()));
    }
  }
  
  private int getCornerBitNumber(SpreadsheetCell.CornerPosition position)
  {
    switch (position)
    {
    case TOP_LEFT: 
      return 0;
    case TOP_RIGHT: 
      return 1;
    case BOTTOM_RIGHT: 
      return 2;
    }
    return 3;
  }
  
  private boolean setMask(boolean flag, int position)
  {
    int oldCorner = this.propertyContainer;
    if (flag) {
      this.propertyContainer |= 1 << position;
    } else {
      this.propertyContainer &= (1 << position ^ 0xFFFFFFFF);
    }
    return this.propertyContainer != oldCorner;
  }
  
  private boolean isSet(int position)
  {
    return (this.propertyContainer & 1 << position) != 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\spreadsheet\SpreadsheetCellBase.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */