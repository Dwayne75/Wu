package com.wurmonline.server.spells;

import com.wurmonline.mesh.BushData.BushType;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.TreeData.TreeType;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class Cleanse
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  public Cleanse()
  {
    super("Cleanse", 930, 30, 26, 30, 33, 0L);
    this.targetTile = true;
    this.description = "cleanses a small area of mycelium infected land";
    this.type = 1;
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer)
  {
    if (performer.getLayer() < 0)
    {
      performer.getCommunicator().sendNormalServerMessage("This spell does not work below ground.", (byte)3);
      return false;
    }
    if (Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)) < 0)
    {
      performer.getCommunicator().sendNormalServerMessage("This spell does not work below water.", (byte)3);
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    performer.getCommunicator().sendNormalServerMessage("An invigorating energy flows through you into the ground and reaches the roots of plants and trees.");
    
    int sx = Zones.safeTileX(tilex - 1 - performer.getNumLinks());
    int sy = Zones.safeTileY(tiley - 1 - performer.getNumLinks());
    int ex = Zones.safeTileX(tilex + 1 + performer.getNumLinks());
    int ey = Zones.safeTileY(tiley + 1 + performer.getNumLinks());
    boolean blocked = false;
    for (int x = sx; x <= ex; x++) {
      for (int y = sy; y <= ey; y++) {
        try
        {
          FaithZone fz = Zones.getFaithZone(x, y, true);
          
          boolean ok = false;
          if (fz != null)
          {
            if ((fz.getCurrentRuler() == null) || (fz.getCurrentRuler() == performer.getDeity()) || (!fz.getCurrentRuler().isHateGod())) {
              ok = true;
            }
          }
          else {
            ok = true;
          }
          if (ok)
          {
            VolaTile t = Zones.getOrCreateTile(x, y, true);
            if ((t == null) || (t.getVillage() == null) || (t.getVillage().kingdom == performer.getKingdomId()))
            {
              int tile = Server.surfaceMesh.getTile(x, y);
              byte type = Tiles.decodeType(tile);
              Tiles.Tile theTile = Tiles.getTile(type);
              byte data = Tiles.decodeData(tile);
              if ((type == Tiles.Tile.TILE_DIRT.id) || (type == Tiles.Tile.TILE_MYCELIUM_LAWN.id) || (type == Tiles.Tile.TILE_MYCELIUM.id) || 
              
                (theTile.isMyceliumTree()) || 
                (theTile.isMyceliumBush()))
              {
                if (theTile.isMyceliumTree()) {
                  Server.setSurfaceTile(x, y, 
                    Tiles.decodeHeight(tile), theTile.getTreeType(data).asNormalTree(), data);
                } else if (theTile.isMyceliumBush()) {
                  Server.setSurfaceTile(x, y, 
                    Tiles.decodeHeight(tile), theTile.getBushType(data).asNormalBush(), data);
                } else if (type == Tiles.Tile.TILE_MYCELIUM_LAWN.id) {
                  Server.setSurfaceTile(x, y, 
                    Tiles.decodeHeight(tile), Tiles.Tile.TILE_LAWN.id, (byte)0);
                } else {
                  Server.setSurfaceTile(x, y, 
                    Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)0);
                }
                Players.getInstance().sendChangedTile(x, y, true, false);
              }
            }
            else
            {
              blocked = true;
            }
          }
          else
          {
            blocked = true;
          }
        }
        catch (NoSuchZoneException localNoSuchZoneException) {}
      }
    }
    if (blocked) {
      performer.getCommunicator().sendNormalServerMessage("The domain of another deity or settlement protects this area.", (byte)3);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\Cleanse.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */