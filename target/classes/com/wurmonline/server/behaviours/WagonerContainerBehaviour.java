package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.WagonerDeliveriesQuestion;
import com.wurmonline.server.questions.WagonerSetupDeliveryQuestion;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class WagonerContainerBehaviour
  extends ItemBehaviour
{
  private static final Logger logger = Logger.getLogger(WagonerContainerBehaviour.class.getName());
  
  WagonerContainerBehaviour()
  {
    super((short)61);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
    toReturn.addAll(getBehavioursForWagonerContainer(performer, null, target));
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
    toReturn.addAll(getBehavioursForWagonerContainer(performer, source, target));
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean[] ans = wagonerContainerActions(act, performer, null, target, action, counter);
    if (ans[0] != 0) {
      return ans[1];
    }
    return super.action(act, performer, target, action, counter);
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    boolean[] ans = wagonerContainerActions(act, performer, source, target, action, counter);
    if (ans[0] != 0) {
      return ans[1];
    }
    return super.action(act, performer, source, target, action, counter);
  }
  
  private List<ActionEntry> getBehavioursForWagonerContainer(Creature performer, @Nullable Item source, Item container)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (Features.Feature.WAGONER.isEnabled())
    {
      if ((container.isPlanted()) && (!container.isSealedByPlayer()) && (!container.isEmpty(false))) {
        toReturn.add(Actions.actionEntrys['Γ']);
      }
      if (container.isSealedByPlayer())
      {
        Delivery delivery = Delivery.canViewDelivery(container, performer);
        if (delivery != null) {
          toReturn.add(Actions.actionEntrys['Ζ']);
        }
        if (Delivery.canUnSealContainer(container, performer)) {
          toReturn.add(Actions.actionEntrys['ˤ']);
        }
      }
    }
    return toReturn;
  }
  
  public boolean[] wagonerContainerActions(Action act, Creature performer, @Nullable Item source, Item container, short action, float counter)
  {
    if (Features.Feature.WAGONER.isEnabled())
    {
      if ((action == 915) && (container.isPlanted()) && (!container.isSealedByPlayer()) && (!container.isEmpty(false)))
      {
        WagonerSetupDeliveryQuestion wsdq = new WagonerSetupDeliveryQuestion(performer, container);
        wsdq.sendQuestion();
        return new boolean[] { true, true };
      }
      Delivery delivery = Delivery.canViewDelivery(container, performer);
      if ((delivery != null) && (action == 918) && (container.isSealedByPlayer()))
      {
        WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(performer, delivery.getDeliveryId(), false);
        wdq.sendQuestion2();
        return new boolean[] { true, true };
      }
      if (Delivery.canUnSealContainer(container, performer)) {
        container.setIsSealedByPlayer(false);
      }
    }
    return new boolean[] { false, false };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\WagonerContainerBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */