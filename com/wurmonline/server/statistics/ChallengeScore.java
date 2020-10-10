package com.wurmonline.server.statistics;

public class ChallengeScore
{
  private final int type;
  private float points;
  private float lastPoints;
  private long lastUpdated;
  
  public ChallengeScore(int scoreType, float numPoints, long aLastUpdated, float aLastPoints)
  {
    this.type = scoreType;
    setPoints(numPoints);
    setLastPoints(aLastPoints);
    setLastUpdated(aLastUpdated);
  }
  
  public int getType()
  {
    return this.type;
  }
  
  public float getPoints()
  {
    return this.points;
  }
  
  public void setPoints(float aPoints)
  {
    this.points = aPoints;
  }
  
  public long getLastUpdated()
  {
    return this.lastUpdated;
  }
  
  public void setLastUpdated(long aLastUpdated)
  {
    this.lastUpdated = aLastUpdated;
  }
  
  public float getLastPoints()
  {
    return this.lastPoints;
  }
  
  public void setLastPoints(float aLastPoints)
  {
    this.lastPoints = aLastPoints;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\statistics\ChallengeScore.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */