package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import java.util.LinkedList;
import java.util.List;

public class MissionBehaviour
  extends Behaviour
{
  public MissionBehaviour()
  {
    super((short)43);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int missionId)
  {
    return getBehavioursFor(performer, missionId);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int missionId)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(Actions.actionEntrys[1]);
    toReturn.add(Actions.actionEntrys[16]);
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, int missionId, short action, float counter)
  {
    if (action == 1) {
      performer.getCommunicator().sendNormalServerMessage("This displays the state of a mission.");
    }
    if (action == 16)
    {
      MissionPerformer mp = MissionPerformed.getMissionPerformer(performer
        .getWurmId());
      MissionPerformed mpf = mp.getMission(missionId);
      mpf.setInactive(true);
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, Item source, int missionId, short action, float counter)
  {
    return action(act, performer, missionId, action, counter);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\MissionBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */