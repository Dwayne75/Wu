package com.wurmonline.server.questions;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.Zones;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OreQuestion
  extends Question
{
  private final int tilex;
  private final int tiley;
  private int numtiles;
  private static final String NOCHANGE = "No change";
  private final Item rod;
  private final Map<Integer, Integer> oretiles = new HashMap();
  
  public OreQuestion(Creature _responder, int _tilex, int _tiley, Item _rod)
  {
    super(_responder, "Selecting Ore Type", "Which ore type should this tile have?", 82, _rod.getWurmId());
    this.tilex = _tilex;
    this.tiley = _tiley;
    this.rod = _rod;
  }
  
  public void answer(Properties answers)
  {
    if (this.type == 82)
    {
      if (this.rod.deleted)
      {
        getResponder().getCommunicator().sendNormalServerMessage("The rod has been destroyed.");
        return;
      }
      if (this.rod.getOwnerId() != getResponder().getWurmId())
      {
        getResponder().getCommunicator().sendNormalServerMessage("You need to be in possession of the rod.");
        return;
      }
      if (this.rod.getWurmId() == this.target)
      {
        String d1 = answers.getProperty("data1");
        if (d1 != null)
        {
          int index = 0;
          try
          {
            index = Integer.parseInt(d1);
          }
          catch (Exception ex)
          {
            getResponder().getCommunicator().sendNormalServerMessage(d1 + " was selected - Error. No change.");
            return;
          }
          if (index == this.numtiles)
          {
            getResponder().getCommunicator().sendNormalServerMessage("You decide to change nothing.");
            return;
          }
          if (getResponder().getLogger() != null) {
            getResponder().getLogger().log(Level.INFO, 
              getResponder() + " setting ore " + this.tilex + ", " + this.tiley + " to : " + d1);
          }
          Integer newType = (Integer)this.oretiles.get(Integer.valueOf(index));
          if (newType == null)
          {
            getResponder().getCommunicator().sendNormalServerMessage("Invalid choice " + index + ".");
            return;
          }
          byte nt = (byte)newType.intValue();
          if (getResponder().isOnSurface())
          {
            getResponder().getCommunicator().sendNormalServerMessage("Please enter the cave.");
            return;
          }
          if (Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley)) == nt)
          {
            getResponder().getCommunicator().sendNormalServerMessage("The terrain is already of that type.");
            return;
          }
          if (!Tiles.isOreCave(nt))
          {
            getResponder().getCommunicator().sendNormalServerMessage("The rod must set to ore of some kind.");
            return;
          }
          if (Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_CAVE_WALL.id)
          {
            Server.caveMesh.setTile(this.tilex, this.tiley, Tiles.encode(Tiles.decodeHeight(Server.caveMesh.getTile(this.tilex, this.tiley)), nt, 
              Tiles.decodeData(Server.caveMesh.getTile(this.tilex, this.tiley))));
            Players.getInstance().sendChangedTile(this.tilex, this.tiley, false, true);
            getResponder().getCommunicator().sendNormalServerMessage("The wall changes to purest " + 
              Tiles.getTile(nt).tiledesc + " and the rod vanishes!");
            Server.setCaveResource(this.tilex, this.tiley, 10000);
            Zones.setMiningState(this.tilex, this.tiley, (byte)-1, false);
            Items.destroyItem(this.rod.getWurmId());
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("You need to use this on a cave wall.");
          }
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    
    buf.append("harray{label{text='Tile type'}dropdown{id='data1';options=\"");
    
    Tiles.Tile[] tiles = Tiles.Tile.getTiles();
    for (int x = 0; x < tiles.length; x++) {
      if (tiles[x] != null) {
        if (Tiles.isOreCave(tiles[x].id)) {
          if ((tiles[x].id != Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) && (tiles[x].id != Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id))
          {
            this.oretiles.put(Integer.valueOf(this.numtiles), Integer.valueOf(x));
            this.numtiles += 1;
            buf.append(tiles[x].tiledesc);
            buf.append(",");
          }
        }
      }
    }
    buf.append("No change");
    buf.append("\"}}");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\OreQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */