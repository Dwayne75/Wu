package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class MovementStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 300;
  
  public short getWindowId()
  {
    return (short)(300 + getCurrentSubStage());
  }
  
  public MovementStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new InventoryStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new ViewStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new MovementStage.WASDSubStage(this, getPlayerId()));
    this.subStages.add(new MovementStage.ClimbingOnSubStage(this, getPlayerId()));
    this.subStages.add(new MovementStage.ClimbingOffSubStage(this, getPlayerId()));
    this.subStages.add(new MovementStage.HealthStaminaSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\MovementStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */