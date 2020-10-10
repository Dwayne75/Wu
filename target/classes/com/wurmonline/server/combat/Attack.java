package com.wurmonline.server.combat;

public final class Attack
{
  public static final byte NOT_FOCUSSED_ON_COMBAT = 0;
  public static final byte BALANCED_FIGHT_LEVEL = 1;
  public static final byte FOCUSSED_FIGHT_LEVEL = 2;
  public static final byte QUICK_FIGHT_LEVEL = 3;
  public static final byte ATTENTIVE_FIGHT_LEVEL = 4;
  public static final byte MAXFIGHTLEVEL = 5;
  public static final String[] focusStrings = { "You are not focused on combat.", "You balance your feet and your soul.", "You are now focused on the enemy and its every move.", "You feel lightning inside, quickening your reflexes.", "Your consciousness is lifted to a higher level, making you very attentive.", "You feel supernatural. Invincible!" };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\combat\Attack.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */