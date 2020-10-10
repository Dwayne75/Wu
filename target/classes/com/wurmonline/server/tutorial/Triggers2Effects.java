package com.wurmonline.server.tutorial;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Triggers2Effects
{
  private static Logger logger = Logger.getLogger(Triggers2Effects.class.getName());
  private static final String LOAD_ALL_LINKS = "SELECT * FROM TRIGGERS2EFFECTS";
  private static final String CREATE_LINK = "INSERT INTO TRIGGERS2EFFECTS (TRIGGERID, EFFECTID) VALUES(?,?)";
  private static final String DELETE_LINK = "DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=? AND EFFECTID=?";
  private static final String DELETE_TRIGGER = "DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=?";
  private static final String DELETE_EFFECT = "DELETE FROM TRIGGERS2EFFECTS WHERE EFFECTID=?";
  private static final Map<Integer, HashSet<Integer>> triggers2Effects = new ConcurrentHashMap();
  private static final Map<Integer, HashSet<Integer>> effects2Triggers = new ConcurrentHashMap();
  
  static
  {
    try
    {
      dbLoadAllTriggers2Effects();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Problems loading all Triggers 2 Effects", ex);
    }
  }
  
  public static TriggerEffect[] getEffectsForTrigger(int triggerId, boolean incInactive)
  {
    Set<TriggerEffect> effs = new HashSet();
    HashSet<Integer> effects = (HashSet)triggers2Effects.get(Integer.valueOf(triggerId));
    if (effects != null) {
      for (Integer effectId : effects)
      {
        TriggerEffect eff = TriggerEffects.getTriggerEffect(effectId.intValue());
        if (eff != null) {
          if ((incInactive) || ((!incInactive) && (!eff.isInactive()))) {
            effs.add(eff);
          }
        }
      }
    }
    return (TriggerEffect[])effs.toArray(new TriggerEffect[effs.size()]);
  }
  
  public static MissionTrigger[] getTriggersForEffect(int effectId, boolean incInactive)
  {
    Set<MissionTrigger> trgs = new HashSet();
    HashSet<Integer> triggers = (HashSet)effects2Triggers.get(Integer.valueOf(effectId));
    if (triggers != null) {
      for (Integer triggerId : triggers)
      {
        MissionTrigger trg = MissionTriggers.getTriggerWithId(triggerId.intValue());
        if (trg != null) {
          if ((incInactive) || ((!incInactive) && (!trg.isInactive()))) {
            trgs.add(trg);
          }
        }
      }
    }
    return (MissionTrigger[])trgs.toArray(new MissionTrigger[trgs.size()]);
  }
  
  public static boolean hasLink(int triggerId, int effectId)
  {
    HashSet<Integer> effects = (HashSet)triggers2Effects.get(Integer.valueOf(triggerId));
    if (effects != null) {
      return effects.contains(Integer.valueOf(effectId));
    }
    return false;
  }
  
  public static boolean hasEffect(int triggerId)
  {
    HashSet<Integer> effects = (HashSet)triggers2Effects.get(Integer.valueOf(triggerId));
    if (effects != null) {
      return !effects.isEmpty();
    }
    return false;
  }
  
  public static boolean hasTrigger(int effectId)
  {
    HashSet<Integer> triggers = (HashSet)effects2Triggers.get(Integer.valueOf(effectId));
    if (triggers != null) {
      return !triggers.isEmpty();
    }
    return false;
  }
  
  public static void addLink(int triggerId, int effectId, boolean loading)
  {
    if ((triggerId <= 0) || (effectId <= 0)) {
      return;
    }
    HashSet<Integer> effects = (HashSet)triggers2Effects.get(Integer.valueOf(triggerId));
    if (effects == null) {
      effects = new HashSet();
    }
    boolean effAdded = effects.add(Integer.valueOf(effectId));
    if (!effects.isEmpty()) {
      triggers2Effects.put(Integer.valueOf(triggerId), effects);
    }
    HashSet<Integer> triggers = (HashSet)effects2Triggers.get(Integer.valueOf(effectId));
    if (triggers == null) {
      triggers = new HashSet();
    }
    boolean trgAdded = triggers.add(Integer.valueOf(triggerId));
    if (!triggers.isEmpty()) {
      effects2Triggers.put(Integer.valueOf(effectId), triggers);
    }
    if (!loading) {
      if ((effAdded) || (trgAdded)) {
        dbCreateLink(triggerId, effectId);
      }
    }
  }
  
  public static void deleteLink(int triggerId, int effectId)
  {
    HashSet<Integer> effects = (HashSet)triggers2Effects.remove(Integer.valueOf(triggerId));
    if (effects != null)
    {
      effects.remove(Integer.valueOf(effectId));
      if (!effects.isEmpty()) {
        triggers2Effects.put(Integer.valueOf(triggerId), effects);
      }
    }
    HashSet<Integer> triggers = (HashSet)effects2Triggers.remove(Integer.valueOf(effectId));
    if (triggers != null)
    {
      triggers.remove(Integer.valueOf(triggerId));
      if (!triggers.isEmpty()) {
        effects2Triggers.put(Integer.valueOf(effectId), triggers);
      }
    }
    dbDeleteLink(triggerId, effectId);
  }
  
  public static void deleteTrigger(int triggerId)
  {
    HashSet<Integer> effects = (HashSet)triggers2Effects.remove(Integer.valueOf(triggerId));
    if (effects != null) {
      for (Integer effectId : effects)
      {
        HashSet<Integer> triggers = (HashSet)effects2Triggers.remove(effectId);
        if (triggers != null)
        {
          triggers.remove(Integer.valueOf(triggerId));
          if (!triggers.isEmpty()) {
            effects2Triggers.put(Integer.valueOf(effectId.intValue()), triggers);
          }
        }
      }
    }
    dbDeleteTrigger(triggerId);
  }
  
  public static void deleteEffect(int effectId)
  {
    HashSet<Integer> triggers = (HashSet)effects2Triggers.remove(Integer.valueOf(effectId));
    if (triggers != null) {
      for (Integer triggerId : triggers)
      {
        HashSet<Integer> effects = (HashSet)effects2Triggers.remove(triggerId);
        if (effects != null)
        {
          effects.remove(Integer.valueOf(effectId));
          if (!effects.isEmpty()) {
            effects2Triggers.put(Integer.valueOf(effectId), triggers);
          }
        }
      }
    }
    dbDeleteEffect(effectId);
  }
  
  private static void dbCreateLink(int triggerId, int effectId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("INSERT INTO TRIGGERS2EFFECTS (TRIGGERID, EFFECTID) VALUES(?,?)");
      ps.setInt(1, triggerId);
      ps.setInt(2, effectId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage());
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbDeleteLink(int triggerId, int effectId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=? AND EFFECTID=?");
      ps.setInt(1, triggerId);
      ps.setInt(2, effectId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage());
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbDeleteTrigger(int triggerId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=?");
      ps.setInt(1, triggerId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage());
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbDeleteEffect(int effectId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM TRIGGERS2EFFECTS WHERE EFFECTID=?");
      ps.setInt(1, effectId);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage());
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbLoadAllTriggers2Effects()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM TRIGGERS2EFFECTS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        int triggerId = rs.getInt("TRIGGERID");
        int effectId = rs.getInt("EFFECTID");
        addLink(triggerId, effectId, true);
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage());
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\Triggers2Effects.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */