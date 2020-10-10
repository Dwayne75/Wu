package com.wurmonline.server.spells;

public final class Corrosion
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  Corrosion()
  {
    super("Corrosion", 262, 30, 40, 50, 44, 0L);
    this.targetJewelry = true;
    this.enchantment = 4;
    this.effectdesc = "will increase any acid damage you cause.";
    this.description = "increases any acid damage you cause";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Corrosion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */