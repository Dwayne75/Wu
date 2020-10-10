package com.wurmonline.server.spells;

public class TrueHit
  extends CreatureEnchantment
{
  public static final int RANGE = 4;
  
  public TrueHit()
  {
    super("Truehit", 447, 10, 10, 15, 30, 0L);
    this.targetCreature = true;
    this.enchantment = 30;
    this.effectdesc = "combat vision and aiming.";
    this.description = "increases offensive combat rating";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\TrueHit.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */