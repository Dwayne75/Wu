package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class TerraformStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 900;
  
  public short getWindowId()
  {
    return (short)(900 + getCurrentSubStage());
  }
  
  public TerraformStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new WoodcuttingStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new DropTakeStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new TerraformStage.DigExplainSubStage(this, getPlayerId()));
    this.subStages.add(new TerraformStage.PlayerDigSubStage(this, getPlayerId()));
    this.subStages.add(new TerraformStage.FlattenSubStage(this, getPlayerId()));
    this.subStages.add(new TerraformStage.LevelSubStage(this, getPlayerId()));
    this.subStages.add(new TerraformStage.TileTypeSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\TerraformStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */