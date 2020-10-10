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

public class InventoryStage
  extends TutorialStage
{
  private static final short WINDOW_ID = 400;
  
  public short getWindowId()
  {
    return (short)(400 + getCurrentSubStage());
  }
  
  public InventoryStage(long playerId)
  {
    super(playerId);
  }
  
  public TutorialStage getNextStage()
  {
    return new StartingStage(getPlayerId());
  }
  
  public TutorialStage getLastStage()
  {
    return new MovementStage(getPlayerId());
  }
  
  public void buildSubStages()
  {
    this.subStages.add(new InventorySubStage(getPlayerId()));
    this.subStages.add(new MoveItemsSubStage(getPlayerId()));
    this.subStages.add(new QualitySubStage(getPlayerId()));
  }
  
  public class InventorySubStage
    extends TutorialStage.TutorialSubStage
  {
    public InventorySubStage(long playerId)
    {
      super(playerId);
      setNextTrigger(PlayerTutorial.PlayerTrigger.ENABLED_INVENTORY);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Inventory & Items", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nPress [$bind:'toggle inventory'$] or click on the inventory button to open your inventory.\r\n\r\n", null, null, null, 300, 400)
        
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
        p.getCommunicator().sendToggleQuickbarBtn((short)2007, true);
      }
      catch (NoSuchPlayerException localNoSuchPlayerException) {}
    }
  }
  
  public class MoveItemsSubStage
    extends TutorialStage.TutorialSubStage
  {
    public MoveItemsSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Inventory & Items", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nIn this window you can select items, move them around, and view any containers and items inside containers that are in your inventory.\r\n\r\n", null, null, null, 300, 400)
        
        .addImage("image.tutorial.moveitems", 300, 150)
        .addText("\r\nWhen opening any container item in the world (such as a chest), you will have a similar window show up for that item where you can interact with any items that are stored inside.\r\n\r\n", null, null, null, 300, 400)
        
        .addText("").toString()), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class QualitySubStage
    extends TutorialStage.TutorialSubStage
  {
    public QualitySubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Inventory & Items", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nEach item in Wurm has an associated Quality Level (QL) and Damage, both between 0 and 100.\r\n\r\n", null, null, null, 300, 400)
        
        .addImage("image.tutorial.qldmg", 300, 150)
        .addText("\r\nAn item with a higher QL will be more effective than a lower QL item and will generally last longer.\r\n\r\nAs an item is used it will gain Damage which lowers the effective QL of the item, and will cause it to be destroyed if the Damage ever reaches 100 without being repaired.\r\n\r\n", null, null, null, 300, 400)
        
        .addText("").toString()), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\stages\InventoryStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */