package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CureSerious
  extends ReligiousSpell
{
  private static final Logger logger = Logger.getLogger(CureSerious.class.getName());
  public static final int RANGE = 12;
  
  CureSerious()
  {
    super("Cure Serious", 248, 15, 17, 15, 35, 0L);
    this.targetWound = true;
    this.targetCreature = true;
    this.healing = true;
    this.description = "heals an extreme amount of damage on a single wound";
    this.type = 0;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    Wounds tWounds = target.getBody().getWounds();
    if ((tWounds != null) && (tWounds.getWounds().length > 0)) {
      return precondition(castSkill, performer, tWounds.getWounds()[0]);
    }
    performer.getCommunicator().sendNormalServerMessage(target.getName() + " has no wounds to heal.");
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    Wounds tWounds = target.getBody().getWounds();
    if ((tWounds != null) && (tWounds.getWounds().length > 0))
    {
      Wound highestWound = tWounds.getWounds()[0];
      float highestSeverity = highestWound.getSeverity();
      for (Wound w : tWounds.getWounds()) {
        if (w.getSeverity() > highestSeverity)
        {
          highestWound = w;
          highestSeverity = w.getSeverity();
        }
      }
      doEffect(castSkill, power, performer, highestWound);
    }
  }
  
  boolean precondition(Skill castSkill, Creature performer, Wound target)
  {
    if (target.getCreature() == null)
    {
      performer.getCommunicator().sendNormalServerMessage("You cannot heal that wound.", (byte)3);
      return false;
    }
    Creature tCret = target.getCreature();
    if (tCret.isReborn()) {
      return true;
    }
    if ((tCret.isPlayer()) && (target.getCreature() != performer))
    {
      if (!tCret.isFriendlyKingdom(performer.getKingdomId()))
      {
        if (performer.faithful)
        {
          performer.getCommunicator().sendNormalServerMessage(performer
            .getDeity().getName() + " would never accept that.", (byte)3);
          
          return false;
        }
        return true;
      }
      return true;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Wound target)
  {
    boolean doeff = true;
    Creature tCret = target.getCreature();
    if (tCret.isReborn())
    {
      doeff = false;
      performer.getCommunicator().sendNormalServerMessage("The wound grows.", (byte)3);
      target.modifySeverity(6000);
    }
    else if ((tCret.isPlayer()) && (target.getCreature() != performer))
    {
      if (performer.getDeity() != null) {
        if (!tCret.isFriendlyKingdom(performer.getKingdomId()))
        {
          performer.getCommunicator().sendNormalServerMessage(performer
            .getDeity().getName() + " becomes very upset at the way you abuse " + performer
            .getDeity().getHisHerItsString() + " powers!", (byte)3);
          try
          {
            performer.setFaith(performer.getFaith() / 2.0F);
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, ex.getMessage(), ex);
          }
        }
      }
    }
    if (doeff)
    {
      double resistance = SpellResist.getSpellResistance(tCret, getNumber());
      double toHeal = 58950.0D;
      toHeal += 58950.0D * (power / 300.0D);
      if ((performer.getCultist() != null) && (performer.getCultist().healsFaster())) {
        toHeal *= 2.0D;
      }
      toHeal *= resistance;
      
      VolaTile t = Zones.getTileOrNull(target.getCreature().getTileX(), target
        .getCreature().getTileY(), target.getCreature().isOnSurface());
      if (t != null) {
        t.sendAttachCreatureEffect(target.getCreature(), (byte)11, (byte)0, (byte)0, (byte)0, (byte)0);
      }
      if (target.getSeverity() <= toHeal)
      {
        SpellResist.addSpellResistance(tCret, getNumber(), target.getSeverity());
        target.heal();
        
        performer.getCommunicator().sendNormalServerMessage("You heal the wound.", (byte)2);
        if (performer != tCret) {
          tCret.getCommunicator().sendNormalServerMessage(performer.getName() + " completely heals your wound.", (byte)2);
        }
      }
      else
      {
        SpellResist.addSpellResistance(tCret, getNumber(), toHeal);
        target.modifySeverity((int)-toHeal);
        
        performer.getCommunicator().sendNormalServerMessage("You cure the wound a bit.", (byte)2);
        if (performer != tCret) {
          tCret.getCommunicator().sendNormalServerMessage(performer.getName() + " partially heals your wound.", (byte)2);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\CureSerious.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */