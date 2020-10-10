package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class WelcomeStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 100;
  
  public short getWindowId()
  {
    return (short)(100 + getCurrentSubStage());
  }
  
  public WelcomeStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new ViewStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return null;
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new WelcomeStage.WelcomeSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\WelcomeStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */