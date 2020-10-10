package com.wurmonline.server.spells;

public class GoatShape
  extends CreatureEnchantment
{
  public static final int RANGE = 20;
  
  public GoatShape()
  {
    super("Goat Shape", 422, 10, 20, 20, 25, 0L);
    this.enchantment = 38;
    this.effectdesc = "better climbing capability.";
    this.description = "increases climbing ability";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\GoatShape.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */