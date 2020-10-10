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
    this.subStages.add(new KeybindSubStage(getPlayerId()));
    this.subStages.add(new QuickbindSubStage(getPlayerId()));
    this.subStages.add(new WurmpediaSubStage(getPlayerId()));
    this.subStages.add(new RulesSubStage(getPlayerId()));
    this.subStages.add(new SettingsSubStage(getPlayerId()));
    this.subStages.add(new GoodLuckSubStage(getPlayerId()));
  }
  
  public class KeybindSubStage
    extends TutorialStage.TutorialSubStage
  {
    public KeybindSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Keybindings", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nMost actions in Wurm have a corresponding keybind that you can bind to any key you would like. Keybindings can be changed from the game Settings.\r\n\r\nOpen the Settings window from the main menu, then choose the Keybinds tab to bind a key.\r\n\r\nFor example, if you bind the Open action to the F key you can then hover over any container in game with your mouse and press F to open it instead of right clicking and selecting Open.\r\n\r\nFor a full list of possible Keybinds, check the Settings via the main menu or visit the Keybinds page on the Wurmpedia.\r\n\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class QuickbindSubStage
    extends TutorialStage.TutorialSubStage
  {
    public QuickbindSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Keybindings", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nYou can also quickly bind a key to an action from the right click menu.\r\n\r\nSimply right click to bring up the menu with the action you want to bind then hover your mouse over that action and hold down a key or key combination for a second. When the key is bound, it will showin the right click menu as a reminder.\r\n\r\n", null, null, null, 300, 400)
        
        .addImage("image.tutorial.quickbind", 300, 150)
        .addText("\r\nPlease note that you can only quickbind actions that are normally possible to bind from the game Settings.\r\n\r\n", null, null, null, 300, 400)
        
        .addText("").toString()), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class WurmpediaSubStage
    extends TutorialStage.TutorialSubStage
  {
    public WurmpediaSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Wurmpedia", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nThe Wurmpedia is your main resource for information on Skills, Items, Actions and Crafting.\r\n\r\nYou can access the Wurmpedia from in-game by pressing [$bind:'toggle wikisearch'$] and typing a keyword before hitting enter.\r\n\r\nYou can also right click anything in game, and select the Wurmpedia action to open the Wurmpedia window and search for that target.\r\n\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class RulesSubStage
    extends TutorialStage.TutorialSubStage
  {
    public RulesSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Game Rules", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nYou can find the full set of Wurm Rules at the following link:\r\n\r\nhttps://forum.wurmonline.com/index.php?/topic/123123-wurm-rules/\r\n\r\nMacroing is not allowed in Wurm Online, and it or any other rules violations may lead to your account or IP being permanently banned.\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class SettingsSubStage
    extends TutorialStage.TutorialSubStage
  {
    public SettingsSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Profile & Settings", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nA large number of settings are available in the game client to help your game run more smoothly. You can find these by pressing [$bind:STOP_OR_MAIN_MENU$] and clicking Settings. Additional windows and information can be found under HUD Settings in the Main Menu.\r\n\r\nAs well as these settings, there are a number of character specific settings you can change from your character profile. You can find your profile by pressing [$bind:TOGGLE_CHARACTER$] then rightclicking the body icon in the bottom right hand corner and choosing Manage -> Profile.\r\n\r\nAlternatively you can right click the Inventory line in your inventory window then choose Manage -> Profile.\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class GoodLuckSubStage
    extends TutorialStage.TutorialSubStage
  {
    public GoodLuckSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Good Luck!", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nYou've made it to the end of the tutorial. You should now know enough about Wurm to get yourself started.\r\n\r\nIf you are looking for some goals or things to do, take a look at the Personal Journal window for some suggestions.\r\n\r\nYou can play through this tutorial again at any point by typing /tutorial into chat.\r\n\r\nIf you are ever going through this tutorial again in the future and want to close it, you can type /skipTutorial into chat at any point during the tutorial to end it.\r\n", null, null, null, 300, 400)
        
        .addText(""), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "End Tutorial", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("restart", "Restart Tutorial", " ", "Are you sure you want to restart the tutorial from the beginning?", null, false, 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
    
    public void triggerOnView()
    {
      try
      {
        Player p = Players.getInstance().getPlayer(getPlayerId());
        p.getCommunicator().sendOpenWindow((short)7, false);
        p.getCommunicator().sendOpenWindow((short)2, false);
        p.getCommunicator().sendToggleQuickbarBtn((short)2002, true);
        p.getCommunicator().sendToggleQuickbarBtn((short)2003, true);
        p.getCommunicator().sendToggleQuickbarBtn((short)2004, true);
        p.getCommunicator().sendToggleQuickbarBtn((short)2010, true);
        p.getCommunicator().sendToggleQuickbarBtn((short)2013, true);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\stages\FinalStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */