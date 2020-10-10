package com.wurmonline.shared.constants;

public final class SpellEffectConstants
{
  public static final int LONGTIMEEFFECT = 100000;
  public static final byte TYPE_GENERAL = 0;
  public static final byte TYPE_PLAYER = 1;
  public static final byte TYPE_KINGDOM = 2;
  public static final byte TYPE_FIEFDOM = 3;
  public static final byte TYPE_VILLAGE = 4;
  public static final byte TYPE_PROXIMITY = 5;
  public static final byte TYPE_RELIGION = 6;
  public static final byte TYPE_CLASS = 7;
  public static final byte TYPE_RACE = 8;
  public static final byte TYPE_ENCHANTMENT = 9;
  public static final byte TYPE_ITEM = 10;
  public static final byte INFLUENCE_BENEFICIAL = 0;
  public static final byte INFLUENCE_HARMFUL = 1;
  public static final byte INFLUENCE_NEUTRAL = 2;
  
  public static final String getTypeName(byte type)
  {
    switch (type)
    {
    case 0: 
      return "General";
    case 1: 
      return "Player";
    case 2: 
      return "Kingdom";
    case 3: 
      return "Fiefdom";
    case 4: 
      return "Village";
    case 5: 
      return "Proximity";
    case 6: 
      return "Religion";
    case 9: 
      return "Enchantment";
    case 7: 
      return "Class";
    case 8: 
      return "Race";
    case 10: 
      return "Item";
    }
    return "Unknown";
  }
  
  public static final String getInfluenceName(byte influence)
  {
    switch (influence)
    {
    case 0: 
      return "Beneficial";
    case 1: 
      return "Harmful";
    case 2: 
      return "Neutral";
    }
    return "Unknown";
  }
  
  public static final String getInfluenceSymbol(byte influence)
  {
    switch (influence)
    {
    case 0: 
      return "+";
    case 1: 
      return "-";
    case 2: 
      return " ";
    }
    return "?";
  }
  
  public static final boolean isTimed(int duration)
  {
    if (duration == 0) {
      return false;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\constants\SpellEffectConstants.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */