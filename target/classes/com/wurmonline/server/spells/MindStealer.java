package com.wurmonline.server.spells;

public class MindStealer
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  public MindStealer()
  {
    super("Mind Stealer", 415, 20, 100, 60, 50, 0L);
    this.targetWeapon = true;
    this.enchantment = 31;
    this.effectdesc = "will steal skill knowledge from some creatures.";
    this.description = "may steal some higher skills from enemies on hits";
    this.type = 2;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\MindStealer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */