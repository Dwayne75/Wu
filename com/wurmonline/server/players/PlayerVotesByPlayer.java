package com.wurmonline.server.players;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PlayerVotesByPlayer
{
  private static Logger logger = Logger.getLogger(PlayerVotesByPlayer.class.getName());
  private final Map<Integer, PlayerVote> playerQuestionVotes = new ConcurrentHashMap();
  
  public PlayerVotesByPlayer() {}
  
  public PlayerVotesByPlayer(PlayerVote pv)
  {
    add(pv);
  }
  
  public void add(PlayerVote pv)
  {
    this.playerQuestionVotes.put(Integer.valueOf(pv.getQuestionId()), pv);
  }
  
  public void remove(int questionId)
  {
    if (this.playerQuestionVotes.containsKey(Integer.valueOf(questionId))) {
      this.playerQuestionVotes.remove(Integer.valueOf(questionId));
    }
  }
  
  public PlayerVote get(int qId)
  {
    return (PlayerVote)this.playerQuestionVotes.get(Integer.valueOf(qId));
  }
  
  public boolean containsKey(int qId)
  {
    return this.playerQuestionVotes.containsKey(Integer.valueOf(qId));
  }
  
  public PlayerVote[] getVotes()
  {
    return (PlayerVote[])this.playerQuestionVotes.values().toArray(new PlayerVote[this.playerQuestionVotes.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\PlayerVotesByPlayer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */