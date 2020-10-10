package com.wurmonline.server.spells;

public final class MorningFog
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  MorningFog()
  {
    super("Morning Fog", 282, 10, 5, 10, 7, 0L);
    this.targetCreature = true;
    this.enchantment = 19;
    this.effectdesc = "protection from thorns and lava.";
    this.description = "protection from thorns and lava";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\MorningFog.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */