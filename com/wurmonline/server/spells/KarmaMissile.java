package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KarmaMissile
  extends KarmaSpell
{
  private static final Logger logger = Logger.getLogger(KarmaMissile.class.getName());
  public static final int RANGE = 24;
  
  public KarmaMissile()
  {
    super("Karma Missile", 551, 15, 300, 15, 1, 60000L);
    this.targetCreature = true;
    
    this.description = "sends a flurry of negative energy missiles towards the target";
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
        sendMissile(performer, target, 0.0F, 0.0F, 0.0F, power);
        double attPower = rollAttack(performer, castSkill, target);
        if (attPower > 0.0D) {
          sendMissile(performer, target, 0.0F, 0.0F, -0.5F, attPower);
        }
        attPower = rollAttack(performer, castSkill, target);
        if (attPower > 0.0D) {
          sendMissile(performer, target, 0.0F, 0.0F, 0.5F, attPower);
        }
        attPower = rollAttack(performer, castSkill, target);
        if (attPower > 0.0D) {
          sendMissile(performer, target, 0.5F, 0.5F, 0.5F, attPower);
        }
        attPower = rollAttack(performer, castSkill, target);
        if (attPower > 0.0D) {
          sendMissile(performer, target, -0.5F, -0.5F, 0.5F, attPower);
        }
      }
      catch (Exception exe)
      {
        logger.log(Level.WARNING, exe.getMessage(), exe);
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You try to missile " + target
        .getName() + " but fail.", (byte)3);
      target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to missile you but fails.", (byte)4);
    }
  }
  
  private final double rollAttack(Creature performer, Skill castSkill, Creature target)
  {
    double distDiff = 0.0D;
    double dist = Creature.getRange(performer, target.getPosX(), target.getPosY());
    try
    {
      distDiff = dist - com.wurmonline.server.behaviours.Actions.actionEntrys[this.number].getRange() / 2.0F;
      if (distDiff > 0.0D) {
        distDiff *= 2.0D;
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, getName() + " error: " + ex.getMessage());
    }
    return trimPower(performer, Math.max(Server.rand
      .nextFloat() * 10.0F, castSkill
      .skillCheck(distDiff + this.difficulty, performer.zoneBonus, true, 1.0F)));
  }
  
  private final void sendMissile(Creature performer, Creature target, float offx, float offy, float offz, double power)
    throws Exception
  {
    VolaTile t = performer.getCurrentTile();
    long shardId = WurmId.getNextTempItemId();
    if (t != null) {
      t.sendProjectile(shardId, (byte)4, "model.spell.ShardOfIce", "Karma Missile", (byte)0, performer
        .getPosX() + offx, performer.getPosY() + offy, performer.getPositionZ() + performer
        .getAltOffZ() + offz, performer
        
        .getStatus().getRotation(), (byte)performer.getLayer(), (int)target.getPosX(), 
        (int)target.getPosY(), target.getPositionZ() + target.getAltOffZ(), performer.getWurmId(), target
        .getWurmId(), 0.0F, 0.0F);
    }
    t = target.getCurrentTile();
    if (t != null) {
      t.sendProjectile(shardId, (byte)4, "model.spell.ShardOfIce", "Karma Missile", (byte)0, performer
        .getPosX() + offx, performer.getPosY() + offy, performer.getPositionZ() + performer
        .getAltOffZ() + offz, performer
        
        .getStatus().getRotation(), (byte)performer.getLayer(), (int)target.getPosX(), 
        (int)target.getPosY(), target.getPositionZ() + target.getAltOffZ(), performer.getWurmId(), target
        .getWurmId(), 0.0F, 0.0F);
    }
    byte pos = target.getBody().getRandomWoundPos();
    double damage = 2500.0D + 3500.0D * (power / 100.0D);
    if ((performer.getPower() > 1) && (Servers.isThisATestServer())) {
      performer.getCommunicator().sendNormalServerMessage("Damage: " + damage);
    }
    target.addWoundOfType(performer, (byte)10, pos, false, 1.0F, true, damage, 0.0F, 0.0F, false, true);
  }
  
  void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    performer.getCommunicator().sendNormalServerMessage("You try to send negative karma to " + target
      .getName() + " but fail.", (byte)3);
    target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to give you negative karma but fails.", (byte)4);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\KarmaMissile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */