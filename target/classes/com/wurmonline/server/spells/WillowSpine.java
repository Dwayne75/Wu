package com.wurmonline.server.spells;

public class WillowSpine
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  public WillowSpine()
  {
    super("Willowspine", 405, 10, 20, 29, 35, 30000L);
    this.enchantment = 23;
    this.effectdesc = "increased chance to dodge.";
    this.description = "increases chance to dodge attacks";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\WillowSpine.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */