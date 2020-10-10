package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.TempWound;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.Random;

public final class Smite
  extends ReligiousSpell
  implements AttitudeConstants
{
  public static final int RANGE = 12;
  
  Smite()
  {
    super("Smite", 252, 30, 50, 70, 70, 30000L);
    this.targetCreature = true;
    this.offensive = true;
    this.description = "damages the targets body with extreme fire damage depending on how healthy they are";
    this.type = 2;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2) && 
      (!performer.getDeity().isLibila())) {
      if (performer.faithful)
      {
        performer.getCommunicator().sendNormalServerMessage(performer
          .getDeity().getName() + " would never accept your smiting " + target.getName() + ".", (byte)3);
        
        return false;
      }
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    if (((target.isHuman()) || (target.isDominated())) && (target.getAttitude(performer) != 2) && 
      (!performer.getDeity().isLibila())) {
      performer.modifyFaith(-5.0F);
    }
    if (Server.rand.nextFloat() > target.addSpellResistance((short)252))
    {
      performer.getCommunicator().sendNormalServerMessage(target
        .getName() + " resists your attempt to smite " + target.getHimHerItString() + ".", (byte)3);
      
      target.getCommunicator().sendSafeServerMessage(performer.getName() + " tries to smite you but you resist.", (byte)4);
      
      return;
    }
    int damage = target.getStatus().damage;
    int minhealth = 65435;
    if (target.isUnique()) {
      minhealth = 15535;
    }
    double maxdam = Math.max(0, minhealth - damage);
    maxdam *= (0.5D + 0.5D * (power / 100.0D));
    float resistance = (float)SpellResist.getSpellResistance(target, getNumber());
    maxdam *= resistance;
    
    SpellResist.addSpellResistance(target, getNumber(), maxdam);
    
    maxdam = Spell.modifyDamage(target, maxdam);
    if (maxdam > 500.0D)
    {
      performer.getCommunicator().sendNormalServerMessage("You smite " + target.getName() + ".", (byte)2);
      target.getCommunicator().sendAlertServerMessage(performer.getName() + " smites you.", (byte)4);
      Wound wound = null;
      if ((target instanceof Player))
      {
        target.addWoundOfType(null, (byte)4, 0, false, 1.0F, true, maxdam, 0.0F, 0.0F, false, true);
      }
      else
      {
        wound = new TempWound((byte)4, (byte)0, (float)maxdam, target.getWurmId(), 0.0F, 0.0F, true);
        target.getBody().addWound(wound);
      }
      VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target
        .isOnSurface());
      if (t != null) {
        t.sendAttachCreatureEffect(target, (byte)10, (byte)0, (byte)0, (byte)0, (byte)0);
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You try to smite " + target
        .getName() + " but there seems to be no effect.", (byte)3);
      target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to smite you but to no avail.", (byte)4);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Smite.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */