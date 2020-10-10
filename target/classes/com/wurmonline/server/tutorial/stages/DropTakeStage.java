package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class DropTakeStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 800;
  
  public short getWindowId()
  {
    return (short)(800 + getCurrentSubStage());
  }
  
  public DropTakeStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new TerraformStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new WorldStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new DropTakeStage.DropSubStage(this, getPlayerId()));
    this.subStages.add(new DropTakeStage.PlaceSubStage(this, getPlayerId()));
    this.subStages.add(new DropTakeStage.TakeSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\DropTakeStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */