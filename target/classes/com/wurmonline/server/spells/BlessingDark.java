package com.wurmonline.server.spells;

public class BlessingDark
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  public BlessingDark()
  {
    super("Blessings of the Dark", 456, 20, 70, 60, 51, 0L);
    this.targetItem = true;
    this.enchantment = 47;
    this.effectdesc = "will increase skill gained and speed with it when used.";
    this.description = "increases skill gain and usage speed";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\BlessingDark.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */