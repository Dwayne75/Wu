package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.TutorialStage.TutorialSubStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;
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
    this.subStages.add(new WarningSubStage(getPlayerId()));
    this.subStages.add(new OutlineSubStage(getPlayerId()));
    this.subStages.add(new TargetSubStage(getPlayerId()));
    this.subStages.add(new SimpleSubStage(getPlayerId()));
  }
  
  public class WarningSubStage
    extends TutorialStage.TutorialSubStage
  {
    public WarningSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Combat", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nThe lands of wurm are dangerous and there are many threats. It is not uncommon to come face to face with dangers such as spiders, bears and trolls during your adventures.\r\n\r\nIt is recommended not to underestimate any creature you may come up against, as even something as small as a rat may cause you issues as you start your Wurm adventure.\r\n\r\nMake sure you have equipped armour and weapons before exploring the world, or you may soon come to regret it.\r\n\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class OutlineSubStage
    extends TutorialStage.TutorialSubStage
  {
    public OutlineSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Combat", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nHovering your mouse over a creature can tell you if a creature is hostile to you or not. If it has a red outline, then getting too close to that creature will cause it to target you and start attacking.\r\n\r\nA blue outline means that creature is neutral to you, and will generally ignore or run from you as you get close.\r\n\r\nA green outline is reserved for friendly creatures, such as players on your friends list, any pets you have tamed, or pets of your friends.\r\n\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class TargetSubStage
    extends TutorialStage.TutorialSubStage
  {
    public TargetSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Combat", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nToggling the Autofight option on the quickbar so it is turned off will enable manual fighting mode.\r\n\r\nDouble clicking on a creature or right clicking and choosing the Target action will cause you to target that creature and if you are close enough to it, you will start trying to swing your weapon at it.\r\n\r\nYou can only have one target at a time, and that will be shown in your target window. Just like the select bar you can interact with the target via right clicking the target window, and also will be able to see its current health.\r\n\r\nYour fighting style, distance to the target, focus level and fighting stances can all be seen and controlled from the fight window which will expand when you're close enough to a target to start attacking.\r\n\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
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
        p.getCommunicator().sendOpenWindow((short)4, true);
        p.getCommunicator().sendOpenWindow((short)11, true);
        p.getCommunicator().sendToggleQuickbarBtn((short)2005, true);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
  }
  
  public class SimpleSubStage
    extends TutorialStage.TutorialSubStage
  {
    public SimpleSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Combat", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nA lot of factors go into determining the winner of a fight including: skill levels of both sides, armour quality and type, weapon quality and type, fighting styles and stances, focus level, distance to the target, enchantments and many more.\r\n\r\nTo keep things simple to start, make sure your armour and weapon are as high quality as you can get them and always be prepared to run if the fight starts turning against you.\r\n\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\stages\CombatStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */