package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class WorldStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 700;
  
  public short getWindowId()
  {
    return (short)(700 + getCurrentSubStage());
  }
  
  public WorldStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new DropTakeStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new EquipmentStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new WorldStage.InteractSubStage(this, getPlayerId()));
    this.subStages.add(new WorldStage.SelectSubStage(this, getPlayerId()));
    this.subStages.add(new WorldStage.KeybindSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\WorldStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */