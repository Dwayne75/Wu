package com.wurmonline.server.spells;

public class Excel
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  public Excel()
  {
    super("Excel", 442, 20, 20, 30, 35, 0L);
    this.enchantment = 28;
    this.effectdesc = "increased defensive combat capabilities.";
    this.description = "increases defensive combat rating";
    this.type = 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Excel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */