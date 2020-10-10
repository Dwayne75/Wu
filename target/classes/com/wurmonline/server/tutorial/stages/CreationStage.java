package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class CreationStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 1100;
  
  public short getWindowId()
  {
    return (short)(1100 + getCurrentSubStage());
  }
  
  public CreationStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new MiningStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new WoodcuttingStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new CreationStage.CreationWindowSubStage(this, getPlayerId()));
    this.subStages.add(new CreationStage.CreateKindlingSubStage(this, getPlayerId()));
    this.subStages.add(new CreationStage.CreateCampfireSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\CreationStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */