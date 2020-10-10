package com.wurmonline.server.spells;

public class Hellstrength
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  public Hellstrength()
  {
    super("Hell Strength", 427, 10, 60, 40, 45, 30000L);
    
    this.enchantment = 40;
    this.effectdesc = "increased body strength and soul strength.";
    this.description = "increases body strength and soul strength";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Hellstrength.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */