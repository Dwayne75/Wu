package impl.org.controlsfx.skin;

import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

public abstract class NotificationBar
  extends Region
{
  private static final double MIN_HEIGHT = 40.0D;
  final Label label;
  Label title;
  ButtonBar actionsBar;
  Button closeBtn;
  private final GridPane pane;
  public DoubleProperty transition = new SimpleDoubleProperty()
  {
    protected void invalidated()
    {
      NotificationBar.this.requestContainerLayout();
    }
  };
  
  public void requestContainerLayout()
  {
    layoutChildren();
  }
  
  public String getTitle()
  {
    return "";
  }
  
  public boolean isCloseButtonVisible()
  {
    return true;
  }
  
  public abstract String getText();
  
  public abstract Node getGraphic();
  
  public abstract ObservableList<Action> getActions();
  
  public abstract void hide();
  
  public abstract boolean isShowing();
  
  public abstract boolean isShowFromTop();
  
  public abstract double getContainerHeight();
  
  public abstract void relocateInParent(double paramDouble1, double paramDouble2);
  
  public NotificationBar()
  {
    getStyleClass().add("notification-bar");
    
    setVisible(isShowing());
    
    this.pane = new GridPane();
    this.pane.getStyleClass().add("pane");
    this.pane.setAlignment(Pos.BASELINE_LEFT);
    getChildren().setAll(new Node[] { this.pane });
    
    String titleStr = getTitle();
    if ((titleStr != null) && (!titleStr.isEmpty()))
    {
      this.title = new Label();
      this.title.getStyleClass().add("title");
      this.title.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
      GridPane.setHgrow(this.title, Priority.ALWAYS);
      
      this.title.setText(titleStr);
      this.title.opacityProperty().bind(this.transition);
    }
    this.label = new Label();
    this.label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    GridPane.setVgrow(this.label, Priority.ALWAYS);
    GridPane.setHgrow(this.label, Priority.ALWAYS);
    
    this.label.setText(getText());
    this.label.setGraphic(getGraphic());
    this.label.opacityProperty().bind(this.transition);
    
    getActions().addListener(new InvalidationListener()
    {
      public void invalidated(Observable arg0)
      {
        NotificationBar.this.updatePane();
      }
    });
    this.closeBtn = new Button();
    this.closeBtn.setOnAction(new EventHandler()
    {
      public void handle(ActionEvent arg0)
      {
        NotificationBar.this.hide();
      }
    });
    this.closeBtn.getStyleClass().setAll(new String[] { "close-button" });
    StackPane graphic = new StackPane();
    graphic.getStyleClass().setAll(new String[] { "graphic" });
    this.closeBtn.setGraphic(graphic);
    this.closeBtn.setMinSize(17.0D, 17.0D);
    this.closeBtn.setPrefSize(17.0D, 17.0D);
    this.closeBtn.setFocusTraversable(false);
    this.closeBtn.opacityProperty().bind(this.transition);
    GridPane.setMargin(this.closeBtn, new Insets(0.0D, 0.0D, 0.0D, 8.0D));
    
    double minHeight = minHeight(-1.0D);
    GridPane.setValignment(this.closeBtn, minHeight == 40.0D ? VPos.CENTER : VPos.TOP);
    
    updatePane();
  }
  
  void updatePane()
  {
    this.actionsBar = ActionUtils.createButtonBar(getActions());
    this.actionsBar.opacityProperty().bind(this.transition);
    GridPane.setHgrow(this.actionsBar, Priority.SOMETIMES);
    this.pane.getChildren().clear();
    
    int row = 0;
    if (this.title != null) {
      this.pane.add(this.title, 0, row++);
    }
    this.pane.add(this.label, 0, row);
    this.pane.add(this.actionsBar, 1, row);
    if (isCloseButtonVisible()) {
      this.pane.add(this.closeBtn, 2, 0, 1, row + 1);
    }
  }
  
  protected void layoutChildren()
  {
    double w = getWidth();
    double h = computePrefHeight(-1.0D);
    
    double notificationBarHeight = prefHeight(w);
    double notificationMinHeight = minHeight(w);
    if (isShowFromTop())
    {
      this.pane.resize(w, h);
      relocateInParent(0.0D, (this.transition.get() - 1.0D) * notificationMinHeight);
    }
    else
    {
      this.pane.resize(w, notificationBarHeight);
      relocateInParent(0.0D, getContainerHeight() - notificationBarHeight);
    }
  }
  
  protected double computeMinHeight(double width)
  {
    return Math.max(super.computePrefHeight(width), 40.0D);
  }
  
  protected double computePrefHeight(double width)
  {
    return Math.max(this.pane.prefHeight(width), minHeight(width)) * this.transition.get();
  }
  
  public void doShow()
  {
    this.transitionStartValue = 0.0D;
    doAnimationTransition();
  }
  
  public void doHide()
  {
    this.transitionStartValue = 1.0D;
    doAnimationTransition();
  }
  
  private final Duration TRANSITION_DURATION = new Duration(350.0D);
  private Timeline timeline;
  private double transitionStartValue;
  
  private void doAnimationTransition()
  {
    Duration duration;
    if ((this.timeline != null) && (this.timeline.getStatus() != Animation.Status.STOPPED))
    {
      Duration duration = this.timeline.getCurrentTime();
      
      duration = duration == Duration.ZERO ? this.TRANSITION_DURATION : duration;
      this.transitionStartValue = this.transition.get();
      
      this.timeline.stop();
    }
    else
    {
      duration = this.TRANSITION_DURATION;
    }
    this.timeline = new Timeline();
    this.timeline.setCycleCount(1);
    KeyFrame k2;
    KeyFrame k1;
    KeyFrame k2;
    if (isShowing())
    {
      KeyFrame k1 = new KeyFrame(Duration.ZERO, new EventHandler()new KeyValue
      {
        public void handle(ActionEvent event)
        {
          NotificationBar.this.setCache(true);
          NotificationBar.this.setVisible(true);
          
          NotificationBar.this.pane.fireEvent(new Event(NotificationPane.ON_SHOWING));
        }
      }
      
        , new KeyValue[] { new KeyValue(this.transition, Double.valueOf(this.transitionStartValue)) });
      
      k2 = new KeyFrame(duration, new EventHandler()new KeyValue
      {
        public void handle(ActionEvent event)
        {
          NotificationBar.this.pane.setCache(false);
          
          NotificationBar.this.pane.fireEvent(new Event(NotificationPane.ON_SHOWN));
        }
      }, new KeyValue[] { new KeyValue(this.transition, 
      
        Integer.valueOf(1), Interpolator.EASE_OUT) });
    }
    else
    {
      k1 = new KeyFrame(Duration.ZERO, new EventHandler()new KeyValue
      {
        public void handle(ActionEvent event)
        {
          NotificationBar.this.pane.setCache(true);
          
          NotificationBar.this.pane.fireEvent(new Event(NotificationPane.ON_HIDING));
        }
      }, new KeyValue[] { new KeyValue(this.transition, 
      
        Double.valueOf(this.transitionStartValue)) });
      
      k2 = new KeyFrame(duration, new EventHandler()new KeyValue
      {
        public void handle(ActionEvent event)
        {
          NotificationBar.this.setCache(false);
          NotificationBar.this.setVisible(false);
          
          NotificationBar.this.pane.fireEvent(new Event(NotificationPane.ON_HIDDEN));
        }
      }, new KeyValue[] { new KeyValue(this.transition, 
      
        Integer.valueOf(0), Interpolator.EASE_IN) });
    }
    this.timeline.getKeyFrames().setAll(new KeyFrame[] { k1, k2 });
    this.timeline.play();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\NotificationBar.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */