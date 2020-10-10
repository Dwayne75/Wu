package com.wurmonline.server.spells;

public final class ProtectionFire
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  ProtectionFire()
  {
    super("Fire Protection", 265, 30, 30, 30, 28, 0L);
    this.targetJewelry = true;
    this.enchantment = 7;
    this.effectdesc = "will reduce any fire damage you take.";
    this.description = "reduces any fire damage you take";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\ProtectionFire.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */