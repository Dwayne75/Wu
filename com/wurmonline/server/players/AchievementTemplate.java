package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import java.util.logging.Logger;

public final class AchievementTemplate
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(AchievementTemplate.class.getName());
  public static final String CREATOR_SYSTEM = "System";
  private final int number;
  private String name;
  private String creator = "System";
  private String description = "";
  private boolean isForCooking = false;
  private boolean isInLiters = false;
  private boolean isPersonalGoal = false;
  private String requirement = "";
  private boolean playSoundOnUpdate = false;
  private final boolean invisible;
  private static int nextAchievementId = Servers.localServer.id * 100000;
  private boolean oneTimer = false;
  private int onTriggerOnCounter = 1;
  private byte type = 3;
  private int[] achievementsTriggered = EMPTY_INT_ARRAY;
  private int[] requiredAchievements = EMPTY_INT_ARRAY;
  
  public AchievementTemplate(int identity, String achName, boolean isInvisible)
  {
    this.number = identity;
    this.name = achName;
    this.invisible = isInvisible;
  }
  
  public AchievementTemplate(int identity, String achName, boolean isInvisible, String requirementString)
  {
    this.number = identity;
    this.name = achName;
    this.invisible = isInvisible;
    
    this.isPersonalGoal = (identity < 335);
    this.requirement = requirementString;
  }
  
  public AchievementTemplate(int identity, String achName, boolean isInvisible, int triggerOn, byte achievementType, boolean playUpdateSound, boolean isOneTimer)
  {
    this.number = identity;
    this.name = achName;
    this.invisible = isInvisible;
    this.onTriggerOnCounter = triggerOn;
    this.type = achievementType;
    this.playSoundOnUpdate = playUpdateSound;
    this.oneTimer = isOneTimer;
  }
  
  public AchievementTemplate(int identity, String achName, boolean isInvisible, int triggerOn, byte achievementType, boolean playUpdateSound, boolean isOneTimer, String requirementString)
  {
    this.number = identity;
    this.name = achName;
    this.invisible = isInvisible;
    this.onTriggerOnCounter = triggerOn;
    this.type = achievementType;
    this.playSoundOnUpdate = playUpdateSound;
    this.oneTimer = isOneTimer;
    
    this.isPersonalGoal = (identity < 335);
    this.requirement = requirementString;
  }
  
  public AchievementTemplate(int identity, String achName, boolean isInvisible, int triggerOn, String myDescription, String creatorName, boolean playUpdateSound, boolean loaded)
  {
    this.number = identity;
    this.name = achName;
    this.invisible = isInvisible;
    this.description = myDescription;
    this.onTriggerOnCounter = triggerOn;
    this.type = 2;
    this.creator = creatorName;
    this.playSoundOnUpdate = playUpdateSound;
    this.oneTimer = true;
    nextAchievementId = Math.max(nextAchievementId, this.number + 1);
    if (!loaded) {
      AchievementGenerator.insertAchievementTemplate(this);
    }
  }
  
  public void delete()
  {
    AchievementGenerator.deleteAchievementTemplate(this);
  }
  
  public static int getNextAchievementId()
  {
    return nextAchievementId;
  }
  
  public int[] getAchievementsTriggered()
  {
    return this.achievementsTriggered;
  }
  
  public void setAchievementsTriggered(int[] aAchievementsTriggered)
  {
    this.achievementsTriggered = aAchievementsTriggered;
  }
  
  public int[] getRequiredAchievements()
  {
    return this.requiredAchievements;
  }
  
  public void setRequiredAchievements(int[] aRequiredAchievements)
  {
    this.requiredAchievements = aRequiredAchievements;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public void setDescription(String aDescription)
  {
    this.description = aDescription;
  }
  
  public boolean isForCooking()
  {
    return this.isForCooking;
  }
  
  public void setIsForCooking(boolean isForCooking)
  {
    this.isForCooking = isForCooking;
  }
  
  public boolean isInLiters()
  {
    return this.isInLiters;
  }
  
  public void setIsInLiters(boolean isInLiters)
  {
    this.isInLiters = isInLiters;
  }
  
  public boolean isInvisible()
  {
    return this.invisible;
  }
  
  public int getNumber()
  {
    return this.number;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String aName)
  {
    this.name = aName;
  }
  
  public int getTriggerOnCounter()
  {
    return this.onTriggerOnCounter;
  }
  
  public void setTriggerOnCounter(int triggerOnCounter)
  {
    this.onTriggerOnCounter = triggerOnCounter;
  }
  
  public String getCreator()
  {
    return this.creator;
  }
  
  public void setCreator(String aCreator)
  {
    this.creator = aCreator;
  }
  
  public byte getType()
  {
    return this.type;
  }
  
  public void setType(byte aType)
  {
    this.type = aType;
  }
  
  public boolean isPlaySoundOnUpdate()
  {
    return this.playSoundOnUpdate;
  }
  
  public void setPlaySoundOnUpdate(boolean aPlaySoundOnUpdate)
  {
    this.playSoundOnUpdate = aPlaySoundOnUpdate;
  }
  
  public boolean isOneTimer()
  {
    return this.oneTimer;
  }
  
  public void setOneTimer(boolean aOneTimer)
  {
    this.oneTimer = aOneTimer;
  }
  
  public String getRequirement()
  {
    return this.requirement;
  }
  
  public boolean isPersonalGoal()
  {
    return this.isPersonalGoal;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\AchievementTemplate.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */