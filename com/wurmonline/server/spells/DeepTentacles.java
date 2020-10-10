package com.wurmonline.server.spells;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class DeepTentacles
  extends ReligiousSpell
{
  public static final int RANGE = 24;
  public static final double BASE_DAMAGE = 400.0D;
  public static final double DAMAGE_PER_SECOND = 1.0D;
  public static final int RADIUS = 1;
  
  public DeepTentacles()
  {
    super("Tentacles", 418, 10, 30, 20, 33, 120000L);
    this.targetTile = true;
    this.offensive = true;
    this.description = "covers an area with bludgeoning tentacles that damage enemies over time";
    this.type = 2;
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer)
  {
    if (layer < 0)
    {
      int tile = Server.caveMesh.getTile(tilex, tiley);
      if (Tiles.isSolidCave(Tiles.decodeType(tile)))
      {
        performer.getCommunicator().sendNormalServerMessage("The spell doesn't work there.", (byte)3);
        
        return false;
      }
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    int tile = Server.surfaceMesh.getTile(tilex, tiley);
    if (layer < 0)
    {
      tile = Server.caveMesh.getTile(tilex, tiley);
      
      byte type = Tiles.decodeType(tile);
      if (Tiles.isSolidCave(type))
      {
        performer.getCommunicator().sendNormalServerMessage("You fail to find a spot to direct the power to.", (byte)3);
        
        return;
      }
    }
    Structure currstr = performer.getCurrentTile().getStructure();
    performer.getCommunicator().sendNormalServerMessage("Waving tentacles appear around the " + 
      Tiles.getTile(Tiles.decodeType(tile)).tiledesc.toLowerCase() + ".");
    int sx = Zones.safeTileX(tilex - 1 - performer.getNumLinks());
    int ex = Zones.safeTileX(tilex + 1 + performer.getNumLinks());
    int sy = Zones.safeTileY(tiley - 1 - performer.getNumLinks());
    int ey = Zones.safeTileY(tiley + 1 + performer.getNumLinks());
    
    calculateArea(sx, sy, ex, ey, tilex, tiley, layer, currstr);
    for (int x = sx; x <= ex; x++) {
      for (int y = sy; y <= ey; y++)
      {
        int currAreaX = x - sx;
        int currAreaY = y - sy;
        if (this.area[currAreaX][currAreaY] == 0) {
          new AreaSpellEffect(performer.getWurmId(), x, y, layer, (byte)34, System.currentTimeMillis() + 1000L * (30 + (int)power / 10), (float)power * 1.5F, layer, 0, true);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\DeepTentacles.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */