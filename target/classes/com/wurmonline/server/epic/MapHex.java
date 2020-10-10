package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WCValreiMapUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapHex
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(MapHex.class.getName());
  private final int id;
  private final int type;
  private final String name;
  private final float moveCost;
  private String presenceStringOne = " is in ";
  private String prepositionString = " in ";
  private String leavesStringOne = " leaves ";
  private static final Random rand = new Random();
  private final LinkedList<Integer> nearHexes = new LinkedList();
  private final LinkedList<EpicEntity> entities = new LinkedList();
  private final Set<EpicEntity> visitedBy = new HashSet();
  private long spawnEntityId = 0L;
  private long homeEntityId = 0L;
  public static final int TYPE_STANDARD = 0;
  public static final int TYPE_TRAP = 1;
  public static final int TYPE_SLOW = 2;
  public static final int TYPE_ENHANCE_STRENGTH = 3;
  public static final int TYPE_ENHANCE_VITALITY = 4;
  public static final int TYPE_TELEPORT = 5;
  private final HexMap myMap;
  private static final String addVisitedBy = "INSERT INTO VISITED(ENTITYID,HEXID) VALUES (?,?)";
  private static final String clearVisitedHex = "DELETE FROM VISITED WHERE HEXID=?";
  
  MapHex(HexMap map, int hexNumber, String hexName, float hexMoveCost, int hexType)
  {
    this.id = hexNumber;
    this.name = hexName;
    this.moveCost = Math.max(0.5F, hexMoveCost);
    this.type = hexType;
    this.myMap = map;
    map.addMapHex(this);
  }
  
  public final int getId()
  {
    return this.id;
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  final String getEnemyStatus(EpicEntity entity)
  {
    StringBuilder build = new StringBuilder();
    if ((entity.isCollectable()) || (entity.isSource())) {
      return "";
    }
    for (EpicEntity e : this.entities) {
      if (e != entity) {
        if ((!e.isCollectable()) && (!e.isSource()))
        {
          if (e.isWurm())
          {
            if (build.length() > 0) {
              build.append(' ');
            }
            build.append(entity.getName() + " is battling the Wurm.");
          }
          else if (e.isSentinelMonster())
          {
            if (build.length() > 0) {
              build.append(' ');
            }
            build.append(entity.getName() + " is trying to defeat the " + e.getName() + ".");
          }
          else if (e.isEnemy(entity))
          {
            if (build.length() > 0) {
              build.append(' ');
            }
            build.append(entity.getName() + " is fighting " + e.getName() + ".");
          }
          else if (entity.getCompanion() == e)
          {
            if (build.length() > 0) {
              build.append(' ');
            }
            build.append(entity.getName() + " is meeting with " + e.getName() + ".");
          }
          if (e.isAlly())
          {
            if (build.length() > 0) {
              build.append(' ');
            }
            build.append(entity.getName() + " visits the " + e.getName() + ".");
          }
        }
      }
    }
    return build.toString();
  }
  
  long getSpawnEntityId()
  {
    return this.spawnEntityId;
  }
  
  long getHomeEntityId()
  {
    return this.homeEntityId;
  }
  
  final String getOwnPresenceString()
  {
    return " is home" + getFullPrepositionString();
  }
  
  final String getFullPresenceString()
  {
    return getPresenceStringOne() + this.name + ".";
  }
  
  final String getFullPrepositionString()
  {
    return getPrepositionString() + this.name + ".";
  }
  
  final float getMoveCost()
  {
    return this.moveCost;
  }
  
  HexMap getMyMap()
  {
    return this.myMap;
  }
  
  final void setPresenceStringOne(String ps)
  {
    this.presenceStringOne = ps;
  }
  
  final String getPresenceStringOne()
  {
    return this.presenceStringOne;
  }
  
  final void setPrepositionString(String ps)
  {
    this.prepositionString = ps;
  }
  
  final String getPrepositionString()
  {
    return this.prepositionString;
  }
  
  final void setLeavesStringOne(String ps)
  {
    this.leavesStringOne = ps;
  }
  
  final String getLeavesStringOne()
  {
    return this.leavesStringOne;
  }
  
  final int getType()
  {
    return this.type;
  }
  
  final void addEntity(EpicEntity entity)
  {
    if (!this.entities.contains(entity))
    {
      this.entities.add(entity);
      entity.setMapHex(this);
      if ((entity.isWurm()) || (entity.isDeity()))
      {
        if (entity.getAttack() > entity.getInitialAttack()) {
          entity.setAttack(entity.getAttack() - 0.1F);
        }
        if (entity.getVitality() > entity.getInitialVitality()) {
          entity.setVitality(entity.getVitality() - 0.1F);
        } else if (entity.getVitality() < entity.getInitialVitality()) {
          entity.setVitality(entity.getVitality() + 0.1F);
        }
      }
      else if ((entity.isCollectable()) || (entity.isSource()))
      {
        clearVisitedBy();
      }
    }
  }
  
  final void removeEntity(EpicEntity entity, boolean load)
  {
    if (this.entities.contains(entity))
    {
      this.entities.remove(entity);
      entity.setMapHex(null);
    }
  }
  
  boolean checkLeaveStatus(EpicEntity entity)
  {
    return setEntityEffects(entity);
  }
  
  public final Integer[] getNearMapHexes()
  {
    return (Integer[])this.nearHexes.toArray(new Integer[this.nearHexes.size()]);
  }
  
  final void addNearHex(int hexId)
  {
    this.nearHexes.add(Integer.valueOf(hexId));
  }
  
  final void addNearHexes(int hexId1, int hexId2, int hexId3, int hexId4, int hexId5, int hexId6)
  {
    this.nearHexes.add(Integer.valueOf(hexId1));
    this.nearHexes.add(Integer.valueOf(hexId2));
    this.nearHexes.add(Integer.valueOf(hexId3));
    this.nearHexes.add(Integer.valueOf(hexId4));
    this.nearHexes.add(Integer.valueOf(hexId5));
    this.nearHexes.add(Integer.valueOf(hexId6));
  }
  
  final boolean isVisitedBy(EpicEntity entity)
  {
    for (EpicEntity ent : this.entities) {
      if ((ent.isCollectable()) || (ent.isSource())) {
        return false;
      }
    }
    if (this.visitedBy.contains(entity)) {
      return true;
    }
    return false;
  }
  
  final void addVisitedBy(EpicEntity entity, boolean load)
  {
    if ((this.visitedBy != null) && (!this.visitedBy.contains(entity)))
    {
      this.visitedBy.add(entity);
      if (!load)
      {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try
        {
          dbcon = DbConnector.getDeityDbCon();
          ps = dbcon.prepareStatement("INSERT INTO VISITED(ENTITYID,HEXID) VALUES (?,?)");
          ps.setLong(1, entity.getId());
          ps.setInt(2, getId());
          ps.executeUpdate();
        }
        catch (SQLException sqx)
        {
          logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
        finally
        {
          DbUtilities.closeDatabaseObjects(ps, null);
          DbConnector.returnConnection(dbcon);
        }
      }
    }
  }
  
  final void clearVisitedBy()
  {
    this.visitedBy.clear();
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("DELETE FROM VISITED WHERE HEXID=?");
      ps.setInt(1, getId());
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  LinkedList<Integer> cloneNearHexes()
  {
    LinkedList<Integer> clone = new LinkedList();
    for (Integer i : this.nearHexes) {
      clone.add(i);
    }
    return clone;
  }
  
  final boolean containsWurm()
  {
    for (EpicEntity e : this.entities) {
      if (e.isWurm()) {
        return true;
      }
    }
    return false;
  }
  
  final boolean containsEnemy(EpicEntity toCheck)
  {
    for (EpicEntity e : this.entities) {
      if (e.isEnemy(toCheck)) {
        return true;
      }
    }
    return false;
  }
  
  final boolean containsMonsterOrHelper()
  {
    for (EpicEntity e : this.entities) {
      if ((e.isSentinelMonster()) || (e.isAlly())) {
        return true;
      }
    }
    return false;
  }
  
  final boolean containsDeity()
  {
    for (EpicEntity e : this.entities) {
      if (e.isDeity()) {
        return true;
      }
    }
    return false;
  }
  
  boolean mayEnter(EpicEntity entity)
  {
    if ((entity.isWurm()) && (containsMonsterOrHelper())) {
      return containsDeity();
    }
    return true;
  }
  
  int getNextHexToWinPoint(EpicEntity entity)
  {
    if (entity.mustReturnHomeToWin())
    {
      MapHex home = this.myMap.getSpawnHex(entity);
      if (home != null) {
        if (home != this) {
          return findClosestHexTo(home.getId(), entity, true);
        }
      }
      return getId();
    }
    return findClosestHexTo(this.myMap.getHexNumRequiredToWin(), entity, true);
  }
  
  int findClosestHexTo(int target, EpicEntity entity, boolean avoidEnemies)
  {
    logger.log(Level.INFO, entity.getName() + " at " + getId() + " pathing to " + target);
    Map<Integer, Integer> steps = new HashMap();
    LinkedList<Integer> copy = cloneNearHexes();
    Set<Integer> checked;
    while (copy.size() > 0)
    {
      Integer i = (Integer)copy.remove(rand.nextInt(copy.size()));
      if (i.intValue() == target) {
        return target;
      }
      MapHex hex = this.myMap.getMapHex(i);
      if (hex.mayEnter(entity)) {
        if ((!avoidEnemies) || (!hex.containsEnemy(entity)))
        {
          checked = new HashSet();
          checked.add(i);
          int numSteps = findNextHex(checked, hex, target, entity, avoidEnemies, 0);
          steps.put(Integer.valueOf(hex.getId()), Integer.valueOf(numSteps));
        }
      }
    }
    int minSteps = 100;
    int hexNum = 0;
    for (Map.Entry<Integer, Integer> entry : steps.entrySet())
    {
      int csteps = ((Integer)entry.getValue()).intValue();
      if (csteps < minSteps)
      {
        minSteps = csteps;
        hexNum = ((Integer)entry.getKey()).intValue();
      }
    }
    return hexNum;
  }
  
  int findNextHex(Set<Integer> checked, MapHex startHex, int targetHexId, EpicEntity entity, boolean avoidEnemies, int counter)
  {
    LinkedList<Integer> nearClone = startHex.cloneNearHexes();
    int minNum = 100;
    while (nearClone.size() > 0)
    {
      Integer ni = (Integer)nearClone.remove(rand.nextInt(nearClone.size()));
      if (ni.intValue() == targetHexId) {
        return counter;
      }
      if (!checked.contains(ni))
      {
        checked.add(ni);
        if (counter < 6)
        {
          MapHex nearhex = this.myMap.getMapHex(ni);
          if (nearhex.mayEnter(entity)) {
            if ((!avoidEnemies) || (!nearhex.containsEnemy(entity)))
            {
              int steps = findNextHex(checked, nearhex, targetHexId, entity, avoidEnemies, ++counter);
              if (steps < minNum) {
                minNum = steps;
              }
            }
          }
        }
      }
    }
    return minNum;
  }
  
  int findNextHex(EpicEntity entity)
  {
    if (this.nearHexes.isEmpty())
    {
      logger.log(Level.WARNING, "Near hexes is empty for map " + getId());
      return 0;
    }
    if (entity.hasEnoughCollectablesToWin())
    {
      if (getId() == this.myMap.getHexNumRequiredToWin()) {
        return getId();
      }
      return getNextHexToWinPoint(entity);
    }
    LinkedList<Integer> copy = cloneNearHexes();
    while (copy.size() > 0)
    {
      Integer i = (Integer)copy.remove(rand.nextInt(copy.size()));
      MapHex hex = this.myMap.getMapHex(i);
      if (hex.mayEnter(entity))
      {
        if (entity.isWurm()) {
          return hex.getId();
        }
        if (!hex.isVisitedBy(entity)) {
          return hex.getId();
        }
      }
    }
    copy = cloneNearHexes();
    while (copy.size() > 0)
    {
      Integer i = (Integer)copy.remove(rand.nextInt(copy.size()));
      MapHex hex = this.myMap.getMapHex(i);
      if (hex.mayEnter(entity))
      {
        LinkedList<Integer> nearClone = hex.cloneNearHexes();
        while (nearClone.size() > 0)
        {
          Integer ni = (Integer)nearClone.remove(rand.nextInt(nearClone.size()));
          MapHex nearhex = this.myMap.getMapHex(ni);
          if (!nearhex.isVisitedBy(entity)) {
            return hex.getId();
          }
        }
      }
    }
    copy = cloneNearHexes();
    while (copy.size() > 0)
    {
      Integer i = (Integer)copy.remove(rand.nextInt(copy.size()));
      MapHex hex = this.myMap.getMapHex(i);
      if (hex.mayEnter(entity)) {
        return i.intValue();
      }
    }
    logger.log(Level.INFO, entity.getName() + " Failed to take random step to neighbour.");
    return 0;
  }
  
  public boolean isTrap()
  {
    return this.type == 1;
  }
  
  public boolean isTeleport()
  {
    return this.type == 5;
  }
  
  public boolean isSlow()
  {
    return this.type == 2;
  }
  
  int getSlowModifier()
  {
    return isSlow() ? 2 : 1;
  }
  
  private final boolean resolveDispute(EpicEntity entity)
  {
    EpicEntity enemy = null;
    for (EpicEntity e : this.entities) {
      if ((e != entity) && (e.isEnemy(entity))) {
        if (enemy == null) {
          enemy = e;
        } else if (Server.rand.nextBoolean()) {
          enemy = e;
        }
      }
    }
    if (enemy == null) {
      return true;
    }
    ValreiFight vFight = new ValreiFight(this, entity, enemy);
    ValreiFightHistory fightHistory = vFight.completeFight(false);
    ValreiFightHistoryManager.getInstance().addFight(fightHistory.getFightId(), fightHistory);
    if (Servers.localServer.LOGINSERVER)
    {
      WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), (byte)5);
      updater.sendFromLoginServer();
    }
    if (fightHistory.getFightWinner() == entity.getId())
    {
      fightEndEffects(entity, enemy);
      return true;
    }
    fightEndEffects(enemy, entity);
    return false;
  }
  
  private final void fightEndEffects(EpicEntity winner, EpicEntity loser)
  {
    if (loser.isWurm()) {
      winner.broadCastWithName(" wards off " + loser.getName() + getFullPrepositionString());
    } else if (winner.isWurm()) {
      loser.broadCastWithName(" is defeated by " + winner.getName() + getFullPrepositionString());
    } else if (loser.isSentinelMonster()) {
      winner.broadCastWithName(" prevails against " + loser.getName() + getFullPrepositionString());
    } else {
      loser.broadCastWithName(" is vanquished by " + winner.getName() + getFullPrepositionString());
    }
    loser.dropAll(winner.isDemigod());
    removeEntity(loser, false);
    
    addVisitedBy(loser, false);
    if (loser.isDemigod()) {
      this.myMap.destroyEntity(loser);
    }
  }
  
  private final boolean resolveDisputeDeprecated(EpicEntity entity)
  {
    EpicEntity enemy = null;
    EpicEntity enemy2 = null;
    EpicEntity helper = null;
    EpicEntity friend = null;
    for (EpicEntity e : this.entities) {
      if (e != entity)
      {
        if (e.isEnemy(entity))
        {
          if (enemy == null) {
            enemy = e;
          } else {
            enemy2 = e;
          }
        }
        else if ((e.isAlly()) && (e.isFriend(entity))) {
          helper = e;
        }
        if ((e.isDeity()) || (e.isDemigod()) || (entity.isFriend(e))) {
          friend = e;
        }
      }
    }
    if (friend != null) {
      if ((friend.countCollectables() > 0) && (entity.countCollectables() > 0) && 
        (entity.isDeity())) {
        friend.giveCollectables(entity);
      }
    }
    if (enemy != null)
    {
      do
      {
        do
        {
          do
          {
            if (enemy != null)
            {
              if (attack(enemy, entity)) {
                return false;
              }
              if (attack(entity, enemy))
              {
                enemy = null;
                if (enemy2 == null) {
                  return true;
                }
              }
              if (helper != null) {
                if (attack(helper, enemy))
                {
                  enemy = null;
                  if (enemy2 == null) {
                    return true;
                  }
                }
              }
            }
          } while (enemy2 == null);
          if (!attack(entity, enemy2)) {
            break;
          }
          enemy2 = null;
        } while (enemy != null);
        return true;
      } while (!attack(enemy2, entity));
      return false;
    }
    return true;
  }
  
  private final boolean attack(EpicEntity entity, EpicEntity enemy)
  {
    if (entity.rollAttack()) {
      if (enemy.setVitality(enemy.getVitality() - 1.0F))
      {
        if (enemy.isWurm()) {
          entity.broadCastWithName(" wards off " + enemy.getName() + getFullPrepositionString());
        } else if (entity.isWurm()) {
          enemy.broadCastWithName(" is defeated by " + entity.getName() + getFullPrepositionString());
        } else if (enemy.isSentinelMonster()) {
          entity.broadCastWithName(" prevails against " + enemy.getName() + getFullPrepositionString());
        } else {
          enemy.broadCastWithName(" is vanquished by " + entity.getName() + getFullPrepositionString());
        }
        enemy.dropAll(entity.isDemigod());
        removeEntity(enemy, false);
        
        addVisitedBy(enemy, false);
        if (enemy.isDemigod()) {
          this.myMap.destroyEntity(enemy);
        }
        return true;
      }
    }
    return false;
  }
  
  protected final String getCollectibleName()
  {
    for (ListIterator<EpicEntity> lit = this.entities.listIterator(); lit.hasNext();)
    {
      EpicEntity next = (EpicEntity)lit.next();
      if (next.isCollectable()) {
        return next.getName();
      }
    }
    return "";
  }
  
  protected final int countCollectibles()
  {
    int toret = 0;
    for (ListIterator<EpicEntity> lit = this.entities.listIterator(); lit.hasNext();)
    {
      EpicEntity next = (EpicEntity)lit.next();
      if (next.isCollectable()) {
        toret++;
      }
    }
    return toret;
  }
  
  private final void pickupStuff(EpicEntity entity)
  {
    for (ListIterator<EpicEntity> lit = this.entities.listIterator(); lit.hasNext();)
    {
      EpicEntity next = (EpicEntity)lit.next();
      if ((next.isCollectable()) || (next.isSource()))
      {
        entity.logWithName(" found " + next.getName() + ".");
        lit.remove();
        next.setMapHex(null);
        next.setCarrier(entity, true, false, false);
      }
    }
  }
  
  public boolean isStrength()
  {
    return this.type == 3;
  }
  
  public boolean isVitality()
  {
    return this.type == 4;
  }
  
  final boolean setEntityEffects(EpicEntity entity)
  {
    if (resolveDispute(entity))
    {
      switch (this.type)
      {
      case 1: 
        break;
      case 2: 
        break;
      case 3: 
        if ((entity.isDeity()) || (entity.isWurm()))
        {
          float current = entity.getCurrentSkill(102);
          entity.setSkill(102, current + (100.0F - current) / 1250.0F);
          current = entity.getCurrentSkill(104);
          entity.setSkill(104, current + (100.0F - current) / 1250.0F);
          current = entity.getCurrentSkill(105);
          entity.setSkill(105, current + (100.0F - current) / 1250.0F);
          
          entity.broadCastWithName(" is strengthened by the influence of " + getName() + ".");
        }
        break;
      case 4: 
        if ((entity.isDeity()) || (entity.isWurm()))
        {
          float current = entity.getCurrentSkill(100);
          entity.setSkill(100, current + (100.0F - current) / 1250.0F);
          current = entity.getCurrentSkill(103);
          entity.setSkill(103, current + (100.0F - current) / 1250.0F);
          current = entity.getCurrentSkill(101);
          entity.setSkill(101, current + (100.0F - current) / 1250.0F);
          
          entity.broadCastWithName(" is vitalized by the influence of " + getName() + ".");
        }
        break;
      case 5: 
        break;
      }
      entity.setVitality(Math.max(entity.getInitialVitality() / 2.0F, entity.getVitality()), false);
      pickupStuff(entity);
      addVisitedBy(entity, false);
      return true;
    }
    return false;
  }
  
  long getEntitySpawn()
  {
    return this.spawnEntityId;
  }
  
  boolean isSpawnFor(long entityId)
  {
    return this.spawnEntityId == entityId;
  }
  
  void setSpawnEntityId(long entityId)
  {
    this.spawnEntityId = entityId;
  }
  
  boolean isSpawn()
  {
    return this.spawnEntityId != 0L;
  }
  
  boolean isHomeFor(long entityId)
  {
    return this.homeEntityId == entityId;
  }
  
  void setHomeEntityId(long entityId)
  {
    this.homeEntityId = entityId;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\epic\MapHex.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */