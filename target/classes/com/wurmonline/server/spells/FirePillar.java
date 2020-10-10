package com.wurmonline.server.spells;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.Random;
import java.util.logging.Logger;

public class FirePillar
  extends ReligiousSpell
  implements AttitudeConstants
{
  private static Logger logger = Logger.getLogger(FirePillar.class.getName());
  public static final int RANGE = 24;
  public static final double BASE_DAMAGE = 300.0D;
  public static final double DAMAGE_PER_SECOND = 2.75D;
  public static final int RADIUS = 2;
  
  public FirePillar()
  {
    super("Fire Pillar", 420, 10, 30, 10, 37, 120000L);
    
    this.targetTile = true;
    this.offensive = true;
    this.description = "covers an area with fire dealing damage to enemies over time";
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
    if (layer < 0) {
      tile = Server.caveMesh.getTile(tilex, tiley);
    }
    byte type = Tiles.decodeType(tile);
    if (Tiles.isSolidCave(type))
    {
      performer.getCommunicator().sendNormalServerMessage("You fail to find a spot to direct the power to.", (byte)3);
      
      return;
    }
    performer.getCommunicator().sendNormalServerMessage("You heat the air around the " + 
      Tiles.getTile(Tiles.decodeType(tile)).tiledesc.toLowerCase() + ".");
    Structure currstr = performer.getCurrentTile().getStructure();
    int sx = Zones.safeTileX(tilex - 2 - performer.getNumLinks());
    int ex = Zones.safeTileX(tilex + 2 + performer.getNumLinks());
    int sy = Zones.safeTileY(tiley - 2 - performer.getNumLinks());
    int ey = Zones.safeTileY(tiley + 2 + performer.getNumLinks());
    
    VolaTile tileTarget = Zones.getOrCreateTile(tilex, tiley, layer >= 0);
    Structure targetStructure = null;
    if (heightOffset != -1) {
      targetStructure = tileTarget.getStructure();
    }
    calculateAOE(sx, sy, ex, ey, tilex, tiley, layer, currstr, targetStructure, heightOffset);
    for (int x = sx; x <= ex; x++) {
      for (int y = sy; y <= ey; y++) {
        if (((tilex == x) && (y == tiley) ? 1 : 0) == 0)
        {
          int currAreaX = x - sx;
          int currAreaY = y - sy;
          if (this.area[currAreaX][currAreaY] == 0) {
            new AreaSpellEffect(performer.getWurmId(), x, y, layer, (byte)51, System.currentTimeMillis() + 1000L * (30 + (int)power / 10), (float)power * 2.0F, layer, this.offsets[currAreaX][currAreaY], true);
          }
        }
        else
        {
          int groundHeight = 0;
          if ((targetStructure == null) || (targetStructure.isTypeHouse()))
          {
            float[] hts = Zones.getNodeHeights(tilex, tiley, layer, -10L);
            
            float h = hts[0] * 0.5F * 0.5F + hts[1] * 0.5F * 0.5F + hts[2] * 0.5F * 0.5F + hts[3] * 0.5F * 0.5F;
            
            groundHeight = (int)(h * 10.0F);
          }
          new AreaSpellEffect(performer.getWurmId(), x, y, layer, (byte)35, System.currentTimeMillis() + 1000L * (30 + (int)power / 10), (float)power * 2.0F, layer, heightOffset + groundHeight, true);
          if (Server.rand.nextInt(1000) < power) {
            if (layer >= 0) {
              if ((Tiles.canSpawnTree(type)) || (Tiles.isTree(type)) || (Tiles.isEnchanted(type)))
              {
                Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_SAND.id, (byte)0);
                
                Players.getInstance().sendChangedTile(x, y, true, true);
              }
            }
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\FirePillar.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */