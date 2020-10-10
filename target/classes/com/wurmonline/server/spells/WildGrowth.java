package com.wurmonline.server.spells;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Crops;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WildGrowth
  extends ReligiousSpell
{
  private static final Logger logger = Logger.getLogger(WildGrowth.class.getName());
  public static final int RANGE = 40;
  
  public WildGrowth()
  {
    super("Wild Growth", 436, 30, 40, 40, 41, 0L);
    this.targetTile = true;
    this.description = "fields and trees are nurtured";
    this.type = 1;
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer)
  {
    if (performer.getLayer() < 0)
    {
      performer.getCommunicator().sendNormalServerMessage("This spell does not work below ground.", (byte)3);
      
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    performer.getCommunicator().sendNormalServerMessage("An invigorating energy flows through you into the ground and reaches the roots of plants and trees.");
    
    int sx = Zones.safeTileX(tilex - (int)Math.max(1.0D, power / 20.0D) - performer.getNumLinks());
    int sy = Zones.safeTileY(tiley - (int)Math.max(1.0D, power / 20.0D) - performer.getNumLinks());
    int ex = Zones.safeTileX(tilex + (int)Math.max(1.0D, power / 20.0D) + performer.getNumLinks());
    int ey = Zones.safeTileY(tiley + (int)Math.max(1.0D, power / 20.0D) + performer.getNumLinks());
    boolean sentMessage = false;
    for (int x = sx; x <= ex; x++) {
      for (int y = sy; y <= ey; y++)
      {
        int tile = Server.surfaceMesh.getTile(x, y);
        byte type = Tiles.decodeType(tile);
        Tiles.Tile theTile = Tiles.getTile(type);
        if (performer.isOnSurface())
        {
          VolaTile t = Zones.getTileOrNull(x, y, true);
          if (t != null) {
            if ((t.getVillage() == null) || 
              (t.getVillage().isActionAllowed((short)468, performer, false, 0, 0)))
            {
              for (Fence fence : t.getFences()) {
                if (fence.isHedge()) {
                  if ((!fence.isHighHedge()) && (fence.getType() != StructureConstantsEnum.HEDGE_FLOWER1_LOW) && 
                    (fence.getType() != StructureConstantsEnum.HEDGE_FLOWER3_MEDIUM))
                  {
                    fence.setDamage(0.0F);
                    fence.setType(StructureConstantsEnum.getEnumByValue((short)(fence.getType().value + 1)));
                    try
                    {
                      fence.save();
                      t.updateFence(fence);
                    }
                    catch (IOException iox)
                    {
                      logger.log(Level.WARNING, x + "," + y + " " + iox.getMessage(), iox);
                    }
                  }
                }
              }
            }
            else if (!sentMessage)
            {
              performer.getCommunicator().sendNormalServerMessage("You are not allowed to affect the hedges in " + t
                .getVillage().getName() + ".", (byte)3);
              
              sentMessage = true;
            }
          }
        }
        if ((type == Tiles.Tile.TILE_FIELD.id) || (type == Tiles.Tile.TILE_FIELD2.id))
        {
          int worldResource = Server.getWorldResource(x, y);
          int farmedCount = worldResource >>> 11;
          int farmedChance = worldResource & 0x7FF;
          farmedChance = (int)Math.min(farmedChance + power * 2.0D + 75.0D, 2047.0D);
          Server.setWorldResource(x, y, (farmedCount << 11) + farmedChance);
          
          byte data = Tiles.decodeData(tile);
          int tileAge = Crops.decodeFieldAge(data);
          int crop = Crops.getCropNumber(type, data);
          if (tileAge < 7)
          {
            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), type, 
              Crops.encodeFieldData(true, tileAge, crop));
            Players.getInstance().sendChangedTile(x, y, true, false);
          }
        }
        else if ((theTile.isNormalTree()) || (theTile.isNormalBush()))
        {
          int age = Tiles.decodeData(tile) >> 4 & 0xF;
          int halfdata = Tiles.decodeData(tile) & 0xF;
          
          Server.setWorldResource(x, y, 0);
          if (age < 15)
          {
            int newData = (age + 1 << 4) + halfdata & 0xFF;
            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), type, (byte)newData);
          }
          else
          {
            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)0);
          }
          Players.getInstance().sendChangedTile(x, y, true, false);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\WildGrowth.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */