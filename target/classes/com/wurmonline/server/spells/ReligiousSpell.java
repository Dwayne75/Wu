package com.wurmonline.server.spells;

abstract class ReligiousSpell
  extends Spell
{
  ReligiousSpell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long cooldown)
  {
    super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, cooldown, true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\ReligiousSpell.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */