package com.wurmonline.server.kingdom;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.Zones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class King
  implements MiscConstants, TimeConstants
{
  private static final String CREATE_KING_ERA = "insert into KING_ERA ( ERA,KINGDOM,KINGDOMNAME, KINGID,KINGSNAME,GENDER,STARTTIME,STARTWURMTIME,STARTLANDPERCENT, CURRENTLANDPERCENT,      NEXTCHALLENGE,CURRENT) VALUES (?,?,?,?,?,?,?,?,?,?,  ?,1)";
  private static final String UPDATE_KING_ERA = "UPDATE KING_ERA SET KINGSNAME=?,GENDER=?,ENDTIME=?,ENDWURMTIME=?, CURRENTLANDPERCENT=?, CAPITAL=?, CURRENT=?,KINGDOM=? WHERE ERA=?";
  private static final String UPDATE_LEVELSKILLED = "UPDATE KING_ERA SET LEVELSKILLED=? WHERE ERA=?";
  private static final String UPDATE_LEVELSLOST = "UPDATE KING_ERA SET LEVELSLOST=? WHERE ERA=?";
  private static final String UPDATE_APPOINTMENTS = "UPDATE KING_ERA SET APPOINTMENTS=? WHERE ERA=?";
  private static final String GET_ALL_KING_ERA = "select * FROM KING_ERA";
  private static final String UPDATE_CHALLENGES = "UPDATE KING_ERA SET NEXTCHALLENGE=?,DECLINEDCHALLENGES=?,ACCEPTDATE=?,CHALLENGEDATE=? WHERE ERA=?";
  public String kingdomName = "unknown kingdom";
  private static Logger logger = Logger.getLogger(King.class.getName());
  public static int currentEra = 0;
  public int era = 0;
  public String kingName = "";
  public long kingid = -10L;
  private long startTime = 0L;
  private long endTime = 0L;
  public long startWurmTime = 0L;
  public long endWurmTime = 0L;
  public float startLand = 0.0F;
  public float currentLand = 0.0F;
  public int appointed = 0;
  public int levelskilled = 0;
  public int levelslost = 0;
  public boolean current = false;
  public byte kingdom = 0;
  private long nextChallenge = 0L;
  private int declinedChallenges = 0;
  private long challengeDate = 0L;
  private long acceptDate = 0L;
  public byte gender = 0;
  public String capital = "";
  private String rulerMaleTitle = "Grand Prince";
  private String rulerFemaleTitle = "Grand Princess";
  private static King kingJenn = null;
  private static King kingMolRehan = null;
  private static King kingHots = null;
  private Appointments appointments = null;
  public static final Map<Integer, King> eras = new HashMap();
  public static final Map<Long, Integer> challenges = new HashMap();
  private static final int challengesRequired = Servers.isThisATestServer() ? 3 : 10;
  private static final int votesRequired = Servers.isThisATestServer() ? 1 : 10;
  private static final Set<King> kings = new HashSet();
  private static final long challengeFactor = Servers.isThisATestServer() ? 60000L : 604800000L;
  public static final float landPercentRequiredForBonus = 2.0F;
  
  private King()
  {
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("Creating new King");
    }
  }
  
  private static void addKing(King king)
  {
    eras.put(Integer.valueOf(king.era), king);
    logger.log(Level.INFO, "Loading kings, adding " + king.kingName);
    if (king.current)
    {
      if (king.kingdom == 1)
      {
        logger.log(Level.INFO, "Setting current jenn king: " + king.kingName);
        kingJenn = king;
      }
      else if (king.kingdom == 2)
      {
        logger.log(Level.INFO, "Setting current mol rehan king: " + king.kingName);
        kingMolRehan = king;
      }
      else if (king.kingdom == 3)
      {
        logger.log(Level.INFO, "Setting current hots king: " + king.kingName);
        kingHots = king;
      }
      kings.add(king);
    }
  }
  
  public static King getKing(byte _kingdom)
  {
    if (_kingdom == 1) {
      return kingJenn;
    }
    if (_kingdom == 2) {
      return kingMolRehan;
    }
    if (_kingdom == 3) {
      return kingHots;
    }
    for (King k : kings) {
      if ((k.kingdom == _kingdom) && (k.current)) {
        return k;
      }
    }
    return null;
  }
  
  public static boolean isKing(long wurmid, byte kingdom)
  {
    King k = getKing(kingdom);
    if (k != null) {
      return k.kingid == wurmid;
    }
    return false;
  }
  
  public static void purgeKing(byte _kingdom)
  {
    Zones.calculateZones(true);
    if (_kingdom == 1)
    {
      if (kingJenn != null)
      {
        kingJenn.currentLand = Zones.getPercentLandForKingdom(_kingdom);
        switchCurrent(kingJenn);
      }
      kingJenn = null;
      
      new Appointments(-1, (byte)1, true);
    }
    else if (_kingdom == 2)
    {
      if (kingMolRehan != null)
      {
        kingMolRehan.currentLand = Zones.getPercentLandForKingdom(_kingdom);
        switchCurrent(kingMolRehan);
      }
      kingMolRehan = null;
      
      new Appointments(-2, (byte)2, true);
    }
    else if (_kingdom == 3)
    {
      if (kingHots != null)
      {
        kingHots.currentLand = Zones.getPercentLandForKingdom(_kingdom);
        switchCurrent(kingHots);
      }
      kingHots = null;
      
      new Appointments(-3, (byte)3, true);
    }
    else
    {
      King[] kingarr = getKings();
      for (King k : kingarr) {
        if (k.kingdom == _kingdom)
        {
          k.currentLand = Zones.getPercentLandForKingdom(_kingdom);
          switchCurrent(k);
        }
      }
    }
  }
  
  public static void pollKings()
  {
    King[] kingarr = getKings();
    for (King k : kingarr) {
      k.poll();
    }
  }
  
  public static final King[] getKings()
  {
    return (King[])kings.toArray(new King[kings.size()]);
  }
  
  private void poll()
  {
    if (System.currentTimeMillis() - this.appointments.lastChecked > 604800000L)
    {
      this.appointments.resetAppointments(this.kingdom);
      
      Kingdom k = Kingdoms.getKingdom(this.kingdom);
      if (k.isCustomKingdom())
      {
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.kingid);
        if (pinf != null) {
          if ((System.currentTimeMillis() - pinf.lastLogout > 2419200000L) && 
            (System.currentTimeMillis() - pinf.lastLogin > 2419200000L))
          {
            Items.deleteRoyalItemForKingdom(this.kingdom, true, false);
            logger.log(Level.INFO, this.kingName + " has not logged in for a month. A new king for " + this.kingdomName + " will be found.");
            
            purgeKing(this.kingdom);
          }
        }
      }
    }
    else
    {
      PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.kingid);
      if (pinf != null) {
        if (pinf.currentServer == Servers.localServer.id) {
          if (!pinf.isPaying())
          {
            Kingdom k = Kingdoms.getKingdom(this.kingdom);
            if (!k.isCustomKingdom())
            {
              Items.deleteRoyalItemForKingdom(this.kingdom, true, true);
              logger.log(Level.INFO, this.kingName + " is no longer premium. Deleted the regalia.");
              purgeKing(this.kingdom);
              return;
            }
          }
        }
      }
      Zones.calculateZones(false);
      float oldland = this.currentLand;
      this.currentLand = Zones.getPercentLandForKingdom(this.kingdom);
      if (oldland != this.currentLand)
      {
        logger.log(Level.INFO, "Saving " + this.kingName + " because new land is " + this.currentLand + " compared to " + oldland);
        save();
      }
      if (hasFailedToRespondToChallenge())
      {
        HistoryManager.addHistory(this.kingName, "decided not to respond to a challenge.");
        Server.getInstance().broadCastAlert(this.kingName + " has decided not to respond to a challenge.");
        
        logger.log(Level.INFO, this.kingName + " did not respond to a challenge.");
        setChallengeDeclined();
        if (hasFailedAllChallenges())
        {
          HistoryManager.addHistory(this.kingName, "may now be voted away from the throne within one week at the duelling stone.");
          
          Server.getInstance().broadCastAlert(
            getFullTitle() + " may now be voted away from the throne within one week at the duelling stone.");
          
          logger.log(Level.INFO, this.kingName + " may now be voted away.");
        }
      }
      if (hasFailedAllChallenges()) {
        if (getVotesNeeded() == 0)
        {
          removeByVote();
        }
        else if (getNextChallenge() < System.currentTimeMillis())
        {
          PlayerInfoFactory.resetVotesForKingdom(this.kingdom);
          this.declinedChallenges = 0;
          updateChallenges();
          HistoryManager.addHistory(this.kingName, "was not voted away from the throne this time. The " + 
            getRulerTitle() + " remains on the throne of " + this.kingdomName + ".");
          
          Server.getInstance().broadCastNormal(this.kingName + " was not voted away from the throne this time. The " + 
            getRulerTitle() + " remains on the throne of " + this.kingdomName + ".");
          
          logger.log(Level.INFO, this.kingName + " may no longer be voted away.");
        }
      }
      if (this.acceptDate > 0L) {
        if (System.currentTimeMillis() > this.acceptDate) {
          try
          {
            Player p = Players.getInstance().getPlayer(this.kingid);
            if (p.isInOwnDuelRing())
            {
              if (Servers.isThisATestServer())
              {
                if (System.currentTimeMillis() - getChallengeAcceptedDate() > 300000L) {
                  passedChallenge();
                }
              }
              else if (System.currentTimeMillis() - this.acceptDate > 1800000L) {
                passedChallenge();
              }
              p.getCommunicator().sendAlertServerMessage("Unseen eyes watch you.");
            }
            else
            {
              setFailedChallenge();
            }
          }
          catch (NoSuchPlayerException nsp)
          {
            setFailedChallenge();
          }
        }
      }
    }
  }
  
  public final void removeByVote()
  {
    HistoryManager.addHistory(this.kingName, "has been voted away from the throne by the people of " + this.kingdomName + "!");
    Server.getInstance().broadCastAlert(
      getFullTitle() + " has been voted away from the throne by the people of " + this.kingdomName + "!");
    Items.deleteRoyalItemForKingdom(this.kingdom, true, true);
    purgeKing(this.kingdom);
    logger.log(Level.INFO, this.kingName + " has been voted away.");
  }
  
  public final void removeByFailChallenge()
  {
    HistoryManager.addHistory(this.kingName, "has failed the challenge by the people of " + this.kingdomName + "!");
    Server.getInstance()
      .broadCastNormal(getFullTitle() + " has failed the challenge by the people of " + this.kingdomName + "!");
    Items.deleteRoyalItemForKingdom(this.kingdom, true, true);
    purgeKing(this.kingdom);
    logger.log(Level.INFO, this.kingName + " has failed the challenge.");
  }
  
  private static void setRulerName(King king)
  {
    king.rulerMaleTitle = getRulerTitle(true, king.kingdom);
    king.rulerFemaleTitle = getRulerTitle(false, king.kingdom);
  }
  
  public String getRulerTitle()
  {
    if (this.gender == 1) {
      return this.rulerFemaleTitle;
    }
    return this.rulerMaleTitle;
  }
  
  public static String getRulerTitle(boolean male, byte kingdom)
  {
    if (kingdom == 1)
    {
      if (male) {
        return "Grand Prince";
      }
      return "Grand Princess";
    }
    if (kingdom == 2)
    {
      if (male) {
        return "Chancellor";
      }
      return "Chancellor";
    }
    if (kingdom == 3)
    {
      if (male) {
        return "Emperor";
      }
      return "Empress";
    }
    if (male) {
      return "Chief";
    }
    return "Chieftain";
  }
  
  public static void loadAllEra()
  {
    logger.log(Level.INFO, "Loading all kingdom eras.");
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("select * FROM KING_ERA");
      rs = ps.executeQuery();
      while (rs.next())
      {
        King k = new King();
        k.era = rs.getInt("ERA");
        k.kingdom = rs.getByte("KINGDOM");
        k.current = rs.getBoolean("CURRENT");
        if (k.era > currentEra) {
          currentEra = k.era;
        }
        k.kingName = rs.getString("KINGSNAME");
        k.gender = rs.getByte("GENDER");
        k.startLand = rs.getFloat("STARTLANDPERCENT");
        k.startTime = rs.getLong("STARTTIME");
        k.endTime = rs.getLong("ENDTIME");
        k.startWurmTime = rs.getLong("STARTWURMTIME");
        k.endWurmTime = rs.getLong("ENDWURMTIME");
        k.currentLand = rs.getFloat("CURRENTLANDPERCENT");
        k.appointed = rs.getInt("APPOINTMENTS");
        k.levelskilled = rs.getInt("LEVELSKILLED");
        k.levelslost = rs.getInt("LEVELSLOST");
        k.capital = rs.getString("CAPITAL");
        k.kingid = rs.getLong("KINGID");
        k.appointed = rs.getInt("APPOINTMENTS");
        k.nextChallenge = rs.getLong("NEXTCHALLENGE");
        k.declinedChallenges = rs.getInt("DECLINEDCHALLENGES");
        k.acceptDate = rs.getLong("ACCEPTDATE");
        k.challengeDate = rs.getLong("CHALLENGEDATE");
        k.kingdomName = rs.getString("KINGDOMNAME");
        byte template = k.kingdom;
        
        Kingdom kingd = Kingdoms.getKingdom(k.kingdom);
        if (kingd != null)
        {
          template = kingd.getTemplate();
          logger.log(Level.INFO, "Template for " + k.kingdom + "=" + template + " (" + kingd.getId() + ")");
        }
        k.appointments = new Appointments(k.era, template, k.current);
        setRulerName(k);
        addKing(k);
      }
    }
    catch (SQLException sqex)
    {
      long end;
      logger.log(Level.WARNING, "Failed to load kingdom eras: " + sqex.getMessage(), sqex);
    }
    finally
    {
      long end;
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      
      long end = System.nanoTime();
      logger.info("Loaded kingdom eras from database took " + (float)(end - start) / 1000000.0F + " ms");
    }
    if (Appointments.jenn == null) {
      new Appointments(-1, (byte)1, true);
    }
    if (Appointments.hots == null) {
      new Appointments(-3, (byte)3, true);
    }
    if (Appointments.molr == null) {
      new Appointments(-2, (byte)2, true);
    }
    if (Appointments.none == null) {
      new Appointments(-5, (byte)0, true);
    }
  }
  
  public static void setToNoKingdom(byte oldKingdom)
  {
    for (King k : eras.values()) {
      if (k.kingdom == oldKingdom)
      {
        k.kingdom = 0;
        k.save();
      }
    }
    for (King k : kings) {
      if (k.kingdom == oldKingdom)
      {
        k.kingdom = 0;
        k.save();
      }
    }
  }
  
  public static Appointments getCurrentAppointments(byte kingdom)
  {
    King k = getKing(kingdom);
    if ((k != null) && (k.current)) {
      return Appointments.getAppointments(k.era);
    }
    Kingdom kingd = Kingdoms.getKingdom(kingdom);
    if (kingd != null) {
      return Appointments.getCurrentAppointments(kingd.getTemplate());
    }
    return null;
  }
  
  public void abdicate(boolean isOnSurface, boolean destroyItems)
  {
    Items.deleteRoyalItemForKingdom(this.kingdom, isOnSurface, destroyItems);
    purgeKing(this.kingdom);
  }
  
  public static King createKing(byte _kingdom, String kingname, long kingwurmid, byte kinggender)
  {
    King k = new King();
    currentEra += 1;
    k.era = currentEra;
    k.kingdom = _kingdom;
    k.kingid = kingwurmid;
    k.kingName = kingname;
    k.gender = kinggender;
    k.startTime = System.currentTimeMillis();
    k.startWurmTime = WurmCalendar.currentTime;
    k.nextChallenge = (System.currentTimeMillis() + challengeFactor);
    k.kingdomName = Kingdoms.getNameFor(_kingdom);
    Zones.calculateZones(true);
    k.startLand = Zones.getPercentLandForKingdom(_kingdom);
    boolean foundCapital = false;
    try
    {
      Player p = Players.getInstance().getPlayer(kingwurmid);
      p.achievement(321);
      if (p.getCitizenVillage() != null)
      {
        foundCapital = true;
        k.setCapital(p.getCitizenVillage().getName(), true);
      }
    }
    catch (NoSuchPlayerException localNoSuchPlayerException) {}
    if (_kingdom == 1)
    {
      if (kingJenn != null)
      {
        kingJenn.currentLand = Zones.getPercentLandForKingdom(_kingdom);
        switchCurrent(kingJenn);
      }
      kingJenn = k;
    }
    else if (_kingdom == 2)
    {
      if (kingMolRehan != null)
      {
        kingMolRehan.currentLand = Zones.getPercentLandForKingdom(_kingdom);
        switchCurrent(kingMolRehan);
      }
      kingMolRehan = k;
    }
    else if (_kingdom == 3)
    {
      if (kingHots != null)
      {
        kingHots.currentLand = Zones.getPercentLandForKingdom(_kingdom);
        switchCurrent(kingHots);
      }
      kingHots = k;
    }
    else
    {
      King oldKing = getKing(_kingdom);
      if (oldKing != null)
      {
        oldKing.currentLand = Zones.getPercentLandForKingdom(_kingdom);
        logger.log(Level.INFO, "Found old king " + oldKing.kingName + " when creating new.");
        switchCurrent(oldKing);
        if (!foundCapital) {
          k.setCapital(oldKing.capital, true);
        }
      }
    }
    k.currentLand = k.startLand;
    k.current = true;
    k.create();
    
    byte template = k.kingdom;
    Kingdom kingd = Kingdoms.getKingdomOrNull(k.kingdom);
    if (kingd != null)
    {
      template = kingd.getTemplate();
      logger.log(Level.INFO, "Using " + Kingdoms.getNameFor(template) + " for " + kingd.getName());
    }
    k.appointments = new Appointments(k.era, template, k.current);
    setRulerName(k);
    addKing(k);
    HistoryManager.addHistory(k.kingName, "is appointed new " + k.getRulerTitle() + " of " + k.kingdomName);
    Items.transferRegaliaForKingdom(_kingdom, kingwurmid);
    pollKings();
    return k;
  }
  
  private static void switchCurrent(King oldking)
  {
    oldking.endTime = System.currentTimeMillis();
    oldking.endWurmTime = WurmCalendar.currentTime;
    oldking.current = false;
    HistoryManager.addHistory(oldking.kingName, "no longer is the " + oldking.getRulerTitle() + " of " + oldking.kingdomName);
    
    Server.getInstance().broadCastNormal(oldking.kingName + " no longer is the " + oldking
      .getRulerTitle() + " of " + oldking.kingdomName);
    oldking.save();
    
    kings.remove(oldking);
    
    PlayerInfoFactory.resetVotesForKingdom(oldking.kingdom);
  }
  
  private void create()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("insert into KING_ERA ( ERA,KINGDOM,KINGDOMNAME, KINGID,KINGSNAME,GENDER,STARTTIME,STARTWURMTIME,STARTLANDPERCENT, CURRENTLANDPERCENT,      NEXTCHALLENGE,CURRENT) VALUES (?,?,?,?,?,?,?,?,?,?,  ?,1)");
      ps.setInt(1, this.era);
      ps.setByte(2, this.kingdom);
      ps.setString(3, this.kingdomName);
      ps.setLong(4, this.kingid);
      ps.setString(5, this.kingName);
      ps.setByte(6, this.gender);
      ps.setLong(7, this.startTime);
      ps.setLong(8, this.startWurmTime);
      ps.setFloat(9, this.startLand);
      ps.setFloat(10, this.currentLand);
      ps.setLong(11, this.nextChallenge);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to create kingdom for era " + this.era + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  private final void save()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE KING_ERA SET KINGSNAME=?,GENDER=?,ENDTIME=?,ENDWURMTIME=?, CURRENTLANDPERCENT=?, CAPITAL=?, CURRENT=?,KINGDOM=? WHERE ERA=?");
      ps.setString(1, this.kingName);
      ps.setByte(2, this.gender);
      ps.setLong(3, this.endTime);
      ps.setLong(4, this.endWurmTime);
      ps.setFloat(5, this.currentLand);
      ps.setString(6, this.capital);
      ps.setBoolean(7, this.current);
      ps.setByte(8, this.kingdom);
      ps.setInt(9, this.era);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to save kingdom for era " + this.era + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  long lastCapital = System.currentTimeMillis();
  
  public final boolean setCapital(String newcapital, boolean forced)
  {
    if ((System.currentTimeMillis() - this.lastCapital > 21600000L) || (forced) || (Servers.isThisATestServer()))
    {
      this.capital = newcapital;
      this.lastCapital = System.currentTimeMillis();
      save();
      return true;
    }
    return false;
  }
  
  public final void setGender(byte newgender)
  {
    this.gender = newgender;
    save();
  }
  
  public final void addAppointment(Appointment app)
  {
    this.appointed += app.getLevel();
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE KING_ERA SET APPOINTMENTS=? WHERE ERA=?");
      ps.setInt(1, this.appointed);
      ps.setInt(2, this.era);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to update appointed: " + this.appointed + " for era " + this.era + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public final void resetNextChallenge(long nextTime)
  {
    this.nextChallenge = nextTime;
    challenges.clear();
    updateChallenges();
  }
  
  public final long getNextChallenge()
  {
    return this.nextChallenge;
  }
  
  public final void setChallengeDate()
  {
    this.challengeDate = System.currentTimeMillis();
    updateChallenges();
  }
  
  public final long getChallengeDate()
  {
    return this.challengeDate;
  }
  
  public final void setChallengeAccepted(long date)
  {
    this.acceptDate = date;
    this.challengeDate = 0L;
    resetNextChallenge(this.acceptDate + challengeFactor * (3 - this.declinedChallenges));
    updateChallenges();
  }
  
  public final void setChallengeDeclined()
  {
    resetNextChallenge(System.currentTimeMillis() + challengeFactor);
    this.challengeDate = 0L;
    this.declinedChallenges += 1;
    updateChallenges();
  }
  
  public final long getChallengeAcceptedDate()
  {
    return this.acceptDate;
  }
  
  public final int getDeclinedChallengesNumber()
  {
    return this.declinedChallenges;
  }
  
  public final void passedChallenge()
  {
    HistoryManager.addHistory(this.kingName, "passed the challenge put forth by the people of " + this.kingdomName + "!");
    Server.getInstance().broadCastNormal(
      getFullTitle() + " passed the challenge put forth by the people of " + this.kingdomName + "!");
    this.acceptDate = 0L;
    this.challengeDate = 0L;
    updateChallenges();
  }
  
  public final void setFailedChallenge()
  {
    if (!hasFailedAllChallenges())
    {
      HistoryManager.addHistory(this.kingName, "failed the challenge put forth by the people of " + this.kingdomName + " and may now be voted away from the throne.");
      
      Message mess = new Message(null, (byte)10, Kingdoms.getChatNameFor(this.kingdom), "<" + this.kingName + "> has failed the challenge and may now be voted away from the throne.");
      
      Player[] playarr = Players.getInstance().getPlayers();
      
      byte windowKingdom = this.kingdom;
      for (Player lElement : playarr) {
        if ((windowKingdom == lElement.getKingdomId()) || (lElement.getPower() > 0)) {
          lElement.getCommunicator().sendMessage(mess);
        }
      }
      resetNextChallenge(System.currentTimeMillis() + challengeFactor);
      this.acceptDate = 0L;
      this.challengeDate = 0L;
      this.declinedChallenges = 3;
      updateChallenges();
    }
  }
  
  public final boolean mayBeChallenged()
  {
    return (System.currentTimeMillis() - this.challengeDate > challengeFactor) && (System.currentTimeMillis() > getNextChallenge());
  }
  
  public final boolean hasFailedToRespondToChallenge()
  {
    return (this.challengeDate != 0L) && (System.currentTimeMillis() - this.challengeDate > challengeFactor);
  }
  
  public final boolean hasFailedAllChallenges()
  {
    return this.declinedChallenges >= 3;
  }
  
  public final int getVotes()
  {
    return PlayerInfoFactory.getVotesForKingdom(this.kingdom);
  }
  
  public final int getVotesNeeded()
  {
    return Math.max(0, votesRequired - getVotes());
  }
  
  public final boolean hasBeenChallenged()
  {
    int challengesCast = 0;
    for (Integer i : challenges.values()) {
      if (i.intValue() == this.era) {
        challengesCast++;
      }
    }
    return challengesCast >= challengesRequired;
  }
  
  public final boolean addChallenge(Creature challenger)
  {
    if (challenger.getKingdomId() == this.kingdom)
    {
      if (Servers.isThisATestServer())
      {
        boolean wasChallenged = hasBeenChallenged();
        challenges.put(Long.valueOf(Server.rand.nextLong()), Integer.valueOf(this.era));
        if (hasBeenChallenged() != wasChallenged) {
          setChallengeDate();
        }
        return true;
      }
      if (challenges.containsKey(Long.valueOf(challenger.getWurmId()))) {
        return false;
      }
      boolean wasChallenged = hasBeenChallenged();
      challenges.put(Long.valueOf(challenger.getWurmId()), Integer.valueOf(this.era));
      if (hasBeenChallenged() != wasChallenged) {
        setChallengeDate();
      }
      return true;
    }
    return false;
  }
  
  public final int getChallengeSize()
  {
    return challenges.size();
  }
  
  public void updateChallenges()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE KING_ERA SET NEXTCHALLENGE=?,DECLINEDCHALLENGES=?,ACCEPTDATE=?,CHALLENGEDATE=? WHERE ERA=?");
      ps.setLong(1, this.nextChallenge);
      ps.setLong(2, this.declinedChallenges);
      ps.setLong(3, this.acceptDate);
      ps.setLong(4, this.challengeDate);
      ps.setInt(5, this.era);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to update challenges: for era " + this.era + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void addLevelsLost(int lost)
  {
    this.levelslost += lost;
    logger.log(Level.INFO, this.kingName + " adding " + lost + " levels lost to " + this.levelslost + " for kingdom " + 
    
      Kingdoms.getChatNameFor(this.kingdom) + " era " + this.era);
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE KING_ERA SET LEVELSLOST=? WHERE ERA=?");
      ps.setInt(1, this.levelslost);
      ps.setInt(2, this.era);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to update for era " + this.era + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public void addLevelsKilled(int killed, String name, int worth)
  {
    this.levelskilled += killed;
    logger.log(Level.INFO, this.kingName + " killed " + name + " worth " + worth + " adding " + killed + " levels killed to " + this.levelskilled + " for kingdom " + 
    
      Kingdoms.getChatNameFor(this.kingdom) + " era " + this.era);
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("UPDATE KING_ERA SET LEVELSKILLED=? WHERE ERA=?");
      ps.setInt(1, this.levelskilled);
      ps.setInt(2, this.era);
      ps.executeUpdate();
    }
    catch (SQLException sqex)
    {
      logger.log(Level.WARNING, "Failed to update for era " + this.era + sqex.getMessage(), sqex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public float getLandSuccessPercent()
  {
    if (this.startLand == 0.0F) {
      this.startLand = this.currentLand;
    }
    if (this.startLand == 0.0F) {
      return 100.0F;
    }
    return this.currentLand / this.startLand * 100.0F;
  }
  
  public float getAppointedSuccessPercent()
  {
    if ((this.levelskilled == 0) && (this.levelslost == 0)) {
      return 100.0F;
    }
    if ((this.levelslost < 20) && (this.levelskilled < 20)) {
      return 100.0F;
    }
    if ((this.levelslost == 0) && (this.levelskilled != 0)) {
      return 100 + this.levelskilled;
    }
    if ((this.levelslost != 0) && (this.levelskilled == 0)) {
      return 100 - this.levelslost;
    }
    return this.levelskilled / this.levelslost * 100.0F;
  }
  
  private String getSuccessTitle()
  {
    float successPercentSinceStart = getLandSuccessPercent();
    if (successPercentSinceStart < 100.0F)
    {
      if (successPercentSinceStart < 10.0F) {
        return "the Traitor";
      }
      if (successPercentSinceStart < 20.0F) {
        return "the Tragic";
      }
      if (successPercentSinceStart < 30.0F) {
        return "the Joke";
      }
      if (successPercentSinceStart < 50.0F) {
        return "the Imbecile";
      }
      if (successPercentSinceStart < 70.0F) {
        return "the Failed";
      }
      if (successPercentSinceStart < 90.0F) {
        return "the Stupid";
      }
      return "the Acceptable";
    }
    if (successPercentSinceStart < 110.0F) {
      return "the Acceptable";
    }
    if (successPercentSinceStart < 120.0F) {
      return "the Lucky";
    }
    if (successPercentSinceStart < 130.0F) {
      return "the Conquering";
    }
    if (successPercentSinceStart < 140.0F) {
      return "the Strong";
    }
    if (successPercentSinceStart < 150.0F) {
      return "the Impressive";
    }
    if (successPercentSinceStart < 180.0F) {
      return "the Great";
    }
    if (successPercentSinceStart < 200.0F) {
      return "the Fantastic";
    }
    if (successPercentSinceStart < 400.0F) {
      return "the Magnificent";
    }
    return "the Divine";
  }
  
  private String getAppointmentSuccess()
  {
    float successPercentSinceStart = getAppointedSuccessPercent();
    if (successPercentSinceStart < 110.0F) {
      return "";
    }
    if (successPercentSinceStart < 120.0F) {
      return "";
    }
    if (successPercentSinceStart < 150.0F) {
      return " Warrior";
    }
    if (successPercentSinceStart < 180.0F) {
      return " Defender";
    }
    if (successPercentSinceStart < 200.0F) {
      return " Statesman";
    }
    if (successPercentSinceStart < 400.0F) {
      return " Saviour";
    }
    return " Holiness";
  }
  
  public String getFullTitle()
  {
    return getRulerTitle() + " " + this.kingName + " " + getSuccessTitle() + getAppointmentSuccess();
  }
  
  public static boolean isOfficial(int officeId, long wurmid, byte kingdom)
  {
    King tempKing = getKing(kingdom);
    if (tempKing != null) {
      if (tempKing.appointments != null) {
        return tempKing.appointments.officials[(officeId - 1500)] == wurmid;
      }
    }
    return false;
  }
  
  public static Creature getOfficial(byte _kingdom, int officeId)
  {
    King tempKing = getKing(_kingdom);
    if (tempKing != null) {
      if (tempKing.appointments != null)
      {
        long wurmid = tempKing.appointments.officials[(officeId - 1500)];
        try
        {
          return Players.getInstance().getPlayer(wurmid);
        }
        catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\kingdom\King.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */