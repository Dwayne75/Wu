package com.wurmonline.server;

import coffee.keenan.network.wrappers.upnp.UPNPService;
import com.wurmonline.communication.ServerListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.communication.SocketServer;
import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.TerraformingTask;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.combat.Arrows;
import com.wurmonline.server.combat.Battles;
import com.wurmonline.server.combat.ServerProjectile;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.LocalSupplyDemand;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.epic.EpicMapListener;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.epic.ValreiMapData;
import com.wurmonline.server.highways.HighwayFinder;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.intra.IntraCommand;
import com.wurmonline.server.intra.IntraServer;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.TimeSync;
import com.wurmonline.server.intra.TimeSync.TimeSyncSender;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.AwardLadder;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.HackerIp;
import com.wurmonline.server.players.PendingAccount;
import com.wurmonline.server.players.PendingAward;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerCommunicatorSender;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerInfoFactory.FatigueSwitcher;
import com.wurmonline.server.questions.Questions;
import com.wurmonline.server.skills.SkillStat;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.statistics.ChallengePointEnum.ChallengePoint;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.steam.SteamHandler;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.Trello;
import com.wurmonline.server.support.VoteQuestions;
import com.wurmonline.server.utils.CreaturePositionDatabaseUpdater;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.RecruitmentAds;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.CropTilePoller;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.ErrorChecks;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.TilePoller;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.Water;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.server.zones.ZonesUtility;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public final class Server
  extends TimerTask
  implements Runnable, ServerMonitoring, ServerListener, CounterTypes, MiscConstants, CreatureTemplateIds, TimeConstants, EpicMapListener
{
  private SocketServer socketServer;
  private boolean isPS = false;
  private static final Logger logger = Logger.getLogger(Server.class.getName());
  private static Server instance = null;
  private static boolean EpicServer;
  private static boolean ChallengeServer;
  public static final Random rand = new Random();
  public static final Object SYNC_LOCK = new Object();
  public static final long SLEEP_TIME = 25L;
  private static final long LIGHTNING_INTERVAL = 5000L;
  private static final long DIRTY_MESH_ROW_SAVE_INTERVAL = 60000L;
  private static final long SKILL_POLL_INTERVAL = 21600000L;
  private static final long MACROING_RESET_INTERVAL = 14400000L;
  private static final long ARROW_POLL_INTERVAL = 100L;
  private static final long MAIL_POLL_INTERVAL = 364000L;
  private static final long RUBBLE_POLL_INTERVAL = 60000L;
  private static final long WATER_POLL_INTERVAL = 1000L;
  private static final float STORM_RAINY_THRESHOLD = 0.5F;
  private static final float STORM_CLOUDY_THRESHOLD = 0.5F;
  private static final long WEATHER_SET_INTERVAL = 70000L;
  private static short counter = 0;
  private List<Long> playersAtLogin;
  private static final ReentrantReadWriteLock PLAYERS_AT_LOGIN_RW_LOCK = new ReentrantReadWriteLock();
  private static boolean locked = false;
  private static short molRehanX = 438;
  private static short molRehanY = 2142;
  private static int newPremiums = 0;
  private static int expiredPremiums = 0;
  private static long lastResetNewPremiums = 0L;
  private static long lastPolledSupplyDepots = 0L;
  private static long savedChallengePage = System.currentTimeMillis() + 120000L;
  private static int oldPremiums = 0;
  private static long lastResetOldPremiums = 0L;
  public static MeshIO surfaceMesh;
  public static MeshIO caveMesh;
  public static MeshIO resourceMesh;
  public static MeshIO rockMesh;
  public static HexMap epicMap;
  private static MeshIO flagsMesh;
  private static final int bitBonatize = 128;
  private static final int bitForage = 64;
  private static final int bitGather = 32;
  private static final int bitInvestigate = 16;
  private static final int bitGrubs = 2048;
  private static final int bitHiveCheck = 1024;
  private static final int bitBeingTransformed = 512;
  private static final int bitTransformed = 256;
  private boolean needSeeds = false;
  private static List<Creature> creaturesToRemove = new ArrayList();
  private static final ReentrantReadWriteLock CREATURES_TO_REMOVE_RW_LOCK = new ReentrantReadWriteLock();
  private static final Set<WebCommand> webcommands = new HashSet();
  private static final Set<TerraformingTask> terraformingTasks = new HashSet();
  public static final ReentrantReadWriteLock TERRAFORMINGTASKS_RW_LOCK = new ReentrantReadWriteLock();
  public static final ReentrantReadWriteLock WEBCOMMANDS_RW_LOCK = new ReentrantReadWriteLock();
  public static int lagticks = 0;
  public static float lastLagticks = 0.0F;
  public static int lagMoveModifier = 0;
  private static int lastSentWarning = 0;
  private static long lastAwardedBattleCamps = System.currentTimeMillis();
  private static long startTime = System.currentTimeMillis();
  private static long lastSecond = System.currentTimeMillis();
  private static long lastPolledRubble = 0L;
  private static long lastPolledShopCultist = System.currentTimeMillis();
  private static Map<String, Boolean> ips = new ConcurrentHashMap();
  private static ConcurrentLinkedQueue<PendingAward> pendingAwards = new ConcurrentLinkedQueue();
  private static int numips = 0;
  private static int logons = 0;
  private static int logonsPrem = 0;
  private static int newbies = 0;
  private static volatile long millisToShutDown = Long.MIN_VALUE;
  private static long lastPinged = 0L;
  private static long lastDeletedPlayer = 0L;
  private static long lastLoweredRanks = System.currentTimeMillis() + 600000L;
  private static volatile String shutdownReason = "Reason: unknown";
  private static List<Long> finalLogins = new ArrayList();
  private static final ReentrantReadWriteLock FINAL_LOGINS_RW_LOCK = new ReentrantReadWriteLock();
  private static boolean pollCommunicators = false;
  public static final int VILLAGE_POLL_MOD = 4000;
  private long lastTicked = 0L;
  private static long lastWeather = 0L;
  private static long lastArrow = 0L;
  private static long lastMailCheck = System.currentTimeMillis();
  private static long lastFaith = 0L;
  private static long lastRecruitmentPoll = 0L;
  private static long lastAwardedItems = System.currentTimeMillis();
  private static int lostConnections = 0;
  private long nextTerraformPoll = System.currentTimeMillis();
  private static int totalTicks = 0;
  private static int commPollCounter = 0;
  private static int commPollCounterInit = 1;
  private long lastLogged = 0L;
  private static long lastPolledBanks = 0L;
  private static long lastPolledWater = 0L;
  private static long lastPolledHighwayFinder = 0L;
  private byte[] externalIp = new byte[4];
  private byte[] internalIp = new byte[4];
  private static final Weather weather = new Weather();
  private boolean thunderMode = false;
  private long lastFlash = 0L;
  private IntraServer intraServer;
  private final List<IntraCommand> intraCommands = new LinkedList();
  private static final ReentrantReadWriteLock INTRA_COMMANDS_RW_LOCK = new ReentrantReadWriteLock();
  private long lastClearedFaithGain = 0L;
  private static int exceptions = 0;
  private static int secondsLag = 0;
  public static String alertMessage1 = "";
  public static long lastAlertMess1 = Long.MAX_VALUE;
  public static String alertMessage2 = "";
  public static long lastAlertMess2 = Long.MAX_VALUE;
  public static String alertMessage3 = "";
  public static long lastAlertMess3 = Long.MAX_VALUE;
  public static String alertMessage4 = "";
  public static long lastAlertMess4 = Long.MAX_VALUE;
  public static long timeBetweenAlertMess1 = Long.MAX_VALUE;
  public static long timeBetweenAlertMess2 = Long.MAX_VALUE;
  public static long timeBetweenAlertMess3 = Long.MAX_VALUE;
  public static long timeBetweenAlertMess4 = Long.MAX_VALUE;
  private static long lastPolledSkills = 0L;
  private static long lastPolledRifts = 0L;
  private static long lastResetAspirations = System.currentTimeMillis();
  private static long lastPolledTileEffects = System.currentTimeMillis();
  private static long lastResetTiles = System.currentTimeMillis();
  private static int combatCounter = 0;
  private static int secondsUptime = 0;
  private ScheduledExecutorService scheduledExecutorService;
  public static boolean allowTradeCheat = true;
  private ExecutorService mainExecutorService;
  private static final int EXECUTOR_SERVICE_NUMBER_OF_THREADS = 20;
  private static PlayerCommunicatorSender playerCommunicatorSender;
  private static boolean appointedSixThousand = false;
  static final double FMOD = 1.3571428060531616D;
  static final double RMOD = 0.1666666716337204D;
  public static int playersThroughTutorial = 0;
  public Water waterThread = null;
  public HighwayFinder highwayFinderThread = null;
  private static Map<Integer, Short> lowDirtHeight = new ConcurrentHashMap();
  private static Set<Integer> newYearEffects = new HashSet();
  public SteamHandler steamHandler = new SteamHandler();
  private static final ConcurrentHashMap<Long, Long> tempEffects = new ConcurrentHashMap();
  
  public static Server getInstance()
  {
    while (locked) {
      try
      {
        Thread.sleep(1000L);
        logger.log(Level.INFO, "Thread sleeping 1 second waiting for server to start.");
      }
      catch (InterruptedException localInterruptedException) {}
    }
    if (instance == null) {
      try
      {
        locked = true;
        instance = new Server();
        locked = false;
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, "Failed to create server instance... shutting down.", ex);
        System.exit(0);
      }
    }
    return instance;
  }
  
  /* Error */
  public void addCreatureToRemove(Creature creature)
  {
    // Byte code:
    //   0: getstatic 20	com/wurmonline/server/Server:CREATURES_TO_REMOVE_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 23	com/wurmonline/server/Server:creaturesToRemove	Ljava/util/List;
    //   12: aload_1
    //   13: invokeinterface 24 2 0
    //   18: pop
    //   19: getstatic 20	com/wurmonline/server/Server:CREATURES_TO_REMOVE_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   22: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   25: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   28: goto +15 -> 43
    //   31: astore_2
    //   32: getstatic 20	com/wurmonline/server/Server:CREATURES_TO_REMOVE_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   35: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   38: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   41: aload_2
    //   42: athrow
    //   43: return
    // Line number table:
    //   Java source line #548	-> byte code offset #0
    //   Java source line #551	-> byte code offset #9
    //   Java source line #555	-> byte code offset #19
    //   Java source line #556	-> byte code offset #28
    //   Java source line #555	-> byte code offset #31
    //   Java source line #556	-> byte code offset #41
    //   Java source line #557	-> byte code offset #43
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	44	0	this	Server
    //   0	44	1	creature	Creature
    //   31	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	19	31	finally
  }
  
  public void startShutdown(int seconds, String reason)
  {
    millisToShutDown = seconds * 1000L;
    
    shutdownReason = "Reason: " + reason;
    int mins = seconds / 60;
    int secs = seconds - mins * 60;
    StringBuffer buf = new StringBuffer();
    if (mins > 0)
    {
      buf.append(mins + " minute");
      if (mins > 1) {
        buf.append("s");
      }
    }
    if (secs > 0)
    {
      if (mins > 0) {
        buf.append(" and ");
      }
      buf.append(secs + " seconds");
    }
    broadCastAlert("The server is shutting down in " + buf.toString() + ". " + shutdownReason, true, (byte)0);
  }
  
  private void removeCreatures()
  {
    CREATURES_TO_REMOVE_RW_LOCK.writeLock().lock();
    try
    {
      Creature[] crets = (Creature[])creaturesToRemove.toArray(new Creature[creaturesToRemove.size()]);
      for (Creature lCret : crets)
      {
        if ((lCret instanceof Player)) {
          Players.getInstance().logoutPlayer((Player)lCret);
        } else {
          Creatures.getInstance().removeCreature(lCret);
        }
        creaturesToRemove.remove(lCret);
      }
      if (creaturesToRemove.size() > 0)
      {
        logger.log(Level.WARNING, "Okay something is weird here. Deleting list. Debug more.");
        creaturesToRemove = new ArrayList();
      }
    }
    finally
    {
      CREATURES_TO_REMOVE_RW_LOCK.writeLock().unlock();
    }
  }
  
  private Server()
    throws Exception
  {}
  
  public boolean isLagging()
  {
    return lagticks >= 2000;
  }
  
  public void setExternalIp()
  {
    StringTokenizer tokens = new StringTokenizer(Servers.localServer.EXTERNALIP, ".");
    int x = 0;
    while (tokens.hasMoreTokens())
    {
      String next = tokens.nextToken();
      this.externalIp[x] = Integer.valueOf(next).byteValue();
      
      x++;
    }
  }
  
  private void setInternalIp()
  {
    StringTokenizer tokens = new StringTokenizer(Servers.localServer.INTRASERVERADDRESS, ".");
    int x = 0;
    while (tokens.hasMoreTokens())
    {
      String next = tokens.nextToken();
      this.internalIp[x] = Integer.valueOf(next).byteValue();
      
      x++;
    }
  }
  
  private void initialiseExecutorService(int aNumberOfThreads)
  {
    logger.info("Initialising ExecutorService with NumberOfThreads: " + aNumberOfThreads);
    this.mainExecutorService = Executors.newFixedThreadPool(aNumberOfThreads);
  }
  
  public ExecutorService getMainExecutorService()
  {
    return this.mainExecutorService;
  }
  
  /* Error */
  public void startRunning()
    throws Exception
  {
    // Byte code:
    //   0: iconst_0
    //   1: invokestatic 96	com/wurmonline/server/Constants:logConstantValues	(Z)V
    //   4: invokestatic 97	com/wurmonline/server/Server:addShutdownHook	()V
    //   7: aload_0
    //   8: invokespecial 98	com/wurmonline/server/Server:logCodeVersionInformation	()V
    //   11: invokestatic 99	com/wurmonline/server/DbConnector:initialize	()V
    //   14: getstatic 100	com/wurmonline/server/Constants:dbAutoMigrate	Z
    //   17: ifeq +28 -> 45
    //   20: invokestatic 101	com/wurmonline/server/DbConnector:hasPendingMigrations	()Z
    //   23: ifeq +30 -> 53
    //   26: invokestatic 102	com/wurmonline/server/DbConnector:performMigrations	()Lcom/wurmonline/server/database/migrations/MigrationResult;
    //   29: invokevirtual 103	com/wurmonline/server/database/migrations/MigrationResult:isError	()Z
    //   32: ifeq +21 -> 53
    //   35: new 104	com/wurmonline/shared/exceptions/WurmServerException
    //   38: dup
    //   39: ldc 105
    //   41: invokespecial 106	com/wurmonline/shared/exceptions/WurmServerException:<init>	(Ljava/lang/String;)V
    //   44: athrow
    //   45: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   48: ldc 107
    //   50: invokevirtual 93	java/util/logging/Logger:info	(Ljava/lang/String;)V
    //   53: getstatic 108	com/wurmonline/server/Constants:checkAllDbTables	Z
    //   56: ifeq +15 -> 71
    //   59: invokestatic 109	com/wurmonline/server/DbConnector:getLoginDbCon	()Ljava/sql/Connection;
    //   62: getstatic 110	com/wurmonline/server/utils/DbUtilities$DbAdminAction:CHECK_MEDIUM	Lcom/wurmonline/server/utils/DbUtilities$DbAdminAction;
    //   65: invokestatic 111	com/wurmonline/server/utils/DbUtilities:performAdminOnAllTables	(Ljava/sql/Connection;Lcom/wurmonline/server/utils/DbUtilities$DbAdminAction;)V
    //   68: goto +11 -> 79
    //   71: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   74: ldc 112
    //   76: invokevirtual 93	java/util/logging/Logger:info	(Ljava/lang/String;)V
    //   79: getstatic 113	com/wurmonline/server/Constants:analyseAllDbTables	Z
    //   82: ifeq +15 -> 97
    //   85: invokestatic 109	com/wurmonline/server/DbConnector:getLoginDbCon	()Ljava/sql/Connection;
    //   88: getstatic 114	com/wurmonline/server/utils/DbUtilities$DbAdminAction:ANALYZE	Lcom/wurmonline/server/utils/DbUtilities$DbAdminAction;
    //   91: invokestatic 111	com/wurmonline/server/utils/DbUtilities:performAdminOnAllTables	(Ljava/sql/Connection;Lcom/wurmonline/server/utils/DbUtilities$DbAdminAction;)V
    //   94: goto +11 -> 105
    //   97: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   100: ldc 115
    //   102: invokevirtual 93	java/util/logging/Logger:info	(Ljava/lang/String;)V
    //   105: getstatic 116	com/wurmonline/server/Constants:optimiseAllDbTables	Z
    //   108: ifeq +15 -> 123
    //   111: invokestatic 109	com/wurmonline/server/DbConnector:getLoginDbCon	()Ljava/sql/Connection;
    //   114: getstatic 117	com/wurmonline/server/utils/DbUtilities$DbAdminAction:OPTIMIZE	Lcom/wurmonline/server/utils/DbUtilities$DbAdminAction;
    //   117: invokestatic 111	com/wurmonline/server/utils/DbUtilities:performAdminOnAllTables	(Ljava/sql/Connection;Lcom/wurmonline/server/utils/DbUtilities$DbAdminAction;)V
    //   120: goto +11 -> 131
    //   123: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   126: ldc 118
    //   128: invokevirtual 93	java/util/logging/Logger:info	(Ljava/lang/String;)V
    //   131: iconst_0
    //   132: invokestatic 119	com/wurmonline/server/Servers:loadAllServers	(Z)V
    //   135: getstatic 120	com/wurmonline/server/Constants:useDirectByteBuffersForMeshIO	Z
    //   138: ifeq +7 -> 145
    //   141: iconst_1
    //   142: invokestatic 121	com/wurmonline/mesh/MeshIO:setAllocateDirectBuffers	(Z)V
    //   145: aload_0
    //   146: getfield 79	com/wurmonline/server/Server:steamHandler	Lcom/wurmonline/server/steam/SteamHandler;
    //   149: invokevirtual 122	com/wurmonline/server/steam/SteamHandler:getIsOfflineServer	()Z
    //   152: ifeq +19 -> 171
    //   155: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   158: ldc 123
    //   160: putfield 83	com/wurmonline/server/ServerEntry:EXTERNALIP	Ljava/lang/String;
    //   163: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   166: ldc 123
    //   168: putfield 91	com/wurmonline/server/ServerEntry:INTRASERVERADDRESS	Ljava/lang/String;
    //   171: aload_0
    //   172: invokespecial 124	com/wurmonline/server/Server:loadWorldMesh	()V
    //   175: aload_0
    //   176: invokespecial 125	com/wurmonline/server/Server:loadCaveMesh	()V
    //   179: aload_0
    //   180: invokespecial 126	com/wurmonline/server/Server:loadResourceMesh	()V
    //   183: aload_0
    //   184: invokespecial 127	com/wurmonline/server/Server:loadRockMesh	()V
    //   187: aload_0
    //   188: invokespecial 128	com/wurmonline/server/Server:loadFlagsMesh	()V
    //   191: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   194: new 27	java/lang/StringBuilder
    //   197: dup
    //   198: invokespecial 28	java/lang/StringBuilder:<init>	()V
    //   201: ldc -127
    //   203: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   206: invokestatic 130	com/wurmonline/server/Server:getMaxHeight	()S
    //   209: invokevirtual 35	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   212: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   215: invokevirtual 93	java/util/logging/Logger:info	(Ljava/lang/String;)V
    //   218: getstatic 131	com/wurmonline/server/Features$Feature:SURFACEWATER	Lcom/wurmonline/server/Features$Feature;
    //   221: invokevirtual 132	com/wurmonline/server/Features$Feature:isEnabled	()Z
    //   224: istore_1
    //   225: goto +6 -> 231
    //   228: astore_1
    //   229: aload_1
    //   230: athrow
    //   231: getstatic 131	com/wurmonline/server/Features$Feature:SURFACEWATER	Lcom/wurmonline/server/Features$Feature;
    //   234: invokevirtual 132	com/wurmonline/server/Features$Feature:isEnabled	()Z
    //   237: ifeq +6 -> 243
    //   240: invokestatic 133	com/wurmonline/server/zones/Water:loadWaterMesh	()V
    //   243: getstatic 134	com/wurmonline/server/Server:surfaceMesh	Lcom/wurmonline/mesh/MeshIO;
    //   246: invokevirtual 135	com/wurmonline/mesh/MeshIO:calcDistantTerrain	()V
    //   249: invokestatic 136	com/wurmonline/server/Features:loadAllFeatures	()V
    //   252: invokestatic 137	com/wurmonline/server/MessageServer:initialise	()V
    //   255: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   258: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   261: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   264: aload_0
    //   265: new 58	java/util/ArrayList
    //   268: dup
    //   269: invokespecial 59	java/util/ArrayList:<init>	()V
    //   272: putfield 139	com/wurmonline/server/Server:playersAtLogin	Ljava/util/List;
    //   275: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   278: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   281: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   284: goto +15 -> 299
    //   287: astore_2
    //   288: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   291: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   294: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   297: aload_2
    //   298: athrow
    //   299: new 140	com/wurmonline/server/Group
    //   302: dup
    //   303: ldc -115
    //   305: invokespecial 142	com/wurmonline/server/Group:<init>	(Ljava/lang/String;)V
    //   308: invokestatic 143	com/wurmonline/server/Groups:addGroup	(Lcom/wurmonline/server/Group;)V
    //   311: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   314: getfield 144	com/wurmonline/server/ServerEntry:EPIC	Z
    //   317: putstatic 145	com/wurmonline/server/Server:EpicServer	Z
    //   320: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   323: invokevirtual 146	com/wurmonline/server/ServerEntry:isChallengeServer	()Z
    //   326: putstatic 147	com/wurmonline/server/Server:ChallengeServer	Z
    //   329: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   332: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   335: ldc -108
    //   337: invokevirtual 10	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   340: invokestatic 149	com/wurmonline/server/items/ItemTemplateCreator:initialiseItemTemplates	()V
    //   343: invokestatic 150	com/wurmonline/server/spells/SpellGenerator:createSpells	()V
    //   346: invokestatic 151	com/wurmonline/server/creatures/CreatureTemplateCreator:createCreatureTemplates	()V
    //   349: invokestatic 152	com/wurmonline/server/loot/LootTableCreator:initializeLootTables	()V
    //   352: getstatic 153	com/wurmonline/server/Constants:createTemporaryDatabaseIndicesAtStartup	Z
    //   355: ifeq +9 -> 364
    //   358: invokestatic 154	com/wurmonline/server/utils/DbIndexManager:createIndexes	()V
    //   361: goto +11 -> 372
    //   364: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   367: ldc -101
    //   369: invokevirtual 156	java/util/logging/Logger:warning	(Ljava/lang/String;)V
    //   372: getstatic 157	com/wurmonline/server/Features$Feature:CROP_POLLER	Lcom/wurmonline/server/Features$Feature;
    //   375: invokevirtual 132	com/wurmonline/server/Features$Feature:isEnabled	()Z
    //   378: ifeq +6 -> 384
    //   381: invokestatic 158	com/wurmonline/server/zones/CropTilePoller:initializeFields	()V
    //   384: getstatic 159	com/wurmonline/server/Constants:RUNBATCH	Z
    //   387: ifeq +3 -> 390
    //   390: getstatic 160	com/wurmonline/server/Constants:crashed	Z
    //   393: ifeq +9 -> 402
    //   396: invokestatic 161	com/wurmonline/server/batchjobs/PlayerBatchJob:reimburseFatigue	()V
    //   399: goto +16 -> 415
    //   402: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   405: getfield 162	com/wurmonline/server/ServerEntry:LOGINSERVER	Z
    //   408: ifne +7 -> 415
    //   411: iconst_1
    //   412: putstatic 160	com/wurmonline/server/Constants:crashed	Z
    //   415: invokestatic 163	com/wurmonline/server/effects/EffectFactory:getInstance	()Lcom/wurmonline/server/effects/EffectFactory;
    //   418: invokevirtual 164	com/wurmonline/server/effects/EffectFactory:loadEffects	()V
    //   421: invokestatic 165	com/wurmonline/server/creatures/AnimalSettings:loadAll	()V
    //   424: invokestatic 166	com/wurmonline/server/items/ItemSettings:loadAll	()V
    //   427: invokestatic 167	com/wurmonline/server/structures/DoorSettings:loadAll	()V
    //   430: invokestatic 168	com/wurmonline/server/structures/StructureSettings:loadAll	()V
    //   433: invokestatic 169	com/wurmonline/server/creatures/MineDoorSettings:loadAll	()V
    //   436: invokestatic 170	com/wurmonline/server/players/PermissionsHistories:loadAll	()V
    //   439: invokestatic 171	com/wurmonline/server/Items:loadAllItemData	()V
    //   442: invokestatic 172	com/wurmonline/server/Items:loadAllItempInscriptionData	()V
    //   445: invokestatic 173	com/wurmonline/server/Items:loadAllStaticItems	()V
    //   448: invokestatic 174	com/wurmonline/server/items/BodyDbStrings:getInstance	()Lcom/wurmonline/server/items/BodyDbStrings;
    //   451: invokestatic 175	com/wurmonline/server/Items:loadAllZoneItems	(Lcom/wurmonline/server/items/DbStrings;)V
    //   454: invokestatic 176	com/wurmonline/server/items/ItemDbStrings:getInstance	()Lcom/wurmonline/server/items/ItemDbStrings;
    //   457: invokestatic 175	com/wurmonline/server/Items:loadAllZoneItems	(Lcom/wurmonline/server/items/DbStrings;)V
    //   460: invokestatic 177	com/wurmonline/server/items/CoinDbStrings:getInstance	()Lcom/wurmonline/server/items/CoinDbStrings;
    //   463: invokestatic 175	com/wurmonline/server/Items:loadAllZoneItems	(Lcom/wurmonline/server/items/DbStrings;)V
    //   466: invokestatic 178	com/wurmonline/server/items/ItemRequirement:loadAllItemRequirements	()V
    //   469: invokestatic 179	com/wurmonline/server/combat/ArmourTemplate:initialize	()V
    //   472: invokestatic 180	com/wurmonline/server/combat/WeaponCreator:createWeapons	()V
    //   475: invokestatic 181	com/wurmonline/server/banks/Banks:loadAllBanks	()V
    //   478: invokestatic 182	com/wurmonline/server/structures/Wall:loadAllWalls	()V
    //   481: invokestatic 183	com/wurmonline/server/structures/Floor:loadAllFloors	()V
    //   484: invokestatic 184	com/wurmonline/server/structures/BridgePart:loadAllBridgeParts	()V
    //   487: invokestatic 185	com/wurmonline/server/kingdom/Kingdom:loadAllKingdoms	()V
    //   490: invokestatic 186	com/wurmonline/server/kingdom/King:loadAllEra	()V
    //   493: invokestatic 187	com/wurmonline/server/spells/Cooldowns:loadAllCooldowns	()V
    //   496: iconst_1
    //   497: getstatic 188	com/wurmonline/server/Constants:meshSize	I
    //   500: ishl
    //   501: iconst_1
    //   502: getstatic 188	com/wurmonline/server/Constants:meshSize	I
    //   505: ishl
    //   506: imul
    //   507: iconst_1
    //   508: isub
    //   509: putstatic 189	com/wurmonline/server/zones/TilePoller:mask	I
    //   512: iconst_0
    //   513: iconst_0
    //   514: iconst_1
    //   515: invokestatic 190	com/wurmonline/server/zones/Zones:getZone	(IIZ)Lcom/wurmonline/server/zones/Zone;
    //   518: pop
    //   519: invokestatic 191	com/wurmonline/server/villages/Villages:loadVillages	()V
    //   522: getstatic 192	com/wurmonline/server/Features$Feature:HIGHWAYS	Lcom/wurmonline/server/Features$Feature;
    //   525: invokevirtual 132	com/wurmonline/server/Features$Feature:isEnabled	()Z
    //   528: ifeq +39 -> 567
    //   531: aload_0
    //   532: new 193	com/wurmonline/server/highways/HighwayFinder
    //   535: dup
    //   536: invokespecial 194	com/wurmonline/server/highways/HighwayFinder:<init>	()V
    //   539: putfield 76	com/wurmonline/server/Server:highwayFinderThread	Lcom/wurmonline/server/highways/HighwayFinder;
    //   542: aload_0
    //   543: getfield 76	com/wurmonline/server/Server:highwayFinderThread	Lcom/wurmonline/server/highways/HighwayFinder;
    //   546: invokevirtual 195	com/wurmonline/server/highways/HighwayFinder:start	()V
    //   549: invokestatic 196	com/wurmonline/server/highways/Routes:generateAllRoutes	()V
    //   552: getstatic 197	com/wurmonline/server/Features$Feature:WAGONER	Lcom/wurmonline/server/Features$Feature;
    //   555: invokevirtual 132	com/wurmonline/server/Features$Feature:isEnabled	()Z
    //   558: ifeq +9 -> 567
    //   561: invokestatic 198	com/wurmonline/server/creatures/Delivery:dbLoadAllDeliveries	()V
    //   564: invokestatic 199	com/wurmonline/server/creatures/Wagoner:dbLoadAllWagoners	()V
    //   567: invokestatic 200	com/wurmonline/server/creatures/CreaturePos:loadAllPositions	()V
    //   570: invokestatic 53	com/wurmonline/server/creatures/Creatures:getInstance	()Lcom/wurmonline/server/creatures/Creatures;
    //   573: invokevirtual 201	com/wurmonline/server/creatures/Creatures:loadAllCreatures	()I
    //   576: pop
    //   577: goto +23 -> 600
    //   580: astore_1
    //   581: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   584: getstatic 56	java/util/logging/Level:WARNING	Ljava/util/logging/Level;
    //   587: aload_1
    //   588: invokevirtual 203	com/wurmonline/server/creatures/NoSuchCreatureException:getMessage	()Ljava/lang/String;
    //   591: aload_1
    //   592: invokevirtual 18	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   595: iconst_0
    //   596: invokestatic 19	java/lang/System:exit	(I)V
    //   599: return
    //   600: invokestatic 204	com/wurmonline/server/villages/Villages:loadDeadVillages	()V
    //   603: invokestatic 205	com/wurmonline/server/villages/Villages:loadCitizens	()V
    //   606: invokestatic 206	com/wurmonline/server/villages/Villages:loadGuards	()V
    //   609: invokestatic 207	com/wurmonline/server/Server:fixHoles	()V
    //   612: invokestatic 208	com/wurmonline/server/Items:loadAllItemEffects	()V
    //   615: invokestatic 209	com/wurmonline/server/creatures/MineDoorPermission:loadAllMineDoors	()V
    //   618: invokestatic 210	com/wurmonline/server/zones/Zones:loadTowers	()V
    //   621: invokestatic 211	com/wurmonline/server/villages/PvPAlliance:loadPvPAlliances	()V
    //   624: invokestatic 212	com/wurmonline/server/villages/Villages:loadWars	()V
    //   627: invokestatic 213	com/wurmonline/server/villages/Villages:loadWarDeclarations	()V
    //   630: invokestatic 214	com/wurmonline/server/villages/RecruitmentAds:loadRecruitmentAds	()V
    //   633: invokestatic 215	com/wurmonline/server/zones/Zones:addWarDomains	()V
    //   636: invokestatic 216	java/lang/System:nanoTime	()J
    //   639: lstore_1
    //   640: invokestatic 217	com/wurmonline/server/economy/Economy:getEconomy	()Lcom/wurmonline/server/economy/Economy;
    //   643: pop
    //   644: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   647: invokevirtual 218	com/wurmonline/server/ServerEntry:getKingsmoneyAtRestart	()I
    //   650: ifle +19 -> 669
    //   653: invokestatic 217	com/wurmonline/server/economy/Economy:getEconomy	()Lcom/wurmonline/server/economy/Economy;
    //   656: invokevirtual 219	com/wurmonline/server/economy/Economy:getKingsShop	()Lcom/wurmonline/server/economy/Shop;
    //   659: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   662: invokevirtual 218	com/wurmonline/server/ServerEntry:getKingsmoneyAtRestart	()I
    //   665: i2l
    //   666: invokevirtual 220	com/wurmonline/server/economy/Shop:setMoney	(J)V
    //   669: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   672: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   675: new 27	java/lang/StringBuilder
    //   678: dup
    //   679: invokespecial 28	java/lang/StringBuilder:<init>	()V
    //   682: ldc -35
    //   684: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   687: invokestatic 216	java/lang/System:nanoTime	()J
    //   690: lload_1
    //   691: lsub
    //   692: l2f
    //   693: ldc -34
    //   695: fdiv
    //   696: invokevirtual 223	java/lang/StringBuilder:append	(F)Ljava/lang/StringBuilder;
    //   699: ldc -32
    //   701: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   704: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   707: invokevirtual 10	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   710: invokestatic 225	com/wurmonline/server/endgames/EndGameItems:loadEndGameItems	()V
    //   713: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   716: getfield 226	com/wurmonline/server/ServerEntry:HOMESERVER	Z
    //   719: ifne +10 -> 729
    //   722: invokestatic 227	com/wurmonline/server/Items:getWarTargets	()[Lcom/wurmonline/server/items/Item;
    //   725: arraylength
    //   726: ifne +3 -> 729
    //   729: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   732: getfield 144	com/wurmonline/server/ServerEntry:EPIC	Z
    //   735: ifeq +12 -> 747
    //   738: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   741: getfield 226	com/wurmonline/server/ServerEntry:HOMESERVER	Z
    //   744: ifne +14 -> 758
    //   747: invokestatic 228	com/wurmonline/server/Items:getSourceSprings	()[Lcom/wurmonline/server/items/Item;
    //   750: arraylength
    //   751: ifne +7 -> 758
    //   754: iconst_1
    //   755: putstatic 229	com/wurmonline/server/zones/Zones:shouldSourceSprings	Z
    //   758: getstatic 230	com/wurmonline/server/Features$Feature:NEWDOMAINS	Lcom/wurmonline/server/Features$Feature;
    //   761: invokevirtual 132	com/wurmonline/server/Features$Feature:isEnabled	()Z
    //   764: ifne +6 -> 770
    //   767: invokestatic 231	com/wurmonline/server/zones/Zones:checkAltars	()V
    //   770: invokestatic 232	com/wurmonline/server/players/PlayerInfoFactory:loadPlayerInfos	()V
    //   773: invokestatic 233	com/wurmonline/server/players/WurmRecord:loadAllChampRecords	()V
    //   776: invokestatic 234	com/wurmonline/server/skills/Affinities:loadAffinities	()V
    //   779: invokestatic 235	com/wurmonline/server/players/PlayerInfoFactory:loadReferers	()V
    //   782: invokestatic 236	com/wurmonline/server/zones/Dens:loadDens	()V
    //   785: invokestatic 237	com/wurmonline/server/players/Reimbursement:loadAll	()V
    //   788: invokestatic 238	com/wurmonline/server/players/PendingAccount:loadAllPendingAccounts	()V
    //   791: invokestatic 239	com/wurmonline/server/intra/PasswordTransfer:loadAllPasswordTransfers	()V
    //   794: invokestatic 240	com/wurmonline/server/zones/Trap:loadAllTraps	()V
    //   797: aload_0
    //   798: invokevirtual 241	com/wurmonline/server/Server:setExternalIp	()V
    //   801: aload_0
    //   802: invokespecial 242	com/wurmonline/server/Server:setInternalIp	()V
    //   805: invokestatic 243	com/wurmonline/server/players/AchievementGenerator:generateAchievements	()V
    //   808: invokestatic 244	com/wurmonline/server/players/Achievements:loadAllAchievements	()V
    //   811: getstatic 245	com/wurmonline/server/Constants:isGameServer	Z
    //   814: ifeq +6 -> 820
    //   817: invokestatic 246	com/wurmonline/server/zones/Zones:writeZones	()V
    //   820: getstatic 247	com/wurmonline/server/Constants:dropTemporaryDatabaseIndicesAtStartup	Z
    //   823: ifeq +9 -> 832
    //   826: invokestatic 248	com/wurmonline/server/utils/DbIndexManager:removeIndexes	()V
    //   829: goto +11 -> 840
    //   832: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   835: ldc -7
    //   837: invokevirtual 156	java/util/logging/Logger:warning	(Ljava/lang/String;)V
    //   840: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   843: getfield 250	com/wurmonline/server/ServerEntry:entryServer	Z
    //   846: putstatic 251	com/wurmonline/server/zones/TilePoller:entryServer	Z
    //   849: invokestatic 252	com/wurmonline/server/webinterface/WcEpicKarmaCommand:loadAllKarmaHelpers	()V
    //   852: invokestatic 253	com/wurmonline/server/zones/FocusZone:loadAll	()V
    //   855: invokestatic 254	com/wurmonline/server/epic/Hota:loadAllHotaItems	()V
    //   858: invokestatic 255	com/wurmonline/server/epic/Hota:loadAllHelpers	()V
    //   861: getstatic 256	com/wurmonline/server/Constants:createSeeds	Z
    //   864: ifne +10 -> 874
    //   867: aload_0
    //   868: getfield 62	com/wurmonline/server/Server:needSeeds	Z
    //   871: ifeq +6 -> 877
    //   874: invokestatic 257	com/wurmonline/server/zones/Zones:createSeeds	()V
    //   877: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   880: getfield 258	com/wurmonline/server/ServerEntry:testServer	Z
    //   883: iconst_1
    //   884: if_icmpne +6 -> 890
    //   887: invokestatic 259	com/wurmonline/server/zones/Zones:createInvestigatables	()V
    //   890: aload_0
    //   891: new 260	com/wurmonline/server/intra/IntraServer
    //   894: dup
    //   895: aload_0
    //   896: invokespecial 261	com/wurmonline/server/intra/IntraServer:<init>	(Lcom/wurmonline/server/ServerMonitoring;)V
    //   899: putfield 262	com/wurmonline/server/Server:intraServer	Lcom/wurmonline/server/intra/IntraServer;
    //   902: invokestatic 263	com/wurmonline/server/statistics/Statistics:getInstance	()Lcom/wurmonline/server/statistics/Statistics;
    //   905: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   908: invokevirtual 264	com/wurmonline/server/statistics/Statistics:startup	(Ljava/util/logging/Logger;)V
    //   911: invokestatic 265	com/wurmonline/server/WurmHarvestables:setStartTimes	()V
    //   914: invokestatic 266	com/wurmonline/server/items/WurmMail:loadAllMails	()V
    //   917: invokestatic 267	com/wurmonline/server/HistoryManager:loadHistory	()V
    //   920: invokestatic 268	com/wurmonline/server/players/Cultist:loadAllCultists	()V
    //   923: invokestatic 269	com/wurmonline/server/epic/Effectuator:loadEffects	()V
    //   926: invokestatic 270	com/wurmonline/server/epic/EpicServerStatus:loadLocalEntries	()V
    //   929: invokestatic 271	com/wurmonline/server/support/Tickets:loadTickets	()V
    //   932: invokestatic 272	com/wurmonline/server/support/VoteQuestions:loadVoteQuestions	()V
    //   935: invokestatic 273	com/wurmonline/server/players/PlayerVotes:loadAllPlayerVotes	()V
    //   938: invokestatic 274	com/wurmonline/server/items/Recipes:loadAllRecipes	()V
    //   941: invokestatic 275	com/wurmonline/server/items/ItemMealData:loadAllMealData	()I
    //   944: pop
    //   945: invokestatic 276	com/wurmonline/server/skills/AffinitiesTimed:loadAllPlayerTimedAffinities	()I
    //   948: pop
    //   949: invokestatic 277	com/wurmonline/server/villages/VillageMessages:loadVillageMessages	()V
    //   952: getstatic 159	com/wurmonline/server/Constants:RUNBATCH	Z
    //   955: ifeq +3 -> 958
    //   958: iconst_0
    //   959: putstatic 159	com/wurmonline/server/Constants:RUNBATCH	Z
    //   962: getstatic 278	com/wurmonline/server/Constants:useMultiThreadedBankPolling	Z
    //   965: ifne +9 -> 974
    //   968: getstatic 279	com/wurmonline/server/Constants:useQueueToSendDataToPlayers	Z
    //   971: ifeq +13 -> 984
    //   974: aload_0
    //   975: bipush 20
    //   977: invokespecial 280	com/wurmonline/server/Server:initialiseExecutorService	(I)V
    //   980: aload_0
    //   981: invokevirtual 281	com/wurmonline/server/Server:initialisePlayerCommunicatorSender	()V
    //   984: aload_0
    //   985: invokespecial 282	com/wurmonline/server/Server:setupScheduledExecutors	()V
    //   988: invokestatic 283	com/wurmonline/server/Eigc:loadAllAccounts	()V
    //   991: aload_0
    //   992: new 284	com/wurmonline/communication/SocketServer
    //   995: dup
    //   996: aload_0
    //   997: getfield 67	com/wurmonline/server/Server:externalIp	[B
    //   1000: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1003: getfield 285	com/wurmonline/server/ServerEntry:EXTERNALPORT	Ljava/lang/String;
    //   1006: invokestatic 286	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1009: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1012: getfield 285	com/wurmonline/server/ServerEntry:EXTERNALPORT	Ljava/lang/String;
    //   1015: invokestatic 286	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   1018: iconst_1
    //   1019: iadd
    //   1020: aload_0
    //   1021: invokespecial 287	com/wurmonline/communication/SocketServer:<init>	([BIILcom/wurmonline/communication/ServerListener;)V
    //   1024: putfield 288	com/wurmonline/server/Server:socketServer	Lcom/wurmonline/communication/SocketServer;
    //   1027: getstatic 289	com/wurmonline/server/Constants:minMillisBetweenPlayerConns	J
    //   1030: putstatic 290	com/wurmonline/communication/SocketServer:MIN_MILLIS_BETWEEN_CONNECTIONS	J
    //   1033: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   1036: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   1039: new 27	java/lang/StringBuilder
    //   1042: dup
    //   1043: invokespecial 28	java/lang/StringBuilder:<init>	()V
    //   1046: ldc_w 291
    //   1049: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1052: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1055: getfield 83	com/wurmonline/server/ServerEntry:EXTERNALIP	Ljava/lang/String;
    //   1058: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1061: ldc_w 292
    //   1064: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1067: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1070: getfield 285	com/wurmonline/server/ServerEntry:EXTERNALPORT	Ljava/lang/String;
    //   1073: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1076: ldc_w 293
    //   1079: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1082: getstatic 290	com/wurmonline/communication/SocketServer:MIN_MILLIS_BETWEEN_CONNECTIONS	J
    //   1085: invokevirtual 294	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   1088: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1091: invokevirtual 10	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   1094: iconst_1
    //   1095: putstatic 295	com/wurmonline/server/Server:commPollCounterInit	I
    //   1098: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1101: getfield 296	com/wurmonline/server/ServerEntry:PVPSERVER	Z
    //   1104: ifne +17 -> 1121
    //   1107: getstatic 297	com/wurmonline/server/zones/Zones:worldTileSizeX	I
    //   1110: sipush 5000
    //   1113: if_icmple +8 -> 1121
    //   1116: bipush 6
    //   1118: putstatic 295	com/wurmonline/server/Server:commPollCounterInit	I
    //   1121: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   1124: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   1127: new 27	java/lang/StringBuilder
    //   1130: dup
    //   1131: invokespecial 28	java/lang/StringBuilder:<init>	()V
    //   1134: ldc_w 298
    //   1137: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1140: getstatic 295	com/wurmonline/server/Server:commPollCounterInit	I
    //   1143: invokevirtual 35	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   1146: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1149: invokevirtual 10	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   1152: getstatic 299	com/wurmonline/server/Constants:useScheduledExecutorForServer	Z
    //   1155: ifeq +49 -> 1204
    //   1158: getstatic 300	com/wurmonline/server/Constants:scheduledExecutorServiceThreads	I
    //   1161: invokestatic 301	java/util/concurrent/Executors:newScheduledThreadPool	(I)Ljava/util/concurrent/ScheduledExecutorService;
    //   1164: astore_3
    //   1165: iconst_0
    //   1166: istore 4
    //   1168: iload 4
    //   1170: getstatic 300	com/wurmonline/server/Constants:scheduledExecutorServiceThreads	I
    //   1173: if_icmpge +28 -> 1201
    //   1176: aload_3
    //   1177: aload_0
    //   1178: iload 4
    //   1180: iconst_2
    //   1181: imul
    //   1182: i2l
    //   1183: ldc2_w 302
    //   1186: getstatic 304	java/util/concurrent/TimeUnit:MILLISECONDS	Ljava/util/concurrent/TimeUnit;
    //   1189: invokeinterface 305 7 0
    //   1194: pop
    //   1195: iinc 4 1
    //   1198: goto -30 -> 1168
    //   1201: goto +26 -> 1227
    //   1204: new 306	java/util/Timer
    //   1207: dup
    //   1208: invokespecial 307	java/util/Timer:<init>	()V
    //   1211: astore_3
    //   1212: aload_3
    //   1213: aload_0
    //   1214: lconst_0
    //   1215: ldc2_w 302
    //   1218: invokevirtual 308	java/util/Timer:scheduleAtFixedRate	(Ljava/util/TimerTask;JJ)V
    //   1221: invokestatic 64	java/lang/System:currentTimeMillis	()J
    //   1224: putstatic 309	com/wurmonline/server/Server:startTime	J
    //   1227: invokestatic 310	com/wurmonline/server/tutorial/Missions:getAllMissions	()[Lcom/wurmonline/server/tutorial/Mission;
    //   1230: pop
    //   1231: invokestatic 311	com/wurmonline/server/tutorial/MissionTriggers:getAllTriggers	()[Lcom/wurmonline/server/tutorial/MissionTrigger;
    //   1234: pop
    //   1235: invokestatic 312	com/wurmonline/server/tutorial/TriggerEffects:getAllEffects	()[Lcom/wurmonline/server/tutorial/TriggerEffect;
    //   1238: pop
    //   1239: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1242: getfield 162	com/wurmonline/server/ServerEntry:LOGINSERVER	Z
    //   1245: ifeq +28 -> 1273
    //   1248: invokestatic 313	com/wurmonline/server/epic/EpicServerStatus:getValrei	()Lcom/wurmonline/server/epic/HexMap;
    //   1251: putstatic 314	com/wurmonline/server/Server:epicMap	Lcom/wurmonline/server/epic/HexMap;
    //   1254: getstatic 314	com/wurmonline/server/Server:epicMap	Lcom/wurmonline/server/epic/HexMap;
    //   1257: invokevirtual 315	com/wurmonline/server/epic/HexMap:loadAllEntities	()V
    //   1260: getstatic 314	com/wurmonline/server/Server:epicMap	Lcom/wurmonline/server/epic/HexMap;
    //   1263: aload_0
    //   1264: invokevirtual 316	com/wurmonline/server/epic/HexMap:addListener	(Lcom/wurmonline/server/epic/EpicMapListener;)V
    //   1267: getstatic 314	com/wurmonline/server/Server:epicMap	Lcom/wurmonline/server/epic/HexMap;
    //   1270: invokestatic 317	com/wurmonline/server/epic/EpicXmlWriter:dumpEntities	(Lcom/wurmonline/server/epic/HexMap;)V
    //   1273: getstatic 131	com/wurmonline/server/Features$Feature:SURFACEWATER	Lcom/wurmonline/server/Features$Feature;
    //   1276: invokevirtual 132	com/wurmonline/server/Features$Feature:isEnabled	()Z
    //   1279: ifeq +28 -> 1307
    //   1282: aload_0
    //   1283: new 318	com/wurmonline/server/zones/Water
    //   1286: dup
    //   1287: invokespecial 319	com/wurmonline/server/zones/Water:<init>	()V
    //   1290: putfield 75	com/wurmonline/server/Server:waterThread	Lcom/wurmonline/server/zones/Water;
    //   1293: aload_0
    //   1294: getfield 75	com/wurmonline/server/Server:waterThread	Lcom/wurmonline/server/zones/Water;
    //   1297: invokevirtual 320	com/wurmonline/server/zones/Water:loadSprings	()V
    //   1300: aload_0
    //   1301: getfield 75	com/wurmonline/server/Server:waterThread	Lcom/wurmonline/server/zones/Water;
    //   1304: invokevirtual 321	com/wurmonline/server/zones/Water:start	()V
    //   1307: getstatic 322	com/wurmonline/server/Constants:startChallenge	Z
    //   1310: ifeq +40 -> 1350
    //   1313: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1316: invokestatic 64	java/lang/System:currentTimeMillis	()J
    //   1319: invokevirtual 323	com/wurmonline/server/ServerEntry:setChallengeStarted	(J)V
    //   1322: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1325: invokestatic 64	java/lang/System:currentTimeMillis	()J
    //   1328: getstatic 324	com/wurmonline/server/Constants:challengeDays	I
    //   1331: i2l
    //   1332: ldc2_w 325
    //   1335: lmul
    //   1336: ladd
    //   1337: invokevirtual 327	com/wurmonline/server/ServerEntry:setChallengeEnds	(J)V
    //   1340: getstatic 82	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   1343: invokevirtual 328	com/wurmonline/server/ServerEntry:saveChallengeTimes	()V
    //   1346: iconst_0
    //   1347: putstatic 322	com/wurmonline/server/Constants:startChallenge	Z
    //   1350: invokestatic 329	com/wurmonline/server/statistics/ChallengeSummary:loadLocalChallengeScores	()V
    //   1353: invokestatic 53	com/wurmonline/server/creatures/Creatures:getInstance	()Lcom/wurmonline/server/creatures/Creatures;
    //   1356: invokevirtual 330	com/wurmonline/server/creatures/Creatures:startPollTask	()V
    //   1359: aload_0
    //   1360: getfield 79	com/wurmonline/server/Server:steamHandler	Lcom/wurmonline/server/steam/SteamHandler;
    //   1363: invokevirtual 331	com/wurmonline/server/steam/SteamHandler:initializeSteam	()V
    //   1366: aload_0
    //   1367: getfield 79	com/wurmonline/server/Server:steamHandler	Lcom/wurmonline/server/steam/SteamHandler;
    //   1370: ldc_w 332
    //   1373: ldc_w 332
    //   1376: ldc_w 333
    //   1379: ldc_w 334
    //   1382: invokevirtual 335	com/wurmonline/server/steam/SteamHandler:createServer	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    //   1385: invokestatic 336	com/wurmonline/server/deities/DbRitual:loadRiteEvents	()V
    //   1388: invokestatic 337	com/wurmonline/server/deities/DbRitual:loadRiteClaims	()V
    //   1391: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   1394: ldc_w 338
    //   1397: invokevirtual 93	java/util/logging/Logger:info	(Ljava/lang/String;)V
    //   1400: return
    // Line number table:
    //   Java source line #683	-> byte code offset #0
    //   Java source line #684	-> byte code offset #4
    //   Java source line #687	-> byte code offset #7
    //   Java source line #693	-> byte code offset #11
    //   Java source line #694	-> byte code offset #14
    //   Java source line #696	-> byte code offset #20
    //   Java source line #699	-> byte code offset #35
    //   Java source line #705	-> byte code offset #45
    //   Java source line #708	-> byte code offset #53
    //   Java source line #710	-> byte code offset #59
    //   Java source line #714	-> byte code offset #71
    //   Java source line #716	-> byte code offset #79
    //   Java source line #718	-> byte code offset #85
    //   Java source line #722	-> byte code offset #97
    //   Java source line #724	-> byte code offset #105
    //   Java source line #726	-> byte code offset #111
    //   Java source line #730	-> byte code offset #123
    //   Java source line #733	-> byte code offset #131
    //   Java source line #736	-> byte code offset #135
    //   Java source line #738	-> byte code offset #141
    //   Java source line #742	-> byte code offset #145
    //   Java source line #744	-> byte code offset #155
    //   Java source line #745	-> byte code offset #163
    //   Java source line #748	-> byte code offset #171
    //   Java source line #749	-> byte code offset #175
    //   Java source line #750	-> byte code offset #179
    //   Java source line #751	-> byte code offset #183
    //   Java source line #752	-> byte code offset #187
    //   Java source line #753	-> byte code offset #191
    //   Java source line #756	-> byte code offset #218
    //   Java source line #761	-> byte code offset #225
    //   Java source line #758	-> byte code offset #228
    //   Java source line #760	-> byte code offset #229
    //   Java source line #762	-> byte code offset #231
    //   Java source line #764	-> byte code offset #240
    //   Java source line #766	-> byte code offset #243
    //   Java source line #767	-> byte code offset #249
    //   Java source line #769	-> byte code offset #252
    //   Java source line #770	-> byte code offset #255
    //   Java source line #773	-> byte code offset #264
    //   Java source line #777	-> byte code offset #275
    //   Java source line #778	-> byte code offset #284
    //   Java source line #777	-> byte code offset #287
    //   Java source line #778	-> byte code offset #297
    //   Java source line #779	-> byte code offset #299
    //   Java source line #780	-> byte code offset #311
    //   Java source line #781	-> byte code offset #320
    //   Java source line #782	-> byte code offset #329
    //   Java source line #783	-> byte code offset #340
    //   Java source line #784	-> byte code offset #343
    //   Java source line #787	-> byte code offset #346
    //   Java source line #788	-> byte code offset #349
    //   Java source line #791	-> byte code offset #352
    //   Java source line #793	-> byte code offset #358
    //   Java source line #797	-> byte code offset #364
    //   Java source line #801	-> byte code offset #372
    //   Java source line #803	-> byte code offset #381
    //   Java source line #806	-> byte code offset #384
    //   Java source line #830	-> byte code offset #390
    //   Java source line #832	-> byte code offset #396
    //   Java source line #834	-> byte code offset #402
    //   Java source line #835	-> byte code offset #411
    //   Java source line #839	-> byte code offset #415
    //   Java source line #841	-> byte code offset #421
    //   Java source line #842	-> byte code offset #424
    //   Java source line #843	-> byte code offset #427
    //   Java source line #844	-> byte code offset #430
    //   Java source line #845	-> byte code offset #433
    //   Java source line #846	-> byte code offset #436
    //   Java source line #847	-> byte code offset #439
    //   Java source line #848	-> byte code offset #442
    //   Java source line #849	-> byte code offset #445
    //   Java source line #850	-> byte code offset #448
    //   Java source line #851	-> byte code offset #454
    //   Java source line #852	-> byte code offset #460
    //   Java source line #853	-> byte code offset #466
    //   Java source line #854	-> byte code offset #469
    //   Java source line #855	-> byte code offset #472
    //   Java source line #856	-> byte code offset #475
    //   Java source line #857	-> byte code offset #478
    //   Java source line #858	-> byte code offset #481
    //   Java source line #859	-> byte code offset #484
    //   Java source line #860	-> byte code offset #487
    //   Java source line #861	-> byte code offset #490
    //   Java source line #862	-> byte code offset #493
    //   Java source line #863	-> byte code offset #496
    //   Java source line #864	-> byte code offset #512
    //   Java source line #866	-> byte code offset #519
    //   Java source line #868	-> byte code offset #522
    //   Java source line #870	-> byte code offset #531
    //   Java source line #871	-> byte code offset #542
    //   Java source line #872	-> byte code offset #549
    //   Java source line #873	-> byte code offset #552
    //   Java source line #875	-> byte code offset #561
    //   Java source line #876	-> byte code offset #564
    //   Java source line #882	-> byte code offset #567
    //   Java source line #883	-> byte code offset #570
    //   Java source line #890	-> byte code offset #577
    //   Java source line #885	-> byte code offset #580
    //   Java source line #887	-> byte code offset #581
    //   Java source line #888	-> byte code offset #595
    //   Java source line #889	-> byte code offset #599
    //   Java source line #892	-> byte code offset #600
    //   Java source line #893	-> byte code offset #603
    //   Java source line #894	-> byte code offset #606
    //   Java source line #895	-> byte code offset #609
    //   Java source line #896	-> byte code offset #612
    //   Java source line #897	-> byte code offset #615
    //   Java source line #898	-> byte code offset #618
    //   Java source line #899	-> byte code offset #621
    //   Java source line #901	-> byte code offset #624
    //   Java source line #902	-> byte code offset #627
    //   Java source line #903	-> byte code offset #630
    //   Java source line #904	-> byte code offset #633
    //   Java source line #905	-> byte code offset #636
    //   Java source line #906	-> byte code offset #640
    //   Java source line #907	-> byte code offset #644
    //   Java source line #908	-> byte code offset #653
    //   Java source line #909	-> byte code offset #669
    //   Java source line #911	-> byte code offset #710
    //   Java source line #912	-> byte code offset #713
    //   Java source line #914	-> byte code offset #722
    //   Java source line #920	-> byte code offset #729
    //   Java source line #922	-> byte code offset #747
    //   Java source line #924	-> byte code offset #754
    //   Java source line #928	-> byte code offset #758
    //   Java source line #929	-> byte code offset #767
    //   Java source line #930	-> byte code offset #770
    //   Java source line #931	-> byte code offset #773
    //   Java source line #932	-> byte code offset #776
    //   Java source line #933	-> byte code offset #779
    //   Java source line #934	-> byte code offset #782
    //   Java source line #936	-> byte code offset #785
    //   Java source line #937	-> byte code offset #788
    //   Java source line #938	-> byte code offset #791
    //   Java source line #939	-> byte code offset #794
    //   Java source line #940	-> byte code offset #797
    //   Java source line #941	-> byte code offset #801
    //   Java source line #942	-> byte code offset #805
    //   Java source line #943	-> byte code offset #808
    //   Java source line #944	-> byte code offset #811
    //   Java source line #946	-> byte code offset #817
    //   Java source line #948	-> byte code offset #820
    //   Java source line #950	-> byte code offset #826
    //   Java source line #954	-> byte code offset #832
    //   Java source line #958	-> byte code offset #840
    //   Java source line #959	-> byte code offset #849
    //   Java source line #960	-> byte code offset #852
    //   Java source line #961	-> byte code offset #855
    //   Java source line #962	-> byte code offset #858
    //   Java source line #963	-> byte code offset #861
    //   Java source line #964	-> byte code offset #874
    //   Java source line #965	-> byte code offset #877
    //   Java source line #966	-> byte code offset #887
    //   Java source line #967	-> byte code offset #890
    //   Java source line #969	-> byte code offset #902
    //   Java source line #972	-> byte code offset #911
    //   Java source line #975	-> byte code offset #914
    //   Java source line #980	-> byte code offset #917
    //   Java source line #981	-> byte code offset #920
    //   Java source line #982	-> byte code offset #923
    //   Java source line #983	-> byte code offset #926
    //   Java source line #984	-> byte code offset #929
    //   Java source line #985	-> byte code offset #932
    //   Java source line #987	-> byte code offset #935
    //   Java source line #988	-> byte code offset #938
    //   Java source line #989	-> byte code offset #941
    //   Java source line #990	-> byte code offset #945
    //   Java source line #991	-> byte code offset #949
    //   Java source line #999	-> byte code offset #952
    //   Java source line #1015	-> byte code offset #958
    //   Java source line #1017	-> byte code offset #962
    //   Java source line #1019	-> byte code offset #974
    //   Java source line #1020	-> byte code offset #980
    //   Java source line #1022	-> byte code offset #984
    //   Java source line #1026	-> byte code offset #988
    //   Java source line #1027	-> byte code offset #991
    //   Java source line #1028	-> byte code offset #1015
    //   Java source line #1029	-> byte code offset #1027
    //   Java source line #1031	-> byte code offset #1033
    //   Java source line #1036	-> byte code offset #1094
    //   Java source line #1037	-> byte code offset #1098
    //   Java source line #1039	-> byte code offset #1107
    //   Java source line #1040	-> byte code offset #1116
    //   Java source line #1044	-> byte code offset #1121
    //   Java source line #1048	-> byte code offset #1152
    //   Java source line #1051	-> byte code offset #1158
    //   Java source line #1052	-> byte code offset #1165
    //   Java source line #1055	-> byte code offset #1176
    //   Java source line #1052	-> byte code offset #1195
    //   Java source line #1057	-> byte code offset #1201
    //   Java source line #1060	-> byte code offset #1204
    //   Java source line #1061	-> byte code offset #1212
    //   Java source line #1063	-> byte code offset #1221
    //   Java source line #1065	-> byte code offset #1227
    //   Java source line #1066	-> byte code offset #1231
    //   Java source line #1067	-> byte code offset #1235
    //   Java source line #1068	-> byte code offset #1239
    //   Java source line #1070	-> byte code offset #1248
    //   Java source line #1071	-> byte code offset #1254
    //   Java source line #1072	-> byte code offset #1260
    //   Java source line #1073	-> byte code offset #1267
    //   Java source line #1075	-> byte code offset #1273
    //   Java source line #1077	-> byte code offset #1282
    //   Java source line #1078	-> byte code offset #1293
    //   Java source line #1079	-> byte code offset #1300
    //   Java source line #1081	-> byte code offset #1307
    //   Java source line #1083	-> byte code offset #1313
    //   Java source line #1084	-> byte code offset #1322
    //   Java source line #1085	-> byte code offset #1340
    //   Java source line #1086	-> byte code offset #1346
    //   Java source line #1089	-> byte code offset #1350
    //   Java source line #1091	-> byte code offset #1353
    //   Java source line #1093	-> byte code offset #1359
    //   Java source line #1094	-> byte code offset #1366
    //   Java source line #1097	-> byte code offset #1385
    //   Java source line #1098	-> byte code offset #1388
    //   Java source line #1100	-> byte code offset #1391
    //   Java source line #1101	-> byte code offset #1400
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1401	0	this	Server
    //   224	1	1	bool	boolean
    //   228	2	1	ex	Exception
    //   580	12	1	nsc	NoSuchCreatureException
    //   639	52	1	start	long
    //   287	11	2	localObject	Object
    //   1164	13	3	scheduledServerRunExecutor	ScheduledExecutorService
    //   1211	2	3	timer	java.util.Timer
    //   1166	30	4	i	int
    // Exception table:
    //   from	to	target	type
    //   218	225	228	java/lang/Exception
    //   264	275	287	finally
    //   567	577	580	com/wurmonline/server/creatures/NoSuchCreatureException
  }
  
  private void setupScheduledExecutors()
  {
    if ((Constants.useScheduledExecutorToWriteLogs) || (Constants.useScheduledExecutorToSaveConstants) || (Constants.useScheduledExecutorToTickCalendar) || (Constants.useScheduledExecutorToCountEggs) || (Constants.useScheduledExecutorToSaveDirtyMeshRows) || (Constants.useScheduledExecutorToSendTimeSync) || (Constants.useScheduledExecutorToSwitchFatigue) || (Constants.useScheduledExecutorToUpdateCreaturePositionInDatabase) || (Constants.useScheduledExecutorToUpdateItemDamageInDatabase) || (Constants.useScheduledExecutorToUpdateItemOwnerInDatabase) || (Constants.useScheduledExecutorToUpdateItemLastOwnerInDatabase) || (Constants.useScheduledExecutorToUpdateItemParentInDatabase) || (Constants.useScheduledExecutorToConnectToTwitter) || (Constants.useScheduledExecutorToUpdatePlayerPositionInDatabase) || (Constants.useItemTransferLog) || (Constants.useTileEventLog)) {
      this.scheduledExecutorService = Executors.newScheduledThreadPool(15);
    }
    if (Constants.useScheduledExecutorToWriteLogs)
    {
      logger.info("Going to use a ScheduledExecutorService to write logs");
      long lInitialDelay = 60L;
      long lDelay = 300L;
      
      this.scheduledExecutorService.scheduleWithFixedDelay(new Server.1(this), 60L, 300L, TimeUnit.SECONDS);
      
      long lPingDelay = 300L;
      long lInitialDelay2 = 5000L;
      
      this.scheduledExecutorService.scheduleWithFixedDelay(new Server.2(this), 5000L, 300L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToCountEggs)
    {
      logger.info("Going to use a ScheduledExecutorService to count eggs");
      long lInitialDelay = 1000L;
      long lDelay = 3600000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new Items.EggCounter(), 1000L, 3600000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToSaveConstants)
    {
      logger.info("Going to use a ScheduledExecutorService to save Constants to wurm.ini");
      long lInitialDelay = 1000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new Constants.ConstantsSaver(), 1000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToSaveDirtyMeshRows)
    {
      logger.info("Going to use a ScheduledExecutorService to call MeshIO.saveNextDirtyRow()");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      long delayInterval = 250L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new MeshSaver(surfaceMesh, "SurfaceMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS);
      
      lInitialDelay += 250L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new MeshSaver(caveMesh, "CaveMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS);
      
      lInitialDelay += 250L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new MeshSaver(rockMesh, "RockMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS);
      
      lInitialDelay += 250L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new MeshSaver(resourceMesh, "ResourceMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS);
      
      lInitialDelay += 250L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new MeshSaver(flagsMesh, "FlagsMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToSendTimeSync) {
      if (Servers.localServer.LOGINSERVER)
      {
        logger.warning("This is the login server so it will not send TimeSync commands");
      }
      else
      {
        logger.info("Going to use a ScheduledExecutorService to send TimeSync commands");
        long lInitialDelay = 1000L;
        long lDelay = 3600000L;
        this.scheduledExecutorService.scheduleWithFixedDelay(new TimeSync.TimeSyncSender(), 1000L, 3600000L, TimeUnit.MILLISECONDS);
      }
    }
    if (Constants.useScheduledExecutorToSwitchFatigue)
    {
      logger.info("Going to use a ScheduledExecutorService to switch fatigue");
      long lInitialDelay = 60000L;
      long lDelay = 86400000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new PlayerInfoFactory.FatigueSwitcher(), 60000L, 86400000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToTickCalendar)
    {
      logger.info("Going to use a ScheduledExecutorService to call WurmCalendar.tickSeconds()");
      long lInitialDelay = 125L;
      long lDelay = 125L;
      this.scheduledExecutorService.scheduleWithFixedDelay(new WurmCalendar.Ticker(), 125L, 125L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useItemTransferLog)
    {
      logger.info("Going to use a ScheduledExecutorService to log Item Transfers");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(Item.getItemlogger(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useTileEventLog)
    {
      logger.info("Going to use a ScheduledExecutorService to log tile events");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(TileEvent.getTilelogger(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToUpdateCreaturePositionInDatabase)
    {
      logger.info("Going to use a ScheduledExecutorService to update creature positions in database");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(CreaturePos.getCreatureDbPosUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToUpdatePlayerPositionInDatabase)
    {
      logger.info("Going to use a ScheduledExecutorService to update player positions in database");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(CreaturePos.getPlayerDbPosUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToUpdateItemDamageInDatabase)
    {
      logger.info("Going to use a ScheduledExecutorService to update item damage in database");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemDamageDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToUpdateItemOwnerInDatabase)
    {
      logger.info("Going to use a ScheduledExecutorService to update item owner in database");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemOwnerDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToUpdateItemLastOwnerInDatabase)
    {
      logger.info("Going to use a ScheduledExecutorService to update item last owner in database");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemLastOwnerDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToUpdateItemParentInDatabase)
    {
      logger.info("Going to use a ScheduledExecutorService to update item parent in database");
      long lInitialDelay = 60000L;
      long lDelay = 1000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemParentDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorToConnectToTwitter)
    {
      logger.info("Going to use a ScheduledExecutorService to connect to twitter");
      long lInitialDelay = 60000L;
      long lDelay = 5000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(Twit.getTwitterThread(), 60000L, 5000L, TimeUnit.MILLISECONDS);
    }
    if (Constants.useScheduledExecutorForTrello)
    {
      logger.info("Going to use a ScheduledExecutorService for maintaining tickets in Trello");
      long lInitialDelay = 5000L;
      long lDelay = 60000L;
      this.scheduledExecutorService.scheduleWithFixedDelay(Trello.getTrelloThread(), 5000L, 60000L, TimeUnit.MILLISECONDS);
    }
  }
  
  void twitLocalServer(String message)
  {
    Twit t = Servers.localServer.createTwit(message);
    if (t != null) {
      Twit.twit(t);
    }
  }
  
  private void logCodeVersionInformation()
  {
    try
    {
      Package p = Class.forName("com.wurmonline.server.Server").getPackage();
      if (p == null)
      {
        logger.warning("Wurm Build Date: UNKNOWN (Package.getPackage() is null!)");
      }
      else
      {
        logger.info("Wurm Impl Title: " + p.getImplementationTitle());
        logger.info("Wurm Impl Vendor: " + p.getImplementationVendor());
        logger.info("Wurm Impl Version: " + p.getImplementationVersion());
      }
    }
    catch (Exception ex)
    {
      logger.severe("Wurm version: UNKNOWN (Error getting version number from MANIFEST.MF)");
    }
    try
    {
      Package p = Class.forName("com.wurmonline.shared.constants.ProtoConstants").getPackage();
      if (p == null)
      {
        logger.warning("Wurm Common: UNKNOWN (Package.getPackage() is null!)");
      }
      else
      {
        logger.info("Wurm Common Impl Title: " + p.getImplementationTitle());
        logger.info("Wurm Common Impl Vendor: " + p.getImplementationVendor());
        logger.info("Wurm Common Impl Version: " + p.getImplementationVersion());
      }
    }
    catch (Exception ex)
    {
      logger.severe("Wurm Common: UNKNOWN (Error getting version number from MANIFEST.MF)");
    }
    try
    {
      Package p = Class.forName("com.mysql.jdbc.Driver").getPackage();
      if (p == null)
      {
        logger.warning("MySQL JDBC: UNKNOWN (Package.getPackage() is null!)");
      }
      else
      {
        logger.info("MySQL JDBC Spec Title: " + p.getSpecificationTitle());
        logger.info("MySQL JDBC Spec Vendor: " + p.getSpecificationVendor());
        logger.info("MySQL JDBC Spec Version: " + p.getSpecificationVersion());
        
        logger.info("MySQL JDBC Impl Title: " + p.getImplementationTitle());
        logger.info("MySQL JDBC Impl Vendor: " + p.getImplementationVendor());
        logger.info("MySQL JDBC Impl Version: " + p.getImplementationVersion());
      }
    }
    catch (Exception ex)
    {
      logger.severe("MySQL JDBC: UNKNOWN (Error getting version number from MANIFEST.MF)");
    }
    try
    {
      Package p = Class.forName("javax.mail.Message").getPackage();
      if (p == null)
      {
        logger.warning("Javax Mail: UNKNOWN (Package.getPackage() is null!)");
      }
      else
      {
        logger.info("Javax Mail Spec Title: " + p.getSpecificationTitle());
        logger.info("Javax Mail Spec Vendor: " + p.getSpecificationVendor());
        logger.info("Javax Mail Spec Version: " + p.getSpecificationVersion());
        
        logger.info("Javax Mail Impl Title: " + p.getImplementationTitle());
        logger.info("Javax Mail Impl Vendor: " + p.getImplementationVendor());
        logger.info("Javax Mail Impl Version: " + p.getImplementationVersion());
      }
    }
    catch (Exception ex)
    {
      logger.severe("Javax Mail: UNKNOWN (Error getting version number from MANIFEST.MF)");
    }
    try
    {
      Package p = Class.forName("javax.activation.DataSource").getPackage();
      if (p == null)
      {
        logger.warning("Javax Activation: UNKNOWN (Package.getPackage() is null!)");
      }
      else
      {
        logger.info("Javax Activation Spec Title: " + p.getSpecificationTitle());
        logger.info("Javax Activation Spec Vendor: " + p.getSpecificationVendor());
        logger.info("Javax Activation Spec Version: " + p.getSpecificationVersion());
        
        logger.info("Javax Activation Impl Title: " + p.getImplementationTitle());
        logger.info("Javax Activation Impl Vendor: " + p.getImplementationVendor());
        logger.info("Javax Activation Impl Version: " + p.getImplementationVersion());
      }
    }
    catch (Exception ex)
    {
      logger.severe("Javax Activation: UNKNOWN (Error getting version number from MANIFEST.MF)");
    }
  }
  
  public void initialisePlayerCommunicatorSender()
  {
    if (Constants.useQueueToSendDataToPlayers)
    {
      playerCommunicatorSender = new PlayerCommunicatorSender();
      getMainExecutorService().execute(playerCommunicatorSender);
    }
  }
  
  private static void fixHoles()
  {
    logger.log(Level.INFO, "Fixing cave entrances.");
    long start = System.nanoTime();
    int found = 0;
    int fixed = 0;
    int fixed2 = 0;
    int fixed3 = 0;
    int fixed4 = 0;
    int fixed5 = 0;
    int fixedWalls = 0;
    int min = 0;
    int ms = Constants.meshSize;
    int max = 1 << ms;
    for (int x = 0; x < max; x++) {
      for (int y = 0; y < max; y++)
      {
        int tile = surfaceMesh.getTile(x, y);
        if (Tiles.decodeType(tile) == Tiles.Tile.TILE_HOLE.id)
        {
          found++;
          boolean fix = false;
          int t = caveMesh.getTile(x, y);
          if (Tiles.decodeType(t) != Tiles.Tile.TILE_CAVE_EXIT.id)
          {
            fixed++;
            setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_ROCK.id, (byte)0);
          }
          else
          {
            for (int xx = 0; xx <= 1; xx++) {
              for (int yy = 0; yy <= 1; yy++)
              {
                int tt = caveMesh.getTile(x + xx, y + yy);
                if ((Tiles.decodeHeight(tt) == -100) && (Tiles.decodeData(tt) == 0))
                {
                  fix = true;
                  break;
                }
              }
            }
            if (fix)
            {
              fixed2++;
              for (int xx = 0; xx <= 1; xx++) {
                for (int yy = 0; yy <= 1; yy++) {
                  caveMesh.setTile(x + xx, y + yy, 
                  
                    Tiles.encode((short)-100, 
                    TileRockBehaviour.prospect(x + xx, y + yy, false), (byte)0));
                }
              }
              setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_ROCK.id, (byte)0);
            }
          }
          if (!fix)
          {
            int lowestX = 100000;
            int lowestY = 100000;
            int nextLowestX = lowestX;
            int nextLowestY = lowestY;
            int lowestHeight = 100000;
            int nextLowestHeight = lowestHeight;
            for (int xa = 0; xa <= 1; xa++) {
              for (int ya = 0; ya <= 1; ya++) {
                if ((x + xa < max) && (y + ya < max))
                {
                  int rockTile = rockMesh.getTile(x + xa, y + ya);
                  int rockHeight = Tiles.decodeHeight(rockTile);
                  if (rockHeight <= lowestHeight)
                  {
                    if ((lowestHeight < nextLowestHeight) && 
                      (TileRockBehaviour.isAdjacent(lowestX, lowestY, x + xa, y + ya)))
                    {
                      nextLowestHeight = lowestHeight;
                      nextLowestX = lowestX;
                      nextLowestY = lowestY;
                    }
                    lowestHeight = rockHeight;
                    lowestX = x + xa;
                    lowestY = y + ya;
                  }
                  else if ((rockHeight <= nextLowestHeight) && (nextLowestHeight > lowestHeight) && 
                    (TileRockBehaviour.isAdjacent(lowestX, lowestY, x + xa, y + ya)))
                  {
                    nextLowestHeight = rockHeight;
                    nextLowestX = x + xa;
                    nextLowestY = y + ya;
                  }
                }
              }
            }
            if ((lowestX != 100000) && (lowestY != 100000) && (nextLowestX != 100000) && (nextLowestY != 100000))
            {
              int lowestRock = rockMesh.getTile(lowestX, lowestY);
              int nextLowestRock = rockMesh.getTile(nextLowestX, nextLowestY);
              int lowestCave = caveMesh.getTile(lowestX, lowestY);
              int nextLowestCave = caveMesh.getTile(nextLowestX, nextLowestY);
              int lowestSurf = surfaceMesh.getTile(lowestX, lowestY);
              int nextLowestSurf = surfaceMesh.getTile(nextLowestX, nextLowestY);
              short lrockHeight = Tiles.decodeHeight(lowestRock);
              short nlrockHeight = Tiles.decodeHeight(nextLowestRock);
              short lcaveHeight = Tiles.decodeHeight(lowestCave);
              short nlcaveHeight = Tiles.decodeHeight(nextLowestCave);
              short lsurfHeight = Tiles.decodeHeight(lowestSurf);
              short nlsurfHeight = Tiles.decodeHeight(nextLowestSurf);
              if ((lcaveHeight != lrockHeight) || (Tiles.decodeData(lowestCave) != 0))
              {
                fixed4++;
                caveMesh.setTile(lowestX, lowestY, 
                  Tiles.encode(lrockHeight, Tiles.decodeType(lowestCave), (byte)0));
              }
              if ((nlcaveHeight != nlrockHeight) || (Tiles.decodeData(nextLowestCave) != 0))
              {
                fixed4++;
                caveMesh.setTile(nextLowestX, nextLowestY, 
                  Tiles.encode(nlrockHeight, Tiles.decodeType(nextLowestCave), (byte)0));
              }
              if (lsurfHeight != lrockHeight)
              {
                fixed5++;
                setSurfaceTile(lowestX, lowestY, lrockHeight, Tiles.decodeType(lowestSurf), 
                  Tiles.decodeData(lowestSurf));
              }
              if (nlsurfHeight != nlrockHeight)
              {
                fixed5++;
                setSurfaceTile(nextLowestX, nextLowestY, nlrockHeight, Tiles.decodeType(nextLowestSurf), 
                  Tiles.decodeData(nextLowestSurf));
              }
            }
          }
        }
        else
        {
          tile = caveMesh.getTile(x, y);
          if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE.id)
          {
            int minheight = -100;
            
            boolean fix = false;
            for (int xx = 0; xx <= 1; xx++) {
              for (int yy = 0; yy <= 1; yy++)
              {
                int tt = caveMesh.getTile(x + xx, y + yy);
                if ((Tiles.decodeHeight(tt) == -100) && (Tiles.decodeData(tt) == 0))
                {
                  fix = true;
                  if (Tiles.decodeHeight(tt) > minheight) {
                    minheight = Tiles.decodeHeight(tt);
                  }
                }
              }
            }
            if (fix)
            {
              fixed3++;
              for (int xx = 0; xx <= 1; xx++) {
                for (int yy = 0; yy <= 1; yy++)
                {
                  int tt = caveMesh.getTile(x + xx, y + yy);
                  int rocktile = rockMesh.getTile(x + xx, y + yy);
                  int rockHeight = Tiles.decodeHeight(rocktile);
                  int maxHeight = rockHeight - minheight;
                  if ((Tiles.decodeHeight(tt) == -100) && 
                    (Tiles.decodeData(tt) == 0)) {
                    caveMesh.setTile(x + xx, y + yy, 
                    
                      Tiles.encode((short)minheight, Tiles.decodeType(tt), 
                      (byte)Math.min(maxHeight, 5)));
                  }
                }
              }
            }
          }
          else if (Tiles.getTile(Tiles.decodeType(tile)) == null)
          {
            caveMesh.setTile(x, y, 
            
              Tiles.encode((short)-100, TileRockBehaviour.prospect(x & (1 << Constants.meshSize) - 1, y >> Constants.meshSize, false), (byte)0));
            
            logger.log(Level.INFO, "Mended a " + Tiles.decodeType(tile) + " cave tile at " + x + "," + y);
          }
          else
          {
            int cavet = caveMesh.getTile(x, y);
            if (Tiles.decodeData(cavet) != 0)
            {
              byte cceil = Tiles.decodeData(cavet);
              int caveh = Tiles.decodeHeight(cavet);
              int rockHeight = Tiles.decodeHeight(rockMesh.getTile(x, y));
              if (cceil + caveh > rockHeight)
              {
                fixedWalls++;
                int maxHeight = rockHeight - caveh;
                caveMesh.setTile(x, y, 
                
                  Tiles.encode((short)caveh, Tiles.decodeType(cavet), 
                  (byte)Math.min(maxHeight, cceil)));
              }
            }
          }
        }
      }
    }
    try
    {
      surfaceMesh.saveAll();
      logger.log(Level.INFO, "Set " + fixed + " cave entrances to rock out of " + found);
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save surfaceMesh", iox);
    }
    if ((fixed2 > 0) || (fixed3 > 0) || (fixedWalls > 0) || (fixed4 > 0) || (fixed5 > 0)) {
      try
      {
        caveMesh.saveAll();
        logger.log(Level.INFO, "Fixed " + fixed2 + " crazy cave entrances and " + fixed3 + " weird caves as well. Also fixed " + fixedWalls + " walls sticking up. Also fixed " + fixed4 + " unleavable exit nodes. Fixed " + fixed5 + " misaligned surface tile nodes.");
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to save surfaceMesh", iox);
      }
    }
    float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
    logger.info("Fixing cave entrances took " + lElapsedTime + " ms");
  }
  
  private void checkShutDown()
  {
    int secondsToShutDown = (int)(millisToShutDown / 1000L);
    if (secondsToShutDown == 2400)
    {
      if (lastSentWarning != 2400)
      {
        lastSentWarning = 2400;
        broadCastAlert("40 minutes to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
      }
    }
    else if (secondsToShutDown == 1200)
    {
      if (lastSentWarning != 1200)
      {
        lastSentWarning = 1200;
        broadCastAlert("20 minutes to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
      }
    }
    else if (secondsToShutDown == 600)
    {
      if (lastSentWarning != 600)
      {
        lastSentWarning = 600;
        broadCastAlert("10 minutes to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
      }
    }
    else if (secondsToShutDown == 300)
    {
      if (lastSentWarning != 300)
      {
        lastSentWarning = 300;
        broadCastAlert("5 minutes to shutdown. ", true, (byte)1);
        broadCastAlert(shutdownReason, true, (byte)0);
        Players.getInstance().setChallengeStep(2);
      }
    }
    else if (secondsToShutDown == 180)
    {
      if (lastSentWarning != 180)
      {
        lastSentWarning = 180;
        broadCastAlert("3 minutes to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
        Players.getInstance().setChallengeStep(3);
        Players.getInstance().setChallengeStep(4);
      }
    }
    else if (secondsToShutDown == 60)
    {
      if (lastSentWarning != 60)
      {
        lastSentWarning = 60;
        broadCastAlert("1 minute to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
      }
    }
    else if (secondsToShutDown == 30)
    {
      if (lastSentWarning != 30)
      {
        lastSentWarning = 30;
        broadCastAlert("30 seconds to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
      }
    }
    else if (secondsToShutDown == 20)
    {
      if (lastSentWarning != 20)
      {
        lastSentWarning = 20;
        broadCastAlert("20 seconds to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
      }
    }
    else if (secondsToShutDown == 10)
    {
      if (lastSentWarning != 10)
      {
        lastSentWarning = 10;
        FocusZone hotaZone = FocusZone.getHotaZone();
        if (hotaZone != null) {
          Hota.forcePillarsToWorld();
        }
        broadCastAlert("10 seconds to shutdown. ", false, (byte)1);
        broadCastAlert(shutdownReason, false, (byte)0);
      }
    }
    else if (secondsToShutDown == 3) {
      if (lastSentWarning != 1)
      {
        lastSentWarning = 1;
        broadCastAlert("Server shutting down NOW!/%7?o#### NO CARRIER", false);
        Players.getInstance().sendLogoff("The server shut down: " + shutdownReason);
        twitLocalServer("The server shut down: " + shutdownReason);
      }
    }
    if (secondsToShutDown < 120) {
      Constants.maintaining = true;
    }
  }
  
  public void run()
  {
    long now = 0L;
    
    long check = 0L;
    try
    {
      now = System.currentTimeMillis();
      check = now;
      if (Constants.isGameServer) {
        TilePoller.pollNext();
      }
      if ((!Servers.localServer.testServer) && (System.currentTimeMillis() - check > Constants.lagThreshold)) {
        logger.log(Level.INFO, "Lag detected at tilepoller.pollnext (0.1): " + 
          (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
      }
      check = System.currentTimeMillis();
      
      Zones.pollNextZones(25L);
      if (Features.Feature.CROP_POLLER.isEnabled()) {
        CropTilePoller.pollCropTiles();
      }
      Players.getInstance().pollPlayers();
      Delivery.poll();
      if ((!Servers.localServer.testServer) && (System.currentTimeMillis() - check > Constants.lagThreshold)) {
        logger.log(Level.INFO, "Lag detected at Zones.pollnextzones (0.5): " + 
          (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
      }
      if (millisToShutDown > -1000L) {
        if (millisToShutDown < 0L)
        {
          shutDown();
        }
        else
        {
          checkShutDown();
          millisToShutDown -= 25L;
        }
      }
      if (counter == 2)
      {
        VoteQuestions.handleVoting();
        VoteQuestions.handleArchiveTickets();
        if (Features.Feature.HIGHWAYS.isEnabled()) {
          Routes.handlePathsToSend();
        }
      }
      if (counter == 3)
      {
        PlayerInfoFactory.handlePlayerStateList();
        Tickets.handleArchiveTickets();
        Tickets.handleTicketsToSend();
      }
      if ((counter = (short)(counter + 1)) == 5)
      {
        if (Constants.useScheduledExecutorToTickCalendar)
        {
          if (!logger.isLoggable(Level.FINEST)) {}
        }
        else {
          WurmCalendar.tickSecond();
        }
        ServerProjectile.pollAll();
        if (now - this.lastLogged > 300000L)
        {
          this.lastLogged = now;
          if (Constants.useScheduledExecutorToWriteLogs) {
            if (logger.isLoggable(Level.FINER)) {
              logger.finer("Using a ScheduledExecutorService to write logs so do not call writePlayerLog() from main Server thread");
            }
          }
          if ((Constants.isGameServer) && 
            (System.currentTimeMillis() - Servers.localServer.getFatigueSwitch() > 86400000L))
          {
            if (Constants.useScheduledExecutorToSwitchFatigue)
            {
              if (logger.isLoggable(Level.FINER)) {
                logger.finer("Using a ScheduledExecutorService to switch fatigue so do not call PlayerInfoFactory.switchFatigue() from main Server thread");
              }
            }
            else {
              PlayerInfoFactory.switchFatigue();
            }
            Offspring.resetOffspringCounters();
            Servers.localServer.setFatigueSwitch(System.currentTimeMillis());
          }
          King.pollKings();
          Players.getInstance().checkElectors();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at 1: " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        if ((Constants.isGameServer) && (now - lastArrow > 100L))
        {
          Arrows.pollAll((float)(now - lastArrow));
          lastArrow = now;
        }
        boolean startHota = (Servers.localServer.getNextHota() > 0L) && (System.currentTimeMillis() > Servers.localServer.getNextHota());
        if (startHota) {
          Hota.poll();
        }
        if (now - lastMailCheck > 364000L)
        {
          WurmMail.poll();
          lastMailCheck = now;
        }
        if (now - lastPolledRubble > 60000L)
        {
          lastPolledRubble = System.currentTimeMillis();
          for (Fence fence : Fence.getRubbleFences()) {
            fence.poll(now);
          }
          for (Wall wall : Wall.getRubbleWalls()) {
            wall.poll(now, null, null);
          }
          if (ChallengeServer) {
            if ((Servers.localServer.getChallengeEnds() > 0L) && 
              (System.currentTimeMillis() > Servers.localServer.getChallengeEnds())) {
              if (millisToShutDown < 0L)
              {
                for (Village v : Villages.getVillages()) {
                  v.disband("System");
                }
                startShutdown(600, "The world is ending.");
                Players.getInstance().setChallengeStep(1);
              }
            }
          }
          if (tempEffects.size() > 0)
          {
            Object toRemove = new HashSet();
            for (Object entry : tempEffects.entrySet()) {
              if (System.currentTimeMillis() > ((Long)((Map.Entry)entry).getValue()).longValue()) {
                ((HashSet)toRemove).add(((Map.Entry)entry).getKey());
              }
            }
            for (??? = ((HashSet)toRemove).iterator(); ???.hasNext();)
            {
              val = (Long)???.next();
              
              tempEffects.remove(val);
              Players.getInstance().removeGlobalEffect(val.longValue());
            }
          }
        }
        Long val;
        if (now - lastPolledWater > 1000L)
        {
          pollSurfaceWater();
          lastPolledWater = System.currentTimeMillis();
        }
        if (now - lastWeather > 70000L)
        {
          check = System.currentTimeMillis();
          lastWeather = now;
          boolean setw = true;
          if (weather.tick()) {
            if (Servers.localServer.LOGINSERVER)
            {
              startSendWeatherThread();
              setw = false;
            }
          }
          if (setw) {
            Players.getInstance().setShouldSendWeather(true);
          }
          this.thunderMode = ((weather.getRain() > 0.5F) && (weather.getCloudiness() > 0.5F));
          if (WurmCalendar.isChristmas())
          {
            Zones.loadChristmas();
          }
          else if (WurmCalendar.wasTestChristmas)
          {
            WurmCalendar.wasTestChristmas = false;
            Zones.deleteChristmas();
          }
          else if (WurmCalendar.isAfterChristmas())
          {
            Zones.deleteChristmas();
          }
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Weather (2): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
          if (!startHota) {
            Hota.poll();
          }
        }
        if ((Constants.isGameServer) && (this.thunderMode)) {
          if (now - this.lastFlash > 5000L)
          {
            this.lastFlash = now;
            if (weather.getRain() - 0.5F + (weather.getCloudiness() - 0.5F) > rand.nextFloat()) {
              Zones.flash();
            }
          }
        }
        if ((Constants.isGameServer) && (now - lastSecond > 60000L))
        {
          check = System.currentTimeMillis();
          lastSecond = now;
          if (Constants.useScheduledExecutorToSaveDirtyMeshRows)
          {
            if (logger.isLoggable(Level.FINER)) {
              logger.finer("useScheduledExecutorToSaveDirtyMeshRows is true so do not save the meshes from Server.run()");
            }
          }
          else
          {
            caveMesh.saveNextDirtyRow();
            surfaceMesh.saveNextDirtyRow();
            rockMesh.saveNextDirtyRow();
            resourceMesh.saveNextDirtyRow();
            flagsMesh.saveNextDirtyRow();
          }
          MountTransfer.pruneTransfers();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Meshes.saveNextDirtyRow (4): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        if ((Constants.isGameServer) && (now - lastPolledSkills > 21600000L))
        {
          check = System.currentTimeMillis();
          SkillStat.pollSkills();
          lastPolledSkills = System.currentTimeMillis();
          EndGameItems.pollAll();
          Trap.checkUpdate();
          Items.pollUnstableRifts();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at pollskills (4.5): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
          if (System.currentTimeMillis() - Servers.localServer.getLastSpawnedUnique() > 1209600000L) {
            Dens.checkDens(true);
          }
        }
        if ((Constants.isGameServer) && (now - lastResetTiles > 14400000L))
        {
          Zones.saveProtectedTiles();
          lastResetTiles = System.currentTimeMillis();
        }
        if (Servers.localServer.LOGINSERVER) {
          if (System.currentTimeMillis() > Servers.localServer.getNextEpicPoll())
          {
            epicMap.pollAllEntities(false);
            
            Servers.localServer.setNextEpicPoll(System.currentTimeMillis() + 1200000L);
          }
        }
        ValreiMapData.pollValreiData();
        
        SpellResist.onServerPoll();
        if (now - lastRecruitmentPoll > 86400000L)
        {
          lastRecruitmentPoll = System.currentTimeMillis();
          RecruitmentAds.poll();
        }
        if (now - lastAwardedItems > 2000L)
        {
          ValreiMapData.pollValreiData();
          pollPendingAwards();
          AwardLadder.clearItemAwards();
          lastAwardedItems = System.currentTimeMillis();
        }
        Object synch;
        if (now - lastFaith > 3600000L)
        {
          check = System.currentTimeMillis();
          lastFaith = System.currentTimeMillis();
          if (Constants.isGameServer)
          {
            Deities.calculateFaiths();
            if (now - this.lastClearedFaithGain > 86400000L)
            {
              Players.resetFaithGain();
              this.lastClearedFaithGain = now;
            }
            Creatures.getInstance().pollOfflineCreatures();
          }
          if (!Servers.isThisLoginServer())
          {
            if (Constants.useScheduledExecutorToSendTimeSync)
            {
              if (logger.isLoggable(Level.FINER)) {
                logger.finer("useScheduledExecutorToSendTimeSync is true so do not send TimeSync from Server.run()");
              }
            }
            else
            {
              synch = new TimeSync();
              addIntraCommand((IntraCommand)synch);
            }
          }
          else {
            ErrorChecks.checkItemWatchers();
          }
          if (rand.nextInt(3) == 0) {
            PendingAccount.poll();
          }
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at 5: " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        if ((Constants.isGameServer) && (now - lastPolledBanks > 3601000L))
        {
          check = System.currentTimeMillis();
          if (Constants.useScheduledExecutorToCountEggs)
          {
            if (logger.isLoggable(Level.FINER)) {
              logger.finer("useScheduledExecutorToCountEggs is true so do not call Items.countEggs() from Server.run()");
            }
          }
          else {
            Items.countEggs();
          }
          lastPolledBanks = now;
          Banks.poll(now);
          
          Players.getInstance().checkAffinities();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Banks and Eggs (6): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        if ((Constants.isGameServer) && (WurmCalendar.currentTime % 4000L == 0L))
        {
          check = System.currentTimeMillis();
          Players.getInstance().calcCRBonus();
          
          Villages.poll();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Villages.poll (7): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
          check = System.currentTimeMillis();
          Kingdoms.poll();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Kingdoms.poll (7.1): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
          check = System.currentTimeMillis();
          Questions.trimQuestions();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Questions.trimQuestions (7.2): " + 
            
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        if (WurmCalendar.currentTime % 100L == 0L)
        {
          check = System.currentTimeMillis();
          Skills.switchSkills(check);
          Battles.poll(false);
          
          Servers.localServer.saveTimers();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Battles and Constants (9): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        else if (WurmCalendar.currentTime % 1050L == 0L)
        {
          Players.getInstance().pollChamps();
          Effectuator.pollEpicEffects();
        }
        if (now - lastDeletedPlayer > 3000L)
        {
          PlayerInfoFactory.checkIfDeleteOnePlayer();
          lastDeletedPlayer = System.currentTimeMillis();
        }
        if (now - lastLoweredRanks > 600000L)
        {
          PlayerInfoFactory.pruneRanks(now);
          EpicServerStatus.pollExpiredMissions();
          lastLoweredRanks = System.currentTimeMillis();
        }
        if (now > this.nextTerraformPoll)
        {
          pollTerraformingTasks();
          this.nextTerraformPoll = (System.currentTimeMillis() + 1000L);
        }
        if ((Servers.localServer.EPIC) && (!Servers.localServer.HOMESERVER) && 
          (now > lastPolledSupplyDepots + 60000L))
        {
          synch = Items.getSupplyDepots();Long localLong1 = synch.length;
          for (Long localLong4 = 0; localLong4 < localLong1; localLong4++)
          {
            Item depot = synch[localLong4];
            
            depot.checkItemSpawn();
          }
          lastPolledSupplyDepots = now;
        }
        if (Servers.localServer.isChallengeServer())
        {
          if (now - lastAwardedBattleCamps > 600000L)
          {
            synch = Items.getWarTargets();Long localLong2 = synch.length;
            for (Long localLong5 = 0; localLong5 < localLong2; localLong5++)
            {
              Item i = synch[localLong5];
              
              Kingdom k = Kingdoms.getKingdom(i.getKingdom());
              if (k != null) {
                k.addWinpoints(1);
              }
              for (PlayerInfo pinf : PlayerInfoFactory.getPlayerInfos()) {
                if (System.currentTimeMillis() - pinf.lastLogin < 86400000L) {
                  if (Players.getInstance().getKingdomForPlayer(pinf.wurmId) == i.getKingdom()) {
                    ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 1.0F);
                  }
                }
              }
            }
            lastAwardedBattleCamps = System.currentTimeMillis();
          }
          if (now > lastPolledSupplyDepots + 60000L)
          {
            synch = Items.getSupplyDepots();Long localLong3 = synch.length;
            for (Long localLong6 = 0; localLong6 < localLong3; localLong6++)
            {
              Item depot = synch[localLong6];
              
              depot.checkItemSpawn();
            }
            lastPolledSupplyDepots = now;
          }
          if (now - savedChallengePage > 10000L)
          {
            ChallengeSummary.saveCurrentGlobalHtmlPage();
            savedChallengePage = System.currentTimeMillis();
          }
        }
        if (now - lastPinged > 1000L)
        {
          Trap.checkQuickUpdate();
          Players.getInstance().checkSendWeather();
          check = System.currentTimeMillis();
          if ((lostConnections > 20) && (lostConnections > Players.getInstance().numberOfPlayers() / 2))
          {
            logger.log(Level.INFO, "Trying to forcibly log off linkless players: " + lostConnections);
            Players.getInstance().logOffLinklessPlayers();
          }
          lostConnections = 0;
          checkAlertMessages();
          lastPinged = now;
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at checkAlertMessages (10): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        if ((Constants.isGameServer) && (now - lastPolledShopCultist > 86400000L))
        {
          lastPolledShopCultist = System.currentTimeMillis();
          Cultist.resetSkillGain();
          logger.log(Level.INFO, "Polling shop demands");
          check = System.currentTimeMillis();
          pollShopDemands();
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at pollShopDemands (11): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        if (System.currentTimeMillis() - lastPolledTileEffects > 3000L)
        {
          AreaSpellEffect.pollEffects();
          lastPolledTileEffects = System.currentTimeMillis();
          Players.printStats();
        }
        if (System.currentTimeMillis() - lastResetAspirations > 90000000L)
        {
          Methods.resetAspirants();
          lastResetAspirations = System.currentTimeMillis();
        }
        if (this.playersAtLogin.size() > 0)
        {
          check = System.currentTimeMillis();
          for (Object it = this.playersAtLogin.listIterator(); ((Iterator)it).hasNext();)
          {
            long pid = ((Long)((Iterator)it).next()).longValue();
            try
            {
              Creature player = Players.getInstance().getPlayer(pid);
              if (player.getVisionArea() == null)
              {
                logger.log(Level.INFO, "VisionArea null for " + player.getName() + ", creating one.");
                player.createVisionArea();
              }
              VisionArea area = player.getVisionArea();
              if ((area != null) && (area.isInitialized())) {
                ((Iterator)it).remove();
              } else {
                try
                {
                  if ((area != null) && (!player.isDead())) {
                    area.sendNextStrip();
                  } else if (area == null) {
                    if ((!player.isDead()) && (!player.isTeleporting()))
                    {
                      logger.log(Level.WARNING, "VisionArea is null for player " + player.getName() + ". Removing from login.");
                      
                      ((Iterator)it).remove();
                    }
                  }
                }
                catch (Exception ex)
                {
                  logger.log(Level.INFO, ex.getMessage(), ex);
                  ((Iterator)it).remove();
                }
              }
            }
            catch (NoSuchPlayerException nsp)
            {
              logger.log(Level.INFO, nsp.getMessage(), nsp);
              ((Iterator)it).remove();
            }
          }
          if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at VisionArea (12): " + 
              (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
          }
        }
        check = System.currentTimeMillis();
        removeCreatures();
        if (System.currentTimeMillis() - check > Constants.lagThreshold) {
          logger.log(Level.INFO, "Lag detected at removeCreatures (13.5): " + 
            (float)(System.currentTimeMillis() - check) / 1000.0F);
        }
        counter = 0;
        
        pollWebCommands();
      }
      check = System.currentTimeMillis();
      MessageServer.sendMessages();
      if (System.currentTimeMillis() - check > Constants.lagThreshold) {
        logger.log(Level.INFO, "Lag detected at sendMessages (14): " + 
          (float)(System.currentTimeMillis() - check) / 1000.0F);
      }
      check = System.currentTimeMillis();
      
      sendFinals();
      if (System.currentTimeMillis() - check > Constants.lagThreshold) {
        logger.log(Level.INFO, "Lag detected at sendFinals (15): " + 
          (float)(System.currentTimeMillis() - check) / 1000.0F);
      }
      check = System.currentTimeMillis();
      this.socketServer.tick();
      
      int realTicks = (int)(now - startTime) / 25;
      
      totalTicks = realTicks - totalTicks;
      if (--commPollCounter <= 0)
      {
        pollComms(now);
        commPollCounter = commPollCounterInit;
      }
      totalTicks = realTicks;
      if (System.currentTimeMillis() - check > Constants.lagThreshold)
      {
        logger.log(Level.INFO, "Lag detected at socketserver.tick (15.5): " + 
          (float)(System.currentTimeMillis() - check) / 1000.0F);
        logger.log(Level.INFO, "Numcommands=" + 
          Communicator.getNumcommands() + ", last=" + Communicator.getLastcommand() + ", prev=" + 
          Communicator.getPrevcommand() + " target=" + Communicator.getCommandAction() + ", Message=" + 
          Communicator.getCommandMessage());
        logger.log(Level.INFO, "Size of connections=" + this.socketServer.getNumberOfConnections() + " logins=" + LoginHandler.logins + ", redirs=" + LoginHandler.redirects + " exceptions=" + exceptions);
      }
      LoginHandler.logins = 0;
      LoginHandler.redirects = 0;
      exceptions = 0;
      check = System.currentTimeMillis();
      pollIntraCommands();
      if (System.currentTimeMillis() - check > Constants.lagThreshold) {
        logger.log(Level.INFO, "Lag detected at pollintracommands (15.8): " + 
          (float)(System.currentTimeMillis() - check) / 1000.0F);
      }
      try
      {
        check = System.currentTimeMillis();
        this.intraServer.socketServer.tick();
        if (System.currentTimeMillis() - check > Constants.lagThreshold) {
          logger.log(Level.INFO, "Lag detected at intraServer.tick (16): " + 
            (float)(System.currentTimeMillis() - check) / 1000.0F);
        }
      }
      catch (IOException iox1)
      {
        logger.log(Level.INFO, "Failed to update intraserver.", iox1);
      }
      long runLoopTime = System.currentTimeMillis() - now;
      if (runLoopTime > 1000L)
      {
        secondsLag = (int)(secondsLag + runLoopTime / 1000L);
        logger.info("Elapsed time (" + runLoopTime + "ms) for this loop was more than 1 second so adding it to the lag count, which is now: " + secondsLag);
      }
      if (!logger.isLoggable(Level.FINEST)) {}
      this.steamHandler.update();
    }
    catch (IOException e1)
    {
      logger.log(Level.INFO, "Failed to update updserver", e1);
    }
    catch (Throwable t)
    {
      logger.log(Level.SEVERE, t.getMessage(), t);
      if ((t.getMessage() == null) && (t.getCause() == null)) {
        logger.log(Level.SEVERE, "Server is shutting down but there is no information in the Exception so creating a new one", new Exception());
      }
      shutDown();
    }
    finally
    {
      if (!logger.isLoggable(Level.FINEST)) {}
    }
  }
  
  private final void pollComms(long now)
  {
    long check = System.currentTimeMillis();
    
    Map<String, Player> playerMap = Players.getInstance().getPlayerMap();
    for (Map.Entry<String, Player> mapEntry : playerMap.entrySet()) {
      if (((Player)mapEntry.getValue()).getCommunicator() != null)
      {
        for (int xm = 0; xm < 10; xm++)
        {
          if ((((Player)mapEntry.getValue()).getCommunicator().getMoves() <= 0) || (((Player)mapEntry.getValue()).getCommunicator().getAvailableMoves() <= 0)) {
            break;
          }
          if (((Player)mapEntry.getValue()).getCommunicator().pollNextMove()) {
            ((Player)mapEntry.getValue()).getCommunicator().setAvailableMoves(((Player)mapEntry.getValue()).getCommunicator().getAvailableMoves() - 1);
          }
        }
        if ((!((Player)mapEntry.getValue()).moveWarned) && (
          (((Player)mapEntry.getValue()).getCommunicator().getMoves() > 240) || 
          (((Player)mapEntry.getValue()).getCommunicator().getMoves() < 65296)))
        {
          if (((Player)mapEntry.getValue()).getPower() >= 5) {
            ((Player)mapEntry.getValue()).getCommunicator().sendAlertServerMessage("Moves at " + ((Player)mapEntry.getValue()).getCommunicator().getMoves());
          } else {
            ((Player)mapEntry.getValue()).getCommunicator().sendAlertServerMessage("Your position on the server is not updated. Please move slower.");
          }
          ((Player)mapEntry.getValue()).moveWarned = true;
          ((Player)mapEntry.getValue()).moveWarnedTime = System.currentTimeMillis();
        }
        else if ((((Player)mapEntry.getValue()).moveWarned) && 
          (((Player)mapEntry.getValue()).getCommunicator().getMoves() > -24) && 
          (((Player)mapEntry.getValue()).getCommunicator().getMoves() < 24))
        {
          ((Player)mapEntry.getValue()).getCommunicator().sendSafeServerMessage("Your position on the server is now updated.");
          
          long seconds = (System.currentTimeMillis() - ((Player)mapEntry.getValue()).moveWarnedTime) / 1000L;
          logger.log(Level.INFO, ((Player)mapEntry.getValue()).getName() + " moves down to " + 
            ((Player)mapEntry.getValue()).getCommunicator().getMoves() + ". Was lagging " + seconds + " seconds with a peak of " + 
            ((Player)mapEntry.getValue()).peakMoves + " moves.");
          ((Player)mapEntry.getValue()).moveWarned = false;
          ((Player)mapEntry.getValue()).peakMoves = 0L;
          ((Player)mapEntry.getValue()).moveWarnedTime = 0L;
        }
        else if ((((Player)mapEntry.getValue()).moveWarned) && (
          (((Player)mapEntry.getValue()).getCommunicator().getMoves() > 1440) || 
          (((Player)mapEntry.getValue()).getCommunicator().getMoves() < 64096)))
        {
          ((Player)mapEntry.getValue()).getCommunicator().sendAlertServerMessage("You are out of synch with the server. Please stand still.");
        }
        if (((Player)mapEntry.getValue()).getCommunicator().getMoves() > 240)
        {
          if (((Player)mapEntry.getValue()).peakMoves < ((Player)mapEntry.getValue()).getCommunicator().getMoves()) {
            ((Player)mapEntry.getValue()).peakMoves = ((Player)mapEntry.getValue()).getCommunicator().getMoves();
          }
        }
        else if (((Player)mapEntry.getValue()).getCommunicator().getMoves() < 65296) {
          if (((Player)mapEntry.getValue()).peakMoves > ((Player)mapEntry.getValue()).getCommunicator().getMoves()) {
            ((Player)mapEntry.getValue()).peakMoves = ((Player)mapEntry.getValue()).getCommunicator().getMoves();
          }
        }
      }
    }
    long time = System.currentTimeMillis() - this.lastTicked;
    if (time <= 3L) {
      lagticks += 1;
    }
    this.lastTicked = System.currentTimeMillis();
    if (System.currentTimeMillis() - check > Constants.lagThreshold) {
      logger.log(Level.INFO, "Lag detected at Player Moves (13): " + 
        (float)(System.currentTimeMillis() - check) / 1000.0F);
    }
  }
  
  private final void pollSurfaceWater()
  {
    if (this.waterThread != null) {
      this.waterThread.propagateChanges();
    }
  }
  
  public void pollShopDemands()
  {
    Shop[] shops = Economy.getEconomy().getShops();
    for (Shop lShop : shops) {
      lShop.getLocalSupplyDemand().lowerDemands();
    }
    LocalSupplyDemand.increaseAllDemands();
    Economy.getEconomy().pollTraderEarnings();
  }
  
  public static void addNewPlayer(String name)
  {
    if (System.currentTimeMillis() - lastResetNewPremiums > 10800000L)
    {
      newPremiums = 0;
      lastResetNewPremiums = System.currentTimeMillis();
    }
    newPremiums += 1;
  }
  
  public static final void addNewbie()
  {
    newbies += 1;
  }
  
  public static final void addExpiry()
  {
    expiredPremiums += 1;
  }
  
  private void sendFinals()
  {
    if (FINAL_LOGINS_RW_LOCK.writeLock().tryLock()) {
      try
      {
        for (it = finalLogins.listIterator(); it.hasNext();) {
          try
          {
            long pid = ((Long)it.next()).longValue();
            Player player = Players.getInstance().getPlayer(pid);
            int step = player.getLoginStep();
            if (player.isNew())
            {
              if (player.hasLink())
              {
                int result = LoginHandler.createPlayer(player, step);
                if (result == Integer.MAX_VALUE)
                {
                  it.remove();
                  if (!isPlayerReceivingTiles(player)) {
                    this.playersAtLogin.add(new Long(player.getWurmId()));
                  }
                  player.setLoginHandler(null);
                }
                else if (result >= 0)
                {
                  player.setLoginStep(++result);
                }
                else
                {
                  player.setLoginHandler(null);
                  it.remove();
                }
              }
              else
              {
                player.setLoginHandler(null);
                it.remove();
              }
            }
            else if (player.hasLink())
            {
              LoginHandler handler = player.getLoginhandler();
              if (handler != null)
              {
                int result = handler.loadPlayer(player, step);
                if (result == Integer.MAX_VALUE)
                {
                  it.remove();
                  if (!isPlayerReceivingTiles(player)) {
                    this.playersAtLogin.add(new Long(player.getWurmId()));
                  }
                  player.setLoginHandler(null);
                }
                else if (result >= 0)
                {
                  player.setLoginStep(++result);
                }
                else
                {
                  player.setLoginHandler(null);
                  it.remove();
                }
              }
              else
              {
                it.remove();
              }
            }
            else
            {
              player.setLoginHandler(null);
              it.remove();
            }
            player.getStatus().setMoving(false);
            if (!player.hasLink()) {
              Players.getInstance().logoutPlayer(player);
            }
          }
          catch (NoSuchPlayerException nsp)
          {
            logger.log(Level.INFO, nsp.getMessage(), nsp);
            it.remove();
          }
        }
      }
      finally
      {
        ListIterator<Long> it;
        FINAL_LOGINS_RW_LOCK.writeLock().unlock();
      }
    }
  }
  
  /* Error */
  public void addCreatureToPort(Creature creature)
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 874	com/wurmonline/server/creatures/Creature:isPlayer	()Z
    //   4: ifeq +80 -> 84
    //   7: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   10: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   13: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   16: aload_0
    //   17: getfield 139	com/wurmonline/server/Server:playersAtLogin	Ljava/util/List;
    //   20: new 603	java/lang/Long
    //   23: dup
    //   24: aload_1
    //   25: invokevirtual 875	com/wurmonline/server/creatures/Creature:getWurmId	()J
    //   28: invokespecial 867	java/lang/Long:<init>	(J)V
    //   31: invokeinterface 876 2 0
    //   36: ifne +24 -> 60
    //   39: aload_0
    //   40: getfield 139	com/wurmonline/server/Server:playersAtLogin	Ljava/util/List;
    //   43: new 603	java/lang/Long
    //   46: dup
    //   47: aload_1
    //   48: invokevirtual 875	com/wurmonline/server/creatures/Creature:getWurmId	()J
    //   51: invokespecial 867	java/lang/Long:<init>	(J)V
    //   54: invokeinterface 24 2 0
    //   59: pop
    //   60: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   63: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   66: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   69: goto +15 -> 84
    //   72: astore_2
    //   73: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   76: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   79: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   82: aload_2
    //   83: athrow
    //   84: return
    // Line number table:
    //   Java source line #2884	-> byte code offset #0
    //   Java source line #2886	-> byte code offset #7
    //   Java source line #2889	-> byte code offset #16
    //   Java source line #2891	-> byte code offset #39
    //   Java source line #2896	-> byte code offset #60
    //   Java source line #2897	-> byte code offset #69
    //   Java source line #2896	-> byte code offset #72
    //   Java source line #2897	-> byte code offset #82
    //   Java source line #2899	-> byte code offset #84
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	85	0	this	Server
    //   0	85	1	creature	Creature
    //   72	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   16	60	72	finally
  }
  
  public void clientConnected(SocketConnection serverConnection)
  {
    HackerIp ip = (HackerIp)LoginHandler.failedIps.get(serverConnection.getIp());
    if ((ip == null) || (System.currentTimeMillis() > ip.mayTryAgain))
    {
      try
      {
        LoginHandler login = new LoginHandler(serverConnection);
        
        serverConnection.setConnectionListener(login);
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, "Failed to create login handler for serverConnection: " + serverConnection + '.', ex);
      }
    }
    else
    {
      logger.log(Level.INFO, ip.name + " Because of the repeated failures the conn may try again in " + 
        getTimeFor(ip.mayTryAgain - System.currentTimeMillis()) + '.');
      serverConnection.disconnect();
    }
  }
  
  /* Error */
  public void addToPlayersAtLogin(Player player)
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 866	com/wurmonline/server/players/Player:getWurmId	()J
    //   4: invokestatic 892	com/wurmonline/server/WurmId:getType	(J)I
    //   7: ifeq +48 -> 55
    //   10: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   13: getstatic 56	java/util/logging/Level:WARNING	Ljava/util/logging/Level;
    //   16: new 27	java/lang/StringBuilder
    //   19: dup
    //   20: invokespecial 28	java/lang/StringBuilder:<init>	()V
    //   23: ldc_w 893
    //   26: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   29: aload_1
    //   30: invokevirtual 831	com/wurmonline/server/players/Player:getName	()Ljava/lang/String;
    //   33: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   36: ldc_w 894
    //   39: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   42: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   45: new 15	java/lang/Exception
    //   48: dup
    //   49: invokespecial 814	java/lang/Exception:<init>	()V
    //   52: invokevirtual 18	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   55: aload_0
    //   56: aload_1
    //   57: invokespecial 865	com/wurmonline/server/Server:isPlayerReceivingTiles	(Lcom/wurmonline/server/players/Player;)Z
    //   60: ifne +57 -> 117
    //   63: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   66: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   69: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   72: aload_0
    //   73: getfield 139	com/wurmonline/server/Server:playersAtLogin	Ljava/util/List;
    //   76: new 603	java/lang/Long
    //   79: dup
    //   80: aload_1
    //   81: invokevirtual 866	com/wurmonline/server/players/Player:getWurmId	()J
    //   84: invokespecial 867	java/lang/Long:<init>	(J)V
    //   87: invokeinterface 24 2 0
    //   92: pop
    //   93: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   96: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   99: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   102: goto +15 -> 117
    //   105: astore_2
    //   106: getstatic 138	com/wurmonline/server/Server:PLAYERS_AT_LOGIN_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   109: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   112: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   115: aload_2
    //   116: athrow
    //   117: return
    // Line number table:
    //   Java source line #2936	-> byte code offset #0
    //   Java source line #2937	-> byte code offset #10
    //   Java source line #2938	-> byte code offset #55
    //   Java source line #2940	-> byte code offset #63
    //   Java source line #2943	-> byte code offset #72
    //   Java source line #2947	-> byte code offset #93
    //   Java source line #2948	-> byte code offset #102
    //   Java source line #2947	-> byte code offset #105
    //   Java source line #2948	-> byte code offset #115
    //   Java source line #2950	-> byte code offset #117
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	118	0	this	Server
    //   0	118	1	player	Player
    //   105	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   72	93	105	finally
  }
  
  public void addPlayer(Player player)
  {
    Players.getInstance().addPlayer(player);
    if (player.isPaying()) {
      logonsPrem += 1;
    }
    logons += 1;
  }
  
  void addIp(String ip)
  {
    if (!ips.keySet().contains(ip))
    {
      ips.put(ip, Boolean.FALSE);
      numips += 1;
    }
    else
    {
      Boolean newb = (Boolean)ips.get(ip);
      if (!newb.booleanValue()) {
        ips.put(ip, Boolean.FALSE);
      }
    }
  }
  
  private void checkAlertMessages()
  {
    if (timeBetweenAlertMess1 < Long.MAX_VALUE) {
      if ((alertMessage1.length() > 0) && (lastAlertMess1 + timeBetweenAlertMess1 < System.currentTimeMillis()))
      {
        broadCastAlert(alertMessage1);
        lastAlertMess1 = System.currentTimeMillis();
      }
    }
    if (timeBetweenAlertMess2 < Long.MAX_VALUE) {
      if ((alertMessage2.length() > 0) && (lastAlertMess2 + timeBetweenAlertMess2 < System.currentTimeMillis()))
      {
        broadCastAlert(alertMessage2);
        lastAlertMess2 = System.currentTimeMillis();
      }
    }
    if (timeBetweenAlertMess3 < Long.MAX_VALUE) {
      if ((alertMessage3.length() > 0) && (lastAlertMess3 + timeBetweenAlertMess3 < System.currentTimeMillis()))
      {
        broadCastAlert(alertMessage3);
        lastAlertMess3 = System.currentTimeMillis();
      }
    }
    if (timeBetweenAlertMess4 < Long.MAX_VALUE) {
      if ((alertMessage4.length() > 0) && (lastAlertMess4 + timeBetweenAlertMess4 < System.currentTimeMillis()))
      {
        broadCastAlert(alertMessage4);
        lastAlertMess4 = System.currentTimeMillis();
      }
    }
  }
  
  /* Error */
  public void startSendingFinals(Player player)
  {
    // Byte code:
    //   0: getstatic 853	com/wurmonline/server/Server:FINAL_LOGINS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 855	com/wurmonline/server/Server:finalLogins	Ljava/util/List;
    //   12: new 603	java/lang/Long
    //   15: dup
    //   16: aload_1
    //   17: invokevirtual 866	com/wurmonline/server/players/Player:getWurmId	()J
    //   20: invokespecial 867	java/lang/Long:<init>	(J)V
    //   23: invokeinterface 24 2 0
    //   28: pop
    //   29: getstatic 853	com/wurmonline/server/Server:FINAL_LOGINS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   32: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   35: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   38: goto +15 -> 53
    //   41: astore_2
    //   42: getstatic 853	com/wurmonline/server/Server:FINAL_LOGINS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   45: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   48: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   51: aload_2
    //   52: athrow
    //   53: return
    // Line number table:
    //   Java source line #3016	-> byte code offset #0
    //   Java source line #3019	-> byte code offset #9
    //   Java source line #3023	-> byte code offset #29
    //   Java source line #3024	-> byte code offset #38
    //   Java source line #3023	-> byte code offset #41
    //   Java source line #3024	-> byte code offset #51
    //   Java source line #3025	-> byte code offset #53
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	54	0	this	Server
    //   0	54	1	player	Player
    //   41	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	29	41	finally
  }
  
  private boolean isPlayerReceivingTiles(Player player)
  {
    PLAYERS_AT_LOGIN_RW_LOCK.readLock().lock();
    try
    {
      return this.playersAtLogin.contains(new Long(player.getWurmId()));
    }
    finally
    {
      PLAYERS_AT_LOGIN_RW_LOCK.readLock().unlock();
    }
  }
  
  public void clientException(SocketConnection conn, Exception ex)
  {
    exceptions += 1;
    try
    {
      Player player = Players.getInstance().getPlayer(conn);
      lostConnections += 1;
      if (this.playersAtLogin != null)
      {
        PLAYERS_AT_LOGIN_RW_LOCK.writeLock().lock();
        try
        {
          this.playersAtLogin.remove(new Long(player.getWurmId()));
        }
        finally
        {
          PLAYERS_AT_LOGIN_RW_LOCK.writeLock().unlock();
        }
      }
      if (finalLogins != null)
      {
        FINAL_LOGINS_RW_LOCK.writeLock().lock();
        try
        {
          finalLogins.remove(new Long(player.getWurmId()));
        }
        finally
        {
          FINAL_LOGINS_RW_LOCK.writeLock().unlock();
        }
      }
      player.setLink(false);
    }
    catch (Exception ex2)
    {
      Player player = Players.getInstance().logout(conn);
      if (player != null)
      {
        if (this.playersAtLogin != null)
        {
          PLAYERS_AT_LOGIN_RW_LOCK.writeLock().lock();
          try
          {
            this.playersAtLogin.remove(new Long(player.getWurmId()));
          }
          finally
          {
            PLAYERS_AT_LOGIN_RW_LOCK.writeLock().unlock();
          }
        }
        if (finalLogins != null)
        {
          FINAL_LOGINS_RW_LOCK.writeLock().lock();
          try
          {
            finalLogins.remove(new Long(player.getWurmId()));
          }
          finally
          {
            FINAL_LOGINS_RW_LOCK.writeLock().unlock();
          }
        }
        logger.log(Level.INFO, player.getName() + " lost link at exception 2");
      }
    }
  }
  
  public Creature getCreature(long creatureId)
    throws NoSuchPlayerException, NoSuchCreatureException
  {
    Creature toReturn = null;
    if (WurmId.getType(creatureId) == 1) {
      toReturn = Creatures.getInstance().getCreature(creatureId);
    } else {
      toReturn = Players.getInstance().getPlayer(creatureId);
    }
    return toReturn;
  }
  
  public Creature getCreatureOrNull(long creatureId)
  {
    if (WurmId.getType(creatureId) == 1) {
      return Creatures.getInstance().getCreatureOrNull(creatureId);
    }
    return Players.getInstance().getPlayerOrNull(creatureId);
  }
  
  public void addMessage(Message message)
  {
    MessageServer.addMessage(message);
  }
  
  public void broadCastNormal(String message)
  {
    broadCastNormal(message, true);
  }
  
  public void broadCastNormal(String message, boolean twit)
  {
    MessageServer.broadCastNormal(message);
    if (twit) {
      twitLocalServer(message);
    }
  }
  
  public void broadCastSafe(String message)
  {
    broadCastSafe(message, true);
  }
  
  public void broadCastSafe(String message, boolean twit)
  {
    broadCastSafe(message, twit, (byte)0);
  }
  
  public void broadCastSafe(String message, boolean twit, byte messageType)
  {
    MessageServer.broadCastSafe(message, messageType);
    if (twit) {
      twitLocalServer(message);
    }
  }
  
  public void broadCastAlert(String message)
  {
    broadCastAlert(message, true);
  }
  
  public void broadCastAlert(String message, boolean twit)
  {
    broadCastAlert(message, twit, (byte)0);
  }
  
  public void broadCastAlert(String message, boolean twit, byte messageType)
  {
    MessageServer.broadCastAlert(message, messageType);
    if (twit) {
      twitLocalServer(message);
    }
  }
  
  public void broadCastAction(String message, Creature performer, int tileDist, boolean combat)
  {
    MessageServer.broadCastAction(message, performer, null, tileDist, combat);
  }
  
  public void broadCastAction(String message, Creature performer, int tileDist)
  {
    MessageServer.broadCastAction(message, performer, tileDist);
  }
  
  public void broadCastAction(String message, Creature performer, Creature receiver, int tileDist)
  {
    MessageServer.broadCastAction(message, performer, receiver, tileDist);
  }
  
  public void broadCastAction(String message, Creature performer, Creature receiver, int tileDist, boolean combat)
  {
    MessageServer.broadCastAction(message, performer, receiver, tileDist, combat);
  }
  
  public void broadCastMessage(String message, int tilex, int tiley, boolean surfaced, int tiledistance)
  {
    MessageServer.broadCastMessage(message, tilex, tiley, surfaced, tiledistance);
  }
  
  private void loadCaveMesh()
  {
    long start = System.nanoTime();
    try
    {
      caveMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "map_cave.map");
    }
    catch (IOException iex)
    {
      float lElapsedTime;
      logger.log(Level.SEVERE, "Cavemap doesn't exist... initializing... size will be " + (1 << Constants.meshSize) + "!");
      try
      {
        Constants.caveImg = true;
        int msize = (1 << Constants.meshSize) * (1 << Constants.meshSize);
        int[] caveArr = new int[msize];
        for (int x = 0; x < msize; x++)
        {
          if (x % 100000 == 0) {
            logger.log(Level.INFO, "Created " + x + " tiles out of " + msize);
          }
          caveArr[x] = Tiles.encode(-100, 
            TileRockBehaviour.prospect(x & (1 << Constants.meshSize) - 1, x >> Constants.meshSize, false), 0);
        }
        caveMesh = MeshIO.createMap(ServerDirInfo.getFileDBPath() + "map_cave.map", Constants.meshSize, caveArr);
      }
      catch (IOException iox)
      {
        logger.log(Level.INFO, "Failed to initialize caves. Exiting. " + iox.getMessage(), iox);
        System.exit(0);
      }
      catch (ArrayIndexOutOfBoundsException ex2)
      {
        logger.log(Level.WARNING, "Failed to initialize caves. Exiting. " + ex2.getMessage(), ex2);
        System.exit(0);
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, "Failed to initialize caves. Exiting. " + ex.getMessage(), ex);
        System.exit(0);
      }
    }
    finally
    {
      float lElapsedTime;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Loading cave mesh, size: " + caveMesh.getSize() + " took " + lElapsedTime + " ms");
    }
    if (Constants.reprospect) {
      TileRockBehaviour.reProspect();
    }
    if (Constants.caveImg)
    {
      ZonesUtility.saveAsImg(caveMesh);
      logger.log(Level.INFO, "Saved cave mesh as img");
    }
  }
  
  private void loadWorldMesh()
  {
    long start = System.nanoTime();
    try
    {
      surfaceMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "top_layer.map");
    }
    catch (IOException iex)
    {
      float lElapsedTime;
      logger.log(Level.SEVERE, "Worldmap " + ServerDirInfo.getFileDBPath() + "top_layer.map doesn't exist.. Shutting down..", iex);
      
      System.exit(0);
    }
    finally
    {
      float lElapsedTime;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Loading world mesh, size: " + surfaceMesh.getSize() + " took " + lElapsedTime + " ms");
    }
  }
  
  private void loadRockMesh()
  {
    long start = System.nanoTime();
    try
    {
      rockMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "rock_layer.map");
    }
    catch (IOException iex)
    {
      float lElapsedTime;
      logger.log(Level.SEVERE, "Worldmap " + ServerDirInfo.getFileDBPath() + "rock_layer.map doesn't exist.. Shutting down..", iex);
      
      System.exit(0);
    }
    finally
    {
      float lElapsedTime;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Loading rock mesh, size: " + rockMesh.getSize() + " took " + lElapsedTime + " ms");
    }
  }
  
  public static int getCaveResource(int tilex, int tiley)
  {
    int value = resourceMesh.getTile(tilex, tiley);
    int toReturn = value >> 16 & 0xFFFF;
    return toReturn;
  }
  
  public static void setCaveResource(int tilex, int tiley, int newValue)
  {
    int value = resourceMesh.getTile(tilex, tiley);
    if ((value >> 16 & 0xFFFF) != newValue) {
      resourceMesh.setTile(tilex, tiley, ((newValue & 0xFFFF) << 16) + (value & 0xFFFF));
    }
  }
  
  public static int getWorldResource(int tilex, int tiley)
  {
    int value = resourceMesh.getTile(tilex, tiley);
    int toReturn = value & 0xFFFF;
    return toReturn;
  }
  
  public static void setWorldResource(int tilex, int tiley, int newValue)
  {
    int value = resourceMesh.getTile(tilex, tiley);
    if ((value & 0xFFFF) != newValue) {
      resourceMesh.setTile(tilex, tiley, (value & 0xFFFF0000) + (newValue & 0xFFFF));
    }
  }
  
  public static int getDigCount(int tilex, int tiley)
  {
    int value = resourceMesh.getTile(tilex, tiley);
    int digCount = value & 0xFF;
    return digCount;
  }
  
  public static void setDigCount(int tilex, int tiley, int newValue)
  {
    int value = resourceMesh.getTile(tilex, tiley);
    if ((value & 0xFF) != newValue) {
      resourceMesh.setTile(tilex, tiley, (value & 0xFF00) + (newValue & 0xFF));
    }
  }
  
  public static int getPotionQLCount(int tilex, int tiley)
  {
    int value = resourceMesh.getTile(tilex, tiley);
    int pQLCount = (value & 0xFF00) >> 8;
    if (pQLCount == 255) {
      return 0;
    }
    return pQLCount;
  }
  
  public static void setPotionQLCount(int tilex, int tiley, int newValue)
  {
    int pQLCount = newValue << 8;
    int value = resourceMesh.getTile(tilex, tiley);
    if ((value & 0xFF00) != pQLCount) {
      resourceMesh.setTile(tilex, tiley, (value & 0xFFFF00FF) + (pQLCount & 0xFF00));
    }
  }
  
  public static boolean isBotanizable(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x80) == 128;
  }
  
  public static void setBotanizable(int tilex, int tiley, boolean isBotanizable)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = isBotanizable ? 128 : 0;
    if ((value & 0x80) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xFF7F | newValue);
    }
  }
  
  public static boolean isForagable(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x40) == 64;
  }
  
  public static void setForagable(int tilex, int tiley, boolean isForagable)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = isForagable ? 64 : 0;
    if ((value & 0x40) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xFFFFFFBF | newValue);
    }
  }
  
  public static boolean isGatherable(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x20) == 32;
  }
  
  public static void setGatherable(int tilex, int tiley, boolean isGather)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = isGather ? 32 : 0;
    if ((value & 0x20) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xFFFFFFDF | newValue);
    }
  }
  
  public static boolean isInvestigatable(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x10) == 16;
  }
  
  public static void setInvestigatable(int tilex, int tiley, boolean isInvestigate)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = isInvestigate ? 16 : 0;
    if ((value & 0x10) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xFFFFFFEF | newValue);
    }
  }
  
  public static boolean isCheckHive(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x400) == 1024;
  }
  
  public static void setCheckHive(int tilex, int tiley, boolean isChecked)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = isChecked ? 1024 : 0;
    if ((value & 0x400) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xFBFF | newValue);
    }
  }
  
  public static boolean wasTransformed(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x100) == 256;
  }
  
  public static void setTransformed(int tilex, int tiley, boolean isTransformed)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = isTransformed ? 256 : 0;
    if ((value & 0x100) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xFEFF | newValue);
    }
  }
  
  public static boolean isBeingTransformed(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x200) == 512;
  }
  
  public static void setBeingTransformed(int tilex, int tiley, boolean isTransformed)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = isTransformed ? 512 : 0;
    if ((value & 0x200) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xFDFF | newValue);
    }
  }
  
  public static boolean hasGrubs(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (value & 0x800) == 2048;
  }
  
  public static void setGrubs(int tilex, int tiley, boolean grubs)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    int newValue = grubs ? 2048 : 0;
    if ((value & 0x800) != newValue) {
      flagsMesh.setTile(tilex, tiley, value & 0xF7FF | newValue);
    }
  }
  
  public static byte getClientSurfaceFlags(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (byte)(value & 0xFF);
  }
  
  public static byte getServerSurfaceFlags(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (byte)(value >>> 8 & 0xFF);
  }
  
  public static byte getServerCaveFlags(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (byte)(value >>> 24 & 0xFF);
  }
  
  public static byte getClientCaveFlags(int tilex, int tiley)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    return (byte)(value >>> 16 & 0xFF);
  }
  
  public static void setServerCaveFlags(int tilex, int tiley, byte newByte)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    flagsMesh.setTile(tilex, tiley, value & 0xFFFFFF | (newByte & 0xFF) << 24);
  }
  
  public static void setClientCaveFlags(int tilex, int tiley, byte newByte)
  {
    int value = flagsMesh.getTile(tilex, tiley);
    flagsMesh.setTile(tilex, tiley, value & 0xFF00FFFF | (newByte & 0xFF) << 16);
  }
  
  public static void setSurfaceTile(@Nonnull TilePos tilePos, short newHeight, byte newTileType, byte newTileData)
  {
    setSurfaceTile(tilePos.x, tilePos.y, newHeight, newTileType, newTileData);
  }
  
  public static void setSurfaceTile(int tilex, int tiley, short newHeight, byte newTileType, byte newTileData)
  {
    int oldTile = surfaceMesh.getTile(tilex, tiley);
    byte oldType = Tiles.decodeType(oldTile);
    if (oldType != newTileType) {
      modifyFlagsByTileType(tilex, tiley, newTileType);
    }
    surfaceMesh.setTile(tilex, tiley, Tiles.encode(newHeight, newTileType, newTileData));
  }
  
  public static void modifyFlagsByTileType(int tilex, int tiley, byte newTileType)
  {
    Tiles.Tile theNewTile = Tiles.getTile(newTileType);
    if (!theNewTile.canBotanize()) {
      setBotanizable(tilex, tiley, false);
    }
    if (!theNewTile.canForage()) {
      setForagable(tilex, tiley, false);
    }
    setGatherable(tilex, tiley, false);
    
    setBeingTransformed(tilex, tiley, false);
    setTransformed(tilex, tiley, false);
  }
  
  public static boolean canBotanize(byte type)
  {
    if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_STEPPE.id) || (type == Tiles.Tile.TILE_MARSH.id) || (type == Tiles.Tile.TILE_MOSS.id) || (type == Tiles.Tile.TILE_PEAT.id) || 
    
      (Tiles.isNormalBush(type)) || 
      (Tiles.isNormalTree(type))) {
      return true;
    }
    return false;
  }
  
  public static boolean canForage(byte type)
  {
    if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_STEPPE.id) || (type == Tiles.Tile.TILE_TUNDRA.id) || (type == Tiles.Tile.TILE_MARSH.id) || 
    
      (Tiles.isNormalBush(type)) || 
      (Tiles.isNormalTree(type))) {
      return true;
    }
    return false;
  }
  
  public static boolean canBearFruit(byte type)
  {
    if ((Tiles.isTree(type)) || (Tiles.isBush(type))) {
      return true;
    }
    return false;
  }
  
  /* Error */
  public void shutDown(String aReason, Throwable aCause)
  {
    // Byte code:
    //   0: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   3: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   6: new 27	java/lang/StringBuilder
    //   9: dup
    //   10: invokespecial 28	java/lang/StringBuilder:<init>	()V
    //   13: ldc_w 998
    //   16: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   19: aload_1
    //   20: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   23: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   26: invokevirtual 10	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   29: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   32: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   35: ldc_w 999
    //   38: aload_2
    //   39: invokevirtual 18	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   42: aload_0
    //   43: invokevirtual 546	com/wurmonline/server/Server:shutDown	()V
    //   46: goto +10 -> 56
    //   49: astore_3
    //   50: aload_0
    //   51: invokevirtual 546	com/wurmonline/server/Server:shutDown	()V
    //   54: aload_3
    //   55: athrow
    //   56: return
    // Line number table:
    //   Java source line #3711	-> byte code offset #0
    //   Java source line #3712	-> byte code offset #29
    //   Java source line #3716	-> byte code offset #42
    //   Java source line #3717	-> byte code offset #46
    //   Java source line #3716	-> byte code offset #49
    //   Java source line #3717	-> byte code offset #54
    //   Java source line #3718	-> byte code offset #56
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	57	0	this	Server
    //   0	57	1	aReason	String
    //   0	57	2	aCause	Throwable
    //   49	6	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	42	49	finally
  }
  
  public void shutDown()
  {
    if (ServerProperties.getBoolean("ENABLE_PNP_PORT_FORWARD", Constants.enablePnpPortForward)) {
      UPNPService.shutdown();
    }
    Creatures.getInstance().shutDownPolltask();
    Creature.shutDownPathFinders();
    logger.log(Level.INFO, "Shutting down at: ", new Exception());
    if (this.highwayFinderThread != null)
    {
      logger.info("Shutting down - Stopping HighwayFinder");
      this.highwayFinderThread.shouldStop();
    }
    ServerProjectile.clear();
    logger.info("Shutting down - Polling Battles");
    if (Constants.isGameServer) {
      Battles.poll(true);
    }
    Zones.saveProtectedTiles();
    logger.info("Shutting down - Saving Players");
    Players.getInstance().savePlayersAtShutdown();
    
    logger.info("Shutting down - Clearing Item Database Batches");
    DbItem.clearBatches();
    
    logger.info("Shutting down - Saving Creatures");
    logger.info("Shutting down - Clearing Creature Database Batches");
    for (Creature c : Creatures.getInstance().getCreatures()) {
      if ((c.getStatus().getPosition() != null) && (c.getStatus().getPosition().isChanged())) {
        try
        {
          c.getStatus().savePosition(c.getWurmId(), false, c.getStatus().getZoneId(), true);
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, iox.getMessage(), iox);
        }
      }
    }
    if (Constants.useScheduledExecutorToUpdateCreaturePositionInDatabase) {
      CreaturePos.getCreatureDbPosUpdater().saveImmediately();
    }
    CreaturePos.clearBatches();
    logger.info("Shutting down - Saving all creatures");
    
    Creatures.getInstance().saveCreatures();
    logger.info("Shutting down - Saving All Zones");
    Zones.saveAllZones();
    if ((this.scheduledExecutorService != null) && (!this.scheduledExecutorService.isShutdown())) {
      this.scheduledExecutorService.shutdown();
    }
    logger.info("Shutting down - Saving Surface Mesh");
    try
    {
      surfaceMesh.saveAll();
      surfaceMesh.close();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save surfacemesh!", iox);
    }
    logger.info("Shutting down - Saving Rock Mesh");
    try
    {
      rockMesh.saveAll();
      rockMesh.close();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save rockmesh!", iox);
    }
    logger.info("Shutting down - Saving Cave Mesh");
    try
    {
      caveMesh.saveAll();
      caveMesh.close();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save cavemesh!", iox);
    }
    logger.info("Shutting down - Saving Resource Mesh");
    try
    {
      resourceMesh.saveAll();
      resourceMesh.close();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save resourcemesh!", iox);
    }
    logger.info("Shutting down - Saving Flags Mesh");
    try
    {
      flagsMesh.saveAll();
      flagsMesh.close();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to save flagsmesh!", iox);
    }
    if (this.waterThread != null)
    {
      logger.info("Shutting down - Saving Water Mesh");
      this.waterThread.shouldStop = true;
    }
    logger.info("Shutting down - Saving Constants");
    Constants.crashed = false;
    Constants.save();
    logger.info("Shutting down - Saving WurmID Numbers");
    WurmId.updateNumbers();
    
    this.steamHandler.closeServer();
    
    logger.info("Shutting down - Closing Database Connections");
    DbConnector.closeAll();
    logger.log(Level.INFO, "The server shut down nicely. Wurmcalendar time is " + WurmCalendar.currentTime);
    System.exit(0);
  }
  
  private void loadResourceMesh()
  {
    long start = System.nanoTime();
    try
    {
      resourceMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "resources.map");
    }
    catch (IOException iex)
    {
      float lElapsedTime;
      logger.log(Level.INFO, "resources doesn't exist.. creating..");
      int[] resourceArr = new int[(1 << Constants.meshSize) * (1 << Constants.meshSize)];
      for (int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); x++) {
        resourceArr[x] = -1;
      }
      try
      {
        resourceMesh = MeshIO.createMap(ServerDirInfo.getFileDBPath() + "resources.map", Constants.meshSize, resourceArr);
      }
      catch (IOException iox)
      {
        logger.log(Level.SEVERE, "Failed to create resources. Exiting.", iox);
        System.exit(0);
      }
    }
    finally
    {
      float lElapsedTime;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Loading resource mesh, size: " + resourceMesh.getSize() + " took " + lElapsedTime + " ms");
    }
  }
  
  private void loadFlagsMesh()
  {
    long start = System.nanoTime();
    try
    {
      flagsMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "flags.map");
      
      int first = flagsMesh.getTile(0, 0);
      if ((first & 0xFF00) == 65280)
      {
        logger.log(Level.INFO, "converting flags.");
        for (int x = 0; x < 1 << Constants.meshSize; x++) {
          for (int y = 0; y < 1 << Constants.meshSize; y++)
          {
            int value = flagsMesh.getTile(x, y) & 0xFF;
            
            int serverSurfaceFlag = value & 0xF;
            value |= serverSurfaceFlag << 8;
            
            value &= 0xFFF0;
            flagsMesh.setTile(x, y, value);
          }
        }
      }
    }
    catch (IOException iex)
    {
      float lElapsedTime;
      logger.log(Level.INFO, "flags doesn't exist.. creating..");
      int[] resourceArr = new int[(1 << Constants.meshSize) * (1 << Constants.meshSize)];
      for (int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); x++) {
        resourceArr[x] = 0;
      }
      try
      {
        flagsMesh = MeshIO.createMap(ServerDirInfo.getFileDBPath() + "flags.map", Constants.meshSize, resourceArr);
        this.needSeeds = true;
      }
      catch (IOException iox)
      {
        logger.log(Level.SEVERE, "Failed to create flags. Exiting.", iox);
        System.exit(0);
      }
    }
    finally
    {
      float lElapsedTime;
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Loading flags mesh, size: " + flagsMesh.getSize() + " took " + lElapsedTime + " ms");
    }
  }
  
  public static final void addPendingAward(PendingAward award)
  {
    pendingAwards.add(award);
  }
  
  private static final void pollPendingAwards()
  {
    for (PendingAward award : pendingAwards) {
      award.award();
    }
    pendingAwards.clear();
  }
  
  public static final String getTimeFor(long aTime)
  {
    String times = "";
    if (aTime < 60000L)
    {
      long secs = aTime / 1000L;
      times = times + secs + (secs == 1L ? " second" : " seconds");
    }
    else
    {
      long daysleft = aTime / 86400000L;
      
      long hoursleft = (aTime - daysleft * 86400000L) / 3600000L;
      long minutesleft = (aTime - daysleft * 86400000L - hoursleft * 3600000L) / 60000L;
      if (daysleft > 0L) {
        times = times + daysleft + (daysleft == 1L ? " day" : " days");
      }
      if (hoursleft > 0L)
      {
        String aft = "";
        if ((daysleft > 0L) && (minutesleft > 0L))
        {
          times = times + ", ";
          aft = aft + " and ";
        }
        else if (daysleft > 0L)
        {
          times = times + " and ";
        }
        else if (minutesleft > 0L)
        {
          aft = aft + " and ";
        }
        times = times + hoursleft + (hoursleft == 1L ? " hour" : " hours") + aft;
      }
      if (minutesleft > 0L)
      {
        String aft = "";
        if ((daysleft > 0L) && (hoursleft == 0L)) {
          aft = " and ";
        }
        times = times + aft + minutesleft + (minutesleft == 1L ? " minute" : " minutes");
      }
    }
    if (times.length() == 0) {
      times = "nothing";
    }
    return times;
  }
  
  public void transaction(long itemId, long oldownerid, long newownerid, String reason, long value)
  {
    Economy.getEconomy().transaction(itemId, oldownerid, newownerid, reason, value);
  }
  
  /* Error */
  private void pollIntraCommands()
  {
    // Byte code:
    //   0: getstatic 1079	com/wurmonline/server/Server:INTRA_COMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 854	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:tryLock	()Z
    //   9: ifeq +94 -> 103
    //   12: aload_0
    //   13: getfield 73	com/wurmonline/server/Server:intraCommands	Ljava/util/List;
    //   16: aload_0
    //   17: getfield 73	com/wurmonline/server/Server:intraCommands	Ljava/util/List;
    //   20: invokeinterface 46 1 0
    //   25: anewarray 1080	com/wurmonline/server/intra/IntraCommand
    //   28: invokeinterface 48 2 0
    //   33: checkcast 1081	[Lcom/wurmonline/server/intra/IntraCommand;
    //   36: astore_1
    //   37: iconst_0
    //   38: istore_2
    //   39: iload_2
    //   40: aload_1
    //   41: arraylength
    //   42: if_icmpge +37 -> 79
    //   45: iload_2
    //   46: bipush 40
    //   48: if_icmpge +25 -> 73
    //   51: aload_1
    //   52: iload_2
    //   53: aaload
    //   54: invokevirtual 1082	com/wurmonline/server/intra/IntraCommand:poll	()Z
    //   57: ifeq +16 -> 73
    //   60: aload_0
    //   61: getfield 73	com/wurmonline/server/Server:intraCommands	Ljava/util/List;
    //   64: aload_1
    //   65: iload_2
    //   66: aaload
    //   67: invokeinterface 55 2 0
    //   72: pop
    //   73: iinc 2 1
    //   76: goto -37 -> 39
    //   79: getstatic 1079	com/wurmonline/server/Server:INTRA_COMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   82: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   85: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   88: goto +15 -> 103
    //   91: astore_3
    //   92: getstatic 1079	com/wurmonline/server/Server:INTRA_COMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   95: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   98: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   101: aload_3
    //   102: athrow
    //   103: getstatic 1083	com/wurmonline/server/intra/MoneyTransfer:transfers	Ljava/util/concurrent/ConcurrentLinkedDeque;
    //   106: getstatic 1083	com/wurmonline/server/intra/MoneyTransfer:transfers	Ljava/util/concurrent/ConcurrentLinkedDeque;
    //   109: invokevirtual 1084	java/util/concurrent/ConcurrentLinkedDeque:size	()I
    //   112: anewarray 1085	com/wurmonline/server/intra/MoneyTransfer
    //   115: invokevirtual 1086	java/util/concurrent/ConcurrentLinkedDeque:toArray	([Ljava/lang/Object;)[Ljava/lang/Object;
    //   118: checkcast 1087	[Lcom/wurmonline/server/intra/MoneyTransfer;
    //   121: astore_1
    //   122: iconst_0
    //   123: istore_2
    //   124: iload_2
    //   125: aload_1
    //   126: arraylength
    //   127: if_icmpge +90 -> 217
    //   130: aload_1
    //   131: iload_2
    //   132: aaload
    //   133: invokevirtual 1088	com/wurmonline/server/intra/MoneyTransfer:poll	()Z
    //   136: ifeq +75 -> 211
    //   139: aload_1
    //   140: iload_2
    //   141: aaload
    //   142: getfield 1089	com/wurmonline/server/intra/MoneyTransfer:deleted	Z
    //   145: ifne +15 -> 160
    //   148: aload_1
    //   149: iload_2
    //   150: aaload
    //   151: getfield 1090	com/wurmonline/server/intra/MoneyTransfer:pollTimes	I
    //   154: sipush 500
    //   157: if_icmple +54 -> 211
    //   160: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   163: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   166: new 27	java/lang/StringBuilder
    //   169: dup
    //   170: invokespecial 28	java/lang/StringBuilder:<init>	()V
    //   173: ldc_w 1091
    //   176: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   179: iload_2
    //   180: invokevirtual 35	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   183: ldc_w 1092
    //   186: invokevirtual 30	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   189: aload_1
    //   190: iload_2
    //   191: aaload
    //   192: invokevirtual 886	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   195: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   198: invokevirtual 10	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   201: getstatic 1083	com/wurmonline/server/intra/MoneyTransfer:transfers	Ljava/util/concurrent/ConcurrentLinkedDeque;
    //   204: aload_1
    //   205: iload_2
    //   206: aaload
    //   207: invokevirtual 1093	java/util/concurrent/ConcurrentLinkedDeque:remove	(Ljava/lang/Object;)Z
    //   210: pop
    //   211: iinc 2 1
    //   214: goto -90 -> 124
    //   217: getstatic 1094	com/wurmonline/server/intra/TimeTransfer:transfers	Ljava/util/List;
    //   220: getstatic 1094	com/wurmonline/server/intra/TimeTransfer:transfers	Ljava/util/List;
    //   223: invokeinterface 46 1 0
    //   228: anewarray 1095	com/wurmonline/server/intra/TimeTransfer
    //   231: invokeinterface 48 2 0
    //   236: checkcast 1096	[Lcom/wurmonline/server/intra/TimeTransfer;
    //   239: astore_2
    //   240: aload_2
    //   241: astore_3
    //   242: aload_3
    //   243: arraylength
    //   244: istore 4
    //   246: iconst_0
    //   247: istore 5
    //   249: iload 5
    //   251: iload 4
    //   253: if_icmpge +54 -> 307
    //   256: aload_3
    //   257: iload 5
    //   259: aaload
    //   260: astore 6
    //   262: aload 6
    //   264: invokevirtual 1097	com/wurmonline/server/intra/TimeTransfer:poll	()Z
    //   267: ifeq +34 -> 301
    //   270: aload 6
    //   272: getfield 1098	com/wurmonline/server/intra/TimeTransfer:deleted	Z
    //   275: ifeq +26 -> 301
    //   278: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   281: getstatic 8	java/util/logging/Level:INFO	Ljava/util/logging/Level;
    //   284: ldc_w 1099
    //   287: invokevirtual 10	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;)V
    //   290: getstatic 1094	com/wurmonline/server/intra/TimeTransfer:transfers	Ljava/util/List;
    //   293: aload 6
    //   295: invokeinterface 55 2 0
    //   300: pop
    //   301: iinc 5 1
    //   304: goto -55 -> 249
    //   307: getstatic 1100	com/wurmonline/server/intra/PasswordTransfer:transfers	Ljava/util/List;
    //   310: getstatic 1100	com/wurmonline/server/intra/PasswordTransfer:transfers	Ljava/util/List;
    //   313: invokeinterface 46 1 0
    //   318: anewarray 1101	com/wurmonline/server/intra/PasswordTransfer
    //   321: invokeinterface 48 2 0
    //   326: checkcast 1102	[Lcom/wurmonline/server/intra/PasswordTransfer;
    //   329: astore_3
    //   330: aload_3
    //   331: astore 4
    //   333: aload 4
    //   335: arraylength
    //   336: istore 5
    //   338: iconst_0
    //   339: istore 6
    //   341: iload 6
    //   343: iload 5
    //   345: if_icmpge +43 -> 388
    //   348: aload 4
    //   350: iload 6
    //   352: aaload
    //   353: astore 7
    //   355: aload 7
    //   357: invokevirtual 1103	com/wurmonline/server/intra/PasswordTransfer:poll	()Z
    //   360: ifeq +22 -> 382
    //   363: aload 7
    //   365: getfield 1104	com/wurmonline/server/intra/PasswordTransfer:deleted	Z
    //   368: ifeq +14 -> 382
    //   371: getstatic 1100	com/wurmonline/server/intra/PasswordTransfer:transfers	Ljava/util/List;
    //   374: aload 7
    //   376: invokeinterface 55 2 0
    //   381: pop
    //   382: iinc 6 1
    //   385: goto -44 -> 341
    //   388: goto +18 -> 406
    //   391: astore_1
    //   392: getstatic 3	com/wurmonline/server/Server:logger	Ljava/util/logging/Logger;
    //   395: getstatic 56	java/util/logging/Level:WARNING	Ljava/util/logging/Level;
    //   398: aload_1
    //   399: invokevirtual 767	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   402: aload_1
    //   403: invokevirtual 18	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   406: return
    // Line number table:
    //   Java source line #4028	-> byte code offset #0
    //   Java source line #4033	-> byte code offset #12
    //   Java source line #4034	-> byte code offset #37
    //   Java source line #4042	-> byte code offset #45
    //   Java source line #4044	-> byte code offset #51
    //   Java source line #4045	-> byte code offset #60
    //   Java source line #4034	-> byte code offset #73
    //   Java source line #4055	-> byte code offset #79
    //   Java source line #4056	-> byte code offset #88
    //   Java source line #4055	-> byte code offset #91
    //   Java source line #4056	-> byte code offset #101
    //   Java source line #4058	-> byte code offset #103
    //   Java source line #4059	-> byte code offset #109
    //   Java source line #4060	-> byte code offset #122
    //   Java source line #4062	-> byte code offset #130
    //   Java source line #4064	-> byte code offset #139
    //   Java source line #4066	-> byte code offset #160
    //   Java source line #4067	-> byte code offset #201
    //   Java source line #4060	-> byte code offset #211
    //   Java source line #4079	-> byte code offset #217
    //   Java source line #4080	-> byte code offset #240
    //   Java source line #4082	-> byte code offset #262
    //   Java source line #4084	-> byte code offset #270
    //   Java source line #4086	-> byte code offset #278
    //   Java source line #4087	-> byte code offset #290
    //   Java source line #4080	-> byte code offset #301
    //   Java source line #4099	-> byte code offset #307
    //   Java source line #4100	-> byte code offset #313
    //   Java source line #4101	-> byte code offset #330
    //   Java source line #4103	-> byte code offset #355
    //   Java source line #4105	-> byte code offset #363
    //   Java source line #4108	-> byte code offset #371
    //   Java source line #4101	-> byte code offset #382
    //   Java source line #4123	-> byte code offset #388
    //   Java source line #4120	-> byte code offset #391
    //   Java source line #4122	-> byte code offset #392
    //   Java source line #4124	-> byte code offset #406
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	407	0	this	Server
    //   36	29	1	comms	IntraCommand[]
    //   121	84	1	transfers	com.wurmonline.server.intra.MoneyTransfer[]
    //   391	12	1	ex	Exception
    //   38	36	2	x	int
    //   123	89	2	x	int
    //   239	2	2	ttransfers	com.wurmonline.server.intra.TimeTransfer[]
    //   91	11	3	localObject	Object
    //   241	16	3	arrayOfTimeTransfer1	com.wurmonline.server.intra.TimeTransfer[]
    //   329	2	3	ptransfers	com.wurmonline.server.intra.PasswordTransfer[]
    //   244	10	4	i	int
    //   331	18	4	arrayOfPasswordTransfer1	com.wurmonline.server.intra.PasswordTransfer[]
    //   247	99	5	localTimeTransfer1	com.wurmonline.server.intra.TimeTransfer
    //   260	123	6	lTtransfer	com.wurmonline.server.intra.TimeTransfer
    //   353	22	7	lPtransfer	com.wurmonline.server.intra.PasswordTransfer
    // Exception table:
    //   from	to	target	type
    //   12	79	91	finally
    //   0	388	391	java/lang/Exception
  }
  
  /* Error */
  public void addIntraCommand(IntraCommand command)
  {
    // Byte code:
    //   0: getstatic 1079	com/wurmonline/server/Server:INTRA_COMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: aload_0
    //   10: getfield 73	com/wurmonline/server/Server:intraCommands	Ljava/util/List;
    //   13: aload_1
    //   14: invokeinterface 24 2 0
    //   19: pop
    //   20: getstatic 1079	com/wurmonline/server/Server:INTRA_COMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   23: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   26: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   29: goto +15 -> 44
    //   32: astore_2
    //   33: getstatic 1079	com/wurmonline/server/Server:INTRA_COMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   36: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   39: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   42: aload_2
    //   43: athrow
    //   44: return
    // Line number table:
    //   Java source line #4128	-> byte code offset #0
    //   Java source line #4131	-> byte code offset #9
    //   Java source line #4135	-> byte code offset #20
    //   Java source line #4136	-> byte code offset #29
    //   Java source line #4135	-> byte code offset #32
    //   Java source line #4136	-> byte code offset #42
    //   Java source line #4137	-> byte code offset #44
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	45	0	this	Server
    //   0	45	1	command	IntraCommand
    //   32	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	20	32	finally
  }
  
  /* Error */
  public void addWebCommand(WebCommand command)
  {
    // Byte code:
    //   0: getstatic 1105	com/wurmonline/server/Server:WEBCOMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 1106	com/wurmonline/server/Server:webcommands	Ljava/util/Set;
    //   12: aload_1
    //   13: invokeinterface 1107 2 0
    //   18: pop
    //   19: getstatic 1105	com/wurmonline/server/Server:WEBCOMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   22: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   25: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   28: goto +15 -> 43
    //   31: astore_2
    //   32: getstatic 1105	com/wurmonline/server/Server:WEBCOMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   35: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   38: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   41: aload_2
    //   42: athrow
    //   43: return
    // Line number table:
    //   Java source line #4159	-> byte code offset #0
    //   Java source line #4160	-> byte code offset #9
    //   Java source line #4164	-> byte code offset #19
    //   Java source line #4165	-> byte code offset #28
    //   Java source line #4164	-> byte code offset #31
    //   Java source line #4165	-> byte code offset #41
    //   Java source line #4167	-> byte code offset #43
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	44	0	this	Server
    //   0	44	1	command	WebCommand
    //   31	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	19	31	finally
  }
  
  /* Error */
  private void pollWebCommands()
  {
    // Byte code:
    //   0: getstatic 1105	com/wurmonline/server/Server:WEBCOMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 1106	com/wurmonline/server/Server:webcommands	Ljava/util/Set;
    //   12: invokeinterface 598 1 0
    //   17: astore_1
    //   18: aload_1
    //   19: invokeinterface 599 1 0
    //   24: ifeq +20 -> 44
    //   27: aload_1
    //   28: invokeinterface 600 1 0
    //   33: checkcast 1108	com/wurmonline/server/webinterface/WebCommand
    //   36: astore_2
    //   37: aload_2
    //   38: invokevirtual 1109	com/wurmonline/server/webinterface/WebCommand:execute	()V
    //   41: goto -23 -> 18
    //   44: getstatic 1106	com/wurmonline/server/Server:webcommands	Ljava/util/Set;
    //   47: invokeinterface 1110 1 0
    //   52: getstatic 1105	com/wurmonline/server/Server:WEBCOMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   55: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   58: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   61: goto +15 -> 76
    //   64: astore_3
    //   65: getstatic 1105	com/wurmonline/server/Server:WEBCOMMANDS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   68: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   71: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   74: aload_3
    //   75: athrow
    //   76: return
    // Line number table:
    //   Java source line #4173	-> byte code offset #0
    //   Java source line #4174	-> byte code offset #9
    //   Java source line #4176	-> byte code offset #37
    //   Java source line #4177	-> byte code offset #41
    //   Java source line #4178	-> byte code offset #44
    //   Java source line #4182	-> byte code offset #52
    //   Java source line #4183	-> byte code offset #61
    //   Java source line #4182	-> byte code offset #64
    //   Java source line #4183	-> byte code offset #74
    //   Java source line #4184	-> byte code offset #76
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	77	0	this	Server
    //   17	11	1	localIterator	Iterator
    //   36	2	2	wc	WebCommand
    //   64	11	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	52	64	finally
  }
  
  /* Error */
  public void addTerraformingTask(TerraformingTask task)
  {
    // Byte code:
    //   0: getstatic 1111	com/wurmonline/server/Server:TERRAFORMINGTASKS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 22	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 1112	com/wurmonline/server/Server:terraformingTasks	Ljava/util/Set;
    //   12: aload_1
    //   13: invokeinterface 1107 2 0
    //   18: pop
    //   19: getstatic 1111	com/wurmonline/server/Server:TERRAFORMINGTASKS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   22: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   25: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   28: goto +15 -> 43
    //   31: astore_2
    //   32: getstatic 1111	com/wurmonline/server/Server:TERRAFORMINGTASKS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   35: invokevirtual 21	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   38: invokevirtual 25	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   41: aload_2
    //   42: athrow
    //   43: return
    // Line number table:
    //   Java source line #4190	-> byte code offset #0
    //   Java source line #4191	-> byte code offset #9
    //   Java source line #4195	-> byte code offset #19
    //   Java source line #4196	-> byte code offset #28
    //   Java source line #4195	-> byte code offset #31
    //   Java source line #4196	-> byte code offset #41
    //   Java source line #4198	-> byte code offset #43
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	44	0	this	Server
    //   0	44	1	task	TerraformingTask
    //   31	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	19	31	finally
  }
  
  private void pollTerraformingTasks()
  {
    try
    {
      TERRAFORMINGTASKS_RW_LOCK.writeLock().lock();
      TerraformingTask[] tasks = (TerraformingTask[])terraformingTasks.toArray(new TerraformingTask[terraformingTasks.size()]);
      for (TerraformingTask task : tasks) {
        if (task.poll()) {
          terraformingTasks.remove(task);
        }
      }
    }
    finally
    {
      TERRAFORMINGTASKS_RW_LOCK.writeLock().unlock();
    }
  }
  
  public byte[] getExternalIp()
  {
    return this.externalIp;
  }
  
  public byte[] getInternalIp()
  {
    return this.internalIp;
  }
  
  public int getIntraServerPort()
  {
    return Integer.parseInt(Servers.localServer.INTRASERVERPORT);
  }
  
  public static short getMolRehanX()
  {
    return molRehanX;
  }
  
  public static void setMolRehanX(short aMolRehanX)
  {
    molRehanX = aMolRehanX;
  }
  
  public static short getMolRehanY()
  {
    return molRehanY;
  }
  
  public static void setMolRehanY(short aMolRehanY)
  {
    molRehanY = aMolRehanY;
  }
  
  public static void incrementOldPremiums(String name)
  {
    if (System.currentTimeMillis() - lastResetOldPremiums > 10800000L)
    {
      oldPremiums = 0;
      lastResetOldPremiums = System.currentTimeMillis();
    }
    oldPremiums += 1;
    if ((!appointedSixThousand) && ((PlayerInfoFactory.getNumberOfPayingPlayers() + 1) % 1000 == 0))
    {
      logger.log(Level.INFO, name + " IS THE NUMBER " + (PlayerInfoFactory.getNumberOfPayingPlayers() + 1) + " PAYING PLAYER");
      
      appointedSixThousand = true;
    }
  }
  
  public static long getStartTime()
  {
    return startTime;
  }
  
  public static long getMillisToShutDown()
  {
    return millisToShutDown;
  }
  
  public static String getShutdownReason()
  {
    return shutdownReason;
  }
  
  public static Weather getWeather()
  {
    return weather;
  }
  
  public static int getCombatCounter()
  {
    return combatCounter;
  }
  
  public static void incrementCombatCounter()
  {
    combatCounter += 1;
  }
  
  public static int getSecondsUptime()
  {
    return secondsUptime;
  }
  
  public static void incrementSecondsUptime()
  {
    secondsUptime += 1;
    Players.getInstance().tickSecond();
    lastLagticks = lagticks;
    if (lastLagticks > 0.0F) {
      lagMoveModifier = (int)Math.max(10.0F, lastLagticks / 30.0F * 24.0F);
    } else {
      lagMoveModifier = 0;
    }
    lagticks = 0;
    Effect globalEffect;
    if (WurmCalendar.isNewYear1())
    {
      logger.log(Level.INFO, "IT's NEW YEAR");
      if (secondsUptime % 20 == 0) {
        if (rand.nextBoolean())
        {
          Effect globalEffect = EffectFactory.getInstance().createSpawnEff(WurmId.getNextTempItemId(), rand
            .nextFloat() * Zones.worldMeterSizeX, rand.nextFloat() * Zones.worldMeterSizeY, 0.0F, true);
          
          newYearEffects.add(Integer.valueOf(globalEffect.getId()));
          try
          {
            ItemFactory.createItem(52, rand.nextFloat() * 90.0F + 1.0F, globalEffect
              .getPosX(), globalEffect.getPosY(), globalEffect.getPosZ(), true, (byte)8, 
              getRandomRarityNotCommon(), -10L, "", (byte)0);
          }
          catch (Exception localException) {}
        }
        else
        {
          globalEffect = EffectFactory.getInstance().createChristmasEff(WurmId.getNextTempItemId(), rand
            .nextFloat() * Zones.worldMeterSizeX, rand.nextFloat() * Zones.worldMeterSizeY, 0.0F, true);
          
          newYearEffects.add(Integer.valueOf(globalEffect.getId()));
          try
          {
            ItemFactory.createItem(52, rand.nextFloat() * 90.0F + 1.0F, globalEffect
              .getPosX(), globalEffect.getPosY(), globalEffect.getPosZ(), true, (byte)8, 
              getRandomRarityNotCommon(), -10L, "", (byte)0);
          }
          catch (Exception localException1) {}
        }
      }
      if (secondsUptime % 11 == 0) {
        Zones.sendNewYear();
      }
    }
    else if (WurmCalendar.isAfterNewYear1())
    {
      if ((newYearEffects != null) && (!newYearEffects.isEmpty())) {
        for (Integer l : newYearEffects) {
          EffectFactory.getInstance().deleteEffect(l.intValue());
        }
      }
      if (newYearEffects != null) {
        newYearEffects.clear();
      }
    }
  }
  
  public static final byte getRandomRarityNotCommon()
  {
    if (rand.nextFloat() * 10000.0F <= 1.0F) {
      return 3;
    }
    if (rand.nextInt(100) <= 0) {
      return 2;
    }
    return 1;
  }
  
  private static void startSendWeatherThread()
  {
    new Server.3().start();
    
    Players.getInstance().sendWeather();
  }
  
  private static void addShutdownHook()
  {
    logger.info("Adding Shutdown Hook");
    
    Runtime.getRuntime().addShutdownHook(new Server.4("WurmServerShutdownHook-Thread"));
  }
  
  public static final double getModifiedFloatEffect(double eff)
  {
    if (EpicServer)
    {
      double modEff = 0.0D;
      if (eff >= 1.0D)
      {
        if (eff <= 70.0D) {
          modEff = 1.3571428060531616D * eff;
        } else {
          modEff = 0.949999988079071D + (eff - 70.0D) * 0.1666666716337204D;
        }
      }
      else {
        modEff = 1.0D - (1.0D - eff) * (1.0D - eff);
      }
      return modEff;
    }
    return eff;
  }
  
  public static final double getModifiedPercentageEffect(double eff)
  {
    if ((EpicServer) || (ChallengeServer))
    {
      double modEff = 0.0D;
      if (eff >= 100.0D)
      {
        if (eff <= 7000.0D) {
          modEff = 1.3571428060531616D * eff;
        } else {
          modEff = 95.0D + (eff - 7000.0D) * 0.1666666716337204D;
        }
      }
      else {
        modEff = (10000.0D - (100.0D - eff) * (100.0D - eff)) / 100.0D;
      }
      return modEff;
    }
    return eff;
  }
  
  public static final double getBuffedQualityEffect(double eff)
  {
    if (eff < 1.0D) {
      return Math.max(0.05D, 1.0D - (1.0D - eff) * (1.0D - eff));
    }
    double base = 2.0D;
    double pow1 = 1.3D;
    double pow2 = 3.0D;
    double newPower = 1.0D + base * (1.0D - Math.pow(2.0D, -Math.pow(eff - 1.0D, pow1) / pow2));
    return newPower;
  }
  
  public static final HexMap getEpicMap()
  {
    return epicMap;
  }
  
  public void broadCastEpicEvent(String event)
  {
    Servers.localServer.createChampTwit(event);
  }
  
  public void broadCastEpicWinCondition(String scenarioname, String scenarioQuest)
  {
    Servers.localServer.createChampTwit(scenarioname + " has begun. " + scenarioQuest);
  }
  
  public final boolean hasThunderMode()
  {
    return this.thunderMode;
  }
  
  public final short getLowDirtHeight(int x, int y)
  {
    Integer xy = Integer.valueOf(x | y << Constants.meshSize);
    if (lowDirtHeight.containsKey(xy)) {
      return ((Short)lowDirtHeight.get(xy)).shortValue();
    }
    return Tiles.decodeHeight(surfaceMesh.getTile(x, y));
  }
  
  public static final boolean isDirtHeightLower(int x, int y, short ht)
  {
    Integer xy = Integer.valueOf(x | y << Constants.meshSize);
    short cHt;
    if (lowDirtHeight.containsKey(xy))
    {
      short cHt = ((Short)lowDirtHeight.get(xy)).shortValue();
      if (ht < cHt) {
        lowDirtHeight.put(xy, Short.valueOf(ht));
      }
    }
    else
    {
      cHt = Tiles.decodeHeight(surfaceMesh.getTile(x, y));
      lowDirtHeight.put(xy, Short.valueOf((short)Math.min(cHt, ht)));
    }
    return ht < cHt;
  }
  
  public boolean isPS()
  {
    return this.isPS;
  }
  
  public void setIsPS(boolean ps)
  {
    this.isPS = ps;
  }
  
  public final void addGlobalTempEffect(long id, long expiretime)
  {
    tempEffects.put(Long.valueOf(id), Long.valueOf(expiretime));
  }
  
  public static short getMaxHeight()
  {
    return surfaceMesh.getMaxHeight();
  }
  
  public HighwayFinder getHighwayFinderThread()
  {
    return this.highwayFinderThread;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\Server.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */