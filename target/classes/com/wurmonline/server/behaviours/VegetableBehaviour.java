package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class VegetableBehaviour
  extends ItemBehaviour
{
  private static final Logger logger = Logger.getLogger(VegetableBehaviour.class.getName());
  
  VegetableBehaviour()
  {
    super((short)16);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(super.getBehavioursFor(performer, object));
    if (object.getOwnerId() == performer.getWurmId())
    {
      if (object.isCrushable()) {
        toReturn.add(Actions.actionEntrys[54]);
      }
      if (object.hasSeeds()) {
        toReturn.add(Actions.actionEntrys[55]);
      }
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(super.getBehavioursFor(performer, source, object));
    if (object.getOwnerId() == performer.getWurmId())
    {
      if (object.isCrushable()) {
        toReturn.add(Actions.actionEntrys[54]);
      }
      if (object.hasSeeds()) {
        toReturn.add(Actions.actionEntrys[55]);
      }
    }
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    boolean done = true;
    if ((action == 54) || (action == 55)) {
      done = action(act, performer, target, action, counter);
    } else {
      done = super.action(act, performer, source, target, action, counter);
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean done = true;
    int nums = -1;
    if (action == 54)
    {
      if (target.getOwnerId() != performer.getWurmId())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't crush that now.");
        return true;
      }
      if (target.isProcessedFood())
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " is processed and cannot be crushed.");
        return true;
      }
      int makes = target.getTemplate().getCrushsTo();
      if (makes > 0) {
        nums = crush(action, performer, target, makes);
      }
      if (nums > 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You crush the " + target.getName() + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " crushes " + target.getNameWithGenus() + ".", performer, 
          Math.max(3, target.getSizeZ() / 10));
      }
      else if (nums == 0)
      {
        performer.getCommunicator().sendNormalServerMessage("You fail to crush the " + target.getName() + ".");
        Server.getInstance().broadCastAction(performer
          .getName() + " tries to crush " + target.getNameWithGenus() + " with " + performer
          .getHisHerItsString() + " bare hands.", performer, 
          Math.max(3, target.getSizeZ() / 10));
      }
    }
    else if (action == 55)
    {
      if (target.getOwnerId() != performer.getWurmId())
      {
        performer.getCommunicator().sendNormalServerMessage("You can't pick that now.");
        return true;
      }
      if (target.isProcessedFood())
      {
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " is processed and there are no seeds to be picked.");
        return true;
      }
      int makes = target.getTemplate().getPickSeeds();
      if (makes > 0) {
        nums = crush(action, performer, target, makes);
      }
      if (nums > 0) {
        performer.getCommunicator().sendNormalServerMessage("You pick the " + target
          .getName() + " for seeds, ruining it.");
      } else if (nums == 0) {
        performer.getCommunicator().sendNormalServerMessage("The " + target
          .getName() + " contains almost no seeds.");
      }
    }
    else
    {
      done = super.action(act, performer, target, action, counter);
    }
    return done;
  }
  
  private int crush(short action, Creature performer, Item target, int templateId)
  {
    int templateWeight = target.getTemplate().getWeightGrams();
    int nums = target.getWeightGrams() / templateWeight;
    Item inventory = performer.getInventory();
    for (int x = 0; x < nums; x++) {
      try
      {
        if ((x != nums - 1) || (!target.getParent().isInventory())) {
          if (!inventory.mayCreatureInsertItem())
          {
            performer.getCommunicator().sendNormalServerMessage("You need more space in your inventory.");
            return x;
          }
        }
        Item toCreate = ItemFactory.createItem(templateId, target.getCurrentQualityLevel(), null);
        if (templateId == 745) {
          toCreate.setWeight(100, true);
        }
        inventory.insertItem(toCreate);
        target.setWeight(target.getWeightGrams() - templateWeight, true);
      }
      catch (FailedException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      catch (NoSuchTemplateException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      catch (NoSuchItemException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    return nums;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\VegetableBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */