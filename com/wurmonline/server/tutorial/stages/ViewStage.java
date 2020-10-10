package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.PlayerTutorial.PlayerTrigger;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.TutorialStage.TutorialSubStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;
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
    this.subStages.add(new ViewSubStage(getPlayerId()));
  }
  
  public class ViewSubStage
    extends TutorialStage.TutorialSubStage
  {
    public ViewSubStage(long playerId)
    {
      super(playerId);
      setNextTrigger(PlayerTutorial.PlayerTrigger.MOVED_PLAYER_VIEW);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Looking Around", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nClick and hold the Left Mouse Button then move the mouse to look around at your surroundings.\r\n\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Waiting...", 80, 20, false)
        .maybeAddSkipButton()));
      
      this.bmlString = builder.toString();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\stages\ViewStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */