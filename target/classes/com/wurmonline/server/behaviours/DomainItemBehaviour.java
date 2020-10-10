package com.wurmonline.server.behaviours;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.EntityMoveQuestion;
import com.wurmonline.server.questions.SelectSpellQuestion;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import java.util.ArrayList;
import java.util.List;

class DomainItemBehaviour
  extends ItemBehaviour
{
  DomainItemBehaviour()
  {
    super((short)33);
  }
  
  DomainItemBehaviour(short type)
  {
    super(type);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = new ArrayList();
    toReturn.addAll(super.getBehavioursFor(performer, target));
    boolean reachable = true;
    if (target.getOwnerId() == -10L)
    {
      reachable = false;
      if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F))
      {
        BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
        if (result == null) {
          reachable = true;
        }
      }
    }
    if (reachable)
    {
      if (Servers.localServer.EPIC) {
        if (performer.getDeity() != null) {
          toReturn.add(Actions.actionEntrys['ɢ']);
        }
      }
      toReturn.add(Actions.actionEntrys['']);
      if (performer.getFaith() >= 10.0F) {
        if (target.getBless() != null)
        {
          toReturn.add(Actions.actionEntrys['']);
          toReturn.add(Actions.actionEntrys['']);
          if (performer.isPriest()) {
            toReturn.add(Actions.actionEntrys['Ǆ']);
          }
        }
      }
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target)
  {
    List<ActionEntry> toReturn = new ArrayList();
    toReturn.addAll(super.getBehavioursFor(performer, source, target));
    boolean reachable = true;
    if (target.getOwnerId() == -10L)
    {
      reachable = false;
      if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F))
      {
        BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
        if (result == null) {
          reachable = true;
        }
      }
    }
    if (reachable)
    {
      if (Servers.localServer.EPIC) {
        if (performer.getDeity() != null) {
          toReturn.add(Actions.actionEntrys['ɢ']);
        }
      }
      toReturn.add(Actions.actionEntrys['']);
      if (performer.getFaith() >= 10.0F)
      {
        if (target.getBless() != null)
        {
          toReturn.add(Actions.actionEntrys['']);
          toReturn.add(Actions.actionEntrys['']);
        }
        if (performer.isPriest())
        {
          toReturn.add(Actions.actionEntrys['Ǆ']);
          if (source.isHolyItem()) {
            toReturn.add(Actions.actionEntrys['Ø']);
          }
        }
      }
      if (source.isRechargeable()) {
        toReturn.add(Actions.actionEntrys['Ų']);
      }
    }
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean done = true;
    if (action == 141)
    {
      done = MethodsReligion.pray(act, performer, target, counter);
    }
    else if (action == 142)
    {
      if (performer.getFaith() >= 10.0F) {
        done = MethodsReligion.sacrifice(act, performer, target);
      } else {
        done = true;
      }
    }
    else if (action == 143)
    {
      if (performer.getFaith() >= 10.0F) {
        done = MethodsReligion.desecrate(act, performer, null, target);
      } else {
        done = true;
      }
    }
    else if (action == 452)
    {
      if (performer.isPriest())
      {
        done = true;
        
        SelectSpellQuestion spq = new SelectSpellQuestion(performer, "Spell set", "These are your available spells", target.getWurmId());
        spq.sendQuestion();
      }
    }
    else if (action == 610)
    {
      done = true;
      if (Servers.localServer.EPIC) {
        if (performer.getDeity() != null)
        {
          EntityMoveQuestion emq = new EntityMoveQuestion(performer);
          emq.sendQuestion();
        }
      }
    }
    else
    {
      done = super.action(act, performer, target, action, counter);
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    boolean done = false;
    boolean reachable = true;
    if (action == 141)
    {
      done = action(act, performer, target, action, counter);
    }
    else if (action == 142)
    {
      if (performer.getFaith() >= 10.0F) {
        done = MethodsReligion.sacrifice(act, performer, target);
      } else {
        done = true;
      }
    }
    else if (action == 143)
    {
      if (performer.getFaith() >= 10.0F) {
        done = MethodsReligion.desecrate(act, performer, source, target);
      } else {
        done = true;
      }
    }
    else if (action == 216)
    {
      done = true;
      if ((performer.isPriest()) && (source.isHolyItem())) {
        if (target.getParentId() != -10L) {
          performer.getCommunicator().sendNormalServerMessage("The altar needs to be on the ground to be used.");
        } else {
          done = MethodsReligion.holdSermon(performer, target, source, act, counter);
        }
      }
    }
    else if ((action == 370) && (source.isRechargeable()))
    {
      if (target.getParentId() != -10L) {
        performer.getCommunicator().sendNormalServerMessage("The altar needs to be on the ground to be used.");
      } else {
        done = MethodsReligion.sendRechargeQuestion(performer, source);
      }
    }
    else if (action == 452)
    {
      done = action(act, performer, target, action, counter);
    }
    else if (action == 610)
    {
      done = true;
      if (Servers.localServer.EPIC) {
        if (performer.getDeity() != null)
        {
          EntityMoveQuestion emq = new EntityMoveQuestion(performer);
          emq.sendQuestion();
        }
      }
    }
    else
    {
      done = super.action(act, performer, source, target, action, counter);
    }
    return done;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\DomainItemBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */