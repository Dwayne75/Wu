package com.wurmonline.server.spells;

import com.wurmonline.shared.constants.AttitudeConstants;

public abstract class KarmaSpell
  extends Spell
  implements AttitudeConstants
{
  public KarmaSpell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long cooldown)
  {
    super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, cooldown, false);
    this.karmaSpell = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\KarmaSpell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */