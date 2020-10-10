package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.List;

final class TileBorderBehaviour
  extends Behaviour
  implements MiscConstants
{
  TileBorderBehaviour()
  {
    super((short)32);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
      return toReturn;
    }
    toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, dir, border, heightOffset));
    toReturn.add(Actions.actionEntrys['ɟ']);
    int templateId = subject.getTemplateId();
    if ((onSurface) && ((subject.isMineDoor()) || (subject.getTemplateId() == 315) || (subject.getTemplateId() == 176)))
    {
      int[] opening = Terraforming.getCaveOpeningCoords(tilex, tiley);
      if ((opening[0] != -1) && (opening[1] != -1)) {
        if (!isWideEntrance(opening[0], opening[1])) {
          toReturn.add(Actions.actionEntrys['ū']);
        }
      }
    }
    boolean hasMarker = hasMarker(tilex, tiley, onSurface, dir);
    if ((!hasMarker) && (MethodsStructure.isCorrectToolForBuilding(performer, templateId)))
    {
      boolean ok = onSurface;
      if (!onSurface)
      {
        VolaTile vt = Zones.getOrCreateTile(tilex, tiley, onSurface);
        if (vt.getVillage() != null) {
          ok = true;
        }
      }
      if (ok)
      {
        toReturn.add(new ActionEntry((short)-12, "Fence", "Fence options"));
        
        toReturn.add(new ActionEntry((short)-5, "Iron", "Fence options"));
        toReturn.add(Actions.actionEntrys['ɣ']);
        toReturn.add(Actions.actionEntrys['ǝ']);
        toReturn.add(Actions.actionEntrys['ǟ']);
        toReturn.add(Actions.actionEntrys['ȡ']);
        toReturn.add(Actions.actionEntrys['Ȣ']);
        
        toReturn.add(new ActionEntry((short)-2, "Log", "Fence options"));
        toReturn.add(Actions.actionEntrys['¥']);
        toReturn.add(Actions.actionEntrys['§']);
        
        toReturn.add(new ActionEntry((short)-8, "Marble", "Fence options"));
        toReturn.add(Actions.actionEntrys['͌']);
        toReturn.add(Actions.actionEntrys['͍']);
        toReturn.add(Actions.actionEntrys['͎']);
        toReturn.add(Actions.actionEntrys['΄']);
        toReturn.add(Actions.actionEntrys['΅']);
        toReturn.add(Actions.actionEntrys['Ά']);
        toReturn.add(Actions.actionEntrys['·']);
        
        toReturn.add(Actions.actionEntrys['Ή']);
        
        toReturn.add(new ActionEntry((short)-4, "Plank", "Fence options"));
        toReturn.add(Actions.actionEntrys['Ȉ']);
        toReturn.add(Actions.actionEntrys['Ȑ']);
        toReturn.add(Actions.actionEntrys['¦']);
        toReturn.add(Actions.actionEntrys['¨']);
        
        toReturn.add(new ActionEntry((short)-8, "Pottery", "Fence options"));
        toReturn.add(Actions.actionEntrys['͆']);
        toReturn.add(Actions.actionEntrys['͇']);
        toReturn.add(Actions.actionEntrys['͈']);
        toReturn.add(Actions.actionEntrys[';']);
        toReturn.add(Actions.actionEntrys['Ϳ']);
        toReturn.add(Actions.actionEntrys['΀']);
        toReturn.add(Actions.actionEntrys['΁']);
        
        toReturn.add(Actions.actionEntrys['΃']);
        
        toReturn.add(new ActionEntry((short)-2, "Rope", "Rope options"));
        toReturn.add(Actions.actionEntrys['Ƞ']);
        toReturn.add(Actions.actionEntrys['ȟ']);
        
        toReturn.add(new ActionEntry((short)-8, "Rounded stone", "Fence options"));
        toReturn.add(Actions.actionEntrys['̓']);
        toReturn.add(Actions.actionEntrys['̈́']);
        toReturn.add(Actions.actionEntrys['ͅ']);
        toReturn.add(Actions.actionEntrys['ͬ']);
        toReturn.add(Actions.actionEntrys['ͭ']);
        toReturn.add(Actions.actionEntrys['ͮ']);
        toReturn.add(Actions.actionEntrys['ͯ']);
        
        toReturn.add(Actions.actionEntrys['ͱ']);
        
        toReturn.add(new ActionEntry((short)-8, "Sandstone", "Fence options"));
        toReturn.add(Actions.actionEntrys['͉']);
        toReturn.add(Actions.actionEntrys['͊']);
        toReturn.add(Actions.actionEntrys['͋']);
        toReturn.add(Actions.actionEntrys['Ͳ']);
        toReturn.add(Actions.actionEntrys['ͳ']);
        toReturn.add(Actions.actionEntrys['ʹ']);
        toReturn.add(Actions.actionEntrys['͵']);
        
        toReturn.add(Actions.actionEntrys['ͷ']);
        
        toReturn.add(new ActionEntry((short)-3, "Shaft", "Fence options"));
        toReturn.add(Actions.actionEntrys['ȏ']);
        toReturn.add(Actions.actionEntrys['Ȏ']);
        toReturn.add(Actions.actionEntrys['ȑ']);
        
        toReturn.add(new ActionEntry((short)-8, "Slate", "Fence options"));
        toReturn.add(Actions.actionEntrys['̀']);
        toReturn.add(Actions.actionEntrys['́']);
        toReturn.add(Actions.actionEntrys['͂']);
        toReturn.add(Actions.actionEntrys['ͦ']);
        toReturn.add(Actions.actionEntrys['ͧ']);
        toReturn.add(Actions.actionEntrys['ͨ']);
        toReturn.add(Actions.actionEntrys['ͩ']);
        
        toReturn.add(Actions.actionEntrys['ͫ']);
        
        toReturn.add(new ActionEntry((short)-5, "Stone", "Fence options"));
        toReturn.add(Actions.actionEntrys['Ȟ']);
        toReturn.add(Actions.actionEntrys['£']);
        toReturn.add(Actions.actionEntrys['ʎ']);
        toReturn.add(Actions.actionEntrys['ȝ']);
        toReturn.add(Actions.actionEntrys['¤']);
        
        toReturn.add(new ActionEntry((short)-1, "Woven", "Fence options"));
        toReturn.add(Actions.actionEntrys['Ǟ']);
      }
    }
    if ((onSurface) && (subject.getTemplateId() == 266))
    {
      if (!hasMarker) {
        toReturn.add(Actions.actionEntrys['º']);
      }
    }
    else if ((onSurface) && (subject.isTrellis()))
    {
      toReturn.add(new ActionEntry((short)-3, "Plant", "Plant options"));
      toReturn.add(Actions.actionEntrys['˪']);
      toReturn.add(new ActionEntry((short)176, "In center", "planting"));
      toReturn.add(Actions.actionEntrys['˫']);
    }
    else if ((onSurface) && (subject.isFlower()))
    {
      if (!hasMarker) {
        toReturn.add(Actions.actionEntrys['ȳ']);
      }
    }
    else if ((onSurface) && (subject.isDiggingtool()))
    {
      toReturn.add(Actions.actionEntrys['ȕ']);
      toReturn.add(Actions.actionEntrys['͡']);
    }
    if ((subject.isMagicStaff()) || ((templateId == 176) && 
      (performer.getPower() >= 4) && (Servers.isThisATestServer())))
    {
      List<ActionEntry> slist = new LinkedList();
      if (performer.knowsKarmaSpell(556)) {
        slist.add(Actions.actionEntrys['Ȭ']);
      }
      if (performer.knowsKarmaSpell(557)) {
        slist.add(Actions.actionEntrys['ȭ']);
      }
      if (performer.knowsKarmaSpell(558)) {
        slist.add(Actions.actionEntrys['Ȯ']);
      }
      if (performer.getPower() >= 4) {
        toReturn.add(new ActionEntry((short)-slist.size(), "Sorcery", "casting"));
      }
      toReturn.addAll(slist);
    }
    if ((onSurface) && ((templateId == 176) || (templateId == 315)) && (performer.getPower() >= 2)) {
      toReturn.add(Actions.actionEntrys[64]);
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
      return toReturn;
    }
    toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, dir, border, heightOffset));
    toReturn.add(Actions.actionEntrys['ɟ']);
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, Tiles.TileBorderDirection dir, long borderId, short action, float counter)
  {
    boolean done = true;
    if (action == 1) {
      done = action(act, performer, tilex, tiley, onSurface, dir, borderId, action, counter);
    } else if (!hasHoleEachSide(tilex, tiley, onSurface, dir)) {
      if (Actions.isActionBuildFence(action))
      {
        boolean ok = onSurface;
        if (!onSurface)
        {
          VolaTile vt = Zones.getOrCreateTile(tilex, tiley, onSurface);
          if (vt.getVillage() != null) {
            ok = true;
          } else {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to make a fence in a cave when not on a deed.");
          }
        }
        if (ok) {
          if (!hasMarker(tilex, tiley, onSurface, dir)) {
            done = MethodsStructure.buildFence(act, performer, source, tilex, tiley, onSurface, heightOffset, dir, borderId, action, counter);
          } else {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to make a fence across a highway.");
          }
        }
      }
      else if ((onSurface) && (action == 533) && (source.isDiggingtool()))
      {
        done = Flattening.flattenTileBorder(borderId, performer, source, tilex, tiley, dir, counter, act);
      }
      else if ((onSurface) && (action == 865) && (source.isDiggingtool()))
      {
        done = Flattening.flattenTileBorder(borderId, performer, source, tilex, tiley, dir, counter, act);
      }
      else if ((onSurface) && (action == 186))
      {
        if (!hasMarker(tilex, tiley, onSurface, dir)) {
          done = Terraforming.plantHedge(performer, source, tilex, tiley, onSurface, dir, counter, act);
        } else {
          performer.getCommunicator().sendNormalServerMessage("You are not allowed to plant a hedge across a highway.");
        }
      }
      else if ((onSurface) && (source.isTrellis()) && ((action == 176) || (action == 746) || (action == 747)))
      {
        done = Terraforming.plantTrellis(performer, source, tilex, tiley, onSurface, dir, action, counter, act);
      }
      else if ((onSurface) && (action == 563))
      {
        if (!hasMarker(tilex, tiley, onSurface, dir)) {
          done = Terraforming.plantFlowerbed(performer, source, tilex, tiley, onSurface, dir, counter, act);
        } else {
          performer.getCommunicator().sendNormalServerMessage("You are not allowed to plant a flowerbed across a highway.");
        }
      }
      else if ((onSurface) && (action == 363))
      {
        if (!hasMarker(tilex, tiley, onSurface, dir))
        {
          int[] opening = Terraforming.getCaveOpeningCoords(tilex, tiley);
          if ((opening[0] != -1) && (opening[1] != -1)) {
            if (!isWideEntrance(opening[0], opening[1])) {
              done = Terraforming.buildMineDoor(performer, source, act, opening[0], opening[1], onSurface, counter);
            } else {
              performer.getCommunicator().sendNormalServerMessage("You are not allowed to add a minedoor on wide mine entrances.");
            }
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You are not allowed to add a minedoor across a highway.");
        }
      }
      else if (action == 64)
      {
        if (performer.getPower() >= 5) {
          if (source.getTemplateId() == 176) {
            try
            {
              Zone z = Zones.getZone(tilex, tiley, onSurface);
              for (int x = z.getStartX(); x < z.getStartX() + z.getSize(); x++) {
                try
                {
                  ItemFactory.createItem(344, 2.0F, (x << 2) + 2, (z.getStartY() << 2) + 2, 1.0F, true, (byte)0, performer
                    .getBridgeId(), performer.getName());
                }
                catch (FailedException localFailedException) {}catch (NoSuchTemplateException localNoSuchTemplateException) {}
              }
              for (int x = z.getStartX(); x < z.getStartX() + z.getSize(); x++) {
                try
                {
                  ItemFactory.createItem(344, 2.0F, (x << 2) + 2, (z.getEndY() << 2) + 2, 1.0F, true, (byte)0, performer
                    .getBridgeId(), performer.getName());
                }
                catch (FailedException localFailedException1) {}catch (NoSuchTemplateException localNoSuchTemplateException1) {}
              }
              for (int x = z.getStartY(); x < z.getStartY() + z.getSize(); x++) {
                if (x != z.getStartY()) {
                  try
                  {
                    ItemFactory.createItem(344, 2.0F, (z.getStartX() << 2) + 2, (x << 2) + 2, 1.0F, true, (byte)0, performer
                      .getBridgeId(), performer.getName());
                  }
                  catch (FailedException localFailedException2) {}catch (NoSuchTemplateException localNoSuchTemplateException2) {}
                }
              }
              for (int x = z.getStartY(); x < z.getStartY() + z.getSize(); x++) {
                if (x != z.getStartY()) {
                  try
                  {
                    ItemFactory.createItem(344, 2.0F, (z.getEndX() << 2) + 2, (x << 2) + 2, 1.0F, true, (byte)0, performer
                      .getBridgeId(), performer.getName());
                  }
                  catch (FailedException localFailedException3) {}catch (NoSuchTemplateException localNoSuchTemplateException3) {}
                }
              }
            }
            catch (NoSuchZoneException nsz)
            {
              performer.getCommunicator().sendNormalServerMessage("No zone at " + tilex + ", " + tiley + "," + onSurface + ".");
            }
          }
        }
      }
      else if (act.isSpell())
      {
        Spell spell = Spells.getSpell(action);
        
        int layer = onSurface ? 0 : -1;
        if ((source.isMagicStaff()) || (
          (source.getTemplateId() == 176) && (performer.getPower() >= 2) && (Servers.isThisATestServer())))
        {
          if (Methods.isActionAllowed(performer, (short)547)) {
            done = Methods.castSpell(performer, spell, tilex, tiley, layer, heightOffset, dir, counter);
          }
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You need to use a magic staff.");
          done = true;
        }
      }
      else if ((onSurface) && (heightOffset == 0) && (source.isMiningtool()) && (action == 145))
      {
        int digTilex = (int)performer.getStatus().getPositionX() + 2 >> 2;
        int digTiley = (int)performer.getStatus().getPositionY() + 2 >> 2;
        int tile = Server.surfaceMesh.getTile(digTilex, digTiley);
        byte type = Tiles.decodeType(tile);
        if (type == Tiles.Tile.TILE_ROCK.id) {
          done = TileRockBehaviour.mine(act, performer, source, tilex, tiley, action, counter, digTilex, digTiley);
        }
      }
      else
      {
        return action(act, performer, tilex, tiley, onSurface, dir, borderId, action, counter);
      }
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, long borderId, short action, float counter)
  {
    if (action == 1)
    {
      handle_EXAMINE(performer, tilex, tiley, onSurface, dir);
    }
    else
    {
      if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
        return true;
      }
      if (action == 607) {
        performer.getCommunicator().sendAddTileBorderToCreationWindow(borderId);
      }
    }
    return true;
  }
  
  private static void handle_EXAMINE(Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir)
  {
    Communicator comm = performer.getCommunicator();
    if (hasHoleEachSide(tilex, tiley, onSurface, dir)) {
      comm.sendNormalServerMessage("This is in the middle of a wide cave entrance.");
    } else {
      comm.sendNormalServerMessage("This outlines where fences and walls may be built.");
    }
    if ((tilex - 2 < 0) || (tilex + 2 > 1 << Constants.meshSize) || (tiley - 2 < 0) || (tiley + 2 > 1 << Constants.meshSize))
    {
      comm.sendNormalServerMessage("The water is too deep to measure.");
      return;
    }
    if (!performer.isWithinTileDistanceTo(tilex, tiley, 20, 3)) {
      return;
    }
    MeshIO mesh;
    MeshIO mesh;
    if (onSurface) {
      mesh = Server.surfaceMesh;
    } else {
      mesh = Server.caveMesh;
    }
    short height = Tiles.decodeHeight(mesh.getTile(tilex, tiley));
    boolean away = false;
    short endheight = height;
    int diff = 0;
    if (dir == Tiles.TileBorderDirection.DIR_HORIZ)
    {
      int endx = tilex + 1;
      endheight = Tiles.decodeHeight(mesh.getTile(endx, tiley));
      float posx = performer.getPosX();
      diff = Math.abs(height - endheight);
      if (Math.abs(posx - (endx << 2)) > Math.abs(posx - (tilex << 2))) {
        away = true;
      }
    }
    else if (dir == Tiles.TileBorderDirection.DIR_DOWN)
    {
      int endy = tiley + 1;
      endheight = Tiles.decodeHeight(mesh.getTile(tilex, endy));
      float posy = performer.getPosY();
      diff = Math.abs(height - endheight);
      if (Math.abs(posy - (endy << 2)) > Math.abs(posy - (tiley << 2))) {
        away = true;
      }
    }
    String dist = "up.";
    if (((away) && (height > endheight)) || ((!away) && (endheight > height))) {
      dist = "down.";
    }
    if (diff != 0) {
      comm.sendNormalServerMessage("The border is " + diff + " slope " + dist);
    } else {
      comm.sendNormalServerMessage("The border is level.");
    }
  }
  
  private static boolean hasHoleEachSide(int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir)
  {
    if (!onSurface) {
      return false;
    }
    MeshTile mTileCurrent = new MeshTile(Server.surfaceMesh, tilex, tiley);
    if (dir == Tiles.TileBorderDirection.DIR_HORIZ)
    {
      MeshTile mTileNorth = mTileCurrent.getNorthMeshTile();
      if ((mTileCurrent.isHole()) && (mTileNorth.isHole())) {
        return true;
      }
    }
    else
    {
      MeshTile mTileWest = mTileCurrent.getWestMeshTile();
      if ((mTileCurrent.isHole()) && (mTileWest.isHole())) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean isWideEntrance(int tilex, int tiley)
  {
    MeshTile mTileCurrent = new MeshTile(Server.surfaceMesh, tilex, tiley);
    if (mTileCurrent.isHole())
    {
      MeshTile mTileNorth = mTileCurrent.getNorthMeshTile();
      if (mTileNorth.isHole()) {
        return true;
      }
      MeshTile mTileWest = mTileCurrent.getWestMeshTile();
      if (mTileWest.isHole()) {
        return true;
      }
      MeshTile mTileSouth = mTileCurrent.getSouthMeshTile();
      if (mTileSouth.isHole()) {
        return true;
      }
      MeshTile mTileEast = mTileCurrent.getEastMeshTile();
      if (mTileEast.isHole()) {
        return true;
      }
    }
    return false;
  }
  
  private static boolean hasMarker(int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir)
  {
    if (Items.getMarker(tilex, tiley, onSurface, 0, -10L) != null) {
      return true;
    }
    if ((dir == Tiles.TileBorderDirection.DIR_HORIZ) && (Items.getMarker(tilex + 1, tiley, onSurface, 0, -10L) != null)) {
      return true;
    }
    if ((dir == Tiles.TileBorderDirection.DIR_DOWN) && (Items.getMarker(tilex, tiley + 1, onSurface, 0, -10L) != null)) {
      return true;
    }
    if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_HOLE.id)
    {
      if (Items.getMarker(tilex, tiley, !onSurface, 0, -10L) != null) {
        return true;
      }
      if (dir == Tiles.TileBorderDirection.DIR_HORIZ) {
        if (Items.getMarker(tilex + 1, tiley, !onSurface, 0, -10L) != null) {
          return true;
        }
      }
      if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
        if (Items.getMarker(tilex, tiley + 1, !onSurface, 0, -10L) != null) {
          return true;
        }
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\TileBorderBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */