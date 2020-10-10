package org.controlsfx.control;

import impl.org.controlsfx.skin.HiddenSidesPaneSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.util.Duration;

public class HiddenSidesPane
  extends ControlsFXControl
{
  public HiddenSidesPane(Node content, Node top, Node right, Node bottom, Node left)
  {
    setContent(content);
    setTop(top);
    setRight(right);
    setBottom(bottom);
    setLeft(left);
  }
  
  public HiddenSidesPane()
  {
    this(null, null, null, null, null);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new HiddenSidesPaneSkin(this);
  }
  
  private DoubleProperty triggerDistance = new SimpleDoubleProperty(this, "triggerDistance", 16.0D);
  
  public final DoubleProperty triggerDistanceProperty()
  {
    return this.triggerDistance;
  }
  
  public final double getTriggerDistance()
  {
    return this.triggerDistance.get();
  }
  
  public final void setTriggerDistance(double distance)
  {
    this.triggerDistance.set(distance);
  }
  
  private ObjectProperty<Node> content = new SimpleObjectProperty(this, "content");
  
  public final ObjectProperty<Node> contentProperty()
  {
    return this.content;
  }
  
  public final Node getContent()
  {
    return (Node)contentProperty().get();
  }
  
  public final void setContent(Node content)
  {
    contentProperty().set(content);
  }
  
  private ObjectProperty<Node> top = new SimpleObjectProperty(this, "top");
  
  public final ObjectProperty<Node> topProperty()
  {
    return this.top;
  }
  
  public final Node getTop()
  {
    return (Node)topProperty().get();
  }
  
  public final void setTop(Node top)
  {
    topProperty().set(top);
  }
  
  private ObjectProperty<Node> right = new SimpleObjectProperty(this, "right");
  
  public final ObjectProperty<Node> rightProperty()
  {
    return this.right;
  }
  
  public final Node getRight()
  {
    return (Node)rightProperty().get();
  }
  
  public final void setRight(Node right)
  {
    rightProperty().set(right);
  }
  
  private ObjectProperty<Node> bottom = new SimpleObjectProperty(this, "bottom");
  
  public final ObjectProperty<Node> bottomProperty()
  {
    return this.bottom;
  }
  
  public final Node getBottom()
  {
    return (Node)bottomProperty().get();
  }
  
  public final void setBottom(Node bottom)
  {
    bottomProperty().set(bottom);
  }
  
  private ObjectProperty<Node> left = new SimpleObjectProperty(this, "left");
  
  public final ObjectProperty<Node> leftProperty()
  {
    return this.left;
  }
  
  public final Node getLeft()
  {
    return (Node)leftProperty().get();
  }
  
  public final void setLeft(Node left)
  {
    leftProperty().set(left);
  }
  
  private ObjectProperty<Side> pinnedSide = new SimpleObjectProperty(this, "pinnedSide");
  
  public final ObjectProperty<Side> pinnedSideProperty()
  {
    return this.pinnedSide;
  }
  
  public final Side getPinnedSide()
  {
    return (Side)pinnedSideProperty().get();
  }
  
  public final void setPinnedSide(Side side)
  {
    pinnedSideProperty().set(side);
  }
  
  private final ObjectProperty<Duration> animationDelay = new SimpleObjectProperty(this, "animationDelay", 
    Duration.millis(300.0D));
  
  public final ObjectProperty<Duration> animationDelayProperty()
  {
    return this.animationDelay;
  }
  
  public final Duration getAnimationDelay()
  {
    return (Duration)this.animationDelay.get();
  }
  
  public final void setAnimationDelay(Duration duration)
  {
    this.animationDelay.set(duration);
  }
  
  private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty(this, "animationDuration", 
    Duration.millis(200.0D));
  
  public final ObjectProperty<Duration> animationDurationProperty()
  {
    return this.animationDuration;
  }
  
  public final Duration getAnimationDuration()
  {
    return (Duration)this.animationDuration.get();
  }
  
  public final void setAnimationDuration(Duration duration)
  {
    this.animationDuration.set(duration);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\HiddenSidesPane.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */