package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import java.util.HashMap;

public abstract class CreatureAIData
{
  private Creature creature;
  private long lastPollTime = 0L;
  private boolean dropsCorpse = true;
  private float movementSpeedModifier = 1.0F;
  private float sizeModifier = 1.0F;
  private HashMap<Integer, Long> aiTimerMap = new HashMap();
  
  public void setTimer(int timer, long time)
  {
    if (!this.aiTimerMap.containsKey(Integer.valueOf(timer))) {
      this.aiTimerMap.put(Integer.valueOf(timer), Long.valueOf(time));
    } else {
      this.aiTimerMap.replace(Integer.valueOf(timer), Long.valueOf(time));
    }
  }
  
  public long getTimer(int timer)
  {
    if (!this.aiTimerMap.containsKey(Integer.valueOf(timer))) {
      setTimer(timer, 0L);
    }
    return ((Long)this.aiTimerMap.get(Integer.valueOf(timer))).longValue();
  }
  
  public void setCreature(Creature c)
  {
    this.creature = c;
  }
  
  public Creature getCreature()
  {
    return this.creature;
  }
  
  public long getLastPollTime()
  {
    return this.lastPollTime;
  }
  
  public void setLastPollTime(long lastPollTime)
  {
    this.lastPollTime = lastPollTime;
  }
  
  public boolean doesDropCorpse()
  {
    return this.dropsCorpse;
  }
  
  public void setDropsCorpse(boolean dropsCorpse)
  {
    this.dropsCorpse = dropsCorpse;
  }
  
  public float getMovementSpeedModifier()
  {
    return this.movementSpeedModifier;
  }
  
  public void setMovementSpeedModifier(float movementModifier)
  {
    this.movementSpeedModifier = movementModifier;
  }
  
  public float getSpeed()
  {
    return this.creature.getTemplate().getSpeed();
  }
  
  public float getSizeModifier()
  {
    return this.sizeModifier;
  }
  
  public void setSizeModifier(float sizeModifier)
  {
    this.sizeModifier = sizeModifier;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\ai\CreatureAIData.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */