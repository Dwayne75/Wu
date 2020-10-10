package com.wurmonline.server.spells;

public class Nimbleness
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  public Nimbleness()
  {
    super("Nimbleness", 416, 20, 60, 60, 30, 0L);
    this.targetWeapon = true;
    this.enchantment = 32;
    this.effectdesc = "increase the chance to hit.";
    this.description = "increases chance to hit";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Nimbleness.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */