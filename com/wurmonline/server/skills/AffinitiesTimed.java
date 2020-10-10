package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class AffinitiesTimed
{
  private static final Logger logger = Logger.getLogger(AffinitiesTimed.class.getName());
  private static final Map<Long, AffinitiesTimed> playerTimedAffinities = new ConcurrentHashMap();
  private static final String GET_ALL_PLAYER_TIMED_AFFINITIES = "SELECT * FROM AFFINITIESTIMED";
  private static final String CREATE_PLAYER_TIMED_AFFINITY = "INSERT INTO AFFINITIESTIMED (PLAYERID,SKILL,EXPIRATION) VALUES (?,?,?)";
  private static final String UPDATE_PLAYER_TIMED_AFFINITY = "UPDATE AFFINITIESTIMED SET EXPIRATION=? WHERE PLAYERID=? AND SKILL=?";
  private static final String DELETE_PLAYER_TIMED_AFFINITIES = "DELETE FROM AFFINITIESTIMED WHERE PLAYERID=?";
  private static final String DELETE_PLAYER_SKILL_TIMED_AFFINITIES = "DELETE FROM AFFINITIESTIMED WHERE PLAYERID=? AND Skill=?";
  private final long wurmId;
  private final Map<Integer, Long> timedAffinities = new ConcurrentHashMap();
  private final Map<Integer, Integer> updateAffinities = new ConcurrentHashMap();
  private int lastSkillId = -1;
  private long lastTime = -1L;
  
  public AffinitiesTimed(long playerId)
  {
    this.wurmId = playerId;
  }
  
  public long getPlayerId()
  {
    return this.wurmId;
  }
  
  int getLastSkillId()
  {
    return this.lastSkillId;
  }
  
  long getLastTime()
  {
    return this.lastTime;
  }
  
  private void put(int skill, long expires)
  {
    this.timedAffinities.put(Integer.valueOf(skill), Long.valueOf(expires));
  }
  
  @Nullable
  public Long getExpires(int skill)
  {
    return (Long)this.timedAffinities.get(Integer.valueOf(skill));
  }
  
  public boolean add(int skill, long duration)
  {
    boolean toReturn = false;
    Long expires = getExpires(skill);
    long newExpires = 0L;
    if (expires == null)
    {
      newExpires = WurmCalendar.getCurrentTime() + duration * 10L;
      toReturn = true;
      this.updateAffinities.put(Integer.valueOf(skill), Integer.valueOf(skill));
    }
    else
    {
      newExpires = expires.longValue() + duration;
      this.updateAffinities.put(Integer.valueOf(skill), Integer.valueOf(skill));
    }
    this.timedAffinities.put(Integer.valueOf(skill), Long.valueOf(newExpires));
    this.lastSkillId = skill;
    this.lastTime = WurmCalendar.getCurrentTime();
    return toReturn;
  }
  
  public void remove(int skill)
  {
    dbRemoveTimedAffinity(this.wurmId, skill);
    this.timedAffinities.remove(Integer.valueOf(skill));
  }
  
  private void pollTimeAffinities(Creature creature)
  {
    for (Map.Entry<Integer, Long> entry : this.timedAffinities.entrySet())
    {
      int skillId = ((Integer)entry.getKey()).intValue();
      long expires = ((Long)entry.getValue()).longValue();
      if (expires < WurmCalendar.getCurrentTime()) {
        sendRemoveTimedAffinity(creature, skillId);
      }
    }
    for (Integer skill : this.updateAffinities.values())
    {
      int skillId = skill.intValue();
      Long expires = (Long)this.timedAffinities.get(skill);
      if (expires == null)
      {
        this.updateAffinities.remove(skill);
      }
      else if (skillId != this.lastSkillId)
      {
        this.updateAffinities.remove(skill);
        
        dbSaveTimedAffinity(this.wurmId, skillId, expires.longValue(), true);
      }
      else if (WurmCalendar.getCurrentTime() > this.lastTime + 50L)
      {
        this.lastSkillId = -1;
        
        this.updateAffinities.remove(skill);
        dbSaveTimedAffinity(this.wurmId, skillId, expires.longValue(), true);
      }
    }
  }
  
  private boolean isEmpty()
  {
    return this.timedAffinities.isEmpty();
  }
  
  public void sendTimedAffinities(Creature creature)
  {
    for (Map.Entry<Integer, Long> entry : this.timedAffinities.entrySet()) {
      if (((Long)entry.getValue()).longValue() > WurmCalendar.getCurrentTime()) {
        sendTimedAffinity(creature, ((Integer)entry.getKey()).intValue());
      }
    }
  }
  
  public void sendTimedAffinity(Creature creature, int skillNum)
  {
    long id = makeId(skillNum);
    
    Long expires = getExpires(skillNum);
    if (expires != null)
    {
      int dur = (int)((float)(expires.longValue() - WurmCalendar.getCurrentTime()) / 8.0F);
      if (dur > 0) {
        creature.getCommunicator().sendAddStatusEffect(id, SpellEffectsEnum.SKILL_TIMED_AFFINITY, dur, 
          SkillSystem.getNameFor(skillNum));
      }
    }
  }
  
  public void sendRemoveTimedAffinities(Creature creature)
  {
    for (Map.Entry<Integer, Long> entry : this.timedAffinities.entrySet()) {
      sendRemoveTimedAffinity(creature, ((Integer)entry.getKey()).intValue());
    }
  }
  
  public void sendRemoveTimedAffinity(Creature creature, int skillNum)
  {
    creature.getCommunicator().sendRemoveFromStatusEffectBar(makeId(skillNum));
    remove(skillNum);
  }
  
  private long makeId(int skillNum)
  {
    long sid = BigInteger.valueOf(skillNum).shiftLeft(32).longValue() + 18L;
    return SpellEffectsEnum.SKILL_TIMED_AFFINITY.createId(sid);
  }
  
  public static void poll(Creature creature)
  {
    AffinitiesTimed at = getTimedAffinitiesByPlayer(creature.getWurmId(), false);
    if (at != null) {
      at.pollTimeAffinities(creature);
    }
  }
  
  public static void sendTimedAffinitiesFor(Creature creature)
  {
    AffinitiesTimed at = getTimedAffinitiesByPlayer(creature.getWurmId(), false);
    if (at != null) {
      at.sendTimedAffinities(creature);
    }
  }
  
  public static SkillTemplate getTimedAffinitySkill(Creature creature, Item item)
  {
    if (!creature.isPlayer()) {
      return null;
    }
    long playerId = creature.getWurmId();
    int ibonus = item.getBonus();
    if (ibonus == -1) {
      return null;
    }
    if ((Server.getInstance().isPS()) || (creature.hasFlag(53)))
    {
      Random affinityRandom = new Random();
      affinityRandom.setSeed(creature.getWurmId());
      ibonus += affinityRandom.nextInt(SkillSystem.getNumberOfSkillTemplates());
      ibonus %= SkillSystem.getNumberOfSkillTemplates();
    }
    else
    {
      ibonus = (int)(ibonus + (playerId & 0xFF));
      ibonus = (int)(ibonus + (playerId >>> 8 & 0xFF));
      ibonus = (int)(ibonus + (playerId >>> 16 & 0xFF));
      ibonus = (int)(ibonus + (playerId >>> 24 & 0xFF));
      ibonus = (int)(ibonus + (playerId >>> 32 & 0xFF));
      ibonus = (int)(ibonus + (playerId >>> 40 & 0xFF));
      ibonus = (int)(ibonus + (playerId >>> 48 & 0xFF));
      ibonus = (int)(ibonus + (playerId >>> 56 & 0xFF));
      ibonus = (ibonus & 0xFF) % SkillSystem.getNumberOfSkillTemplates();
    }
    return SkillSystem.getSkillTemplateByIndex(ibonus);
  }
  
  public static void addTimedAffinityFromBonus(Creature creature, int weight, Item item)
  {
    if (!creature.isPlayer()) {
      return;
    }
    int ibonus = item.getBonus();
    if (ibonus == -1) {
      return;
    }
    long playerId = creature.getWurmId();
    
    SkillTemplate skillTemplate = getTimedAffinitySkill(creature, item);
    if (skillTemplate == null) {
      return;
    }
    int skillId = skillTemplate.getNumber();
    
    float rarityMod = 1.0F + item.getRarity() * item.getRarity() * 0.1F;
    int duration = (int)(weight * item.getCurrentQualityLevel() * rarityMod * item.getFoodComplexity());
    AffinitiesTimed at = getTimedAffinitiesByPlayer(playerId, true);
    
    boolean sendMessage = (at.getLastSkillId() != skillId) || (WurmCalendar.getCurrentTime() > at.getLastTime() + 50L);
    
    at.add(skillId, duration);
    if (sendMessage) {
      creature.getCommunicator().sendNormalServerMessage("You suddenly realise that you have more of an insight about " + skillTemplate
        .getName().toLowerCase() + "!", (byte)2);
    }
    at.sendTimedAffinity(creature, skillTemplate.getNumber());
  }
  
  public static boolean isTimedAffinity(long playerId, int skill)
  {
    AffinitiesTimed at = getTimedAffinitiesByPlayer(playerId, false);
    if (at != null)
    {
      Long expires = at.getExpires(skill);
      if (expires == null)
      {
        at.remove(skill);
      }
      else
      {
        if (expires.longValue() > WurmCalendar.getCurrentTime()) {
          return true;
        }
        at.remove(skill);
      }
    }
    return false;
  }
  
  @Nullable
  public static final AffinitiesTimed getTimedAffinitiesByPlayer(long playerId, boolean autoCreate)
  {
    AffinitiesTimed at = (AffinitiesTimed)playerTimedAffinities.get(Long.valueOf(playerId));
    if ((at == null) && (autoCreate))
    {
      at = new AffinitiesTimed(playerId);
      playerTimedAffinities.put(Long.valueOf(playerId), at);
    }
    return at;
  }
  
  public static void deleteTimedAffinitiesForPlayer(long playerId)
  {
    dbRemovePlayerTimedAffinities(playerId);
    playerTimedAffinities.remove(Long.valueOf(playerId));
  }
  
  public static void removeTimedAffinitiesForPlayer(Creature creature)
  {
    AffinitiesTimed at = getTimedAffinitiesByPlayer(creature.getWurmId(), false);
    if (at != null) {
      at.sendRemoveTimedAffinities(creature);
    }
  }
  
  public static final int loadAllPlayerTimedAffinities()
  {
    logger.info("Loading all Player Timed Affinities");
    long start = System.nanoTime();
    int count = 0;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM AFFINITIESTIMED");
      rs = ps.executeQuery();
      while (rs.next())
      {
        count++;
        long playerId = rs.getLong("PLAYERID");
        int skill = rs.getInt("SKILL");
        long expires = rs.getLong("EXPIRATION");
        
        AffinitiesTimed at = getTimedAffinitiesByPlayer(playerId, true);
        at.put(skill, expires);
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to load all player timed affinities: " + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    logger.log(Level.INFO, "Number of player timed affinities=" + count + ".");
    
    logger.log(Level.INFO, "Player timed affinities loaded. That took " + 
      (float)(System.nanoTime() - start) / 1000000.0F + " ms.");
    return count;
  }
  
  private static void dbSaveTimedAffinity(long playerId, int skill, long expires, boolean update)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      if (update)
      {
        ps = dbcon.prepareStatement("UPDATE AFFINITIESTIMED SET EXPIRATION=? WHERE PLAYERID=? AND SKILL=?");
        ps.setLong(1, expires);
        ps.setLong(2, playerId);
        ps.setInt(3, skill);
        int did = ps.executeUpdate();
        if (did > 0) {
          return;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
      }
      ps = dbcon.prepareStatement("INSERT INTO AFFINITIESTIMED (PLAYERID,SKILL,EXPIRATION) VALUES (?,?,?)");
      ps.setLong(1, playerId);
      ps.setInt(2, skill);
      ps.setLong(3, expires);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to save player (" + playerId + ") skill (" + skill + ") timed affinities: " + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbRemovePlayerTimedAffinities(long playerId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM AFFINITIESTIMED WHERE PLAYERID=?");
      ps.setLong(1, playerId);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to remove player (" + playerId + ") timed affiniies: " + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private static void dbRemoveTimedAffinity(long playerId, int skill)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM AFFINITIESTIMED WHERE PLAYERID=? AND Skill=?");
      ps.setLong(1, playerId);
      ps.setInt(2, skill);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to remove player (" + playerId + ")  skill (" + skill + ") timed affinity: " + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\skills\AffinitiesTimed.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */