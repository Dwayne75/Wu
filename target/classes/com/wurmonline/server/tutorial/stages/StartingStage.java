package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class StartingStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 500;
  
  public short getWindowId()
  {
    return (short)(500 + getCurrentSubStage());
  }
  
  public StartingStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new EquipmentStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new InventoryStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new StartingStage.StartingInvSubStage(this, getPlayerId()));
    this.subStages.add(new StartingStage.DeathSubStage(this, getPlayerId()));
    this.subStages.add(new StartingStage.ChatSubStage(this, getPlayerId()));
    this.subStages.add(new StartingStage.EventSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\StartingStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */