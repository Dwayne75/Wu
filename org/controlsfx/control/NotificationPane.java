package org.controlsfx.control;

import impl.org.controlsfx.skin.NotificationPaneSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import org.controlsfx.control.action.Action;

public class NotificationPane
  extends ControlsFXControl
{
  public static final String STYLE_CLASS_DARK = "dark";
  public static final EventType<Event> ON_SHOWING = new EventType(Event.ANY, "NOTIFICATION_PANE_ON_SHOWING");
  public static final EventType<Event> ON_SHOWN = new EventType(Event.ANY, "NOTIFICATION_PANE_ON_SHOWN");
  public static final EventType<Event> ON_HIDING = new EventType(Event.ANY, "NOTIFICATION_PANE_ON_HIDING");
  public static final EventType<Event> ON_HIDDEN = new EventType(Event.ANY, "NOTIFICATION_PANE_ON_HIDDEN");
  
  public NotificationPane()
  {
    this(null);
  }
  
  public NotificationPane(Node content)
  {
    getStyleClass().add("notification-pane");
    setContent(content);
    
    updateStyleClasses();
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new NotificationPaneSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(NotificationPane.class, "notificationpane.css");
  }
  
  private ObjectProperty<Node> content = new SimpleObjectProperty(this, "content");
  
  public final ObjectProperty<Node> contentProperty()
  {
    return this.content;
  }
  
  public final void setContent(Node value)
  {
    this.content.set(value);
  }
  
  public final Node getContent()
  {
    return (Node)this.content.get();
  }
  
  private StringProperty text = new SimpleStringProperty(this, "text");
  
  public final StringProperty textProperty()
  {
    return this.text;
  }
  
  public final void setText(String value)
  {
    this.text.set(value);
  }
  
  public final String getText()
  {
    return (String)this.text.get();
  }
  
  private ObjectProperty<Node> graphic = new SimpleObjectProperty(this, "graphic");
  
  public final ObjectProperty<Node> graphicProperty()
  {
    return this.graphic;
  }
  
  public final void setGraphic(Node value)
  {
    this.graphic.set(value);
  }
  
  public final Node getGraphic()
  {
    return (Node)this.graphic.get();
  }
  
  private ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");
  
  public final ReadOnlyBooleanProperty showingProperty()
  {
    return this.showing.getReadOnlyProperty();
  }
  
  private final void setShowing(boolean value)
  {
    this.showing.set(value);
  }
  
  public final boolean isShowing()
  {
    return this.showing.get();
  }
  
  private BooleanProperty showFromTop = new SimpleBooleanProperty(this, "showFromTop", true)
  {
    protected void invalidated()
    {
      NotificationPane.this.updateStyleClasses();
    }
  };
  
  public final BooleanProperty showFromTopProperty()
  {
    return this.showFromTop;
  }
  
  public final void setShowFromTop(boolean value)
  {
    this.showFromTop.set(value);
  }
  
  public final boolean isShowFromTop()
  {
    return this.showFromTop.get();
  }
  
  public final ObjectProperty<EventHandler<Event>> onShowingProperty()
  {
    return this.onShowing;
  }
  
  public final void setOnShowing(EventHandler<Event> value)
  {
    onShowingProperty().set(value);
  }
  
  public final EventHandler<Event> getOnShowing()
  {
    return (EventHandler)onShowingProperty().get();
  }
  
  private ObjectProperty<EventHandler<Event>> onShowing = new SimpleObjectProperty(this, "onShowing")
  {
    protected void invalidated()
    {
      NotificationPane.this.setEventHandler(NotificationPane.ON_SHOWING, (EventHandler)get());
    }
  };
  
  public final ObjectProperty<EventHandler<Event>> onShownProperty()
  {
    return this.onShown;
  }
  
  public final void setOnShown(EventHandler<Event> value)
  {
    onShownProperty().set(value);
  }
  
  public final EventHandler<Event> getOnShown()
  {
    return (EventHandler)onShownProperty().get();
  }
  
  private ObjectProperty<EventHandler<Event>> onShown = new SimpleObjectProperty(this, "onShown")
  {
    protected void invalidated()
    {
      NotificationPane.this.setEventHandler(NotificationPane.ON_SHOWN, (EventHandler)get());
    }
  };
  
  public final ObjectProperty<EventHandler<Event>> onHidingProperty()
  {
    return this.onHiding;
  }
  
  public final void setOnHiding(EventHandler<Event> value)
  {
    onHidingProperty().set(value);
  }
  
  public final EventHandler<Event> getOnHiding()
  {
    return (EventHandler)onHidingProperty().get();
  }
  
  private ObjectProperty<EventHandler<Event>> onHiding = new SimpleObjectProperty(this, "onHiding")
  {
    protected void invalidated()
    {
      NotificationPane.this.setEventHandler(NotificationPane.ON_HIDING, (EventHandler)get());
    }
  };
  
  public final ObjectProperty<EventHandler<Event>> onHiddenProperty()
  {
    return this.onHidden;
  }
  
  public final void setOnHidden(EventHandler<Event> value)
  {
    onHiddenProperty().set(value);
  }
  
  public final EventHandler<Event> getOnHidden()
  {
    return (EventHandler)onHiddenProperty().get();
  }
  
  private ObjectProperty<EventHandler<Event>> onHidden = new SimpleObjectProperty(this, "onHidden")
  {
    protected void invalidated()
    {
      NotificationPane.this.setEventHandler(NotificationPane.ON_HIDDEN, (EventHandler)get());
    }
  };
  private BooleanProperty closeButtonVisible = new SimpleBooleanProperty(this, "closeButtonVisible", true);
  
  public final BooleanProperty closeButtonVisibleProperty()
  {
    return this.closeButtonVisible;
  }
  
  public final void setCloseButtonVisible(boolean value)
  {
    this.closeButtonVisible.set(value);
  }
  
  public final boolean isCloseButtonVisible()
  {
    return this.closeButtonVisible.get();
  }
  
  private final ObservableList<Action> actions = FXCollections.observableArrayList();
  private static final String DEFAULT_STYLE_CLASS = "notification-pane";
  
  public final ObservableList<Action> getActions()
  {
    return this.actions;
  }
  
  public void show()
  {
    setShowing(true);
  }
  
  public void show(final String text)
  {
    hideAndThen(new Runnable()
    {
      public void run()
      {
        NotificationPane.this.setText(text);
        NotificationPane.this.setShowing(true);
      }
    });
  }
  
  public void show(final String text, final Node graphic)
  {
    hideAndThen(new Runnable()
    {
      public void run()
      {
        NotificationPane.this.setText(text);
        NotificationPane.this.setGraphic(graphic);
        NotificationPane.this.setShowing(true);
      }
    });
  }
  
  public void show(final String text, final Node graphic, final Action... actions)
  {
    hideAndThen(new Runnable()
    {
      public void run()
      {
        NotificationPane.this.setText(text);
        NotificationPane.this.setGraphic(graphic);
        if (actions == null) {
          NotificationPane.this.getActions().clear();
        } else {
          for (Action action : actions) {
            if (action != null) {
              NotificationPane.this.getActions().add(action);
            }
          }
        }
        NotificationPane.this.setShowing(true);
      }
    });
  }
  
  public void hide()
  {
    setShowing(false);
  }
  
  private void updateStyleClasses()
  {
    getStyleClass().removeAll(new String[] { "top", "bottom" });
    getStyleClass().add(isShowFromTop() ? "top" : "bottom");
  }
  
  private void hideAndThen(final Runnable r)
  {
    if (isShowing())
    {
      EventHandler<Event> eventHandler = new EventHandler()
      {
        public void handle(Event e)
        {
          r.run();
          NotificationPane.this.removeEventHandler(NotificationPane.ON_HIDDEN, this);
        }
      };
      addEventHandler(ON_HIDDEN, eventHandler);
      hide();
    }
    else
    {
      r.run();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\NotificationPane.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */