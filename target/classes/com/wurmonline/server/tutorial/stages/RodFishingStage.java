package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class RodFishingStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 2100;
  
  public RodFishingStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new FinalFishingStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new FishingStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new RodFishingStage.RodStartSubStage(this, getPlayerId()));
    this.subStages.add(new RodFishingStage.RodSetupSubStage(this, getPlayerId()));
    this.subStages.add(new RodFishingStage.RodActionSubStage(this, getPlayerId()));
    this.subStages.add(new RodFishingStage.RodHookingSubStage(this, getPlayerId()));
  }
  
  public short getWindowId()
  {
    return 2100;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\RodFishingStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */