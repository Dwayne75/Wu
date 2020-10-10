package com.wurmonline.server.spells;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShardOfIce
  extends DamageSpell
  implements AttitudeConstants
{
  private static Logger logger = Logger.getLogger(FireHeart.class.getName());
  public static final int RANGE = 50;
  public static final double BASE_DAMAGE = 5000.0D;
  public static final double DAMAGE_PER_POWER = 120.0D;
  
  public ShardOfIce()
  {
    super("Shard of Ice", 485, 7, 20, 30, 35, 30000L);
    this.targetCreature = true;
    this.offensive = true;
    this.description = "damages the targets body with a spear of ice causing frost damage";
    this.type = 2;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2)) {
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
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2)) {
      if (!performer.isDuelOrSpar(target)) {
        performer.modifyFaith(-(100.0F - performer.getFaith()) / 50.0F);
      }
    }
    try
    {
      VolaTile t = performer.getCurrentTile();
      long shardId = WurmId.getNextTempItemId();
      if (t != null) {
        t.sendProjectile(shardId, (byte)4, "model.spell.ShardOfIce", "Shard Of Ice", (byte)0, performer
          .getPosX(), performer.getPosY(), performer.getPositionZ() + performer
          .getAltOffZ(), performer
          .getStatus().getRotation(), (byte)performer.getLayer(), (int)target.getPosX(), 
          (int)target.getPosY(), target.getPositionZ() + target.getAltOffZ(), performer.getWurmId(), target
          .getWurmId(), 0.0F, 0.0F);
      }
      t = target.getCurrentTile();
      if (t != null) {
        t.sendProjectile(shardId, (byte)4, "model.spell.ShardOfIce", "Shard Of Ice", (byte)0, performer
          .getPosX(), performer.getPosY(), performer.getPositionZ() + performer
          .getAltOffZ(), performer
          .getStatus().getRotation(), (byte)performer.getLayer(), (int)target.getPosX(), 
          (int)target.getPosY(), target.getPositionZ() + target.getAltOffZ(), performer.getWurmId(), target
          .getWurmId(), 0.0F, 0.0F);
      }
      byte pos = target.getBody().getCenterWoundPos();
      
      double damage = calculateDamage(target, power, 5000.0D, 120.0D);
      
      target.addWoundOfType(performer, (byte)8, pos, false, 1.0F, false, damage, 0.0F, 0.0F, false, true);
    }
    catch (Exception exe)
    {
      logger.log(Level.WARNING, exe.getMessage(), exe);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\ShardOfIce.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */