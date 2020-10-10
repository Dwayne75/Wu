package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class InventoryStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 400;
  
  public short getWindowId()
  {
    return (short)(400 + getCurrentSubStage());
  }
  
  public InventoryStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new StartingStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new MovementStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new InventoryStage.InventorySubStage(this, getPlayerId()));
    this.subStages.add(new InventoryStage.MoveItemsSubStage(this, getPlayerId()));
    this.subStages.add(new InventoryStage.QualitySubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\InventoryStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */