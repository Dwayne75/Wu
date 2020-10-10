package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FireHeart
  extends DamageSpell
  implements AttitudeConstants
{
  private static Logger logger = Logger.getLogger(FireHeart.class.getName());
  public static final int RANGE = 50;
  public static final double BASE_DAMAGE = 9000.0D;
  public static final double DAMAGE_PER_POWER = 80.0D;
  
  public FireHeart()
  {
    super("Fireheart", 424, 7, 20, 20, 35, 30000L);
    this.targetCreature = true;
    this.offensive = true;
    this.description = "damages the targets heart with superheated fire";
    this.type = 2;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2)) {
      if (performer.faithful) {
        if (!performer.isDuelOrSpar(target))
        {
          performer.getCommunicator().sendNormalServerMessage(performer
            .getDeity().getName() + " would never accept your attack on " + target.getNameWithGenus() + ".", (byte)3);
          
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
        performer.modifyFaith(-5.0F);
      }
    }
    VolaTile t = target.getCurrentTile();
    if (t != null)
    {
      t.sendAddQuickTileEffect((byte)35, target.getFloorLevel());
      t.sendAttachCreatureEffect(target, (byte)5, (byte)0, (byte)0, (byte)0, (byte)0);
    }
    try
    {
      byte pos = target.getBody().getCenterWoundPos();
      
      double damage = calculateDamage(target, power, 9000.0D, 80.0D);
      
      target.addWoundOfType(performer, (byte)4, pos, false, 1.0F, false, damage, 0.0F, 0.0F, false, true);
    }
    catch (Exception exe)
    {
      logger.log(Level.WARNING, exe.getMessage(), exe);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\FireHeart.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */