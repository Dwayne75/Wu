package com.wurmonline.server.modifiers;

import java.util.Iterator;
import java.util.Set;

public class DoubleValueModifier
  extends ValueModifier
{
  private double modifier = 0.0D;
  
  public DoubleValueModifier(double value)
  {
    this.modifier = value;
  }
  
  public DoubleValueModifier(int aType, double value)
  {
    super(aType);
    this.modifier = value;
  }
  
  public double getModifier()
  {
    return this.modifier;
  }
  
  public void setModifier(double newValue)
  {
    this.modifier = newValue;
    Iterator<ValueModifiedListener> it;
    if (getListeners() != null) {
      for (it = getListeners().iterator(); it.hasNext();)
      {
        ValueModifiedListener list = (ValueModifiedListener)it.next();
        list.valueChanged(this.modifier, newValue);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\modifiers\DoubleValueModifier.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */