package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import impl.org.controlsfx.behavior.RangeSliderBehavior;
import impl.org.controlsfx.behavior.RangeSliderBehavior.FocusedChild;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.controlsfx.control.RangeSlider;

public class RangeSliderSkin
  extends BehaviorSkinBase<RangeSlider, RangeSliderBehavior>
{
  private NumberAxis tickLine = null;
  private double trackToTickGap = 2.0D;
  private boolean showTickMarks;
  private double thumbWidth;
  private double thumbHeight;
  private Orientation orientation;
  private StackPane track;
  private double trackStart;
  private double trackLength;
  private double lowThumbPos;
  private double rangeEnd;
  private double rangeStart;
  private ThumbPane lowThumb;
  private ThumbPane highThumb;
  private StackPane rangeBar;
  private double preDragPos;
  private Point2D preDragThumbPoint;
  private RangeSliderBehavior.FocusedChild currentFocus = RangeSliderBehavior.FocusedChild.LOW_THUMB;
  
  public RangeSliderSkin(final RangeSlider rangeSlider)
  {
    super(rangeSlider, new RangeSliderBehavior(rangeSlider));
    this.orientation = ((RangeSlider)getSkinnable()).getOrientation();
    initFirstThumb();
    initSecondThumb();
    initRangeBar();
    registerChangeListener(rangeSlider.lowValueProperty(), "LOW_VALUE");
    registerChangeListener(rangeSlider.highValueProperty(), "HIGH_VALUE");
    registerChangeListener(rangeSlider.minProperty(), "MIN");
    registerChangeListener(rangeSlider.maxProperty(), "MAX");
    registerChangeListener(rangeSlider.orientationProperty(), "ORIENTATION");
    registerChangeListener(rangeSlider.showTickMarksProperty(), "SHOW_TICK_MARKS");
    registerChangeListener(rangeSlider.showTickLabelsProperty(), "SHOW_TICK_LABELS");
    registerChangeListener(rangeSlider.majorTickUnitProperty(), "MAJOR_TICK_UNIT");
    registerChangeListener(rangeSlider.minorTickCountProperty(), "MINOR_TICK_COUNT");
    this.lowThumb.focusedProperty().addListener(new ChangeListener()
    {
      public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus)
      {
        if (hasFocus.booleanValue()) {
          RangeSliderSkin.this.currentFocus = RangeSliderBehavior.FocusedChild.LOW_THUMB;
        }
      }
    });
    this.highThumb.focusedProperty().addListener(new ChangeListener()
    {
      public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus)
      {
        if (hasFocus.booleanValue()) {
          RangeSliderSkin.this.currentFocus = RangeSliderBehavior.FocusedChild.HIGH_THUMB;
        }
      }
    });
    this.rangeBar.focusedProperty().addListener(new ChangeListener()
    {
      public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus)
      {
        if (hasFocus.booleanValue()) {
          RangeSliderSkin.this.currentFocus = RangeSliderBehavior.FocusedChild.RANGE_BAR;
        }
      }
    });
    rangeSlider.focusedProperty().addListener(new ChangeListener()
    {
      public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus)
      {
        if (hasFocus.booleanValue())
        {
          RangeSliderSkin.this.lowThumb.setFocus(true);
        }
        else
        {
          RangeSliderSkin.this.lowThumb.setFocus(false);
          RangeSliderSkin.this.highThumb.setFocus(false);
          RangeSliderSkin.this.currentFocus = RangeSliderBehavior.FocusedChild.NONE;
        }
      }
    });
    EventHandler<KeyEvent> keyEventHandler = new EventHandler()
    {
      private final Algorithm algorithm = new Algorithm()
      {
        public Node selectLast(TraversalContext context)
        {
          List<Node> focusTraversableNodes = context.getAllTargetNodes();
          
          return (Node)focusTraversableNodes.get(focusTraversableNodes
            .size() - 1);
        }
        
        public Node selectFirst(TraversalContext context)
        {
          return (Node)context.getAllTargetNodes().get(0);
        }
        
        public Node select(Node owner, Direction dir, TraversalContext context)
        {
          int direction;
          int direction;
          switch (RangeSliderSkin.19.$SwitchMap$com$sun$javafx$scene$traversal$Direction[dir.ordinal()])
          {
          case 1: 
          case 2: 
          case 3: 
          case 4: 
            direction = 1;
            
            break;
          case 5: 
          case 6: 
          case 7: 
            direction = -2;
            
            break;
          default: 
            throw new EnumConstantNotPresentException(dir.getClass(), dir.name());
          }
          int direction;
          List<Node> focusTraversableNodes = context.getAllTargetNodes();
          
          int focusReceiverIndex = focusTraversableNodes.indexOf(owner) + direction;
          if (focusReceiverIndex < 0) {
            return (Node)focusTraversableNodes.get(focusTraversableNodes
              .size() - 1);
          }
          if (focusReceiverIndex == focusTraversableNodes.size()) {
            return (Node)focusTraversableNodes.get(0);
          }
          return (Node)focusTraversableNodes.get(focusReceiverIndex);
        }
      };
      
      public void handle(KeyEvent event)
      {
        if (KeyCode.TAB.equals(event.getCode())) {
          if (RangeSliderSkin.this.lowThumb.isFocused())
          {
            if (event.isShiftDown())
            {
              RangeSliderSkin.this.lowThumb.setFocus(false);
              new ParentTraversalEngine(rangeSlider
                .getScene().getRoot(), this.algorithm)
                .select(RangeSliderSkin.this.lowThumb, Direction.PREVIOUS)
                .requestFocus();
            }
            else
            {
              RangeSliderSkin.this.lowThumb.setFocus(false);
              RangeSliderSkin.this.highThumb.setFocus(true);
            }
            event.consume();
          }
          else if (RangeSliderSkin.this.highThumb.isFocused())
          {
            if (event.isShiftDown())
            {
              RangeSliderSkin.this.highThumb.setFocus(false);
              RangeSliderSkin.this.lowThumb.setFocus(true);
            }
            else
            {
              RangeSliderSkin.this.highThumb.setFocus(false);
              new ParentTraversalEngine(rangeSlider
                .getScene().getRoot(), this.algorithm)
                .select(RangeSliderSkin.this.highThumb, Direction.NEXT)
                .requestFocus();
            }
            event.consume();
          }
        }
      }
    };
    ((RangeSlider)getSkinnable()).addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
    
    ((RangeSliderBehavior)getBehavior()).setSelectedValue(new Callback()
    {
      public RangeSliderBehavior.FocusedChild call(Void v)
      {
        return RangeSliderSkin.this.currentFocus;
      }
    });
  }
  
  private void initFirstThumb()
  {
    this.lowThumb = new ThumbPane(null);
    this.lowThumb.getStyleClass().setAll(new String[] { "low-thumb" });
    this.lowThumb.setFocusTraversable(true);
    this.track = new StackPane();
    this.track.getStyleClass().setAll(new String[] { "track" });
    
    getChildren().clear();
    getChildren().addAll(new Node[] { this.track, this.lowThumb });
    setShowTickMarks(((RangeSlider)getSkinnable()).isShowTickMarks(), ((RangeSlider)getSkinnable()).isShowTickLabels());
    this.track.setOnMousePressed(new EventHandler()
    {
      public void handle(MouseEvent me)
      {
        if ((!RangeSliderSkin.this.lowThumb.isPressed()) && (!RangeSliderSkin.this.highThumb.isPressed())) {
          if (RangeSliderSkin.this.isHorizontal()) {
            ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).trackPress(me, me.getX() / RangeSliderSkin.this.trackLength);
          } else {
            ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).trackPress(me, me.getY() / RangeSliderSkin.this.trackLength);
          }
        }
      }
    });
    this.track.setOnMouseReleased(new EventHandler()
    {
      public void handle(MouseEvent me)
      {
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).trackRelease(me, 0.0D);
      }
    });
    this.lowThumb.setOnMousePressed(new EventHandler()
    {
      public void handle(MouseEvent me)
      {
        RangeSliderSkin.this.highThumb.setFocus(false);
        RangeSliderSkin.this.lowThumb.setFocus(true);
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).lowThumbPressed(me, 0.0D);
        RangeSliderSkin.this.preDragThumbPoint = RangeSliderSkin.this.lowThumb.localToParent(me.getX(), me.getY());
        RangeSliderSkin.this.preDragPos = 
          ((((RangeSlider)RangeSliderSkin.this.getSkinnable()).getLowValue() - ((RangeSlider)RangeSliderSkin.this.getSkinnable()).getMin()) / RangeSliderSkin.this.getMaxMinusMinNoZero());
      }
    });
    this.lowThumb.setOnMouseReleased(new EventHandler()
    {
      public void handle(MouseEvent me)
      {
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).lowThumbReleased(me);
      }
    });
    this.lowThumb.setOnMouseDragged(new EventHandler()
    {
      public void handle(MouseEvent me)
      {
        Point2D cur = RangeSliderSkin.this.lowThumb.localToParent(me.getX(), me.getY());
        
        double dragPos = RangeSliderSkin.this.isHorizontal() ? cur.getX() - RangeSliderSkin.this.preDragThumbPoint.getX() : -(cur.getY() - RangeSliderSkin.this.preDragThumbPoint.getY());
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).lowThumbDragged(me, RangeSliderSkin.this.preDragPos + dragPos / RangeSliderSkin.this.trackLength);
      }
    });
  }
  
  private void initSecondThumb()
  {
    this.highThumb = new ThumbPane(null);
    this.highThumb.getStyleClass().setAll(new String[] { "high-thumb" });
    this.highThumb.setFocusTraversable(true);
    if (!getChildren().contains(this.highThumb)) {
      getChildren().add(this.highThumb);
    }
    this.highThumb.setOnMousePressed(new EventHandler()
    {
      public void handle(MouseEvent e)
      {
        RangeSliderSkin.this.lowThumb.setFocus(false);
        RangeSliderSkin.this.highThumb.setFocus(true);
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).highThumbPressed(e, 0.0D);
        RangeSliderSkin.this.preDragThumbPoint = RangeSliderSkin.this.highThumb.localToParent(e.getX(), e.getY());
        RangeSliderSkin.this.preDragPos = 
          ((((RangeSlider)RangeSliderSkin.this.getSkinnable()).getHighValue() - ((RangeSlider)RangeSliderSkin.this.getSkinnable()).getMin()) / RangeSliderSkin.this.getMaxMinusMinNoZero());
      }
    });
    this.highThumb.setOnMouseReleased(new EventHandler()
    {
      public void handle(MouseEvent e)
      {
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).highThumbReleased(e);
      }
    });
    this.highThumb.setOnMouseDragged(new EventHandler()
    {
      public void handle(MouseEvent e)
      {
        boolean orientation = ((RangeSlider)RangeSliderSkin.this.getSkinnable()).getOrientation() == Orientation.HORIZONTAL;
        double trackLength = orientation ? RangeSliderSkin.this.track.getWidth() : RangeSliderSkin.this.track.getHeight();
        
        Point2D point2d = RangeSliderSkin.this.highThumb.localToParent(e.getX(), e.getY());
        double d = ((RangeSlider)RangeSliderSkin.this.getSkinnable()).getOrientation() != Orientation.HORIZONTAL ? -(point2d.getY() - RangeSliderSkin.this.preDragThumbPoint.getY()) : point2d.getX() - RangeSliderSkin.this.preDragThumbPoint.getX();
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).highThumbDragged(e, RangeSliderSkin.this.preDragPos + d / trackLength);
      }
    });
  }
  
  private void initRangeBar()
  {
    this.rangeBar = new StackPane();
    this.rangeBar.cursorProperty().bind(new ObjectBinding()
    {
      protected Cursor computeValue()
      {
        return RangeSliderSkin.this.rangeBar.isHover() ? Cursor.HAND : Cursor.DEFAULT;
      }
    });
    this.rangeBar.getStyleClass().setAll(new String[] { "range-bar" });
    
    this.rangeBar.setOnMousePressed(new EventHandler()
    {
      public void handle(MouseEvent e)
      {
        RangeSliderSkin.this.rangeBar.requestFocus();
        RangeSliderSkin.this.preDragPos = (RangeSliderSkin.this.isHorizontal() ? e.getX() : -e.getY());
      }
    });
    this.rangeBar.setOnMouseDragged(new EventHandler()
    {
      public void handle(MouseEvent e)
      {
        double delta = (RangeSliderSkin.this.isHorizontal() ? e.getX() : -e.getY()) - RangeSliderSkin.this.preDragPos;
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).moveRange(delta);
      }
    });
    this.rangeBar.setOnMouseReleased(new EventHandler()
    {
      public void handle(MouseEvent e)
      {
        ((RangeSliderBehavior)RangeSliderSkin.this.getBehavior()).confirmRange();
      }
    });
    getChildren().add(this.rangeBar);
  }
  
  private void setShowTickMarks(boolean ticksVisible, boolean labelsVisible)
  {
    this.showTickMarks = ((ticksVisible) || (labelsVisible));
    RangeSlider rangeSlider = (RangeSlider)getSkinnable();
    if (this.showTickMarks)
    {
      if (this.tickLine == null)
      {
        this.tickLine = new NumberAxis();
        this.tickLine.tickLabelFormatterProperty().bind(((RangeSlider)getSkinnable()).labelFormatterProperty());
        this.tickLine.setAnimated(false);
        this.tickLine.setAutoRanging(false);
        this.tickLine.setSide(isHorizontal() ? Side.BOTTOM : Side.RIGHT);
        this.tickLine.setUpperBound(rangeSlider.getMax());
        this.tickLine.setLowerBound(rangeSlider.getMin());
        this.tickLine.setTickUnit(rangeSlider.getMajorTickUnit());
        this.tickLine.setTickMarkVisible(ticksVisible);
        this.tickLine.setTickLabelsVisible(labelsVisible);
        this.tickLine.setMinorTickVisible(ticksVisible);
        
        this.tickLine.setMinorTickCount(Math.max(rangeSlider.getMinorTickCount(), 0) + 1);
        getChildren().clear();
        getChildren().addAll(new Node[] { this.tickLine, this.track, this.lowThumb });
      }
      else
      {
        this.tickLine.setTickLabelsVisible(labelsVisible);
        this.tickLine.setTickMarkVisible(ticksVisible);
        this.tickLine.setMinorTickVisible(ticksVisible);
      }
    }
    else
    {
      getChildren().clear();
      getChildren().addAll(new Node[] { this.track, this.lowThumb });
    }
    ((RangeSlider)getSkinnable()).requestLayout();
  }
  
  protected void handleControlPropertyChanged(String p)
  {
    super.handleControlPropertyChanged(p);
    if ("ORIENTATION".equals(p))
    {
      this.orientation = ((RangeSlider)getSkinnable()).getOrientation();
      if ((this.showTickMarks) && (this.tickLine != null)) {
        this.tickLine.setSide(isHorizontal() ? Side.BOTTOM : Side.RIGHT);
      }
      ((RangeSlider)getSkinnable()).requestLayout();
    }
    else if ("MIN".equals(p))
    {
      if ((this.showTickMarks) && (this.tickLine != null)) {
        this.tickLine.setLowerBound(((RangeSlider)getSkinnable()).getMin());
      }
      ((RangeSlider)getSkinnable()).requestLayout();
    }
    else if ("MAX".equals(p))
    {
      if ((this.showTickMarks) && (this.tickLine != null)) {
        this.tickLine.setUpperBound(((RangeSlider)getSkinnable()).getMax());
      }
      ((RangeSlider)getSkinnable()).requestLayout();
    }
    else if (("SHOW_TICK_MARKS".equals(p)) || ("SHOW_TICK_LABELS".equals(p)))
    {
      setShowTickMarks(((RangeSlider)getSkinnable()).isShowTickMarks(), ((RangeSlider)getSkinnable()).isShowTickLabels());
      if (!getChildren().contains(this.highThumb)) {
        getChildren().add(this.highThumb);
      }
      if (!getChildren().contains(this.rangeBar)) {
        getChildren().add(this.rangeBar);
      }
    }
    else if ("MAJOR_TICK_UNIT".equals(p))
    {
      if (this.tickLine != null)
      {
        this.tickLine.setTickUnit(((RangeSlider)getSkinnable()).getMajorTickUnit());
        ((RangeSlider)getSkinnable()).requestLayout();
      }
    }
    else if ("MINOR_TICK_COUNT".equals(p))
    {
      if (this.tickLine != null)
      {
        this.tickLine.setMinorTickCount(Math.max(((RangeSlider)getSkinnable()).getMinorTickCount(), 0) + 1);
        ((RangeSlider)getSkinnable()).requestLayout();
      }
    }
    else if ("LOW_VALUE".equals(p))
    {
      positionLowThumb();
      this.rangeBar.resizeRelocate(this.rangeStart, this.rangeBar.getLayoutY(), this.rangeEnd - this.rangeStart, this.rangeBar
        .getHeight());
    }
    else if ("HIGH_VALUE".equals(p))
    {
      positionHighThumb();
      this.rangeBar.resize(this.rangeEnd - this.rangeStart, this.rangeBar.getHeight());
    }
    super.handleControlPropertyChanged(p);
  }
  
  private double getMaxMinusMinNoZero()
  {
    RangeSlider s = (RangeSlider)getSkinnable();
    return s.getMax() - s.getMin() == 0.0D ? 1.0D : s.getMax() - s.getMin();
  }
  
  private void positionLowThumb()
  {
    RangeSlider s = (RangeSlider)getSkinnable();
    boolean horizontal = isHorizontal();
    
    double lx = horizontal ? this.trackStart + (this.trackLength * ((s.getLowValue() - s.getMin()) / getMaxMinusMinNoZero()) - this.thumbWidth / 2.0D) : this.lowThumbPos;
    
    double ly = horizontal ? this.lowThumbPos : ((RangeSlider)getSkinnable()).getInsets().getTop() + this.trackLength - this.trackLength * ((s.getLowValue() - s.getMin()) / getMaxMinusMinNoZero());
    this.lowThumb.setLayoutX(lx);
    this.lowThumb.setLayoutY(ly);
    if (horizontal) {
      this.rangeStart = (lx + this.thumbWidth);
    } else {
      this.rangeEnd = ly;
    }
  }
  
  private void positionHighThumb()
  {
    RangeSlider slider = (RangeSlider)getSkinnable();
    boolean orientation = ((RangeSlider)getSkinnable()).getOrientation() == Orientation.HORIZONTAL;
    
    double thumbWidth = this.lowThumb.getWidth();
    double thumbHeight = this.lowThumb.getHeight();
    this.highThumb.resize(thumbWidth, thumbHeight);
    
    double pad = 0.0D;
    double trackStart = orientation ? this.track.getLayoutX() : this.track.getLayoutY();
    trackStart += pad;
    double trackLength = orientation ? this.track.getWidth() : this.track.getHeight();
    trackLength -= 2.0D * pad;
    
    double x = orientation ? trackStart + (trackLength * ((slider.getHighValue() - slider.getMin()) / getMaxMinusMinNoZero()) - thumbWidth / 2.0D) : this.lowThumb.getLayoutX();
    double y = orientation ? this.lowThumb.getLayoutY() : ((RangeSlider)getSkinnable()).getInsets().getTop() + trackLength - trackLength * ((slider.getHighValue() - slider.getMin()) / getMaxMinusMinNoZero());
    this.highThumb.setLayoutX(x);
    this.highThumb.setLayoutY(y);
    if (orientation) {
      this.rangeEnd = x;
    } else {
      this.rangeStart = (y + thumbWidth);
    }
  }
  
  protected void layoutChildren(double x, double y, double w, double h)
  {
    this.thumbWidth = this.lowThumb.prefWidth(-1.0D);
    this.thumbHeight = this.lowThumb.prefHeight(-1.0D);
    this.lowThumb.resize(this.thumbWidth, this.thumbHeight);
    
    double trackRadius = this.track.getBackground().getFills().size() > 0 ? ((BackgroundFill)this.track.getBackground().getFills().get(0)).getRadii().getTopLeftHorizontalRadius() : this.track.getBackground() == null ? 0.0D : 0.0D;
    if (isHorizontal())
    {
      double tickLineHeight = this.showTickMarks ? this.tickLine.prefHeight(-1.0D) : 0.0D;
      double trackHeight = this.track.prefHeight(-1.0D);
      double trackAreaHeight = Math.max(trackHeight, this.thumbHeight);
      double totalHeightNeeded = trackAreaHeight + (this.showTickMarks ? this.trackToTickGap + tickLineHeight : 0.0D);
      double startY = y + (h - totalHeightNeeded) / 2.0D;
      this.trackLength = (w - this.thumbWidth);
      this.trackStart = (x + this.thumbWidth / 2.0D);
      double trackTop = (int)(startY + (trackAreaHeight - trackHeight) / 2.0D);
      this.lowThumbPos = ((int)(startY + (trackAreaHeight - this.thumbHeight) / 2.0D));
      
      positionLowThumb();
      
      this.track.resizeRelocate(this.trackStart - trackRadius, trackTop, this.trackLength + trackRadius + trackRadius, trackHeight);
      positionHighThumb();
      
      this.rangeBar.resizeRelocate(this.rangeStart, trackTop, this.rangeEnd - this.rangeStart, trackHeight);
      if (this.showTickMarks)
      {
        this.tickLine.setLayoutX(this.trackStart);
        this.tickLine.setLayoutY(trackTop + trackHeight + this.trackToTickGap);
        this.tickLine.resize(this.trackLength, tickLineHeight);
        this.tickLine.requestAxisLayout();
      }
      else
      {
        if (this.tickLine != null)
        {
          this.tickLine.resize(0.0D, 0.0D);
          this.tickLine.requestAxisLayout();
        }
        this.tickLine = null;
      }
    }
    else
    {
      double tickLineWidth = this.showTickMarks ? this.tickLine.prefWidth(-1.0D) : 0.0D;
      double trackWidth = this.track.prefWidth(-1.0D);
      double trackAreaWidth = Math.max(trackWidth, this.thumbWidth);
      double totalWidthNeeded = trackAreaWidth + (this.showTickMarks ? this.trackToTickGap + tickLineWidth : 0.0D);
      double startX = x + (w - totalWidthNeeded) / 2.0D;
      this.trackLength = (h - this.thumbHeight);
      this.trackStart = (y + this.thumbHeight / 2.0D);
      double trackLeft = (int)(startX + (trackAreaWidth - trackWidth) / 2.0D);
      this.lowThumbPos = ((int)(startX + (trackAreaWidth - this.thumbWidth) / 2.0D));
      
      positionLowThumb();
      
      this.track.resizeRelocate(trackLeft, this.trackStart - trackRadius, trackWidth, this.trackLength + trackRadius + trackRadius);
      positionHighThumb();
      
      this.rangeBar.resizeRelocate(trackLeft, this.rangeStart, trackWidth, this.rangeEnd - this.rangeStart);
      if (this.showTickMarks)
      {
        this.tickLine.setLayoutX(trackLeft + trackWidth + this.trackToTickGap);
        this.tickLine.setLayoutY(this.trackStart);
        this.tickLine.resize(tickLineWidth, this.trackLength);
        this.tickLine.requestAxisLayout();
      }
      else
      {
        if (this.tickLine != null)
        {
          this.tickLine.resize(0.0D, 0.0D);
          this.tickLine.requestAxisLayout();
        }
        this.tickLine = null;
      }
    }
  }
  
  private double minTrackLength()
  {
    return 2.0D * this.lowThumb.prefWidth(-1.0D);
  }
  
  protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    if (isHorizontal()) {
      return leftInset + minTrackLength() + this.lowThumb.minWidth(-1.0D) + rightInset;
    }
    return leftInset + this.lowThumb.prefWidth(-1.0D) + rightInset;
  }
  
  protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    if (isHorizontal()) {
      return topInset + this.lowThumb.prefHeight(-1.0D) + bottomInset;
    }
    return topInset + minTrackLength() + this.lowThumb.prefHeight(-1.0D) + bottomInset;
  }
  
  protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    if (isHorizontal())
    {
      if (this.showTickMarks) {
        return Math.max(140.0D, this.tickLine.prefWidth(-1.0D));
      }
      return 140.0D;
    }
    return 
      leftInset + Math.max(this.lowThumb.prefWidth(-1.0D), this.track.prefWidth(-1.0D)) + (this.showTickMarks ? this.trackToTickGap + this.tickLine.prefWidth(-1.0D) : 0.0D) + rightInset;
  }
  
  protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    if (isHorizontal()) {
      return 
        ((RangeSlider)getSkinnable()).getInsets().getTop() + Math.max(this.lowThumb.prefHeight(-1.0D), this.track.prefHeight(-1.0D)) + (this.showTickMarks ? this.trackToTickGap + this.tickLine.prefHeight(-1.0D) : 0.0D) + bottomInset;
    }
    if (this.showTickMarks) {
      return Math.max(140.0D, this.tickLine.prefHeight(-1.0D));
    }
    return 140.0D;
  }
  
  protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    if (isHorizontal()) {
      return Double.MAX_VALUE;
    }
    return ((RangeSlider)getSkinnable()).prefWidth(-1.0D);
  }
  
  protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
  {
    if (isHorizontal()) {
      return ((RangeSlider)getSkinnable()).prefHeight(width);
    }
    return Double.MAX_VALUE;
  }
  
  private boolean isHorizontal()
  {
    return (this.orientation == null) || (this.orientation == Orientation.HORIZONTAL);
  }
  
  private static class ThumbPane
    extends StackPane
  {
    public void setFocus(boolean value)
    {
      setFocused(value);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\RangeSliderSkin.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */