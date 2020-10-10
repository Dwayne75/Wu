package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.PlayerTutorial.PlayerTrigger;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.TutorialStage.TutorialSubStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;
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
    this.subStages.add(new WASDSubStage(getPlayerId()));
    this.subStages.add(new ClimbingOnSubStage(getPlayerId()));
    this.subStages.add(new ClimbingOffSubStage(getPlayerId()));
    this.subStages.add(new HealthStaminaSubStage(getPlayerId()));
  }
  
  public class WASDSubStage
    extends TutorialStage.TutorialSubStage
  {
    public WASDSubStage(long playerId)
    {
      super(playerId);
      setNextTrigger(PlayerTutorial.PlayerTrigger.MOVED_PLAYER);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Movement", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nUse the [$bind:MOVE_FORWARD$], [$bind:MOVE_LEFT$], [$bind:MOVE_BACK$] and [$bind:MOVE_RIGHT$] keys in order to move around.\r\n\r\n", null, null, null, 300, 400)
        
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
  
  public class ClimbingOnSubStage
    extends TutorialStage.TutorialSubStage
  {
    public ClimbingOnSubStage(long playerId)
    {
      super(playerId);
      setNextTrigger(PlayerTutorial.PlayerTrigger.ENABLED_CLIMBING);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Movement", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nSome land in Wurm can be too steep for you to simply walk across.\r\n\r\nEnable climbing mode by pressing $bind:TOGGLE_CLIMB$ or the climb button in order to climb steep slopes.\r\n\r\n", null, null, null, 300, 400)
        
        .addImage("image.tutorial.climbing", 300, 150)
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Waiting...", 80, 20, false)
        .maybeAddSkipButton()));
      
      this.bmlString = builder.toString();
    }
    
    public void triggerOnView()
    {
      try
      {
        Player p = Players.getInstance().getPlayer(getPlayerId());
        p.getCommunicator().sendOpenWindow((short)9, true);
        p.getCommunicator().sendToggleQuickbarBtn((short)2001, true);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
  }
  
  public class ClimbingOffSubStage
    extends TutorialStage.TutorialSubStage
  {
    public ClimbingOffSubStage(long playerId)
    {
      super(playerId);
      setNextTrigger(PlayerTutorial.PlayerTrigger.DISABLED_CLIMBING);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Movement", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nWhile climbing mode is enabled, you'll walk a lot slower and drain more stamina than usual.\r\n\r\nDisable climbing mode by pressing $bind:TOGGLE_CLIMB$ or the climb button again.\r\n\r\n", null, null, null, 300, 400)
        
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
  
  public class HealthStaminaSubStage
    extends TutorialStage.TutorialSubStage
  {
    public HealthStaminaSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Movement", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nKeep an eye on your stamina while moving, climbing or doing actions.\r\n\r\nRunning out of stamina can lead to your character getting tired and walking slower, completing actions slower, or possibly falling from a steep slope if you are climbing.\r\n\r\n", null, null, null, 300, 400)
        
        .addImage("image.tutorial.stamina", 300, 100)
        .addText("\r\nTo regain your stamina simply stand still for a few seconds. You cannot regain stamina while climbing is toggled on.\r\n\r\n", null, null, null, 300, 400)
        
        .addText("").toString()), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
    
    public void triggerOnView()
    {
      try
      {
        Player p = Players.getInstance().getPlayer(getPlayerId());
        p.getCommunicator().sendOpenWindow((short)5, true);
        p.getCommunicator().sendOpenWindow((short)13, false);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\stages\MovementStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */