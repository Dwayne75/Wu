package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

final class MenuRequestBehaviour
  extends Behaviour
{
  private static final Logger logger = Logger.getLogger(MenuRequestBehaviour.class.getName());
  
  MenuRequestBehaviour()
  {
    super((short)53);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int menuId)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (menuId == 0) {
      toReturn.addAll(ManageMenu.getBehavioursFor(performer));
    }
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, int menuId, short action, float counter)
  {
    boolean done = true;
    if ((menuId == 0) && (ManageMenu.isManageAction(performer, action))) {
      return ManageMenu.action(act, performer, action, counter);
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\MenuRequestBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */