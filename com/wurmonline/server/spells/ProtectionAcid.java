package com.wurmonline.server.spells;

public final class ProtectionAcid
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  ProtectionAcid()
  {
    super("Acid Protection", 263, 30, 30, 30, 32, 0L);
    this.targetJewelry = true;
    this.enchantment = 5;
    this.effectdesc = "will reduce any acid damage you take.";
    this.description = "reduces any acid damage you take";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\ProtectionAcid.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */