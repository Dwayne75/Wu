package com.wurmonline.server.statistics;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChallengeSummary
  implements MiscConstants
{
  private static final String loadAllScores = "SELECT * FROM CHALLENGE";
  private static final String insertScore = "INSERT INTO CHALLENGE(LASTUPDATED,WURMID,ROUND,TYPE,POINTS,LASTPOINTS) VALUES (?,?,?,?,?,?)";
  private static final String updateScore = "UPDATE CHALLENGE SET LASTUPDATED=?,POINTS=?,LASTPOINTS=? WHERE WURMID=? AND ROUND=? AND TYPE=?";
  private static final Logger logger = Logger.getLogger(ChallengeSummary.class.getName());
  private static final Map<Long, ChallengeSummary> allScores = new ConcurrentHashMap();
  private static boolean isDirty = false;
  private final long wid;
  private final String name;
  private static final ChallengeScore[] topScores = new ChallengeScore[ChallengePointEnum.ChallengePoint.getTypes().length];
  private static final String[] topScorers = new String[ChallengePointEnum.ChallengePoint.getTypes().length];
  private final ConcurrentHashMap<Integer, ChallengeRound> privateRounds = new ConcurrentHashMap();
  static final String start = "<TABLE id=\"gameDataTable\">\n\t\t<TR class=\"gameDataTopTenTR\">\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Points</TH>\n\t\t\t<TH>Last points</TH>\n\t\t\t<TH>Date</TH>\n\t\t</TR>\n\t\t";
  private static final String header = "<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
  private static final String rootdir = "/var/www/challenge/";
  private static String headerFilename = "/var/www/challenge/main" + ChallengePointEnum.ChallengeScenario.current.getNum() + ".html";
  private static final String mainHeader = "<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
  static final String headerStart = "<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Challenge</TH>\n\t\t\t<TH>Leader</TH>\n\t\t\t<TH>Points</TH>\n\t\t\t<TH>Last Points</TH>\n\t\t\t<TH>Date</TH>\n\t\t</TR>\n\t\t";
  private static final String tablefooter = "</TABLE>\n\n";
  private static final String pagefooter = "</BODY>\n</HTML>";
  private boolean fileExists = false;
  private String filename = "";
  static boolean writing = false;
  static final String hscpStart = "<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Rank</TH>\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Points</TH>\n\t\t\t<TH>Last Points</TH>\n\t\t\t<TH>Date</TH>\n\t\t</TR>\n\t\t";
  
  public ChallengeSummary(long wurmId, String playerName)
  {
    this.wid = wurmId;
    this.name = playerName;
  }
  
  public static final void addToScore(PlayerInfo pid, int scoreType, float added)
  {
    if (pid.getPower() > 0) {
      return;
    }
    if (added != 0.0F)
    {
      boolean newScore = false;
      ChallengeSummary summary = getSummary(pid.wurmId);
      if (summary == null)
      {
        summary = new ChallengeSummary(pid.wurmId, pid.getName());
        addChallengeSummary(summary);
      }
      ChallengeRound round = summary.getPrivateChallengeRound(ChallengePointEnum.ChallengeScenario.current.getNum());
      if (round == null)
      {
        round = new ChallengeRound(ChallengePointEnum.ChallengeScenario.current.getNum());
        summary.addPrivateChallengeRound(round);
      }
      ChallengeScore scoreObj = round.getCurrentScoreForType(scoreType);
      if (scoreObj == null)
      {
        scoreObj = new ChallengeScore(scoreType, added, System.currentTimeMillis(), added);
        newScore = true;
      }
      else
      {
        scoreObj.setPoints(scoreObj.getPoints() + added);
        scoreObj.setLastPoints(added);
      }
      round.setScore(scoreObj);
      ChallengePointEnum.ChallengePoint.fromInt(scoreType).setDirty(true);
      if (newScore) {
        createScore(pid.wurmId, ChallengePointEnum.ChallengeScenario.current.getNum(), scoreObj);
      } else {
        updateScore(pid.wurmId, ChallengePointEnum.ChallengeScenario.current.getNum(), scoreObj);
      }
      if (checkIfTopScore(scoreObj, pid)) {
        try
        {
          Player player = Players.getInstance().getPlayer(pid.wurmId);
          player.getCommunicator().sendSafeServerMessage("New High Score: " + 
            ChallengePointEnum.ChallengePoint.fromInt(scoreType).getName() + " " + scoreObj.getPoints() + "!");
        }
        catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
      if (scoreType == ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype()) {
        summary.saveCurrentPersonalHtmlPage();
      }
    }
  }
  
  private static final boolean checkIfTopScore(ChallengeScore score, PlayerInfo pinf)
  {
    if (score.getType() != 0)
    {
      if ((topScores[score.getType()] == null) && (score.getPoints() > 0.0F))
      {
        topScores[score.getType()] = score;
        topScorers[score.getType()] = pinf.getName();
        return true;
      }
      if ((score.getPoints() > 0.0F) && 
        (score.getPoints() > topScores[score.getType()].getPoints()))
      {
        topScores[score.getType()] = score;
        topScorers[score.getType()] = pinf.getName();
        return true;
      }
    }
    return false;
  }
  
  public static final void addScoreFromLoad(PlayerInfo pid, int roundNumber, ChallengeScore score)
  {
    ChallengeSummary summary = getSummary(pid.wurmId);
    if (summary == null)
    {
      summary = new ChallengeSummary(pid.wurmId, pid.getName());
      addChallengeSummary(summary);
    }
    ChallengeRound round = summary.getPrivateChallengeRound(roundNumber);
    if (round == null)
    {
      round = new ChallengeRound(roundNumber);
      summary.addPrivateChallengeRound(round);
    }
    round.setScore(score);
    checkIfTopScore(score, pid);
  }
  
  public static final void addChallengeSummary(ChallengeSummary summary)
  {
    allScores.put(Long.valueOf(summary.getPlayerId()), summary);
  }
  
  public static final ChallengeSummary getSummary(long playerId)
  {
    return (ChallengeSummary)allScores.get(Long.valueOf(playerId));
  }
  
  public static final ChallengeRound getRoundSummary(long playerId, int round)
  {
    ChallengeSummary summary = (ChallengeSummary)allScores.get(Long.valueOf(playerId));
    if (summary != null) {
      return summary.getPrivateChallengeRound(round);
    }
    return null;
  }
  
  public final ChallengeRound getPrivateChallengeRound(int round)
  {
    return (ChallengeRound)this.privateRounds.get(Integer.valueOf(round));
  }
  
  public final void addPrivateChallengeRound(ChallengeRound round)
  {
    this.privateRounds.put(Integer.valueOf(round.getRound()), round);
  }
  
  public final long getPlayerId()
  {
    return this.wid;
  }
  
  public final String getPlayerName()
  {
    return this.name;
  }
  
  public static final void loadLocalChallengeScores()
  {
    if (Servers.localServer.isChallengeServer())
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      int loadedScores = 0;
      long lStart = System.nanoTime();
      try
      {
        dbcon = DbConnector.getLoginDbCon();
        ps = dbcon.prepareStatement("SELECT * FROM CHALLENGE");
        rs = ps.executeQuery();
        while (rs.next())
        {
          long wurmid = rs.getLong("WURMID");
          PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
          int round = rs.getInt("ROUND");
          float points = rs.getFloat("POINTS");
          int scoreType = rs.getInt("TYPE");
          long lastUpdated = rs.getLong("LASTUPDATED");
          long lastAdded = rs.getLong("LASTPOINTS");
          if (pinf != null) {
            addScoreFromLoad(pinf, round, new ChallengeScore(scoreType, points, lastUpdated, (float)lastAdded));
          }
          loadedScores++;
        }
      }
      catch (SQLException sqx)
      {
        long end;
        logger.log(Level.WARNING, "Failed to load scores, SqlState: " + sqx
          .getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
        Exception lNext = sqx.getNextException();
        if (lNext != null) {
          logger.log(Level.WARNING, "Failed to load scores, Next Exception", lNext);
        }
      }
      finally
      {
        long end;
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        
        long end = System.nanoTime();
        logger.info("Loaded " + loadedScores + " challenge scores from database took " + (float)(end - lStart) / 1000000.0F + " ms");
      }
    }
  }
  
  public static final void createScore(long pid, int round, ChallengeScore score)
  {
    try
    {
      if (Servers.localServer.isChallengeServer())
      {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
          dbcon = DbConnector.getLoginDbCon();
          ps = dbcon.prepareStatement("INSERT INTO CHALLENGE(LASTUPDATED,WURMID,ROUND,TYPE,POINTS,LASTPOINTS) VALUES (?,?,?,?,?,?)");
          ps.setLong(1, score.getLastUpdated());
          ps.setLong(2, pid);
          ps.setInt(3, round);
          ps.setInt(4, score.getType());
          ps.setFloat(5, score.getPoints());
          ps.setFloat(6, score.getLastPoints());
          ps.execute();
        }
        catch (SQLException sqx)
        {
          logger.log(Level.WARNING, "Failed to save score " + pid + "," + round + "," + score
          
            .getPoints() + ", SqlState: " + sqx
            .getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
          Exception lNext = sqx.getNextException();
          if (lNext != null) {
            logger.log(Level.WARNING, "Failed to save scores, Next Exception", lNext);
          }
        }
        finally
        {
          DbUtilities.closeDatabaseObjects(ps, rs);
          DbConnector.returnConnection(dbcon);
        }
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Exception saving challenge score " + ex.getMessage(), ex);
    }
  }
  
  public static final void updateScore(long pid, int round, ChallengeScore score)
  {
    try
    {
      if (Servers.localServer.isChallengeServer())
      {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
          dbcon = DbConnector.getLoginDbCon();
          ps = dbcon.prepareStatement("UPDATE CHALLENGE SET LASTUPDATED=?,POINTS=?,LASTPOINTS=? WHERE WURMID=? AND ROUND=? AND TYPE=?");
          ps.setLong(1, score.getLastUpdated());
          ps.setFloat(2, score.getPoints());
          ps.setFloat(3, score.getLastPoints());
          ps.setLong(4, pid);
          ps.setInt(5, round);
          ps.setInt(6, score.getType());
          ps.executeUpdate();
        }
        catch (SQLException sqx)
        {
          logger.log(Level.WARNING, "Failed to save score " + pid + "," + round + "," + score
          
            .getPoints() + ", SqlState: " + sqx
            .getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
          Exception lNext = sqx.getNextException();
          if (lNext != null) {
            logger.log(Level.WARNING, "Failed to load scores, Next Exception", lNext);
          }
        }
        finally
        {
          DbUtilities.closeDatabaseObjects(ps, rs);
          DbConnector.returnConnection(dbcon);
        }
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Exception " + ex.getMessage(), ex);
    }
  }
  
  private final File createFile()
  {
    if (!this.fileExists)
    {
      String dir = "/var/www/challenge/" + this.name.substring(0, 1) + File.separator;
      
      File dirFile = new File(dir.toLowerCase());
      if (!dirFile.exists()) {
        dirFile.mkdirs();
      }
      this.fileExists = true;
      this.filename = (dir.toLowerCase() + this.name.toLowerCase() + ".html");
    }
    return new File(this.filename);
  }
  
  public final void saveCurrentPersonalHtmlPage()
  {
    isDirty = true;
    new ChallengeSummary.1(this)
    
      .start();
  }
  
  private static final File createHeaderFile()
  {
    return new File(headerFilename);
  }
  
  private static final String getHighScoreUrl(int pointType)
  {
    return 
      ChallengePointEnum.ChallengePoint.fromInt(pointType).getName().replace(" ", "").trim().toLowerCase() + ChallengePointEnum.ChallengeScenario.current.getNum() + ".html";
  }
  
  private static final String getPlayerHomePageUrl(String playerName)
  {
    return 
      playerName.substring(0, 1).toLowerCase() + "/" + playerName.toLowerCase() + ".html";
  }
  
  public static final void saveCurrentGlobalHtmlPage()
  {
    if (isDirty)
    {
      isDirty = false;
      if (!writing)
      {
        writing = true;
        new ChallengeSummary.2()
        
          .start();
      }
    }
  }
  
  public static final void createHighScorePage(int scoreType)
  {
    String fileName = "/var/www/challenge/" + getHighScoreUrl(scoreType);
    File aFile = new File(fileName);
    Writer output = null;
    try
    {
      ChallengePointEnum.ChallengePoint point = ChallengePointEnum.ChallengePoint.fromInt(scoreType);
      
      output = new BufferedWriter(new FileWriter(aFile));
      output.write("<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t");
      output.write("<H1>Summary for " + point.getName() + "</H1>\n\t<br>");
      output.write("<img src=\"" + ChallengePointEnum.ChallengeScenario.current.getUrl() + "\" alt=\"round icon\"/><p><a href=\"main" + ChallengePointEnum.ChallengeScenario.current
        .getNum() + ".html\">" + ChallengePointEnum.ChallengeScenario.current
        .getName() + "</a></p>\n\t");
      try
      {
        output.write("<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Rank</TH>\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Points</TH>\n\t\t\t<TH>Last Points</TH>\n\t\t\t<TH>Date</TH>\n\t\t</TR>\n\t\t");
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
      ConcurrentSkipListSet<ScoreNamePair> scores = new ConcurrentSkipListSet();
      for (ChallengeSummary summary : allScores.values())
      {
        ChallengeRound round = summary.getPrivateChallengeRound(ChallengePointEnum.ChallengeScenario.current.getNum());
        if (round != null)
        {
          ChallengeScore[] scoreArr = round.getScores();
          for (ChallengeScore score : scoreArr) {
            if ((score.getType() == scoreType) && (score.getPoints() > 0.0F))
            {
              scores.add(new ScoreNamePair(summary.getPlayerName(), score));
              break;
            }
          }
        }
      }
      ScoreNamePair[] topScoreArr = (ScoreNamePair[])scores.toArray(new ScoreNamePair[scores.size()]);
      Arrays.sort(topScoreArr);
      for (int x = 0; x < topScoreArr.length; x++) {
        if (topScoreArr[x] != null)
        {
          String scorerUrl = getPlayerHomePageUrl(topScoreArr[x].name);
          output.write("<TR class=\"gameDataTopTenTR\">\n\t\t\t<TD class=\"gameDataTopTenTDValue\">" + (x + 1) + "</TD>\n\t\t\t<TD class=\"gameDataTopTenTDName\"><a href=\"" + scorerUrl + "\">" + topScoreArr[x].name + "</a></TD>\n\t\t\t<TD class=\"gameDataTopTenTDValue\">" + topScoreArr[x].score
          
            .getPoints() + "</TD>\n\t\t\n\t\t<TR>\n\t\t\t<TH>" + topScoreArr[x].score
            
            .getLastPoints() + "</TH>\n\t\t\t<TH>" + new Date(topScoreArr[x].score
            
            .getLastUpdated()) + "</TH>\n\t\t</TR>\n\t\t");
        }
      }
      output.write("</TABLE>\n\n");
      output.write("</BODY>\n</HTML>");
      try
      {
        if (output != null) {
          output.close();
        }
      }
      catch (IOException localIOException1) {}
      ChallengePointEnum.ChallengePoint.fromInt(scoreType).setDirty(false);
    }
    catch (IOException iox)
    {
      logger.log(Level.WARNING, "Failed to close html file for main page", iox);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\statistics\ChallengeSummary.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */