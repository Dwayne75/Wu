package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcCreateEpicMission;
import com.wurmonline.server.webinterface.WcEpicStatusReport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EpicEntity
  implements MiscConstants, TimeConstants
{
  private static final String CREATE_ENTITY = "INSERT INTO ENTITIES (ID,NAME,SPAWNPOINT,ENTITYTYPE,ATTACK,VITALITY,INATTACK,INVITALITY,CARRIER) VALUES (?,?,?,?,?,?,?,?,?)";
  private static final String CREATE_ENTITY_SKILLS = "INSERT INTO ENTITYSKILLS (ENTITYID,SKILLID,DEFAULTVAL,CURRENTVAL) VALUES (?,?,?,?)";
  private static final String UPDATE_ENTITY_SKILLS = "UPDATE ENTITYSKILLS SET DEFAULTVAL=?,CURRENTVAL=? WHERE ENTITYID=? AND SKILLID=?";
  private static final String UPDATE_ENTITY_COMPANION = "UPDATE ENTITIES SET COMPANION=? WHERE ID=?";
  private static final String UPDATE_ENTITY_DEMIGODPLUS = "UPDATE ENTITIES SET DEMIGODPLUS=? WHERE ID=?";
  private static final String UPDATE_ENTITY_CARRIER = "UPDATE ENTITIES SET CARRIER=? WHERE ID=?";
  private static final String UPDATE_ENTITY_POWERVIT = "UPDATE ENTITIES SET ATTACK=?,VITALITY=?,INATTACK=?,INVITALITY=? WHERE ID=?";
  private static final String UPDATE_ENTITY_HEX = "UPDATE ENTITIES SET CURRENTHEX=?,HELPED=?,ENTERED=?,LEAVING=?,TARGETHEX=? WHERE ID=?";
  private static final String UPDATE_ENTITY_TYPE = "UPDATE ENTITIES SET ENTITYTYPE=? WHERE ID=?";
  private static final String DELETE_ENTITY = "DELETE FROM ENTITIES WHERE ID=?";
  private static final Logger logger = Logger.getLogger(EpicEntity.class.getName());
  static final int TYPE_DEITY = 0;
  public static final int TYPE_SOURCE = 1;
  public static final int TYPE_COLLECT = 2;
  static final int TYPE_WURM = 4;
  public static final int TYPE_MONSTER_SENTINEL = 5;
  public static final int TYPE_ALLY = 6;
  public static final int TYPE_DEMIGOD = 7;
  static final long MIN_TIME_PER_HEX = 7200000L;
  static final long MOVE_TIME_PER_HEX = 60000L;
  static final long MIN_TIME_TRAPPED = 86400000L;
  static final long MAX_TIME_TRAPPED = 518400000L;
  private static final int HELPED_TIME_MODIFIER = 1;
  static final long MISSION_TIME_EFFECT = 43200000L;
  private static final int NOT_HELPED_TIME_MODIFIER = 12;
  private static final Random RAND = new Random();
  private boolean headingHome = false;
  private static final int DIEROLL = 20;
  private final String name;
  private final long identifier;
  private int type = 0;
  private boolean helped = false;
  private String collName = "";
  private long enteredCurrentHex = 0L;
  private long timeUntilLeave = 0L;
  private boolean shouldCreateMission = false;
  private boolean succeedLastMission = false;
  private int targetHex = 0;
  private float attack = 0.0F;
  private float vitality = 0.0F;
  private float initialAttack = 0.0F;
  private float initialVitality = 0.0F;
  private MapHex hex = null;
  private HexMap myMap = null;
  private EpicEntity carrier = null;
  private EpicEntity companion = null;
  private int steps = 0;
  private byte demigodsToAppoint = 0;
  private static final int TWELVE_HOURS = 43200000;
  private static final int TWENTY_HOURS = 72000000;
  private static final int LEAVE_TIME = 259200000;
  private long nextSpawnedCreatures = System.currentTimeMillis() + 43200000L + new Random().nextInt(43200000);
  private boolean dirtyVitality = false;
  private final List<EpicEntity> entities = new ArrayList();
  private static boolean dumpToXML = true;
  private long nextHeal = System.currentTimeMillis() + 3600000L;
  private WcCreateEpicMission lastSentWCC;
  private final Set<Integer> serversFailed = new HashSet();
  private int latestMissionDifficulty = -10;
  private HashMap<Integer, SkillVal> skills = new HashMap();
  
  EpicEntity(HexMap map, long id, String entityName, int entityType)
  {
    this.identifier = id;
    this.name = entityName;
    this.type = entityType;
    setHexMap(map);
  }
  
  EpicEntity(HexMap map, long id, String entityName, int entityType, float entityInitialAttack, float entityInitialVitality)
  {
    this(map, id, entityName, entityType, entityInitialAttack, entityInitialVitality, false, 0L, System.currentTimeMillis() + 259200000L, -1);
  }
  
  public static final void toggleXmlDump(boolean dump)
  {
    dumpToXML = dump;
  }
  
  EpicEntity(HexMap map, long id, String entityName, int entityType, float entityInitialAttack, float entityInitialVitality, boolean isHelped, long enterTime, long leaveTime, int targetH)
  {
    this.identifier = id;
    this.name = entityName;
    this.type = entityType;
    this.initialAttack = entityInitialAttack;
    this.attack = this.initialAttack;
    this.initialVitality = entityInitialVitality;
    this.vitality = this.initialVitality;
    this.helped = isHelped;
    this.enteredCurrentHex = enterTime;
    this.timeUntilLeave = leaveTime;
    this.targetHex = targetH;
    setHexMap(map);
  }
  
  public void setLatestMissionDifficulty(int em)
  {
    this.latestMissionDifficulty = em;
  }
  
  public int getLatestMissionDifficulty()
  {
    return this.latestMissionDifficulty;
  }
  
  void setHexMap(HexMap newMap)
  {
    if (this.myMap != null) {
      this.myMap.removeEntity(this);
    }
    this.myMap = newMap;
    if (this.myMap != null) {
      this.myMap.addEntity(this);
    }
  }
  
  public final long getId()
  {
    return this.identifier;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  void setType(int newType)
  {
    this.type = newType;
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("UPDATE ENTITIES SET ENTITYTYPE=? WHERE ID=?");
      ps.setInt(1, this.type);
      ps.setLong(2, getId());
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
  
  EpicEntity getDemiGod()
  {
    return this.myMap.getDemiGodFor(this);
  }
  
  public final int getType()
  {
    return this.type;
  }
  
  public final boolean isDeity()
  {
    return this.type == 0;
  }
  
  public final String getCollectibleName()
  {
    return this.collName;
  }
  
  public final boolean isDemigod()
  {
    return this.type == 7;
  }
  
  final boolean isSentinelMonster()
  {
    return this.type == 5;
  }
  
  final boolean isWurm()
  {
    return this.type == 4;
  }
  
  public final boolean isCollectable()
  {
    return this.type == 2;
  }
  
  final boolean isAlly()
  {
    return this.type == 6;
  }
  
  final void setCompanion(EpicEntity entity)
  {
    setCompanion(entity, false);
  }
  
  final void setCompanion(EpicEntity entity, boolean load)
  {
    if (this.companion != null) {
      logger.log(Level.WARNING, getName() + " replacing " + this.companion.getName() + " with " + entity.getName());
    }
    this.companion = entity;
    if (!load) {
      setCompanionForEntity(this.companion == null ? 0L : this.companion.getId());
    }
  }
  
  public final void addFailedServer(int serverId)
  {
    this.serversFailed.add(Integer.valueOf(serverId));
    logger.log(Level.INFO, getName() + " adding failed server for epic mission creation command.");
  }
  
  public final void checkifServerFailed(int serverId)
  {
    if (this.lastSentWCC == null) {
      return;
    }
    boolean remove = false;
    for (Integer id : this.serversFailed) {
      if (id == Integer.valueOf(serverId)) {
        if (this.lastSentWCC != null)
        {
          LoginServerWebConnection lsw = new LoginServerWebConnection(serverId);
          lsw.sendWebCommand(this.lastSentWCC.getType(), this.lastSentWCC);
          logger.log(Level.INFO, getName() + " ... Server " + serverId + " has reconnected. Resent WCC!");
          remove = true;
        }
      }
    }
    if (remove) {
      this.serversFailed.remove(Integer.valueOf(serverId));
    }
    if (this.serversFailed.isEmpty()) {
      this.lastSentWCC = null;
    }
  }
  
  protected void sendNewScenarioWebCommand(int difficulty)
  {
    if ((this.myMap != null) && (isDeity()))
    {
      EpicMission oldmission = EpicServerStatus.getEpicMissionForEntity((int)getId());
      if (oldmission != null) {
        EpicServerStatus.deleteMission(oldmission);
      }
      int numberOfLoyalServers = Servers.getNumberOfLoyalServers((int)getId());
      if (!Servers.localServer.EPIC)
      {
        EpicMission mission = new EpicMission((int)getId(), this.myMap.getScenarioNumber(), getName() + " waiting for help", this.myMap.getScenarioName(), (int)getId(), (byte)-10, difficulty, 0.0F, numberOfLoyalServers, System.currentTimeMillis(), false, true);
        EpicServerStatus.addMission(mission);
        mission.setCurrent(true);
      }
      WcCreateEpicMission wce = new WcCreateEpicMission(WurmId.getNextWCCommandId(), this.myMap.getScenarioName(), this.myMap.getScenarioNumber(), this.myMap.getReasonAndEffectInt(), this.myMap.getCollictblesRequiredToWin(), this.myMap.getCollictblesRequiredForWurmToWin(), this.myMap.isSpawnPointRequiredToWin(), this.myMap.getHexNumRequiredToWin(), this.myMap.getScenarioQuestString() + ' ' + getLocationStatus() + ' ' + getEnemyStatus(), getId(), difficulty, getName(), (getTimeUntilLeave() - System.currentTimeMillis()) / 1000L, false);
      this.lastSentWCC = wce;
      wce.sendFromLoginServer();
    }
  }
  
  final void setDemigodsToAppoint(byte aDemigodsToAppoint)
  {
    this.demigodsToAppoint = aDemigodsToAppoint;
  }
  
  void setMapHex(MapHex mapHex)
  {
    if (mapHex != null) {
      broadCastWithName(" enters " + mapHex.getName());
    }
    setMapHex(mapHex, false);
  }
  
  int resetSteps()
  {
    int toReturn = this.steps;
    this.steps = 0;
    return toReturn;
  }
  
  protected void setMapHex(MapHex mapHex, boolean load)
  {
    if ((mapHex != null) && (!mapHex.equals(this.hex)))
    {
      if (this.hex != null) {
        this.hex.removeEntity(this, load);
      }
      this.hex = mapHex;
      this.steps += 1;
      setHelped(false, load);
      toggleXmlDump(false);
      this.hex.addEntity(this);
      if (!load) {
        setEnteredCurrentHex();
      }
      toggleXmlDump(true);
    }
    else if (mapHex == null)
    {
      if (this.hex != null)
      {
        toggleXmlDump(false);
        this.hex.removeEntity(this, load);
        toggleXmlDump(true);
      }
      this.hex = null;
      saveHexPos();
    }
  }
  
  public final void setHelped(boolean isHelped, boolean load)
  {
    this.helped = isHelped;
    if (!load) {
      saveHexPos();
    }
  }
  
  final float getHelpModifier()
  {
    if (isDeity())
    {
      if (this.helped) {
        return 1.0F;
      }
      return 12.0F;
    }
    return 1.0F;
  }
  
  public final long getTimeUntilLeave()
  {
    if (this.hex != null) {
      return this.timeUntilLeave;
    }
    return getMinTimePerHex();
  }
  
  public final long getTimeToNextHex()
  {
    if (this.targetHex > 0)
    {
      MapHex next = this.myMap.getMapHex(this.targetHex);
      if (next != null) {
        return ((float)getTimeUntilLeave() + 60000.0F * next.getMoveCost());
      }
    }
    return getTimeUntilLeave();
  }
  
  final void poll()
  {
    if (this.hex != null)
    {
      if (this.targetHex > 0) {
        if (System.currentTimeMillis() > getTimeUntilLeave())
        {
          MapHex next = this.myMap.getMapHex(this.targetHex);
          if ((next != null) && (System.currentTimeMillis() > getTimeToNextHex())) {
            if (this.hex.checkLeaveStatus(this))
            {
              if (this.hex.isTeleport())
              {
                next = this.myMap.getRandomHex();
                while (!next.mayEnter(this)) {
                  next = this.myMap.getRandomHex();
                }
                this.targetHex = 0;
                broadCastWithName(" shifts to " + next.getName() + ".");
              }
              setMapHex(next);
            }
          }
        }
      }
    }
    else if ((!isCollectable()) && (!isSource())) {
      spawn();
    }
    if ((!isCollectable()) && (!isSource()) && (System.currentTimeMillis() > this.nextHeal)) {
      if (getVitality() < getInitialVitality())
      {
        setVitality(Math.min(getInitialVitality(), getVitality() + 1.0F));
        this.nextHeal = (System.currentTimeMillis() + 72000000L);
      }
    }
    if ((isDeity()) || (isWurm())) {
      findNextTargetHex();
    }
    if (((isDeity()) || (isWurm())) && (System.currentTimeMillis() > this.nextSpawnedCreatures))
    {
      int next = 72000000;
      if (this.myMap.spawnCreatures(this)) {
        next = 144000000;
      }
      this.nextSpawnedCreatures = (System.currentTimeMillis() + 72000000L + new Random().nextInt(next));
      logger.log(Level.INFO, getName() + " spawns creatures. Next in " + 
        Server.getTimeFor(this.nextSpawnedCreatures - System.currentTimeMillis()));
    }
    if (this.dirtyVitality)
    {
      updateEntityVitality();
      this.dirtyVitality = false;
    }
  }
  
  final boolean setVitality(float newVitality)
  {
    return setVitality(newVitality, false);
  }
  
  public static final long getMinTimePerHex()
  {
    return 7200000L;
  }
  
  final boolean setVitality(float newVitality, boolean load)
  {
    if (this.initialVitality == 0.0F) {
      this.initialVitality = newVitality;
    }
    this.vitality = newVitality;
    if ((!load) && (this.vitality > 0.0F)) {
      this.dirtyVitality = true;
    }
    return this.vitality <= 0.0F;
  }
  
  final void permanentlyModifyVitality(float modifierVal)
  {
    this.vitality += modifierVal;
    updateEntityVitality();
  }
  
  final void permanentlyModifyAttack(float modifierVal)
  {
    this.attack += modifierVal;
    updateEntityVitality();
  }
  
  public final boolean isSource()
  {
    return this.type == 1;
  }
  
  public final float getVitality()
  {
    return this.vitality;
  }
  
  final float getInitialVitality()
  {
    return this.initialVitality;
  }
  
  final float getInitialAttack()
  {
    return this.initialAttack;
  }
  
  final boolean isFriend(EpicEntity other)
  {
    if (other != null)
    {
      if (other.equals(this.companion)) {
        return true;
      }
      if ((other.getCompanion() != null) && (other.getCompanion() != this) && (other.getCompanion().isCompanion(this))) {
        return true;
      }
      return other.isCompanion(this);
    }
    return false;
  }
  
  final boolean isEnemy(EpicEntity other)
  {
    if (other == this) {
      return false;
    }
    if (isFriend(other)) {
      return false;
    }
    if (other.isFriend(this)) {
      return false;
    }
    if (other.isSentinelMonster()) {
      return !other.isWurm();
    }
    if (other.isWurm()) {
      return (isDeity()) || (isAlly()) || (isDemigod());
    }
    if ((other.isDeity()) || (other.isDemigod())) {
      if ((isDeity()) || (isWurm()) || (isSentinelMonster()) || (isDemigod())) {
        return true;
      }
    }
    return other.isCompanion(this);
  }
  
  final boolean rollAttack()
  {
    int bonus = 0;
    for (EpicEntity e : this.entities) {
      if (e.isSource()) {
        bonus++;
      }
    }
    if (this.hex != null)
    {
      if (this.hex.isHomeFor(this.identifier)) {
        bonus++;
      }
      if (this.hex.isSpawnFor(getId())) {
        bonus++;
      }
    }
    if (this.helped) {
      bonus++;
    }
    return RAND.nextInt(20) < Math.min(18.0F, this.attack + bonus);
  }
  
  final EpicEntity getCompanion()
  {
    return this.companion;
  }
  
  final boolean isCompanion(EpicEntity entity)
  {
    return (entity != null) && (entity.equals(this.companion));
  }
  
  final void setAttack(float newAttack)
  {
    setAttack(newAttack, false);
  }
  
  final void setAttack(float newAttack, boolean load)
  {
    if (this.initialAttack == 0.0F) {
      this.initialAttack = newAttack;
    }
    this.attack = Math.min(18.0F, Math.max(this.initialAttack, newAttack));
    if (!load) {
      updateEntityVitality();
    }
  }
  
  public final float getAttack()
  {
    return this.attack;
  }
  
  final void spawn()
  {
    this.headingHome = false;
    this.carrier = null;
    this.vitality = this.initialVitality;
    this.attack = this.initialAttack;
    updateEntityVitality();
    this.targetHex = 0;
    this.helped = false;
    resetSteps();
    if (this.myMap != null)
    {
      MapHex mh = this.myMap.getSpawnHex(this);
      if (mh != null)
      {
        if (!mh.containsEnemy(this)) {
          mh.addEntity(this);
        }
      }
      else {
        saveHexPos();
      }
    }
  }
  
  final EpicEntity getCarrier()
  {
    return this.carrier;
  }
  
  int getHexNumRequiredToWin()
  {
    return this.myMap.getHexNumRequiredToWin();
  }
  
  boolean mustReturnHomeToWin()
  {
    return this.myMap.isSpawnPointRequiredToWin();
  }
  
  boolean hasEnoughCollectablesToWin()
  {
    if (isWurm()) {
      return countCollectables() >= this.myMap.getCollictblesRequiredForWurmToWin();
    }
    if (isDeity()) {
      return countCollectables() >= this.myMap.getCollictblesRequiredToWin();
    }
    return false;
  }
  
  private final void findNextTargetHex()
  {
    if (this.hex != null) {
      if ((this.targetHex == this.hex.getId()) || (this.targetHex <= 0)) {
        setNextTargetHex(this.hex.findNextHex(this));
      }
    }
  }
  
  public final void setNextTargetHex(int target)
  {
    if (target > 0) {
      logger.log(Level.INFO, getName() + " set target hex to " + this.myMap.getMapHex(target).getName());
    } else {
      logger.log(Level.INFO, getName() + " set target hex to 0.");
    }
    this.targetHex = target;
    saveHexPos();
    sendEntityData();
  }
  
  public final int getTargetHex()
  {
    return this.targetHex;
  }
  
  public final long getEnteredCurrentHexTime()
  {
    return this.enteredCurrentHex;
  }
  
  private final void setEnteredCurrentHex()
  {
    this.enteredCurrentHex = System.currentTimeMillis();
    if (isWurm()) {
      this.timeUntilLeave = (System.currentTimeMillis() + 86400000L);
    } else {
      this.timeUntilLeave = (System.currentTimeMillis() + 259200000L);
    }
    if (this.hex != null)
    {
      if (this.hex.isTrap()) {
        if (isWurm()) {
          this.timeUntilLeave += 86400000L;
        } else {
          this.timeUntilLeave += 259200000L;
        }
      }
      if (this.hex.isSlow()) {
        if (isWurm()) {
          this.timeUntilLeave += 43200000L;
        } else {
          this.timeUntilLeave += 86400000L;
        }
      }
    }
    setShouldCreateMission(true, true);
    saveHexPos();
  }
  
  public long modifyTimeToLeave(long timeChanged)
  {
    this.timeUntilLeave += timeChanged;
    return this.timeUntilLeave;
  }
  
  public MapHex getMapHex()
  {
    return this.hex;
  }
  
  void setCarrier(EpicEntity entity, boolean setReverse, boolean load, boolean log)
  {
    if (setReverse)
    {
      if (entity != null) {
        entity.addEntity(this, log, true);
      }
      if (this.carrier != null) {
        this.carrier.removeEntity(this, log);
      }
    }
    this.carrier = entity;
    if (!load) {
      saveCarrierForEntity();
    }
  }
  
  private final void addEntity(EpicEntity entity, boolean log, boolean receives)
  {
    if (!this.entities.contains(entity))
    {
      this.entities.add(entity);
      if (log) {
        if (receives) {
          logWithName(" receives " + entity.getName());
        } else {
          logWithName(" finds " + entity.getName());
        }
      }
    }
  }
  
  private final void removeEntity(EpicEntity entity, boolean log)
  {
    if (this.entities.contains(entity))
    {
      this.entities.remove(entity);
      if (log) {
        logWithName(" drops " + entity.getName());
      }
    }
  }
  
  final void dropAll(boolean killedByDemigod)
  {
    toggleXmlDump(false);
    ListIterator<EpicEntity> lit;
    if (!this.entities.isEmpty()) {
      for (lit = this.entities.listIterator(); lit.hasNext();)
      {
        EpicEntity next = (EpicEntity)lit.next();
        lit.remove();
        next.setCarrier(null, false, false, true);
        if (killedByDemigod) {
          next.setMapHex(this.myMap.getRandomHex());
        } else {
          next.setMapHex(this.hex);
        }
      }
    }
    toggleXmlDump(true);
  }
  
  void setHeadingHome(boolean headingHomeToSet)
  {
    this.headingHome = headingHomeToSet;
  }
  
  boolean isHeadingHome()
  {
    return this.headingHome;
  }
  
  public void broadCastWithName(String toBroadCast)
  {
    if (this.myMap != null) {
      this.myMap.broadCast(this.name + toBroadCast);
    }
  }
  
  void broadCast(String toBroadCast)
  {
    if (this.myMap != null) {
      this.myMap.broadCast(toBroadCast);
    }
  }
  
  void log(String toLog)
  {
    logger.log(Level.INFO, toLog);
  }
  
  void logWithName(String toLog)
  {
    logger.log(Level.INFO, this.name + toLog);
  }
  
  public final String getLocationStatus()
  {
    if (this.hex != null)
    {
      if ((isCollectable()) || (isSource())) {
        return this.name + " is" + this.hex.getPrepositionString() + this.hex.getName() + ".";
      }
      String prep = this.name + this.hex.getFullPresenceString();
      if (this.hex.getSpawnEntityId() == getId()) {
        prep = this.name + this.hex.getOwnPresenceString();
      }
      if ((this.myMap != null) && (this.targetHex > 0)) {
        prep = prep + " Heading to " + this.myMap.getMapHex(this.targetHex).getName() + " leaving in " + Server.getTimeFor(getTimeUntilLeave() - System.currentTimeMillis()) + " time to next=" + Server.getTimeFor(getTimeToNextHex() - System.currentTimeMillis());
      }
      return prep;
    }
    return this.name + " is in an unknown location.";
  }
  
  public final String getEnemyStatus()
  {
    if (this.hex != null)
    {
      String prep = this.hex.getEnemyStatus(this);
      if ((prep != null) && (prep.length() > 0)) {
        logger.log(Level.INFO, prep);
      }
      return prep;
    }
    return this.name + " is in an unknown location.";
  }
  
  public final int countCollectables()
  {
    int numColl = 0;
    for (EpicEntity e : this.entities) {
      if (e.isCollectable())
      {
        this.collName = e.getName();
        numColl++;
      }
    }
    return numColl;
  }
  
  public final List<EpicEntity> getAllCollectedItems()
  {
    return this.entities;
  }
  
  public final void giveCollectables(EpicEntity receiver)
  {
    Set<EpicEntity> collsToGive = new HashSet();
    for (EpicEntity e : this.entities) {
      if (e.isCollectable()) {
        collsToGive.add(e);
      }
    }
    for (EpicEntity e : collsToGive) {
      if (e.isCollectable()) {
        e.setCarrier(receiver, true, false, true);
      }
    }
  }
  
  boolean checkWinCondition()
  {
    if ((isDeity()) || (isWurm())) {
      if (this.hex != null)
      {
        int numColl = countCollectables();
        numColl += this.hex.countCollectibles();
        if (this.steps > 0)
        {
          if (this.hex.containsEnemy(this))
          {
            if (isShouldCreateMission()) {
              sendNewScenarioWebCommand(succeededLastMission() ? -2 : -3);
            }
            setShouldCreateMission(false, false);
            return false;
          }
          boolean win = this.myMap.winCondition(isWurm(), numColl, this.hex.isSpawnFor(getId()), this.hex.getId());
          if (win)
          {
            this.myMap.win(this, this.collName, numColl);
            
            sendNewScenarioWebCommand(1);
          }
          else if (isShouldCreateMission())
          {
            sendNewScenarioWebCommand(succeededLastMission() ? -2 : -3);
          }
          setShouldCreateMission(false, false);
          return win;
        }
        if (isShouldCreateMission()) {
          sendNewScenarioWebCommand(succeededLastMission() ? -2 : -3);
        }
        setShouldCreateMission(false, false);
      }
    }
    return false;
  }
  
  final void createEntity(int spawn)
  {
    if (spawn > 0) {
      if ((this.type != 2) && (this.type != 1))
      {
        MapHex mh = this.myMap.getMapHex(spawn);
        if (!mh.isSpawnFor(this.identifier)) {
          if (!mh.isSpawn()) {
            mh.setSpawnEntityId(this.identifier);
          }
        }
      }
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("INSERT INTO ENTITIES (ID,NAME,SPAWNPOINT,ENTITYTYPE,ATTACK,VITALITY,INATTACK,INVITALITY,CARRIER) VALUES (?,?,?,?,?,?,?,?,?)");
      ps.setLong(1, this.identifier);
      ps.setString(2, this.name);
      ps.setInt(3, spawn);
      ps.setInt(4, this.type);
      ps.setFloat(5, this.attack);
      ps.setFloat(6, this.vitality);
      ps.setFloat(7, this.attack);
      ps.setFloat(8, this.vitality);
      if (this.carrier != null) {
        ps.setLong(9, this.carrier.getId());
      } else {
        ps.setLong(9, 0L);
      }
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem creating an Epic Entity for spawn: " + spawn + " due to " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public final void createAndSaveSkills()
  {
    if (this.skills.isEmpty())
    {
      logger.log(Level.WARNING, "Error creating skills for epic entity " + getName() + ". No default skills exist for this entity.");
      
      return;
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      for (localIterator = this.skills.keySet().iterator(); localIterator.hasNext();)
      {
        int skillId = ((Integer)localIterator.next()).intValue();
        
        ps = dbcon.prepareStatement("INSERT INTO ENTITYSKILLS (ENTITYID,SKILLID,DEFAULTVAL,CURRENTVAL) VALUES (?,?,?,?)");
        ps.setLong(1, this.identifier);
        ps.setInt(2, skillId);
        ps.setFloat(3, ((SkillVal)this.skills.get(Integer.valueOf(skillId))).getDefaultVal());
        ps.setFloat(4, ((SkillVal)this.skills.get(Integer.valueOf(skillId))).getCurrentVal());
        ps.executeUpdate();
      }
    }
    catch (SQLException sqx)
    {
      Iterator localIterator;
      logger.log(Level.WARNING, "Problem creating an epic entity skill due to " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public final void updateSkills()
  {
    if (this.skills.isEmpty())
    {
      logger.log(Level.WARNING, "Error updating skills for epic entity " + getName() + ". No skills exist for this entity.");
      
      return;
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      for (localIterator = this.skills.keySet().iterator(); localIterator.hasNext();)
      {
        int skillId = ((Integer)localIterator.next()).intValue();
        
        ps = dbcon.prepareStatement("UPDATE ENTITYSKILLS SET DEFAULTVAL=?,CURRENTVAL=? WHERE ENTITYID=? AND SKILLID=?");
        ps.setFloat(1, ((SkillVal)this.skills.get(Integer.valueOf(skillId))).getDefaultVal());
        ps.setFloat(2, ((SkillVal)this.skills.get(Integer.valueOf(skillId))).getCurrentVal());
        ps.setLong(3, this.identifier);
        ps.setInt(4, skillId);
        ps.executeUpdate();
      }
    }
    catch (SQLException sqx)
    {
      Iterator localIterator;
      logger.log(Level.WARNING, "Problem updating an epic entity skill due to " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private final void updateEntityVitality()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("UPDATE ENTITIES SET ATTACK=?,VITALITY=?,INATTACK=?,INVITALITY=? WHERE ID=?");
      ps.setFloat(1, this.attack);
      ps.setFloat(2, this.vitality);
      ps.setFloat(3, this.initialAttack);
      ps.setFloat(4, this.initialVitality);
      ps.setLong(5, this.identifier);
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
  
  public final void sendEntityData()
  {
    if (this.myMap != null) {
      if (dumpToXML)
      {
        EpicXmlWriter.dumpEntities(this.myMap);
        WcEpicStatusReport report = new WcEpicStatusReport(WurmId.getNextWCCommandId(), false, 0, (byte)-1, -1);
        report.fillStatusReport(this.myMap);
        report.sendFromLoginServer();
        if (Features.Feature.VALREI_MAP.isEnabled()) {
          ValreiMapData.updateFromEpicEntity(this);
        }
      }
    }
  }
  
  final void saveHexPos()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("UPDATE ENTITIES SET CURRENTHEX=?,HELPED=?,ENTERED=?,LEAVING=?,TARGETHEX=? WHERE ID=?");
      if (this.hex != null) {
        ps.setInt(1, this.hex.getId());
      } else {
        ps.setInt(1, -1);
      }
      ps.setBoolean(2, this.helped);
      ps.setLong(3, this.enteredCurrentHex);
      ps.setLong(4, this.timeUntilLeave);
      ps.setInt(5, this.targetHex);
      ps.setLong(6, getId());
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
    sendEntityData();
  }
  
  final void deleteEntity()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("DELETE FROM ENTITIES WHERE ID=?");
      ps.setLong(1, getId());
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
  
  final void setCompanionForEntity(long companionId)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("UPDATE ENTITIES SET COMPANION=? WHERE ID=?");
      ps.setLong(1, companionId);
      ps.setLong(2, this.identifier);
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
  
  final byte getDemigodsToAppoint()
  {
    return this.demigodsToAppoint;
  }
  
  final void setDemigodPlusForEntity(byte numsToAppoint)
  {
    this.demigodsToAppoint = numsToAppoint;
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("UPDATE ENTITIES SET DEMIGODPLUS=? WHERE ID=?");
      ps.setByte(1, numsToAppoint);
      ps.setLong(2, this.identifier);
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
  
  private final void saveCarrierForEntity()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("UPDATE ENTITIES SET CARRIER=? WHERE ID=?");
      ps.setLong(1, this.carrier == null ? 0L : this.carrier.getId());
      ps.setLong(2, getId());
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
  
  public boolean isShouldCreateMission()
  {
    return this.shouldCreateMission;
  }
  
  public void setShouldCreateMission(boolean aShouldCreateMission, boolean lastMissionSuccess)
  {
    this.shouldCreateMission = aShouldCreateMission;
    this.succeedLastMission = lastMissionSuccess;
  }
  
  public boolean succeededLastMission()
  {
    return this.succeedLastMission;
  }
  
  public boolean isPlayerGod()
  {
    if ((isDeity()) && (this.identifier > 100L)) {
      return true;
    }
    return false;
  }
  
  public boolean setSkill(int skillId, float newCurrentVal)
  {
    if (this.skills.containsKey(Integer.valueOf(skillId)))
    {
      ((SkillVal)this.skills.get(Integer.valueOf(skillId))).setCurrentVal(newCurrentVal);
      updateSkills();
      return true;
    }
    return false;
  }
  
  public void addSkill(int skillId, float skillVal)
  {
    setSkill(skillId, skillVal, skillVal);
  }
  
  public void setSkill(int skillId, float defaultVal, float currentVal)
  {
    if (!this.skills.containsKey(Integer.valueOf(skillId)))
    {
      this.skills.put(Integer.valueOf(skillId), new SkillVal(defaultVal, currentVal));
    }
    else
    {
      SkillVal existing = (SkillVal)this.skills.get(Integer.valueOf(skillId));
      existing.setDefaultVal(defaultVal);
      existing.setCurrentVal(currentVal);
    }
  }
  
  public void increaseRandomSkill(float skillDivider)
  {
    int randomSkill = 100 + Server.rand.nextInt(7);
    float currentSkill = getCurrentSkill(randomSkill);
    
    setSkill(randomSkill, currentSkill + (100.0F - currentSkill) / skillDivider);
  }
  
  public SkillVal getSkill(int skillId)
  {
    return (SkillVal)this.skills.get(Integer.valueOf(skillId));
  }
  
  public HashMap<Integer, SkillVal> getAllSkills()
  {
    return this.skills;
  }
  
  public float getCurrentSkill(int skillId)
  {
    if ((isCollectable()) || (isSource())) {
      return -1.0F;
    }
    if (this.skills.get(Integer.valueOf(skillId)) != null) {
      return ((SkillVal)this.skills.get(Integer.valueOf(skillId))).getCurrentVal();
    }
    if ((skillId == 102) || (skillId == 103) || (skillId == 104) || (skillId == 100) || (skillId == 101) || (skillId == 105) || (skillId == 106))
    {
      HexMap.VALREI.setEntityDefaultSkills(this);
      if (this.skills.get(Integer.valueOf(skillId)) != null)
      {
        createAndSaveSkills();
        return ((SkillVal)this.skills.get(Integer.valueOf(skillId))).getCurrentVal();
      }
    }
    logger.log(Level.WARNING, "Unable to find skill value for epic entity: " + getName() + " skill: " + skillId);
    return -1.0F;
  }
  
  class SkillVal
  {
    private float defaultVal;
    private float currentVal;
    
    SkillVal()
    {
      this(-1.0F, -1.0F);
    }
    
    SkillVal(float defaultVal, float currentVal)
    {
      this.defaultVal = defaultVal;
      this.currentVal = currentVal;
    }
    
    public void setCurrentVal(float newCurrentVal)
    {
      this.currentVal = newCurrentVal;
    }
    
    public float getCurrentVal()
    {
      return this.currentVal;
    }
    
    public void setDefaultVal(float newDefaultVal)
    {
      this.defaultVal = newDefaultVal;
    }
    
    public float getDefaultVal()
    {
      return this.defaultVal;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\epic\EpicEntity.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */