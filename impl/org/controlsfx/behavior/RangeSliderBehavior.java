package impl.org.controlsfx.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.behavior.OrientedKeyBinding;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.tools.Utils;

public class RangeSliderBehavior
  extends BehaviorBase<RangeSlider>
{
  private static final List<KeyBinding> RANGESLIDER_BINDINGS = new ArrayList();
  private Callback<Void, FocusedChild> selectedValue;
  
  static
  {
    RANGESLIDER_BINDINGS.add(new KeyBinding(KeyCode.F4, "TraverseDebug").alt().ctrl().shift());
    
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.LEFT, "DecrementValue"));
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_LEFT, "DecrementValue"));
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.UP, "IncrementValue").vertical());
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_UP, "IncrementValue").vertical());
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.RIGHT, "IncrementValue"));
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_RIGHT, "IncrementValue"));
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.DOWN, "DecrementValue").vertical());
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_DOWN, "DecrementValue").vertical());
    
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.LEFT, "TraverseLeft").vertical());
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_LEFT, "TraverseLeft").vertical());
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.UP, "TraverseUp"));
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_UP, "TraverseUp"));
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.RIGHT, "TraverseRight").vertical());
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_RIGHT, "TraverseRight").vertical());
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.DOWN, "TraverseDown"));
    RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KeyCode.KP_DOWN, "TraverseDown"));
    
    RANGESLIDER_BINDINGS.add(new KeyBinding(KeyCode.HOME, KeyEvent.KEY_RELEASED, "Home"));
    RANGESLIDER_BINDINGS.add(new KeyBinding(KeyCode.END, KeyEvent.KEY_RELEASED, "End"));
  }
  
  public RangeSliderBehavior(RangeSlider slider)
  {
    super(slider, RANGESLIDER_BINDINGS);
  }
  
  protected void callAction(String s)
  {
    if (("Home".equals(s)) || ("Home2".equals(s))) {
      home();
    } else if (("End".equals(s)) || ("End2".equals(s))) {
      end();
    } else if (("IncrementValue".equals(s)) || ("IncrementValue2".equals(s))) {
      incrementValue();
    } else if (("DecrementValue".equals(s)) || ("DecrementValue2".equals(s))) {
      decrementValue();
    } else {
      super.callAction(s);
    }
  }
  
  public void setSelectedValue(Callback<Void, FocusedChild> c)
  {
    this.selectedValue = c;
  }
  
  public void trackPress(MouseEvent e, double position)
  {
    RangeSlider rangeSlider = (RangeSlider)getControl();
    if (!rangeSlider.isFocused()) {
      rangeSlider.requestFocus();
    }
    if (this.selectedValue != null)
    {
      double newPosition;
      double newPosition;
      if (rangeSlider.getOrientation().equals(Orientation.HORIZONTAL)) {
        newPosition = position * (rangeSlider.getMax() - rangeSlider.getMin()) + rangeSlider.getMin();
      } else {
        newPosition = (1.0D - position) * (rangeSlider.getMax() - rangeSlider.getMin()) + rangeSlider.getMin();
      }
      if (newPosition < rangeSlider.getLowValue()) {
        rangeSlider.adjustLowValue(newPosition);
      } else {
        rangeSlider.adjustHighValue(newPosition);
      }
    }
  }
  
  public void lowThumbPressed(MouseEvent e, double position)
  {
    RangeSlider rangeSlider = (RangeSlider)getControl();
    if (!rangeSlider.isFocused()) {
      rangeSlider.requestFocus();
    }
    rangeSlider.setLowValueChanging(true);
  }
  
  public void lowThumbDragged(MouseEvent e, double position)
  {
    RangeSlider rangeSlider = (RangeSlider)getControl();
    double newValue = Utils.clamp(rangeSlider.getMin(), position * (rangeSlider
      .getMax() - rangeSlider.getMin()) + rangeSlider.getMin(), rangeSlider
      .getMax());
    rangeSlider.setLowValue(newValue);
  }
  
  public void lowThumbReleased(MouseEvent e)
  {
    RangeSlider rangeSlider = (RangeSlider)getControl();
    rangeSlider.setLowValueChanging(false);
    if (rangeSlider.isSnapToTicks()) {
      rangeSlider.setLowValue(snapValueToTicks(rangeSlider.getLowValue()));
    }
  }
  
  void home()
  {
    RangeSlider slider = (RangeSlider)getControl();
    slider.adjustHighValue(slider.getMin());
  }
  
  void decrementValue()
  {
    RangeSlider slider = (RangeSlider)getControl();
    if (this.selectedValue != null) {
      if (this.selectedValue.call(null) == FocusedChild.HIGH_THUMB)
      {
        if (slider.isSnapToTicks()) {
          slider.adjustHighValue(slider.getHighValue() - computeIncrement());
        } else {
          slider.decrementHighValue();
        }
      }
      else if (slider.isSnapToTicks()) {
        slider.adjustLowValue(slider.getLowValue() - computeIncrement());
      } else {
        slider.decrementLowValue();
      }
    }
  }
  
  void end()
  {
    RangeSlider slider = (RangeSlider)getControl();
    slider.adjustHighValue(slider.getMax());
  }
  
  void incrementValue()
  {
    RangeSlider slider = (RangeSlider)getControl();
    if (this.selectedValue != null) {
      if (this.selectedValue.call(null) == FocusedChild.HIGH_THUMB)
      {
        if (slider.isSnapToTicks()) {
          slider.adjustHighValue(slider.getHighValue() + computeIncrement());
        } else {
          slider.incrementHighValue();
        }
      }
      else if (slider.isSnapToTicks()) {
        slider.adjustLowValue(slider.getLowValue() + computeIncrement());
      } else {
        slider.incrementLowValue();
      }
    }
  }
  
  double computeIncrement()
  {
    RangeSlider rangeSlider = (RangeSlider)getControl();
    double d = 0.0D;
    if (rangeSlider.getMinorTickCount() != 0) {
      d = rangeSlider.getMajorTickUnit() / (Math.max(rangeSlider.getMinorTickCount(), 0) + 1);
    } else {
      d = rangeSlider.getMajorTickUnit();
    }
    if ((rangeSlider.getBlockIncrement() > 0.0D) && (rangeSlider.getBlockIncrement() < d)) {
      return d;
    }
    return rangeSlider.getBlockIncrement();
  }
  
  private double snapValueToTicks(double d)
  {
    RangeSlider rangeSlider = (RangeSlider)getControl();
    double d1 = d;
    double d2 = 0.0D;
    if (rangeSlider.getMinorTickCount() != 0) {
      d2 = rangeSlider.getMajorTickUnit() / (Math.max(rangeSlider.getMinorTickCount(), 0) + 1);
    } else {
      d2 = rangeSlider.getMajorTickUnit();
    }
    int i = (int)((d1 - rangeSlider.getMin()) / d2);
    double d3 = i * d2 + rangeSlider.getMin();
    double d4 = (i + 1) * d2 + rangeSlider.getMin();
    d1 = Utils.nearest(d3, d1, d4);
    return Utils.clamp(rangeSlider.getMin(), d1, rangeSlider.getMax());
  }
  
  public void highThumbReleased(MouseEvent e)
  {
    RangeSlider slider = (RangeSlider)getControl();
    slider.setHighValueChanging(false);
    if (slider.isSnapToTicks()) {
      slider.setHighValue(snapValueToTicks(slider.getHighValue()));
    }
  }
  
  public void highThumbPressed(MouseEvent e, double position)
  {
    RangeSlider slider = (RangeSlider)getControl();
    if (!slider.isFocused()) {
      slider.requestFocus();
    }
    slider.setHighValueChanging(true);
  }
  
  public void highThumbDragged(MouseEvent e, double position)
  {
    RangeSlider slider = (RangeSlider)getControl();
    slider.setHighValue(Utils.clamp(slider.getMin(), position * (slider.getMax() - slider.getMin()) + slider.getMin(), slider.getMax()));
  }
  
  public void moveRange(double position)
  {
    RangeSlider slider = (RangeSlider)getControl();
    double min = slider.getMin();
    double max = slider.getMax();
    double lowValue = slider.getLowValue();
    double newLowValue = Utils.clamp(min, lowValue + position * (max - min) / (slider
      .getOrientation() == Orientation.HORIZONTAL ? slider.getWidth() : slider.getHeight()), max);
    double highValue = slider.getHighValue();
    double newHighValue = Utils.clamp(min, highValue + position * (max - min) / (slider
      .getOrientation() == Orientation.HORIZONTAL ? slider.getWidth() : slider.getHeight()), max);
    if ((newLowValue <= min) || (newHighValue >= max)) {
      return;
    }
    slider.setLowValueChanging(true);
    slider.setHighValueChanging(true);
    slider.setLowValue(newLowValue);
    slider.setHighValue(newHighValue);
  }
  
  public void confirmRange()
  {
    RangeSlider slider = (RangeSlider)getControl();
    
    slider.setLowValueChanging(false);
    if (slider.isSnapToTicks()) {
      slider.setLowValue(snapValueToTicks(slider.getLowValue()));
    }
    slider.setHighValueChanging(false);
    if (slider.isSnapToTicks()) {
      slider.setHighValue(snapValueToTicks(slider.getHighValue()));
    }
  }
  
  public void trackRelease(MouseEvent e, double position) {}
  
  public static class RangeSliderKeyBinding
    extends OrientedKeyBinding
  {
    public RangeSliderKeyBinding(KeyCode code, String action)
    {
      super(action);
    }
    
    public RangeSliderKeyBinding(KeyCode code, EventType<KeyEvent> type, String action)
    {
      super(type, action);
    }
    
    public boolean getVertical(Control control)
    {
      return ((RangeSlider)control).getOrientation() == Orientation.VERTICAL;
    }
  }
  
  public static enum FocusedChild
  {
    LOW_THUMB,  HIGH_THUMB,  RANGE_BAR,  NONE;
    
    private FocusedChild() {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\behavior\RangeSliderBehavior.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */