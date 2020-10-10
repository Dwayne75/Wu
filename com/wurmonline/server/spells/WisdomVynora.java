package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.skills.Skill;

public class WisdomVynora
  extends ReligiousSpell
{
  public static final int RANGE = 12;
  
  public WisdomVynora()
  {
    super("Wisdom of Vynora", 445, 30, 30, 50, 30, 1800000L);
    this.targetCreature = true;
    this.description = "transfers fatigue to sleep bonus";
    this.type = 1;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (target.isReborn()) {
      return true;
    }
    if (target.getFatigueLeft() < 100)
    {
      performer.getCommunicator().sendNormalServerMessage(target.getName() + " has almost no fatigue left.", (byte)3);
      
      return false;
    }
    if (!target.equals(performer))
    {
      if (performer.getDeity() != null)
      {
        if (target.getDeity() != null)
        {
          if (target.getDeity().isHateGod())
          {
            if (performer.isFaithful())
            {
              performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " would never help the infidel " + target
                .getName() + "!", (byte)3);
              
              return false;
            }
            return true;
          }
          return true;
        }
        return true;
      }
      return true;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    double toconvert = power;
    toconvert = Math.max(20.0D, power);
    toconvert = Math.min(99.0D, toconvert + performer.getNumLinks() * 10);
    
    toconvert /= 100.0D;
    
    int numsecondsToMove = Math.min((int)(target.getFatigueLeft() / 12.0F * toconvert), 3600);
    target.setFatigue(-numsecondsToMove);
    
    numsecondsToMove = (int)(numsecondsToMove * 0.2F);
    if (target.isPlayer()) {
      ((Player)target).getSaveFile().addToSleep(numsecondsToMove);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\WisdomVynora.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */