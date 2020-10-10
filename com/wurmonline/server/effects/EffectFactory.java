package com.wurmonline.server.effects;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Players;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.EffectConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EffectFactory
  implements EffectConstants
{
  private final Map<Integer, Effect> effects;
  private static EffectFactory instance;
  private static final Logger logger = Logger.getLogger(EffectFactory.class.getName());
  private static final String GETEFFECTS = "SELECT * FROM EFFECTS";
  
  private EffectFactory()
  {
    this.effects = new HashMap();
  }
  
  public static EffectFactory getInstance()
  {
    if (instance == null) {
      instance = new EffectFactory();
    }
    return instance;
  }
  
  private void addEffect(Effect effect)
  {
    addEffect(effect, false);
  }
  
  private void addEffect(Effect effect, boolean temp)
  {
    if (!temp) {
      this.effects.put(Integer.valueOf(effect.getId()), effect);
    }
    int tileX = (int)effect.getPosX() >> 2;
    int tileY = (int)effect.getPosY() >> 2;
    if (effect.isGlobal()) {
      Players.getInstance().sendGlobalNonPersistantEffect(effect.getOwner(), effect.getType(), tileX, tileY, effect
        .getPosZ());
    } else {
      try
      {
        Zone zone = Zones.getZone(tileX, tileY, effect.isOnSurface());
        zone.addEffect(effect, temp);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, nsz.getMessage(), nsz);
      }
    }
  }
  
  public Effect createFire(long id, float posX, float posY, float posZ, boolean surfaced)
  {
    Effect toReturn = new DbEffect(id, (short)0, posX, posY, posZ, surfaced);
    addEffect(toReturn);
    return toReturn;
  }
  
  public Effect createProjectileLanding(float posX, float posY, float posZ, boolean surfaced)
  {
    Effect toReturn = new TempEffect(-1L, (short)26, posX, posY, posZ, surfaced);
    addEffect(toReturn, true);
    return toReturn;
  }
  
  public Effect createGenericEffect(long id, String effectName, float posX, float posY, float posZ, boolean surfaced, float timeout, float rotationOffset)
  {
    Effect toReturn = new TempEffect(id, (short)27, posX, posY, posZ, surfaced);
    toReturn.setEffectString(effectName);
    toReturn.setTimeout(timeout);
    toReturn.setRotationOffset(rotationOffset);
    addEffect(toReturn, id == -1L);
    return toReturn;
  }
  
  public Effect createGenericTempEffect(String effectName, float posX, float posY, float posZ, boolean surfaced, float timeout, float rotationOffset)
  {
    return createGenericEffect(-1L, effectName, posX, posY, posZ, surfaced, timeout, rotationOffset);
  }
  
  public Effect createSpawnEff(long id, float posX, float posY, float posZ, boolean surfaced)
  {
    Effect toReturn = new DbEffect(id, (short)19, posX, posY, posZ, surfaced);
    addEffect(toReturn);
    return toReturn;
  }
  
  public Effect createChristmasEff(long id, float posX, float posY, float posZ, boolean surfaced)
  {
    Effect toReturn = new DbEffect(id, (short)4, posX, posY, posZ, surfaced);
    addEffect(toReturn);
    return toReturn;
  }
  
  public final void deleteEffByOwner(long id)
  {
    for (Effect eff : getAllEffects()) {
      if (eff.getOwner() == id)
      {
        deleteEffect(eff.getId());
        break;
      }
    }
  }
  
  public final Effect[] getAllEffects()
  {
    return (Effect[])this.effects.values().toArray(new Effect[this.effects.size()]);
  }
  
  public Effect deleteEffect(int id)
  {
    Effect toRemove = (Effect)this.effects.get(Integer.valueOf(id));
    this.effects.remove(Integer.valueOf(id));
    if (toRemove != null)
    {
      if (toRemove.isGlobal())
      {
        Players.getInstance().removeGlobalEffect(toRemove.getOwner());
      }
      else
      {
        int tileX = (int)toRemove.getPosX() >> 2;
        int tileY = (int)toRemove.getPosY() >> 2;
        try
        {
          Zone zone = Zones.getZone(tileX, tileY, toRemove.isOnSurface());
          zone.removeEffect(toRemove);
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.WARNING, nsz.getMessage(), nsz);
        }
      }
      toRemove.delete();
    }
    return toRemove;
  }
  
  public void getEffectsFor(Item item)
  {
    for (Effect effect : this.effects.values()) {
      if (effect.getOwner() == item.getWurmId())
      {
        effect.setPosX(item.getPosX());
        effect.setPosY(item.getPosY());
        effect.setSurfaced(item.isOnSurface());
        item.addEffect(effect);
        int tileX = (int)effect.getPosX() >> 2;
        int tileY = (int)effect.getPosY() >> 2;
        try
        {
          Zone zone = Zones.getZone(tileX, tileY, effect.isOnSurface());
          zone.addEffect(effect, false);
        }
        catch (NoSuchZoneException nsz)
        {
          logger.log(Level.WARNING, nsz.getMessage(), nsz);
        }
      }
    }
  }
  
  public Effect getEffectForOwner(long id)
  {
    for (Effect eff : getAllEffects()) {
      if (eff.getOwner() == id) {
        return eff;
      }
    }
    return null;
  }
  
  public void loadEffects()
    throws IOException
  {
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM EFFECTS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        float posX = rs.getFloat("POSX");
        float posY = rs.getFloat("POSY");
        float posZ = rs.getFloat("POSZ");
        short type = rs.getShort("TYPE");
        long owner = rs.getLong("OWNER");
        long startTime = rs.getLong("STARTTIME");
        int id = rs.getInt("ID");
        DbEffect effect = new DbEffect(id, owner, type, posX, posY, posZ, startTime);
        this.effects.put(Integer.valueOf(id), effect);
      }
    }
    catch (SQLException sqx)
    {
      long end;
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long end = System.nanoTime();
      logger.info("Loaded " + this.effects.size() + " effects from database took " + (float)(end - start) / 1000000.0F + " ms");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\effects\EffectFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */