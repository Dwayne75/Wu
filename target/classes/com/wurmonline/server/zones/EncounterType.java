package com.wurmonline.server.zones;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Logger;

public final class EncounterType
{
  private static final Logger logger;
  private final byte tiletype;
  public static final byte ELEVATION_GROUND = 0;
  public static final byte ELEVATION_WATER = 1;
  public static final byte ELEVATION_DEEP_WATER = 2;
  public static final byte ELEVATION_FLYING = 3;
  public static final byte ELEVATION_FLYING_HIGH = 4;
  public static final byte ELEVATION_BEACH = 5;
  public static final byte ELEVATION_CAVES = -1;
  public static final Encounter NULL_ENCOUNTER;
  private final byte elev;
  private final LinkedList<Integer> chances = new LinkedList();
  private final LinkedList<Encounter> encounters = new LinkedList();
  private int sumchance = 0;
  
  static
  {
    logger = Logger.getLogger(EncounterType.class.getName());
    
    NULL_ENCOUNTER = new Encounter();
    NULL_ENCOUNTER.addType(-10, 0);
  }
  
  public EncounterType(byte aTiletype, byte aElevation)
  {
    this.tiletype = aTiletype;
    this.elev = aElevation;
  }
  
  public void addEncounter(Encounter enc, int chance)
  {
    this.chances.addLast(Integer.valueOf(chance + this.sumchance));
    this.encounters.addLast(enc);
    this.sumchance += chance;
  }
  
  public Encounter getRandomEncounter(Creature loggerCret)
  {
    assert (this.sumchance > 0) : ("sumchance was 0, which means that no Encounters have been added to this EncounterType - " + this);
    
    loggerCret.getCommunicator().sendNormalServerMessage("Sumchance=" + this.sumchance + " for elevation " + this.elev);
    if (this.sumchance > 0)
    {
      int rand = Server.rand.nextInt(this.sumchance) + 1;
      loggerCret.getCommunicator().sendNormalServerMessage("Rand=" + rand);
      for (int x = 0; x < this.chances.size(); x++)
      {
        Integer ii = (Integer)this.chances.get(x);
        loggerCret.getCommunicator().sendNormalServerMessage("Chance integer=" + ii + " for " + 
          ((Encounter)this.encounters.get(x)).getTypes());
        if (rand <= ii.intValue())
        {
          loggerCret.getCommunicator().sendNormalServerMessage("Returning " + x);
          
          return (Encounter)this.encounters.get(x);
        }
      }
    }
    else
    {
      logger.warning("sumchance was 0, which means that no Encounters have been added to this EncounterType - " + this);
    }
    return null;
  }
  
  Encounter getRandomEncounter()
  {
    assert (this.sumchance > 0) : ("sumchance was 0, which means that no Encounters have been added to this EncounterType - " + this);
    if (this.sumchance > 0)
    {
      int rand = Server.rand.nextInt(this.sumchance) + 1;
      for (int x = 0; x < this.chances.size(); x++)
      {
        Integer ii = (Integer)this.chances.get(x);
        if (rand <= ii.intValue()) {
          return (Encounter)this.encounters.get(x);
        }
      }
    }
    else
    {
      logger.warning("sumchance was 0, which means that no Encounters have been added to this EncounterType - " + this);
    }
    return null;
  }
  
  public byte getTiletype()
  {
    return this.tiletype;
  }
  
  public byte getElev()
  {
    return this.elev;
  }
  
  public int getNumberOfEncounters()
  {
    return this.encounters.size();
  }
  
  public int getSumchance()
  {
    return this.sumchance;
  }
  
  public String toString()
  {
    return "EncounterType [tiletype=" + this.tiletype + ", elev=" + this.elev + ", encounters=" + getNumberOfEncounters() + ", sumchance=" + this.sumchance + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\EncounterType.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */