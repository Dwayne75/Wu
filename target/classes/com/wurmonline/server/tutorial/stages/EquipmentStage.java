package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class EquipmentStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 600;
  
  public short getWindowId()
  {
    return (short)(600 + getCurrentSubStage());
  }
  
  public EquipmentStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new WorldStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new StartingStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new EquipmentStage.ActivateSubStage(this, getPlayerId()));
    this.subStages.add(new EquipmentStage.CharacterSubStage(this, getPlayerId()));
    this.subStages.add(new EquipmentStage.EquipSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\EquipmentStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */