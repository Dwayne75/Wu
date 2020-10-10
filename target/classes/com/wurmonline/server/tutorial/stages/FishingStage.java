package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class FishingStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 2000;
  
  public FishingStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new RodFishingStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return null;
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new FishingStage.FishingStartSubStage(this, getPlayerId()));
    this.subStages.add(new FishingStage.FishModifiersSubStage(this, getPlayerId()));
    this.subStages.add(new FishingStage.NettingSubStage(this, getPlayerId()));
    this.subStages.add(new FishingStage.SpearingSubStage(this, getPlayerId()));
  }
  
  public short getWindowId()
  {
    return 2000;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\FishingStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */