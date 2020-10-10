package org.controlsfx.control.decoration;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class StyleClassDecoration
  extends Decoration
{
  private final String[] styleClasses;
  
  public StyleClassDecoration(String... styleClass)
  {
    if ((styleClass == null) || (styleClass.length == 0)) {
      throw new IllegalArgumentException("var-arg style class array must not be null or empty");
    }
    this.styleClasses = styleClass;
  }
  
  public Node applyDecoration(Node targetNode)
  {
    List<String> styleClassList = targetNode.getStyleClass();
    for (String styleClass : this.styleClasses) {
      if (!styleClassList.contains(styleClass)) {
        styleClassList.add(styleClass);
      }
    }
    return null;
  }
  
  public void removeDecoration(Node targetNode)
  {
    targetNode.getStyleClass().removeAll(this.styleClasses);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\decoration\StyleClassDecoration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */