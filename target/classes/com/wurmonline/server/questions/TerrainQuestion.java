package com.wurmonline.server.questions;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TerrainQuestion
  extends Question
{
  private Item wand;
  private final int tilex;
  private final int tiley;
  private List<Integer> tilelist = null;
  private int category = 0;
  private String filter = "*";
  private static final String NOCHANGE = "No change";
  
  public TerrainQuestion(Creature _responder, Item wand, int _tilex, int _tiley)
  {
    super(_responder, "Changing the terrain", "Which tile type should this tile have?", 52, _responder.getWurmId());
    this.wand = wand;
    this.tilex = _tilex;
    this.tiley = _tiley;
  }
  
  public TerrainQuestion(Creature _responder, Item wand, int _tilex, int _tiley, int category, String filter)
  {
    this(_responder, wand, _tilex, _tiley);
    this.category = category;
    this.filter = filter;
  }
  
  public void answer(Properties answers)
  {
    if (this.type == 52) {
      if (getResponder().getWurmId() == this.target)
      {
        String cat = answers.getProperty("cat");
        int catn = Integer.valueOf(cat).intValue();
        String newFilter = answers.getProperty("filtertext");
        if ((newFilter == null) || (newFilter.length() == 0)) {
          newFilter = "*";
        }
        String buttonChangeCat = answers.getProperty("changecat");
        String buttonFilter = answers.getProperty("filterme");
        if (((buttonChangeCat != null) && (buttonChangeCat.equals("true"))) || ((buttonFilter != null) && (buttonFilter.equals("true"))))
        {
          TerrainQuestion tq = new TerrainQuestion(getResponder(), this.wand, this.tilex, this.tiley, catn, newFilter);
          tq.sendQuestion();
          return;
        }
        boolean auto = false;
        int sizex = 0;
        int sizey = 0;
        String autoStr = answers.getProperty("auto");
        if ((autoStr != null) && (autoStr.equals("true")))
        {
          auto = true;
          String sizexStr = answers.getProperty("sizex", "0");
          String sizeyStr = answers.getProperty("sizey", "0");
          if ((sizexStr.length() > 0) && (sizeyStr.length() > 0))
          {
            sizex = Integer.valueOf(sizexStr).intValue();
            sizey = Integer.valueOf(sizeyStr).intValue();
          }
        }
        String d1 = answers.getProperty("data1");
        if (d1 != null)
        {
          int ttype = 0;
          try
          {
            ttype = ((Integer)this.tilelist.get(Integer.parseInt(d1))).intValue();
          }
          catch (Exception ex)
          {
            getResponder().getCommunicator().sendNormalServerMessage(d1 + " was selected - Error. No change.");
            return;
          }
          if (ttype == 0)
          {
            getResponder().getCommunicator().sendNormalServerMessage("You decide to change nothing.");
            return;
          }
          Tiles.Tile theTile = Tiles.getTile(ttype);
          if (getResponder().getLogger() != null) {
            getResponder().getLogger().log(Level.INFO, 
              getResponder() + " changing tile " + this.tilex + ", " + this.tiley + " to : " + theTile.getDesc() + " (" + theTile.getId() + ")");
          }
          getResponder().getCommunicator().sendNormalServerMessage("Trying to change tile " + this.tilex + ", " + this.tiley + " to : " + theTile
            .getDesc() + " (" + theTile.getId() + ").");
          byte newType = (byte)ttype;
          if (auto)
          {
            this.wand.setAuxData(newType);
            if ((sizex > 0) && (sizey > 0))
            {
              this.wand.setData1(sizex);
              this.wand.setData2(sizey);
              getResponder().getCommunicator().sendNormalServerMessage("wand setup for " + theTile
                .getDesc() + " (" + theTile.getId() + ") and x,y of " + sizex + "," + sizey + ".");
            }
            else
            {
              getResponder().getCommunicator().sendNormalServerMessage("wand setup for " + theTile
                .getDesc() + " (" + theTile.getId() + ").");
            }
          }
          if (getResponder().isOnSurface())
          {
            if (Tiles.decodeType(Server.surfaceMesh.getTile(this.tilex, this.tiley)) == newType)
            {
              getResponder().getCommunicator().sendNormalServerMessage("The terrain is already of that type.");
              
              return;
            }
            if ((theTile.isSolidCave()) || (newType == Tiles.Tile.TILE_CAVE.id) || 
            
              (theTile.isReinforcedCave()))
            {
              getResponder().getCommunicator().sendNormalServerMessage("You cannot set the surface terrain to some sort of rock.");
              
              return;
            }
            if ((getResponder().getPower() < 5) && ((newType == Tiles.Tile.TILE_ROCK.id) || 
            
              (Tiles.decodeType(Server.surfaceMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_ROCK.id) || (newType == Tiles.Tile.TILE_CLIFF.id) || 
              
              (Tiles.decodeType(Server.surfaceMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_CLIFF.id)))
            {
              getResponder().getCommunicator().sendNormalServerMessage("That would have impact on the rock layer, and is not allowed for now.");
              
              return;
            }
            if (newType == Tiles.Tile.TILE_ROCK.id)
            {
              Server.caveMesh.setTile(this.tilex, this.tiley, 
              
                Tiles.encode((short)-100, Tiles.Tile.TILE_CAVE_WALL.id, (byte)0));
              
              Server.rockMesh
                .setTile(this.tilex, this.tiley, 
                
                Tiles.encode(Tiles.decodeHeight(Server.surfaceMesh.getTile(this.tilex, this.tiley)), Tiles.Tile.TILE_ROCK.id, (byte)0));
            }
            byte tileData = 0;
            if ((Tiles.isBush(newType)) || (Tiles.isTree(newType))) {
              tileData = 1;
            }
            Server.setSurfaceTile(this.tilex, this.tiley, 
              Tiles.decodeHeight(Server.surfaceMesh.getTile(this.tilex, this.tiley)), newType, tileData);
            if ((newType == Tiles.Tile.TILE_FIELD.id) || (newType == Tiles.Tile.TILE_FIELD2.id)) {
              Server.setWorldResource(this.tilex, this.tiley, 0);
            } else {
              Server.setWorldResource(this.tilex, this.tiley, -1);
            }
            Players.getInstance().sendChangedTile(this.tilex, this.tiley, true, true);
            return;
          }
          Terraforming.paintCaveTerrain((Player)getResponder(), newType, this.tilex, this.tiley);
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("harray{label{text=\"Category\"}dropdown{id=\"cat\";default=\"" + this.category + "\"options=\"All,Bushes,Cave,Minedoors,Normal,Paving,Surface,Trees\"}label{text=\" Filter:\"};input{maxchars=\"30\";id=\"filtertext\";text=\"" + this.filter + "\";onenter=\"filterme\"}label{text=\" \"};button{text=\"Update list\";id=\"filterme\"}}");
    
    buf.append("label{text=\"\"}");
    
    this.tilelist = new ArrayList();
    buf.append("harray{label{text='Tile type'}dropdown{id='data1';options=\"");
    Tiles.Tile[] tiles = Tiles.Tile.getTiles(this.category, this.filter);
    if (tiles.length == 0)
    {
      this.tilelist.add(Integer.valueOf(0));
      buf.append("No change");
    }
    else if (tiles.length == 1)
    {
      buf.append(tiles[0].tiledesc + " (" + (tiles[0].id & 0xFF) + ")");
      this.tilelist.add(Integer.valueOf(tiles[0].id));
    }
    else
    {
      this.tilelist.add(Integer.valueOf(0));
      buf.append("No change");
      
      Arrays.sort(tiles, new TerrainQuestion.1(this));
      for (int x = 0; x < tiles.length; x++) {
        if (tiles[x] != null)
        {
          buf.append("," + tiles[x].tiledesc + " (" + (tiles[x].id & 0xFF) + ")");
          this.tilelist.add(Integer.valueOf(tiles[x].id));
        }
      }
    }
    buf.append("\"}}");
    buf.append("label{text=\"\"}");
    
    buf.append("checkbox{id=\"auto\";text=\"Check this if you want the aux byte set on the wand for paint terrain.\"};");
    buf.append("harray{label{text=\"You can also set the area to paint, max is 9 in either direction \"};input{id=\"sizex\";text=\"\";maxchars=\"1\"};label{text=\" \"};input{id=\"sizey\";text=\"\";maxchars=\"1\"};}");
    
    buf.append("label{text=\"\"}");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(370, 240, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\TerrainQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */