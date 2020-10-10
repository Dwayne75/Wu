package com.wurmonline.server.spells;

public final class SharedPain
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  SharedPain()
  {
    super("Aura of Shared Pain", 278, 20, 35, 60, 25, 0L);
    this.targetArmour = true;
    this.enchantment = 17;
    this.effectdesc = "may damage creatures when they hit this armour.";
    this.description = "causes damage to creatures when they hit armour enchanted with this";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\SharedPain.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */