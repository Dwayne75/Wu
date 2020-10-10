package org.controlsfx.control.decoration;

import impl.org.controlsfx.ImplUtils;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;

public class GraphicDecoration
  extends Decoration
{
  private final Node decorationNode;
  private final Pos pos;
  private final double xOffset;
  private final double yOffset;
  
  public GraphicDecoration(Node decorationNode)
  {
    this(decorationNode, Pos.TOP_LEFT);
  }
  
  public GraphicDecoration(Node decorationNode, Pos position)
  {
    this(decorationNode, position, 0.0D, 0.0D);
  }
  
  public GraphicDecoration(Node decorationNode, Pos position, double xOffset, double yOffset)
  {
    this.decorationNode = decorationNode;
    this.decorationNode.setManaged(false);
    this.pos = position;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }
  
  public Node applyDecoration(Node targetNode)
  {
    List<Node> targetNodeChildren = ImplUtils.getChildren((Parent)targetNode, true);
    updateGraphicPosition(targetNode);
    if (!targetNodeChildren.contains(this.decorationNode)) {
      targetNodeChildren.add(this.decorationNode);
    }
    return null;
  }
  
  public void removeDecoration(Node targetNode)
  {
    List<Node> targetNodeChildren = ImplUtils.getChildren((Parent)targetNode, true);
    if (targetNodeChildren.contains(this.decorationNode)) {
      targetNodeChildren.remove(this.decorationNode);
    }
  }
  
  private void updateGraphicPosition(final Node targetNode)
  {
    double decorationNodeWidth = this.decorationNode.prefWidth(-1.0D);
    double decorationNodeHeight = this.decorationNode.prefHeight(-1.0D);
    
    Bounds targetBounds = targetNode.getLayoutBounds();
    double x = targetBounds.getMinX();
    double y = targetBounds.getMinY();
    
    double targetWidth = targetBounds.getWidth();
    if (targetWidth <= 0.0D) {
      targetWidth = targetNode.prefWidth(-1.0D);
    }
    double targetHeight = targetBounds.getHeight();
    if (targetHeight <= 0.0D) {
      targetHeight = targetNode.prefHeight(-1.0D);
    }
    if ((targetWidth <= 0.0D) && (targetHeight <= 0.0D)) {
      targetNode.layoutBoundsProperty().addListener(new ChangeListener()
      {
        public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue)
        {
          targetNode.layoutBoundsProperty().removeListener(this);
          GraphicDecoration.this.updateGraphicPosition(targetNode);
        }
      });
    }
    switch (this.pos.getHpos())
    {
    case CENTER: 
      x += targetWidth / 2.0D - decorationNodeWidth / 2.0D;
      break;
    case LEFT: 
      x -= decorationNodeWidth / 2.0D;
      break;
    case RIGHT: 
      x += targetWidth - decorationNodeWidth / 2.0D;
    }
    switch (this.pos.getVpos())
    {
    case CENTER: 
      y += targetHeight / 2.0D - decorationNodeHeight / 2.0D;
      break;
    case TOP: 
      y -= decorationNodeHeight / 2.0D;
      break;
    case BOTTOM: 
      y += targetHeight - decorationNodeWidth / 2.0D;
      break;
    case BASELINE: 
      y += targetNode.getBaselineOffset() - this.decorationNode.getBaselineOffset() - decorationNodeHeight / 2.0D;
    }
    this.decorationNode.setLayoutX(x + this.xOffset);
    this.decorationNode.setLayoutY(y + this.yOffset);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\decoration\GraphicDecoration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */