package com.wurmonline.server;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Artist;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.DbSearcher;
import com.wurmonline.server.players.IPBan;
import com.wurmonline.server.players.KingdomIp;
import com.wurmonline.server.players.MapAnnotation;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerKills;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.players.SteamIdBan;
import com.wurmonline.server.players.TabData;
import com.wurmonline.server.players.WurmRecord;
import com.wurmonline.server.questions.KosWarningInfo;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.steam.SteamHandler;
import com.wurmonline.server.steam.SteamId;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.KosWarning;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.webinterface.WCGmMessage;
import com.wurmonline.server.webinterface.WcDemotion;
import com.wurmonline.server.webinterface.WcTabLists;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.EffectConstants;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.website.StatsXMLWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Players
  implements MiscConstants, CreatureTemplateIds, EffectConstants, MonetaryConstants, TimeConstants
{
  private static Map<String, Player> players = new ConcurrentHashMap();
  private static Map<Long, Player> playersById = new ConcurrentHashMap();
  private final Map<Long, Byte> pkingdoms = new ConcurrentHashMap();
  private static final ConcurrentHashMap<String, TabData> tabListGM = new ConcurrentHashMap();
  private static final ConcurrentHashMap<String, TabData> tabListMGMT = new ConcurrentHashMap();
  private static Players instance = null;
  private static Logger logger = Logger.getLogger(Players.class.getName());
  private static final String DOES_PLAYER_NAME_EXIST = "SELECT WURMID FROM PLAYERS WHERE NAME=?";
  private static final Map<Long, Artist> artists = new HashMap();
  private static final String GET_ARTISTS = "SELECT * FROM ARTISTS";
  private static final String SET_ARTISTS = "INSERT INTO ARTISTS (WURMID,SOUND,GRAPHICS) VALUES(?,?,?)";
  private static final String DELETE_ARTIST = "DELETE FROM ARTISTS WHERE WURMID=?";
  private static final String GET_PLAYERS_BANNED = "SELECT NAME,BANREASON,BANEXPIRY FROM PLAYERS WHERE BANNED=1";
  private static final String SET_NOSTRUCTURE = "update PLAYERS set BUILDINGID=-10 WHERE BUILDINGID=?";
  private static final String GET_LASTLOGOUT = "SELECT LASTLOGOUT FROM PLAYERS WHERE WURMID=?";
  private static final String GET_KINGDOM = "SELECT KINGDOM FROM PLAYERS WHERE WURMID=?";
  private static final String GET_KINGDOM_PLAYERS = "SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND CURRENTSERVER=? AND POWER=0";
  private static final String GET_PREMIUM_KINGDOM_PLAYERS = "SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND PAYMENTEXPIRE>? AND POWER=0";
  private static final String GET_CHAMPION_KINGDOM_PLAYERS = "SELECT NAME,WURMID,REALDEATH,LASTLOSTCHAMPION FROM PLAYERS WHERE KINGDOM=? AND REALDEATH>0 AND REALDEATH<4 AND POWER=0";
  private static final String RESET_FAITHGAIN = "UPDATE PLAYERS SET LASTFAITH=0,NUMFAITH=0";
  private static final String GM_SALARY = "UPDATE PLAYERS SET MONEY=MONEY+250000 WHERE POWER>1";
  private static final String RESET_PLAYER_SKILLS = "UPDATE SKILLS SET VALUE=20, MINVALUE=20 WHERE VALUE>20 AND OWNER=?";
  private static final String RESET_PLAYER_FAITH = "UPDATE PLAYERS SET FAITH=20 WHERE FAITH>20 AND WURMID=?";
  private static final String ADD_GM_MESSAGE = "INSERT INTO GMMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)";
  private static final String ADD_MGMT_MESSAGE = "INSERT INTO MGMTMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)";
  private static final String GET_GM_MESSAGES = "SELECT TIME,SENDER,MESSAGE FROM GMMESSAGES ORDER BY TIME";
  private static final String GET_MGMT_MESSAGES = "SELECT TIME,SENDER,MESSAGE FROM MGMTMESSAGES ORDER BY TIME";
  private static final String PRUNE_GM_MESSAGES = "DELETE FROM GMMESSAGES WHERE TIME<?";
  private static final String PRUNE_MGMT_MESSAGES = "DELETE FROM MGMTMESSAGES WHERE TIME<?";
  private static final String GET_BATTLE_RANKS = "select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?";
  private static final String GET_MAXBATTLE_RANKS = "select MAXRANK,RANK, NAME from PLAYERS ORDER BY MAXRANK DESC LIMIT ?";
  private static final String GET_FRIENDS = "select p.NAME,p.WURMID from PLAYERS p INNER JOIN FRIENDS f ON f.FRIEND=p.WURMID WHERE f.WURMID=? ORDER BY NAME";
  private static final String GET_PLAYERID_BY_NAME = "SELECT WURMID FROM PLAYERS WHERE NAME=?";
  private static final String GET_PLAYERS_MUTED = "SELECT NAME,MUTEREASON,MUTEEXPIRY FROM PLAYERS WHERE MUTED=1";
  private static final String GET_MUTERS = "SELECT NAME FROM PLAYERS WHERE MAYMUTE=1";
  private static final String GET_DEVTALKERS = "SELECT NAME FROM PLAYERS WHERE DEVTALK=1";
  private static final String GET_CAS = "SELECT NAME FROM PLAYERS WHERE PA=1";
  private static final String GET_HEROS = "SELECT NAME FROM PLAYERS WHERE POWER=? AND CURRENTSERVER=?";
  private static final String GET_PRIVATE_MAP_POI = "SELECT * FROM MAP_ANNOTATIONS WHERE POITYPE=0 AND OWNERID=?";
  private static final String CHANGE_KINGDOM = "UPDATE PLAYERS SET KINGDOM=? WHERE KINGDOM=?";
  private static final String CHANGE_KINGDOM_FOR_PLAYER = "UPDATE PLAYERS SET KINGDOM=? WHERE WURMID=?";
  public static final String CACHAN = "CA HELP";
  public static final String GVCHAN = "GV HELP";
  public static final String JKCHAN = "JK HELP";
  public static final String MRCHAN = "MR HELP";
  public static final String HOTSCHAN = "HOTS HELP";
  private static final String CAPREFIX = " CA ";
  private static final Map<String, Logger> loggers = new HashMap();
  private static Set<Ban> bans = new HashSet();
  private static final Map<Long, PlayerKills> playerKills = new ConcurrentHashMap();
  private final Map<Byte, Float> crBonuses = new HashMap();
  private boolean shouldSendWeather = false;
  private final long timeBetweenChampDecreases = 604800000L;
  private static ConcurrentLinkedQueue<KosWarning> kosList = new ConcurrentLinkedQueue();
  private static String header = "<HTML> <HEAD><TITLE>Wurm battle ranks</TITLE></HEAD><BODY><BR><BR>";
  private long lastPoll = System.currentTimeMillis();
  private static final float minDelta = 0.095F;
  private static boolean pollCheckClients = false;
  private long lastCheckClients = System.currentTimeMillis();
  private static int challengeStep = 0;
  private static HashMap<Long, Short> deathCount = new HashMap();
  private static final Logger caHelpLogger = Logger.getLogger("ca-help");
  
  public static Players getInstance()
  {
    if (instance == null) {
      instance = new Players();
    }
    return instance;
  }
  
  static synchronized Players getInstanceForUnitTestingWithoutDatabase()
  {
    if (instance == null) {
      instance = new Players(true);
    }
    return instance;
  }
  
  public void setCreatureDead(Creature dead)
  {
    long deadid = dead.getWurmId();
    
    Player[] plays = getInstance().getPlayers();
    for (Player player : plays)
    {
      if (player.opponent == dead) {
        player.setOpponent(null);
      }
      if (player.target == deadid) {
        player.setTarget(-10L, true);
      }
      player.removeTarget(deadid);
    }
    Vehicles.removeDragger(dead);
  }
  
  private Players()
  {
    loadBannedIps();
    loadBannedSteamIds();
    header = getBattleRanksHtmlHeader();
  }
  
  private Players(boolean forUnitTestingWithoutDatabase)
  {
    if (forUnitTestingWithoutDatabase)
    {
      logger.warning("Instantiating Players for Unit Test without a database");
    }
    else
    {
      loadBannedIps();
      loadBannedSteamIds();
    }
  }
  
  static String getBattleRanksHtmlHeader()
  {
    return "<HTML> <HEAD><TITLE>Wurm battle ranks on " + Servers.getLocalServerName() + "</TITLE></HEAD><BODY><BR><BR>";
  }
  
  public int numberOfPlayers()
  {
    return players.size();
  }
  
  public int numberOfPremiumPlayers()
  {
    int x = 0;
    for (Player lPlayer : getInstance().getPlayers()) {
      if ((lPlayer.isPaying()) && (lPlayer.getPower() == 0)) {
        x++;
      }
    }
    return x;
  }
  
  public void weatherFlash(int tilex, int tiley, float height)
  {
    for (Player p : getInstance().getPlayers()) {
      if (p != null)
      {
        Communicator lPlayerCommunicator = p.getCommunicator();
        if (lPlayerCommunicator != null) {
          lPlayerCommunicator.sendAddEffect(9223372036854775707L, (short)1, tilex << 2, tiley << 2, height, (byte)0);
        }
      }
    }
  }
  
  public void sendGlobalNonPersistantComplexEffect(long target, short effect, int tilex, int tiley, float height, float radiusMeters, float lengthMeters, int direction, byte kingdomTemplateId, byte epicEntityId)
  {
    long effectId = Long.MAX_VALUE - Server.rand.nextInt(1000);
    for (Player p : getInstance().getPlayers()) {
      if (p != null)
      {
        Communicator lPlayerCommunicator = p.getCommunicator();
        if (lPlayerCommunicator != null) {
          lPlayerCommunicator.sendAddComplexEffect(effectId, target, effect, tilex << 2, tiley << 2, height, (byte)0, radiusMeters, lengthMeters, direction, kingdomTemplateId, epicEntityId);
        }
      }
    }
  }
  
  public void sendGlobalNonPersistantEffect(long id, short effect, int tilex, int tiley, float height)
  {
    long effectId = Long.MAX_VALUE - Server.rand.nextInt(1000);
    for (Player p : getInstance().getPlayers()) {
      if (p != null)
      {
        Communicator lPlayerCommunicator = p.getCommunicator();
        if (lPlayerCommunicator != null) {
          lPlayerCommunicator.sendAddEffect(id <= 0L ? effectId : id, effect, tilex << 2, tiley << 2, height, (byte)0);
        }
      }
    }
  }
  
  public void sendGlobalNonPersistantTimedEffect(long id, short effect, int tilex, int tiley, float height, long expireTime)
  {
    long effectId = id <= 0L ? Long.MAX_VALUE - Server.rand.nextInt(10000) : id;
    Server.getInstance().addGlobalTempEffect(effectId, expireTime);
    for (Player p : getInstance().getPlayers()) {
      if (p != null)
      {
        Communicator lPlayerCommunicator = p.getCommunicator();
        if (lPlayerCommunicator != null) {
          lPlayerCommunicator.sendAddEffect(effectId, effect, tilex << 2, tiley << 2, height, (byte)0);
        }
      }
    }
  }
  
  public final int getChallengeStep()
  {
    return challengeStep;
  }
  
  public final void setChallengeStep(int step)
  {
    if ((Servers.localServer.isChallengeServer()) || (Servers.localServer.testServer))
    {
      challengeStep = step;
      byte toSend = 0;
      switch (challengeStep)
      {
      case 1: 
        toSend = 20;
        break;
      case 2: 
        toSend = 21;
        break;
      case 3: 
        toSend = 22;
        break;
      case 4: 
        toSend = 23;
        break;
      default: 
        toSend = 0;
      }
      if (toSend > 0) {
        sendGlobalNonPersistantEffect(Long.MAX_VALUE - Server.rand.nextInt(100000), (short)toSend, 0, 0, 0.0F);
      }
    }
  }
  
  public void sendPlayerStatus(Player player)
  {
    for (Player p : getInstance().getPlayers()) {
      player.getCommunicator().sendNormalServerMessage(p
        .getName() + ", secstolog=" + p.getSecondsToLogout() + ", logged off=" + p.loggedout);
    }
  }
  
  public int getOnlinePlayersFromKingdom(byte kingdomId)
  {
    int nums = 0;
    for (Player lPlayer : getInstance().getPlayers()) {
      if (lPlayer.getKingdomId() == kingdomId) {
        nums++;
      }
    }
    return nums;
  }
  
  public Player[] getPlayersByIp()
  {
    Player[] playerArr = getPlayers();
    Arrays.sort(playerArr, new Comparator()
    {
      public int compare(Player o1, Player o2)
      {
        Player i1 = o1;
        Player i2 = o2;
        return i1.getSaveFile().getIpaddress().compareTo(i2.getSaveFile().getIpaddress());
      }
    });
    return playerArr;
  }
  
  public void sendIpsToPlayer(Player player)
  {
    Player[] playerArr = getPlayersByIp();
    for (Player lPlayer : playerArr) {
      if ((lPlayer.getPower() <= player.getPower()) && (player.getPower() > 1)) {
        player.getCommunicator().sendNormalServerMessage(lPlayer
          .getName() + " IP: " + lPlayer.getSaveFile().getIpaddress());
      }
    }
    player.getCommunicator().sendNormalServerMessage(playerArr.length + " players logged on.");
  }
  
  public void sendIpsToPlayer(Player player, String playername)
  {
    PlayerInfo pinfo = null;
    try
    {
      pinfo = getPlayer(playername).getSaveFile();
    }
    catch (NoSuchPlayerException nsp)
    {
      pinfo = PlayerInfoFactory.createPlayerInfo(playername);
      try
      {
        pinfo.load();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
    }
    if (pinfo != null)
    {
      Map<String, String> ps;
      if ((pinfo.getPower() <= player.getPower()) && (player.getPower() > 1))
      {
        Player[] playerArr = getPlayersByIp();
        ps = new HashMap();
        boolean error = false;
        ps.put(playername, pinfo.getIpaddress());
        for (Player lPlayer : playerArr) {
          if (lPlayer.getSaveFile().getIpaddress().equals(pinfo.getIpaddress())) {
            ps.put(lPlayer.getName(), lPlayer.getSaveFile().getIpaddress());
          }
        }
        for (??? = ps.keySet().iterator(); ((Iterator)???).hasNext();)
        {
          String name = (String)((Iterator)???).next();
          
          String ip = (String)ps.get(name);
          player.getCommunicator().sendNormalServerMessage(name + ", " + ip);
        }
      }
      else
      {
        player.getCommunicator().sendNormalServerMessage("You may not check that player's ip.");
      }
    }
    else
    {
      player.getCommunicator().sendNormalServerMessage(playername + " - not found!");
    }
  }
  
  public static void stopLoggers()
  {
    for (Logger logger : loggers.values()) {
      if (logger != null) {
        for (Handler h : logger.getHandlers()) {
          h.close();
        }
      }
    }
  }
  
  public static Logger getLogger(Player player)
  {
    if ((player.getPower() > 0) || (player.isLogged()) || (isArtist(player.getWurmId(), false, false)))
    {
      String name = player.getName();
      Logger personalLogger = (Logger)loggers.get(name);
      if (personalLogger == null)
      {
        personalLogger = Logger.getLogger(name);
        personalLogger.setUseParentHandlers(false);
        Handler[] h = logger.getHandlers();
        for (int i = 0; i != h.length; i++) {
          personalLogger.removeHandler(h[i]);
        }
        try
        {
          FileHandler fh = new FileHandler(name + ".log", 0, 1, true);
          fh.setFormatter(new SimpleFormatter());
          personalLogger.addHandler(fh);
        }
        catch (IOException ie)
        {
          Logger.getLogger(name).log(Level.WARNING, name + ":no redirection possible!");
        }
        loggers.put(name, personalLogger);
      }
      return personalLogger;
    }
    return null;
  }
  
  public Player getPlayer(String name)
    throws NoSuchPlayerException
  {
    Player p = getPlayerByName(LoginHandler.raiseFirstLetter(name));
    if (p == null) {
      throw new NoSuchPlayerException(name);
    }
    return p;
  }
  
  private Player getPlayerByName(String aName)
  {
    return (Player)players.get(aName);
  }
  
  public Player getPlayerOrNull(String aName)
  {
    return getPlayerByName(aName);
  }
  
  public Optional<Player> getPlayerOptional(String aName)
  {
    return Optional.ofNullable(getPlayerByName(aName));
  }
  
  public Player getPlayer(long id)
    throws NoSuchPlayerException
  {
    Player p = getPlayerById(id);
    if (p != null) {
      return p;
    }
    throw new NoSuchPlayerException("Player with id " + id + " could not be found.");
  }
  
  public Player getPlayerOrNull(long id)
  {
    return getPlayerById(id);
  }
  
  public Optional<Player> getPlayerOptional(long id)
  {
    return Optional.ofNullable(getPlayerById(id));
  }
  
  public Player getPlayer(Long id)
    throws NoSuchPlayerException
  {
    Player p = getPlayerById(id);
    if (p != null) {
      return p;
    }
    throw new NoSuchPlayerException("Player with id " + id + " could not be found.");
  }
  
  private Player getPlayerById(long aWurmID)
  {
    return getPlayerById(new Long(aWurmID));
  }
  
  private Player getPlayerById(Long aWurmID)
  {
    return (Player)playersById.get(aWurmID);
  }
  
  public Player getPlayer(SocketConnection serverConnection)
    throws NoSuchPlayerException
  {
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr) {
      try
      {
        if (serverConnection == lPlayer.getCommunicator().getConnection()) {
          return lPlayer;
        }
      }
      catch (NullPointerException ex)
      {
        if (lPlayer == null) {
          logger.log(Level.WARNING, "A player in the Players list is null. this shouldn't happen.");
        } else if (lPlayer.getCommunicator() == null) {
          logger.log(Level.WARNING, lPlayer + "'s communicator is null.");
        } else {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
      }
    }
    throw new NoSuchPlayerException("Player could not be found.");
  }
  
  void sendReconnect(Player player)
  {
    if (!player.isUndead())
    {
      player.getCommunicator().sendClearFriendsList();
      sendConnectInfo(player, " reconnected.", player.getLastLogin(), PlayerOnlineStatus.ONLINE);
    }
  }
  
  void sendAddPlayer(Player player)
  {
    if (!player.isUndead()) {
      sendConnectInfo(player, " joined.", player.getLastLogin(), PlayerOnlineStatus.ONLINE);
    }
  }
  
  public void sendConnectAlert(String message)
  {
    Player[] playerArr = getPlayers();
    for (Player lPlayer : playerArr) {
      if (lPlayer.getPower() > 0) {
        lPlayer.getCommunicator().sendAlertServerMessage(message);
      }
    }
  }
  
  public void sendConnectInfo(Player player, String message, long whenStateChanged, PlayerOnlineStatus loginstatus)
  {
    sendConnectInfo(player, message, whenStateChanged, loginstatus, false);
  }
  
  public void sendConnectInfo(Player player, String message, long whenStateChanged, PlayerOnlineStatus loginstatus, boolean loggedin)
  {
    PlayerInfoFactory.updatePlayerState(player, whenStateChanged, loginstatus);
    if (!player.isUndead())
    {
      Player[] playerArr = getPlayers();
      int tilex = player.getTileX();
      int tiley = player.getTileY();
      int tilez = (int)(player.getPositionZ() + player.getAltOffZ()) >> 2;
      int vision = 80;
      try
      {
        vision = CreatureTemplateFactory.getInstance().getTemplate(1).getVision();
      }
      catch (NoSuchCreatureTemplateException nst)
      {
        logger.log(Level.WARNING, "Failed to find HUMAN_CID. Vision set to " + vision);
      }
      Village village = player.getCitizenVillage();
      for (Player lPlayer : playerArr) {
        if (player != lPlayer)
        {
          if (lPlayer.getPower() > 1)
          {
            if ((player.getCommunicator() != null) && (player.getCommunicator().getConnection() != null) && 
              (lPlayer.getPower() > player.getPower())) {
              try
              {
                lPlayer.getCommunicator().sendSystemMessage(player
                  .getName() + "[" + player.getCommunicator().getConnection().getIp() + "] " + message);
              }
              catch (Exception ex)
              {
                lPlayer.getCommunicator().sendSystemMessage(player.getName() + message);
              }
            }
          }
          else if ((player.isVisibleTo(lPlayer)) && ((!loggedin) || (player.getPower() <= 1))) {
            if (!lPlayer.isFriend(player.getWurmId())) {
              if ((village != null) && (lPlayer.getCitizenVillage() == village)) {
                lPlayer.getCommunicator().sendSafeServerMessage(player.getName() + message);
              } else if ((lPlayer.isOnSurface() == player.isOnSurface()) && 
                (lPlayer.isWithinTileDistanceTo(tilex, tiley, tilez, vision))) {
                lPlayer.getCommunicator().sendSafeServerMessage(player.getName() + message);
              }
            }
          }
          if ((lPlayer.seesPlayerAssistantWindow()) && (player.seesPlayerAssistantWindow()))
          {
            if (player.isVisibleTo(lPlayer)) {
              if (player.isPlayerAssistant()) {
                lPlayer.getCommunicator().sendAddPa(" CA " + player.getName(), player.getWurmId());
              } else if (shouldReceivePlayerList(lPlayer)) {
                lPlayer.getCommunicator().sendAddPa(player.getName(), player.getWurmId());
              }
            }
            if (lPlayer.isVisibleTo(player)) {
              if (lPlayer.isPlayerAssistant()) {
                player.getCommunicator().sendAddPa(" CA " + lPlayer.getName(), lPlayer.getWurmId());
              } else if (shouldReceivePlayerList(player)) {
                player.getCommunicator().sendAddPa(lPlayer.getName(), lPlayer.getWurmId());
              }
            }
          }
        }
      }
    }
  }
  
  public void combatRound()
  {
    for (Player lPlayer : getInstance().getPlayers()) {
      lPlayer.getCombatHandler().clearRound();
    }
  }
  
  public void pollKosWarnings()
  {
    for (KosWarning kos : kosList)
    {
      try
      {
        Player p = getPlayer(kos.playerId);
        if ((p.isFullyLoaded()) && (p.getVisionArea() != null) && (p.getVisionArea().isInitialized()) && (p.hasLink()))
        {
          if (kos.getTick() < 10)
          {
            if (kos.tick() == 10) {
              if (p.acceptsKosPopups(kos.village.getId()))
              {
                KosWarningInfo kwi = new KosWarningInfo(p, kos.playerId, kos.village);
                kwi.sendQuestion();
              }
              else
              {
                p.getCommunicator().sendAlertServerMessage("You are being put on the KOS list of " + kos.village
                  .getName() + " again.", (byte)4);
              }
            }
          }
          else if (kos.getTick() % 30 == 0) {
            if (p.acceptsKosPopups(kos.village.getId())) {
              if (p.getCurrentVillage() == kos.village) {
                p.getCommunicator().sendAlertServerMessage("You must leave the settlement of " + kos.village
                  .getName() + " immediately or you will be attacked by the guards!", (byte)4);
              } else {
                p.getCommunicator().sendAlertServerMessage("Make sure to stay out of " + kos.village
                  .getName() + " since you soon will be killed on sight there!", (byte)4);
              }
            }
          }
          if (kos.getTick() >= 130) {
            if (p.acceptsKosPopups(kos.village.getId())) {
              p.getCommunicator().sendAlertServerMessage("You will now be killed on sight in " + kos.village
                .getName() + "!", (byte)4);
            }
          }
        }
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
      PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(kos.playerId);
      if (pinf != null)
      {
        if (kos.getTick() >= 10)
        {
          kos.tick();
          if (kos.getTick() >= 130)
          {
            Reputation r = kos.village.setReputation(kos.playerId, kos.newReputation, false, true);
            r.setPermanent(kos.permanent);
            kos.village.addHistory(pinf.getName(), "will now be killed on sight.");
            kosList.remove(kos);
          }
        }
      }
      else {
        kosList.remove(kos);
      }
    }
  }
  
  public final boolean addKosWarning(KosWarning newkos)
  {
    for (KosWarning kosw : kosList) {
      if (kosw.playerId == newkos.playerId) {
        return false;
      }
    }
    kosList.add(newkos);
    return true;
  }
  
  public final boolean removeKosFor(long wurmId)
  {
    for (KosWarning kos : kosList) {
      if (kos.playerId == wurmId)
      {
        kosList.remove(kos);
        return true;
      }
    }
    return false;
  }
  
  public void pollDeadPlayers()
  {
    Player[] playerarr = getPlayers();
    for (Player lPlayer : playerarr) {
      if (lPlayer != null) {
        if ((lPlayer.getSaveFile() != null) && 
          (lPlayer.pollDead()))
        {
          logger.log(Level.INFO, "Removing from players " + lPlayer.getName() + ".");
          players.remove(lPlayer.getName());
        }
      }
    }
  }
  
  public void broadCastMissionInfo(String missionInfo, int missionRelated)
  {
    Player[] playarr = getPlayers();
    for (int x = 0; x < playarr.length; x++)
    {
      MissionPerformer mp = MissionPerformed.getMissionPerformer(playarr[x].getWurmId());
      if (mp != null)
      {
        MissionPerformed m = mp.getMission(missionRelated);
        if (m != null) {
          playarr[x].getCommunicator().sendSafeServerMessage(missionInfo);
        }
      }
    }
  }
  
  public final void broadCastConquerInfo(Creature conquerer, String info)
  {
    Player[] playarr = getPlayers();
    for (int x = 0; x < playarr.length; x++)
    {
      int r = 200;
      int g = 200;
      int b = 25;
      if (conquerer.isFriendlyKingdom(playarr[x].getKingdomId())) {
        r = 25;
      } else {
        g = 25;
      }
      playarr[x].getCommunicator().sendDeathServerMessage(info, (byte)r, (byte)g, (byte)25);
    }
  }
  
  public final void broadCastDestroyInfo(Creature performer, String info)
  {
    Player[] playarr = getInstance().getPlayers();
    for (int x = 0; x < playarr.length; x++)
    {
      int r = 200;
      int g = 200;
      int b = 25;
      if (performer.getKingdomId() == playarr[x].getKingdomId()) {
        r = 25;
      } else {
        g = 25;
      }
      playarr[x].getCommunicator().sendDeathServerMessage(info, (byte)r, (byte)g, (byte)25);
    }
  }
  
  public final void broadCastBashInfo(Item target, String info)
  {
    Player[] playarr = getInstance().getPlayers();
    for (int x = 0; x < playarr.length; x++) {
      if (target.getKingdom() == playarr[x].getKingdomId())
      {
        int r = 200;
        int g = 25;
        int b = 25;
        playarr[x].getCommunicator().sendDeathServerMessage(info, (byte)r, (byte)g, (byte)b);
      }
    }
  }
  
  public final void broadCastDeathInfo(Player player, String slayers)
  {
    Player[] playarr;
    int x;
    int r;
    if (Servers.isThisAPvpServer())
    {
      String toSend = player.getName() + " slain by " + slayers;
      playarr = getPlayers();
      for (x = 0; x < playarr.length; x++)
      {
        r = 200;
        int g = 200;
        int b = 25;
        if (player.isFriendlyKingdom(playarr[x].getKingdomId())) {
          r = 25;
        } else {
          g = 25;
        }
        playarr[x].getCommunicator().sendDeathServerMessage(toSend, (byte)r, (byte)g, (byte)25);
      }
    }
    else if (Features.Feature.PVE_DEATHTABS.isEnabled())
    {
      String toSend = player.getName() + " slain by " + slayers;
      for (Player p : getInstance().getPlayers()) {
        if (!p.hasFlag(60)) {
          p.getCommunicator().sendDeathServerMessage(toSend, (byte)25, (byte)-56, (byte)25);
        }
      }
    }
  }
  
  public final void sendAddToAlliance(Creature player, Village village)
  {
    if (village != null)
    {
      Player[] playerArr = getPlayers();
      for (Player lPlayer : playerArr) {
        if (player != lPlayer) {
          if (player.isVisibleTo(lPlayer)) {
            if ((lPlayer.getCitizenVillage() != null) && (village.getAllianceNumber() > 0) && 
              (village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber()))
            {
              lPlayer.getCommunicator().sendAddAlly(player.getName(), player.getWurmId());
              player.getCommunicator().sendAddAlly(lPlayer.getName(), lPlayer.getWurmId());
            }
          }
        }
      }
    }
  }
  
  public final void sendRemoveFromAlliance(Creature player, Village village)
  {
    if (village != null)
    {
      Player[] playerArr = getPlayers();
      for (Player lPlayer : playerArr) {
        if (player != lPlayer) {
          if ((lPlayer.getCitizenVillage() != null) && (village.getAllianceNumber() > 0) && 
            (village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber()))
          {
            lPlayer.getCommunicator().sendRemoveAlly(player.getName());
            player.getCommunicator().sendRemoveAlly(lPlayer.getName());
          }
        }
      }
    }
  }
  
  public void addToGroups(Player player)
  {
    if (!player.isUndead())
    {
      try
      {
        Groups.getGroup("wurm").addMember(player.getName(), player);
      }
      catch (NoSuchGroupException ex)
      {
        logger.log(Level.WARNING, "Could not get group for Group 'wurm', Player: " + player + " due to " + ex
          .getMessage(), ex);
      }
      Village citvil = Villages.getVillageForCreature(player);
      player.setCitizenVillage(citvil);
      player.sendSkills();
      if (citvil != null) {
        try
        {
          citvil.setLogin();
          Groups.getGroup(citvil.getName()).addMember(player.getName(), player);
          if (citvil.getAllianceNumber() > 0)
          {
            PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(citvil.getAllianceNumber());
            if ((pvpAll != null) && (!pvpAll.getMotd().isEmpty()))
            {
              Message mess = pvpAll.getMotdMessage();
              player.getCommunicator().sendMessage(mess);
            }
            else
            {
              Message mess = new Message(player, (byte)15, "Alliance", "");
              player.getCommunicator().sendMessage(mess);
            }
          }
        }
        catch (NoSuchGroupException ex)
        {
          logger.log(Level.WARNING, "Could not get group for Village: " + citvil + ", Player: " + player + " due to " + ex
            .getMessage(), ex);
        }
      }
      Player[] playerArr = getPlayers();
      
      Village village = player.getCitizenVillage();
      if (village != null) {
        for (Player lPlayer : playerArr) {
          if (player != lPlayer) {
            if (player.isVisibleTo(lPlayer, true))
            {
              if (lPlayer.getCitizenVillage() == village) {
                lPlayer.getCommunicator().sendAddVillager(player.getName(), player.getWurmId());
              }
              if ((lPlayer.getCitizenVillage() != null) && (village.getAllianceNumber() > 0) && 
                (village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber()))
              {
                lPlayer.getCommunicator().sendAddAlly(player.getName(), player.getWurmId());
                player.getCommunicator().sendAddAlly(lPlayer.getName(), lPlayer.getWurmId());
              }
            }
          }
        }
      }
    }
    else
    {
      player.sendSkills();
    }
  }
  
  private void removeFromGroups(Player player)
  {
    try
    {
      Groups.getGroup("wurm").dropMember(player.getName());
      if (player.getCitizenVillage() != null) {
        Groups.getGroup(player.getCitizenVillage().getName()).dropMember(player.getName());
      }
    }
    catch (NoSuchGroupException nsg)
    {
      logger.log(Level.WARNING, "Could not get group for Village: " + player.getCitizenVillage() + ", Player: " + player + " due to " + nsg
        .getMessage(), nsg);
    }
    if ((player.mayHearDevTalk()) || (player.mayHearMgmtTalk()))
    {
      removeFromTabs(player.getWurmId(), player.getName());
      sendRemoveFromTabs(player.getWurmId(), player.getName());
    }
    Village village = player.getCitizenVillage();
    Player[] playerArr = getPlayers();
    for (Player lPlayer : playerArr) {
      if (player != lPlayer)
      {
        if (village != null)
        {
          if (lPlayer.getCitizenVillage() == village) {
            lPlayer.getCommunicator().sendRemoveVillager(player.getName());
          }
          if ((lPlayer.getCitizenVillage() != null) && (village.getAllianceNumber() > 0) && 
            (village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber())) {
            lPlayer.getCommunicator().sendRemoveAlly(player.getName());
          }
        }
        if ((player.seesPlayerAssistantWindow()) && (lPlayer.seesPlayerAssistantWindow())) {
          if (player.isPlayerAssistant()) {
            lPlayer.getCommunicator().sendRemovePa(" CA " + player.getName());
          } else if (shouldReceivePlayerList(lPlayer)) {
            lPlayer.getCommunicator().sendRemovePa(player.getName());
          }
        }
      }
    }
  }
  
  public void setShouldSendWeather(boolean shouldSend)
  {
    this.shouldSendWeather = shouldSend;
  }
  
  private boolean shouldSendWeather()
  {
    return this.shouldSendWeather;
  }
  
  public void checkSendWeather()
  {
    if (shouldSendWeather())
    {
      sendWeather();
      setShouldSendWeather(false);
    }
  }
  
  public void sendWeather()
  {
    Player[] playerArr = getPlayers();
    for (Player lPlayer : playerArr) {
      if ((lPlayer != null) && (lPlayer.getCommunicator() != null)) {
        lPlayer.getCommunicator().sendWeather();
      }
    }
  }
  
  public Player logout(SocketConnection serverConnection)
  {
    Player player = null;
    String ip = "";
    if (serverConnection != null)
    {
      try
      {
        ip = serverConnection.getIp();
      }
      catch (Exception localException) {}
      try
      {
        serverConnection.disconnect();
      }
      catch (NullPointerException localNullPointerException) {}
    }
    try
    {
      player = getPlayer(serverConnection);
      logoutPlayer(player);
    }
    catch (NoSuchPlayerException ex)
    {
      try
      {
        player = getPlayer(serverConnection);
        if (player != null)
        {
          if (ip.equals("")) {
            ip = player.getSaveFile().getIpaddress();
          }
          removeFromGroups(player);
          players.remove(player.getName());
          playersById.remove(Long.valueOf(player.getWurmId()));
          logger.log(Level.INFO, "Logout - " + ex.getMessage() + " please verify that player " + player.getName() + " is logged out.", ex);
        }
        else
        {
          logger.log(Level.INFO, "Logout - " + ex.getMessage(), ex);
        }
      }
      catch (NoSuchPlayerException localNoSuchPlayerException1) {}
    }
    if ((Servers.localServer.PVPSERVER) && (!Servers.isThisATestServer())) {
      if (!ip.isEmpty())
      {
        KingdomIp kip = KingdomIp.getKIP(ip, (byte)0);
        if (kip != null) {
          kip.logoff();
        }
      }
    }
    return player;
  }
  
  public void sendPAWindow(Player player)
  {
    String chan = getKingdomHelpChannelName(player.getKingdomId());
    if (chan.length() == 0) {
      return;
    }
    Message mess = new Message(player, (byte)12, chan, "<System> This is the Community Assistance window. Just type your questions here. To stop receiving these messages, manage your profile.");
    
    player.getCommunicator().sendMessage(mess);
    joinPAChannel(player);
  }
  
  public static String getKingdomHelpChannelName(byte kingdomId)
  {
    String chan = "";
    if (kingdomId == 4) {
      chan = "CA HELP";
    } else if (kingdomId == 1) {
      chan = "JK HELP";
    } else if (kingdomId == 2) {
      chan = "MR HELP";
    } else if (kingdomId == 3) {
      chan = "HOTS HELP";
    }
    return chan;
  }
  
  public void sendGVHelpWindow(Player player)
  {
    Message mess = new Message(player, (byte)12, "GV HELP", "<System> This is the GV Help window. just reply to questions here. To stop receiving these messages, manage your profile.");
    
    player.getCommunicator().sendMessage(mess);
  }
  
  void sendGmsToPlayer(Player player)
  {
    Message mess = new Message(player, (byte)9, "MGMT", "");
    player.getCommunicator().sendMessage(mess);
    if ((player.mayHearMgmtTalk()) || (player.mayHearDevTalk())) {
      sendToTabs(player, player.getPower() < 2, player.getPower() >= 2);
    }
    Iterator localIterator;
    if (player.mayHearMgmtTalk()) {
      for (localIterator = tabListMGMT.values().iterator(); localIterator.hasNext();)
      {
        tabData = (TabData)localIterator.next();
        if ((tabData.isVisible()) || (tabData.getPower() < 2)) {
          player.getCommunicator().sendAddMgmt(tabData.getName(), tabData.getWurmId());
        }
      }
    }
    TabData tabData;
    if (player.mayMute())
    {
      Message mess2 = new Message(player, (byte)11, "GM", "");
      player.getCommunicator().sendMessage(mess2);
      if (player.mayHearDevTalk()) {
        for (TabData tabData : tabListGM.values()) {
          if ((tabData.isVisible()) || (tabData.getPower() <= player.getPower())) {
            player.getCommunicator().sendAddGm(tabData.getName(), tabData.getWurmId());
          }
        }
      }
    }
  }
  
  void sendTicketsToPlayer(Player player)
  {
    Ticket[] tickets = Tickets.getTickets(player);
    for (Ticket t : tickets) {
      player.getCommunicator().sendTicket(t);
    }
  }
  
  public final void removeGlobalEffect(long id)
  {
    for (Player player : getPlayers()) {
      player.getCommunicator().sendRemoveEffect(id);
    }
  }
  
  void sendAltarsToPlayer(Player player)
  {
    for (EndGameItem eg : EndGameItems.altars.values())
    {
      Iterator localIterator2;
      if (eg.isHoly())
      {
        player.getCommunicator().sendAddEffect(eg.getWurmid(), (short)2, eg.getItem().getPosX(), eg
          .getItem().getPosY(), eg.getItem().getPosZ(), (byte)0);
        if (WurmCalendar.isChristmas())
        {
          if (Zones.santaMolRehan != null) {
            player.getCommunicator().sendAddEffect(Zones.santaMolRehan.getWurmId(), (short)4, Zones.santaMolRehan
              .getPosX(), Zones.santaMolRehan.getPosY(), Zones.santaMolRehan.getPositionZ(), (byte)0);
          }
          if (Zones.santa != null) {
            player.getCommunicator().sendAddEffect(Zones.santa.getWurmId(), (short)4, Zones.santa.getPosX(), Zones.santa
              .getPosY(), Zones.santa.getPositionZ(), (byte)0);
          }
          if ((Zones.santas != null) && (!Zones.santas.isEmpty())) {
            for (localIterator2 = Zones.santas.values().iterator(); localIterator2.hasNext();)
            {
              santa = (Creature)localIterator2.next();
              
              player.getCommunicator().sendAddEffect(santa.getWurmId(), (short)4, santa.getPosX(), santa
                .getPosY(), santa.getPositionZ(), (byte)0);
            }
          }
        }
      }
      else
      {
        player.getCommunicator().sendAddEffect(eg.getWurmid(), (short)3, eg.getItem().getPosX(), eg
          .getItem().getPosY(), eg.getItem().getPosZ(), (byte)0);
        if ((WurmCalendar.isChristmas()) && (Zones.evilsanta != null)) {
          player.getCommunicator().sendAddEffect(Zones.evilsanta.getWurmId(), (short)4, Zones.evilsanta
            .getPosX(), Zones.evilsanta.getPosY(), Zones.evilsanta.getPositionZ(), (byte)0);
        }
      }
    }
    if ((EndGameItems.altars == null) || (EndGameItems.altars.isEmpty())) {
      if (WurmCalendar.isChristmas())
      {
        if (Zones.santa != null) {
          player.getCommunicator().sendAddEffect(Zones.santa.getWurmId(), (short)4, Zones.santa.getPosX(), Zones.santa
            .getPosY(), Zones.santa.getPositionZ(), (byte)0);
        }
        if (Zones.santaMolRehan != null) {
          player.getCommunicator().sendAddEffect(Zones.santaMolRehan.getWurmId(), (short)4, Zones.santaMolRehan
            .getPosX(), Zones.santaMolRehan.getPosY(), Zones.santaMolRehan.getPositionZ(), (byte)0);
        }
        if (Zones.evilsanta != null) {
          player.getCommunicator().sendAddEffect(Zones.evilsanta.getWurmId(), (short)4, Zones.evilsanta
            .getPosX(), Zones.evilsanta.getPosY(), Zones.evilsanta.getPositionZ(), (byte)0);
        }
        if ((Zones.santas != null) && (!Zones.santas.isEmpty())) {
          for (??? = Zones.santas.values().iterator(); ???.hasNext();)
          {
            santa = (Creature)???.next();
            
            player.getCommunicator().sendAddEffect(santa.getWurmId(), (short)4, santa.getPosX(), santa
              .getPosY(), santa.getPositionZ(), (byte)0);
          }
        }
      }
    }
    if (Servers.localServer.isChallengeServer()) {
      if (challengeStep > 0)
      {
        player.getCommunicator().sendAddEffect(Long.MAX_VALUE - Server.rand.nextInt(100000), (short)20, 0.0F, 0.0F, 0.0F, (byte)0);
        if (challengeStep > 1) {
          player.getCommunicator().sendAddEffect(Long.MAX_VALUE - Server.rand.nextInt(100000), (short)21, 0.0F, 0.0F, 0.0F, (byte)0);
        }
        if (challengeStep > 2) {
          player.getCommunicator().sendAddEffect(Long.MAX_VALUE - Server.rand.nextInt(100000), (short)22, 0.0F, 0.0F, 0.0F, (byte)0);
        }
        if (challengeStep > 3) {
          player.getCommunicator().sendAddEffect(Long.MAX_VALUE - Server.rand.nextInt(100000), (short)23, 0.0F, 0.0F, 0.0F, (byte)0);
        }
      }
    }
    Effect[] effs = EffectFactory.getInstance().getAllEffects();
    Creature santa = effs;Creature localCreature1 = santa.length;
    for (Creature santa = 0; santa < localCreature1; santa++)
    {
      Effect effect = santa[santa];
      if (effect.isGlobal())
      {
        if (logger.isLoggable(Level.FINER)) {
          logger.finer(player.getName() + " Sending effect type " + effect.getType() + " at position (x,y,z) " + effect
            .getPosX() + ',' + effect.getPosY() + ',' + effect.getPosZ());
        }
        player.getCommunicator().sendAddEffect(effect.getOwner(), effect.getType(), effect
          .getPosX(), effect.getPosY(), effect.getPosZ(), (byte)0);
      }
    }
  }
  
  public void logoutPlayer(Player player)
  {
    if (player.hasLink())
    {
      try
      {
        player.getCommunicator().sendShutDown("You were logged out by the server.", false);
      }
      catch (Exception e)
      {
        if (logger.isLoggable(Level.FINEST)) {
          logger.log(Level.FINEST, "Could not send shutdown to " + player + " due to " + e.getMessage(), e);
        }
      }
      try
      {
        player.getCommunicator().disconnect();
      }
      catch (Exception e)
      {
        if (logger.isLoggable(Level.FINEST)) {
          logger.log(Level.FINEST, "Could not send disconnect to " + player + " due to " + e.getMessage(), e);
        }
      }
    }
    else
    {
      player.getCommunicator().disconnect();
    }
    sendConnectInfo(player, " left the world.", System.currentTimeMillis(), PlayerOnlineStatus.OFFLINE);
    removeFromGroups(player);
    setCreatureDead(player);
    Creatures.getInstance().setCreatureDead(player);
    player.logout();
    players.remove(player.getName());
    playersById.remove(Long.valueOf(player.getWurmId()));
    
    Server.getInstance().steamHandler.EndAuthSession(player.getSteamId().toString());
    if ((Servers.localServer.PVPSERVER) && (!Servers.isThisATestServer())) {
      if (player.getPower() < 1)
      {
        KingdomIp kip = KingdomIp.getKIP(player.getSaveFile().getIpaddress(), (byte)0);
        if (kip != null) {
          kip.logoff();
        }
      }
    }
  }
  
  final void removePlayer(Player player)
  {
    players.remove(player.getName());
    playersById.remove(Long.valueOf(player.getWurmId()));
  }
  
  final void addPlayer(Player player)
  {
    players.put(player.getName(), player);
    playersById.put(Long.valueOf(player.getWurmId()), player);
  }
  
  public Player[] getPlayers()
  {
    return (Player[])players.values().toArray(new Player[players.size()]);
  }
  
  public Map<String, Player> getPlayerMap()
  {
    return players;
  }
  
  String[] getPlayerNames()
  {
    String[] lReturn = (String[])players.keySet().toArray(new String[players.size()]);
    return lReturn;
  }
  
  public void sendEffect(short effType, float posx, float posy, float posz, boolean surfaced, float maxDistMeters)
  {
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr) {
      try
      {
        if ((lPlayer.getVisionArea() != null) && (lPlayer.isOnSurface() == surfaced)) {
          if (lPlayer.isWithinDistanceTo(posx, posy, posz, maxDistMeters)) {
            lPlayer.getCommunicator().sendAddEffect(WurmId.getNextTempItemId(), effType, posx, posy, posz, (byte)(surfaced ? 0 : -1));
          }
        }
      }
      catch (NullPointerException npe)
      {
        logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
        
        lPlayer.setLink(false);
      }
    }
  }
  
  public void sendChangedTile(@Nonnull TilePos tilePos, boolean surfaced, boolean destroyTrap)
  {
    sendChangedTile(tilePos.x, tilePos.y, surfaced, destroyTrap);
  }
  
  public void sendChangedTile(int tilex, int tiley, boolean surfaced, boolean destroyTrap)
  {
    Player[] playarr = getPlayers();
    byte tiletype;
    if (destroyTrap)
    {
      Trap t = Trap.getTrap(tilex, tiley, surfaced ? 0 : -1);
      if (t != null)
      {
        tiletype = Tiles.decodeType(Zones.getMesh(surfaced).getTile(tilex, tiley));
        if (!t.mayTrapRemainOnTile(tiletype)) {
          try
          {
            t.delete();
          }
          catch (IOException localIOException) {}
        }
      }
    }
    boolean nearRoad = isNearRoad(surfaced, tilex, tiley);
    if (surfaced)
    {
      tiletype = playarr;localIOException = tiletype.length;
      for (IOException localIOException2 = 0; localIOException2 < localIOException; localIOException2++)
      {
        Player lPlayer = tiletype[localIOException2];
        try
        {
          if ((lPlayer.getVisionArea() != null) && (lPlayer.getVisionArea().contains(tilex, tiley))) {
            if (lPlayer.getCommunicator() != null) {
              try
              {
                lPlayer.getMovementScheme().touchFreeMoveCounter();
                if (nearRoad) {
                  lPlayer.getCommunicator().sendTileStrip((short)(tilex - 1), (short)(tiley - 1), 3, 3);
                } else {
                  lPlayer.getCommunicator().sendTileStrip((short)tilex, (short)tiley, 1, 1);
                }
              }
              catch (IOException localIOException1) {}
            }
          }
        }
        catch (NullPointerException npe)
        {
          if (lPlayer == null)
          {
            logger.log(Level.INFO, "Null player detected. Ignoring for now.");
          }
          else
          {
            logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
            
            lPlayer.setLink(false);
          }
        }
      }
    }
    else
    {
      tiletype = playarr;localIOException = tiletype.length;
      for (IOException localIOException3 = 0; localIOException3 < localIOException; localIOException3++)
      {
        Player lPlayer = tiletype[localIOException3];
        try
        {
          if ((lPlayer.getVisionArea() != null) && (lPlayer.getVisionArea().containsCave(tilex, tiley)))
          {
            lPlayer.getMovementScheme().touchFreeMoveCounter();
            lPlayer.getCommunicator().sendCaveStrip((short)(tilex - 1), (short)(tiley - 1), 3, 3);
          }
        }
        catch (NullPointerException npe)
        {
          logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
          
          lPlayer.setLink(false);
        }
      }
    }
  }
  
  public void sendChangedTiles(int startX, int startY, int sizeX, int sizeY, boolean surfaced, boolean destroyTrap)
  {
    if (destroyTrap) {
      for (int x = 0; x < sizeX; x++) {
        for (int y = 0; y < sizeY; y++)
        {
          tempTileX = startX + x;
          tempTileY = startY + y;
          if (GeneralUtilities.isValidTileLocation(tempTileX, tempTileY))
          {
            t = Trap.getTrap(tempTileX, tempTileY, surfaced ? 0 : -1);
            if (t != null)
            {
              byte tiletype = Tiles.decodeType(Zones.getMesh(surfaced).getTile(tempTileX, tempTileY));
              if (!t.mayTrapRemainOnTile(tiletype)) {
                try
                {
                  t.delete();
                }
                catch (IOException localIOException) {}
              }
            }
          }
        }
      }
    }
    boolean nearRoad = (sizeX == 1) && (sizeY == 1) && (isNearRoad(surfaced, startX, startY));
    Player[] playarr = getPlayers();
    int tempTileX = playarr;int tempTileY = tempTileX.length;
    for (Trap t = 0; t < tempTileY; t++)
    {
      Player lPlayer = tempTileX[t];
      try
      {
        if (surfaced)
        {
          if (nearRoad)
          {
            if ((lPlayer.getVisionArea() != null) && (
              (lPlayer.getVisionArea().contains(startX, startY)) || 
              (lPlayer.getVisionArea().contains(startX, startY + sizeY)) || 
              (lPlayer.getVisionArea().contains(startX + sizeX, startY + sizeY)) || 
              (lPlayer.getVisionArea().contains(startX + sizeX, startY)))) {
              try
              {
                lPlayer.getCommunicator().sendTileStrip((short)(startX - 1), (short)(startY - 1), 3, 3);
              }
              catch (IOException localIOException1) {}
            }
          }
          else if ((lPlayer.getVisionArea() != null) && (
            (lPlayer.getVisionArea().contains(startX, startY)) || 
            (lPlayer.getVisionArea().contains(startX, startY + sizeY)) || 
            (lPlayer.getVisionArea().contains(startX + sizeX, startY + sizeY)) || 
            (lPlayer.getVisionArea().contains(startX + sizeX, startY)))) {
            try
            {
              lPlayer.getCommunicator().sendTileStrip((short)startX, (short)startY, sizeX, sizeY);
            }
            catch (IOException localIOException2) {}
          }
        }
        else if (lPlayer.isNearCave()) {
          for (int xx = startX; xx < startX + sizeX; xx++) {
            for (int yy = startY; yy < startY + sizeY; yy++) {
              if ((lPlayer.getVisionArea() != null) && (lPlayer.getVisionArea().containsCave(xx, yy))) {
                lPlayer.getCommunicator().sendCaveStrip((short)xx, (short)yy, 1, 1);
              }
            }
          }
        }
      }
      catch (NullPointerException npe)
      {
        logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
        
        lPlayer.setLink(false);
      }
    }
  }
  
  private boolean isNearRoad(boolean surfaced, int tilex, int tiley)
  {
    try
    {
      if (surfaced) {
        for (int x = -1; x <= 1; x++) {
          for (int y = -1; y <= 1; y++) {
            if ((GeneralUtilities.isValidTileLocation(tilex + x, tiley + y)) && 
              (Tiles.isRoadType(Server.surfaceMesh.getTile(tilex + x, tiley + y)))) {
              return true;
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "****** Oops invalid x,y " + tilex + "," + tiley + ".");
    }
    return false;
  }
  
  void savePlayersAtShutdown()
  {
    logger.info("Saving Players");
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr)
    {
      if (lPlayer.getDraggedItem() != null) {
        Items.stopDragging(lPlayer.getDraggedItem());
      }
      try
      {
        lPlayer.sleep();
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
    logger.info("Finished saving Players");
  }
  
  public String getNameFor(long playerId)
    throws NoSuchPlayerException, IOException
  {
    Long pid = Long.valueOf(playerId);
    Player p = getPlayerById(pid);
    if (p != null) {
      return p.getName();
    }
    PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
    if (info != null) {
      return info.getName();
    }
    PlayerState pState = PlayerInfoFactory.getPlayerState(playerId);
    if (pState != null) {
      return pState.getPlayerName();
    }
    return DbSearcher.getNameForPlayer(playerId);
  }
  
  public long getWurmIdFor(String name)
    throws NoSuchPlayerException, IOException
  {
    PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
    if (info.loaded) {
      return info.wurmId;
    }
    return DbSearcher.getWurmIdForPlayer(name);
  }
  
  private void loadBannedIps()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement(IPBan.getSelectSql());
      rs = ps.executeQuery();
      while (rs.next())
      {
        String ip = rs.getString("IPADDRESS");
        String reason = rs.getString("BANREASON");
        long expiry = rs.getLong("BANEXPIRY");
        Ban bip = new IPBan(ip, reason, expiry);
        if (!bip.isExpired()) {
          bans.add(bip);
        } else {
          removeBan(bip);
        }
      }
      logger.info("Loaded " + bans.size() + " banned IPs");
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to load banned ips.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private void loadBannedSteamIds()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement(SteamIdBan.getSelectSql());
      rs = ps.executeQuery();
      while (rs.next())
      {
        String identifier = rs.getString("STEAM_ID");
        String reason = rs.getString("BANREASON");
        long expiry = rs.getLong("BANEXPIRY");
        Ban bip = new SteamIdBan(SteamId.fromSteamID64(Long.valueOf(identifier).longValue()), reason, expiry);
        if (!bip.isExpired()) {
          bans.add(bip);
        } else {
          removeBan(bip);
        }
      }
      logger.info("Loaded " + bans.size() + " more bans from steamids");
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to load banned steamids.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public int getNumberOfPlayers()
  {
    return players.size();
  }
  
  public void addBannedIp(String ip, String reason, long expiry)
  {
    Ban ban = new IPBan(ip, reason, expiry);
    addBan(ban);
  }
  
  public void addBan(Ban ban)
  {
    if ((ban == null) || (ban.getIdentifier() == null) || (ban.getIdentifier().isEmpty()))
    {
      logger.warning("Cannot add a null ban");
      return;
    }
    Ban bip = getBannedIp(ban.getIdentifier());
    if (bip == null)
    {
      bans.add(ban);
      
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement(ban.getInsertSql());
        ps.setString(1, ban.getIdentifier());
        ps.setString(2, ban.getReason());
        ps.setLong(3, ban.getExpiry());
        ps.executeUpdate();
      }
      catch (SQLException sqex)
      {
        logger.log(Level.WARNING, "Failed to add ban " + ban.getIdentifier(), sqex);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    else
    {
      bip.setReason(ban.getReason());
      bip.setExpiry(ban.getExpiry());
      
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement(bip.getUpdateSql());
        ps.setString(1, bip.getReason());
        ps.setLong(2, bip.getExpiry());
        ps.setString(3, bip.getIdentifier());
        ps.executeUpdate();
      }
      catch (SQLException sqex)
      {
        logger.log(Level.WARNING, "Failed to update ban " + bip.getIdentifier(), sqex);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public boolean removeBan(String identifier)
  {
    Ban existing = null;
    for (Ban lBip : bans) {
      if (lBip.getIdentifier().equals(identifier)) {
        existing = lBip;
      } else if ((identifier.contains("*")) && (lBip.getIdentifier().startsWith(identifier))) {
        existing = lBip;
      }
    }
    if (existing == null) {
      existing = Ban.fromString(identifier);
    }
    return removeBan(existing);
  }
  
  public boolean removeBan(Ban ban)
  {
    bans.remove(ban);
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement(ban.getDeleteSql());
      ps.setString(1, ban.getIdentifier());
      ps.executeUpdate();
      return true;
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to remove ban " + ban.getIdentifier(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    return false;
  }
  
  public final void tickSecond()
  {
    for (Player p : players.values()) {
      if ((p.getSaveFile() != null) && (p.getSaveFile().sleep > 0) && (!p.getSaveFile().frozenSleep))
      {
        float chance = p.getStatus().getFats() / 3.0F;
        if (Server.rand.nextFloat() >= chance) {
          p.getSaveFile().sleep -= 1;
        }
      }
    }
  }
  
  public Ban[] getPlayersBanned()
  {
    Set<Ban> banned = new HashSet();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME,BANREASON,BANEXPIRY FROM PLAYERS WHERE BANNED=1");
      rs = ps.executeQuery();
      while (rs.next())
      {
        String ip = rs.getString("NAME");
        String reason = rs.getString("BANREASON");
        long expiry = rs.getLong("BANEXPIRY");
        if (expiry > System.currentTimeMillis()) {
          banned.add(new IPBan(ip, reason, expiry));
        }
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to get players banned.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return (Ban[])banned.toArray(new Ban[banned.size()]);
  }
  
  public void sendGmMessage(Creature sender, String playerName, String message, boolean emote, int red, int green, int blue)
  {
    Message mess = null;
    if (emote) {
      mess = new Message(sender, (byte)6, "GM", message);
    } else {
      mess = new Message(sender, (byte)11, "GM", "<" + playerName + "> " + message, red, green, blue);
    }
    addGmMessage(playerName, message);
    
    Player[] playerArr = getInstance().getPlayers();
    for (Player lPlayer : playerArr) {
      if (lPlayer.mayHearDevTalk())
      {
        if (sender == null) {
          mess.setSender(lPlayer);
        }
        lPlayer.getCommunicator().sendMessage(mess);
      }
    }
  }
  
  public void sendGmMessage(Creature sender, String playerName, String message, boolean emote)
  {
    sendGmMessage(sender, playerName, message, emote, -1, -1, -1);
  }
  
  public void sendGlobalKingdomMessage(Creature sender, long senderId, String playerName, String message, boolean emote, byte kingdom, int r, int g, int b)
  {
    Message mess = null;
    
    mess = new Message(sender, (byte)16, "GL-" + Kingdoms.getChatNameFor(kingdom), "<" + playerName + "> " + message);
    
    mess.setSenderKingdom(kingdom);
    mess.setSenderId(senderId);
    mess.setColorR(r);
    mess.setColorG(g);
    mess.setColorB(b);
    
    Server.getInstance().addMessage(mess);
  }
  
  public void sendGlobalTradeMessage(Creature sender, long senderId, String playerName, String message, byte kingdom, int r, int g, int b)
  {
    Message mess = null;
    mess = new Message(sender, (byte)18, "Trade", "<" + playerName + "> " + message);
    
    mess.setSenderKingdom(kingdom);
    mess.setSenderId(senderId);
    mess.setColorR(r);
    mess.setColorG(g);
    mess.setColorB(b);
    Server.getInstance().addMessage(mess);
  }
  
  public void partPAChannel(Player player)
  {
    if (!player.seesPlayerAssistantWindow())
    {
      Player[] playerArr = getInstance().getPlayers();
      for (Player lPlayer : playerArr) {
        if ((lPlayer.getSaveFile() != null) && (lPlayer.seesPlayerAssistantWindow())) {
          lPlayer.getCommunicator().sendRemovePa(player.getName());
        }
      }
    }
  }
  
  public void joinPAChannel(Player player)
  {
    Player[] playerArr = getInstance().getPlayers();
    for (Player lPlayer : playerArr) {
      if ((lPlayer.getSaveFile() != null) && (lPlayer.seesPlayerAssistantWindow()) && (player.isVisibleTo(lPlayer))) {
        if (player.isPlayerAssistant()) {
          lPlayer.getCommunicator().sendAddPa(" CA " + player.getName(), player.getWurmId());
        } else if ((shouldReceivePlayerList(lPlayer)) && (player.getPower() < 2)) {
          lPlayer.getCommunicator().sendAddPa(player.getName(), player.getWurmId());
        }
      }
    }
  }
  
  public void partChannels(Player player)
  {
    boolean mayDev = player.mayHearDevTalk();
    boolean mayMgmt = player.mayHearMgmtTalk();
    boolean mayHelp = player.seesPlayerAssistantWindow();
    if ((!mayDev) && (!mayMgmt) && (!mayHelp)) {
      return;
    }
    String playerName = player.getName();
    if ((mayDev) || (mayMgmt))
    {
      removeFromTabs(player.getWurmId(), playerName);
      sendRemoveFromTabs(player.getWurmId(), playerName);
    }
    for (Player otherPlayer : getInstance().getPlayers()) {
      if (!player.isVisibleTo(otherPlayer)) {
        if ((mayHelp) && (otherPlayer.seesPlayerAssistantWindow())) {
          if (player.isPlayerAssistant()) {
            otherPlayer.getCommunicator().sendRemovePa(" CA " + playerName);
          } else if (shouldReceivePlayerList(otherPlayer)) {
            otherPlayer.getCommunicator().sendRemovePa(playerName);
          }
        }
      }
    }
  }
  
  public void joinChannels(Player player)
  {
    boolean mayDev = player.mayHearDevTalk();
    boolean mayMgmt = player.mayHearMgmtTalk();
    boolean mayHelp = player.seesPlayerAssistantWindow();
    if ((!mayDev) && (!mayMgmt) && (!mayHelp)) {
      return;
    }
    long playerId = player.getWurmId();
    String playerName = player.getName();
    if ((mayDev) || (mayMgmt)) {
      sendToTabs(player, player.getPower() < 2, player.getPower() >= 2);
    }
    for (Player otherPlayer : getInstance().getPlayers()) {
      if ((player.isVisibleTo(otherPlayer)) && (player != otherPlayer)) {
        if ((mayHelp) && (otherPlayer.seesPlayerAssistantWindow())) {
          if (player.isPlayerAssistant()) {
            otherPlayer.getCommunicator().sendAddPa(" CA " + playerName, playerId);
          } else if ((shouldReceivePlayerList(otherPlayer)) && (player.getPower() < 2)) {
            otherPlayer.getCommunicator().sendAddPa(playerName, playerId);
          }
        }
      }
    }
  }
  
  public void sendPaMessage(Message mes)
  {
    caHelpLogger.info(mes.getMessage());
    Player[] playerArr = getInstance().getPlayers();
    for (Player lPlayer : playerArr) {
      if (lPlayer.seesPlayerAssistantWindow()) {
        lPlayer.getCommunicator().sendMessage(mes);
      }
    }
  }
  
  public void sendGVMessage(Message mes)
  {
    caHelpLogger.info(mes.getMessage());
    Player[] playerArr = getInstance().getPlayers();
    for (Player lPlayer : playerArr) {
      if (lPlayer.seesGVHelpWindow()) {
        lPlayer.getCommunicator().sendMessage(mes);
      }
    }
  }
  
  public void sendCaMessage(byte kingdom, Message mes)
  {
    caHelpLogger.info(mes.getMessage());
    Player[] playerArr = getInstance().getPlayers();
    for (Player lPlayer : playerArr) {
      if ((lPlayer.seesPlayerAssistantWindow()) && ((lPlayer.getKingdomId() == kingdom) || (lPlayer.getPower() >= 2))) {
        lPlayer.getCommunicator().sendMessage(mes);
      }
    }
  }
  
  public String[] getMuters()
  {
    Set<String> muted = new HashSet();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE MAYMUTE=1");
      rs = ps.executeQuery();
      while (rs.next())
      {
        String name = rs.getString("NAME");
        muted.add(name);
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to get muters.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return (String[])muted.toArray(new String[muted.size()]);
  }
  
  public String[] getDevTalkers()
  {
    Set<String> devTalkers = new HashSet();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE DEVTALK=1");
      rs = ps.executeQuery();
      while (rs.next())
      {
        String name = rs.getString("NAME");
        devTalkers.add(name);
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to get dev talkers.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return (String[])devTalkers.toArray(new String[devTalkers.size()]);
  }
  
  public String[] getCAs()
  {
    Set<String> pas = new HashSet();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE PA=1");
      rs = ps.executeQuery();
      while (rs.next())
      {
        String name = rs.getString("NAME");
        pas.add(name);
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to get pas.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return (String[])pas.toArray(new String[pas.size()]);
  }
  
  public String[] getHeros(byte checkPower)
  {
    Set<String> heros = new HashSet();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE POWER=? AND CURRENTSERVER=?");
      ps.setByte(1, checkPower);
      ps.setInt(2, Servers.localServer.getId());
      rs = ps.executeQuery();
      while (rs.next())
      {
        String name = rs.getString("NAME");
        heros.add(name);
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to get heros.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return (String[])heros.toArray(new String[heros.size()]);
  }
  
  public Ban[] getPlayersMuted()
  {
    Set<Ban> muted = new HashSet();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME,MUTEREASON,MUTEEXPIRY FROM PLAYERS WHERE MUTED=1");
      rs = ps.executeQuery();
      while (rs.next())
      {
        String ip = rs.getString("NAME");
        String reason = rs.getString("MUTEREASON");
        long expiry = rs.getLong("MUTEEXPIRY");
        muted.add(new IPBan(ip, reason, expiry));
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to get players muted.", sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return (Ban[])muted.toArray(new Ban[muted.size()]);
  }
  
  public Ban getAnyBan(String ip, Player player, String steamId)
  {
    Ban ban = player.getBan();
    if (ban == null) {
      ban = getBannedIp(ip);
    }
    if (ban == null) {
      ban = getBannedSteamId(steamId);
    }
    return ban;
  }
  
  public Ban getBannedSteamId(String steamId)
  {
    if (steamId.isEmpty()) {
      return null;
    }
    Ban[] banArr = (Ban[])bans.toArray(new Ban[0]);
    for (Ban ban : banArr) {
      if ((ban != null) && 
        (ban.getIdentifier().equals(steamId))) {
        if (ban.isExpired()) {
          removeBan(ban);
        } else {
          return ban;
        }
      }
    }
    return null;
  }
  
  public Ban getBannedIp(String ip)
  {
    if (ip.isEmpty()) {
      return null;
    }
    Ban[] bips = (Ban[])bans.toArray(new Ban[0]);
    int dots = 0;
    for (Ban lBip : bips)
    {
      if (lBip == null)
      {
        logger.warning("BannedIPs includes a null");
        return null;
      }
      dots = lBip.getIdentifier().indexOf("*");
      if (dots > 0) {
        if (lBip.isExpired()) {
          removeBan(lBip.getIdentifier());
        } else if (lBip.getIdentifier().substring(0, dots).equals(ip.substring(0, dots))) {
          return lBip;
        }
      }
      if (lBip.getIdentifier().equals(ip)) {
        if (lBip.isExpired()) {
          removeBan(lBip.getIdentifier());
        } else {
          return lBip;
        }
      }
    }
    return null;
  }
  
  public Ban[] getBans()
  {
    Ban[] bips = (Ban[])bans.toArray(new Ban[bans.size()]);
    Arrays.sort(bips, new Comparator()
    {
      public int compare(Ban o1, Ban o2)
      {
        Ban i1 = o1;
        Ban i2 = o2;
        return i1.getIdentifier().compareTo(i2.getIdentifier());
      }
    });
    return bips;
  }
  
  public void convertFromKingdomToKingdom(byte oldKingdom, byte newKingdom)
  {
    Player[] playerArr = getPlayers();
    for (Player play : playerArr) {
      if (play.getKingdomId() == oldKingdom) {
        try
        {
          play.setKingdomId(newKingdom, true);
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, iox.getMessage(), iox);
        }
      }
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE PLAYERS SET KINGDOM=? WHERE KINGDOM=?");
      ps.setByte(1, newKingdom);
      ps.setByte(2, oldKingdom);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to change kingdom to " + newKingdom, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static void convertPlayerToKingdom(long wurmId, byte newKingdom)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE PLAYERS SET KINGDOM=? WHERE WURMID=?");
      ps.setByte(1, newKingdom);
      ps.setLong(2, wurmId);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to change kingdom to " + newKingdom + " for " + wurmId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public long getLastLogoutForPlayer(long wurmid)
  {
    long toReturn = 0L;
    if (getPlayerById(wurmid) != null)
    {
      toReturn = System.currentTimeMillis();
    }
    else
    {
      PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
      if (pinf != null) {
        return pinf.lastLogout;
      }
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT LASTLOGOUT FROM PLAYERS WHERE WURMID=?");
      ps.setLong(1, wurmid);
      rs = ps.executeQuery();
      if (rs.next()) {
        toReturn = rs.getLong("LASTLOGOUT");
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to retrieve lastlogout for " + wurmid, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return toReturn;
  }
  
  public boolean doesPlayerNameExist(String name)
  {
    if (getPlayerByName(name) != null) {
      return true;
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT WURMID FROM PLAYERS WHERE NAME=?");
      ps.setString(1, name);
      rs = ps.executeQuery();
      if (rs.next()) {
        return true;
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to check if " + name + " exists:" + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return false;
  }
  
  public long getWurmIdByPlayerName(String name)
  {
    String lName = LoginHandler.raiseFirstLetter(name);
    if (getPlayerByName(lName) != null) {
      return getPlayerByName(lName).getWurmId();
    }
    PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(lName);
    if (pinf.wurmId > 0L) {
      return pinf.wurmId;
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT WURMID FROM PLAYERS WHERE NAME=?");
      ps.setString(1, lName);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getLong("WURMID");
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to retrieve wurmid for " + name + " exists:" + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return -1L;
  }
  
  public void registerNewKingdom(Creature registered)
  {
    registerNewKingdom(registered.getWurmId(), registered.getKingdomId());
  }
  
  public void pollChamps()
  {
    if (System.currentTimeMillis() - Servers.localServer.lastDecreasedChampionPoints > 604800000L)
    {
      Servers.localServer.setChampStamp();
      PlayerInfo[] playinfos = PlayerInfoFactory.getPlayerInfos();
      for (int p = 0; p < playinfos.length; p++) {
        if ((playinfos[p].realdeath > 0) && (playinfos[p].realdeath < 5)) {
          try
          {
            Player play = getInstance().getPlayer(playinfos[p].wurmId);
            play.sendAddChampionPoints();
          }
          catch (NoSuchPlayerException localNoSuchPlayerException) {}
        }
      }
    }
    printChampStats();
  }
  
  public void registerNewKingdom(long aWurmId, byte aKingdom)
  {
    this.pkingdoms.put(Long.valueOf(aWurmId), Byte.valueOf(aKingdom));
  }
  
  public byte getKingdomForPlayer(long wurmid)
  {
    Byte b = (Byte)this.pkingdoms.get(Long.valueOf(wurmid));
    if (b != null) {
      return b.byteValue();
    }
    Player lPlayerById = getPlayerById(wurmid);
    if (lPlayerById != null)
    {
      registerNewKingdom(wurmid, lPlayerById.getKingdomId());
      return lPlayerById.getKingdomId();
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT KINGDOM FROM PLAYERS WHERE WURMID=?");
      ps.setLong(1, wurmid);
      rs = ps.executeQuery();
      if (rs.next())
      {
        byte toret = rs.getByte("KINGDOM");
        this.pkingdoms.put(Long.valueOf(wurmid), Byte.valueOf(toret));
        return toret;
      }
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to retrieve kingdom for " + wurmid, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return 0;
  }
  
  public int getPlayersFromKingdom(byte kingdomId)
  {
    int nums = 0;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND CURRENTSERVER=? AND POWER=0");
      ps.setByte(1, kingdomId);
      ps.setInt(2, Servers.localServer.id);
      rs = ps.executeQuery();
      rs.last();
      return rs.getRow();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return 0;
  }
  
  public static int getChampionsFromKingdom(byte kingdomId)
  {
    int nums = 0;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME,WURMID,REALDEATH,LASTLOSTCHAMPION FROM PLAYERS WHERE KINGDOM=? AND REALDEATH>0 AND REALDEATH<4 AND POWER=0");
      ps.setByte(1, kingdomId);
      rs = ps.executeQuery();
      long wid;
      while (rs.next())
      {
        wid = rs.getLong("WURMID");
        
        String name = rs.getString("NAME");
        long lastChamped = rs.getLong("LASTLOSTCHAMPION");
        int realDeath = rs.getInt("REALDEATH");
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid);
        if (pinf.getCurrentServer() == Servers.localServer.id) {
          if (System.currentTimeMillis() - pinf.championTimeStamp < 14515200000L) {
            nums++;
          }
        }
      }
      return nums;
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return nums;
  }
  
  public static int getChampionsFromKingdom(byte kingdomId, int deity)
  {
    int nums = 0;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME,WURMID,REALDEATH,LASTLOSTCHAMPION FROM PLAYERS WHERE KINGDOM=? AND REALDEATH>0 AND REALDEATH<4 AND POWER=0");
      ps.setByte(1, kingdomId);
      rs = ps.executeQuery();
      long wid;
      while (rs.next())
      {
        wid = rs.getLong("WURMID");
        
        String name = rs.getString("NAME");
        long lastChamped = rs.getLong("LASTLOSTCHAMPION");
        int realDeath = rs.getInt("REALDEATH");
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid);
        if (pinf.getCurrentServer() == Servers.localServer.id) {
          if (System.currentTimeMillis() - pinf.championTimeStamp < 14515200000L) {
            if ((pinf.getDeity() != null) && (pinf.getDeity().getNumber() == deity)) {
              nums++;
            }
          }
        }
      }
      logger.log(Level.INFO, "Found " + nums + " champs for kingdom =" + kingdomId);
      return nums;
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return nums;
  }
  
  public static int getPremiumPlayersFromKingdom(byte kingdomId)
  {
    int nums = 0;
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND PAYMENTEXPIRE>? AND POWER=0");
      ps.setByte(1, kingdomId);
      ps.setLong(2, System.currentTimeMillis());
      rs = ps.executeQuery();
      long wid;
      while (rs.next())
      {
        wid = rs.getLong("WURMID");
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid);
        if ((pinf.getCurrentServer() == Servers.localServer.id) || 
          (System.currentTimeMillis() - pinf.getLastLogout() < 259200000L)) {
          nums++;
        }
      }
      return nums;
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return nums;
  }
  
  public void setStructureFinished(long structureid)
  {
    Player[] playarr = getPlayers();
    boolean found = false;
    for (Player lPlayer : playarr) {
      try
      {
        if (lPlayer.getStructure().getWurmId() == structureid) {
          try
          {
            lPlayer.setStructure(null);
            lPlayer.save();
            found = true;
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, "Failed to set structure finished for " + lPlayer, ex);
          }
        }
      }
      catch (NoSuchStructureException localNoSuchStructureException) {}
    }
    if (!found)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("update PLAYERS set BUILDINGID=-10 WHERE BUILDINGID=?");
        ps.setLong(1, structureid);
        ps.executeUpdate();
      }
      catch (SQLException sqex)
      {
        logger.log(Level.WARNING, "Failed to set buidlingid to -10 for " + structureid, sqex);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public static void resetFaithGain()
  {
    PlayerInfoFactory.resetFaithGain();
    
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE PLAYERS SET LASTFAITH=0,NUMFAITH=0");
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem resetting faith gain - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void payGms()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE PLAYERS SET MONEY=MONEY+250000 WHERE POWER>1");
      
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem processing GM Salary - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr) {
      if (lPlayer.getPower() > 0) {
        lPlayer.getCommunicator().sendSafeServerMessage("You have now received salary.");
      }
    }
  }
  
  public void resetPlayer(long wurmid)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE SKILLS SET VALUE=20, MINVALUE=20 WHERE VALUE>20 AND OWNER=?");
      ps.setLong(1, wurmid);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem resetting player skills - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("UPDATE PLAYERS SET FAITH=20 WHERE FAITH>20 AND WURMID=?");
      ps.setLong(1, wurmid);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem resetting player faith - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
    try
    {
      Player p = getPlayer(wurmid);
      try
      {
        if (p.isChampion())
        {
          p.revertChamp();
          if (p.getFaith() > 20.0F) {
            p.setFaith(20.0F);
          }
        }
      }
      catch (IOException localIOException2) {}
      Skills sk = p.getSkills();
      Skill[] skills = sk.getSkills();
      for (int x = 0; x < skills.length; x++) {
        if (skills[x].getKnowledge() > 20.0D)
        {
          skills[x].minimum = 20.0D;
          skills[x].setKnowledge(20.0D, true);
        }
      }
    }
    catch (NoSuchPlayerException nsp)
    {
      PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
      if (p != null) {
        try
        {
          p.setRealDeath((byte)0);
        }
        catch (IOException localIOException3) {}
      }
    }
  }
  
  public static void sendGmMessages(Player player)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT TIME,SENDER,MESSAGE FROM GMMESSAGES ORDER BY TIME");
      rs = ps.executeQuery();
      while (rs.next()) {
        player.getCommunicator().sendGmMessage(rs.getLong("TIME"), rs.getString("SENDER"), rs.getString("MESSAGE"));
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem getting GM messages - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    pruneMessages();
  }
  
  public static void sendMgmtMessages(Player player)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT TIME,SENDER,MESSAGE FROM MGMTMESSAGES ORDER BY TIME");
      rs = ps.executeQuery();
      while (rs.next()) {
        player.getCommunicator().sendMgmtMessage(rs.getLong("TIME"), rs.getString("SENDER"), rs.getString("MESSAGE"));
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem getting management messages - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    pruneMessages();
  }
  
  public void sendStartKingdomChat(Player player)
  {
    if (player.showKingdomStartMessage())
    {
      Message mess = new Message(player, (byte)10, Kingdoms.getChatNameFor(player.getKingdomId()), "<System> This is the Kingdom Chat for your current server. ", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess);
      
      Message mess1 = new Message(player, (byte)10, Kingdoms.getChatNameFor(player.getKingdomId()), "<System> You can disable receiving these messages, by a setting in your profile.", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess1);
    }
  }
  
  public void sendStartGlobalKingdomChat(Player player)
  {
    if (player.showGlobalKingdomStartMessage())
    {
      Message mess = new Message(player, (byte)16, "GL-" + Kingdoms.getChatNameFor(player.getKingdomId()), "<System> This is your Global Kingdom Chat. ", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess);
      
      Message mess1 = new Message(player, (byte)16, "GL-" + Kingdoms.getChatNameFor(player.getKingdomId()), "<System> You can disable receiving these messages, by a setting in your profile.", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess1);
    }
  }
  
  public void sendStartGlobalTradeChannel(Player player)
  {
    if (player.showTradeStartMessage())
    {
      Message mess = new Message(player, (byte)18, "Trade", "<System> This is the Trade channel. ", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess);
      Message mess1 = new Message(player, (byte)18, "Trade", "<System> Only messages starting with WTB, WTS, WTT, PC or @ are allowed. ", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess1);
      Message mess2 = new Message(player, (byte)18, "Trade", "<System> Please PM the person if you are interested in the Item.", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess2);
      Message mess3 = new Message(player, (byte)18, "Trade", "<System> You can also use @<name> to send a reply in this channel to <name>.", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess3);
      Message mess4 = new Message(player, (byte)18, "Trade", "<System> You can disable receiving these messages, by a setting in your profile.", 250, 150, 250);
      
      player.getCommunicator().sendMessage(mess4);
    }
  }
  
  public static Map<String, Integer> getBattleRanks(int num)
  {
    Map<String, Integer> toReturn = new HashMap();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?");
      ps.setInt(1, num);
      rs = ps.executeQuery();
      while (rs.next()) {
        toReturn.put(rs.getString("NAME"), Integer.valueOf(rs.getInt("RANK")));
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem getting battle ranks - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return toReturn;
  }
  
  public static Map<String, Integer> getMaxBattleRanks(int num)
  {
    Map<String, Integer> toReturn = new HashMap();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("select MAXRANK,RANK, NAME from PLAYERS ORDER BY MAXRANK DESC LIMIT ?");
      ps.setInt(1, num);
      rs = ps.executeQuery();
      while (rs.next()) {
        toReturn.put(rs.getString("NAME"), Integer.valueOf(rs.getInt("MAXRANK")));
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem getting Max battle ranks - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return toReturn;
  }
  
  public static void printMaxRanks()
  {
    Writer output = null;
    try
    {
      String dir = Constants.webPath;
      if (!dir.endsWith(File.separator)) {
        dir = dir + File.separator;
      }
      File aFile = new File(dir + "maxranks.html");
      
      output = new BufferedWriter(new FileWriter(aFile));
      String start = "<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>";
      try
      {
        output.write("<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>");
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
      int nums = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("select MAXRANK,RANK, NAME from PLAYERS ORDER BY MAXRANK DESC LIMIT ?");
        ps.setInt(1, 30);
        rs = ps.executeQuery();
        while (rs.next())
        {
          if (nums < 10) {
            output.write("<TR class=\"gameDataTopTenTR\"><TD class=\"gameDataTopTenTDName\">" + rs
              .getString("NAME") + "</TD><TD class=\"gameDataTopTenTDValue\">" + rs
              .getInt("MAXRANK") + "</TD></TR>");
          } else {
            output.write("<TR class=\"gameDataTR\"><TD class=\"gameDataTDName\">" + rs.getString("NAME") + "</TD><TD class=\"gameDataTDValue\">" + rs
              .getInt("MAXRANK") + "</TD></TR>");
          }
          nums++;
        }
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Problem writing maxranks" + sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
      output.write("</TABLE>"); return;
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save maxranks.html", iox);
    }
    finally
    {
      try
      {
        if (output != null) {
          output.close();
        }
      }
      catch (IOException localIOException3) {}
    }
  }
  
  private static String header2 = "<HTML>\n\t<HEAD>\n\t<TITLE>Wurm Online battle ranks</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
  private static final String footer2 = "\n</BODY>\n</HTML>";
  private static String headerStats = "<!DOCTYPE html> <HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"> <TITLE>Wurm Online Server Stats</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
  private static String headerStats2 = "<!DOCTYPE html> <HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"> <TITLE>Wurm Online Champion Eternal Records</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
  private static final String footerStats = "\n</BODY>\n</HTML>";
  
  public static void printRanks()
  {
    printMaxRanks();
    Writer output = null;
    try
    {
      String dir = Constants.webPath;
      if (!dir.endsWith(File.separator)) {
        dir = dir + File.separator;
      }
      File aFile = new File(dir + "ranks.html");
      
      output = new BufferedWriter(new FileWriter(aFile));
      output.write(header2);
      String start = "<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Rank</TH>\n\t\t</TR>\n\t\t";
      try
      {
        output.write("<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Rank</TH>\n\t\t</TR>\n\t\t");
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
      int nums = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?");
        ps.setInt(1, 30);
        rs = ps.executeQuery();
        while (rs.next())
        {
          if (nums < 10) {
            output.write("<TR class=\"gameDataTopTenTR\">\n\t\t\t<TD class=\"gameDataTopTenTDName\">" + rs
              .getString("NAME") + "</TD>\n\t\t\t<TD class=\"gameDataTopTenTDValue\">" + rs
              .getInt("RANK") + "</TD>\n\t\t</TR>\n\t\t");
          } else {
            output.write("<TR class=\"gameDataTR\">\n\t\t\t<TD class=\"gameDataTDName\">" + rs.getString("NAME") + "</TD>\n\t\t\t<TD class=\"gameDataTDValue\">" + rs
              .getInt("RANK") + "</TD>\n\t\t</TR>\n\n\t");
          }
          nums++;
        }
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
      output.write("</TABLE>\n");
      
      output.write("\n</BODY>\n</HTML>"); return;
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to close ranks.html", iox);
    }
    finally
    {
      try
      {
        if (output != null) {
          output.close();
        }
      }
      catch (IOException localIOException3) {}
    }
  }
  
  public static void printChampStats()
  {
    WurmRecord[] alls = PlayerInfoFactory.getChampionRecords();
    if (alls.length > 0)
    {
      Writer output = null;
      try
      {
        String dir = Constants.webPath;
        if (!dir.endsWith(File.separator)) {
          dir = dir + File.separator;
        }
        File aFile = new File(dir + "champs.html");
        
        output = new BufferedWriter(new FileWriter(aFile));
        try
        {
          output.write(headerStats2);
          String start = "<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t";
          output.write("<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t");
          
          int total = 0;
          int totalLimit = 0;
          for (WurmRecord entry : alls)
          {
            output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">" + entry.getHolder() + " players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + entry
            
              .getValue() + " current=" + entry.isCurrent() + "</TD>\n\t\t</TR>\n\t\t");
            total += entry.getValue();
            totalLimit++;
          }
          output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Average points</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + total + "/" + totalLimit + "=" + total / totalLimit + "</TD>\n\t\t</TR>\n\t\t");
          
          output.write("</TABLE>\n");
          output.write("\n</BODY>\n</HTML>");
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, "Problem writing server stats = " + iox.getMessage(), iox);
        }
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to open stats.html", iox);
      }
      finally
      {
        try
        {
          if (output != null) {
            output.close();
          }
        }
        catch (IOException localIOException3) {}
      }
    }
  }
  
  public static void printStats()
  {
    try
    {
      String dir = Constants.webPath;
      if (!dir.endsWith(File.separator)) {
        dir = dir + File.separator;
      }
      File aFile = new File(dir + "stats.xml");
      StatsXMLWriter.createXML(aFile);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    Writer output = null;
    try
    {
      String dir = Constants.webPath;
      if (!dir.endsWith(File.separator)) {
        dir = dir + File.separator;
      }
      File aFile = new File(dir + "stats.html");
      
      output = new BufferedWriter(new FileWriter(aFile));
      try
      {
        output.write(headerStats);
        String start = "<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t";
        output.write("<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t");
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Server name</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + Servers.localServer
          .getName() + "</TD>\n\t\t</TR>\n\t\t");
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Last updated</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + 
          DateFormat.getDateInstance(2).format(new Timestamp(System.currentTimeMillis())) + "</TD>\n\t\t</TR>\n\t\t");
        
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Status</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + (
        
          Server.getMillisToShutDown() > 0L ? "Shutting down in " + 
          Server.getMillisToShutDown() / 1000L + " seconds" : Servers.localServer.maintaining ? "Maintenance" : "Up and running") + "</TD>\n\t\t</TR>\n\t\t");
        
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Uptime</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + 
          Server.getTimeFor(Server.getSecondsUptime() * 1000) + "</TD>\n\t\t</TR>\n\t\t");
        
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Wurm Time</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + 
          WurmCalendar.getTime() + "</TD>\n\t\t</TR>\n\t\t");
        
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Weather</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + 
          Server.getWeather().getWeatherString(false) + "</TD>\n\t\t</TR>\n\t\t");
        
        int total = 0;
        int totalLimit = 0;
        int epic = 0;
        int epicMax = 0;
        ServerEntry[] alls = Servers.getAllServers();
        for (ServerEntry entry : alls) {
          if (!entry.EPIC)
          {
            if (!entry.isLocal)
            {
              output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">" + entry.getName() + " players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + entry.currentPlayers + "/" + entry.pLimit + "</TD>\n\t\t</TR>\n\t\t");
              
              total += entry.currentPlayers;
              totalLimit += entry.pLimit;
            }
            else
            {
              output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">" + entry.getName() + " players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + 
              
                getInstance().getNumberOfPlayers() + "/" + entry.pLimit + "</TD>\n\t\t</TR>\n\t\t");
              total += getInstance().getNumberOfPlayers();
              totalLimit += entry.pLimit;
            }
          }
          else
          {
            epic += entry.currentPlayers;
            epicMax += entry.pLimit;
            totalLimit += entry.pLimit;
            total += entry.currentPlayers;
          }
        }
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Epic cluster players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + epic + "/" + epicMax + "</TD>\n\t\t</TR>\n\t\t");
        
        output.write("<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Total players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">" + total + "/" + totalLimit + "</TD>\n\t\t</TR>\n\t\t");
        
        output.write("</TABLE>\n");
        output.write("\n</BODY>\n</HTML>");
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Problem writing server stats = " + iox.getMessage(), iox);
      }
      return;
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to open stats.html", iox);
    }
    finally
    {
      try
      {
        if (output != null) {
          output.close();
        }
      }
      catch (IOException localIOException3) {}
    }
  }
  
  public static void printRanks2()
  {
    printMaxRanks();
    Writer output = null;
    try
    {
      String dir = Constants.webPath;
      if (!dir.endsWith(File.separator)) {
        dir = dir + File.separator;
      }
      File aFile = new File(dir + "ranks.html");
      
      output = new BufferedWriter(new FileWriter(aFile));
      
      String start = "<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>";
      try
      {
        output.write("<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>");
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
      int nums = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?");
        ps.setInt(1, 30);
        rs = ps.executeQuery();
        while (rs.next())
        {
          if (nums < 10) {
            output.write("<TR class=\"gameDataTopTenTR\"><TD class=\"gameDataTopTenTDName\">" + rs
              .getString("NAME") + "</TD><TD class=\"gameDataTopTenTDValue\">" + rs
              .getInt("RANK") + "</TD></TR>");
          } else {
            output.write("<TR class=\"gameDataTR\"><TD class=\"gameDataTDName\">" + rs.getString("NAME") + "</TD><TD class=\"gameDataTDValue\">" + rs
              .getInt("RANK") + "</TD></TR>");
          }
          nums++;
        }
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
      output.write("</TABLE>"); return;
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to close ranks.html", iox);
    }
    finally
    {
      try
      {
        if (output != null) {
          output.close();
        }
      }
      catch (IOException localIOException3) {}
    }
  }
  
  public static Map<String, Long> getFriends(long wurmid)
  {
    Map<String, Long> toReturn = new HashMap();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("select p.NAME,p.WURMID from PLAYERS p INNER JOIN FRIENDS f ON f.FRIEND=p.WURMID WHERE f.WURMID=? ORDER BY NAME");
      ps.setLong(1, wurmid);
      rs = ps.executeQuery();
      while (rs.next()) {
        toReturn.put(rs.getString("NAME"), Long.valueOf(rs.getLong("WURMID")));
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return toReturn;
  }
  
  public static void pruneMessages()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM GMMESSAGES WHERE TIME<? AND MESSAGE NOT LIKE '<Roads> %' AND MESSAGE NOT LIKE '<System> Debug:'");
      ps.setLong(1, System.currentTimeMillis() - 172800000L);
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
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM GMMESSAGES WHERE TIME<?");
      ps.setLong(1, System.currentTimeMillis() - 604800000L);
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
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM MGMTMESSAGES WHERE TIME<?");
      ps.setLong(1, System.currentTimeMillis() - 86400000L);
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
  
  public static void addMgmtMessage(String sender, String message)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("INSERT INTO MGMTMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)");
      ps.setLong(1, System.currentTimeMillis());
      ps.setString(2, sender);
      ps.setString(3, message.substring(0, Math.min(message.length(), 200)));
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
  
  public static void addGmMessage(String sender, String message)
  {
    if (!message.contains(" movement too "))
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("INSERT INTO GMMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)");
        ps.setLong(1, System.currentTimeMillis());
        ps.setString(2, sender);
        ps.setString(3, message.substring(0, Math.min(message.length(), 200)));
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
  
  public static void loadAllPrivatePOIForPlayer(Player player)
  {
    if (!player.getPrivateMapAnnotations().isEmpty()) {
      return;
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM MAP_ANNOTATIONS WHERE POITYPE=0 AND OWNERID=?");
      ps.setLong(1, player.getWurmId());
      rs = ps.executeQuery();
      while (rs.next())
      {
        long wid = rs.getLong("ID");
        String name = rs.getString("NAME");
        long position = rs.getLong("POSITION");
        byte type = rs.getByte("POITYPE");
        long ownerId = rs.getLong("OWNERID");
        String server = rs.getString("SERVER");
        byte icon = rs.getByte("ICON");
        player.addMapPOI(new MapAnnotation(wid, name, type, position, ownerId, server, icon), false);
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem loading all private POI's - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static void loadAllArtists()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM ARTISTS");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long wid = rs.getLong("WURMID");
        artists.put(Long.valueOf(wid), new Artist(wid, rs.getBoolean("SOUND"), rs.getBoolean("GRAPHICS")));
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem loading all artists - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static boolean isArtist(long wurmid, boolean soundRequired, boolean graphicsRequired)
  {
    if (!artists.containsKey(Long.valueOf(wurmid))) {
      return false;
    }
    Artist artist = (Artist)artists.get(Long.valueOf(wurmid));
    if (soundRequired)
    {
      if (artist.isSound())
      {
        if (graphicsRequired) {
          return artist.isGraphics();
        }
        return artist.isSound();
      }
      return false;
    }
    if (graphicsRequired)
    {
      if (artist.isGraphics())
      {
        if (soundRequired) {
          return artist.isSound();
        }
        return artist.isGraphics();
      }
      return false;
    }
    return true;
  }
  
  public static void addArtist(long wurmid, boolean sound, boolean graphics)
  {
    if (!artists.containsKey(Long.valueOf(wurmid)))
    {
      Artist artist = new Artist(wurmid, sound, graphics);
      artists.put(Long.valueOf(wurmid), artist);
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("INSERT INTO ARTISTS (WURMID,SOUND,GRAPHICS) VALUES(?,?,?)");
        ps.setLong(1, wurmid);
        ps.setBoolean(2, sound);
        ps.setBoolean(3, graphics);
        ps.executeUpdate();
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Problem adding artist with id: " + wurmid + " - " + sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
    else
    {
      Artist artist = (Artist)artists.get(Long.valueOf(wurmid));
      if ((artist.isSound() != sound) || (artist.isGraphics() != graphics))
      {
        deleteArtist(wurmid);
        addArtist(wurmid, sound, graphics);
      }
    }
  }
  
  public static void deleteArtist(long wurmid)
  {
    artists.remove(Long.valueOf(wurmid));
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM ARTISTS WHERE WURMID=?");
      ps.setLong(1, wurmid);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem deleting artist with id: " + wurmid + " - " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public long getNumberOfKills()
  {
    long totalPlayerKills = 0L;
    for (PlayerKills pk : playerKills.values()) {
      if ((pk != null) && (pk.getNumberOfKills() > 0)) {
        totalPlayerKills += pk.getNumberOfKills();
      }
    }
    return totalPlayerKills;
  }
  
  public PlayerKills getPlayerKillsFor(long wurmId)
  {
    PlayerKills pk = (PlayerKills)playerKills.get(Long.valueOf(wurmId));
    if (pk == null)
    {
      pk = new PlayerKills(wurmId);
      playerKills.put(Long.valueOf(wurmId), pk);
    }
    return pk;
  }
  
  public boolean isOverKilling(long killerid, long victimid)
  {
    PlayerKills pk = getPlayerKillsFor(killerid);
    if (pk.isOverKilling(victimid)) {
      return true;
    }
    if ((deathCount.containsKey(Long.valueOf(victimid))) && (((Short)deathCount.get(Long.valueOf(victimid))).shortValue() > 3)) {
      return true;
    }
    return false;
  }
  
  public void addKill(long killerid, long victimid, String victimName)
  {
    PlayerKills pk = getPlayerKillsFor(killerid);
    pk.addKill(victimid, victimName);
  }
  
  public void addPvPDeath(long victimId)
  {
    short currentCount = 0;
    if (deathCount.containsKey(Long.valueOf(victimId))) {
      currentCount = ((Short)deathCount.get(Long.valueOf(victimId))).shortValue();
    }
    deathCount.put(Long.valueOf(victimId), Short.valueOf((short)(currentCount + 1)));
  }
  
  public void removePvPDeath(long victimId)
  {
    if (!deathCount.containsKey(Long.valueOf(victimId))) {
      return;
    }
    short currentCount = ((Short)deathCount.get(Long.valueOf(victimId))).shortValue();
    if (currentCount > 1) {
      deathCount.put(Long.valueOf(victimId), Short.valueOf((short)(currentCount - 1)));
    } else {
      deathCount.remove(Long.valueOf(victimId));
    }
  }
  
  public boolean hasPvpDeaths(long victimId)
  {
    return deathCount.containsKey(Long.valueOf(victimId));
  }
  
  public void sendLogoff(String reason)
  {
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr) {
      lPlayer.getCommunicator().sendShutDown(reason, false);
    }
  }
  
  public void logOffLinklessPlayers()
  {
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr) {
      if (!lPlayer.hasLink()) {
        logoutPlayer(lPlayer);
      }
    }
  }
  
  public void checkAffinities()
  {
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr) {
      lPlayer.checkAffinity();
    }
  }
  
  public void checkElectors()
  {
    Player[] playarr = getPlayers();
    for (Player lPlayer : playarr) {
      if (lPlayer.isAspiringKing()) {
        return;
      }
    }
    Methods.resetJennElector();
    Methods.resetHotsElector();
    Methods.resetMolrStone();
  }
  
  public float getCRBonus(byte kingdomId)
  {
    Float f = (Float)this.crBonuses.get(Byte.valueOf(kingdomId));
    if (f != null) {
      return f.floatValue();
    }
    return 0.0F;
  }
  
  public void sendUpdateEpicMission(EpicMission mission)
  {
    for (Player p : getPlayers()) {
      if (!Servers.localServer.PVPSERVER) {
        MissionPerformer.sendEpicMission(mission, p.getCommunicator());
      } else {
        MissionPerformer.sendEpicMissionPvPServer(mission, p, p.getCommunicator());
      }
    }
  }
  
  public void calcCRBonus()
  {
    float total;
    if (!Servers.isThisAHomeServer())
    {
      Map<Byte, Float> numPs = new HashMap();
      total = 0.0F;
      for (Player lPlayer : getPlayers()) {
        if (lPlayer.isPaying())
        {
          byte kingdomId = lPlayer.getKingdomId();
          Float f = (Float)numPs.get(Byte.valueOf(kingdomId));
          if (f == null) {
            f = Float.valueOf(1.0F);
          } else {
            f = Float.valueOf(f.floatValue() + 1.0F);
          }
          numPs.put(Byte.valueOf(kingdomId), f);
          total += 1.0F;
        }
      }
      Object alliedPs = new HashMap();
      for (Byte b : numPs.keySet())
      {
        f = (Float)numPs.get(b);
        ((Map)alliedPs).put(b, f);
        Kingdom k = Kingdoms.getKingdom(b.byteValue());
        if (k != null)
        {
          Map<Byte, Byte> allies = k.getAllianceMap();
          for (Map.Entry<Byte, Byte> entry : allies.entrySet()) {
            if (((Byte)entry.getValue()).byteValue() == 1)
            {
              Float other = (Float)numPs.get(entry.getKey());
              if (other != null) {
                f = Float.valueOf(f.floatValue() + other.floatValue());
              }
            }
          }
        }
      }
      Float f;
      this.crBonuses.clear();
      if (total > 20.0F) {
        for (Object totals : ((Map)alliedPs).entrySet())
        {
          float numbers = ((Float)((Map.Entry)totals).getValue()).floatValue();
          if (numbers / total < 0.05F) {
            this.crBonuses.put(((Map.Entry)totals).getKey(), Float.valueOf(2.0F));
          } else if (numbers / total < 0.1F) {
            this.crBonuses.put(((Map.Entry)totals).getKey(), Float.valueOf(1.0F));
          }
        }
      }
    }
  }
  
  public final void updateEigcInfo(EigcClient client)
  {
    if (!client.getPlayerName().isEmpty()) {
      try
      {
        Player p = getPlayer(client.getPlayerName());
        p.getCommunicator().updateEigcInfo(client);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
  }
  
  public static boolean existsPlayerWithIp(String ipAddress)
  {
    for (Player p : getInstance().getPlayers()) {
      if (p.getSaveFile().getIpaddress().contains(ipAddress)) {
        return true;
      }
    }
    return false;
  }
  
  public final void sendGlobalGMMessage(Creature sender, String message)
  {
    Message mess = new Message(sender, (byte)11, "GM", "<" + sender.getName() + "> " + message);
    
    Server.getInstance().addMessage(mess);
    addGmMessage(sender.getName(), message);
    if (message.trim().length() > 1)
    {
      WCGmMessage wc = new WCGmMessage(WurmId.getNextWCCommandId(), sender.getName(), "(" + Servers.localServer.getAbbreviation() + ") " + message, false);
      if (Servers.localServer.LOGINSERVER) {
        wc.sendFromLoginServer();
      } else {
        wc.sendToLoginServer();
      }
    }
  }
  
  public final void pollPlayers()
  {
    long delta = System.currentTimeMillis() - this.lastPoll;
    if ((float)delta < 0.095F) {
      return;
    }
    this.lastPoll = System.currentTimeMillis();
    for (Player lPlayer : getPlayers()) {
      if (lPlayer != null) {
        lPlayer.pollActions();
      }
    }
  }
  
  public final void sendKingdomToPlayers(Kingdom kingdom)
  {
    for (Player lPlayer : getPlayers()) {
      if (lPlayer.hasLink()) {
        lPlayer.getCommunicator().sendNewKingdom(kingdom.getId(), kingdom.getName(), kingdom.getSuffix());
      }
    }
  }
  
  public static void tellFriends(PlayerState pState)
  {
    for (Player p : getInstance().getPlayers()) {
      if (p.isFriend(pState.getPlayerId())) {
        p.getCommunicator().sendFriend(pState);
      }
    }
  }
  
  public final void sendTicket(Ticket ticket)
  {
    for (Player p : getInstance().getPlayers()) {
      if (p.hasLink()) {
        if (ticket.isTicketShownTo(p)) {
          p.getCommunicator().sendTicket(ticket);
        }
      }
    }
  }
  
  public final void sendTicket(Ticket ticket, @Nullable TicketAction ticketAction)
  {
    for (Player p : getInstance().getPlayers()) {
      if (p.hasLink()) {
        if ((ticket.isTicketShownTo(p)) && ((ticketAction == null) || 
          (ticketAction.isActionShownTo(p)))) {
          p.getCommunicator().sendTicket(ticket, ticketAction);
        }
      }
    }
  }
  
  public final void removeTicket(Ticket ticket)
  {
    for (Player p : getInstance().getPlayers()) {
      if (p.hasLink()) {
        if (ticket.isTicketShownTo(p)) {
          p.getCommunicator().removeTicket(ticket);
        }
      }
    }
  }
  
  public static final void sendVotingOpen(VoteQuestion vq)
  {
    for (Player p : getInstance().getPlayers()) {
      sendVotingOpen(p, vq);
    }
  }
  
  public static void sendVotingOpen(Player p, VoteQuestion vq)
  {
    if (p.hasLink()) {
      if (vq.canVote(p)) {
        p.getCommunicator().sendServerMessage("Poll for " + vq.getQuestionTitle() + " is open, use /poll to participate.", 250, 150, 250);
      }
    }
  }
  
  public void sendMgmtMessage(Creature sender, String playerName, String message, boolean emote, boolean logit, int red, int green, int blue)
  {
    Message mess = null;
    if (emote) {
      mess = new Message(sender, (byte)6, "MGMT", message);
    } else {
      mess = new Message(sender, (byte)9, "MGMT", "<" + playerName + "> " + message, red, green, blue);
    }
    if (logit) {
      addMgmtMessage(playerName, message);
    }
    Player[] playerArr = getInstance().getPlayers();
    for (Player lPlayer : playerArr) {
      if (lPlayer.mayHearMgmtTalk())
      {
        if (sender == null) {
          mess.setSender(lPlayer);
        }
        lPlayer.getCommunicator().sendMessage(mess);
      }
    }
  }
  
  private boolean shouldReceivePlayerList(Player player)
  {
    return (player.getKingdomId() == 4) && (
      (player.isPlayerAssistant()) || (player.mayMute()) || (player.mayHearDevTalk()) || (player.getPower() > 0));
  }
  
  public static boolean getPollCheckClients()
  {
    return pollCheckClients;
  }
  
  public static void setPollCheckClients(boolean doit)
  {
    pollCheckClients = doit;
  }
  
  public static void appointCA(Creature performer, String targetName)
  {
    Player playerPerformer = null;
    if ((performer instanceof Player)) {
      playerPerformer = (Player)performer;
    } else {
      return;
    }
    if (playerPerformer.mayAppointPlayerAssistant())
    {
      String pname = targetName;
      pname = LoginHandler.raiseFirstLetter(pname);
      
      Player p = null;
      try
      {
        p = getInstance().getPlayer(pname);
      }
      catch (NoSuchPlayerException nsp)
      {
        playerPerformer.getCommunicator().sendNormalServerMessage("No player online with the name " + pname);
      }
      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(pname);
      try
      {
        pinf.load();
      }
      catch (IOException e)
      {
        performer.getCommunicator().sendAlertServerMessage("This player does not exist.");
        return;
      }
      if (pinf.wurmId > 0L)
      {
        if (pinf.isPlayerAssistant())
        {
          pinf.setIsPlayerAssistant(false);
          if (p != null) {
            p.getCommunicator().sendAlertServerMessage("You no longer have the duties of a community assistant.", (byte)1);
          }
          playerPerformer.getCommunicator().sendSafeServerMessage(pname + " no longer has the duties of being a community assistant.", (byte)1);
          
          WcDemotion wc = new WcDemotion(WurmId.getNextWCCommandId(), playerPerformer.getWurmId(), pinf.wurmId, (short)1);
          wc.sendToLoginServer();
        }
        else
        {
          if (p != null)
          {
            p.setPlayerAssistant(true);
            p.togglePlayerAssistantWindow(true);
            
            p.getCommunicator().sendSafeServerMessage("You are now a Community Assistant and receives a CA window.");
            
            p.getCommunicator().sendSafeServerMessage("New players will also receive that and may ask you questions.");
            
            p.getCommunicator()
              .sendSafeServerMessage("The suggested way to approach new players is not to approach them directly");
            
            p.getCommunicator()
              .sendSafeServerMessage("but instead let them ask questions. Otherwise many of them may become deterred");
            
            p.getCommunicator()
              .sendSafeServerMessage("since this may be an early online experience or they have poor english knowledge.");
          }
          else
          {
            pinf.setIsPlayerAssistant(true);
            pinf.togglePlayerAssistantWindow(true);
            playerPerformer.getCommunicator().sendAlertServerMessage(pname + " needs to be online in order to receive the title.", (byte)2);
          }
          playerPerformer.getCommunicator().sendSafeServerMessage(pname + " is now appointed Community Assistant.", (byte)1);
          if (playerPerformer.getLogger() != null) {
            playerPerformer.getLogger().log(Level.INFO, playerPerformer
              .getName() + " appoints " + pname + " community assistant.");
          }
          logger.log(Level.INFO, playerPerformer.getName() + " appoints " + pname + " as community assistant.");
        }
      }
      else {
        playerPerformer.getCommunicator().sendNormalServerMessage("No player found with the name " + pname);
      }
    }
  }
  
  public static void appointCM(Creature performer, String targetName)
  {
    if (performer.getPower() >= 1)
    {
      String pname = targetName;
      pname = LoginHandler.raiseFirstLetter(pname);
      
      Player p = null;
      try
      {
        p = getInstance().getPlayer(pname);
      }
      catch (NoSuchPlayerException nsp)
      {
        performer.getCommunicator().sendNormalServerMessage("No player online with the name " + pname);
      }
      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(pname);
      try
      {
        pinf.load();
      }
      catch (IOException e)
      {
        performer.getCommunicator().sendAlertServerMessage("This player does not exist.");
        return;
      }
      if (pinf.wurmId > 0L)
      {
        if (pinf.getPower() == 0)
        {
          if (pinf.mayMute)
          {
            pinf.setMayMute(false);
            if (p != null) {
              p.getCommunicator().sendAlertServerMessage("You may no longer mute other players.", (byte)1);
            }
            performer.getCommunicator().sendSafeServerMessage(pname + " may no longer mute other players.");
            
            WcDemotion wc = new WcDemotion(WurmId.getNextWCCommandId(), performer.getWurmId(), pinf.wurmId, (short)2);
            wc.sendToLoginServer();
          }
          else
          {
            pinf.setMayMute(true);
            if (p != null)
            {
              p.getCommunicator().sendSafeServerMessage("You may now mute other players. Use this with extreme care and wise judgement.");
              
              p.getCommunicator().sendSafeServerMessage("The syntax is #mute <playername> <number of hours> <reason>");
              
              p.getCommunicator().sendSafeServerMessage("For example: #mute unforgiven 6 foul language");
              
              p.getCommunicator().sendSafeServerMessage("To unmute a player, use #unmute <playername>");
              
              p.getCommunicator().sendSafeServerMessage("You may see who are muted with the command #showmuted");
            }
            performer.getCommunicator().sendSafeServerMessage(pname + " may now mute other players.", (byte)1);
            if (performer.getLogger() != null) {
              performer.getLogger().log(Level.INFO, performer
                .getName() + " allows " + pname + " to mute other players.");
            }
            logger.log(Level.INFO, performer.getName() + " allows " + pname + " to mute other players.");
          }
        }
        else {
          performer.getCommunicator().sendNormalServerMessage(pinf.getName() + " may already mute, because he is a Hero or higher.");
        }
      }
      else {
        performer.getCommunicator().sendNormalServerMessage("No player found with the name " + pname);
      }
    }
  }
  
  public static void displayLCMInfo(Creature performer, String targetName)
  {
    if ((performer == null) || (!performer.hasLink())) {
      return;
    }
    if (performer.getPower() >= 1) {
      try
      {
        PlayerInfo targetInfo = PlayerInfoFactory.createPlayerInfo(targetName);
        targetInfo.load();
        PlayerState targetState = PlayerInfoFactory.getPlayerState(targetInfo.wurmId);
        
        Logger logger = performer.getLogger();
        if (logger != null) {
          logger.log(Level.INFO, performer.getName() + " tried to view the info of " + targetInfo.getName());
        }
        if (performer.getPower() < targetInfo.getPower())
        {
          performer.getCommunicator().sendSafeServerMessage("You can't just look at the information of higher ranking staff members!");
          return;
        }
        String email = targetInfo.emailAddress;
        String ip = targetInfo.getIpaddress();
        String lastLogout = new Date(targetState.getLastLogout()).toString();
        String timePlayed = Server.getTimeFor(targetInfo.playingTime);
        String CAInfo = targetInfo.getName() + " is " + (targetInfo.isPlayerAssistant() ? "a CA." : "not a CA.");
        String CMInfo = targetInfo.getName() + " is " + (targetInfo.mayMute ? "a CM." : "not a CM.");
        
        performer.getCommunicator().sendNormalServerMessage("Information about " + targetInfo.getName());
        performer.getCommunicator().sendNormalServerMessage("-----");
        performer.getCommunicator().sendNormalServerMessage("Email address: " + email);
        performer.getCommunicator().sendNormalServerMessage("IP address: " + ip);
        performer.getCommunicator().sendNormalServerMessage("Last logout: " + lastLogout);
        performer.getCommunicator().sendNormalServerMessage("Time played: " + timePlayed);
        performer.getCommunicator().sendNormalServerMessage(CAInfo);
        performer.getCommunicator().sendNormalServerMessage(CMInfo);
        performer.getCommunicator().sendNormalServerMessage("-----");
      }
      catch (Exception e)
      {
        performer.getCommunicator().sendAlertServerMessage("This player does not exist.");
      }
    }
  }
  
  public void updateTabs(byte tab, TabData tabData)
  {
    if (tab == 2)
    {
      removeFromTabs(tabData.getWurmId(), tabData.getName());
    }
    else if (tab == 0)
    {
      tabListGM.put(tabData.getName(), tabData);
      for (Player player : getInstance().getPlayers()) {
        if (player.mayHearDevTalk()) {
          if ((tabData.isVisible()) || (tabData.getPower() <= player.getPower())) {
            player.getCommunicator().sendAddGm(tabData.getName(), tabData.getWurmId());
          } else {
            player.getCommunicator().sendRemoveGm(tabData.getName());
          }
        }
      }
    }
    else if (tab == 1)
    {
      tabListMGMT.put(tabData.getName(), tabData);
      for (Player player : getInstance().getPlayers()) {
        if (player.mayHearMgmtTalk()) {
          if (tabData.isVisible()) {
            player.getCommunicator().sendAddMgmt(tabData.getName(), tabData.getWurmId());
          } else {
            player.getCommunicator().sendRemoveMgmt(tabData.getName());
          }
        }
      }
    }
  }
  
  public void sendToTabs(Player player, boolean showMe, boolean justGM)
  {
    if ((player.getPower() >= 2) || (player.mayHearDevTalk()))
    {
      boolean sendGM = false;
      TabData tabData = (TabData)tabListGM.get(player.getName());
      if (tabData == null)
      {
        tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), (showMe) || (player.getPower() < 2));
        sendGM = true;
      }
      else if ((tabData.isVisible() != showMe) && (player.getPower() >= 2))
      {
        tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), showMe);
        sendGM = true;
      }
      if (sendGM)
      {
        updateTabs((byte)0, tabData);
        WcTabLists wtl = new WcTabLists((byte)0, tabData);
        if (Servers.isThisLoginServer()) {
          wtl.sendFromLoginServer();
        } else {
          wtl.sendToLoginServer();
        }
      }
    }
    if (!justGM)
    {
      boolean sendMGMT = false;
      TabData tabData = (TabData)tabListMGMT.get(player.getName());
      if (tabData == null)
      {
        tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), (showMe) || (player.getPower() < 2));
        sendMGMT = true;
      }
      else if ((tabData.isVisible() != showMe) && (player.getPower() >= 2))
      {
        tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), showMe);
        sendMGMT = true;
      }
      if (sendMGMT)
      {
        updateTabs((byte)1, tabData);
        WcTabLists wtl = new WcTabLists((byte)1, tabData);
        if (Servers.isThisLoginServer()) {
          wtl.sendFromLoginServer();
        } else {
          wtl.sendToLoginServer();
        }
      }
    }
  }
  
  public void removeFromTabs(long wurmId, String name)
  {
    TabData oldGMTabData = (TabData)tabListGM.remove(name);
    TabData oldMGMTTabData = (TabData)tabListMGMT.remove(name);
    if ((oldGMTabData != null) || (oldMGMTTabData != null)) {
      for (Player player : getInstance().getPlayers())
      {
        if ((oldGMTabData != null) && (player.mayHearDevTalk())) {
          player.getCommunicator().sendRemoveGm(name);
        }
        if ((oldMGMTTabData != null) && (player.mayHearMgmtTalk())) {
          player.getCommunicator().sendRemoveMgmt(name);
        }
      }
    }
  }
  
  public void sendRemoveFromTabs(long wurmId, String name)
  {
    TabData tabData = new TabData(wurmId, name, (byte)0, false);
    if (tabData != null)
    {
      WcTabLists wtl = new WcTabLists((byte)2, tabData);
      if (Servers.isThisLoginServer()) {
        wtl.sendFromLoginServer();
      } else {
        wtl.sendToLoginServer();
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\Players.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */