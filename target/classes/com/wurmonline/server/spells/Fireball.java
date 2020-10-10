package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Fireball
  extends KarmaSpell
{
  private static final Logger logger = Logger.getLogger(Fireball.class.getName());
  public static final int RANGE = 24;
  
  public Fireball()
  {
    super("Fireball", 549, 15, 1000, 30, 1, 180000L);
    this.targetCreature = true;
    this.offensive = true;
    
    this.description = "sends an exploding ball of fire towards the target";
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2) && 
      (!performer.getDeity().isHateGod())) {
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
    if ((!target.isUnique()) || (power > 99.0D))
    {
      int diameter = (int)Math.max(power / 30.0D, 1.0D);
      int tfloorlevel = target.getFloorLevel();
      Set<PathTile> tiles = Zones.explode(target.getTileX(), target.getTileY(), tfloorlevel, true, diameter);
      
      boolean insideStructure = false;
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " burns you.", (byte)4);
      if (target.getCurrentTile() != null)
      {
        target.getCurrentTile().sendAttachCreatureEffect(target, (byte)4, (byte)0, (byte)0, (byte)0, (byte)0);
        
        insideStructure = true;
      }
      int maxLevel = tfloorlevel >= 0 ? 2 : tfloorlevel;
      for (int fLevel = target.getFloorLevel(); fLevel <= maxLevel; fLevel++) {
        if ((fLevel <= 0) || (insideStructure)) {
          for (PathTile pathtile : tiles)
          {
            VolaTile targetVolaTile = Zones.getOrCreateTile(pathtile.getTileX(), pathtile
              .getTileY(), pathtile.getFloorLevel() >= 0);
            if (targetVolaTile != null)
            {
              Creature[] crets = targetVolaTile.getCreatures();
              for (Creature creature : crets) {
                if ((creature.getWurmId() != performer.getWurmId()) && 
                  (creature.getKingdomId() != performer.getKingdomId()) && 
                  (creature.getPower() < 2)) {
                  if (creature.getFloorLevel() == fLevel) {
                    try
                    {
                      double damage = 9000.0D + 6000.0D * (power / 100.0D);
                      byte pos = creature.getBody().getRandomWoundPos();
                      
                      creature.addWoundOfType(performer, (byte)4, pos, false, 1.0F, true, damage, 0.0F, 0.0F, false, true);
                    }
                    catch (Exception ex)
                    {
                      logger.log(Level.INFO, ex.getMessage(), ex);
                    }
                  }
                }
              }
              if (targetVolaTile.getStructure() != null)
              {
                insideStructure = true;
                
                Floor f = targetVolaTile.getTopFloor();
                if ((f == null) || (f.getFloorLevel() > fLevel)) {
                  targetVolaTile.sendAddQuickTileEffect((byte)65, fLevel);
                }
              }
              else
              {
                targetVolaTile.sendAddQuickTileEffect((byte)65, fLevel);
              }
            }
          }
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You try to fireball " + target
        .getNameWithGenus() + " but fail.");
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to fireball you but fails.");
    }
  }
  
  void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    performer.getCommunicator().sendNormalServerMessage("You try to set " + target
      .getNameWithGenus() + " on fire but fail.");
    target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to set you on fire but fails.");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\Fireball.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */