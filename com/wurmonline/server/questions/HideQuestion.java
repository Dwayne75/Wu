package com.wurmonline.server.questions;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.Properties;
import java.util.Random;

public final class HideQuestion
  extends Question
{
  public HideQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 70, aTarget);
  }
  
  public void answer(Properties answers)
  {
    if (getResponder().getPower() >= 2)
    {
      boolean putOnSurface = false;
      String key2 = "putonsurf";
      String val2 = answers.getProperty("putonsurf");
      if ((val2 != null) && (val2.equals("true"))) {
        putOnSurface = true;
      }
      String key = "height";
      String val = answers.getProperty("height");
      if (((val != null) && (val.length() > 0)) || (putOnSurface)) {
        try
        {
          int x = val == null ? 0 : Integer.parseInt(val);
          try
          {
            Item i = Items.getItem(this.target);
            short rock = Tiles.decodeHeight(Server.rockMesh.getTile(getResponder().getCurrentTile().tilex, 
              getResponder().getCurrentTile().tiley));
            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(
              getResponder().getCurrentTile().tilex, getResponder().getCurrentTile().tiley));
            int diff = height - rock;
            if ((i.getOwnerId() == -10L) || (i.getOwnerId() == getResponder().getWurmId())) {
              if ((x < diff) || (putOnSurface))
              {
                Items.hideItem(getResponder(), i, (rock + x) / 10.0F, putOnSurface);
                if (putOnSurface) {
                  getResponder().getCommunicator().sendNormalServerMessage("You carefully hide the " + i
                    .getName() + " here.");
                } else {
                  getResponder().getCommunicator().sendNormalServerMessage("You carefully hide the " + i
                    .getName() + " at " + (rock + x) / 10.0F + " meters.");
                }
              }
              else
              {
                getResponder().getCommunicator().sendNormalServerMessage("You can not hide the " + i
                  .getName() + " at " + (rock + x) + ". Rock is at " + rock + ", and surface is at " + height + ".");
              }
            }
          }
          catch (NoSuchItemException nsi)
          {
            getResponder().getCommunicator().sendNormalServerMessage("The item can no longer be found!");
          }
        }
        catch (NumberFormatException nf)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Failed to parse " + val + " as an integer number.");
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    try
    {
      Item it = Items.getItem(this.target);
      buf.append("text{type='';text='Hiding " + it.getName() + ".'}");
      if (!getResponder().isOnSurface())
      {
        buf.append("text{type='';text='You can only hide items on the surface now.'}");
      }
      else
      {
        short rock = Tiles.decodeHeight(Server.rockMesh.getTile(getResponder().getCurrentTile().tilex, 
          getResponder().getCurrentTile().tiley));
        short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(getResponder().getCurrentTile().tilex, 
          getResponder().getCurrentTile().tiley));
        int diff = height - rock;
        buf.append("text{type='';text='The rock is at " + rock + " decimeter, soil at " + height + " decimeter above sea level. Suggested height above rock is " + diff / 2 + " decimeter.'}");
        if (diff > 3) {
          buf.append("harray{input{id='height'; maxchars='4'; text='" + diff / 2 + "'}label{text='Height in decimeters over rock layer'}}");
        } else {
          buf.append("text{type='';text='The soil here is too shallow.'}");
        }
        buf.append("harray{label{text=\"Just put on surface \"};checkbox{id=\"putonsurf\";selected=\"false\"};}");
        buf.append("text{type='';text='Here is a random location position for treasure hunts:'}");
        findTreasureHuntLocation(buf);
      }
    }
    catch (NoSuchItemException localNoSuchItemException) {}
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private final void findTreasureHuntLocation(StringBuilder buf)
  {
    for (int x = 0; x < 10; x++)
    {
      int suggx = Server.rand.nextInt(Zones.worldTileSizeX);
      int suggy = Server.rand.nextInt(Zones.worldTileSizeY);
      short rock = Tiles.decodeHeight(Server.rockMesh.getTile(suggx, suggy));
      short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(suggx, suggy));
      if (height > 0)
      {
        int diff = height - rock;
        if (diff >= 2)
        {
          buf.append("text{type='';text='Tile at " + suggx + ", " + suggy + " has depth " + diff + "'}");
          break;
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\HideQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */