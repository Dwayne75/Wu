package org.controlsfx.control;

import com.sun.javafx.css.converters.EnumConverter;
import impl.org.controlsfx.skin.PlusMinusSliderSkin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.InputEvent;

public class PlusMinusSlider
  extends ControlsFXControl
{
  private static final String DEFAULT_STYLE_CLASS = "plus-minus-slider";
  private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
  private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");
  
  public PlusMinusSlider()
  {
    getStyleClass().add("plus-minus-slider");
    
    setOrientation(Orientation.HORIZONTAL);
    
    getProperties().addListener(new MapChangeListener()
    {
      public void onChanged(MapChangeListener.Change<? extends Object, ? extends Object> change)
      {
        if ((change.getKey().equals("plusminusslidervalue")) && 
          (change.getValueAdded() != null))
        {
          Double valueAdded = (Double)change.getValueAdded();
          PlusMinusSlider.this.value.set(valueAdded.doubleValue());
          change.getMap().remove("plusminusslidervalue");
        }
      }
    });
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(PlusMinusSlider.class, "plusminusslider.css");
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new PlusMinusSliderSkin(this);
  }
  
  private ReadOnlyDoubleWrapper value = new ReadOnlyDoubleWrapper(this, "value", 0.0D);
  private ObjectProperty<Orientation> orientation;
  
  public final ReadOnlyDoubleProperty valueProperty()
  {
    return this.value.getReadOnlyProperty();
  }
  
  public final double getValue()
  {
    return valueProperty().get();
  }
  
  public final void setOrientation(Orientation value)
  {
    orientationProperty().set(value);
  }
  
  public final Orientation getOrientation()
  {
    return this.orientation == null ? Orientation.HORIZONTAL : (Orientation)this.orientation.get();
  }
  
  public final ObjectProperty<Orientation> orientationProperty()
  {
    if (this.orientation == null) {
      this.orientation = new StyleableObjectProperty(null)
      {
        protected void invalidated()
        {
          boolean vertical = get() == Orientation.VERTICAL;
          PlusMinusSlider.this.pseudoClassStateChanged(PlusMinusSlider.VERTICAL_PSEUDOCLASS_STATE, vertical);
          
          PlusMinusSlider.this.pseudoClassStateChanged(PlusMinusSlider.HORIZONTAL_PSEUDOCLASS_STATE, !vertical);
        }
        
        public CssMetaData<PlusMinusSlider, Orientation> getCssMetaData()
        {
          return PlusMinusSlider.StyleableProperties.ORIENTATION;
        }
        
        public Object getBean()
        {
          return PlusMinusSlider.this;
        }
        
        public String getName()
        {
          return "orientation";
        }
      };
    }
    return this.orientation;
  }
  
  public final ObjectProperty<EventHandler<PlusMinusEvent>> onValueChangedProperty()
  {
    return this.onValueChanged;
  }
  
  public final void setOnValueChanged(EventHandler<PlusMinusEvent> value)
  {
    onValueChangedProperty().set(value);
  }
  
  public final EventHandler<PlusMinusEvent> getOnValueChanged()
  {
    return (EventHandler)onValueChangedProperty().get();
  }
  
  private ObjectProperty<EventHandler<PlusMinusEvent>> onValueChanged = new ObjectPropertyBase()
  {
    protected void invalidated()
    {
      PlusMinusSlider.this.setEventHandler(PlusMinusSlider.PlusMinusEvent.VALUE_CHANGED, (EventHandler)get());
    }
    
    public Object getBean()
    {
      return PlusMinusSlider.this;
    }
    
    public String getName()
    {
      return "onValueChanged";
    }
  };
  
  private static class StyleableProperties
  {
    private static final CssMetaData<PlusMinusSlider, Orientation> ORIENTATION = new CssMetaData("-fx-orientation", new EnumConverter(Orientation.class), Orientation.VERTICAL)
    {
      public Orientation getInitialValue(PlusMinusSlider node)
      {
        return node.getOrientation();
      }
      
      public boolean isSettable(PlusMinusSlider n)
      {
        return (n.orientation == null) || (!n.orientation.isBound());
      }
      
      public StyleableProperty<Orientation> getStyleableProperty(PlusMinusSlider n)
      {
        return (StyleableProperty)n.orientationProperty();
      }
    };
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
    
    static
    {
      List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList(Control.getClassCssMetaData());
      styleables.add(ORIENTATION);
      
      STYLEABLES = Collections.unmodifiableList(styleables);
    }
  }
  
  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
  {
    return StyleableProperties.STYLEABLES;
  }
  
  public static class PlusMinusEvent
    extends InputEvent
  {
    private static final long serialVersionUID = 2881004583512990781L;
    public static final EventType<PlusMinusEvent> ANY = new EventType(InputEvent.ANY, "ANY");
    public static final EventType<PlusMinusEvent> VALUE_CHANGED = new EventType(ANY, "VALUE_CHANGED");
    private double value;
    
    public PlusMinusEvent(Object source, EventTarget target, EventType<? extends InputEvent> eventType, double value)
    {
      super(target, eventType);
      
      this.value = value;
    }
    
    public double getValue()
    {
      return this.value;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\PlusMinusSlider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */