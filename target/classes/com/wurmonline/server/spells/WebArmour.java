package com.wurmonline.server.spells;

public class WebArmour
  extends ItemEnchantment
{
  public static final int RANGE = 4;
  
  public WebArmour()
  {
    super("Web Armour", 455, 20, 35, 60, 25, 0L);
    this.targetArmour = true;
    this.enchantment = 46;
    this.effectdesc = "may slow down creatures when they hit this armour.";
    this.description = "may slow down creatures when they hit armour enchanted with this";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\WebArmour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */