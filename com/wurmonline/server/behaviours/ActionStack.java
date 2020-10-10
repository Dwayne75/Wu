package com.wurmonline.server.behaviours;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public final class ActionStack
  implements TimeConstants
{
  private final LinkedList<Action> quickActions;
  private final LinkedList<Action> slowActions;
  private boolean clearing = false;
  
  public ActionStack()
  {
    this.quickActions = new LinkedList();
    this.slowActions = new LinkedList();
  }
  
  public void addAction(Action action)
  {
    int maxPrio = 1;
    if (action.isQuick())
    {
      if (this.quickActions.size() < 10) {
        this.quickActions.addLast(action);
      } else {
        action.getPerformer().getCommunicator().sendSafeServerMessage("You can't remember that many things to do in advance.");
      }
    }
    else if (!this.slowActions.isEmpty())
    {
      if (!Action.isStackable(action.getNumber()))
      {
        action.getPerformer().getCommunicator().sendNormalServerMessage("You're too busy."); return;
      }
      ListIterator<Action> it;
      if ((this.slowActions.size() > 1) && (!Action.isStackableFight(action.getNumber()))) {
        for (it = this.slowActions.listIterator(); it.hasNext();)
        {
          Action curr = (Action)it.next();
          if (curr.getNumber() == action.getNumber())
          {
            action.getPerformer().getCommunicator().sendNormalServerMessage("You're too busy.");
            return;
          }
        }
      }
      boolean insertedAndShouldPoll = false;
      for (ListIterator<Action> it = this.slowActions.listIterator(); it.hasNext();)
      {
        Action curr = (Action)it.next();
        if (maxPrio < curr.getPriority())
        {
          maxPrio = curr.getPriority();
          if (action.getPriority() > maxPrio)
          {
            it.previous();
            if (action.getNumber() == 114)
            {
              insertedAndShouldPoll = true;
              it.add(action);
              break;
            }
            it.add(action);
            return;
          }
        }
      }
      if (insertedAndShouldPoll)
      {
        action.poll();
        return;
      }
      if ((action.getPerformer().isPlayer()) && (this.slowActions.size() > action.getPerformer().getMaxNumActions()))
      {
        if (!Action.isActionAttack(action.getNumber())) {
          action.getPerformer().getCommunicator().sendNormalServerMessage("You're too busy.");
        }
      }
      else
      {
        if ((Actions.actionEntrys[((Action)this.slowActions.getLast()).getNumber()] == Actions.actionEntrys[action.getNumber()]) && 
          (!Action.isActionAttack(action.getNumber()))) {
          action.getPerformer().getCommunicator().sendNormalServerMessage("After you " + Actions.actionEntrys[
            ((Action)this.slowActions.getLast()).getNumber()].getVerbFinishString() + " you will " + Actions.actionEntrys[action
            .getNumber()].getVerbStartString() + " again.");
        } else if ((!((Action)this.slowActions.getLast()).isOffensive()) && (!Action.isActionAttack(action.getNumber()))) {
          action.getPerformer().getCommunicator().sendNormalServerMessage("After you " + Actions.actionEntrys[
            ((Action)this.slowActions.getLast()).getNumber()].getVerbFinishString() + " you will " + Actions.actionEntrys[action
            .getNumber()].getVerbStartString() + ".");
        } else if ((((Action)this.slowActions.getLast()).isOffensive()) && (action.isSpell())) {
          action.getPerformer().getCommunicator().sendCombatNormalMessage("After you " + Actions.actionEntrys[
            ((Action)this.slowActions.getLast()).getNumber()].getVerbFinishString() + " you will " + Actions.actionEntrys[action
            .getNumber()].getVerbStartString() + ".");
        }
        this.slowActions.addLast(action);
      }
    }
    else if (action.getNumber() == 114)
    {
      if (!action.poll()) {
        this.slowActions.add(action);
      }
    }
    else
    {
      this.slowActions.add(action);
    }
  }
  
  private void removeAction(Action action)
  {
    this.quickActions.remove(action);
    this.slowActions.remove(action);
  }
  
  public String stopCurrentAction(boolean farAway)
    throws NoSuchActionException
  {
    String toReturn = "";
    Action current = getCurrentAction();
    if (current.getNumber() == 136) {
      current.getPerformer().setStealth(current.getPerformer().isStealth());
    }
    toReturn = current.stop(farAway);
    if (current.getNumber() == 160) {
      MethodsFishing.playerOutOfRange(current.getPerformer(), current);
    }
    if ((current.getNumber() == 925) || (current.getNumber() == 926))
    {
      current.getPerformer().getCommunicator().sendCancelPlacingItem();
      toReturn = "";
    }
    removeAction(current);
    return toReturn;
  }
  
  public Action getCurrentAction()
    throws NoSuchActionException
  {
    if (!this.quickActions.isEmpty()) {
      return (Action)this.quickActions.getFirst();
    }
    if (!this.slowActions.isEmpty()) {
      return (Action)this.slowActions.getFirst();
    }
    throw new NoSuchActionException("No Current Action");
  }
  
  private long lastPolledStunned = 0L;
  
  public boolean poll(Creature owner)
  {
    boolean toReturn = true;
    if ((owner.getStatus().getStunned() > 0.0F) && (!owner.isDead()))
    {
      if (this.lastPolledStunned == 0L) {
        this.lastPolledStunned = System.currentTimeMillis();
      }
      toReturn = false;
      float delta = (float)(System.currentTimeMillis() - this.lastPolledStunned) / 1000.0F;
      owner.getStatus().setStunned(owner.getStatus().getStunned() - delta, false);
      if (owner.getStatus().getStunned() <= 0.0F) {
        this.lastPolledStunned = 0L;
      } else {
        this.lastPolledStunned = System.currentTimeMillis();
      }
    }
    else
    {
      if (!this.quickActions.isEmpty()) {
        while (!this.quickActions.isEmpty()) {
          if (((Action)this.quickActions.getFirst()).poll()) {
            this.quickActions.removeFirst();
          }
        }
      }
      if (!this.slowActions.isEmpty())
      {
        Action first = (Action)this.slowActions.getFirst();
        if (first.poll())
        {
          if (!this.slowActions.isEmpty()) {
            this.slowActions.removeFirst();
          }
          if (!this.slowActions.isEmpty())
          {
            first = (Action)this.slowActions.getFirst();
            if ((first.getCounterAsFloat() >= 1.0F) && (first.getNumber() != 114) && 
              (first.getNumber() != 160)) {
              owner.sendActionControl(first.getActionString(), true, first.getTimeLeft());
            } else if (first.getNumber() != 160) {
              owner.sendActionControl("", false, 0);
            }
          }
          else
          {
            owner.sendActionControl("", false, 0);
          }
        }
        else
        {
          toReturn = false;
        }
      }
    }
    return toReturn;
  }
  
  public void removeAttacks(Creature owner)
  {
    ListIterator<Action> lit;
    if (!this.clearing) {
      for (lit = this.slowActions.listIterator(); lit.hasNext();)
      {
        Action act = (Action)lit.next();
        if (act.getNumber() == 114) {
          lit.remove();
        }
      }
    }
  }
  
  public void removeTarget(long wurmid)
  {
    ListIterator<Action> lit;
    if (!this.clearing) {
      for (lit = this.slowActions.listIterator(); lit.hasNext();)
      {
        Action act = (Action)lit.next();
        if (act.getTarget() == wurmid)
        {
          try
          {
            if (act == getCurrentAction())
            {
              act.getPerformer().getCommunicator().sendNormalServerMessage(act.stop(false));
              act.getPerformer().sendActionControl("", false, 0);
            }
          }
          catch (NoSuchActionException localNoSuchActionException) {}
          lit.remove();
        }
      }
    }
  }
  
  public void replaceTarget(long wurmid)
  {
    ListIterator<Action> lit;
    if (!this.clearing) {
      for (lit = this.slowActions.listIterator(); lit.hasNext();)
      {
        Action act = (Action)lit.next();
        if (act.isOffensive()) {
          act.setTarget(wurmid);
        }
      }
    }
  }
  
  public void clear()
  {
    this.clearing = true;
    this.quickActions.clear();
    for (Action actionToStop : this.slowActions) {
      actionToStop.stop(false);
    }
    this.slowActions.clear();
    this.clearing = false;
  }
  
  public Action getLastSlowAction()
  {
    if (!this.clearing) {
      if (!this.slowActions.isEmpty()) {
        try
        {
          return (Action)this.slowActions.getLast();
        }
        catch (NoSuchElementException nse)
        {
          return null;
        }
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\ActionStack.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */