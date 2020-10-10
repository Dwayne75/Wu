package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class WoodcuttingStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 1000;
  
  public short getWindowId()
  {
    return (short)(1000 + getCurrentSubStage());
  }
  
  public WoodcuttingStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new CreationStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new TerraformStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new WoodcuttingStage.CutDownSubStage(this, getPlayerId()));
    this.subStages.add(new WoodcuttingStage.FellTreeSubStage(this, getPlayerId()));
    this.subStages.add(new WoodcuttingStage.CreateLogSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\WoodcuttingStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */