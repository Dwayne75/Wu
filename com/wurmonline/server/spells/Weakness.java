package com.wurmonline.server.spells;

public class Weakness
  extends CreatureEnchantment
{
  public static final int RANGE = 50;
  
  public Weakness()
  {
    super("Weakness", 429, 20, 50, 40, 40, 30000L);
    this.enchantment = 41;
    this.offensive = true;
    this.effectdesc = "reduced body strength.";
    this.description = "reduces body strength by one fifth";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Weakness.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */