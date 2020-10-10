package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.LinkedList;
import java.util.List;

final class ExamineBehaviour
  extends Behaviour
{
  ExamineBehaviour()
  {
    super((short)11);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(Actions.actionEntrys[1]);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(Actions.actionEntrys[1]);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(Actions.actionEntrys[1]);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(Actions.actionEntrys[1]);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Creature object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(Actions.actionEntrys[1]);
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Creature object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.add(Actions.actionEntrys[1]);
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter)
  {
    if (action == 1) {
      performer.getCommunicator().sendNormalServerMessage("You see a part of the lands of Wurm.");
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter)
  {
    if (action == 1) {
      performer.getCommunicator().sendNormalServerMessage("You see a part of the lands of Wurm.");
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    if (action == 1)
    {
      performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
      target.sendEnchantmentStrings(performer.getCommunicator());
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    if (action == 1)
    {
      performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
      target.sendEnchantmentStrings(performer.getCommunicator());
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, Item source, Creature target, short action, float counter)
  {
    if (action == 1)
    {
      performer.getCommunicator().sendNormalServerMessage(target.examine());
      target.getCommunicator().sendNormalServerMessage(source.getName() + " takes a long, good look at you.");
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, Creature target, short action, float counter)
  {
    if (action == 1)
    {
      performer.getCommunicator().sendNormalServerMessage(target.examine());
      target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " takes a long, good look at you.");
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\ExamineBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */