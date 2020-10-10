package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.Decorator;

public class DecorationPane
  extends StackPane
{
  private final Map<Node, List<Node>> nodeDecorationMap = new WeakHashMap();
  ChangeListener<Boolean> visibilityListener = new ChangeListener()
  {
    public void changed(ObservableValue<? extends Boolean> o, Boolean wasVisible, Boolean isVisible)
    {
      BooleanProperty p = (BooleanProperty)o;
      Node n = (Node)p.getBean();
      
      DecorationPane.this.removeAllDecorationsOnNode(n, Decorator.getDecorations(n));
      Decorator.removeAllDecorations(n);
    }
  };
  
  public DecorationPane()
  {
    setBackground(null);
  }
  
  public void setRoot(Node root)
  {
    getChildren().setAll(new Node[] { root });
  }
  
  public void updateDecorationsOnNode(Node targetNode, List<Decoration> added, List<Decoration> removed)
  {
    removeAllDecorationsOnNode(targetNode, removed);
    addAllDecorationsOnNode(targetNode, added);
  }
  
  private void showDecoration(Node targetNode, Decoration decoration)
  {
    Node decorationNode = decoration.applyDecoration(targetNode);
    if (decorationNode != null)
    {
      List<Node> decorationNodes = (List)this.nodeDecorationMap.get(targetNode);
      if (decorationNodes == null)
      {
        decorationNodes = new ArrayList();
        this.nodeDecorationMap.put(targetNode, decorationNodes);
      }
      decorationNodes.add(decorationNode);
      if (!getChildren().contains(decorationNode))
      {
        getChildren().add(decorationNode);
        StackPane.setAlignment(decorationNode, Pos.TOP_LEFT);
      }
    }
    targetNode.visibleProperty().addListener(this.visibilityListener);
  }
  
  private void removeAllDecorationsOnNode(Node targetNode, List<Decoration> decorations)
  {
    if ((decorations == null) || (targetNode == null)) {
      return;
    }
    List<Node> decorationNodes = (List)this.nodeDecorationMap.remove(targetNode);
    if (decorationNodes != null) {
      for (Node decorationNode : decorationNodes)
      {
        boolean success = getChildren().remove(decorationNode);
        if (!success) {
          throw new IllegalStateException("Could not remove decoration " + decorationNode + " from decoration pane children list: " + getChildren());
        }
      }
    }
    for (Decoration decoration : decorations) {
      decoration.removeDecoration(targetNode);
    }
  }
  
  private void addAllDecorationsOnNode(Node targetNode, List<Decoration> decorations)
  {
    if (decorations == null) {
      return;
    }
    for (Decoration decoration : decorations) {
      showDecoration(targetNode, decoration);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\skin\DecorationPane.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */