package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class FinalStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 1500;
  
  public short getWindowId()
  {
    return (short)(1500 + getCurrentSubStage());
  }
  
  public FinalStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return null;
  }
  
  public TutorialStage getLastStage()
  {
    return new CombatStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new FinalStage.KeybindSubStage(this, getPlayerId()));
    this.subStages.add(new FinalStage.QuickbindSubStage(this, getPlayerId()));
    this.subStages.add(new FinalStage.WurmpediaSubStage(this, getPlayerId()));
    this.subStages.add(new FinalStage.RulesSubStage(this, getPlayerId()));
    this.subStages.add(new FinalStage.SettingsSubStage(this, getPlayerId()));
    this.subStages.add(new FinalStage.GoodLuckSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\FinalStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */