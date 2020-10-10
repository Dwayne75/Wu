package org.controlsfx.control;

import com.sun.javafx.event.EventHandlerManager;
import impl.org.controlsfx.skin.HyperlinkLabelSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.control.Skin;

public class HyperlinkLabel
  extends ControlsFXControl
  implements EventTarget
{
  private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
  
  public HyperlinkLabel()
  {
    this(null);
  }
  
  public HyperlinkLabel(String text)
  {
    setText(text);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new HyperlinkLabelSkin(this);
  }
  
  private final StringProperty text = new SimpleStringProperty(this, "text");
  private ObjectProperty<EventHandler<ActionEvent>> onAction;
  
  public final StringProperty textProperty()
  {
    return this.text;
  }
  
  public final String getText()
  {
    return (String)this.text.get();
  }
  
  public final void setText(String value)
  {
    this.text.set(value);
  }
  
  public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty()
  {
    if (this.onAction == null) {
      this.onAction = new SimpleObjectProperty(this, "onAction")
      {
        protected void invalidated()
        {
          HyperlinkLabel.this.eventHandlerManager.setEventHandler(ActionEvent.ACTION, (EventHandler)get());
        }
      };
    }
    return this.onAction;
  }
  
  public final void setOnAction(EventHandler<ActionEvent> value)
  {
    onActionProperty().set(value);
  }
  
  public final EventHandler<ActionEvent> getOnAction()
  {
    return this.onAction == null ? null : (EventHandler)this.onAction.get();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\HyperlinkLabel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */