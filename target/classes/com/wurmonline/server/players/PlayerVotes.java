package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Players;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerVotes
{
  private static Logger logger = Logger.getLogger(PlayerVotes.class.getName());
  private static final Map<Long, PlayerVotesByPlayer> playerVotes = new ConcurrentHashMap();
  private static final String LOADALLPLAYERVOTES = "SELECT * FROM VOTES";
  private static final String DELETEQUESTIONVOTES = "DELETE FROM VOTES WHERE QUESTIONID=?";
  
  public static void loadAllPlayerVotes()
  {
    long start = System.nanoTime();
    try
    {
      dbLoadAllPlayerVotes();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Problems loading Player Votes.", ex);
    }
    float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
    logger.log(Level.INFO, "Loaded " + playerVotes.size() + " Player Votes. It took " + lElapsedTime + " millis.");
  }
  
  public static PlayerVote addPlayerVote(PlayerVote newPlayerVote, boolean saveit)
  {
    Long pId = Long.valueOf(newPlayerVote.getPlayerId());
    if (!playerVotes.containsKey(pId)) {
      playerVotes.put(pId, new PlayerVotesByPlayer());
    }
    PlayerVotesByPlayer pvbp = (PlayerVotesByPlayer)playerVotes.get(pId);
    PlayerVote oldPlayerVote = pvbp.get(newPlayerVote.getQuestionId());
    if (oldPlayerVote != null)
    {
      oldPlayerVote.update(newPlayerVote.getOption1(), newPlayerVote.getOption2(), newPlayerVote
        .getOption3(), newPlayerVote.getOption4());
      return oldPlayerVote;
    }
    pvbp.add(newPlayerVote);
    if (saveit) {
      newPlayerVote.save();
    }
    return newPlayerVote;
  }
  
  public static PlayerVote[] getPlayerVotes(long aPlayerId)
  {
    PlayerVotesByPlayer pvbp = (PlayerVotesByPlayer)playerVotes.get(Long.valueOf(aPlayerId));
    if (pvbp == null) {
      return new PlayerVote[0];
    }
    return pvbp.getVotes();
  }
  
  public static boolean hasPlayerVotedByQuestion(long aPlayerId, int aQuestionId)
  {
    Long pId = Long.valueOf(aPlayerId);
    if (playerVotes.containsKey(pId))
    {
      PlayerVotesByPlayer pvbp = (PlayerVotesByPlayer)playerVotes.get(pId);
      if (pvbp.containsKey(aQuestionId))
      {
        PlayerVote pv = pvbp.get(aQuestionId);
        return pv.hasVoted();
      }
    }
    return false;
  }
  
  public static PlayerVote getPlayerVotesByQuestions(long aPlayerId, int aQuestionId)
  {
    Long pId = Long.valueOf(aPlayerId);
    if (playerVotes.containsKey(pId))
    {
      PlayerVotesByPlayer pvbp = (PlayerVotesByPlayer)playerVotes.get(pId);
      if (pvbp.containsKey(aQuestionId))
      {
        PlayerVote pv = pvbp.get(aQuestionId);
        return pv;
      }
    }
    return null;
  }
  
  public static PlayerVote getPlayerVoteByQuestion(long aPlayerId, int aQuestionId)
  {
    Long pId = Long.valueOf(aPlayerId);
    if (playerVotes.containsKey(pId))
    {
      PlayerVotesByPlayer pvbp = (PlayerVotesByPlayer)playerVotes.get(pId);
      if (pvbp.containsKey(aQuestionId))
      {
        PlayerVote pv = pvbp.get(aQuestionId);
        return pv;
      }
    }
    return null;
  }
  
  public static PlayerVote[] getPlayerVotesByQuestion(int aQuestionId)
  {
    Map<Long, PlayerVote> pVotes = new HashMap();
    for (Map.Entry<Long, PlayerVotesByPlayer> entry : playerVotes.entrySet()) {
      if (((PlayerVotesByPlayer)entry.getValue()).containsKey(aQuestionId))
      {
        PlayerVote pv = ((PlayerVotesByPlayer)entry.getValue()).get(aQuestionId);
        if (pv.hasVoted()) {
          pVotes.put(entry.getKey(), pv);
        }
      }
    }
    return (PlayerVote[])pVotes.values().toArray(new PlayerVote[pVotes.size()]);
  }
  
  public static void deletePlayerVotes(int questionId)
  {
    for (Object localObject1 = playerVotes.entrySet().iterator(); ((Iterator)localObject1).hasNext();)
    {
      entry = (Map.Entry)((Iterator)localObject1).next();
      ((PlayerVotesByPlayer)entry.getValue()).remove(questionId);
    }
    localObject1 = Players.getInstance().getPlayers();Map.Entry<Long, PlayerVotesByPlayer> entry = localObject1.length;
    for (Map.Entry<Long, PlayerVotesByPlayer> localEntry1 = 0; localEntry1 < entry; localEntry1++)
    {
      Player p = localObject1[localEntry1];
      p.removePlayerVote(questionId);
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("DELETE FROM VOTES WHERE QUESTIONID=?");
      ps.setInt(1, questionId);
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
  
  private static void dbLoadAllPlayerVotes()
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM VOTES");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long aPlayerId = rs.getLong("PLAYERID");
        int aQuestionId = rs.getInt("QUESTIONID");
        boolean aOption1 = rs.getBoolean("OPTION1");
        boolean aOption2 = rs.getBoolean("OPTION2");
        boolean aOption3 = rs.getBoolean("OPTION3");
        boolean aOption4 = rs.getBoolean("OPTION4");
        
        addPlayerVote(new PlayerVote(aPlayerId, aQuestionId, aOption1, aOption2, aOption3, aOption4), false);
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
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\PlayerVotes.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */