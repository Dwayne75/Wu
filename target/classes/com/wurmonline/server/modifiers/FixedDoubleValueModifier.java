package com.wurmonline.server.modifiers;

public final class FixedDoubleValueModifier
  extends DoubleValueModifier
{
  public FixedDoubleValueModifier(double aValue)
  {
    super(aValue);
  }
  
  public FixedDoubleValueModifier(int aType, double aValue)
  {
    super(aType, aValue);
  }
  
  public void setModifier(double aNewValue)
  {
    if (!$assertionsDisabled) {
      throw new AssertionError("Do not call FixedDoubleValueModifier.setModifier()");
    }
    throw new IllegalArgumentException("Do not call FixedDoubleValueModifier.setModifier(). The modifier cannot be changed.");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\modifiers\FixedDoubleValueModifier.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */