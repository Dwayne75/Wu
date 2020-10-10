package org.controlsfx.control;

import impl.org.controlsfx.skin.MasterDetailPaneSkin;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

public class MasterDetailPane
  extends ControlsFXControl
{
  public MasterDetailPane(Side side, Node masterNode, Node detailNode, boolean showDetail)
  {
    Objects.requireNonNull(side);
    Objects.requireNonNull(masterNode);
    Objects.requireNonNull(detailNode);
    
    getStyleClass().add("master-detail-pane");
    
    setDetailSide(side);
    setMasterNode(masterNode);
    setDetailNode(detailNode);
    setShowDetailNode(showDetail);
    switch (side)
    {
    case BOTTOM: 
    case RIGHT: 
      setDividerPosition(0.8D);
      break;
    case TOP: 
    case LEFT: 
      setDividerPosition(0.2D);
      break;
    }
  }
  
  public MasterDetailPane(Side pos, boolean showDetail)
  {
    this(pos, new Placeholder(true), new Placeholder(false), showDetail);
  }
  
  public MasterDetailPane(Side pos)
  {
    this(pos, new Placeholder(true), new Placeholder(false), true);
  }
  
  public MasterDetailPane()
  {
    this(Side.RIGHT, new Placeholder(true), new Placeholder(false), true);
  }
  
  protected Skin<?> createDefaultSkin()
  {
    return new MasterDetailPaneSkin(this);
  }
  
  public String getUserAgentStylesheet()
  {
    return getUserAgentStylesheet(MasterDetailPane.class, "masterdetailpane.css");
  }
  
  public final void resetDividerPosition()
  {
    Node node = getDetailNode();
    if (node == null) {
      return;
    }
    boolean wasShowing = isShowDetailNode();
    boolean wasAnimated = isAnimated();
    if (!wasShowing)
    {
      setAnimated(false);
      setShowDetailNode(true);
      
      node.applyCss();
    }
    double dividerSize = getDividerSizeHint();
    double ps;
    double ps;
    switch (getDetailSide())
    {
    case RIGHT: 
    case LEFT: 
      ps = node.prefWidth(-1.0D) + dividerSize;
      break;
    case BOTTOM: 
    case TOP: 
    default: 
      ps = node.prefHeight(-1.0D) + dividerSize;
    }
    double position = 0.0D;
    switch (getDetailSide())
    {
    case LEFT: 
      position = ps / getWidth();
      break;
    case RIGHT: 
      position = 1.0D - ps / getWidth();
      break;
    case TOP: 
      position = ps / getHeight();
      break;
    case BOTTOM: 
      position = 1.0D - ps / getHeight();
    }
    setDividerPosition(Math.min(1.0D, Math.max(0.0D, position)));
    if (!wasShowing)
    {
      setShowDetailNode(wasShowing);
      setAnimated(wasAnimated);
    }
  }
  
  private final DoubleProperty dividerSizeHint = new SimpleDoubleProperty(this, "dividerSizeHint", 10.0D)
  {
    public void set(double newValue)
    {
      super.set(Math.max(0.0D, newValue));
    }
  };
  
  public final DoubleProperty dividerSizeHintProperty()
  {
    return this.dividerSizeHint;
  }
  
  public final void setDividerSizeHint(double size)
  {
    this.dividerSizeHint.set(size);
  }
  
  public final double getDividerSizeHint()
  {
    return this.dividerSizeHint.get();
  }
  
  private final ObjectProperty<Side> detailSide = new SimpleObjectProperty(this, "detailSide", Side.RIGHT);
  
  public final ObjectProperty<Side> detailSideProperty()
  {
    return this.detailSide;
  }
  
  public final Side getDetailSide()
  {
    return (Side)detailSideProperty().get();
  }
  
  public final void setDetailSide(Side side)
  {
    Objects.requireNonNull(side);
    detailSideProperty().set(side);
  }
  
  private final BooleanProperty showDetailNode = new SimpleBooleanProperty(this, "showDetailNode", true);
  
  public final BooleanProperty showDetailNodeProperty()
  {
    return this.showDetailNode;
  }
  
  public final boolean isShowDetailNode()
  {
    return showDetailNodeProperty().get();
  }
  
  public final void setShowDetailNode(boolean show)
  {
    showDetailNodeProperty().set(show);
  }
  
  private final ObjectProperty<Node> masterNode = new SimpleObjectProperty(this, "masterNode");
  
  public final ObjectProperty<Node> masterNodeProperty()
  {
    return this.masterNode;
  }
  
  public final Node getMasterNode()
  {
    return (Node)masterNodeProperty().get();
  }
  
  public final void setMasterNode(Node node)
  {
    Objects.requireNonNull(node);
    masterNodeProperty().set(node);
  }
  
  private final ObjectProperty<Node> detailNode = new SimpleObjectProperty(this, "detailNode");
  
  public final ObjectProperty<Node> detailNodeProperty()
  {
    return this.detailNode;
  }
  
  public final Node getDetailNode()
  {
    return (Node)detailNodeProperty().get();
  }
  
  public final void setDetailNode(Node node)
  {
    detailNodeProperty().set(node);
  }
  
  private final BooleanProperty animated = new SimpleBooleanProperty(this, "animated", true);
  
  public final BooleanProperty animatedProperty()
  {
    return this.animated;
  }
  
  public final boolean isAnimated()
  {
    return animatedProperty().get();
  }
  
  public final void setAnimated(boolean animated)
  {
    animatedProperty().set(animated);
  }
  
  private DoubleProperty dividerPosition = new SimpleDoubleProperty(this, "dividerPosition", 0.33D);
  
  public final DoubleProperty dividerPositionProperty()
  {
    return this.dividerPosition;
  }
  
  public final double getDividerPosition()
  {
    return this.dividerPosition.get();
  }
  
  public final void setDividerPosition(double position)
  {
    if (getDividerPosition() == position) {
      this.dividerPosition.set(-1.0D);
    }
    this.dividerPosition.set(position);
  }
  
  private static final class Placeholder
    extends Label
  {
    public Placeholder(boolean master)
    {
      super();
      
      setAlignment(Pos.CENTER);
      if (master) {
        setStyle("-fx-background-color: -fx-background;");
      } else {
        setStyle("-fx-background-color: derive(-fx-background, -10%);");
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\MasterDetailPane.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */