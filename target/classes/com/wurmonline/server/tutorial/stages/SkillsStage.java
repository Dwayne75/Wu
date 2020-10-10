package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class SkillsStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 1300;
  
  public short getWindowId()
  {
    return (short)(1300 + getCurrentSubStage());
  }
  
  public SkillsStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new CombatStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new MiningStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new SkillsStage.SkillWindowSubStage(this, getPlayerId()));
    this.subStages.add(new SkillsStage.SkillGainSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\SkillsStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */