package com.wurmonline.server.spells;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KarmaBolt
  extends KarmaSpell
{
  private static final Logger logger = Logger.getLogger(KarmaMissile.class.getName());
  public static final int RANGE = 24;
  
  public KarmaBolt()
  {
    super("Karma Bolt", 550, 10, 200, 10, 1, 180000L);
    this.targetCreature = true;
    this.offensive = true;
    
    this.description = "sends a thick bolt of negative Karma towards the target";
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2) && 
      (!performer.getDeity().isHateGod())) {
      if (performer.faithful) {
        if (!performer.isDuelOrSpar(target))
        {
          performer.getCommunicator().sendNormalServerMessage(performer
            .getDeity().getName() + " would never accept your attack on " + target.getName() + ".", (byte)3);
          
          return false;
        }
      }
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    if ((!target.isUnique()) || (power > 99.0D))
    {
      if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2) && 
        (!performer.getDeity().isHateGod())) {
        if (!performer.isDuelOrSpar(target)) {
          performer.modifyFaith(-(100.0F - performer.getFaith()) / 50.0F);
        }
      }
      try
      {
        sendBolt(performer, target, 0.0F, 0.0F, 0.0F, power);
      }
      catch (Exception exe)
      {
        logger.log(Level.WARNING, exe.getMessage(), exe);
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You try to bolt " + target
        .getName() + " but fail.", (byte)3);
      target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to bolt you but fails.", (byte)4);
    }
  }
  
  private final void sendBolt(Creature performer, Creature target, float offx, float offy, float offz, double power)
    throws Exception
  {
    VolaTile t = performer.getCurrentTile();
    long shardId = WurmId.getNextTempItemId();
    if (t != null) {
      t.sendProjectile(shardId, (byte)4, "model.spell.ShardOfIce", "Karma Bolt", (byte)0, performer
        .getPosX() + offx, performer.getPosY() + offy, performer.getPositionZ() + performer
        .getAltOffZ() + offz, performer
        
        .getStatus().getRotation(), (byte)performer.getLayer(), (int)target.getPosX(), 
        (int)target.getPosY(), target.getPositionZ() + target.getAltOffZ(), performer.getWurmId(), target
        .getWurmId(), 0.0F, 0.0F);
    }
    t = target.getCurrentTile();
    if (t != null) {
      t.sendProjectile(shardId, (byte)4, "model.spell.ShardOfIce", "Karma Bolt", (byte)0, performer
        .getPosX() + offx, performer.getPosY() + offy, performer.getPositionZ() + performer
        .getAltOffZ() + offz, performer
        
        .getStatus().getRotation(), (byte)performer.getLayer(), (int)target.getPosX(), 
        (int)target.getPosY(), target.getPositionZ() + target.getAltOffZ(), performer.getWurmId(), target
        .getWurmId(), 0.0F, 0.0F);
    }
    double damage = 5000.0D + 5000.0D * (power / 100.0D);
    target.addWoundOfType(performer, (byte)6, 1, true, 1.0F, true, damage, 20.0F, 0.0F, false, true);
  }
  
  void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    performer.getCommunicator().sendNormalServerMessage("You try to send negative karma to " + target
      .getName() + " but fail.", (byte)3);
    
    target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to give you negative karma but fails.", (byte)4);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\KarmaBolt.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */