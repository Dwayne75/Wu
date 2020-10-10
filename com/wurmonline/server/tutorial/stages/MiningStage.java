package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.PlayerTutorial.PlayerTrigger;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.TutorialStage.TutorialSubStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;
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
    this.subStages.add(new DigRockSubStage(getPlayerId()));
    this.subStages.add(new MineIronSubStage(getPlayerId()));
  }
  
  public class DigRockSubStage
    extends TutorialStage.TutorialSubStage
  {
    public DigRockSubStage(long playerId)
    {
      super(playerId);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Mining", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nMany things in Wurm need metals in their creation process, and to get these metals you will need to get underground in order to mine them.\r\n\r\nThe first step to opening a mine is completing the Tunnel action on a Rock tile with a Pickaxe activated.\r\n\r\nRock tiles can be found in the world, or uncovered by digging up all of the dirt or sand on a tile. Once all 4 corners of a tile have no more dirt or sand to dig the tile will turn to Rock.\r\n\r\n", null, null, null, 300, 400)
        
        .addImage("image.tutorial.rock", 300, 150)
        .addText("").toString()), null, 
        
        BMLBuilder.createLeftAlignedNode(
        BMLBuilder.createHorizArrayNode(false)
        .addButton("back", "Back", 80, 20, true)
        .addText("", null, null, null, 35, 0)
        .addButton("next", "Next", 80, 20, true)));
      
      this.bmlString = builder.toString();
    }
  }
  
  public class MineIronSubStage
    extends TutorialStage.TutorialSubStage
  {
    public MineIronSubStage(long playerId)
    {
      super(playerId);
      setNextTrigger(PlayerTutorial.PlayerTrigger.MINE_IRON);
    }
    
    protected void buildBMLString()
    {
      BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
        BMLBuilder.createCenteredNode(
        BMLBuilder.createVertArrayNode(false)
        .addText("")
        .addHeader("Mining", Color.LIGHT_GRAY)), null, 
        
        BMLBuilder.createVertArrayNode(false)
        .addPassthrough("tutorialid", Long.toString(getPlayerId()))
        .addText("\r\nOnce you have opened a tunnel, mining deeper is a matter of destroying cave walls using the Mine action.\r\n\r\nOccasionally you will find metal ore veins or special stone veins such as marble, slate and sandstone. These veins may take a lot longer to destroy than normal rock walls.\r\n\r\nRock, marble, slate and sandstone shards can be shaped into bricks and other items for use in Masonry, and metal ores can be smelted down in a Furnace or Smelter for useable metal lumps.\r\n\r\nMine some Iron Ore to continue.\r\n\r\n", null, null, null, 300, 400)
        
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\stages\MiningStage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */