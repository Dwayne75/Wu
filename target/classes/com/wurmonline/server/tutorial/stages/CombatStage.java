package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class CombatStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 1400;
  
  public short getWindowId()
  {
    return (short)(1400 + getCurrentSubStage());
  }
  
  public CombatStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new FinalStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new SkillsStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new CombatStage.WarningSubStage(this, getPlayerId()));
    this.subStages.add(new CombatStage.OutlineSubStage(this, getPlayerId()));
    this.subStages.add(new CombatStage.TargetSubStage(this, getPlayerId()));
    this.subStages.add(new CombatStage.SimpleSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\CombatStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */