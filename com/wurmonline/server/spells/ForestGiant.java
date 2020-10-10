package com.wurmonline.server.spells;

public class ForestGiant
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  ForestGiant()
  {
    super("Forest Giant Strength", 410, 10, 50, 49, 55, 0L);
    this.enchantment = 25;
    this.effectdesc = "increased body strength.";
    this.description = "increases body strength";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\ForestGiant.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */