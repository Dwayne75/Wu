package com.wurmonline.server.players;

public final class HackerIp
{
  public int timesFailed = 0;
  public long mayTryAgain = 0L;
  public String name;
  
  public HackerIp(int _timesFailed, long _mayTryAgain, String _name)
  {
    this.timesFailed = _timesFailed;
    this.mayTryAgain = _mayTryAgain;
    this.name = _name;
  }
  
  public int getTimesFailed()
  {
    return this.timesFailed;
  }
  
  public void incrementTimesFailed()
  {
    this.timesFailed += 1;
  }
  
  public long getMayTryAgain()
  {
    return this.mayTryAgain;
  }
  
  public void setMayTryAgain(long aMayTryAgain)
  {
    this.mayTryAgain = aMayTryAgain;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String aName)
  {
    this.name = aName;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\HackerIp.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */