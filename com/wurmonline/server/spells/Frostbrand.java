package com.wurmonline.server.spells;

public class Frostbrand
  extends ItemEnchantment
{
  public static final int RANGE = 40;
  
  public Frostbrand()
  {
    super("Frostbrand", 417, 20, 45, 60, 40, 0L);
    this.targetWeapon = true;
    this.enchantment = 33;
    this.effectdesc = "will cause frost wounds.";
    this.description = "causes extra frost wounds on an enemy when hit";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Frostbrand.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */