package org.controlsfx.control.decoration;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.Node;

public abstract class Decoration
{
  private volatile Map<String, Object> properties;
  
  public abstract Node applyDecoration(Node paramNode);
  
  public abstract void removeDecoration(Node paramNode);
  
  public final synchronized Map<String, Object> getProperties()
  {
    if (this.properties == null) {
      this.properties = new HashMap();
    }
    return this.properties;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\controlsfx\control\decoration\Decoration.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */