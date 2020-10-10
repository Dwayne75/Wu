package com.wurmonline.server.behaviours;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.List;

final class TileCornerBehaviour
  extends Behaviour
  implements MiscConstants
{
  TileCornerBehaviour()
  {
    super((short)54);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset)
  {
    List<ActionEntry> toReturn = new LinkedList();
    byte type = Tiles.decodeType(tile);
    toReturn.addAll(super.getBehavioursFor(performer, source, tilex, tiley, onSurface, corner, tile, heightOffset));
    if ((performer.getPower() >= 4) && (source.getTemplateId() == 176) && (heightOffset == 0)) {
      toReturn.add(Actions.actionEntrys['Ȇ']);
    }
    if ((onSurface) && (source.isDiggingtool()) && (heightOffset == 0) && (type != Tiles.Tile.TILE_ROCK.id)) {
      toReturn.add(Actions.actionEntrys['']);
    }
    if ((source.isMiningtool()) && (heightOffset == 0) && (((onSurface) && (type == Tiles.Tile.TILE_ROCK.id)) || (!onSurface))) {
      toReturn.add(Actions.actionEntrys['']);
    } else if ((source.getTemplateId() == 782) && (heightOffset == 0)) {
      toReturn.add(Actions.actionEntrys['Ȇ']);
    } else if ((source.isSign()) || (source.isStreetLamp()))
    {
      if (performer.isWithinDistanceTo(tilex << 2, tiley << 2, 0.0F, 4.0F)) {
        toReturn.add(Actions.actionEntrys['°']);
      }
    }
    else if ((Features.Feature.HIGHWAYS.isEnabled()) && (source.isRoadMarker())) {
      if (!Zones.getOrCreateTile(tilex, tiley, onSurface).hasFenceOnCorner(performer.getFloorLevel())) {
        if (performer.isWithinDistanceTo(tilex << 2, tiley << 2, 0.0F, 4.0F)) {
          if (passFloorCheck(tilex, tiley, onSurface, heightOffset))
          {
            HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface, heightOffset);
            if (highwayPos != null) {
              if (((MethodsHighways.middleOfHighway(highwayPos)) && 
                (!MethodsHighways.containsMarker(highwayPos, (byte)0)) && 
                (!MethodsHighways.isNextToACamp(highwayPos))) || (performer.getPower() > 1))
              {
                byte pLinks = MethodsHighways.getPossibleLinksFrom(highwayPos, source);
                if (MethodsHighways.canPlantMarker(null, highwayPos, source, pLinks)) {
                  toReturn.add(new ActionEntry((short)176, "Plant", "planting"));
                }
                toReturn.add(new ActionEntry((short)759, "View possible protected tiles", "viewing"));
                if (pLinks != 0) {
                  toReturn.add(new ActionEntry((short)748, "View possible links", "viewing"));
                }
              }
            }
          }
        }
      }
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, corner, tile, heightOffset));
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset, short action, float counter)
  {
    boolean done = true;
    if (action == 1)
    {
      done = action(act, performer, tilex, tiley, onSurface, corner, tile, heightOffset, action, counter);
    }
    else if ((onSurface) && (action == 144) && (source.isDiggingtool()) && (heightOffset == 0))
    {
      done = Terraforming.dig(performer, source, tilex, tiley, tile, counter, true, performer
        .isOnSurface() ? Server.surfaceMesh : Server.caveMesh);
    }
    else if ((action == 145) && (source.isMiningtool()) && (heightOffset == 0))
    {
      if (onSurface)
      {
        done = TileRockBehaviour.mine(act, performer, source, tilex, tiley, action, counter, tilex, tiley);
      }
      else
      {
        TilePos digTilePos = TilePos.fromXY(tilex, tiley);
        done = CaveTileBehaviour.mine(act, performer, source, tilex, tiley, action, counter, 0, digTilePos);
      }
    }
    else if ((action == 518) && (source.getTemplateId() == 782) && (heightOffset == 0))
    {
      done = CaveTileBehaviour.raiseRockLevel(performer, source, tilex, tiley, counter, act);
    }
    else if ((action == 518) && (heightOffset == 0) && (performer.getPower() >= 4) && (source.getTemplateId() == 176))
    {
      done = CaveTileBehaviour.raiseRockLevel(performer, source, tilex, tiley, counter, act);
    }
    else if ((action == 176) && ((source.isSign()) || (source.isStreetLamp())))
    {
      if (performer.getPower() > 0) {
        done = MethodsItems.plantSignFinish(performer, source, true, tilex, tiley, onSurface, -10L, false, -1L);
      } else {
        done = MethodsItems.plantSign(performer, source, counter, true, tilex, tiley, onSurface, -10L, false, -1L);
      }
    }
    else if ((action == 176) && (source.isRoadMarker()) && (Features.Feature.HIGHWAYS.isEnabled()))
    {
      if (!Zones.getOrCreateTile(tilex, tiley, onSurface).hasFenceOnCorner(performer.getFloorLevel()))
      {
        if (passFloorCheck(tilex, tiley, onSurface, heightOffset))
        {
          HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface, heightOffset);
          if (highwayPos != null)
          {
            if ((MethodsHighways.middleOfHighway(highwayPos)) && 
              (!MethodsHighways.containsMarker(highwayPos, (byte)0)))
            {
              if ((MethodsHighways.isNextToACamp(highwayPos)) && (performer.getPower() <= 1))
              {
                performer.getCommunicator().sendNormalServerMessage("That tile corner borders onto a wagoners camp, who does not allow such actions.");
                
                return true;
              }
              byte pLinks = MethodsHighways.getPossibleLinksFrom(highwayPos, source);
              if (!MethodsHighways.canPlantMarker(performer, highwayPos, source, pLinks)) {
                done = true;
              } else if (performer.getPower() > 0) {
                done = MethodsItems.plantSignFinish(performer, source, true, highwayPos.getTilex(), highwayPos.getTiley(), highwayPos
                  .isOnSurface(), highwayPos.getBridgeId(), false, -1L);
              } else {
                done = MethodsItems.plantSign(performer, source, counter, true, highwayPos.getTilex(), highwayPos.getTiley(), highwayPos
                  .isOnSurface(), highwayPos.getBridgeId(), false, -1L);
              }
              if ((done) && (source.isPlanted())) {
                MethodsHighways.autoLink(source, pLinks);
              }
            }
            else
            {
              performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
              return true;
            }
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
            return true;
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("There is a floor in the way.");
          return true;
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("There is a fence in the way.");
        return true;
      }
    }
    else if ((action == 748) && (source.isRoadMarker()) && (Features.Feature.HIGHWAYS.isEnabled()))
    {
      if (!Zones.getOrCreateTile(tilex, tiley, onSurface).hasFenceOnCorner(performer.getFloorLevel()))
      {
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface, heightOffset);
        if (highwayPos != null)
        {
          if ((MethodsHighways.middleOfHighway(highwayPos)) && (!MethodsHighways.containsMarker(highwayPos, (byte)0)))
          {
            done = MarkerBehaviour.showLinks(performer, source, act, counter, highwayPos);
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
            return true;
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
          return true;
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("There is a fence in the way.");
        return true;
      }
    }
    else if ((action == 759) && (source.isRoadMarker()) && (Features.Feature.HIGHWAYS.isEnabled()))
    {
      HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface, heightOffset);
      if (highwayPos != null)
      {
        if ((MethodsHighways.middleOfHighway(highwayPos)) && (!MethodsHighways.containsMarker(highwayPos, (byte)0)))
        {
          done = MarkerBehaviour.showProtection(performer, source, act, counter, highwayPos);
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
          return true;
        }
      }
      else
      {
        performer.getCommunicator().sendNormalServerMessage("Not a valid tile.");
        return true;
      }
    }
    else
    {
      return action(act, performer, tilex, tiley, onSurface, corner, tile, heightOffset, action, counter);
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, boolean corner, int tile, int heightOffset, short action, float counter)
  {
    if (action == 1)
    {
      HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
      if ((highwayPos != null) && (MethodsHighways.middleOfHighway(highwayPos))) {
        performer.getCommunicator().sendNormalServerMessage("This outlines where signs and road markers could be planted.");
      } else {
        performer.getCommunicator().sendNormalServerMessage("This outlines where signs can be planted.");
      }
      if (performer.getPower() > 2)
      {
        int meshtile = Server.surfaceMesh.getTile(tilex, tiley);
        
        short tileHeight = Tiles.decodeHeight(meshtile);
        int rockTile = Server.rockMesh.getTile(tilex, tiley);
        short rockHeight = Tiles.decodeHeight(rockTile);
        performer.getCommunicator().sendNormalServerMessage("Height Surface:" + tileHeight + " Rock:" + rockHeight + ".");
        
        int currtile = Server.caveMesh.getTile(tilex, tiley);
        short currHeight = Tiles.decodeHeight(currtile);
        short cceil = (short)(Tiles.decodeData(currtile) & 0xFF);
        if (currHeight == -100) {
          performer.getCommunicator().sendNormalServerMessage("No cave.");
        } else {
          performer.getCommunicator().sendNormalServerMessage("Height Cave Floor:" + currHeight + " Ceiling:" + (currHeight + cceil) + ".");
        }
      }
      if ((tilex - 2 < 0) || (tilex + 2 > 1 << Constants.meshSize) || (tiley - 2 < 0) || (tiley + 2 > 1 << Constants.meshSize))
      {
        performer.getCommunicator().sendNormalServerMessage("The water is too deep to measure.");
        return true;
      }
    }
    return true;
  }
  
  private static boolean passFloorCheck(int tilex, int tiley, boolean onSurface, int heightOffset)
  {
    if (heightOffset != 0) {
      return true;
    }
    Floor[] floors = null;
    Structure structure = Structures.getStructureForTile(tilex, tiley, onSurface);
    if ((structure != null) && (structure.isTypeHouse())) {
      floors = Zones.getFloorsAtTile(tilex, tiley, 0, 0, onSurface);
    }
    if ((structure == null) || (!structure.isTypeHouse()) || (floors == null)) {
      structure = Structures.getStructureForTile(tilex - 1, tiley, onSurface);
    }
    if ((structure != null) && (structure.isTypeHouse()) && (floors == null)) {
      floors = Zones.getFloorsAtTile(tilex - 1, tiley, 0, 0, onSurface);
    }
    if ((structure == null) || (!structure.isTypeHouse()) || (floors == null)) {
      structure = Structures.getStructureForTile(tilex - 1, tiley - 1, onSurface);
    }
    if ((structure != null) && (structure.isTypeHouse()) && (floors == null)) {
      floors = Zones.getFloorsAtTile(tilex - 1, tiley - 1, 0, 0, onSurface);
    }
    if ((structure == null) || (!structure.isTypeHouse()) || (floors == null)) {
      structure = Structures.getStructureForTile(tilex, tiley - 1, onSurface);
    }
    if ((structure != null) && (structure.isTypeHouse()) && (floors == null)) {
      floors = Zones.getFloorsAtTile(tilex, tiley - 1, 0, 0, onSurface);
    }
    return (structure == null) || (!structure.isTypeHouse()) || (floors == null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\TileCornerBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */