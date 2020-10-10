package com.wurmonline.server.villages;

public class KosWarning
{
  public final long playerId;
  public final int newReputation;
  private int ticks = 0;
  public final Village village;
  public final boolean permanent;
  
  public KosWarning(long pid, int newRep, Village vill, boolean perma)
  {
    this.playerId = pid;
    this.newReputation = newRep;
    this.village = vill;
    this.permanent = perma;
  }
  
  public final int getTick()
  {
    return this.ticks;
  }
  
  public final int tick()
  {
    return ++this.ticks;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\KosWarning.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */