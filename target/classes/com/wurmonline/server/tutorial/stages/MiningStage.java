package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class MiningStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 1200;
  
  public short getWindowId()
  {
    return (short)(1200 + getCurrentSubStage());
  }
  
  public MiningStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new SkillsStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new CreationStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new MiningStage.DigRockSubStage(this, getPlayerId()));
    this.subStages.add(new MiningStage.MineIronSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\MiningStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */