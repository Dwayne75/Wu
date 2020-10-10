package com.wurmonline.server.modifiers;

import java.util.HashSet;
import java.util.Set;

public abstract class ValueModifier
  implements ModifierTypes
{
  private final int type;
  private Set<ValueModifiedListener> listeners;
  
  ValueModifier()
  {
    this.type = 0;
  }
  
  ValueModifier(int typ)
  {
    this.type = typ;
  }
  
  public final int getType()
  {
    return this.type;
  }
  
  public final void addListener(ValueModifiedListener list)
  {
    if (this.listeners == null) {
      this.listeners = new HashSet();
    }
    this.listeners.add(list);
  }
  
  public final void removeListener(ValueModifiedListener list)
  {
    if (this.listeners == null) {
      this.listeners = new HashSet();
    } else {
      this.listeners.remove(list);
    }
  }
  
  Set<ValueModifiedListener> getListeners()
  {
    return this.listeners;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\modifiers\ValueModifier.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */