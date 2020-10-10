package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.skills.Skill;

public final class LocateArtifact
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  LocateArtifact()
  {
    super("Locate Artifact", 271, 30, 70, 70, 80, 1800000L);
    this.targetTile = true;
    this.description = "locates hidden artifacts";
    this.type = 2;
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer)
  {
    if ((performer.getPower() > 0) && (performer.getPower() < 5))
    {
      performer.getCommunicator().sendNormalServerMessage("You may not cast this spell.", (byte)3);
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    performer.getCommunicator().sendNormalServerMessage(EndGameItems.locateRandomEndGameItem(performer));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\LocateArtifact.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */