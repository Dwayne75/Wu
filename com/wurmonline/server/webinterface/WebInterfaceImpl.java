package com.wurmonline.server.webinterface;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.epic.Valrei;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.PasswordTransfer;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMetaData;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Awards;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.PendingAccount;
import com.wurmonline.server.players.PendingAward;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Reimbursement;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.questions.AscensionQuestion;
import com.wurmonline.server.questions.NewsInfo;
import com.wurmonline.server.questions.WurmInfo;
import com.wurmonline.server.questions.WurmInfo2;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillStat;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebInterfaceImpl
  extends UnicastRemoteObject
  implements WebInterface, Serializable, MiscConstants, TimeConstants, CounterTypes, MonetaryConstants
{
  public static final String VERSION = "$Revision: 1.54 $";
  public static String mailAccount = "mail@mydomain.com";
  public static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[\\w\\.\\+-=]+@[\\w\\.-]+\\.[\\w-]+$");
  private static final String PASSWORD_CHARS = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789";
  private static final long serialVersionUID = -2682536434841429586L;
  private final boolean isRunning = true;
  private final Random faceRandom = new Random();
  private static final long faceRandomSeed = 8263186381637L;
  private static final DecimalFormat twoDecimals = new DecimalFormat("##0.00");
  private static final Set<String> moneyDetails = new HashSet();
  private static final Set<String> timeDetails = new HashSet();
  private static final Logger logger = Logger.getLogger(WebInterfaceImpl.class.getName());
  private static final long[] noInfoLong = { -1L, -1L };
  private static final String BAD_PASSWORD = "Access denied.";
  private final SimpleDateFormat alloformatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
  private String hostname = "localhost";
  private static final Map<String, Long> ipAttempts = new HashMap();
  private String[] bannedMailHosts = { "sharklasers", "spam4", "grr.la", "guerrillamail" };
  
  public WebInterfaceImpl(int port)
    throws RemoteException
  {
    super(port);
    try
    {
      InetAddress localMachine = InetAddress.getLocalHost();
      this.hostname = localMachine.getHostName();
      logger.info("Hostname of local machine used to send registration emails: " + this.hostname);
    }
    catch (UnknownHostException uhe)
    {
      throw new RemoteException("Could not find localhost for WebInterface", uhe);
    }
  }
  
  public WebInterfaceImpl()
    throws RemoteException
  {}
  
  private String getRemoteClientDetails()
  {
    try
    {
      return getClientHost();
    }
    catch (ServerNotActiveException e)
    {
      logger.log(Level.WARNING, "Could not get ClientHost details due to " + e.getMessage(), e);
    }
    return "Unknown Remote Client";
  }
  
  public int getPower(String intraServerPassword, long aPlayerID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPower for playerID: " + aPlayerID);
    }
    try
    {
      PlayerInfo p = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
      
      p.load();
      return p.getPower();
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, aPlayerID + ": " + iox.getMessage(), iox);
      return 0;
    }
    catch (NoSuchPlayerException localNoSuchPlayerException) {}
    return 0;
  }
  
  public boolean isRunning(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " isRunning");
    }
    return true;
  }
  
  public int getPlayerCount(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPlayerCount");
    }
    return Players.getInstance().numberOfPlayers();
  }
  
  public int getPremiumPlayerCount(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPremiumPlayerCount");
    }
    return Players.getInstance().numberOfPremiumPlayers();
  }
  
  /* Error */
  public String getTestMessage(String intraServerPassword)
    throws RemoteException
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: invokespecial 39	com/wurmonline/server/webinterface/WebInterfaceImpl:validateIntraServerPassword	(Ljava/lang/String;)V
    //   5: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   8: getstatic 40	java/util/logging/Level:FINER	Ljava/util/logging/Level;
    //   11: invokevirtual 41	java/util/logging/Logger:isLoggable	(Ljava/util/logging/Level;)Z
    //   14: ifeq +31 -> 45
    //   17: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   20: new 21	java/lang/StringBuilder
    //   23: dup
    //   24: invokespecial 22	java/lang/StringBuilder:<init>	()V
    //   27: aload_0
    //   28: invokespecial 42	com/wurmonline/server/webinterface/WebInterfaceImpl:getRemoteClientDetails	()Ljava/lang/String;
    //   31: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   34: ldc 61
    //   36: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   39: invokevirtual 25	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   42: invokevirtual 45	java/util/logging/Logger:finer	(Ljava/lang/String;)V
    //   45: getstatic 62	com/wurmonline/server/Server:SYNC_LOCK	Ljava/lang/Object;
    //   48: dup
    //   49: astore_2
    //   50: monitorenter
    //   51: new 21	java/lang/StringBuilder
    //   54: dup
    //   55: invokespecial 22	java/lang/StringBuilder:<init>	()V
    //   58: ldc 63
    //   60: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   63: invokestatic 64	java/lang/System:currentTimeMillis	()J
    //   66: invokevirtual 44	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   69: invokevirtual 25	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   72: aload_2
    //   73: monitorexit
    //   74: areturn
    //   75: astore_3
    //   76: aload_2
    //   77: monitorexit
    //   78: aload_3
    //   79: athrow
    // Line number table:
    //   Java source line #305	-> byte code offset #0
    //   Java source line #306	-> byte code offset #5
    //   Java source line #308	-> byte code offset #17
    //   Java source line #310	-> byte code offset #45
    //   Java source line #312	-> byte code offset #51
    //   Java source line #313	-> byte code offset #75
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	80	0	this	WebInterfaceImpl
    //   0	80	1	intraServerPassword	String
    //   49	28	2	Ljava/lang/Object;	Object
    //   75	4	3	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   51	74	75	finally
    //   75	78	75	finally
  }
  
  public void broadcastMessage(String intraServerPassword, String message)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " broadcastMessage: " + message);
    }
    synchronized (Server.SYNC_LOCK)
    {
      Server.getInstance().broadCastAlert(message);
    }
  }
  
  public long getAccountStatusForPlayer(String intraServerPassword, String playerName)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getAccountStatusForPlayer for player: " + playerName);
    }
    synchronized (Server.SYNC_LOCK)
    {
      if (Servers.localServer.id != Servers.loginServer.id) {
        throw new RemoteException("Not a valid request for this server. Ask the login server instead.");
      }
      PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
      try
      {
        p.load();
        return p.money;
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, playerName + ": " + iox.getMessage(), iox);
        return 0L;
      }
    }
  }
  
  /* Error */
  public Map<String, Integer> getBattleRanks(String intraServerPassword, int numberOfRanksToGet)
    throws RemoteException
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: invokespecial 39	com/wurmonline/server/webinterface/WebInterfaceImpl:validateIntraServerPassword	(Ljava/lang/String;)V
    //   5: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   8: getstatic 40	java/util/logging/Level:FINER	Ljava/util/logging/Level;
    //   11: invokevirtual 41	java/util/logging/Logger:isLoggable	(Ljava/util/logging/Level;)Z
    //   14: ifeq +35 -> 49
    //   17: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   20: new 21	java/lang/StringBuilder
    //   23: dup
    //   24: invokespecial 22	java/lang/StringBuilder:<init>	()V
    //   27: aload_0
    //   28: invokespecial 42	com/wurmonline/server/webinterface/WebInterfaceImpl:getRemoteClientDetails	()Ljava/lang/String;
    //   31: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   34: ldc 75
    //   36: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   39: iload_2
    //   40: invokevirtual 76	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   43: invokevirtual 25	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   46: invokevirtual 45	java/util/logging/Logger:finer	(Ljava/lang/String;)V
    //   49: getstatic 62	com/wurmonline/server/Server:SYNC_LOCK	Ljava/lang/Object;
    //   52: dup
    //   53: astore_3
    //   54: monitorenter
    //   55: iload_2
    //   56: invokestatic 77	com/wurmonline/server/Players:getBattleRanks	(I)Ljava/util/Map;
    //   59: aload_3
    //   60: monitorexit
    //   61: areturn
    //   62: astore 4
    //   64: aload_3
    //   65: monitorexit
    //   66: aload 4
    //   68: athrow
    // Line number table:
    //   Java source line #381	-> byte code offset #0
    //   Java source line #382	-> byte code offset #5
    //   Java source line #384	-> byte code offset #17
    //   Java source line #386	-> byte code offset #49
    //   Java source line #388	-> byte code offset #55
    //   Java source line #389	-> byte code offset #62
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	69	0	this	WebInterfaceImpl
    //   0	69	1	intraServerPassword	String
    //   0	69	2	numberOfRanksToGet	int
    //   53	12	3	Ljava/lang/Object;	Object
    //   62	5	4	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   55	61	62	finally
    //   62	66	62	finally
  }
  
  public String getServerStatus(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getServerStatus");
    }
    synchronized (Server.SYNC_LOCK)
    {
      String toReturn = "Up and running.";
      if (Server.getMillisToShutDown() > -1000L) {
        toReturn = "Shutting down in " + Server.getMillisToShutDown() / 1000L + " seconds: " + Server.getShutdownReason();
      } else if (Constants.maintaining) {
        toReturn = "The server is in maintenance mode and not open for connections.";
      }
      return toReturn;
    }
  }
  
  /* Error */
  public Map<String, Long> getFriends(String intraServerPassword, long aPlayerID)
    throws RemoteException
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: invokespecial 39	com/wurmonline/server/webinterface/WebInterfaceImpl:validateIntraServerPassword	(Ljava/lang/String;)V
    //   5: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   8: getstatic 40	java/util/logging/Level:FINER	Ljava/util/logging/Level;
    //   11: invokevirtual 41	java/util/logging/Logger:isLoggable	(Ljava/util/logging/Level;)Z
    //   14: ifeq +35 -> 49
    //   17: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   20: new 21	java/lang/StringBuilder
    //   23: dup
    //   24: invokespecial 22	java/lang/StringBuilder:<init>	()V
    //   27: aload_0
    //   28: invokespecial 42	com/wurmonline/server/webinterface/WebInterfaceImpl:getRemoteClientDetails	()Ljava/lang/String;
    //   31: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   34: ldc 90
    //   36: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   39: lload_2
    //   40: invokevirtual 44	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
    //   43: invokevirtual 25	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   46: invokevirtual 45	java/util/logging/Logger:finer	(Ljava/lang/String;)V
    //   49: getstatic 62	com/wurmonline/server/Server:SYNC_LOCK	Ljava/lang/Object;
    //   52: dup
    //   53: astore 4
    //   55: monitorenter
    //   56: lload_2
    //   57: invokestatic 91	com/wurmonline/server/Players:getFriends	(J)Ljava/util/Map;
    //   60: aload 4
    //   62: monitorexit
    //   63: areturn
    //   64: astore 5
    //   66: aload 4
    //   68: monitorexit
    //   69: aload 5
    //   71: athrow
    // Line number table:
    //   Java source line #446	-> byte code offset #0
    //   Java source line #447	-> byte code offset #5
    //   Java source line #449	-> byte code offset #17
    //   Java source line #451	-> byte code offset #49
    //   Java source line #453	-> byte code offset #56
    //   Java source line #454	-> byte code offset #64
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	72	0	this	WebInterfaceImpl
    //   0	72	1	intraServerPassword	String
    //   0	72	2	aPlayerID	long
    //   53	14	4	Ljava/lang/Object;	Object
    //   64	6	5	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   56	63	64	finally
    //   64	69	64	finally
  }
  
  public Map<String, String> getInventory(String intraServerPassword, long aPlayerID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getInventory for playerid: " + aPlayerID);
    }
    synchronized (Server.SYNC_LOCK)
    {
      Map<String, String> toReturn = new HashMap();
      try
      {
        Player p = Players.getInstance().getPlayer(aPlayerID);
        Item inventory = p.getInventory();
        Item[] items = inventory.getAllItems(false);
        for (int x = 0; x < items.length; x++) {
          toReturn.put(String.valueOf(items[x].getWurmId()), items[x]
            .getName() + ", QL: " + items[x].getQualityLevel() + ", DAM: " + items[x].getDamage());
        }
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
      return toReturn;
    }
  }
  
  public Map<Long, Long> getBodyItems(String intraServerPassword, long aPlayerID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getBodyItems for playerid: " + aPlayerID);
    }
    synchronized (Server.SYNC_LOCK)
    {
      Map<Long, Long> toReturn = new HashMap();
      try
      {
        Player p = Players.getInstance().getPlayer(aPlayerID);
        Body lBody = p.getBody();
        if (lBody != null)
        {
          Item[] items = lBody.getAllItems();
          for (int x = 0; x < items.length; x++) {
            toReturn.put(Long.valueOf(items[x].getWurmId()), Long.valueOf(items[x].getParentId()));
          }
        }
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
      return toReturn;
    }
  }
  
  public Map<String, Float> getSkills(String intraServerPassword, long aPlayerID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getSkills for playerid: " + aPlayerID);
    }
    synchronized (Server.SYNC_LOCK)
    {
      Map<String, Float> toReturn = new HashMap();
      Skills skills = SkillsFactory.createSkills(aPlayerID);
      try
      {
        skills.load();
        Skill[] skillarr = skills.getSkills();
        for (int x = 0; x < skillarr.length; x++) {
          toReturn.put(skillarr[x].getName(), new Float(skillarr[x].getKnowledge(0.0D)));
        }
      }
      catch (Exception iox)
      {
        logger.log(Level.WARNING, aPlayerID + ": " + iox.getMessage(), iox);
      }
      return toReturn;
    }
  }
  
  public Map<String, ?> getPlayerSummary(String intraServerPassword, long aPlayerID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPlayerSummary for playerid: " + aPlayerID);
    }
    synchronized (Server.SYNC_LOCK)
    {
      Map<String, Object> toReturn = new HashMap();
      if (WurmId.getType(aPlayerID) == 0) {
        try
        {
          Player p = Players.getInstance().getPlayer(aPlayerID);
          toReturn.put("Name", p.getName());
          if (p.citizenVillage != null)
          {
            Citizen citiz = p.citizenVillage.getCitizen(aPlayerID);
            toReturn.put("CitizenVillage", p.citizenVillage.getName());
            toReturn.put("CitizenRole", citiz.getRole().getName());
          }
          String location = "unknown";
          if (p.currentVillage != null) {
            location = p.currentVillage.getName() + ", in " + Kingdoms.getNameFor(p.currentVillage.kingdom);
          } else if (p.currentKingdom != 0) {
            location = Kingdoms.getNameFor(p.currentKingdom);
          }
          toReturn.put("Location", location);
          if (p.getDeity() != null) {
            toReturn.put("Deity", p.getDeity().name);
          }
          toReturn.put("Faith", new Float(p.getFaith()));
          toReturn.put("Favor", new Float(p.getFavor()));
          toReturn.put("Gender", Byte.valueOf(p.getSex()));
          toReturn.put("Alignment", new Float(p.getAlignment()));
          toReturn.put("Kingdom", Byte.valueOf(p.getKingdomId()));
          toReturn.put("Battle rank", Integer.valueOf(p.getRank()));
          toReturn.put("WurmId", new Long(aPlayerID));
          
          toReturn.put("Banned", Boolean.valueOf(p.getSaveFile().isBanned()));
          toReturn.put("Money in bank", Long.valueOf(p.getMoney()));
          toReturn.put("Payment", new Date(p.getPaymentExpire()));
          toReturn.put("Email", p.getSaveFile().emailAddress);
          toReturn.put("Current server", Integer.valueOf(Servers.localServer.id));
          toReturn.put("Last login", new Date(p.getLastLogin()));
          toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
          if (p.getSaveFile().isBanned())
          {
            toReturn.put("IPBan reason", p.getSaveFile().banreason);
            toReturn.put("IPBan expires in", 
              Server.getTimeFor(p.getSaveFile().banexpiry - System.currentTimeMillis()));
          }
          toReturn.put("Warnings", String.valueOf(p.getSaveFile().getWarnings()));
          if (p.isMute())
          {
            toReturn.put("Muted", Boolean.TRUE);
            toReturn.put("Mute reason", p.getSaveFile().mutereason);
            toReturn.put("Mute expires in", 
              Server.getTimeFor(p.getSaveFile().muteexpiry - System.currentTimeMillis()));
          }
          toReturn.put("PlayingTime", Server.getTimeFor(p.getSaveFile().playingTime));
          toReturn.put("Reputation", Integer.valueOf(p.getReputation()));
          if ((p.getTitle() != null) || ((Features.Feature.COMPOUND_TITLES.isEnabled()) && (p.getSecondTitle() != null))) {
            toReturn.put("Title", p.getTitleString());
          }
          toReturn.put("Coord x", Integer.valueOf((int)p.getStatus().getPositionX() >> 2));
          toReturn.put("Coord y", Integer.valueOf((int)p.getStatus().getPositionY() >> 2));
          if (p.isPriest()) {
            toReturn.put("Priest", Boolean.TRUE);
          }
          toReturn.put("LoggedOut", Boolean.valueOf(p.loggedout));
        }
        catch (NoSuchPlayerException nsp)
        {
          try
          {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
            p.load();
            toReturn.put("Name", p.getName());
            if (p.getDeity() != null) {
              toReturn.put("Deity", p.getDeity().name);
            }
            toReturn.put("Faith", new Float(p.getFaith()));
            toReturn.put("Favor", new Float(p.getFavor()));
            toReturn.put("Current server", Integer.valueOf(p.currentServer));
            toReturn.put("Alignment", new Float(p.getAlignment()));
            
            toReturn.put("Battle rank", Integer.valueOf(p.getRank()));
            toReturn.put("WurmId", new Long(aPlayerID));
            toReturn.put("Banned", Boolean.valueOf(p.isBanned()));
            toReturn.put("Money in bank", new Long(p.money));
            toReturn.put("Payment", new Date(p.getPaymentExpire()));
            toReturn.put("Email", p.emailAddress);
            toReturn.put("Last login", new Date(p.getLastLogin()));
            toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
            if (p.isBanned())
            {
              toReturn.put("IPBan reason", p.banreason);
              toReturn.put("IPBan expires in", Server.getTimeFor(p.banexpiry - System.currentTimeMillis()));
            }
            toReturn.put("Warnings", String.valueOf(p.getWarnings()));
            if (p.isMute())
            {
              toReturn.put("Muted", Boolean.TRUE);
              toReturn.put("Mute reason", p.mutereason);
              toReturn.put("Mute expires in", Server.getTimeFor(p.muteexpiry - System.currentTimeMillis()));
            }
            toReturn.put("PlayingTime", Server.getTimeFor(p.playingTime));
            toReturn.put("Reputation", Integer.valueOf(p.getReputation()));
            if ((p.title != null) && (p.title.getName(true) != null)) {
              toReturn.put("Title", p.title.getName(true));
            }
            if (p.isPriest) {
              toReturn.put("Priest", Boolean.TRUE);
            }
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, aPlayerID + ":" + iox.getMessage(), iox);
          }
          catch (NoSuchPlayerException nsp2)
          {
            logger.log(Level.WARNING, aPlayerID + ":" + nsp2.getMessage(), nsp2);
          }
        }
      } else {
        toReturn.put("Not a player", String.valueOf(aPlayerID));
      }
      return toReturn;
    }
  }
  
  public long getLocalCreationTime(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getLocalCreationTime");
    }
    return Server.getStartTime();
  }
  
  public Map<Integer, String> getKingdoms(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getKingdoms");
    }
    synchronized (Server.SYNC_LOCK)
    {
      Map<Integer, String> toReturn = new HashMap();
      if (Servers.localServer.HOMESERVER)
      {
        toReturn.put(Integer.valueOf(Servers.localServer.KINGDOM), Kingdoms.getNameFor(Servers.localServer.KINGDOM));
      }
      else
      {
        toReturn.put(Integer.valueOf(1), Kingdoms.getNameFor((byte)1));
        toReturn.put(Integer.valueOf(3), Kingdoms.getNameFor((byte)3));
        toReturn.put(Integer.valueOf(2), 
          Kingdoms.getNameFor((byte)2));
      }
      return toReturn;
    }
  }
  
  public Map<Long, String> getPlayersForKingdom(String intraServerPassword, int aKingdom)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPlayersForKingdom: " + aKingdom);
    }
    synchronized (Server.SYNC_LOCK)
    {
      Map<Long, String> toReturn = new HashMap();
      Player[] players = Players.getInstance().getPlayers();
      for (int x = 0; x < players.length; x++) {
        if (players[x].getKingdomId() == aKingdom) {
          toReturn.put(new Long(players[x].getWurmId()), players[x].getName());
        }
      }
      return toReturn;
    }
  }
  
  /* Error */
  public long getPlayerId(String intraServerPassword, String name)
    throws RemoteException
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: invokespecial 39	com/wurmonline/server/webinterface/WebInterfaceImpl:validateIntraServerPassword	(Ljava/lang/String;)V
    //   5: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   8: getstatic 40	java/util/logging/Level:FINER	Ljava/util/logging/Level;
    //   11: invokevirtual 41	java/util/logging/Logger:isLoggable	(Ljava/util/logging/Level;)Z
    //   14: ifeq +35 -> 49
    //   17: getstatic 20	com/wurmonline/server/webinterface/WebInterfaceImpl:logger	Ljava/util/logging/Logger;
    //   20: new 21	java/lang/StringBuilder
    //   23: dup
    //   24: invokespecial 22	java/lang/StringBuilder:<init>	()V
    //   27: aload_0
    //   28: invokespecial 42	com/wurmonline/server/webinterface/WebInterfaceImpl:getRemoteClientDetails	()Ljava/lang/String;
    //   31: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   34: ldc -18
    //   36: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   39: aload_2
    //   40: invokevirtual 24	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   43: invokevirtual 25	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   46: invokevirtual 45	java/util/logging/Logger:finer	(Ljava/lang/String;)V
    //   49: getstatic 62	com/wurmonline/server/Server:SYNC_LOCK	Ljava/lang/Object;
    //   52: dup
    //   53: astore_3
    //   54: monitorenter
    //   55: invokestatic 46	com/wurmonline/server/Players:getInstance	()Lcom/wurmonline/server/Players;
    //   58: aload_2
    //   59: invokestatic 239	com/wurmonline/server/LoginHandler:raiseFirstLetter	(Ljava/lang/String;)Ljava/lang/String;
    //   62: invokevirtual 240	com/wurmonline/server/Players:getWurmIdByPlayerName	(Ljava/lang/String;)J
    //   65: aload_3
    //   66: monitorexit
    //   67: lreturn
    //   68: astore 4
    //   70: aload_3
    //   71: monitorexit
    //   72: aload 4
    //   74: athrow
    // Line number table:
    //   Java source line #862	-> byte code offset #0
    //   Java source line #863	-> byte code offset #5
    //   Java source line #865	-> byte code offset #17
    //   Java source line #867	-> byte code offset #49
    //   Java source line #869	-> byte code offset #55
    //   Java source line #870	-> byte code offset #68
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	75	0	this	WebInterfaceImpl
    //   0	75	1	intraServerPassword	String
    //   0	75	2	name	String
    //   53	18	3	Ljava/lang/Object;	Object
    //   68	5	4	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   55	67	68	finally
    //   68	72	68	finally
  }
  
  public Map<String, ?> createPlayer(String intraServerPassword, String name, String password, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " createPlayer for player name: " + name);
    }
    appearance = Server.rand.nextInt(5);
    
    this.faceRandom.setSeed(8263186381637L + appearance);
    appearance = this.faceRandom.nextLong();
    
    Map<String, Object> toReturn = new HashMap();
    logger.log(Level.INFO, "Trying to create player " + name);
    synchronized (Server.SYNC_LOCK)
    {
      if (isEmailValid(emailAddress)) {
        try
        {
          toReturn.put("PlayerId", new Long(
          
            LoginHandler.createPlayer(name, password, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender)));
        }
        catch (Exception ex)
        {
          toReturn.put("PlayerId", Long.valueOf(-1L));
          toReturn.put("error", ex.getMessage());
          logger.log(Level.WARNING, name + ":" + ex.getMessage(), ex);
        }
      } else {
        toReturn.put("error", "The email address " + emailAddress + " is not valid.");
      }
    }
    return toReturn;
  }
  
  public Map<String, String> getPendingAccounts(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPendingAccounts");
    }
    Map<String, String> toReturn = new HashMap();
    for (Iterator<Map.Entry<String, PendingAccount>> it = PendingAccount.accounts.entrySet().iterator(); it.hasNext();)
    {
      Map.Entry<String, PendingAccount> entry = (Map.Entry)it.next();
      toReturn.put(entry.getKey(), 
        ((PendingAccount)entry.getValue()).emailAddress + ", " + GeneralUtilities.toGMTString(((PendingAccount)entry.getValue()).expiration));
    }
    return toReturn;
  }
  
  public Map<String, String> createPlayerPhaseOne(String intraServerPassword, String aPlayerName, String aEmailAddress)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Map<String, String> toReturn = new HashMap();
    if (Constants.maintaining)
    {
      toReturn.put("error", "The server is currently in maintenance mode.");
      return toReturn;
    }
    logger.log(Level.INFO, getRemoteClientDetails() + " Trying to create player phase one " + aPlayerName);
    synchronized (Server.SYNC_LOCK)
    {
      aPlayerName = LoginHandler.raiseFirstLetter(aPlayerName);
      String errstat = LoginHandler.checkName2(aPlayerName);
      if (errstat.length() == 0)
      {
        if (PlayerInfoFactory.doesPlayerExist(aPlayerName))
        {
          toReturn.put("error", "The name " + aPlayerName + " is taken.");
          return toReturn;
        }
        if (PendingAccount.doesPlayerExist(aPlayerName))
        {
          toReturn.put("error", "The name " + aPlayerName + " is reserved for up to two days.");
          return toReturn;
        }
        if (!isEmailValid(aEmailAddress))
        {
          toReturn.put("error", "The email " + aEmailAddress + " is invalid.");
          return toReturn;
        }
        String[] numAccounts = PlayerInfoFactory.getAccountsForEmail(aEmailAddress);
        if (numAccounts.length >= 5)
        {
          String accnames = "";
          for (int x = 0; x < numAccounts.length; x++) {
            accnames = accnames + " " + numAccounts[x];
          }
          toReturn.put("error", "You may only have 5 accounts. Please play Wurm with any of the following:" + accnames + ".");
          
          return toReturn;
        }
        String[] numAccounts2 = PendingAccount.getAccountsForEmail(aEmailAddress);
        String accnames;
        int x;
        if (numAccounts2.length >= 5)
        {
          accnames = "";
          for (x = 0; x < numAccounts2.length; x++) {
            accnames = accnames + " " + numAccounts2[x];
          }
          toReturn.put("error", "You may only have 5 accounts. The following accounts are awaiting confirmation by following the link in the verification email:" + accnames + ".");
          
          return toReturn;
        }
        for (String blocked : this.bannedMailHosts) {
          if (aEmailAddress.toLowerCase().contains(blocked))
          {
            String domain = aEmailAddress.substring(aEmailAddress.indexOf("@"), aEmailAddress.length());
            toReturn.put("error", "We do not accept email addresses from :" + domain + ".");
            
            return toReturn;
          }
        }
        if (numAccounts.length + numAccounts2.length >= 5)
        {
          String accnames = "";
          for (int x = 0; x < numAccounts.length; x++) {
            accnames = accnames + " " + numAccounts[x];
          }
          for (int x = 0; x < numAccounts2.length; x++) {
            accnames = accnames + " " + numAccounts2[x];
          }
          toReturn.put("error", "You may only have 5 accounts. The following accounts are already registered or awaiting confirmation by following the link in the verification email:" + accnames + ".");
          
          return toReturn;
        }
        String password = generateRandomPassword();
        
        long expireTime = System.currentTimeMillis() + 172800000L;
        PendingAccount pedd = new PendingAccount();
        pedd.accountName = aPlayerName;
        pedd.emailAddress = aEmailAddress;
        pedd.expiration = expireTime;
        pedd.password = password;
        if (pedd.create())
        {
          try
          {
            if (!Constants.devmode)
            {
              String email = Mailer.getPhaseOneMail();
              email = email.replace("@pname", aPlayerName);
              email = email.replace("@email", URLEncoder.encode(aEmailAddress, "UTF-8"));
              email = email.replace("@expiration", GeneralUtilities.toGMTString(expireTime));
              email = email.replace("@password", password);
              
              Mailer.sendMail(mailAccount, aEmailAddress, "Wurm Online character creation request", email);
            }
            else
            {
              toReturn.put("Hash", password);
              logger.log(Level.WARNING, "NO MAIL SENT: DEVMODE ACTIVE");
            }
            toReturn.put("ok", "An email has been sent to " + aEmailAddress + ". You will have to click a link in order to proceed with the registration.");
          }
          catch (Exception ex)
          {
            toReturn.put("error", "An error occured when sending the mail: " + ex.getMessage() + ". No account was reserved.");
            
            pedd.delete();
            logger.log(Level.WARNING, aEmailAddress + ":" + ex.getMessage(), ex);
          }
        }
        else
        {
          toReturn.put("error", "The account could not be created. Please try later.");
          logger.warning(aEmailAddress + " The account could not be created. Please try later.");
        }
      }
      else
      {
        toReturn.put("error", errstat);
      }
    }
    return toReturn;
  }
  
  public Map<String, ?> createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, String phaseOneHash)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " createPlayerPhaseTwo for player name: " + playerName);
    }
    appearance = Server.rand.nextInt(5);
    
    this.faceRandom.setSeed(8263186381637L + appearance);
    appearance = this.faceRandom.nextLong();
    return createPlayerPhaseTwo(intraServerPassword, playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, 1);
  }
  
  public Map<String, ?> createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, String phaseOneHash, int serverId)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    appearance = Server.rand.nextInt(5);
    
    this.faceRandom.setSeed(8263186381637L + appearance);
    appearance = this.faceRandom.nextLong();
    return createPlayerPhaseTwo(intraServerPassword, playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, serverId, true);
  }
  
  public Map<String, ?> createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, String phaseOneHash, int serverId, boolean optInEmail)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    serverId = 1;
    appearance = Server.rand.nextInt(5);
    
    this.faceRandom.setSeed(8263186381637L + appearance);
    appearance = this.faceRandom.nextLong();
    
    kingdom = 4;
    if (kingdom == 3) {
      serverId = 3;
    }
    Map<String, Object> toReturn = new HashMap();
    if (Constants.maintaining)
    {
      toReturn.put("error", "The server is currently in maintenance mode.");
      return toReturn;
    }
    logger.log(Level.INFO, getRemoteClientDetails() + " Trying to create player phase two " + playerName);
    synchronized (Server.SYNC_LOCK)
    {
      if ((playerName == null) || (hashedIngamePassword == null) || (challengePhrase == null) || (challengeAnswer == null) || (emailAddress == null) || (phaseOneHash == null))
      {
        if (playerName == null) {
          toReturn.put("error", "PlayerName is null.");
        }
        if (hashedIngamePassword == null) {
          toReturn.put("error", "hashedIngamePassword is null.");
        }
        if (challengePhrase == null) {
          toReturn.put("error", "ChallengePhrase is null.");
        }
        if (challengeAnswer == null) {
          toReturn.put("error", "ChallengeAnswer is null.");
        }
        if (emailAddress == null) {
          toReturn.put("error", "EmailAddress is null.");
        }
        if (phaseOneHash == null) {
          toReturn.put("error", "phaseOneHash is null.");
        }
        return toReturn;
      }
      if (challengePhrase.equals(challengeAnswer))
      {
        toReturn.put("error", "We don't allow the password retrieval question and answer to be the same.");
        return toReturn;
      }
      playerName = LoginHandler.raiseFirstLetter(playerName);
      
      String errstat = LoginHandler.checkName2(playerName);
      if (errstat.length() > 0)
      {
        toReturn.put("error", errstat);
        return toReturn;
      }
      if (PlayerInfoFactory.doesPlayerExist(playerName))
      {
        toReturn.put("error", "The name " + playerName + " is taken. Your reservation must have expired.");
        return toReturn;
      }
      if ((hashedIngamePassword.length() < 6) || (hashedIngamePassword.length() > 40))
      {
        toReturn.put("error", "The hashed password must contain at least 6 characters and maximum 40 characters.");
        return toReturn;
      }
      if ((challengePhrase.length() < 4) || (challengePhrase.length() > 120))
      {
        toReturn.put("error", "The challenge phrase must contain at least 4 characters and max 120 characters.");
        return toReturn;
      }
      if ((challengeAnswer.length() < 1) || (challengeAnswer.length() > 20))
      {
        toReturn.put("error", "The challenge answer must contain at least 1 character and max 20 characters.");
        return toReturn;
      }
      if (emailAddress.length() > 125)
      {
        toReturn.put("error", "The email address consists of too many characters.");
        return toReturn;
      }
      if (isEmailValid(emailAddress)) {
        try
        {
          PendingAccount pacc = PendingAccount.getAccount(playerName);
          if (pacc == null)
          {
            toReturn.put("PlayerId", Long.valueOf(-1L));
            toReturn.put("error", "The verification is done too late or the name was never reserved. The name reservation expires after two days. Please try to create the player again.");
            
            return toReturn;
          }
          if (pacc.password.equals(phaseOneHash))
          {
            if (pacc.emailAddress.toLowerCase().equals(emailAddress.toLowerCase()))
            {
              try
              {
                if (serverId == Servers.localServer.id)
                {
                  toReturn.put("PlayerId", new Long(
                  
                    LoginHandler.createPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender)));
                }
                else if (Servers.localServer.LOGINSERVER)
                {
                  ServerEntry toCreateOn = Servers.getServerWithId(serverId);
                  if (toCreateOn != null)
                  {
                    int tilex = toCreateOn.SPAWNPOINTJENNX;
                    int tiley = toCreateOn.SPAWNPOINTJENNY;
                    if (kingdom == 2)
                    {
                      tilex = toCreateOn.SPAWNPOINTMOLX;
                      tiley = toCreateOn.SPAWNPOINTMOLY;
                    }
                    if (kingdom == 3)
                    {
                      tilex = toCreateOn.SPAWNPOINTLIBX;
                      tiley = toCreateOn.SPAWNPOINTLIBY;
                    }
                    if (serverId == 5)
                    {
                      tilex = 2884;
                      tiley = 3004;
                    }
                    LoginServerWebConnection lsw = new LoginServerWebConnection(serverId);
                    byte[] playerData = lsw.createAndReturnPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, false, false, false);
                    
                    long wurmId = IntraServerConnection.savePlayerToDisk(playerData, tilex, tiley, true, true);
                    
                    toReturn.put("PlayerId", Long.valueOf(wurmId));
                  }
                  else
                  {
                    toReturn.put("PlayerId", Long.valueOf(-1L));
                    toReturn.put("error", "Failed to create player " + playerName + ": The desired server does not exist.");
                  }
                }
                else
                {
                  toReturn.put("PlayerId", Long.valueOf(-1L));
                  toReturn.put("error", "Failed to create player " + playerName + ": This is not a login server.");
                }
              }
              catch (Exception cex)
              {
                logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + cex.getMessage(), cex);
                toReturn.put("PlayerId", Long.valueOf(-1L));
                toReturn.put("error", "Failed to create player " + playerName + ":" + cex.getMessage());
                return toReturn;
              }
            }
            else
            {
              toReturn.put("PlayerId", Long.valueOf(-1L));
              toReturn.put("error", "The email supplied does not match with the one that was registered with the name.");
              
              return toReturn;
            }
            pacc.delete();
            try
            {
              if (!Constants.devmode)
              {
                String mail = Mailer.getPhaseTwoMail();
                mail = mail.replace("@pname", playerName);
                
                Mailer.sendMail(mailAccount, emailAddress, "Wurm Online character creation success", mail);
              }
            }
            catch (Exception cex2)
            {
              logger.log(Level.WARNING, "Failed to send email to " + emailAddress + " for player " + playerName + ":" + cex2
                .getMessage(), cex2);
              
              toReturn.put("error", "Failed to send email to " + emailAddress + " for player " + playerName + ":" + cex2
                .getMessage());
            }
          }
          else
          {
            toReturn.put("PlayerId", Long.valueOf(-1L));
            toReturn.put("error", "The verification hash does not match.");
          }
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + ex.getMessage(), ex);
          toReturn.put("PlayerId", Long.valueOf(-1L));
          toReturn.put("error", ex.getMessage());
        }
      }
      toReturn.put("error", "The email address " + emailAddress + " is not valid.");
    }
    return toReturn;
  }
  
  public byte[] createAndReturnPlayer(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (Constants.maintaining) {
      throw new RemoteException("The server is currently in maintenance mode.");
    }
    try
    {
      appearance = Server.rand.nextInt(5);
      
      this.faceRandom.setSeed(8263186381637L + appearance);
      appearance = this.faceRandom.nextLong();
      logger.log(Level.INFO, getClientHost() + " Received create attempt for " + playerName);
      return LoginHandler.createAndReturnPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, titleKeeper, addPremium, passwordIsHashed);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
      throw new RemoteException(ex.getMessage());
    }
  }
  
  public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    byte executor = 6;
    
    boolean ok = true;
    String campaignId = "";
    name = LoginHandler.raiseFirstLetter(name);
    Map<String, String> toReturn = new HashMap();
    if ((name == null) || (name.length() == 0))
    {
      toReturn.put("error", "Illegal name.");
      return toReturn;
    }
    if (moneyToAdd <= 0L)
    {
      toReturn.put("error", "Invalid amount; must be greater than zero");
      return toReturn;
    }
    synchronized (Server.SYNC_LOCK)
    {
      try
      {
        Player p = Players.getInstance().getPlayer(name);
        p.addMoney(moneyToAdd);
        long money = p.getMoney();
        new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, executor, campaignId);
        Change change = new Change(moneyToAdd);
        Change current = new Change(money);
        p.save();
        toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current
          .getChangeString() + ".");
      }
      catch (NoSuchPlayerException nsp)
      {
        try
        {
          PlayerInfo p = PlayerInfoFactory.createPlayerInfo(name);
          p.load();
          if (p.wurmId > 0L)
          {
            p.setMoney(p.money + moneyToAdd);
            Change change = new Change(moneyToAdd);
            Change current = new Change(p.money);
            p.save();
            toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current
              .getChangeString() + ". It may take a while to reach your server.");
            if (Servers.localServer.id != p.currentServer) {
              new MoneyTransfer(name, p.wurmId, p.money, moneyToAdd, transactionDetail, executor, campaignId, false);
            } else {
              new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, executor, campaignId);
            }
          }
          else
          {
            toReturn.put("error", "No player found with the name " + name + ".");
          }
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
          throw new RemoteException("An error occured. Please contact customer support.");
        }
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
        throw new RemoteException("An error occured. Please contact customer support.");
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, name + ":" + ex.getMessage(), ex);
        throw new RemoteException("An error occured. Please contact customer support.");
      }
    }
    return toReturn;
  }
  
  public long getMoney(String intraServerPassword, long playerId, String playerName)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
    if (p == null)
    {
      p = PlayerInfoFactory.createPlayerInfo(playerName);
      try
      {
        p.load();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to load pinfo for " + playerName);
      }
      if (p.wurmId <= 0L) {
        return 0L;
      }
    }
    if (p != null) {
      return p.money;
    }
    return 0L;
  }
  
  public Map<String, String> reversePayment(String intraServerPassword, long moneyToRemove, int monthsToRemove, int daysToRemove, String reversalTransactionID, String originalTransactionID, String playerName)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Map<String, String> toReturn = new HashMap();
    logger.log(Level.INFO, getRemoteClientDetails() + " Reverse payment for player name: " + playerName + ", reversalTransactionID: " + reversalTransactionID + ", originalTransactionID: " + originalTransactionID);
    try
    {
      PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
      p.load();
      if (p.wurmId > 0L)
      {
        if (moneyToRemove > 0L)
        {
          if (p.money < moneyToRemove)
          {
            Change lack = new Change(moneyToRemove - p.money);
            toReturn.put("moneylack", "An amount of " + lack.getChangeString() + " was lacking from the account. Removing what we can.");
          }
          p.setMoney(Math.max(0L, p.money - moneyToRemove));
          Change change = new Change(moneyToRemove);
          Change current = new Change(p.money);
          p.save();
          toReturn.put("moneyok", "An amount of " + change.getChangeString() + " has been removed from the account. Current balance is " + current
            .getChangeString() + ".");
          if (Servers.localServer.id != p.currentServer) {
            new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, (byte)4, "", false);
          } else {
            new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, (byte)4, "");
          }
        }
        if ((daysToRemove > 0) || (monthsToRemove > 0))
        {
          long timeToRemove = 0L;
          if (daysToRemove > 0) {
            timeToRemove = daysToRemove * 86400000L;
          }
          if (monthsToRemove > 0) {
            timeToRemove += monthsToRemove * 86400000L * 30L;
          }
          long currTime = p.getPaymentExpire();
          
          currTime = Math.max(currTime, System.currentTimeMillis());
          currTime = Math.max(currTime - timeToRemove, System.currentTimeMillis());
          try
          {
            p.setPaymentExpire(currTime);
            String expireString = "The premier playing time has expired now.";
            if (System.currentTimeMillis() < currTime) {
              expireString = "The player now has premier playing time until " + GeneralUtilities.toGMTString(currTime) + ". Your in game player account will be updated shortly.";
            }
            p.save();
            toReturn.put("timeok", expireString);
            if (p.currentServer != Servers.localServer.id) {
              new TimeTransfer(playerName, p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID, false);
            } else {
              new TimeTransfer(p.getName(), p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID);
            }
          }
          catch (IOException iox)
          {
            toReturn.put("timeerror", p
              .getName() + ": failed to set expire to " + currTime + ", " + iox.getMessage());
            logger.log(Level.WARNING, p
              .getName() + ": failed to set expire to " + currTime + ", " + iox.getMessage(), iox);
          }
        }
      }
      else
      {
        toReturn.put("error", "No player found with the name " + playerName + ".");
      }
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, playerName + ":" + iox.getMessage(), iox);
      throw new RemoteException("An error occured. Please contact customer support.");
    }
    return toReturn;
  }
  
  public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail, boolean ingame)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " addMoneyToBank for player name: " + name);
    }
    return addMoneyToBank(intraServerPassword, name, -1L, moneyToAdd, transactionDetail, ingame);
  }
  
  public static String encryptMD5(String plaintext)
    throws Exception
  {
    MessageDigest md = null;
    try
    {
      md = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new WurmServerException("No such algorithm 'MD5'", e);
    }
    try
    {
      md.update(plaintext.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new WurmServerException("No such encoding: UTF-8", e);
    }
    byte[] raw = md.digest();
    BigInteger bi = new BigInteger(1, raw);
    String hash = bi.toString(16);
    return hash;
  }
  
  public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long wurmId, long moneyToAdd, String transactionDetail, boolean ingame)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    synchronized (Server.SYNC_LOCK)
    {
      Map<String, String> toReturn = new HashMap();
      if (((name == null) || (name.length() == 0)) && (wurmId <= 0L))
      {
        toReturn.put("error", "Illegal name.");
        return toReturn;
      }
      if (moneyToAdd <= 0L)
      {
        toReturn.put("error", "Invalid amount; must be greater than zero");
        return toReturn;
      }
      if (name != null) {
        name = LoginHandler.raiseFirstLetter(name);
      }
      byte executor = 6;
      String campaignId = "";
      
      logger.log(Level.INFO, getRemoteClientDetails() + " Add money to bank 2 , " + moneyToAdd + " for player name: " + name + ", wid " + wurmId);
      if (((name != null) && (name.length() > 0)) || (wurmId > 0L)) {
        try
        {
          Player p = null;
          if (wurmId <= 0L) {
            p = Players.getInstance().getPlayer(name);
          } else {
            p = Players.getInstance().getPlayer(wurmId);
          }
          p.addMoney(moneyToAdd);
          long money = p.getMoney();
          if (!ingame) {
            new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, (byte)6, "");
          }
          Change change = new Change(moneyToAdd);
          Change current = new Change(money);
          p.save();
          toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current
            .getChangeString() + ".");
        }
        catch (NoSuchPlayerException nsp)
        {
          try
          {
            PlayerInfo p = null;
            if ((name != null) && (name.length() > 0)) {
              p = PlayerInfoFactory.createPlayerInfo(name);
            } else {
              p = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
            }
            if (p != null)
            {
              p.load();
              if (p.wurmId > 0L)
              {
                p.setMoney(p.money + moneyToAdd);
                Change change = new Change(moneyToAdd);
                Change current = new Change(p.money);
                p.save();
                toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current
                  .getChangeString() + ". It may take a while to reach your server.");
                if (!ingame) {
                  if (Servers.localServer.id != p.currentServer) {
                    new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, (byte)6, "", false);
                  } else {
                    new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, (byte)6, "");
                  }
                }
              }
              else
              {
                toReturn.put("error", "No player found with the wurmid " + p.wurmId + ".");
              }
            }
            else
            {
              toReturn.put("error", "No player found with the name " + name + ".");
            }
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, name + ": " + wurmId + "," + iox.getMessage(), iox);
            throw new RemoteException("An error occured. Please contact customer support.");
          }
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, name + ":" + wurmId + "," + iox.getMessage(), iox);
          throw new RemoteException("An error occured. Please contact customer support.");
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, name + ":" + wurmId + "," + ex.getMessage(), ex);
          throw new RemoteException("An error occured. Please contact customer support.");
        }
      }
      return toReturn;
    }
  }
  
  public long chargeMoney(String intraServerPassword, String playerName, long moneyToCharge)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    logger.log(Level.INFO, getRemoteClientDetails() + " ChargeMoney for player name: " + playerName + ", money: " + moneyToCharge);
    if (Servers.localServer.id == Servers.loginServer.id)
    {
      PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
      try
      {
        p.load();
        if (p.money > 0L)
        {
          if (p.money - moneyToCharge < 0L) {
            return -10L;
          }
          p.setMoney(p.money - moneyToCharge);
          logger.info(playerName + " was charged " + moneyToCharge + " and now has " + p.money);
          
          return p.money;
        }
        return -10L;
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, playerName + ": " + iox.getMessage(), iox);
        return -10L;
      }
    }
    logger.warning(playerName + " cannot charge " + moneyToCharge + " as this server is not the login server");
    return -10L;
  }
  
  public Map<String, String> addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    return addPlayingTime(intraServerPassword, name, months, days, transactionDetail, true);
  }
  
  public Map<String, String> addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail, boolean addSleepPowder)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    synchronized (Server.SYNC_LOCK)
    {
      Map<String, String> toReturn = new HashMap();
      if ((name == null) || (name.length() == 0) || (transactionDetail == null) || (transactionDetail.length() == 0))
      {
        toReturn.put("error", "Illegal arguments. Check if name or transaction detail is null or empty strings.");
        return toReturn;
      }
      if ((months < 0) || (days < 0))
      {
        toReturn.put("error", "Illegal arguments. Make sure that the values for days and months are not negative.");
        return toReturn;
      }
      boolean ok = true;
      
      logger.log(Level.INFO, getRemoteClientDetails() + " Addplayingtime for player name: " + name + ", months: " + months + ", days: " + days + ", transactionDetail: " + transactionDetail);
      
      SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
      synchronized (Server.SYNC_LOCK)
      {
        long timeToAdd = 0L;
        if (days != 0) {
          timeToAdd = days * 86400000L;
        }
        if (months != 0) {
          timeToAdd += months * 86400000L * 30L;
        }
        try
        {
          Player p = Players.getInstance().getPlayer(name);
          long currTime = p.getPaymentExpire();
          if (timeToAdd > 0L) {
            if (currTime <= 0L) {
              Server.addNewPlayer(p.getName());
            } else {
              Server.incrementOldPremiums(p.getName());
            }
          }
          currTime = Math.max(currTime, System.currentTimeMillis());
          currTime += timeToAdd;
          try
          {
            p.getSaveFile().setPaymentExpire(currTime, !transactionDetail.startsWith("firstBuy"));
            new TimeTransfer(p.getName(), p.getWurmId(), months, addSleepPowder, days, transactionDetail);
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, p
              .getName() + ": failed to set expire to " + currTime + ", " + iox.getMessage(), iox);
          }
          String expireString = "You now have premier playing time until " + formatter.format(new Date(currTime)) + ".";
          
          p.save();
          toReturn.put("ok", expireString);
          Message mess = new Message(null, (byte)3, ":Event", expireString);
          
          mess.setReceiver(p.getWurmId());
          Server.getInstance().addMessage(mess);
          logger.info(p.getName() + ' ' + expireString);
          if (addSleepPowder) {
            try
            {
              Item inventory = p.getInventory();
              for (int x = 0; x < months; x++)
              {
                Item i = ItemFactory.createItem(666, 99.0F, "");
                inventory.insertItem(i, true);
              }
              logger.log(Level.INFO, "Inserted " + months + " sleep powder in " + p.getName() + " inventory " + inventory
                .getWurmId());
              Message rmess = new Message(null, (byte)3, ":Event", "You have received " + months + " sleeping powders in your inventory.");
              
              rmess.setReceiver(p.getWurmId());
              Server.getInstance().addMessage(rmess);
            }
            catch (Exception ex)
            {
              logger.log(Level.INFO, ex.getMessage(), ex);
            }
          }
          return toReturn;
        }
        catch (NoSuchPlayerException nsp)
        {
          try
          {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(name);
            p.load();
            if (p.wurmId > 0L)
            {
              long currTime = p.getPaymentExpire();
              if (timeToAdd > 0L) {
                if (currTime <= 0L) {
                  Server.addNewPlayer(p.getName());
                } else {
                  Server.incrementOldPremiums(p.getName());
                }
              }
              currTime = Math.max(currTime, System.currentTimeMillis());
              currTime += timeToAdd;
              try
              {
                p.setPaymentExpire(currTime, !transactionDetail.startsWith("firstBuy"));
              }
              catch (IOException iox)
              {
                logger.log(Level.WARNING, p.getName() + ": failed to set expire to " + currTime + ", " + iox
                  .getMessage(), iox);
              }
              ServerEntry entry = Servers.getServerWithId(p.currentServer);
              String expireString = "Your premier playing time has expired now.";
              if (System.currentTimeMillis() < currTime) {
                if (entry.entryServer) {
                  expireString = "You now have premier playing time until " + formatter.format(new Date(currTime)) + ". Your in game player account will be updated shortly. NOTE that you will have to use a portal to get to the premium servers in order to benefit from it.";
                } else {
                  expireString = "You now have premier playing time until " + formatter.format(new Date(currTime)) + ". Your in game player account will be updated shortly.";
                }
              }
              p.save();
              toReturn.put("ok", expireString);
              logger.info(p.getName() + ' ' + expireString);
              if (p.currentServer != Servers.localServer.id)
              {
                new TimeTransfer(name, p.wurmId, months, addSleepPowder, days, transactionDetail, false);
              }
              else
              {
                new TimeTransfer(p.getName(), p.wurmId, months, addSleepPowder, days, transactionDetail);
                if (addSleepPowder) {
                  try
                  {
                    long inventoryId = DbCreatureStatus.getInventoryIdFor(p.wurmId);
                    for (int x = 0; x < months; x++)
                    {
                      Item i = ItemFactory.createItem(666, 99.0F, "");
                      i.setParentId(inventoryId, true);
                      i.setOwnerId(p.wurmId);
                    }
                    logger.log(Level.INFO, "Inserted " + months + " sleep powder in offline " + p
                      .getName() + " inventory " + inventoryId);
                  }
                  catch (Exception ex)
                  {
                    logger.log(Level.INFO, ex.getMessage(), ex);
                  }
                }
              }
              return toReturn;
            }
            toReturn.put("error", "No player found with the name " + name + ".");
            return toReturn;
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
            throw new RemoteException("An error occured. Please contact customer support.");
          }
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
          throw new RemoteException("An error occured. Please contact customer support.");
        }
        catch (Exception ex)
        {
          logger.log(Level.WARNING, name + ":" + ex.getMessage(), ex);
          throw new RemoteException("An error occured. Please contact customer support.");
        }
      }
    }
  }
  
  public Map<Integer, String> getDeeds(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getDeeds");
    }
    Map<Integer, String> toReturn = new HashMap();
    Village[] vills = Villages.getVillages();
    for (int x = 0; x < vills.length; x++) {
      toReturn.put(Integer.valueOf(vills[x].id), vills[x].getName());
    }
    return toReturn;
  }
  
  public Map<String, ?> getDeedSummary(String intraServerPassword, int aVillageID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getDeedSummary for villageID: " + aVillageID);
    }
    try
    {
      Village village = Villages.getVillage(aVillageID);
      Map<String, Object> toReturn = new HashMap();
      toReturn.put("Villageid", Integer.valueOf(village.getId()));
      toReturn.put("Deedid", Long.valueOf(village.getDeedId()));
      toReturn.put("Name", village.getName());
      toReturn.put("Motto", village.getMotto());
      toReturn.put("Location", Kingdoms.getNameFor(village.kingdom));
      toReturn.put("Size", Integer.valueOf((village.getEndX() - village.getStartX()) / 2));
      toReturn.put("Founder", village.getFounderName());
      toReturn.put("Mayor", village.mayorName);
      if (village.disband > 0L)
      {
        toReturn.put("Disbanding in", Server.getTimeFor(village.disband - System.currentTimeMillis()));
        toReturn.put("Disbander", Players.getInstance().getNameFor(village.disbander));
      }
      toReturn.put("Citizens", Integer.valueOf(village.citizens.size()));
      toReturn.put("Allies", Integer.valueOf(village.getAllies().length));
      if (village.guards != null) {
        toReturn.put("guards", Integer.valueOf(village.guards.size()));
      }
      try
      {
        short[] sp = village.getTokenCoords();
        toReturn.put("Token Coord x", Integer.valueOf(sp[0]));
        toReturn.put("Token Coord y", Integer.valueOf(sp[1]));
      }
      catch (NoSuchItemException localNoSuchItemException) {}
      return toReturn;
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
      throw new RemoteException(ex.getMessage());
    }
  }
  
  public Map<String, Long> getPlayersForDeed(String intraServerPassword, int aVillageID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPlayersForDeed for villageID: " + aVillageID);
    }
    Map<String, Long> toReturn = new HashMap();
    try
    {
      Village village = Villages.getVillage(aVillageID);
      Citizen[] citizens = village.getCitizens();
      for (int x = 0; x < citizens.length; x++) {
        if (WurmId.getType(citizens[x].getId()) == 0) {
          try
          {
            toReturn.put(Players.getInstance().getNameFor(citizens[x].getId()), new Long(citizens[x].getId()));
          }
          catch (NoSuchPlayerException localNoSuchPlayerException) {}
        }
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
      throw new RemoteException(ex.getMessage());
    }
    return toReturn;
  }
  
  public Map<String, Integer> getAlliesForDeed(String intraServerPassword, int aVillageID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getAlliesForDeed for villageID: " + aVillageID);
    }
    Map<String, Integer> toReturn = new HashMap();
    try
    {
      Village village = Villages.getVillage(aVillageID);
      Village[] allies = village.getAllies();
      for (int x = 0; x < allies.length; x++) {
        toReturn.put(allies[x].getName(), Integer.valueOf(allies[x].getId()));
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
      throw new RemoteException(ex.getMessage());
    }
    return toReturn;
  }
  
  public String[] getHistoryForDeed(String intraServerPassword, int villageID, int maxLength)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getHistoryForDeed for villageID: " + villageID + ", maxLength: " + maxLength);
    }
    try
    {
      Village village = Villages.getVillage(villageID);
      return village.getHistoryAsStrings(maxLength);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
      throw new RemoteException(ex.getMessage());
    }
  }
  
  public String[] getAreaHistory(String intraServerPassword, int maxLength)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getAreaHistory maxLength: " + maxLength);
    }
    return HistoryManager.getHistory(maxLength);
  }
  
  public Map<String, ?> getItemSummary(String intraServerPassword, long aWurmID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getItemSummary for WurmId: " + aWurmID);
    }
    Map<String, Object> toReturn = new HashMap();
    try
    {
      Item item = Items.getItem(aWurmID);
      toReturn.put("WurmId", new Long(aWurmID));
      toReturn.put("Name", item.getName());
      toReturn.put("QL", String.valueOf(item.getQualityLevel()));
      toReturn.put("DMG", String.valueOf(item.getDamage()));
      toReturn.put("SizeX", String.valueOf(item.getSizeX()));
      toReturn.put("SizeY", String.valueOf(item.getSizeY()));
      toReturn.put("SizeZ", String.valueOf(item.getSizeZ()));
      if (item.getOwnerId() != -10L) {
        toReturn.put("Owner", new Long(item.getOwnerId()));
      } else {
        toReturn.put("Last owner", new Long(item.lastOwner));
      }
      toReturn.put("Coord x", Integer.valueOf((int)item.getPosX() >> 2));
      toReturn.put("Coord y", Integer.valueOf((int)item.getPosY() >> 2));
      toReturn.put("Creator", item.creator);
      toReturn.put("Creationdate", WurmCalendar.getTimeFor(item.creationDate));
      
      toReturn.put("Description", item.getDescription());
      toReturn.put("Material", Item.getMaterialString(item.getMaterial()));
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
      throw new RemoteException(ex.getMessage());
    }
    return toReturn;
  }
  
  public Map<String, String> getPlayerIPAddresses(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPlayerIPAddresses");
    }
    Map<String, String> toReturn = new HashMap();
    Player[] playerArr = Players.getInstance().getPlayersByIp();
    for (int x = 0; x < playerArr.length; x++) {
      if (playerArr[x].getSaveFile().getPower() == 0) {
        toReturn.put(playerArr[x].getName(), playerArr[x].getSaveFile().getIpaddress());
      }
    }
    return toReturn;
  }
  
  public Map<String, String> getNameBans(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getNameBans");
    }
    Map<String, String> toReturn = new HashMap();
    Ban[] bips = Players.getInstance().getPlayersBanned();
    if (bips.length > 0) {
      for (int x = 0; x < bips.length; x++)
      {
        long daytime = bips[x].getExpiry() - System.currentTimeMillis();
        toReturn.put(bips[x].getIdentifier(), Server.getTimeFor(daytime) + ", " + bips[x].getReason());
      }
    }
    return toReturn;
  }
  
  public Map<String, String> getIPBans(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getIPBans");
    }
    Map<String, String> toReturn = new HashMap();
    Ban[] bips = Players.getInstance().getBans();
    if (bips.length > 0) {
      for (int x = 0; x < bips.length; x++)
      {
        long daytime = bips[x].getExpiry() - System.currentTimeMillis();
        toReturn.put(bips[x].getIdentifier(), Server.getTimeFor(daytime) + ", " + bips[x].getReason());
      }
    }
    return toReturn;
  }
  
  public Map<String, String> getWarnings(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getWarnings");
    }
    Map<String, String> toReturn = new HashMap();
    toReturn.put("Not implemented", "Need a name to check.");
    return toReturn;
  }
  
  public String getWurmTime(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getWurmTime");
    }
    return WurmCalendar.getTime();
  }
  
  public String getUptime(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getUptime");
    }
    return Server.getTimeFor(System.currentTimeMillis() - Server.getStartTime());
  }
  
  public String getNews(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getNews");
    }
    return NewsInfo.getInfo();
  }
  
  public String getGameInfo(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getGameInfo");
    }
    return WurmInfo.getInfo() + WurmInfo2.getInfo();
  }
  
  public Map<String, String> getKingdomInfluence(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getKingdomInfluence");
    }
    Map<String, String> toReturn = new HashMap();
    Zones.calculateZones(false);
    Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
    for (int x = 0; x < kingdoms.length; x++) {
      toReturn.put("Percent controlled by " + kingdoms[x].getName(), twoDecimals
        .format(Zones.getPercentLandForKingdom(kingdoms[x].getId())));
    }
    return toReturn;
  }
  
  public Map<String, ?> getMerchantSummary(String intraServerPassword, long aWurmID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getMerchantSummary for WurmID: " + aWurmID);
    }
    Map<String, Object> toReturn = new HashMap();
    toReturn.put("Not implemented", "not yet");
    return toReturn;
  }
  
  public Map<String, ?> getBankAccount(String intraServerPassword, long aPlayerID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getBankAccount for playerid: " + aPlayerID);
    }
    Map<String, Object> toReturn = new HashMap();
    logger.log(Level.INFO, "GetBankAccount " + aPlayerID);
    try
    {
      Bank lBank = Banks.getBank(aPlayerID);
      if (lBank != null)
      {
        toReturn.put("BankID", Long.valueOf(lBank.id));
        toReturn.put("Owner", Long.valueOf(lBank.owner));
        toReturn.put("StartedMoving", Long.valueOf(lBank.startedMoving));
        toReturn.put("Open", Boolean.valueOf(lBank.open));
        toReturn.put("Size", Integer.valueOf(lBank.size));
        try
        {
          Village lCurrentVillage = lBank.getCurrentVillage();
          if (lCurrentVillage != null)
          {
            toReturn.put("CurrentVillageID", Integer.valueOf(lCurrentVillage.getId()));
            toReturn.put("CurrentVillageName", lCurrentVillage.getName());
          }
        }
        catch (BankUnavailableException localBankUnavailableException) {}
        int lTargetVillageID = lBank.targetVillage;
        if (lTargetVillageID > 0) {
          toReturn.put("TargetVillageID", Integer.valueOf(lTargetVillageID));
        }
        BankSlot[] lSlots = lBank.slots;
        if ((lSlots != null) && (lSlots.length > 0))
        {
          Map<Long, String> lItemsMap = new HashMap(lSlots.length + 1);
          for (int i = 0; i < lSlots.length; i++) {
            if (lSlots[i] == null)
            {
              logger.log(Level.INFO, "Weird. Bank Slot " + i + " is null for " + aPlayerID);
            }
            else
            {
              Item lItem = lSlots[i].item;
              if (lItem != null) {
                lItemsMap.put(Long.valueOf(lItem.getWurmId()), lItem.getName() + ", Inserted: " + lSlots[i].inserted + ", Stasis: " + lSlots[i].stasis);
              }
            }
          }
          if ((lItemsMap != null) && (lItemsMap.size() > 0)) {
            toReturn.put("Items", lItemsMap);
          }
        }
      }
      else
      {
        toReturn.put("Error", "Cannot find bank for player ID " + aPlayerID);
      }
    }
    catch (RuntimeException e)
    {
      logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
      
      toReturn.put("Error", "Problem getting bank account for player ID " + aPlayerID + ", " + e);
    }
    return toReturn;
  }
  
  public Map<String, ?> authenticateUser(String intraServerPassword, String playerName, String emailAddress, String hashedIngamePassword, Map params)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
    }
    Map<String, Object> toReturn = new HashMap();
    if (Constants.maintaining)
    {
      toReturn.put("ResponseCode0", "NOTOK");
      toReturn.put("ErrorMessage0", "The server is currently unavailable.");
      toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
      return toReturn;
    }
    try
    {
      boolean ver = false;
      Object answer = params.get("VerifiedPayPalAccount");
      if ((answer != null) && ((answer instanceof Boolean))) {
        ver = ((Boolean)answer).booleanValue();
      }
      boolean rev = false;
      answer = params.get("ChargebackOrReversal");
      if ((answer != null) && ((answer instanceof Boolean))) {
        rev = ((Boolean)answer).booleanValue();
      }
      Date lastReversal = (Date)params.get("LastChargebackOrReversal");
      
      Date first = (Date)params.get("FirstTransactionDate");
      Date last = (Date)params.get("LastTransactionDate");
      int total = 0;
      answer = params.get("TotalEurosSuccessful");
      if ((answer != null) && ((answer instanceof Integer)))
      {
        total = ((Integer)answer).intValue();
        if (total < 0) {
          total = 0;
        }
      }
      int lastMonthEuros = 0;
      answer = params.get("LastMonthEurosSuccessful");
      if ((answer != null) && ((answer instanceof Integer)))
      {
        lastMonthEuros = ((Integer)answer).intValue();
        if (lastMonthEuros < 0) {
          lastMonthEuros = 0;
        }
      }
      String ipAddress = (String)params.get("IP");
      if (ipAddress != null)
      {
        logger.log(Level.INFO, "IP:" + ipAddress);
        Long lastAttempt = (Long)ipAttempts.get(ipAddress);
        if (lastAttempt != null) {
          if (System.currentTimeMillis() - lastAttempt.longValue() < 5000L)
          {
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "Too many logon attempts. Please try again in a few seconds.");
            toReturn.put("display_text0", "Too many logon attempts. Please try again in a few seconds.");
            return toReturn;
          }
        }
        ipAttempts.put(ipAddress, Long.valueOf(System.currentTimeMillis()));
      }
      PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
      if (file.undeadType != 0)
      {
        toReturn.put("ResponseCode0", "NOTOK");
        toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
        toReturn.put("display_text0", "Undeads not allowed in here!");
        return toReturn;
      }
      try
      {
        file.load();
        if (file.undeadType != 0)
        {
          toReturn.put("ResponseCode0", "NOTOK");
          toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
          toReturn.put("display_text0", "Undeads not allowed in here!");
          return toReturn;
        }
      }
      catch (IOException iox)
      {
        toReturn.put("ResponseCode0", "NOTOK");
        toReturn.put("ErrorMessage0", "An error occurred when loading your account.");
        toReturn.put("display_text0", "An error occurred when loading your account.");
        logger.log(Level.WARNING, iox.getMessage(), iox);
        return toReturn;
      }
      if ((!file.overRideShop) && (rev)) {
        if ((lastReversal == null) || (last == null) || (lastReversal.after(last)))
        {
          toReturn.put("ResponseCode0", "NOTOK");
          toReturn.put("ErrorMessage0", "This paypal account has reversed transactions registered.");
          toReturn.put("display_text0", "This paypal account has reversed transactions registered.");
          return toReturn;
        }
      }
      toReturn = authenticateUser(intraServerPassword, playerName, emailAddress, hashedIngamePassword);
      Integer max = (Integer)toReturn.get("maximum_silver0");
      if (max != null)
      {
        int maxval = max.intValue();
        if (file.overRideShop)
        {
          maxval = 50 + Math.min(50, (int)(file.playingTime / 3600000L * 3L));
          toReturn.put("maximum_silver0", Integer.valueOf(maxval));
        }
        else if (lastMonthEuros >= 400)
        {
          maxval = 0;
          toReturn.put("maximum_silver0", Integer.valueOf(maxval));
          toReturn.put("display_text0", "You may only purchase 400 silver via PayPal per month");
        }
      }
      return toReturn;
    }
    catch (Exception ew)
    {
      logger.log(Level.WARNING, "Error: " + ew.getMessage(), ew);
      toReturn.put("ResponseCode0", "NOTOK");
      toReturn.put("ErrorMessage0", "An error occured.");
    }
    return toReturn;
  }
  
  public Map<String, String> doesPlayerExist(String intraServerPassword, String playerName)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " doesPlayerExist for player name: " + playerName);
    }
    Map<String, String> toReturn = new HashMap();
    if (Constants.maintaining)
    {
      toReturn.put("ResponseCode", "NOTOK");
      toReturn.put("ErrorMessage", "The server is currently unavailable.");
      toReturn.put("display_text", "The server is currently unavailable.");
      return toReturn;
    }
    toReturn.put("ResponseCode", "OK");
    if (playerName != null)
    {
      PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
      try
      {
        file.load();
        if (file.wurmId <= 0L)
        {
          toReturn.clear();
          toReturn.put("ResponseCode", "NOTOK");
          toReturn.put("ErrorMessage", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity.");
          
          toReturn.put("display_text", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity.");
        }
      }
      catch (Exception ex)
      {
        toReturn.clear();
        toReturn.put("ResponseCode", "NOTOK");
        toReturn.put("ErrorMessage", ex.getMessage());
        toReturn.put("display_text", "An error occurred on the " + Servers.localServer.name + " game server: " + ex
          .getMessage());
      }
    }
    return toReturn;
  }
  
  public Map<String, ?> authenticateUser(String intraServerPassword, String playerName, String emailAddress, String hashedIngamePassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
    }
    Map<String, Object> toReturn = new HashMap();
    if (Constants.maintaining)
    {
      toReturn.put("ResponseCode0", "NOTOK");
      toReturn.put("ErrorMessage0", "The server is currently unavailable.");
      toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
      return toReturn;
    }
    if (playerName != null)
    {
      PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
      if (file.undeadType != 0)
      {
        toReturn.put("ResponseCode0", "NOTOK");
        toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
        toReturn.put("display_text0", "Undeads not allowed in here!");
        return toReturn;
      }
      try
      {
        file.load();
        if (file.undeadType != 0)
        {
          toReturn.put("ResponseCode0", "NOTOK");
          toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
          toReturn.put("display_text0", "Undeads not allowed in here!");
          return toReturn;
        }
        if (file.wurmId <= 0L)
        {
          toReturn.put("ResponseCode0", "NOTOK");
          toReturn.put("ErrorMessage0", "No such player.");
        }
        else if (hashedIngamePassword.equals(file.getPassword()))
        {
          if (Servers.isThisLoginServer())
          {
            LoginServerWebConnection lsw = new LoginServerWebConnection(file.currentServer);
            Map<String, String> m = lsw.doesPlayerExist(playerName);
            String resp = (String)m.get("ResponseCode");
            if ((resp != null) && (resp.equals("NOTOK")))
            {
              toReturn.put("ResponseCode0", "NOTOK");
              toReturn.put("ErrorMessage0", m.get("ErrorMessage"));
              toReturn.put("display_text0", m.get("display_text"));
              return toReturn;
            }
          }
          toReturn.put("ErrorMessage0", "");
          if (file.getPaymentExpire() < 0L) {
            toReturn.put("display_text0", "You are new to the game and may give away an in-game referral to the person who introduced you to Wurm Online using the chat command '/refer' if you purchase premium game time.");
          } else {
            toReturn.put("display_text0", "Don't forget to use the in-game '/refer' chat command to refer the one who introduced you to Wurm Online.");
          }
          if (file.getPaymentExpire() < System.currentTimeMillis() + 604800000L)
          {
            toReturn.put("display_text0", "You have less than a week left of premium game time so the amount of coins you can purchase is somewhat limited.");
            
            toReturn.put("maximum_silver0", Integer.valueOf(10));
          }
          else
          {
            toReturn.put("maximum_silver0", 
              Integer.valueOf(20 + Math.min(100, (int)(file.playingTime / 3600000L * 3L))));
          }
          if ((!file.overRideShop) && (file.isBanned()))
          {
            toReturn.put("PurchaseOk0", "NOTOK");
            toReturn.put("maximum_silver0", Integer.valueOf(0));
            toReturn.put("display_text0", "You have been banned. Reason: " + file.banreason);
            toReturn.put("ErrorMessage0", "The player has been banned. Reason: " + file.banreason);
          }
          else
          {
            toReturn.put("PurchaseOk0", "OK");
          }
          int maxMonths = 0;
          if (file.getPaymentExpire() > System.currentTimeMillis())
          {
            long maxMonthsMillis = System.currentTimeMillis() + 36288000000L - file.getPaymentExpire();
            maxMonths = (int)(maxMonthsMillis / 2419200000L);
            if (maxMonths < 0) {
              maxMonths = 0;
            }
          }
          else
          {
            maxMonths = 12;
          }
          toReturn.put("maximum_months0", Integer.valueOf(maxMonths));
          
          toReturn.put("new_customer0", Boolean.valueOf(file.getPaymentExpire() <= 0L));
          
          toReturn.put("ResponseCode0", "OK");
          toReturn.put("PlayerID0", new Long(file.wurmId));
          toReturn.put("ingameBankBalance0", new Long(file.money));
          toReturn.put("PlayingTimeExpire0", new Long(file.getPaymentExpire()));
        }
        else
        {
          toReturn.put("ResponseCode0", "NOTOK");
          toReturn.put("ErrorMessage0", "Password does not match.");
        }
      }
      catch (Exception ex)
      {
        toReturn.put("ResponseCode0", "NOTOK");
        toReturn.put("ErrorMessage0", ex.getMessage());
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
    else if (isEmailValid(emailAddress))
    {
      PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
      for (int x = 0; x < infos.length; x++) {
        if (infos[x].getPassword().equals(hashedIngamePassword))
        {
          toReturn.put("ErrorMessage" + x, "");
          if (infos[x].getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
            toReturn.put("maximum_silver" + x, Integer.valueOf(10));
          } else {
            toReturn.put("maximum_silver" + x, 
              Integer.valueOf(10 + Math.min(100, (int)(infos[x].playingTime / 86400000L))));
          }
          if ((!infos[x].overRideShop) && (infos[x].isBanned()))
          {
            toReturn.put("PurchaseOk" + x, "NOTOK");
            toReturn.put("maximum_silver" + x, Integer.valueOf(0));
            toReturn.put("display_text" + x, "You have been banned. Reason: " + infos[x].banreason);
            toReturn.put("ErrorMessage" + x, "The player has been banned. Reason: " + infos[x].banreason);
          }
          else
          {
            toReturn.put("PurchaseOk" + x, "OK");
          }
          int maxMonths = 0;
          if (infos[x].getPaymentExpire() > System.currentTimeMillis())
          {
            long maxMonthsMillis = System.currentTimeMillis() + 36288000000L - infos[x].getPaymentExpire();
            maxMonths = (int)(maxMonthsMillis / 2419200000L);
            if (maxMonths < 0) {
              maxMonths = 0;
            }
          }
          else
          {
            maxMonths = 12;
          }
          toReturn.put("maximum_months" + x, Integer.valueOf(maxMonths));
          toReturn.put("new_customer" + x, Boolean.valueOf(infos[x].getPaymentExpire() <= 0L));
          
          toReturn.put("ResponseCode" + x, "OK");
          toReturn.put("PlayerID" + x, new Long(infos[x].wurmId));
          toReturn.put("ingameBankBalance" + x, new Long(infos[x].money));
          toReturn.put("PlayingTimeExpire" + x, new Long(infos[x].getPaymentExpire()));
        }
        else
        {
          toReturn.put("ResponseCode" + x, "NOTOK");
          toReturn.put("ErrorMessage" + x, "Password does not match.");
        }
      }
    }
    else
    {
      toReturn.put("ResponseCode0", "NOTOK");
      toReturn.put("ErrorMessage0", "Invalid email: " + emailAddress);
    }
    return toReturn;
  }
  
  public Map<String, String> changePassword(String intraServerPassword, String playerName, String emailAddress, String newPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Map<String, String> toReturn = new HashMap();
    try
    {
      toReturn.put("Result", "Unknown email.");
      logger.log(Level.INFO, getRemoteClientDetails() + " Changepassword Name: " + playerName + ", email: " + emailAddress);
      PlayerInfo p;
      if (emailAddress != null)
      {
        if (!isEmailValid(emailAddress))
        {
          toReturn.put("Error", emailAddress + " is an invalid email.");
        }
        else
        {
          PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
          int nums = 0;
          for (int x = 0; x < infos.length; x++) {
            if (infos[x].getPower() == 0)
            {
              try
              {
                infos[x].updatePassword(newPassword);
                if (infos[x].currentServer != Servers.localServer.id) {
                  new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                }
                nums++;
                toReturn.put("Account" + nums, infos[x].getName() + " password was updated.");
              }
              catch (IOException iox)
              {
                logger.log(Level.WARNING, "Failed to update password for " + infos[x].getName(), iox);
                toReturn.put("Error" + nums, infos[x].getName() + " password was _not_ updated.");
              }
            }
            else
            {
              toReturn.put("Error" + nums, "Failed to update password for " + infos[x].getName());
              logger.warning("Failed to update password for " + infos[x].getName() + " as power is " + infos[x]
                .getPower());
            }
          }
          if (nums > 0) {
            toReturn.put("Result", nums + " player accounts were affected.");
          } else {
            toReturn.put("Error", nums + " player accounts were affected.");
          }
          return toReturn;
        }
      }
      else if (playerName != null)
      {
        p = PlayerInfoFactory.createPlayerInfo(playerName);
        try
        {
          p.load();
          if (isEmailValid(p.emailAddress))
          {
            emailAddress = p.emailAddress;
            PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
            int nums = 0;
            boolean failed = false;
            for (int x = 0; x < infos.length; x++) {
              if (infos[x].getPower() == 0)
              {
                try
                {
                  infos[x].updatePassword(newPassword);
                  if (infos[x].currentServer != Servers.localServer.id) {
                    new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                  }
                  nums++;
                  toReturn.put("Account" + nums, infos[x].getName() + " password was updated.");
                }
                catch (IOException iox)
                {
                  failed = true;
                  toReturn.put("Error" + nums, "Failed to update password for a player.");
                }
              }
              else
              {
                failed = true;
                logger.warning("Failed to update password for " + infos[x].getName() + " as power is " + infos[x]
                  .getPower());
              }
            }
            if (nums > 0) {
              toReturn.put("Result", nums + " player accounts were affected.");
            } else {
              toReturn.put("Error", nums + " player accounts were affected.");
            }
            if (failed) {
              logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
            }
            return toReturn;
          }
          toReturn.put("Error", emailAddress + " is an invalid email.");
        }
        catch (IOException iox)
        {
          toReturn.put("Error", "Failed to load player data. Password not changed.");
          logger.log(Level.WARNING, iox.getMessage(), iox);
        }
      }
      return toReturn;
    }
    finally
    {
      logger.info("Changepassword Name: " + playerName + ", email: " + emailAddress + ", exit: " + toReturn);
    }
  }
  
  public Map<String, String> changePassword(String intraServerPassword, String playerName, String emailAddress, String hashedOldPassword, String newPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Map<String, String> toReturn = new HashMap();
    toReturn.put("Result", "Unknown email.");
    logger.log(Level.INFO, getRemoteClientDetails() + " Changepassword 2 for player name: " + playerName);
    if (emailAddress != null)
    {
      if (!isEmailValid(emailAddress))
      {
        toReturn.put("Result", emailAddress + " is an invalid email.");
      }
      else
      {
        PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
        boolean ok = false;
        int nums = 0;
        for (int x = 0; x < infos.length; x++) {
          if (infos[x].getPassword().equals(hashedOldPassword)) {
            ok = true;
          }
        }
        if (ok)
        {
          boolean failed = false;
          for (int x = 0; x < infos.length; x++) {
            if (infos[x].getPower() == 0)
            {
              try
              {
                infos[x].updatePassword(newPassword);
                if (infos[x].currentServer != Servers.localServer.id) {
                  new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                }
                nums++;
                toReturn.put("Account" + nums, infos[x].getName() + " password was updated.");
              }
              catch (IOException iox)
              {
                failed = true;
                toReturn.put("Error" + nums, "Failed to update password for " + infos[x].getName());
              }
            }
            else
            {
              failed = true;
              toReturn.put("Error" + nums, infos[x].getName() + " password was _not_ updated.");
            }
          }
          if (failed) {
            logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
          }
        }
        if (nums > 0) {
          toReturn.put("Result", nums + " player accounts were affected.");
        } else {
          toReturn.put("Error", nums + " player accounts were affected.");
        }
        return toReturn;
      }
    }
    else if (playerName != null)
    {
      PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
      try
      {
        p.load();
        boolean ok = false;
        if (isEmailValid(p.emailAddress))
        {
          emailAddress = p.emailAddress;
          PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
          for (int x = 0; x < infos.length; x++) {
            if (infos[x].getPassword().equals(hashedOldPassword)) {
              ok = true;
            }
          }
          int nums = 0;
          if (ok)
          {
            boolean failed = false;
            for (int x = 0; x < infos.length; x++) {
              if (infos[x].getPower() == 0) {
                try
                {
                  infos[x].updatePassword(newPassword);
                  if (infos[x].currentServer != Servers.localServer.id) {
                    new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                  }
                  nums++;
                  toReturn.put("Account" + nums, infos[x].getName() + " password was updated.");
                }
                catch (IOException iox)
                {
                  failed = true;
                  toReturn.put("Error" + x, "Failed to update password for " + infos[x].getName());
                }
              } else {
                failed = true;
              }
            }
            if (failed) {
              logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
            }
          }
          if (nums > 0) {
            toReturn.put("Result", nums + " player accounts were affected.");
          } else {
            toReturn.put("Error", nums + " player accounts were affected.");
          }
          return toReturn;
        }
        toReturn.put("Error", emailAddress + " is an invalid email.");
      }
      catch (IOException iox)
      {
        toReturn.put("Error", "Failed to load player data. Password not changed.");
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
    }
    return toReturn;
  }
  
  public Map<String, String> changeEmail(String intraServerPassword, String playerName, String oldEmailAddress, String newEmailAddress)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Map<String, String> toReturn = new HashMap();
    toReturn.put("Result", "Unknown email.");
    logger.log(Level.INFO, getRemoteClientDetails() + " Change Email for player name: " + playerName);
    if (Constants.maintaining)
    {
      toReturn.put("Error", "The server is currently unavailable.");
      toReturn.put("Result", "The server is in maintenance mode. Please try later.");
      return toReturn;
    }
    if (oldEmailAddress != null)
    {
      if (!isEmailValid(oldEmailAddress))
      {
        toReturn.put("Error", "The old email address, " + oldEmailAddress + " is an invalid email.");
      }
      else if (!isEmailValid(newEmailAddress))
      {
        toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
      }
      else
      {
        PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
        int nums = 0;
        for (int x = 0; x < infos.length; x++) {
          if (infos[x].getPower() == 0)
          {
            infos[x].setEmailAddress(newEmailAddress);
            nums++;
            toReturn.put("Account" + nums, infos[x].getName() + " account was affected.");
          }
          else
          {
            toReturn.put("Account" + nums, infos[x].getName() + " account was _not_ affected.");
          }
        }
        if (nums > 0) {
          toReturn.put("Result", nums + " player accounts were affected.");
        } else {
          toReturn.put("Error", nums + " player accounts were affected.");
        }
      }
      return toReturn;
    }
    if (playerName != null)
    {
      PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
      try
      {
        p.load();
        if (!isEmailValid(newEmailAddress))
        {
          toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
        }
        else
        {
          oldEmailAddress = p.emailAddress;
          PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
          int nums = 0;
          for (int x = 0; x < infos.length; x++) {
            if (infos[x].getPower() == 0)
            {
              infos[x].setEmailAddress(newEmailAddress);
              nums++;
              toReturn.put("Account" + nums, infos[x].getName() + " account was affected.");
            }
            else
            {
              toReturn.put("Account" + nums, infos[x].getName() + " account was _not_ affected.");
            }
          }
          if (nums > 0) {
            toReturn.put("Result", nums + " player accounts were affected.");
          } else {
            toReturn.put("Error", nums + " player accounts were affected.");
          }
          return toReturn;
        }
      }
      catch (IOException iox)
      {
        toReturn.put("Error", "Failed to load player data. Email not changed.");
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
    }
    return toReturn;
  }
  
  public String getChallengePhrase(String intraServerPassword, String playerName)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (playerName.contains("@"))
    {
      PlayerInfo[] pinfos = PlayerInfoFactory.getPlayerInfosForEmail(playerName);
      if (pinfos.length > 0) {
        return pinfos[0].pwQuestion;
      }
      return "Incorrect email.";
    }
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getChallengePhrase for player name: " + playerName);
    }
    PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
    try
    {
      p.load();
      return p.pwQuestion;
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
    return "Error";
  }
  
  public String[] getPlayerNamesForEmail(String intraServerPassword, String emailAddress)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getPlayerNamesForEmail: " + emailAddress);
    }
    String[] nameArray = PlayerInfoFactory.getAccountsForEmail(emailAddress);
    return nameArray;
  }
  
  public String getEmailAddress(String intraServerPassword, String playerName)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getEmailAddress for player name: " + playerName);
    }
    PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
    try
    {
      p.load();
      return p.emailAddress;
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
    return "Error";
  }
  
  public static String generateRandomPassword()
  {
    Random rand = new Random();
    
    int length = rand.nextInt(3) + 6;
    char[] password = new char[length];
    for (int x = 0; x < length; x++)
    {
      int randDecimalAsciiVal = rand.nextInt("abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".length());
      password[x] = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".charAt(randDecimalAsciiVal);
    }
    return String.valueOf(password);
  }
  
  public static final boolean isEmailValid(String emailAddress)
  {
    if (emailAddress == null) {
      return false;
    }
    Matcher m = VALID_EMAIL_PATTERN.matcher(emailAddress);
    return m.matches();
  }
  
  public Map<String, String> requestPasswordReset(String intraServerPassword, String email, String challengePhraseAnswer)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    Map<String, String> toReturn = new HashMap();
    if (Constants.maintaining)
    {
      toReturn.put("Error0", "The server is currently in maintenance mode.");
      return toReturn;
    }
    boolean ok = false;
    
    String password = generateRandomPassword();
    String playernames = "";
    logger.log(Level.INFO, getRemoteClientDetails() + " Password reset for email/name: " + email);
    if ((challengePhraseAnswer == null) || (challengePhraseAnswer.length() < 1))
    {
      toReturn.put("Error0", "The answer is too short.");
      return toReturn;
    }
    if (!email.contains("@"))
    {
      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(email);
      if (!pinf.loaded) {
        try
        {
          pinf.load();
          
          logger.log(Level.INFO, email + " " + challengePhraseAnswer + " compares to " + pinf.pwAnswer);
          if (System.currentTimeMillis() - pinf.lastRequestedPassword > 60000L)
          {
            logger.log(Level.INFO, email + " time ok. comparing.");
            if (pinf.pwAnswer.equalsIgnoreCase(challengePhraseAnswer))
            {
              logger.log(Level.INFO, email + " challenge answer correct.");
              
              ok = true;
              playernames = pinf.getName();
              pinf.updatePassword(password);
              if (pinf.currentServer != Servers.localServer.id) {
                new PasswordTransfer(pinf.getName(), pinf.wurmId, pinf.getPassword(), System.currentTimeMillis(), false);
              }
            }
          }
          else
          {
            toReturn.put("Error", "Please try again in a minute.");
            return toReturn;
          }
          pinf.lastRequestedPassword = System.currentTimeMillis();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, email + ":" + iox.getMessage(), iox);
          toReturn.put("Error", "An error occured. Please try later.");
          return toReturn;
        }
      }
    }
    else
    {
      PlayerInfo[] p = PlayerInfoFactory.getPlayerInfosWithEmail(email);
      for (int x = 0; x < p.length; x++) {
        try
        {
          p[x].load();
          if ((p[x].pwAnswer.toLowerCase().equals(challengePhraseAnswer.toLowerCase())) || (
            (p[x].pwAnswer.length() == 0) && (p[x].pwQuestion.length() == 0))) {
            if (System.currentTimeMillis() - p[x].lastRequestedPassword > 60000L)
            {
              ok = true;
              if (playernames.length() > 0) {
                playernames = playernames + ", " + p[x].getName();
              } else {
                playernames = p[x].getName();
              }
              p[x].updatePassword(password);
              if (p[x].currentServer != Servers.localServer.id) {
                new PasswordTransfer(p[x].getName(), p[x].wurmId, p[x].getPassword(), System.currentTimeMillis(), false);
              }
            }
            else if (!ok)
            {
              toReturn.put("Error", "Please try again in a minute.");
              return toReturn;
            }
          }
          p[x].lastRequestedPassword = System.currentTimeMillis();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, email + ":" + iox.getMessage(), iox);
          toReturn.put("Error", "An error occured. Please try later.");
          return toReturn;
        }
      }
    }
    if (ok) {
      toReturn.put("Result", "Password was changed.");
    } else {
      toReturn.put("Error", "Password was not changed.");
    }
    if (playernames.length() > 0)
    {
      try
      {
        String mail = Mailer.getPasswordMail();
        mail = mail.replace("@pname", playernames);
        mail = mail.replace("@password", password);
        Mailer.sendMail(mailAccount, email, "Wurm Online password request", mail);
        toReturn.put("MailResult", "A mail was sent to the mail adress: " + email + " for " + playernames + ".");
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, email + ":" + ex.getMessage(), ex);
        toReturn.put("MailError", "An error occured - " + ex.getMessage() + ". Please try later.");
      }
    }
    else
    {
      toReturn.put("Error", "Wrong answer.");
      return toReturn;
    }
    return toReturn;
  }
  
  public Map<Integer, String> getAllServers(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    return getAllServerInternalAddresses(intraServerPassword);
  }
  
  public Map<Integer, String> getAllServerInternalAddresses(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Map<Integer, String> toReturn = new HashMap();
    ServerEntry[] entries = Servers.getAllServers();
    for (int x = 0; x < entries.length; x++) {
      toReturn.put(Integer.valueOf(entries[x].id), entries[x].INTRASERVERADDRESS);
    }
    return toReturn;
  }
  
  public boolean sendMail(String intraServerPassword, String sender, String receiver, String subject, String text)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (!isEmailValid(sender)) {
      return false;
    }
    if (!isEmailValid(receiver)) {
      return false;
    }
    try
    {
      Mailer.sendMail(sender, receiver, subject, text);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
      return false;
    }
    return true;
  }
  
  public void shutDown(String intraServerPassword, String playerName, String password, String reason, int seconds)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINE)) {
      logger.fine(getRemoteClientDetails() + " shutDown by player name: " + playerName);
    }
    PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(playerName));
    try
    {
      pinf.load();
      if (pinf.getPower() >= 4) {
        try
        {
          String pw = LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(pinf.getName())));
          if (pw.equals(pinf.getPassword()))
          {
            logger.log(Level.INFO, getRemoteClientDetails() + " player: " + playerName + " initiated shutdown in " + seconds + " seconds: " + reason);
            if (seconds <= 0) {
              Server.getInstance().shutDown();
            } else {
              Server.getInstance().startShutdown(seconds, reason);
            }
          }
          else
          {
            logger.log(Level.WARNING, getRemoteClientDetails() + " player: " + playerName + " denied shutdown due to wrong password.");
          }
        }
        catch (Exception ex)
        {
          logger.log(Level.INFO, "Failed to encrypt password for player " + playerName, ex);
        }
      } else {
        logger.log(Level.INFO, getRemoteClientDetails() + " player: " + playerName + " DENIED shutdown in " + seconds + " seconds: " + reason);
      }
    }
    catch (IOException iox)
    {
      logger.log(Level.INFO, getRemoteClientDetails() + " player: " + playerName + ": " + iox.getMessage(), iox);
    }
  }
  
  public Map<String, Byte> getReferrers(String intraServerPassword, long wurmid)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getReferrers for WurmID: " + wurmid);
    }
    return PlayerInfoFactory.getReferrers(wurmid);
  }
  
  public String addReferrer(String intraServerPassword, String receiver, long referrer)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    logger.info(getRemoteClientDetails() + " addReferrer for Receiver player name: " + receiver + ", referrerID: " + referrer);
    synchronized (Server.SYNC_LOCK)
    {
      try
      {
        PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(receiver);
        try
        {
          pinf.load();
        }
        catch (IOException iox)
        {
          return receiver + " - no such player exists. Please check the spelling.";
        }
        if (pinf.wurmId == referrer) {
          return "You may not refer yourself.";
        }
        if (pinf.getPaymentExpire() <= 0L) {
          return pinf.getName() + " has never had a premium account and may not receive referrals.";
        }
        if (PlayerInfoFactory.addReferrer(pinf.wurmId, referrer)) {
          return String.valueOf(pinf.wurmId);
        }
        return "You have already awarded referral to that player.";
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, e.getMessage() + " " + receiver + " from " + referrer, e);
        return "An error occurred. Please write a bug report about this.";
      }
    }
  }
  
  public String acceptReferrer(String intraServerPassword, long wurmid, String awarderName, boolean money)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINE)) {
      logger.fine(getRemoteClientDetails() + " acceptReferrer for player wurmid: " + wurmid + ", awarderName: " + awarderName + ", money: " + money);
    }
    String name = awarderName;
    PlayerInfo pinf = null;
    try
    {
      long l = Long.parseLong(awarderName);
      pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(l);
    }
    catch (NumberFormatException nfe)
    {
      pinf = PlayerInfoFactory.createPlayerInfo(name);
      try
      {
        pinf.load();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
        return "Failed to locate the player " + awarderName + " in the database.";
      }
    }
    if (pinf != null) {
      try
      {
        synchronized (Server.SYNC_LOCK)
        {
          if (PlayerInfoFactory.acceptReferer(wurmid, pinf.wurmId, money)) {
            try
            {
              if (money) {
                PlayerInfoFactory.addMoneyToBank(wurmid, 30000L, "Referred by " + pinf.getName());
              } else {
                PlayerInfoFactory.addPlayingTime(wurmid, 0, 20, "Referred by " + pinf.getName());
              }
            }
            catch (Exception ex)
            {
              logger.log(Level.WARNING, ex.getMessage(), ex);
              PlayerInfoFactory.revertReferer(wurmid, pinf.wurmId);
              return "An error occured. Please try later or post a bug report.";
            }
          } else {
            return "Failed to match " + awarderName + " to any existing referral.";
          }
        }
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
        return "An error occured. Please try later or post a bug report.";
      }
    }
    return "Failed to locate " + awarderName + " in the database.";
    return "Okay, accepted the referral from " + awarderName + ". The reward will arrive soon if it has not already.";
  }
  
  public Map<String, Double> getSkillStats(String intraServerPassword, int skillid)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getSkillStats for skillid: " + skillid);
    }
    Map<String, Double> toReturn = new HashMap();
    try
    {
      SkillStat sk = SkillStat.getSkillStatForSkill(skillid);
      for (it = sk.stats.entrySet().iterator(); it.hasNext();)
      {
        Map.Entry<Long, Double> entry = (Map.Entry)it.next();
        Long lid = (Long)entry.getKey();
        long pid = lid.longValue();
        PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(pid);
        if (p != null) {
          if (((Double)entry.getValue()).doubleValue() > 1.0D) {
            toReturn.put(p.getName(), entry.getValue());
          }
        }
      }
    }
    catch (Exception ex)
    {
      Iterator<Map.Entry<Long, Double>> it;
      logger.log(Level.WARNING, ex.getMessage(), ex);
      toReturn.put("ERROR: " + ex.getMessage(), Double.valueOf(0.0D));
    }
    return toReturn;
  }
  
  public Map<Integer, String> getSkills(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    return SkillSystem.skillNames;
  }
  
  public Map<String, ?> getStructureSummary(String intraServerPassword, long aStructureID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getStructureSummary for StructureID: " + aStructureID);
    }
    Map<String, Object> lToReturn = new HashMap(10);
    try
    {
      Structure lStructure = Structures.getStructure(aStructureID);
      if (lStructure != null)
      {
        lToReturn.put("CenterX", Integer.valueOf(lStructure.getCenterX()));
        lToReturn.put("CenterY", Integer.valueOf(lStructure.getCenterY()));
        
        lToReturn.put("CreationDate", Long.valueOf(lStructure.getCreationDate()));
        lToReturn.put("Door Count", Integer.valueOf(lStructure.getDoors()));
        lToReturn.put("FinalFinished", Boolean.valueOf(lStructure.isFinalFinished()));
        lToReturn.put("Finalized", Boolean.valueOf(lStructure.isFinalized()));
        lToReturn.put("Finished", Boolean.valueOf(lStructure.isFinished()));
        lToReturn.put("Guest Count", Integer.valueOf(lStructure.getPermissionsPlayerList().size()));
        lToReturn.put("Limit", Integer.valueOf(lStructure.getLimit()));
        lToReturn.put("Lockable", Boolean.valueOf(lStructure.isLockable()));
        lToReturn.put("Locked", Boolean.valueOf(lStructure.isLocked()));
        lToReturn.put("MaxX", Integer.valueOf(lStructure.getMaxX()));
        lToReturn.put("MaxY", Integer.valueOf(lStructure.getMaxY()));
        lToReturn.put("MinX", Integer.valueOf(lStructure.getMinX()));
        lToReturn.put("MinY", Integer.valueOf(lStructure.getMinY()));
        lToReturn.put("Name", lStructure.getName());
        lToReturn.put("OwnerID", Long.valueOf(lStructure.getOwnerId()));
        lToReturn.put("Roof", Byte.valueOf(lStructure.getRoof()));
        lToReturn.put("Size", Integer.valueOf(lStructure.getSize()));
        lToReturn.put("HasWalls", Boolean.valueOf(lStructure.hasWalls()));
        Wall[] lWalls = lStructure.getWalls();
        if (lWalls != null) {
          lToReturn.put("Wall Count", Integer.valueOf(lWalls.length));
        } else {
          lToReturn.put("Wall Count", Integer.valueOf(0));
        }
        lToReturn.put("WritID", Long.valueOf(lStructure.getWritId()));
        lToReturn.put("WurmID", Long.valueOf(lStructure.getWurmId()));
      }
      else
      {
        lToReturn.put("Error", "No such Structure");
      }
    }
    catch (NoSuchStructureException nss)
    {
      logger.log(Level.WARNING, "Structure with id " + aStructureID + " not found.", nss);
      lToReturn.put("Error", "No such Structure");
      lToReturn.put("Exception", nss.getMessage());
    }
    catch (RuntimeException e)
    {
      logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
      lToReturn.put("Exception", e);
    }
    return lToReturn;
  }
  
  public long getStructureIdFromWrit(String intraServerPassword, long aWritID)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getStructureIdFromWrit for WritID: " + aWritID);
    }
    try
    {
      Structure struct = Structures.getStructureForWrit(aWritID);
      if (struct != null) {
        return struct.getWurmId();
      }
    }
    catch (NoSuchStructureException localNoSuchStructureException) {}
    return -1L;
  }
  
  public Map<String, ?> getTileSummary(String intraServerPassword, int tilex, int tiley, boolean surfaced)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getTileSummary for tile (x,y): " + tilex + ", " + tiley);
    }
    Map<String, Object> lToReturn = new HashMap(10);
    try
    {
      Zone zone = Zones.getZone(tilex, tiley, surfaced);
      
      VolaTile tile = zone.getTileOrNull(tilex, tiley);
      if (tile != null)
      {
        Structure lStructure = tile.getStructure();
        if (lStructure != null)
        {
          lToReturn.put("StructureID", Long.valueOf(lStructure.getWurmId()));
          lToReturn.put("StructureName", lStructure.getName());
        }
        lToReturn.put("Kingdom", Byte.valueOf(tile.getKingdom()));
        
        Village lVillage = tile.getVillage();
        if (lVillage != null)
        {
          lToReturn.put("VillageID", Integer.valueOf(lVillage.getId()));
          lToReturn.put("VillageName", lVillage.getName());
        }
        lToReturn.put("Coord x", Integer.valueOf(tile.getTileX()));
        
        lToReturn.put("Coord y", Integer.valueOf(tile.getTileY()));
      }
      else
      {
        lToReturn.put("Error", "No such tile");
      }
    }
    catch (NoSuchZoneException e)
    {
      lToReturn.put("Error", "No such zone");
      lToReturn.put("Exception", e.getMessage());
    }
    catch (RuntimeException e)
    {
      logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
      lToReturn.put("Exception", e);
    }
    return lToReturn;
  }
  
  public String getReimbursementInfo(String intraServerPassword, String email)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getReimbursementInfo for email: " + email);
    }
    return Reimbursement.getReimbursementInfo(email);
  }
  
  public boolean withDraw(String intraServerPassword, String retriever, String name, String _email, int _months, int _silvers, boolean titlebok, int _daysLeft)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    logger.info(getRemoteClientDetails() + " withDraw for retriever: " + retriever + ", name: " + name + ", email: " + _email + ", months: " + _months + ", silvers: " + _silvers);
    
    return Reimbursement.withDraw(retriever, name, _email, _months, _silvers, titlebok, _daysLeft);
  }
  
  public boolean transferPlayer(String intraServerPassword, String playerName, int posx, int posy, boolean surfaced, int power, byte[] data)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if ((Constants.maintaining) && (power <= 0)) {
      return false;
    }
    logger.log(Level.INFO, getRemoteClientDetails() + " Transferplayer name: " + playerName + ", position (x,y): " + posx + ", " + posy + ", surfaced: " + surfaced);
    if (IntraServerConnection.savePlayerToDisk(data, posx, posy, surfaced, false) > 0L)
    {
      if (!Servers.isThisLoginServer())
      {
        if (new LoginServerWebConnection().setCurrentServer(playerName, Servers.localServer.id)) {
          return true;
        }
        return false;
      }
      return true;
    }
    return false;
  }
  
  public boolean changePassword(String intraServerPassword, long wurmId, String newPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    logger.log(Level.INFO, getRemoteClientDetails() + " Changepassword name: " + wurmId);
    return IntraServerConnection.setNewPassword(wurmId, newPassword);
  }
  
  public boolean setCurrentServer(String intraServerPassword, String name, int currentServer)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " setCurrentServer to " + currentServer + " for player name: " + name);
    }
    PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
    if (pinf == null) {
      return false;
    }
    pinf.setCurrentServer(currentServer);
    return true;
  }
  
  public boolean addDraggedItem(String intraServerPassword, long itemId, byte[] itemdata, long draggerId, int posx, int posy)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
    logger.log(Level.INFO, getRemoteClientDetails() + " Adddraggeditem itemID: " + itemId + ", draggerId: " + draggerId);
    try
    {
      Set<ItemMetaData> idset = new HashSet();
      int nums = iis.readInt();
      for (int x = 0; x < nums; x++) {
        IntraServerConnection.createItem(iis, 0.0F, 0.0F, 0.0F, idset, false);
      }
      Items.convertItemMetaData((ItemMetaData[])idset.toArray(new ItemMetaData[idset.size()]));
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, iox.getMessage(), iox);
      return false;
    }
    try
    {
      Item i = Items.getItem(itemId);
      Zone z = Zones.getZone(posx, posy, true);
      z.addItem(i);
      return true;
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.WARNING, nsi.getMessage(), nsi);
      return false;
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, nsz.getMessage(), nsz);
    }
    return false;
  }
  
  public String rename(String intraServerPassword, String oldName, String newName, String newPass, int power)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " rename oldName: " + oldName + ", newName: " + newName + ", power: " + power);
    }
    String toReturn = "";
    newName = LoginHandler.raiseFirstLetter(newName);
    if ((Servers.localServer.LOGINSERVER) && (Players.getInstance().doesPlayerNameExist(newName))) {
      return "The name " + newName + " already exists. This is an Error.";
    }
    if (Servers.localServer.LOGINSERVER) {
      toReturn = toReturn + Servers.rename(oldName, newName, newPass, power);
    }
    if (!toReturn.contains("Error.")) {
      try
      {
        toReturn = PlayerInfoFactory.rename(oldName, newName, newPass, power);
      }
      catch (IOException iox)
      {
        toReturn = toReturn + Servers.localServer.name + " " + iox.getMessage() + ". This is an Error.\n";
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
    }
    return toReturn;
  }
  
  public String changePassword(String intraServerPassword, String changerName, String name, String newPass, int power)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " changePassword, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
    }
    String toReturn = "";
    changerName = LoginHandler.raiseFirstLetter(changerName);
    name = LoginHandler.raiseFirstLetter(name);
    try
    {
      toReturn = PlayerInfoFactory.changePassword(changerName, name, newPass, power);
    }
    catch (IOException iox)
    {
      toReturn = toReturn + Servers.localServer.name + " " + iox.getMessage() + "\n";
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
    logger.log(Level.INFO, getRemoteClientDetails() + " changePassword, changerName: " + changerName + ", for player name: " + name);
    if (Servers.localServer.LOGINSERVER) {
      if (changerName.equals(name))
      {
        PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        if ((pinf != null) && (Servers.localServer.id != pinf.currentServer))
        {
          LoginServerWebConnection lsw = new LoginServerWebConnection(pinf.currentServer);
          toReturn = toReturn + lsw.changePassword(changerName, name, newPass, power);
        }
      }
      else
      {
        toReturn = toReturn + Servers.sendChangePass(changerName, name, newPass, power);
      }
    }
    return toReturn;
  }
  
  public String changeEmail(String intraServerPassword, String changerName, String name, String newEmail, String password, int power, String pwQuestion, String pwAnswer)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " changeEmail, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
    }
    changerName = LoginHandler.raiseFirstLetter(changerName);
    name = LoginHandler.raiseFirstLetter(name);
    String toReturn = "";
    logger.log(Level.INFO, getRemoteClientDetails() + " changeEmail, changerName: " + changerName + ", for player name: " + name);
    try
    {
      toReturn = PlayerInfoFactory.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
      if ((toReturn.equals("NO")) || 
        (toReturn.equals("NO Retrieval info updated."))) {
        return "You may only have 5 accounts with the same email. Also you need to provide the correct password for a character with that email address in order to change to it.";
      }
    }
    catch (IOException iox)
    {
      toReturn = toReturn + Servers.localServer.name + " " + iox.getMessage() + "\n";
      logger.log(Level.WARNING, iox.getMessage(), iox);
    }
    if (Servers.localServer.LOGINSERVER) {
      toReturn = toReturn + Servers.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
    }
    return toReturn;
  }
  
  public String addReimb(String intraServerPassword, String changerName, String name, int numMonths, int _silver, int _daysLeft, boolean setbok)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINE)) {
      logger.fine(getRemoteClientDetails() + " addReimb, changerName: " + changerName + ", for player name: " + name + ", numMonths: " + numMonths + ", silver: " + _silver + ", daysLeft: " + _daysLeft + ", setbok: " + setbok);
    }
    changerName = LoginHandler.raiseFirstLetter(changerName);
    name = LoginHandler.raiseFirstLetter(name);
    if (Servers.localServer.LOGINSERVER) {
      return Reimbursement.addReimb(changerName, name, numMonths, _silver, _daysLeft, setbok);
    }
    return Servers.localServer.name + " - failed to add reimbursement. This is not the login server.";
  }
  
  public long[] getCurrentServerAndWurmid(String intraServerPassword, String name, long wurmid)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " getCurrentServerAndWurmid for player name: " + name + ", wurmid: " + wurmid);
    }
    PlayerInfo pinf = null;
    if ((name != null) && (name.length() > 2))
    {
      name = LoginHandler.raiseFirstLetter(name);
      pinf = PlayerInfoFactory.createPlayerInfo(name);
    }
    else if (wurmid > 0L)
    {
      pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
    }
    if (pinf != null) {
      try
      {
        pinf.load();
        return new long[] { pinf.currentServer, pinf.wurmId };
      }
      catch (IOException localIOException) {}
    }
    return noInfoLong;
  }
  
  public Map<Long, byte[]> getPlayerStates(String intraServerPassword, long[] wurmids)
    throws RemoteException, WurmServerException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      if (wurmids.length == 0)
      {
        logger.finer(getRemoteClientDetails() + " getPlayersSubInfo for ALL players.");
      }
      else
      {
        StringBuilder buf = new StringBuilder();
        for (int x = 0; x < wurmids.length; x++)
        {
          if (x > 0) {
            buf.append(",");
          }
          buf.append(wurmids[x]);
        }
        logger.finer(getRemoteClientDetails() + " getPlayersSubInfo for player wurmids: " + buf
          .toString());
      }
    }
    return PlayerInfoFactory.getPlayerStates(wurmids);
  }
  
  public void manageFeature(String intraServerPassword, int serverId, final int featureId, final boolean aOverridden, final boolean aEnabled, final boolean global)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " manageFeature " + featureId);
    }
    Thread t = new Thread("manageFeature-Thread-" + featureId)
    {
      public void run()
      {
        Features.Feature.setOverridden(Servers.getLocalServerId(), featureId, aOverridden, aEnabled, global);
      }
    };
    t.setPriority(4);
    t.start();
  }
  
  public void startShutdown(String intraServerPassword, String instigator, int seconds, String reason)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (Servers.isThisLoginServer()) {
      Servers.startShutdown(instigator, seconds, reason);
    }
    logger.log(Level.INFO, instigator + " shutting down server in " + seconds + " seconds, reason: " + reason);
    
    Server.getInstance().startShutdown(seconds, reason);
  }
  
  public String sendMail(String intraServerPassword, byte[] maildata, byte[] itemdata, long sender, long wurmid, int targetServer)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    logger.log(Level.INFO, getRemoteClientDetails() + " sendMail " + sender + " to server " + targetServer + ", receiver ID: " + wurmid);
    if (targetServer == Servers.localServer.id)
    {
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(maildata));
      try
      {
        int nums = dis.readInt();
        for (int x = 0; x < nums; x++)
        {
          WurmMail m = new WurmMail(dis.readByte(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readInt(), dis.readBoolean(), false);
          
          WurmMail.addWurmMail(m);
          m.createInDatabase();
        }
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
        return "A database error occurred. Please report this to a GM.";
      }
      DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
      try
      {
        Set<ItemMetaData> idset = new HashSet();
        int nums = iis.readInt();
        for (int x = 0; x < nums; x++) {
          IntraServerConnection.createItem(iis, 0.0F, 0.0F, 0.0F, idset, false);
        }
        Items.convertItemMetaData((ItemMetaData[])idset.toArray(new ItemMetaData[idset.size()]));
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
        return "A database error occurred when inserting an item. Please report this to a GM.";
      }
    }
    else
    {
      ServerEntry entry = Servers.getServerWithId(targetServer);
      if (entry != null)
      {
        if (entry.isAvailable(5, true))
        {
          LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
          return lsw.sendMail(maildata, itemdata, sender, wurmid, targetServer);
        }
        return "The target server is not available right now.";
      }
      return "Failed to locate target server.";
    }
    return "";
  }
  
  public String pardonban(String intraServerPassword, String name)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " pardonban for player name: " + name);
    }
    if (Servers.localServer.LOGINSERVER)
    {
      PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
      if (info != null)
      {
        try
        {
          info.load();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, getRemoteClientDetails() + " Failed to load the player information. Not pardoned - " + iox
            .getMessage(), iox);
          return "Failed to load the player information. Not pardoned.";
        }
        try
        {
          info.setBanned(false, "", 0L);
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, getRemoteClientDetails() + " Failed to save the player information. Not pardoned - " + iox
            .getMessage(), iox);
          return "Failed to save the player information. Not pardoned.";
        }
        logger.info(getRemoteClientDetails() + " Login server pardoned " + name);
        return "Login server pardoned " + name + ".";
      }
      logger.warning("Failed to locate the player " + name + ".");
      return "Failed to locate the player " + name + ".";
    }
    logger.warning(Servers.localServer.name + " not login server. Pardon failed.");
    return Servers.localServer.name + " not login server. Pardon failed.";
  }
  
  public String ban(String intraServerPassword, String name, String reason, int days)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " ban for player name: " + name + ", reason: " + reason + ", for " + days + " days");
    }
    if (Servers.localServer.LOGINSERVER)
    {
      PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
      if (info != null)
      {
        long expiry = System.currentTimeMillis() + days * 86400000L;
        try
        {
          info.load();
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, "Failed to load the player information. Not banned - " + iox.getMessage(), iox);
          return "Failed to load the player information. Not banned.";
        }
        try
        {
          info.setBanned(true, reason, expiry);
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, "Failed to save the player information. Not banned - " + iox.getMessage(), iox);
          return "Failed to save the player information. Not banned.";
        }
        logger.info(getRemoteClientDetails() + " Login server banned " + name + ": " + reason + " for " + days + " days.");
        
        return "Login server banned " + name + ": " + reason + " for " + days + " days.";
      }
      logger.warning("Failed to locate the player " + name + ".");
      return "Failed to locate the player " + name + ".";
    }
    logger.warning(Servers.localServer.name + " not login server. IPBan failed.");
    return Servers.localServer.name + " not login server. IPBan failed.";
  }
  
  public String addBannedIp(String intraServerPassword, String ip, String reason, int days)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    long expiry = System.currentTimeMillis() + days * 86400000L;
    Players.getInstance().addBannedIp(ip, reason, expiry);
    logger.info(getRemoteClientDetails() + " RMI client requested " + ip + " banned for " + days + " days - " + reason);
    return ip + " banned for " + days + " days - " + reason;
  }
  
  public Ban[] getPlayersBanned(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    return Players.getInstance().getPlayersBanned();
  }
  
  public Ban[] getIpsBanned(String intraServerPassword)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    return Players.getInstance().getBans();
  }
  
  public String removeBannedIp(String intraServerPassword, String ip)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (Players.getInstance().removeBan(ip))
    {
      logger.log(Level.INFO, getRemoteClientDetails() + " RMI client requested " + ip + " was pardoned.");
      return "Okay, " + ip + " was pardoned.";
    }
    logger.info(getRemoteClientDetails() + " RMI client requested pardon but the ip " + ip + " was not previously banned.");
    
    return "The ip " + ip + " was not previously banned.";
  }
  
  public String setPlayerMoney(String intraServerPassword, long wurmid, long currentMoney, long moneyAdded, String detail)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (moneyDetails.contains(detail))
    {
      logger.warning(getRemoteClientDetails() + " RMI client The money transaction has already been performed, wurmid: " + wurmid + ", currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail);
      
      return "The money transaction has already been performed";
    }
    logger.log(Level.INFO, getRemoteClientDetails() + " RMI client set player money for " + wurmid);
    PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
    if (info != null)
    {
      try
      {
        info.load();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to load player info for " + wurmid + ", detail: " + detail + ": " + iox
          .getMessage(), iox);
        return "Failed to load the player from database. Transaction failed.";
      }
    }
    else
    {
      logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
      
      return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
    }
    if (info.wurmId > 0L)
    {
      if (info.currentServer != Servers.localServer.id)
      {
        logger.warning("Received a CMD_SET_PLAYER_MONEY for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info
          .getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + "), detail: " + detail);
        
        return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
      }
      try
      {
        info.setMoney(currentMoney);
        new MoneyTransfer(info.getName(), wurmid, currentMoney, moneyAdded, detail, (byte)6, "");
        
        Change c = new Change(currentMoney);
        
        moneyDetails.add(detail);
        try
        {
          logger.info(getRemoteClientDetails() + " RMI client Added " + moneyAdded + " to player ID: " + wurmid + ", currentMoney: " + currentMoney + ", detail: " + detail);
          
          Player p = Players.getInstance().getPlayer(wurmid);
          
          Message mess = new Message(null, (byte)3, ":Event", "Your available money in the bank is now " + c.getChangeString() + ".");
          mess.setReceiver(p.getWurmId());
          Server.getInstance().addMessage(mess);
        }
        catch (NoSuchPlayerException exp)
        {
          if (logger.isLoggable(Level.FINER)) {
            logger.finer("player ID: " + wurmid + " is not online, currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail);
          }
        }
        return "Okay. The player now has " + c.getChangeString() + " in the bank.";
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, wurmid + ", failed to set money to " + currentMoney + ", detail: " + detail + ".", iox);
        
        return "Money transaction failed. Error reported was " + iox.getMessage() + ".";
      }
    }
    logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
    
    return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
  }
  
  public String setPlayerPremiumTime(String intraServerPassword, long wurmid, long currentExpire, int days, int months, String detail)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (timeDetails.contains(detail))
    {
      logger.warning(getRemoteClientDetails() + " RMI client The time transaction has already been performed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail);
      
      return "The time transaction has already been performed";
    }
    logger.log(Level.INFO, getRemoteClientDetails() + " RMI client set premium time for " + wurmid);
    PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
    if (info != null)
    {
      try
      {
        info.load();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to load the player from database. Transaction failed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail, iox);
        
        return "Failed to load the player from database. Transaction failed.";
      }
      if (info.currentServer != Servers.localServer.id)
      {
        logger.warning("Received a CMD_SET_PLAYER_PAYMENTEXPIRE for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info
          .getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + "), detail: " + detail);
        
        return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
      }
      try
      {
        info.setPaymentExpire(currentExpire);
        new TimeTransfer(info.getName(), wurmid, months, false, days, detail);
        
        timeDetails.add(detail);
        try
        {
          Player p = Players.getInstance().getPlayer(wurmid);
          
          String expireString = "You now have premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
          Message mess = new Message(null, (byte)3, ":Event", expireString);
          
          mess.setReceiver(p.getWurmId());
          Server.getInstance().addMessage(mess);
        }
        catch (NoSuchPlayerException localNoSuchPlayerException) {}
        logger.info(getRemoteClientDetails() + " RMI client " + info.getName() + " now has premier playing time until " + 
          WurmCalendar.formatGmt(currentExpire) + ", wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail + '.');
        
        return "Okay. " + info.getName() + " now has premier playing time until " + 
          WurmCalendar.formatGmt(currentExpire) + ".";
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Transaction failed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail + ", " + iox
          .getMessage(), iox);
        return "Time transaction failed. Error reported was " + iox.getMessage() + ".";
      }
    }
    logger.log(Level.WARNING, wurmid + ", failed to locate player info and set expire time to " + currentExpire + "!, detail: " + detail);
    
    return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
  }
  
  public void setWeather(String intraServerPassword, float windRotation, float windpower, float windDir)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Server.getWeather().setWindOnly(windRotation, windpower, windDir);
    logger.log(Level.INFO, getRemoteClientDetails() + " RMI client. Received weather data from login server. Propagating windrot=" + windRotation);
    
    Players.getInstance().setShouldSendWeather(true);
  }
  
  public String sendVehicle(String intraServerPassword, byte[] passengerdata, byte[] itemdata, long pilotId, long vehicleId, int targetServer, int tilex, int tiley, int layer, float rot)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    logger.log(Level.INFO, getRemoteClientDetails() + " RMI client send vehicle for pilot " + pilotId + " vehicle " + vehicleId + " itemdata bytes=" + itemdata.length + " passenger data bytes=" + passengerdata.length);
    if (targetServer == Servers.localServer.id)
    {
      long start = System.nanoTime();
      
      DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
      Set<ItemMetaData> idset = new HashSet();
      try
      {
        int nums = iis.readInt();
        logger.log(Level.INFO, "Trying to create " + nums + " items for vehicle: " + vehicleId);
        posx = tilex * 4 + 2;
        float posy = tiley * 4 + 2;
        IntraServerConnection.resetTransferVariables(String.valueOf(pilotId));
        for (int x = 0; x < nums; x++) {
          IntraServerConnection.createItem(iis, posx, posy, 0.0F, idset, false);
        }
        Items.convertItemMetaData((ItemMetaData[])idset.toArray(new ItemMetaData[idset.size()]));
      }
      catch (IOException iox)
      {
        float posx;
        logger.log(Level.WARNING, iox.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId, iox);
        for (ItemMetaData md : idset) {
          logger.log(Level.INFO, md.itname + ", " + md.itemId);
        }
        return "A database error occurred when inserting an item. Please report this to a GM.";
      }
      catch (Exception ex)
      {
        logger.log(Level.WARNING, ex.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId, ex);
        
        return "A database error occurred when inserting an item. Please report this to a GM.";
      }
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(passengerdata));
      try
      {
        Item i = Items.getItem(vehicleId);
        
        i.setPosXYZ(tilex * 4 + 2, tiley * 4 + 2, 0.0F);
        i.setRotation(rot);
        logger.log(Level.INFO, "Trying to put " + i.getName() + ", " + i.getDescription() + " at " + i.getTileX() + "," + i
          .getTileY());
        
        Zones.getZone(i.getTileX(), i.getTileY(), layer == 0).addItem(i);
        Vehicles.createVehicle(i);
        MountTransfer mt = new MountTransfer(vehicleId, pilotId);
        int nums = dis.readInt();
        for (int x = 0; x < nums; x++) {
          mt.addToSeat(dis.readLong(), dis.readInt());
        }
      }
      catch (NoSuchItemException nsi)
      {
        logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + nsi.getMessage(), nsi);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + nsz.getMessage(), nsz);
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + iox.getMessage(), iox);
        return "A database error occurred. Please report this to a GM.";
      }
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.log(Level.INFO, "Transferring vehicle " + vehicleId + " took " + lElapsedTime + " ms.");
    }
    else
    {
      ServerEntry entry = Servers.getServerWithId(targetServer);
      if (entry != null)
      {
        if (entry.isAvailable(5, true))
        {
          LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
          return lsw.sendVehicle(passengerdata, itemdata, pilotId, vehicleId, targetServer, tilex, tiley, layer, rot);
        }
        return "The target server is not available right now.";
      }
      return "Failed to locate target server.";
    }
    return "";
  }
  
  public void genericWebCommand(String intraServerPassword, short wctype, long id, byte[] data)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    WebCommand wc = WebCommand.createWebCommand(wctype, id, data);
    if (wc != null)
    {
      if (Servers.localServer.LOGINSERVER) {
        if (wc.autoForward()) {
          Servers.sendWebCommandToAllServers(wctype, wc, wc.isEpicOnly());
        }
      }
      if ((WurmId.getOrigin(id) == Servers.localServer.id ? 1 : 0) == 0) {
        Server.getInstance().addWebCommand(wc);
      }
    }
  }
  
  public void setKingdomInfo(String intraServerPassword, int serverId, byte kingdomId, byte templateKingdom, String _name, String _password, String _chatName, String _suffix, String mottoOne, String mottoTwo, boolean acceptsPortals)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    Kingdom newInfo = new Kingdom(kingdomId, templateKingdom, _name, _password, _chatName, _suffix, mottoOne, mottoTwo, acceptsPortals);
    if (serverId != Servers.localServer.id) {
      Kingdoms.addKingdom(newInfo);
    }
    WcKingdomInfo wck = new WcKingdomInfo(WurmId.getNextWCCommandId(), true, kingdomId);
    wck.encode();
    Servers.sendWebCommandToAllServers((short)7, wck, wck.isEpicOnly());
  }
  
  public boolean kingdomExists(String intraServerPassword, int serverId, byte kingdomId, boolean exists)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    logger.log(Level.INFO, "serverId:" + serverId + " kingdom id " + kingdomId + " exists=" + exists);
    boolean result = Servers.kingdomExists(serverId, kingdomId, exists);
    if ((Servers.getServerWithId(serverId) != null) && (Servers.getServerWithId(serverId).name != null)) {
      logger.log(Level.INFO, Servers.getServerWithId(serverId).name + " kingdom id " + kingdomId + " exists=" + exists);
    } else if (Servers.getServerWithId(serverId) == null) {
      logger.log(Level.INFO, serverId + " server is null " + kingdomId + " exists=" + exists);
    } else {
      logger.log(Level.INFO, "Name for " + Servers.getServerWithId(serverId) + " server is null " + kingdomId + " exists=" + exists);
    }
    if (Servers.localServer.LOGINSERVER) {
      if (!exists)
      {
        if (!result)
        {
          Kingdom k = Kingdoms.getKingdomOrNull(kingdomId);
          boolean sendDelete = false;
          if (k != null) {
            if (k.isCustomKingdom())
            {
              k.delete();
              Kingdoms.removeKingdom(kingdomId);
              sendDelete = true;
            }
          }
        }
        else
        {
          Servers.sendKingdomExistsToAllServers(serverId, kingdomId, false);
        }
      }
      else {
        Servers.sendKingdomExistsToAllServers(serverId, kingdomId, true);
      }
    }
    return result;
  }
  
  public static void main(String[] args)
  {
    if ((args.length == 2) && (args[0].compareTo("ShutdownLive") == 0)) {
      try
      {
        WebInterfaceTest wit = new WebInterfaceTest();
        System.out.println("Shutting down ALL live servers!");
        wit.shutdownAll("Maintenance restart. Up to thirty minutes downtime.", Integer.parseInt(args[1]));
        System.out.println("I do hope this is what you wanted. All servers will be down in approximately " + args[1] + " seconds.");
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    } else if (args.length == 3) {
      try
      {
        WebInterfaceTest wit = new WebInterfaceTest();
        System.out.println("Attempting to shutdown server at " + args[0] + ", port " + args[1]);
        String[] userInfo = args[2].split(":");
        wit.shutDown(args[0], args[1], userInfo[0], userInfo[1]);
      }
      catch (Exception ex)
      {
        logger.log(Level.INFO, "failed to shut down localhost", ex);
        ex.printStackTrace();
      }
    } else {
      System.out.println("Usage:\nNo arguments - This message.\nShutdownLive <delay> - Shutsdown ALL LIVE SERVERS using the seconds provided as a delay\n<host> <port> <user>:<password> - Shutdown the specified server using your GM credentials.");
    }
  }
  
  private boolean validateAccount(String user, String password, byte power)
    throws IOException, Exception
  {
    PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(user));
    if (pinf == null) {
      return false;
    }
    pinf.load();
    if (pinf.getPower() <= power) {
      return false;
    }
    String pw = LoginHandler.encrypt(pinf.getName() + password);
    if (pw.equals(pinf.getPassword())) {
      return true;
    }
    return false;
  }
  
  private void interactiveShutdown()
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    int state = 0;
    boolean interactive = true;
    String user = "";
    String message = "Maintenance shutdown. Up to thirty minutes downtime. See the forums for more information: http://forum.wurmonline.com/";
    int delay = 1800;
    System.out.println("[Shutdown Servers]\n(Type 'quit' at any time to abort)");
    while (interactive) {
      try
      {
        switch (state)
        {
        case 0: 
          System.out.print("GM Name: ");
          user = br.readLine().trim();
          state = 1;
          break;
        case 1: 
          System.out.print("GM password: ");
          String password = br.readLine().trim();
          if (!validateAccount(user, password, (byte)4))
          {
            interactive = false;
            System.out.println("Invalid password or power level insufficient.");
            return;
          }
          state = 2;
          break;
        case 2: 
          System.out.print("Message: [default '" + message + "'] ");
          String in = br.readLine().trim();
          if (!in.isEmpty()) {
            message = in;
          }
          state = 3;
          in = "";
          break;
        case 3: 
          System.out.print("Delay: [default '" + delay + "']");
          String in = br.readLine().trim();
          if (!in.isEmpty()) {
            delay = Integer.valueOf(in).intValue();
          }
          state = 4;
        }
        String s = br.readLine();
        System.out.print("Enter Integer:");
        int i = Integer.parseInt(br.readLine());
      }
      catch (NumberFormatException nfe)
      {
        System.err.println("Invalid Format!");
      }
      catch (Exception localException) {}
    }
  }
  
  public void requestDemigod(String intraServerPassword, byte existingDeity, String existingDeityName)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    
    Player[] players = Players.getInstance().getPlayers();
    for (int x = 0; x < players.length; x++) {
      if ((players[x].getKingdomTemplateId() == Deities.getFavoredKingdom(existingDeity)) && (
        (players[x].getPower() == 0) || (Servers.localServer.testServer)))
      {
        MissionPerformer mp = MissionPerformed.getMissionPerformer(players[x].getWurmId());
        if (mp != null)
        {
          MissionPerformed[] perfs = mp.getAllMissionsPerformed();
          int numsForDeity = 0;
          logger.log(Level.INFO, "Checking if " + players[x].getName() + " can be elevated.");
          for (MissionPerformed mpf : perfs)
          {
            Mission m = mpf.getMission();
            if (m != null)
            {
              logger.log(Level.INFO, "Found a mission for " + existingDeityName);
              if (m.getCreatorType() == 2) {
                if (m.getOwnerId() == existingDeity) {
                  numsForDeity++;
                }
              }
            }
          }
          logger.log(Level.INFO, "Found " + numsForDeity + " missions for " + players[x].getName());
          if (Server.rand.nextInt(numsForDeity) > 2)
          {
            logger.log(Level.INFO, "Sending ascension to " + players[x].getName());
            AscensionQuestion asc = new AscensionQuestion(players[x], existingDeity, existingDeityName);
            asc.sendQuestion();
          }
        }
      }
    }
  }
  
  public String ascend(String intraServerPassword, int newId, String deityName, long wurmid, byte existingDeity, byte gender, byte newPower, float initialBStr, float initialBSta, float initialBCon, float initialML, float initialMS, float initialSS, float initialSD)
  {
    try
    {
      validateIntraServerPassword(intraServerPassword);
    }
    catch (AccessException e)
    {
      e.printStackTrace();
    }
    String toReturn = "";
    if (Servers.localServer.LOGINSERVER)
    {
      Deity deity = null;
      if (newPower == 2)
      {
        deity = Deities.ascend(newId, deityName, wurmid, gender, newPower, -1.0F, -1.0F);
        if (deity != null)
        {
          StringBuilder builder = new StringBuilder("You have now ascended! ");
          if (initialBStr < 30.0F) {
            builder.append("The other immortals will not fear your strength initially. ");
          } else if (initialBStr < 45.0F) {
            builder.append("You have acceptable strength as a demigod. ");
          } else if (initialBStr < 60.0F) {
            builder.append("Your strength and skills will impress other immortals. ");
          } else {
            builder.append("Your enormous strength will strike fear in other immortals. ");
          }
          if (initialBSta < 30.0F) {
            builder.append("You are not the most vital demigod around so you will have to watch your back in the beginning. ");
          } else if (initialBSta < 45.0F) {
            builder.append("Your vitality is acceptable and will earn respect. ");
          } else if (initialBSta < 60.0F) {
            builder.append("You have good vitality and can expect a bright future as immortal. ");
          } else {
            builder.append("Other immortals will envy your fantastic vitality and avoid confrontations with you. ");
          }
          if (deity.isHealer()) {
            builder.append("Your love and kindness will be a beacon for everyone to follow. ");
          } else if (deity.isHateGod()) {
            builder.append("Your true nature turns out to be based on rage and hate. ");
          }
          if (deity.isForestGod()) {
            builder.append("Love for trees and living things will bind your followers together. ");
          }
          if (deity.isMountainGod()) {
            builder.append("Your followers will look for you in high places and fear and adore you as they do the dragon. ");
          }
          if (deity.isWaterGod()) {
            builder.append("You will be considered the pathfinder and explorer of your kin. ");
          }
          HexMap.VALREI.addDemigod(deityName, deity.number, existingDeity, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD);
          
          toReturn = builder.toString();
        }
        else
        {
          return "Ouch, failed to save your demigod on the login server. Please contact administration";
        }
      }
      else if (newPower > 2)
      {
        String sgender = "He";
        String sposs = "his";
        if (gender == 1)
        {
          sgender = "She";
          sposs = "her";
        }
        Servers.ascend(newId, deityName, wurmid, existingDeity, gender, newPower, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD);
        
        HistoryManager.addHistory(deityName, "has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender
          .toLowerCase() + " will now forever partake in the hunts on Valrei!");
        Server.getInstance().broadCastSafe(deityName + " has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender
        
          .toLowerCase() + " will now forever partake in the hunts on Valrei!");
      }
    }
    else if (newPower > 2)
    {
      Deities.ascend(newId, deityName, wurmid, gender, newPower, -1.0F, -1.0F);
      String sgender = "He";
      String sposs = "his";
      if (gender == 1)
      {
        sgender = "She";
        sposs = "her";
      }
      HistoryManager.addHistory(deityName, "has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender
        .toLowerCase() + " will now forever partake in the hunts on Valrei!");
      Server.getInstance().broadCastSafe(deityName + " has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender
      
        .toLowerCase() + " will now forever partake in the hunts on Valrei!");
    }
    return toReturn;
  }
  
  static final int[] emptyIntZero = { 0, 0 };
  
  public final int[] getPremTimeSilvers(String intraServerPassword, long wurmId)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
    if (info != null) {
      try
      {
        if (!info.loaded) {
          info.load();
        }
        if (info.getPaymentExpire() > 0L) {
          if (info.awards != null) {
            return new int[] {info.awards.getMonthsPaidEver(), info.awards.getSilversPaidEver() };
          }
        }
      }
      catch (IOException localIOException) {}
    }
    return emptyIntZero;
  }
  
  public void awardPlayer(String intraServerPassword, long wurmid, String name, int days, int months)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    Server.addPendingAward(new PendingAward(wurmid, name, days, months));
  }
  
  public boolean requestDeityMove(String intraServerPassword, int deityNum, int desiredHex, String guide)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (Servers.localServer.LOGINSERVER)
    {
      EpicEntity entity = HexMap.VALREI.getEntity(deityNum);
      if (entity != null)
      {
        logger.log(Level.INFO, "Requesting move for " + entity);
        MapHex mh = HexMap.VALREI.getMapHex(desiredHex);
        if (mh != null)
        {
          entity.setNextTargetHex(desiredHex);
          entity.broadCastWithName(" was guided by " + guide + " towards " + mh.getName() + ".");
          entity.sendEntityData();
          return true;
        }
        logger.log(Level.INFO, "No hex for " + desiredHex);
      }
      else
      {
        logger.log(Level.INFO, "Requesting move for nonexistant " + deityNum);
      }
    }
    return false;
  }
  
  private void validateIntraServerPassword(String intraServerPassword)
    throws AccessException
  {
    if (!Servers.localServer.INTRASERVERPASSWORD.equals(intraServerPassword)) {
      throw new AccessException("Access denied.");
    }
  }
  
  public boolean isFeatureEnabled(String intraServerPassword, int aFeatureId)
    throws RemoteException
  {
    validateIntraServerPassword(intraServerPassword);
    if (logger.isLoggable(Level.FINER)) {
      logger.finer(getRemoteClientDetails() + " isFeatureEnabled " + aFeatureId);
    }
    return Features.Feature.isFeatureEnabled(aFeatureId);
  }
  
  public boolean setPlayerFlag(String intraServerPassword, long wurmid, int flag, boolean set)
    throws RemoteException
  {
    return false;
  }
  
  public boolean setPlayerFlag(long wurmid, int flag, boolean set)
  {
    PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
    if (pinf != null)
    {
      pinf.setFlag(flag, set);
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WebInterfaceImpl.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */