package com.wurmonline.server.players;

public abstract class JournalReward
{
  private final String rewardDescription;
  
  public abstract void runReward(Player paramPlayer);
  
  public JournalReward(String rewardDescription)
  {
    this.rewardDescription = rewardDescription;
  }
  
  public String getRewardDesc()
  {
    return this.rewardDescription;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\JournalReward.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */