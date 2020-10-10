package org.seamless.swing;

import java.util.List;

public abstract interface Node<T>
{
  public abstract Long getId();
  
  public abstract T getParent();
  
  public abstract List<T> getChildren();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\Node.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */