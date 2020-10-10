package com.wurmonline.server.spells;

public class Venom
  extends ItemEnchantment
{
  public static final int RANGE = 40;
  
  Venom()
  {
    super("Venom", 412, 20, 100, 60, 62, 0L);
    this.targetWeapon = true;
    this.enchantment = 27;
    this.effectdesc = "will deal only poison damage wounds.";
    this.description = "causes a weapon to deal poison wounds instead of normal damage, but may reduce damage";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\Venom.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */