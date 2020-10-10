package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

final class OwnershipPaperBehaviour
  extends ItemBehaviour
{
  private static final Logger logger = Logger.getLogger(OwnershipPaperBehaviour.class.getName());
  
  OwnershipPaperBehaviour()
  {
    super((short)52);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
    toReturn.addAll(getBehavioursForPaper());
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
    toReturn.addAll(getBehavioursForPaper());
    return toReturn;
  }
  
  List<ActionEntry> getBehavioursForPaper()
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(new ActionEntry((short)17, "Read paper", "Reading"));
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean done = true;
    if ((action == 1) && (target.getTemplateId() == 1000)) {
      performer.getCommunicator().sendNormalServerMessage("This is the writ of ownership. It can be traded with another player to transfer ownership.");
    } else if (action != 17) {
      done = super.action(act, performer, target, action, counter);
    }
    return done;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\OwnershipPaperBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */