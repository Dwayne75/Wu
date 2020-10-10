package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.List;
import java.util.logging.Logger;

final class CornucopiaBehaviour
  extends ItemBehaviour
{
  private static final Logger logger = Logger.getLogger(CornucopiaBehaviour.class.getName());
  
  CornucopiaBehaviour()
  {
    super((short)30);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
    if (WurmPermissions.mayCreateItems(performer)) {
      toReturn.add(Actions.actionEntrys['']);
    } else {
      logger.warning(performer.getName() + " tried to use a Cornucopia but their power was only " + performer.getPower());
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
    if (WurmPermissions.mayCreateItems(performer)) {
      toReturn.add(Actions.actionEntrys['']);
    } else {
      logger.warning(performer.getName() + " tried to use a Cornucopia but their power was only " + performer.getPower());
    }
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    boolean done = true;
    if (action == 148)
    {
      done = true;
      if (WurmPermissions.mayCreateItems(performer)) {
        Methods.sendCreateQuestion(performer, source);
      } else {
        logger.warning(performer.getName() + " tried to use a Cornucopia but their power was only " + performer
          .getPower());
      }
    }
    else
    {
      done = super.action(act, performer, source, target, action, counter);
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean done = true;
    if (action == 148)
    {
      done = true;
      if (WurmPermissions.mayCreateItems(performer)) {
        Methods.sendCreateQuestion(performer, target);
      } else {
        logger.warning(performer.getName() + " tried to use a Cornucopia but their power was only " + performer
          .getPower());
      }
    }
    else
    {
      done = super.action(act, performer, target, action, counter);
    }
    return done;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\CornucopiaBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */