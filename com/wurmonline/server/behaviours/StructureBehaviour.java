package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.CaveTile;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public final class StructureBehaviour
  extends Behaviour
{
  private static final Logger logger = Logger.getLogger(WallBehaviour.class.getName());
  
  StructureBehaviour()
  {
    super((short)6);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, tilex, tiley, onSurface, dir, border, heightOffset);
    toReturn.add(Actions.actionEntrys['ɟ']);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item target, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, boolean border, int heightOffset)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, target, tilex, tiley, onSurface, dir, border, heightOffset);
    
    toReturn.add(Actions.actionEntrys['ɟ']);
    boolean hasMarker = hasMarker(tilex, tiley, onSurface, dir, heightOffset);
    if (!MethodsStructure.isCorrectToolForBuilding(performer, target.getTemplateId())) {
      return toReturn;
    }
    Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(tilex, tiley, dir, onSurface);
    if ((structure != null) && (structure.isActionAllowed(performer, (short)116)))
    {
      if (!onSurface)
      {
        int minHeight = (performer.getFloorLevel() + 1) * 30;
        int nwCorner = Server.caveMesh.getTile(tilex, tiley);
        short nwCeil = (short)CaveTile.decodeCeilingHeight(nwCorner);
        if (nwCeil < minHeight) {
          return toReturn;
        }
        if (dir == Tiles.TileBorderDirection.DIR_HORIZ)
        {
          int neCorner = Server.caveMesh.getTile(tilex + 1, tiley);
          short neCeil = (short)CaveTile.decodeCeilingHeight(neCorner);
          if (neCeil < minHeight) {
            return toReturn;
          }
        }
        else
        {
          int swCorner = Server.caveMesh.getTile(tilex, tiley + 1);
          short swCeil = (short)CaveTile.decodeCeilingHeight(swCorner);
          if (swCeil < minHeight) {
            return toReturn;
          }
        }
      }
      boolean hasArch = false;
      if (!MethodsStructure.doesTileBorderContainWallOrFence(tilex, tiley, heightOffset, dir, onSurface, false))
      {
        toReturn.add(new ActionEntry((short)-1, "Plan", "planning"));
        toReturn.add(new ActionEntry((short)(20000 + StructureTypeEnum.SOLID.ordinal()), "Wall", "planning wall", emptyIntArr));
      }
      else
      {
        hasArch = true;
      }
      if (!hasMarker)
      {
        toReturn.add(new ActionEntry((short)-11, "Fence", "Fence options"));
        
        List<ActionEntry> iron = new LinkedList();
        iron.add(Actions.actionEntrys['ɣ']);
        iron.add(Actions.actionEntrys['ǝ']);
        iron.add(Actions.actionEntrys['ǟ']);
        iron.add(Actions.actionEntrys['ȉ']);
        iron.add(Actions.actionEntrys['ȡ']);
        if (!hasArch) {
          iron.add(Actions.actionEntrys['Ȣ']);
        }
        toReturn.add(new ActionEntry((short)-iron.size(), "Iron", "Fence options"));
        Collections.sort(iron);
        toReturn.addAll(iron);
        
        List<ActionEntry> marble = new LinkedList();
        marble.add(Actions.actionEntrys['͌']);
        marble.add(Actions.actionEntrys['͍']);
        marble.add(Actions.actionEntrys['͎']);
        marble.add(Actions.actionEntrys['Έ']);
        marble.add(Actions.actionEntrys['Ή']);
        marble.add(Actions.actionEntrys['Ά']);
        if (!hasArch)
        {
          marble.add(Actions.actionEntrys['΄']);
          marble.add(Actions.actionEntrys['΅']);
          marble.add(Actions.actionEntrys['·']);
        }
        toReturn.add(new ActionEntry((short)-marble.size(), "Marble", "Fence options"));
        Collections.sort(marble);
        toReturn.addAll(marble);
        
        List<ActionEntry> plank = new LinkedList();
        plank.add(Actions.actionEntrys['Ȉ']);
        plank.add(Actions.actionEntrys['Ȑ']);
        plank.add(Actions.actionEntrys['¦']);
        plank.add(Actions.actionEntrys['¨']);
        if (!hasArch) {
          plank.add(Actions.actionEntrys['Ȅ']);
        }
        toReturn.add(new ActionEntry((short)-plank.size(), "Plank", "Fence options"));
        Collections.sort(plank);
        toReturn.addAll(plank);
        
        List<ActionEntry> pottery = new LinkedList();
        pottery.add(Actions.actionEntrys['͆']);
        pottery.add(Actions.actionEntrys['͇']);
        pottery.add(Actions.actionEntrys['͈']);
        pottery.add(Actions.actionEntrys['΂']);
        pottery.add(Actions.actionEntrys['΃']);
        pottery.add(Actions.actionEntrys['΀']);
        if (!hasArch)
        {
          pottery.add(Actions.actionEntrys[';']);
          pottery.add(Actions.actionEntrys['Ϳ']);
          pottery.add(Actions.actionEntrys['΁']);
        }
        toReturn.add(new ActionEntry((short)-pottery.size(), "Pottery", "Fence options"));
        Collections.sort(pottery);
        toReturn.addAll(pottery);
        
        List<ActionEntry> rope = new LinkedList();
        rope.add(Actions.actionEntrys['Ƞ']);
        rope.add(Actions.actionEntrys['ȟ']);
        toReturn.add(new ActionEntry((short)-rope.size(), "Rope", "Rope options"));
        Collections.sort(rope);
        toReturn.addAll(rope);
        
        List<ActionEntry> round = new LinkedList();
        round.add(Actions.actionEntrys['̓']);
        round.add(Actions.actionEntrys['̈́']);
        round.add(Actions.actionEntrys['ͅ']);
        round.add(Actions.actionEntrys['Ͱ']);
        round.add(Actions.actionEntrys['ͱ']);
        round.add(Actions.actionEntrys['ͮ']);
        if (!hasArch)
        {
          round.add(Actions.actionEntrys['ͬ']);
          round.add(Actions.actionEntrys['ͭ']);
          round.add(Actions.actionEntrys['ͯ']);
        }
        toReturn.add(new ActionEntry((short)-round.size(), "Rounded stone", "Fence options"));
        Collections.sort(round);
        toReturn.addAll(round);
        
        List<ActionEntry> sandstone = new LinkedList();
        sandstone.add(Actions.actionEntrys['͉']);
        sandstone.add(Actions.actionEntrys['͊']);
        sandstone.add(Actions.actionEntrys['͋']);
        sandstone.add(Actions.actionEntrys['Ͷ']);
        sandstone.add(Actions.actionEntrys['ͷ']);
        sandstone.add(Actions.actionEntrys['ʹ']);
        if (!hasArch)
        {
          sandstone.add(Actions.actionEntrys['Ͳ']);
          sandstone.add(Actions.actionEntrys['ͳ']);
          sandstone.add(Actions.actionEntrys['͵']);
        }
        toReturn.add(new ActionEntry((short)-sandstone.size(), "Sandstone", "Fence options"));
        Collections.sort(sandstone);
        toReturn.addAll(sandstone);
        
        List<ActionEntry> shaft = new LinkedList();
        shaft.add(Actions.actionEntrys['ȏ']);
        shaft.add(Actions.actionEntrys['Ȏ']);
        shaft.add(Actions.actionEntrys['ȑ']);
        toReturn.add(new ActionEntry((short)-shaft.size(), "Shaft", "Fence options"));
        Collections.sort(shaft);
        toReturn.addAll(shaft);
        
        List<ActionEntry> slate = new LinkedList();
        slate.add(Actions.actionEntrys['̀']);
        slate.add(Actions.actionEntrys['́']);
        slate.add(Actions.actionEntrys['͂']);
        slate.add(Actions.actionEntrys['ͪ']);
        slate.add(Actions.actionEntrys['ͫ']);
        slate.add(Actions.actionEntrys['ͨ']);
        if (!hasArch)
        {
          slate.add(Actions.actionEntrys['ͦ']);
          slate.add(Actions.actionEntrys['ͧ']);
          slate.add(Actions.actionEntrys['ͩ']);
        }
        toReturn.add(new ActionEntry((short)-slate.size(), "Slate", "Fence options"));
        Collections.sort(slate);
        toReturn.addAll(slate);
        
        List<ActionEntry> stone = new LinkedList();
        stone.add(Actions.actionEntrys['Ȟ']);
        stone.add(Actions.actionEntrys['ȝ']);
        stone.add(Actions.actionEntrys['ȅ']);
        if (!hasArch)
        {
          stone.add(Actions.actionEntrys['ʎ']);
          stone.add(Actions.actionEntrys['¤']);
        }
        toReturn.add(new ActionEntry((short)-stone.size(), "Stone", "Fence options"));
        Collections.sort(stone);
        toReturn.addAll(stone);
        
        List<ActionEntry> woven = new LinkedList();
        woven.add(Actions.actionEntrys['Ǟ']);
        toReturn.add(new ActionEntry((short)-woven.size(), "Woven", "Fence options"));
        Collections.sort(woven);
        toReturn.addAll(woven);
      }
    }
    return toReturn;
  }
  
  static boolean canBuildFenceOnFloor(short action)
  {
    switch (action)
    {
    case 164: 
    case 166: 
    case 168: 
    case 477: 
    case 478: 
    case 479: 
    case 516: 
    case 517: 
    case 520: 
    case 521: 
    case 526: 
    case 527: 
    case 528: 
    case 529: 
    case 541: 
    case 542: 
    case 543: 
    case 544: 
    case 545: 
    case 546: 
    case 611: 
    case 654: 
    case 832: 
    case 833: 
    case 834: 
    case 835: 
    case 836: 
    case 837: 
    case 838: 
    case 839: 
    case 840: 
    case 841: 
    case 842: 
    case 843: 
    case 844: 
    case 845: 
    case 846: 
    case 870: 
    case 871: 
    case 872: 
    case 873: 
    case 874: 
    case 875: 
    case 876: 
    case 877: 
    case 878: 
    case 879: 
    case 880: 
    case 881: 
    case 882: 
    case 883: 
    case 884: 
    case 885: 
    case 886: 
    case 887: 
    case 888: 
    case 889: 
    case 890: 
    case 891: 
    case 892: 
    case 893: 
    case 894: 
    case 895: 
    case 896: 
    case 897: 
    case 898: 
    case 899: 
    case 900: 
    case 901: 
    case 902: 
    case 903: 
    case 904: 
    case 905: 
      return true;
    }
    return false;
  }
  
  public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, Tiles.TileBorderDirection dir, long borderId, short action, float counter)
  {
    boolean hasMarker = hasMarker(tilex, tiley, onSurface, dir, heightOffset);
    Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(tilex, tiley, dir, onSurface);
    if ((action == 20000 + StructureTypeEnum.SOLID.ordinal()) && (structure != null) && (structure.isActionAllowed(performer, (short)116))) {
      return MethodsStructure.planWallAt(act, performer, source, tilex, tiley, onSurface, heightOffset, dir, action, counter);
    }
    if ((!hasMarker) && (canBuildFenceOnFloor(action)) && (structure != null) && (structure.isActionAllowed(performer, (short)116))) {
      return MethodsStructure.buildFence(act, performer, source, tilex, tiley, onSurface, heightOffset, dir, borderId, action, counter);
    }
    if (action == 607)
    {
      performer.getCommunicator().sendAddTileBorderToCreationWindow(borderId);
      return true;
    }
    if (action == 1) {
      return action(act, performer, tilex, tiley, onSurface, dir, borderId, action, counter);
    }
    if (hasMarker) {
      performer.getCommunicator().sendNormalServerMessage("You cannot do that on a highway.");
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, long borderId, short action, float counter)
  {
    if (action == 1) {
      performer.getCommunicator().sendNormalServerMessage("This outlines where walls may be built.");
    } else if (action == 607) {
      performer.getCommunicator().sendAddTileBorderToCreationWindow(borderId);
    }
    return true;
  }
  
  static boolean hasMarker(int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, int heightOffset)
  {
    Floor[] floors = Zones.getFloorsAtTile(tilex, tiley, heightOffset, heightOffset, onSurface);
    HighwayPos highwaypos;
    HighwayPos highwaypos;
    if ((floors != null) && (floors.length == 1))
    {
      highwaypos = MethodsHighways.getHighwayPos(floors[0]);
    }
    else
    {
      HighwayPos highwaypos;
      if (heightOffset > 0)
      {
        BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
        HighwayPos highwaypos;
        if (bridgePart != null) {
          highwaypos = MethodsHighways.getHighwayPos(bridgePart);
        } else {
          return false;
        }
      }
      else
      {
        highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
      }
    }
    if (MethodsHighways.containsMarker(highwaypos, (byte)-1)) {
      return true;
    }
    if ((dir == Tiles.TileBorderDirection.DIR_HORIZ) && (MethodsHighways.containsMarker(highwaypos, (byte)4))) {
      return true;
    }
    if ((dir == Tiles.TileBorderDirection.DIR_DOWN) && (MethodsHighways.containsMarker(highwaypos, (byte)16))) {
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\StructureBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */