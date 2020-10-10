package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import java.util.ArrayList;

public class ViewStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 200;
  
  public short getWindowId()
  {
    return (short)(200 + getCurrentSubStage());
  }
  
  public ViewStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new MovementStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new WelcomeStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new ViewStage.ViewSubStage(this, getPlayerId()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\stages\ViewStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */