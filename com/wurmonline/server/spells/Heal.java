package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
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

public final class Heal
  extends ReligiousSpell
{
  private static Logger logger = Logger.getLogger(Heal.class.getName());
  public static final int RANGE = 12;
  
  Heal()
  {
    super("Heal", 249, 30, 40, 30, 40, 10000L);
    this.targetCreature = true;
    this.healing = true;
    this.description = "heals an extreme amount of damage";
    this.type = 0;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if ((target.getBody() == null) || (target.getBody().getWounds() == null))
    {
      performer.getCommunicator().sendNormalServerMessage(target
        .getNameWithGenus() + " has no wounds to heal.", (byte)3);
      
      return false;
    }
    if (target.isReborn()) {
      return true;
    }
    if (!target.equals(performer))
    {
      if ((target.isPlayer()) && (performer.getDeity() != null))
      {
        if (!target.isFriendlyKingdom(performer.getKingdomId()))
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
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    boolean doeff = true;
    if (target.isReborn())
    {
      doeff = false;
      performer.getCommunicator().sendNormalServerMessage("You slay " + target.getNameWithGenus() + ".", (byte)4);
      
      Server.getInstance().broadCastAction(performer.getName() + " slays " + target.getNameWithGenus() + "!", performer, 5);
      target.addAttacker(performer);
      target.die(false, "Heal cast on Reborn");
    }
    else if (!target.equals(performer))
    {
      if ((target.isPlayer()) && (performer.getDeity() != null)) {
        if (!target.isFriendlyKingdom(performer.getKingdomId()))
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
      Wounds tWounds = target.getBody().getWounds();
      if (tWounds == null)
      {
        performer.getCommunicator().sendNormalServerMessage(target
          .getName() + " has no wounds to heal.", (byte)3);
        
        return;
      }
      double resistance = SpellResist.getSpellResistance(target, getNumber());
      double healingPool = Math.max(20.0D, power) / 100.0D * 65535.0D * 2.0D;
      if ((performer.getCultist() != null) && (performer.getCultist().healsFaster())) {
        healingPool *= 2.0D;
      }
      healingPool *= resistance;
      for (Wound w : tWounds.getWounds()) {
        if (w.getSeverity() <= healingPool)
        {
          healingPool -= w.getSeverity();
          SpellResist.addSpellResistance(target, getNumber(), w.getSeverity());
          w.heal();
        }
      }
      if ((tWounds.getWounds().length > 0) && (healingPool > 0.0D))
      {
        SpellResist.addSpellResistance(target, getNumber(), healingPool);
        tWounds.getWounds()[Server.rand.nextInt(tWounds.getWounds().length)].modifySeverity((int)-healingPool);
      }
      if (tWounds.getWounds().length > 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You heal some of " + target.getNameWithGenus() + "'s wounds.", (byte)4);
        
        target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " heals some of your wounds.", (byte)4);
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("You fully heal " + target.getNameWithGenus() + ".", (byte)4);
        
        target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " heals your wounds.", (byte)4);
      }
      VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target
        .isOnSurface());
      if (t != null) {
        t.sendAttachCreatureEffect(target, (byte)11, (byte)0, (byte)0, (byte)0, (byte)0);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Heal.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */