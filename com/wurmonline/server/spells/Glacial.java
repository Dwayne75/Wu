package com.wurmonline.server.spells;

public final class Glacial
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  Glacial()
  {
    super("Glacial", 261, 30, 40, 50, 47, 0L);
    this.targetJewelry = true;
    this.enchantment = 3;
    this.effectdesc = "will increase any frost damage you cause.";
    this.description = "increases any frost damage you cause";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Glacial.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */