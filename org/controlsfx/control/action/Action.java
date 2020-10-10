package org.controlsfx.control.action;

import impl.org.controlsfx.i18n.Localization;
import impl.org.controlsfx.i18n.SimpleLocalizedStringProperty;
import java.util.function.Consumer;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;

public class Action
  implements EventHandler<ActionEvent>
{
  private boolean locked = false;
  private Consumer<ActionEvent> eventHandler;
  private StringProperty style;
  
  public Action(@NamedArg("text") String text)
  {
    this(text, null);
  }
  
  public Action(Consumer<ActionEvent> eventHandler)
  {
    this("", eventHandler);
  }
  
  public Action(@NamedArg("text") String text, Consumer<ActionEvent> eventHandler)
  {
    setText(text);
    setEventHandler(eventHandler);
    getStyleClass().add("action");
  }
  
  protected void lock()
  {
    this.locked = true;
  }
  
  public final void setStyle(String value)
  {
    styleProperty().set(value);
  }
  
  public final String getStyle()
  {
    return this.style == null ? "" : (String)this.style.get();
  }
  
  public final StringProperty styleProperty()
  {
    if (this.style == null) {
      this.style = new SimpleStringProperty(this, "style")
      {
        public void set(String style)
        {
          if (Action.this.locked) {
            throw new UnsupportedOperationException("The action is immutable, property change support is disabled.");
          }
          super.set(style);
        }
      };
    }
    return this.style;
  }
  
  private final ObservableList<String> styleClass = FXCollections.observableArrayList();
  
  public ObservableList<String> getStyleClass()
  {
    return this.styleClass;
  }
  
  private final BooleanProperty selectedProperty = new SimpleBooleanProperty(this, "selected")
  {
    public void set(boolean selected)
    {
      if (Action.this.locked) {
        throw new UnsupportedOperationException("The action is immutable, property change support is disabled.");
      }
      super.set(selected);
    }
  };
  
  public final BooleanProperty selectedProperty()
  {
    return this.selectedProperty;
  }
  
  public final boolean isSelected()
  {
    return this.selectedProperty.get();
  }
  
  public final void setSelected(boolean selected)
  {
    this.selectedProperty.set(selected);
  }
  
  private final StringProperty textProperty = new SimpleLocalizedStringProperty(this, "text")
  {
    public void set(String value)
    {
      if (Action.this.locked) {
        throw new RuntimeException("The action is immutable, property change support is disabled.");
      }
      super.set(value);
    }
  };
  
  public final StringProperty textProperty()
  {
    return this.textProperty;
  }
  
  public final String getText()
  {
    return (String)this.textProperty.get();
  }
  
  public final void setText(String value)
  {
    this.textProperty.set(value);
  }
  
  private final BooleanProperty disabledProperty = new SimpleBooleanProperty(this, "disabled")
  {
    public void set(boolean value)
    {
      if (Action.this.locked) {
        throw new RuntimeException("The action is immutable, property change support is disabled.");
      }
      super.set(value);
    }
  };
  
  public final BooleanProperty disabledProperty()
  {
    return this.disabledProperty;
  }
  
  public final boolean isDisabled()
  {
    return this.disabledProperty.get();
  }
  
  public final void setDisabled(boolean value)
  {
    this.disabledProperty.set(value);
  }
  
  private final StringProperty longTextProperty = new SimpleLocalizedStringProperty(this, "longText")
  {
    public void set(String value)
    {
      if (Action.this.locked) {
        throw new RuntimeException("The action is immutable, property change support is disabled.");
      }
      super.set(value);
    }
  };
  
  public final StringProperty longTextProperty()
  {
    return this.longTextProperty;
  }
  
  public final String getLongText()
  {
    return Localization.localize((String)this.longTextProperty.get());
  }
  
  public final void setLongText(String value)
  {
    this.longTextProperty.set(value);
  }
  
  private final ObjectProperty<Node> graphicProperty = new SimpleObjectProperty(this, "graphic")
  {
    public void set(Node value)
    {
      if (Action.this.locked) {
        throw new RuntimeException("The action is immutable, property change support is disabled.");
      }
      super.set(value);
    }
  };
  
  public final ObjectProperty<Node> graphicProperty()
  {
    return this.graphicProperty;
  }
  
  public final Node getGraphic()
  {
    return (Node)this.graphicProperty.get();
  }
  
  public final void setGraphic(Node value)
  {
    this.graphicProperty.set(value);
  }
  
  private final ObjectProperty<KeyCombination> acceleratorProperty = new SimpleObjectProperty(this, "accelerator")
  {
    public void set(KeyCombination value)
    {
      if (Action.this.locked) {
        throw new RuntimeException("The action is immutable, property change support is disabled.");
      }
      super.set(value);
    }
  };
  private ObservableMap<Object, Object> props;
  
  public final ObjectProperty<KeyCombination> acceleratorProperty()
  {
    return this.acceleratorProperty;
  }
  
  public final KeyCombination getAccelerator()
  {
    return (KeyCombination)this.acceleratorProperty.get();
  }
  
  public final void setAccelerator(KeyCombination value)
  {
    this.acceleratorProperty.set(value);
  }
  
  public final synchronized ObservableMap<Object, Object> getProperties()
  {
    if (this.props == null) {
      this.props = FXCollections.observableHashMap();
    }
    return this.props;
  }
  
  protected Consumer<ActionEvent> getEventHandler()
  {
    return this.eventHandler;
  }
  
  protected void setEventHandler(Consumer<ActionEvent> eventHandler)
  {
    this.eventHandler = eventHandler;
  }
  
  public final void handle(ActionEvent event)
  {
    if ((this.eventHandler != null) && (!isDisabled())) {
      this.eventHandler.accept(event);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\action\Action.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */