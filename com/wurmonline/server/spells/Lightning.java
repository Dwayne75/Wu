package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Logger;

public class Lightning
  extends KarmaSpell
{
  private static final Logger logger = Logger.getLogger(Lightning.class.getName());
  public static final int RANGE = 24;
  
  public Lightning()
  {
    super("Lightning", 561, 20, 500, 20, 1, 180000L);
    this.targetCreature = true;
    this.targetTile = true;
    this.offensive = true;
    
    this.description = "creates and calls lightning down on your foes";
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
    if (!performer.isOnSurface())
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be outside to call lightning.", (byte)3);
      return false;
    }
    if (performer.getCurrentTile().getStructure() != null)
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be outside to call lightning.", (byte)3);
      return false;
    }
    if (target.isOnSurface())
    {
      if (Server.getInstance().hasThunderMode())
      {
        VolaTile t = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), true);
        if ((t.getStructure() == null) || (t.getStructure().isTypeBridge())) {
          return true;
        }
        performer.getCommunicator().sendNormalServerMessage(target
          .getName() + " is hiding in the structure.", (byte)3);
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("There is no lightning in the sky.", (byte)3);
      }
    }
    else {
      performer.getCommunicator().sendNormalServerMessage("You need to be outside to call lightning.", (byte)3);
    }
    return false;
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer)
  {
    if (!performer.isOnSurface())
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be outside to call lightning.", (byte)3);
      return false;
    }
    if (performer.getCurrentTile().getStructure() != null)
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be outside to call lightning.", (byte)3);
      return false;
    }
    if (layer < 0)
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be outside to call lightning.", (byte)3);
      return false;
    }
    VolaTile t = Zones.getOrCreateTile(tilex, tiley, true);
    if ((t.getStructure() == null) || (t.getStructure().isTypeBridge())) {
      return true;
    }
    performer.getCommunicator().sendNormalServerMessage("The structure is in the way.", (byte)3);
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    if (target.isOnSurface())
    {
      if (Server.getInstance().hasThunderMode())
      {
        VolaTile t = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), true);
        if ((t.getStructure() == null) || (t.getStructure().isTypeBridge()))
        {
          float damage = 5000.0F + 5000.0F * ((float)power / 100.0F);
          performer.getCommunicator().sendNormalServerMessage("You call down lightning on " + target.getName() + "!", (byte)2);
          
          target.getCommunicator().sendAlertServerMessage(performer.getName() + " calls down lightning on you!", (byte)4);
          
          Zones.flashSpell(target.getTileX(), target.getTileY(), damage, performer);
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage(target
            .getName() + " is hiding in the structure.", (byte)3);
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("There is no lightning in the sky.", (byte)3);
      }
    }
    else {
      performer.getCommunicator().sendNormalServerMessage("There is no lightning there.", (byte)3);
    }
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    if (performer.isOnSurface())
    {
      VolaTile t = Zones.getOrCreateTile(tilex, tiley, true);
      if ((t.getStructure() == null) || (t.getStructure().isTypeBridge()))
      {
        float damage = 5000.0F + 5000.0F * ((float)power / 100.0F);
        if ((performer.getPower() > 1) && (Servers.isThisATestServer())) {
          performer.getCommunicator().sendNormalServerMessage("Base damage: " + damage);
        }
        Server.getWeather().setRainAdd(40.0F);
        Server.getWeather().setCloudTarget(40.0F);
        performer.getCommunicator().sendNormalServerMessage("You call down lightning in the area!");
        t.broadCastAction(performer.getName() + " calls down lightning on you!", performer, true);
        Zones.flashSpell(tilex, tiley, damage, performer);
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("Contains a structure blocking your efforts.", (byte)3);
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("You need to be above ground to call lightning.", (byte)3);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Lightning.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */