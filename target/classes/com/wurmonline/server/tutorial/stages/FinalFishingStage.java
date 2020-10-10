package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class FinalFishingStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 2200;
  
  public FinalFishingStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return null;
  }
  
  public TutorialStage getLastStage()
  {
    return new RodFishingStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new FinalFishingStage.FishingEndSubStage(this, getPlayerId()));
  }
  
  public short getWindowId()
  {
    return 2200;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\FinalFishingStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */