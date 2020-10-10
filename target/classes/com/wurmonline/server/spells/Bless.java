package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Bless
  extends ReligiousSpell
  implements AttitudeConstants
{
  private static final Logger logger = Logger.getLogger(Bless.class.getName());
  public static final int RANGE = 4;
  
  Bless()
  {
    super("Bless", 245, 10, 10, 10, 8, 0L);
    this.targetCreature = true;
    this.targetItem = true;
    this.description = "adds a holy aura of purity";
    this.type = 0;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (performer.getDeity() != null)
    {
      if (target.isPlayer())
      {
        if (target.getAttitude(performer) != 2) {
          return true;
        }
        performer.getCommunicator().sendNormalServerMessage(performer
          .getDeity().getName() + " would never help the infidel " + target.getName() + ".", (byte)3);
        
        return false;
      }
      boolean isLibila = performer.getDeity().isLibila();
      if (isLibila)
      {
        if (target.hasTrait(22))
        {
          performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is already corrupt.", (byte)3);
          return false;
        }
        return true;
      }
      if (target.hasTrait(22)) {
        return true;
      }
      performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is not corrupt.", (byte)3);
      return false;
    }
    return false;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if (performer.getDeity() != null)
    {
      if (target.getBless() == null)
      {
        if (target.isUnfinished())
        {
          performer.getCommunicator().sendNormalServerMessage("The spell will not work on unfinished items.", (byte)3);
          return false;
        }
        return true;
      }
      performer.getCommunicator().sendNormalServerMessage("The " + target
        .getName() + " is already blessed to " + target.getBless().getName() + ".", (byte)3);
    }
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    target.getCommunicator().sendNormalServerMessage(performer.getName() + " blesses you.");
    performer.getCommunicator().sendNormalServerMessage("You bless " + target.getNameWithGenus() + ".");
    if (performer.getDeity() != null) {
      if (target.isPlayer())
      {
        if (performer.getDeity().accepts(target.getAlignment()))
        {
          try
          {
            if (target.getFavor() < performer.getFavor())
            {
              if (target.getFavor() < target.getFaith()) {
                if (performer.getDeity().isHateGod()) {
                  performer.maybeModifyAlignment(-1.0F);
                } else {
                  performer.maybeModifyAlignment(1.0F);
                }
              }
              target.setFavor(
                (float)(target.getFavor() + this.cost * 100 / (performer.getFaith() * 30.0F) * castSkill.getKnowledge(performer.zoneBonus) / 100.0D));
              target.getCommunicator().sendNormalServerMessage("The light of " + performer
                .getDeity().getName() + " shines upon you.");
            }
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, performer.getName(), iox);
          }
        }
        else
        {
          target.getCommunicator().sendNormalServerMessage(performer
            .getDeity().getName() + " does not seem pleased with " + target.getNameWithGenus() + ".");
          performer.getCommunicator().sendNormalServerMessage(performer
            .getDeity().getName() + " does not seem pleased with " + target.getNameWithGenus() + ".");
        }
      }
      else {
        blessCreature(performer, target);
      }
    }
  }
  
  void blessCreature(Creature performer, Creature target)
  {
    boolean isLibila = performer.getDeity().isLibila();
    boolean isCorrupt = target.hasTrait(22);
    if ((isLibila) && (!isCorrupt))
    {
      target.getStatus().setTraitBit(22, true);
      if (!target.hasTrait(63)) {
        performer.getCommunicator().sendNormalServerMessage("The dark energies of Libila flows through " + target
          .getNameWithGenus() + " corrupting " + target
          .getHimHerItString() + ".");
      } else {
        performer.getCommunicator().sendNormalServerMessage("The dark energies of Libila flows through " + target
          .getNameWithGenus() + " corrupting " + target
          .getHimHerItString() + ".");
      }
    }
    else if ((!isLibila) && (isCorrupt))
    {
      target.getStatus().setTraitBit(22, false);
      
      String deityName = performer.getDeity().getName();
      
      performer.getCommunicator().sendNormalServerMessage(
        StringUtil.format("The cleansing power of %s courses through %s purifying %s.", new Object[] { deityName, target
        
        .getNameWithGenus(), target.getHimHerItString() }));
    }
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    target.bless(performer.getDeity().getNumber());
    if (target.isDomainItem())
    {
      target.setName(target.getName() + " of " + performer.getDeity().getName());
      performer.getCommunicator().sendNormalServerMessage("You may now pray at the blessed altar.");
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You bless the " + target
        .getName() + " with the power of " + performer.getDeity().getName() + ".");
      if (target.getTemplateId() == 654) {
        performer.getCommunicator().sendUpdateInventoryItem(target);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\Bless.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */