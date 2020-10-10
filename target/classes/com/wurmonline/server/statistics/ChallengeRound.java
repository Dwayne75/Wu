package com.wurmonline.server.statistics;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengeRound
{
  private final int round;
  private final ConcurrentHashMap<Integer, ChallengeScore> privateScores = new ConcurrentHashMap();
  
  ChallengeRound(int roundval)
  {
    this.round = roundval;
  }
  
  protected final void setScore(ChallengeScore score)
  {
    this.privateScores.put(Integer.valueOf(score.getType()), score);
  }
  
  protected final ChallengeScore getCurrentScoreForType(int type)
  {
    return (ChallengeScore)this.privateScores.get(Integer.valueOf(type));
  }
  
  protected final ChallengeScore[] getScores()
  {
    return (ChallengeScore[])this.privateScores.values().toArray(new ChallengeScore[this.privateScores.size()]);
  }
  
  public int getRound()
  {
    return this.round;
  }
  
  public final String getRoundName()
  {
    return ChallengePointEnum.ChallengeScenario.fromInt(this.round).getName();
  }
  
  public final String getRoundDescription()
  {
    return ChallengePointEnum.ChallengeScenario.fromInt(this.round).getDesc();
  }
  
  public final String getRoundIcon()
  {
    return ChallengePointEnum.ChallengeScenario.fromInt(this.round).getUrl();
  }
  
  public final boolean isCurrent()
  {
    return this.round == ChallengePointEnum.ChallengeScenario.current.getNum();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\statistics\ChallengeRound.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */