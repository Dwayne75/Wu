package com.wurmonline.server.statistics;

public class ScoreNamePair
  implements Comparable<ScoreNamePair>
{
  public final String name;
  public final ChallengeScore score;
  
  public ScoreNamePair(String owner, ChallengeScore score)
  {
    this.name = owner;
    this.score = score;
  }
  
  public int compareTo(ScoreNamePair namePair)
  {
    if (this.score.getPoints() > namePair.score.getPoints()) {
      return -1;
    }
    if ((this.name.toLowerCase().equals(namePair.name.toLowerCase())) && (this.score.getPoints() == namePair.score.getPoints())) {
      return 0;
    }
    return 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\statistics\ScoreNamePair.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */