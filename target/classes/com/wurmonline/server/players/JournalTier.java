package com.wurmonline.server.players;

import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import java.util.ArrayList;
import java.util.HashMap;

public class JournalTier
{
  private final ArrayList<Integer> achievementList;
  private final byte tierId;
  private final String tierName;
  private final byte lastTierId;
  private final byte nextTierId;
  private final int unlockNextNeeded;
  private final int rewardFlagId;
  private JournalReward reward = null;
  
  public JournalTier(byte tierId, String tierName, byte lastTierId, byte nextTierId, int unlockNextNeeded, int rewardFlag, int... achievements)
  {
    this.tierId = tierId;
    this.tierName = tierName;
    this.lastTierId = lastTierId;
    this.nextTierId = nextTierId;
    this.unlockNextNeeded = unlockNextNeeded;
    this.rewardFlagId = rewardFlag;
    
    this.achievementList = new ArrayList();
    for (int i : achievements) {
      this.achievementList.add(Integer.valueOf(i));
    }
  }
  
  public byte getTierId()
  {
    return this.tierId;
  }
  
  public String getTierName()
  {
    return this.tierName;
  }
  
  public boolean containsAchievement(int achievementId)
  {
    return this.achievementList.contains(Integer.valueOf(achievementId));
  }
  
  public boolean isVisible(long playerId)
  {
    if (this.lastTierId < 0) {
      return true;
    }
    return ((JournalTier)PlayerJournal.getAllTiers().get(Byte.valueOf(this.lastTierId))).isNextTierUnlocked(playerId);
  }
  
  public boolean isNextTierUnlocked(long playerId)
  {
    if (!isVisible(playerId)) {
      return false;
    }
    Achievement[] achieves = Achievements.getAchievements(playerId);
    int countCompleted = 0;
    for (Achievement a : achieves) {
      if (this.achievementList.contains(Integer.valueOf(a.getTemplate().getNumber()))) {
        countCompleted++;
      }
    }
    return countCompleted >= this.unlockNextNeeded;
  }
  
  public boolean shouldUnlockNextTier(long playerId)
  {
    if (!isVisible(playerId)) {
      return false;
    }
    Achievement[] achieves = Achievements.getAchievements(playerId);
    int countCompleted = 0;
    for (Achievement a : achieves) {
      if (this.achievementList.contains(Integer.valueOf(a.getTemplate().getNumber()))) {
        countCompleted++;
      }
    }
    return countCompleted + 1 == this.unlockNextNeeded;
  }
  
  public boolean isRewardUnlocked(long playerId)
  {
    if (!isVisible(playerId)) {
      return false;
    }
    Achievement[] achieves = Achievements.getAchievements(playerId);
    int countCompleted = 0;
    for (Achievement a : achieves) {
      if (this.achievementList.contains(Integer.valueOf(a.getTemplate().getNumber()))) {
        countCompleted++;
      }
    }
    return countCompleted >= this.achievementList.size();
  }
  
  public boolean hasBeenAwarded(long playerId)
  {
    Player p = Players.getInstance().getPlayerOrNull(playerId);
    if (p != null)
    {
      if (p.hasFlag(getRewardFlag())) {
        return true;
      }
    }
    else
    {
      PlayerInfo pInf = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
      if ((pInf != null) && (pInf.isFlagSet(getRewardFlag()))) {
        return true;
      }
    }
    return false;
  }
  
  public boolean shouldUnlockReward(long playerId)
  {
    if (!isVisible(playerId)) {
      return false;
    }
    if (hasBeenAwarded(playerId)) {
      return false;
    }
    Achievement[] achieves = Achievements.getAchievements(playerId);
    int countCompleted = 0;
    for (Achievement a : achieves) {
      if (this.achievementList.contains(Integer.valueOf(a.getTemplate().getNumber()))) {
        countCompleted++;
      }
    }
    return countCompleted + 1 == this.achievementList.size();
  }
  
  public int getNextUnlockCount()
  {
    return this.unlockNextNeeded;
  }
  
  public byte getLastTierId()
  {
    return this.lastTierId;
  }
  
  public byte getNextTierId()
  {
    return this.nextTierId;
  }
  
  public int getTotalAchievements()
  {
    return this.achievementList.size();
  }
  
  public ArrayList<Integer> getAchievementList()
  {
    return this.achievementList;
  }
  
  public JournalTier getNextTier()
  {
    return (JournalTier)PlayerJournal.getAllTiers().get(Byte.valueOf(this.nextTierId));
  }
  
  public int getRewardFlag()
  {
    return this.rewardFlagId;
  }
  
  public void setReward(JournalReward jr)
  {
    this.reward = jr;
  }
  
  public void awardReward(long playerId)
  {
    Player p = Players.getInstance().getPlayerOrNull(playerId);
    if (p == null) {
      return;
    }
    if (this.reward != null)
    {
      this.reward.runReward(p);
      p.setFlag(getRewardFlag(), true);
      p.getCommunicator().sendSafeServerMessage("Congratulations, you fully completed " + getTierName() + " and earned the reward: " + this.reward
        .getRewardDesc(), (byte)2);
    }
  }
  
  public String getRewardString()
  {
    if (this.reward != null) {
      return this.reward.getRewardDesc();
    }
    return "";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\JournalTier.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */