package org.controlsfx.control;

import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.EnumConverter;
import com.sun.javafx.css.converters.SizeConverter;
import impl.org.controlsfx.skin.RangeSliderSkin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleConverter;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;
import org.controlsfx.tools.Utils;

public class RangeSlider
  extends ControlsFXControl
{
  public RangeSlider()
  {
    this(0.0D, 1.0D, 0.25D, 0.75D);
  }
  
  public RangeSlider(double min, double max, double lowValue, double highValue)
  {
    getStyleClass().setAll(new String[] { "range-slider" });
    
    setMax(max);
    setMin(min);
    adjustValues();
    setLowValue(lowValue);
    setHighValue(highValue);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(RangeSlider.class, "rangeslider.css");
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new RangeSliderSkin(this);
  }
  
  public final DoubleProperty lowValueProperty()
  {
    return this.lowValue;
  }
  
  private DoubleProperty lowValue = new SimpleDoubleProperty(this, "lowValue", 0.0D)
  {
    protected void invalidated()
    {
      RangeSlider.this.adjustLowValues();
    }
  };
  private BooleanProperty lowValueChanging;
  
  public final void setLowValue(double d)
  {
    lowValueProperty().set(d);
  }
  
  public final double getLowValue()
  {
    return this.lowValue != null ? this.lowValue.get() : 0.0D;
  }
  
  public final BooleanProperty lowValueChangingProperty()
  {
    if (this.lowValueChanging == null) {
      this.lowValueChanging = new SimpleBooleanProperty(this, "lowValueChanging", false);
    }
    return this.lowValueChanging;
  }
  
  public final void setLowValueChanging(boolean value)
  {
    lowValueChangingProperty().set(value);
  }
  
  public final boolean isLowValueChanging()
  {
    return this.lowValueChanging == null ? false : this.lowValueChanging.get();
  }
  
  public final DoubleProperty highValueProperty()
  {
    return this.highValue;
  }
  
  private DoubleProperty highValue = new SimpleDoubleProperty(this, "highValue", 100.0D)
  {
    protected void invalidated()
    {
      RangeSlider.this.adjustHighValues();
    }
    
    public Object getBean()
    {
      return RangeSlider.this;
    }
    
    public String getName()
    {
      return "highValue";
    }
  };
  private BooleanProperty highValueChanging;
  
  public final void setHighValue(double d)
  {
    if (!highValueProperty().isBound()) {
      highValueProperty().set(d);
    }
  }
  
  public final double getHighValue()
  {
    return this.highValue != null ? this.highValue.get() : 100.0D;
  }
  
  public final BooleanProperty highValueChangingProperty()
  {
    if (this.highValueChanging == null) {
      this.highValueChanging = new SimpleBooleanProperty(this, "highValueChanging", false);
    }
    return this.highValueChanging;
  }
  
  public final void setHighValueChanging(boolean value)
  {
    highValueChangingProperty().set(value);
  }
  
  public final boolean isHighValueChanging()
  {
    return this.highValueChanging == null ? false : this.highValueChanging.get();
  }
  
  private final ObjectProperty<StringConverter<Number>> tickLabelFormatter = new SimpleObjectProperty();
  private DoubleProperty max;
  private DoubleProperty min;
  private BooleanProperty snapToTicks;
  private DoubleProperty majorTickUnit;
  private IntegerProperty minorTickCount;
  private DoubleProperty blockIncrement;
  private ObjectProperty<Orientation> orientation;
  private BooleanProperty showTickLabels;
  private BooleanProperty showTickMarks;
  private static final String DEFAULT_STYLE_CLASS = "range-slider";
  
  public final StringConverter<Number> getLabelFormatter()
  {
    return (StringConverter)this.tickLabelFormatter.get();
  }
  
  public final void setLabelFormatter(StringConverter<Number> value)
  {
    this.tickLabelFormatter.set(value);
  }
  
  public final ObjectProperty<StringConverter<Number>> labelFormatterProperty()
  {
    return this.tickLabelFormatter;
  }
  
  public void incrementLowValue()
  {
    adjustLowValue(getLowValue() + getBlockIncrement());
  }
  
  public void decrementLowValue()
  {
    adjustLowValue(getLowValue() - getBlockIncrement());
  }
  
  public void incrementHighValue()
  {
    adjustHighValue(getHighValue() + getBlockIncrement());
  }
  
  public void decrementHighValue()
  {
    adjustHighValue(getHighValue() - getBlockIncrement());
  }
  
  public void adjustLowValue(double newValue)
  {
    double d1 = getMin();
    double d2 = getMax();
    if (d2 > d1)
    {
      newValue = newValue >= d1 ? newValue : d1;
      newValue = newValue <= d2 ? newValue : d2;
      setLowValue(snapValueToTicks(newValue));
    }
  }
  
  public void adjustHighValue(double newValue)
  {
    double d1 = getMin();
    double d2 = getMax();
    if (d2 > d1)
    {
      newValue = newValue >= d1 ? newValue : d1;
      newValue = newValue <= d2 ? newValue : d2;
      setHighValue(snapValueToTicks(newValue));
    }
  }
  
  public final void setMax(double value)
  {
    maxProperty().set(value);
  }
  
  public final double getMax()
  {
    return this.max == null ? 100.0D : this.max.get();
  }
  
  public final DoubleProperty maxProperty()
  {
    if (this.max == null) {
      this.max = new DoublePropertyBase(100.0D)
      {
        protected void invalidated()
        {
          if (get() < RangeSlider.this.getMin()) {
            RangeSlider.this.setMin(get());
          }
          RangeSlider.this.adjustValues();
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "max";
        }
      };
    }
    return this.max;
  }
  
  public final void setMin(double value)
  {
    minProperty().set(value);
  }
  
  public final double getMin()
  {
    return this.min == null ? 0.0D : this.min.get();
  }
  
  public final DoubleProperty minProperty()
  {
    if (this.min == null) {
      this.min = new DoublePropertyBase(0.0D)
      {
        protected void invalidated()
        {
          if (get() > RangeSlider.this.getMax()) {
            RangeSlider.this.setMax(get());
          }
          RangeSlider.this.adjustValues();
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "min";
        }
      };
    }
    return this.min;
  }
  
  public final void setSnapToTicks(boolean value)
  {
    snapToTicksProperty().set(value);
  }
  
  public final boolean isSnapToTicks()
  {
    return this.snapToTicks == null ? false : this.snapToTicks.get();
  }
  
  public final BooleanProperty snapToTicksProperty()
  {
    if (this.snapToTicks == null) {
      this.snapToTicks = new StyleableBooleanProperty(false)
      {
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData()
        {
          return RangeSlider.StyleableProperties.SNAP_TO_TICKS;
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "snapToTicks";
        }
      };
    }
    return this.snapToTicks;
  }
  
  public final void setMajorTickUnit(double value)
  {
    if (value <= 0.0D) {
      throw new IllegalArgumentException("MajorTickUnit cannot be less than or equal to 0.");
    }
    majorTickUnitProperty().set(value);
  }
  
  public final double getMajorTickUnit()
  {
    return this.majorTickUnit == null ? 25.0D : this.majorTickUnit.get();
  }
  
  public final DoubleProperty majorTickUnitProperty()
  {
    if (this.majorTickUnit == null) {
      this.majorTickUnit = new StyleableDoubleProperty(25.0D)
      {
        public void invalidated()
        {
          if (get() <= 0.0D) {
            throw new IllegalArgumentException("MajorTickUnit cannot be less than or equal to 0.");
          }
        }
        
        public CssMetaData<? extends Styleable, Number> getCssMetaData()
        {
          return RangeSlider.StyleableProperties.MAJOR_TICK_UNIT;
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "majorTickUnit";
        }
      };
    }
    return this.majorTickUnit;
  }
  
  public final void setMinorTickCount(int value)
  {
    minorTickCountProperty().set(value);
  }
  
  public final int getMinorTickCount()
  {
    return this.minorTickCount == null ? 3 : this.minorTickCount.get();
  }
  
  public final IntegerProperty minorTickCountProperty()
  {
    if (this.minorTickCount == null) {
      this.minorTickCount = new StyleableIntegerProperty(3)
      {
        public CssMetaData<? extends Styleable, Number> getCssMetaData()
        {
          return RangeSlider.StyleableProperties.MINOR_TICK_COUNT;
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "minorTickCount";
        }
      };
    }
    return this.minorTickCount;
  }
  
  public final void setBlockIncrement(double value)
  {
    blockIncrementProperty().set(value);
  }
  
  public final double getBlockIncrement()
  {
    return this.blockIncrement == null ? 10.0D : this.blockIncrement.get();
  }
  
  public final DoubleProperty blockIncrementProperty()
  {
    if (this.blockIncrement == null) {
      this.blockIncrement = new StyleableDoubleProperty(10.0D)
      {
        public CssMetaData<? extends Styleable, Number> getCssMetaData()
        {
          return RangeSlider.StyleableProperties.BLOCK_INCREMENT;
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "blockIncrement";
        }
      };
    }
    return this.blockIncrement;
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
      this.orientation = new StyleableObjectProperty(Orientation.HORIZONTAL)
      {
        protected void invalidated()
        {
          boolean vertical = get() == Orientation.VERTICAL;
          RangeSlider.this.pseudoClassStateChanged(RangeSlider.VERTICAL_PSEUDOCLASS_STATE, vertical);
          RangeSlider.this.pseudoClassStateChanged(RangeSlider.HORIZONTAL_PSEUDOCLASS_STATE, !vertical);
        }
        
        public CssMetaData<? extends Styleable, Orientation> getCssMetaData()
        {
          return RangeSlider.StyleableProperties.ORIENTATION;
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "orientation";
        }
      };
    }
    return this.orientation;
  }
  
  public final void setShowTickLabels(boolean value)
  {
    showTickLabelsProperty().set(value);
  }
  
  public final boolean isShowTickLabels()
  {
    return this.showTickLabels == null ? false : this.showTickLabels.get();
  }
  
  public final BooleanProperty showTickLabelsProperty()
  {
    if (this.showTickLabels == null) {
      this.showTickLabels = new StyleableBooleanProperty(false)
      {
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData()
        {
          return RangeSlider.StyleableProperties.SHOW_TICK_LABELS;
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "showTickLabels";
        }
      };
    }
    return this.showTickLabels;
  }
  
  public final void setShowTickMarks(boolean value)
  {
    showTickMarksProperty().set(value);
  }
  
  public final boolean isShowTickMarks()
  {
    return this.showTickMarks == null ? false : this.showTickMarks.get();
  }
  
  public final BooleanProperty showTickMarksProperty()
  {
    if (this.showTickMarks == null) {
      this.showTickMarks = new StyleableBooleanProperty(false)
      {
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData()
        {
          return RangeSlider.StyleableProperties.SHOW_TICK_MARKS;
        }
        
        public Object getBean()
        {
          return RangeSlider.this;
        }
        
        public String getName()
        {
          return "showTickMarks";
        }
      };
    }
    return this.showTickMarks;
  }
  
  private void adjustValues()
  {
    adjustLowValues();
    adjustHighValues();
  }
  
  private void adjustLowValues()
  {
    if ((getLowValue() < getMin()) || (getLowValue() > getMax()))
    {
      double value = Utils.clamp(getMin(), getLowValue(), getMax());
      setLowValue(value);
    }
    else if ((getLowValue() >= getHighValue()) && (getHighValue() >= getMin()) && (getHighValue() <= getMax()))
    {
      double value = Utils.clamp(getMin(), getLowValue(), getHighValue());
      setLowValue(value);
    }
  }
  
  private double snapValueToTicks(double d)
  {
    double d1 = d;
    if (isSnapToTicks())
    {
      double d2 = 0.0D;
      if (getMinorTickCount() != 0) {
        d2 = getMajorTickUnit() / (Math.max(getMinorTickCount(), 0) + 1);
      } else {
        d2 = getMajorTickUnit();
      }
      int i = (int)((d1 - getMin()) / d2);
      double d3 = i * d2 + getMin();
      double d4 = (i + 1) * d2 + getMin();
      d1 = Utils.nearest(d3, d1, d4);
    }
    return Utils.clamp(getMin(), d1, getMax());
  }
  
  private void adjustHighValues()
  {
    if ((getHighValue() < getMin()) || (getHighValue() > getMax())) {
      setHighValue(Utils.clamp(getMin(), getHighValue(), getMax()));
    } else if ((getHighValue() < getLowValue()) && (getLowValue() >= getMin()) && (getLowValue() <= getMax())) {
      setHighValue(Utils.clamp(getLowValue(), getHighValue(), getMax()));
    }
  }
  
  private static class StyleableProperties
  {
    private static final CssMetaData<RangeSlider, Number> BLOCK_INCREMENT = new CssMetaData("-fx-block-increment", 
    
      SizeConverter.getInstance(), Double.valueOf(10.0D))
    {
      public boolean isSettable(RangeSlider n)
      {
        return (n.blockIncrement == null) || (!n.blockIncrement.isBound());
      }
      
      public StyleableProperty<Number> getStyleableProperty(RangeSlider n)
      {
        return (StyleableProperty)n.blockIncrementProperty();
      }
    };
    private static final CssMetaData<RangeSlider, Boolean> SHOW_TICK_LABELS = new CssMetaData("-fx-show-tick-labels", 
    
      BooleanConverter.getInstance(), Boolean.FALSE)
    {
      public boolean isSettable(RangeSlider n)
      {
        return (n.showTickLabels == null) || (!n.showTickLabels.isBound());
      }
      
      public StyleableProperty<Boolean> getStyleableProperty(RangeSlider n)
      {
        return (StyleableProperty)n.showTickLabelsProperty();
      }
    };
    private static final CssMetaData<RangeSlider, Boolean> SHOW_TICK_MARKS = new CssMetaData("-fx-show-tick-marks", 
    
      BooleanConverter.getInstance(), Boolean.FALSE)
    {
      public boolean isSettable(RangeSlider n)
      {
        return (n.showTickMarks == null) || (!n.showTickMarks.isBound());
      }
      
      public StyleableProperty<Boolean> getStyleableProperty(RangeSlider n)
      {
        return (StyleableProperty)n.showTickMarksProperty();
      }
    };
    private static final CssMetaData<RangeSlider, Boolean> SNAP_TO_TICKS = new CssMetaData("-fx-snap-to-ticks", 
    
      BooleanConverter.getInstance(), Boolean.FALSE)
    {
      public boolean isSettable(RangeSlider n)
      {
        return (n.snapToTicks == null) || (!n.snapToTicks.isBound());
      }
      
      public StyleableProperty<Boolean> getStyleableProperty(RangeSlider n)
      {
        return (StyleableProperty)n.snapToTicksProperty();
      }
    };
    private static final CssMetaData<RangeSlider, Number> MAJOR_TICK_UNIT = new CssMetaData("-fx-major-tick-unit", 
    
      SizeConverter.getInstance(), Double.valueOf(25.0D))
    {
      public boolean isSettable(RangeSlider n)
      {
        return (n.majorTickUnit == null) || (!n.majorTickUnit.isBound());
      }
      
      public StyleableProperty<Number> getStyleableProperty(RangeSlider n)
      {
        return (StyleableProperty)n.majorTickUnitProperty();
      }
    };
    private static final CssMetaData<RangeSlider, Number> MINOR_TICK_COUNT = new CssMetaData("-fx-minor-tick-count", 
    
      SizeConverter.getInstance(), Double.valueOf(3.0D))
    {
      public void set(RangeSlider node, Number value, StyleOrigin origin)
      {
        super.set(node, Integer.valueOf(value.intValue()), origin);
      }
      
      public boolean isSettable(RangeSlider n)
      {
        return (n.minorTickCount == null) || (!n.minorTickCount.isBound());
      }
      
      public StyleableProperty<Number> getStyleableProperty(RangeSlider n)
      {
        return (StyleableProperty)n.minorTickCountProperty();
      }
    };
    private static final CssMetaData<RangeSlider, Orientation> ORIENTATION = new CssMetaData("-fx-orientation", new EnumConverter(Orientation.class), Orientation.HORIZONTAL)
    {
      public Orientation getInitialValue(RangeSlider node)
      {
        return node.getOrientation();
      }
      
      public boolean isSettable(RangeSlider n)
      {
        return (n.orientation == null) || (!n.orientation.isBound());
      }
      
      public StyleableProperty<Orientation> getStyleableProperty(RangeSlider n)
      {
        return (StyleableProperty)n.orientationProperty();
      }
    };
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
    
    static
    {
      List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList(Control.getClassCssMetaData());
      styleables.add(BLOCK_INCREMENT);
      styleables.add(SHOW_TICK_LABELS);
      styleables.add(SHOW_TICK_MARKS);
      styleables.add(SNAP_TO_TICKS);
      styleables.add(MAJOR_TICK_UNIT);
      styleables.add(MINOR_TICK_COUNT);
      styleables.add(ORIENTATION);
      
      STYLEABLES = Collections.unmodifiableList(styleables);
    }
  }
  
  public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
  {
    return StyleableProperties.STYLEABLES;
  }
  
  private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");
  private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\RangeSlider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */