package impl.org.controlsfx.skin;

import com.sun.javafx.event.EventHandlerManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class AutoCompletePopup<T>
  extends PopupControl
{
  private static final int TITLE_HEIGHT = 28;
  private final ObservableList<T> suggestions = FXCollections.observableArrayList();
  private StringConverter<T> converter;
  private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 10);
  
  public static class SuggestionEvent<TE>
    extends Event
  {
    public static final EventType<SuggestionEvent> SUGGESTION = new EventType("SUGGESTION");
    private final TE suggestion;
    
    public SuggestionEvent(TE suggestion)
    {
      super();
      this.suggestion = suggestion;
    }
    
    public TE getSuggestion()
    {
      return (TE)this.suggestion;
    }
  }
  
  public AutoCompletePopup()
  {
    setAutoFix(true);
    setAutoHide(true);
    setHideOnEscape(true);
    
    getStyleClass().add("auto-complete-popup");
  }
  
  public ObservableList<T> getSuggestions()
  {
    return this.suggestions;
  }
  
  public void show(Node node)
  {
    if ((node.getScene() == null) || (node.getScene().getWindow() == null)) {
      throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
    }
    if (isShowing()) {
      return;
    }
    Window parent = node.getScene().getWindow();
    show(parent, parent
    
      .getX() + node.localToScene(0.0D, 0.0D).getX() + node
      .getScene().getX(), parent
      .getY() + node.localToScene(0.0D, 0.0D).getY() + node
      .getScene().getY() + 28.0D);
  }
  
  public void setConverter(StringConverter<T> converter)
  {
    this.converter = converter;
  }
  
  public StringConverter<T> getConverter()
  {
    return this.converter;
  }
  
  public final void setVisibleRowCount(int value)
  {
    this.visibleRowCount.set(value);
  }
  
  public final int getVisibleRowCount()
  {
    return this.visibleRowCount.get();
  }
  
  public final IntegerProperty visibleRowCountProperty()
  {
    return this.visibleRowCount;
  }
  
  private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
  
  public final ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestionProperty()
  {
    return this.onSuggestion;
  }
  
  public final void setOnSuggestion(EventHandler<SuggestionEvent<T>> value)
  {
    onSuggestionProperty().set(value);
  }
  
  public final EventHandler<SuggestionEvent<T>> getOnSuggestion()
  {
    return (EventHandler)onSuggestionProperty().get();
  }
  
  private ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestion = new ObjectPropertyBase()
  {
    protected void invalidated()
    {
      AutoCompletePopup.this.eventHandlerManager.setEventHandler(AutoCompletePopup.SuggestionEvent.SUGGESTION, (EventHandler)get());
    }
    
    public Object getBean()
    {
      return AutoCompletePopup.this;
    }
    
    public String getName()
    {
      return "onSuggestion";
    }
  };
  public static final String DEFAULT_STYLE_CLASS = "auto-complete-popup";
  
  public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail)
  {
    return super.buildEventDispatchChain(tail).append(this.eventHandlerManager);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new AutoCompletePopupSkin(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\AutoCompletePopup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */