package com.wurmonline.server.spells;

public final class ProtectionFrost
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  ProtectionFrost()
  {
    super("Frost Protection", 264, 30, 30, 30, 30, 0L);
    this.targetJewelry = true;
    this.enchantment = 6;
    this.effectdesc = "will reduce any frost damage you take.";
    this.description = "reduces any frost damage you take";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\ProtectionFrost.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */