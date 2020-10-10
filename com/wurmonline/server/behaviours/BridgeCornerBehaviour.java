package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.BridgePart;
import java.util.LinkedList;
import java.util.List;

final class BridgeCornerBehaviour
  extends Behaviour
  implements MiscConstants
{
  BridgeCornerBehaviour()
  {
    super((short)60);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, boolean onSurface, BridgePart bridgePart)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if ((source.isSign()) || (source.isStreetLamp()))
    {
      toReturn.add(Actions.actionEntrys['°']);
    }
    else if ((Features.Feature.HIGHWAYS.isEnabled()) && (source.isRoadMarker()))
    {
      HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
      if (highwayPos != null) {
        if ((MethodsHighways.middleOfHighway(highwayPos)) && (!MethodsHighways.containsMarker(highwayPos, (byte)0)))
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
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, boolean onSurface, BridgePart bridgePart)
  {
    List<ActionEntry> toReturn = new LinkedList();
    
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item source, boolean onSurface, BridgePart bridgePart, int encodedTile, short action, float counter)
  {
    boolean done = true;
    if (action == 1)
    {
      done = action(act, performer, onSurface, bridgePart, encodedTile, action, counter);
    }
    else
    {
      if ((action == 176) && ((source.isSign()) || (source.isStreetLamp())))
      {
        if (performer.getPower() > 0) {
          return MethodsItems.plantSignFinish(performer, source, true, bridgePart.getTileX(), bridgePart.getTileY(), performer.isOnSurface(), performer.getBridgeId(), false, -1L);
        }
        return MethodsItems.plantSign(performer, source, counter, true, bridgePart.getTileX(), bridgePart.getTileY(), performer.isOnSurface(), performer.getBridgeId(), false, -1L);
      }
      if ((action == 176) && (source.isRoadMarker()) && (Features.Feature.HIGHWAYS.isEnabled()))
      {
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
        if (highwayPos != null)
        {
          if ((MethodsHighways.middleOfHighway(highwayPos)) && (!MethodsHighways.containsMarker(highwayPos, (byte)0)))
          {
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
      else if ((action == 748) && (source.isRoadMarker()) && (Features.Feature.HIGHWAYS.isEnabled()))
      {
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
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
      else if ((action == 759) && (source.isRoadMarker()) && (Features.Feature.HIGHWAYS.isEnabled()))
      {
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
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
        return action(act, performer, onSurface, bridgePart, encodedTile, action, counter);
      }
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, boolean onSurface, BridgePart bridgePart, int encodedTile, short action, float counter)
  {
    if (action == 1)
    {
      HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
      if ((highwayPos != null) && (MethodsHighways.middleOfHighway(highwayPos))) {
        performer.getCommunicator().sendNormalServerMessage("This outlines where signs and road markers could be planted.");
      } else {
        performer.getCommunicator().sendNormalServerMessage("This outlines where signs can be planted.");
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\BridgeCornerBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */