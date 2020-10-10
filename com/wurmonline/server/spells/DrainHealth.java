package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public final class DrainHealth
  extends DamageSpell
{
  public static final int RANGE = 50;
  public static final double BASE_DAMAGE = 2000.0D;
  public static final double DAMAGE_PER_POWER = 20.0D;
  
  DrainHealth()
  {
    super("Drain Health", 255, 3, 15, 20, 19, 30000L);
    this.targetCreature = true;
    this.offensive = true;
    this.healing = true;
    this.description = "damages a creature internally and heals you";
    this.type = 2;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    double castDamage = power * 20.0D;
    castDamage += 2000.0D;
    
    double toHeal = castDamage * 2.0D;
    double healingResistance = SpellResist.getSpellResistance(performer, getNumber());
    toHeal *= healingResistance;
    if (toHeal > 1.0D)
    {
      if (performer.getBody().getWounds() != null)
      {
        Wound[] wounds = performer.getBody().getWounds().getWounds();
        if (wounds.length > 0)
        {
          if (wounds[0].getSeverity() < toHeal)
          {
            SpellResist.addSpellResistance(performer, getNumber(), wounds[0].getSeverity());
            wounds[0].heal();
          }
          else
          {
            SpellResist.addSpellResistance(performer, getNumber(), toHeal);
            wounds[0].modifySeverity((int)-toHeal);
          }
        }
        else {
          performer.getStatus().modifyWounds(-(int)(0.5D * toHeal));
        }
      }
      else
      {
        performer.getStatus().modifyWounds(-(int)(0.5D * toHeal));
      }
      byte pos = 1;
      try
      {
        pos = target.getBody().getRandomWoundPos();
      }
      catch (Exception ex)
      {
        pos = 1;
      }
      double toDamage = calculateDamage(target, power, 2000.0D, 20.0D);
      
      target.addWoundOfType(performer, (byte)9, pos, false, 1.0F, false, toDamage, 0.0F, 0.0F, false, true);
      
      performer.getCommunicator().sendNormalServerMessage("You gain some health from " + target.getNameWithGenus() + ".", (byte)4);
      
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " drains you on health.", (byte)4);
      
      VolaTile targetVolaTile = Zones.getTileOrNull(target.getTileX(), target
        .getTileY(), target.isOnSurface());
      if (targetVolaTile != null) {
        targetVolaTile.sendAttachCreatureEffect(target, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0);
      }
      VolaTile performerVolaTile = Zones.getTileOrNull(performer.getTileX(), performer
        .getTileY(), performer.isOnSurface());
      if (performerVolaTile != null) {
        performerVolaTile.sendAttachCreatureEffect(performer, (byte)9, (byte)0, (byte)0, (byte)0, (byte)0);
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You try to drain some health from " + target
        .getNameWithGenus() + " but fail.", (byte)4);
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to drain you on health but fails.", (byte)4);
    }
  }
  
  void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    performer.getCommunicator().sendNormalServerMessage("You try to drain some health from " + target
      .getNameWithGenus() + " but fail.", (byte)4);
    target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to drain you on health but fails.", (byte)4);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\DrainHealth.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */