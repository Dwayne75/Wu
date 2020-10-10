package com.wurmonline.server.tutorial;

import com.wurmonline.server.utils.BMLBuilder;
import java.util.ArrayList;

public abstract class TutorialStage
{
  private static final String ERROR_NOSUBSTAGE = BMLBuilder.createText("Error while loading tutorial stage: no sub stages found.");
  private static final String ERROR_OVERSUBSTAGE = BMLBuilder.createText("Error while loading tutorial stage: not enough sub stages found.");
  private static final String ERROR_NOSUBSTAGE_UPDATE = BMLBuilder.createText("Error while updating tutorial stage: no sub stages found.");
  private static final String ERROR_OVERSUBSTAGE_UPDATE = BMLBuilder.createText("Error while updating tutorial stage: not enough sub stages found.");
  private final long playerId;
  private int currentSubStage;
  private boolean forceOpened = false;
  protected ArrayList<TutorialStage.TutorialSubStage> subStages = new ArrayList();
  
  public TutorialStage(long playerId)
  {
    this.playerId = playerId;
    
    buildSubStages();
  }
  
  public abstract TutorialStage getNextStage();
  
  public abstract TutorialStage getLastStage();
  
  public abstract void buildSubStages();
  
  public abstract short getWindowId();
  
  public String getCurrentBML()
  {
    if (this.subStages == null) {
      return ERROR_NOSUBSTAGE;
    }
    if (this.subStages.size() < getCurrentSubStage()) {
      return ERROR_OVERSUBSTAGE;
    }
    return ((TutorialStage.TutorialSubStage)this.subStages.get(this.currentSubStage)).getBMLString();
  }
  
  public String getUpdateBML()
  {
    if (this.subStages == null) {
      return ERROR_NOSUBSTAGE_UPDATE;
    }
    if (this.subStages.size() < getCurrentSubStage()) {
      return ERROR_OVERSUBSTAGE_UPDATE;
    }
    return ((TutorialStage.TutorialSubStage)this.subStages.get(this.currentSubStage)).getBMLUpdateString();
  }
  
  public boolean isAwaitingAnyTrigger()
  {
    return ((TutorialStage.TutorialSubStage)this.subStages.get(this.currentSubStage)).awaitingTrigger();
  }
  
  public boolean shouldSkipTrigger()
  {
    return (!isAwaitingAnyTrigger()) && (((TutorialStage.TutorialSubStage)this.subStages.get(this.currentSubStage)).hadNextTrigger());
  }
  
  public boolean awaitingTrigger(PlayerTutorial.PlayerTrigger trigger)
  {
    return ((TutorialStage.TutorialSubStage)this.subStages.get(this.currentSubStage)).hasNextTrigger(trigger);
  }
  
  public void clearTrigger()
  {
    ((TutorialStage.TutorialSubStage)this.subStages.get(this.currentSubStage)).clearNextTrigger();
  }
  
  public int getCurrentSubStage()
  {
    return this.currentSubStage;
  }
  
  public void setForceOpened(boolean forceOpened)
  {
    this.forceOpened = forceOpened;
  }
  
  public boolean isForceOpened()
  {
    return this.forceOpened;
  }
  
  public boolean increaseSubStage()
  {
    this.currentSubStage += 1;
    if (this.currentSubStage < this.subStages.size()) {
      return true;
    }
    return false;
  }
  
  public boolean decreaseSubStage()
  {
    if (this.currentSubStage == 0) {
      return true;
    }
    this.currentSubStage = Math.max(0, this.currentSubStage - 1);
    return false;
  }
  
  public void toLastSubStage()
  {
    this.currentSubStage = (this.subStages.size() - 1);
  }
  
  public void resetSubStage()
  {
    this.subStages.clear();
    this.currentSubStage = 0;
    buildSubStages();
  }
  
  public long getPlayerId()
  {
    return this.playerId;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\TutorialStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */