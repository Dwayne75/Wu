package com.wurmonline.server.modifiers;

public final class FloatValueModifier
  extends ValueModifier
{
  private float modifier = 0.0F;
  
  public FloatValueModifier(float value)
  {
    this.modifier = value;
  }
  
  public FloatValueModifier(int aType, float value)
  {
    super(aType);
    this.modifier = value;
  }
  
  public float getModifier()
  {
    return this.modifier;
  }
  
  public void setModifier(float newValue)
  {
    this.modifier = newValue;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\modifiers\FloatValueModifier.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */