package com.wurmonline.server.bodys;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Wounds
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(Wounds.class.getName());
  private Map<Long, Wound> wounds;
  private static final Map<Long, Wound> allWounds = new HashMap();
  public static final Wound[] emptyWounds = new Wound[0];
  
  public Wound[] getWounds()
  {
    Wound[] toReturn = null;
    if ((this.wounds == null) || (this.wounds.size() == 0)) {
      toReturn = emptyWounds;
    } else {
      toReturn = (Wound[])this.wounds.values().toArray(new Wound[this.wounds.size()]);
    }
    return toReturn;
  }
  
  boolean hasWounds()
  {
    return (this.wounds != null) && (!this.wounds.isEmpty());
  }
  
  void addWound(Wound wound)
  {
    if (this.wounds == null) {
      this.wounds = new HashMap();
    }
    this.wounds.put(new Long(wound.getWurmId()), wound);
    allWounds.put(new Long(wound.getWurmId()), wound);
    if (wound.getCreature() != null) {
      try
      {
        Item bodypart = wound.getCreature().getBody().getBodyPartForWound(wound);
        try
        {
          Creature[] watchers = bodypart.getWatchers();
          for (int x = 0; x < watchers.length; x++) {
            watchers[x].getCommunicator().sendAddWound(wound, bodypart);
          }
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}
      }
      catch (NoSpaceException nsp)
      {
        logger.log(Level.INFO, nsp.getMessage(), nsp);
      }
    }
  }
  
  public Wound getWound(long id)
  {
    Wound toReturn = null;
    if (this.wounds != null) {
      toReturn = (Wound)this.wounds.get(new Long(id));
    }
    return toReturn;
  }
  
  public Wound getWoundAtLocation(byte location)
  {
    if (this.wounds != null)
    {
      Wound[] w = getWounds();
      for (int x = 0; x < w.length; x++) {
        if (w[x].getLocation() == location) {
          return w[x];
        }
      }
    }
    return null;
  }
  
  public Wound getWoundTypeAtLocation(byte location, byte type)
  {
    if (this.wounds != null)
    {
      Wound[] w = getWounds();
      for (int x = 0; x < w.length; x++) {
        if ((w[x].getLocation() == location) && (w[x].getType() == type)) {
          return w[x];
        }
      }
    }
    return null;
  }
  
  public static Wound getAnyWound(long id)
  {
    return (Wound)allWounds.get(new Long(id));
  }
  
  void remove(Wound wound)
  {
    if (this.wounds != null)
    {
      wound.removeAllModifiers();
      this.wounds.remove(new Long(wound.getWurmId()));
      allWounds.remove(new Long(wound.getWurmId()));
      wound.delete();
      if (this.wounds.size() == 0) {
        this.wounds = null;
      }
      if (wound.getCreature() != null)
      {
        try
        {
          Item bodypart = wound.getCreature().getBody().getBodyPartForWound(wound);
          Creature[] watchers = bodypart.getWatchers();
          for (int x = 0; x < watchers.length; x++) {
            watchers[x].getCommunicator().sendRemoveWound(wound);
          }
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSpaceException nsp)
        {
          logger.log(Level.INFO, nsp.getMessage(), nsp);
        }
        wound.removeCreature();
      }
    }
  }
  
  void poll(Creature holder)
  {
    if (this.wounds != null)
    {
      boolean woundPrevention = (holder != null) && (holder.hasFingerOfFoBonus());
      Wound[] w = getWounds();
      for (int x = 0; x < w.length; x++) {
        try
        {
          w[x].poll(woundPrevention);
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
    }
  }
  
  static final int getModifiedSkill(int woundPos)
  {
    return getModifiedSkill(woundPos, (byte)0);
  }
  
  static final int getModifiedSkill(int woundPos, byte woundType)
  {
    if (woundPos == 1)
    {
      if (woundType == 9) {
        return 10067;
      }
      return 100;
    }
    if (woundPos == 21) {
      return 10073;
    }
    if ((woundPos == 13) || (woundPos == 14)) {
      return 10056;
    }
    if ((woundPos == 9) || (woundPos == 10)) {
      return 1030;
    }
    if (woundPos == 3) {
      return 1002;
    }
    if ((woundPos == 22) || (woundPos == 24)) {
      return 102;
    }
    if (woundPos == 25) {
      return 104;
    }
    if (woundPos == 29) {
      return 101;
    }
    if ((woundPos == 33) || (woundPos == 17)) {
      return 10067;
    }
    return -1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\bodys\Wounds.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */