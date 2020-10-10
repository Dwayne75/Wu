package com.wurmonline.server.behaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.TicketUpdateQuestion;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.Tickets;
import java.util.LinkedList;
import java.util.List;

public class TicketBehaviour
  extends Behaviour
  implements MiscConstants
{
  TicketBehaviour()
  {
    super((short)50);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int ticketId)
  {
    return getBehavioursFor(performer, ticketId);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, int ticketId)
  {
    List<ActionEntry> toReturn = new LinkedList();
    
    Ticket ticket = Tickets.getTicket(ticketId);
    if (ticket == null) {
      return toReturn;
    }
    Player player = (Player)performer;
    if (player.mayHearDevTalk())
    {
      if (ticket.isOpen())
      {
        toReturn.add(new ActionEntry((short)-4, "Forward", "forward", emptyIntArr));
        toReturn.add(Actions.actionEntrys['ɔ']);
        toReturn.add(Actions.actionEntrys['ɏ']);
        toReturn.add(Actions.actionEntrys['ɐ']);
        toReturn.add(Actions.actionEntrys['ɑ']);
        if (ticket.getResponderName().equalsIgnoreCase(performer.getName())) {
          toReturn.add(Actions.actionEntrys['ɒ']);
        }
        if (ticket.getCategoryCode() != 11) {
          toReturn.add(Actions.actionEntrys['ɍ']);
        }
        toReturn.add(Actions.actionEntrys['Ɏ']);
        if (!ticket.getResponderName().equalsIgnoreCase(performer.getName())) {
          toReturn.add(Actions.actionEntrys['ɓ']);
        }
      }
      else if (ticket.hasFeedback())
      {
        toReturn.add(Actions.actionEntrys['ɕ']);
      }
      else if (ticket.getStateCode() == 2)
      {
        toReturn.add(Actions.actionEntrys['ɗ']);
      }
      toReturn.add(Actions.actionEntrys['ɋ']);
    }
    else if (ticket.getPlayerId() == player.getWurmId())
    {
      if (ticket.isOpen())
      {
        if ((player.mayHearMgmtTalk()) && (ticket.getLevelCode() == 1))
        {
          toReturn.add(new ActionEntry((short)-1, "Forward", "forward", emptyIntArr));
          toReturn.add(Actions.actionEntrys['ɏ']);
        }
        toReturn.add(Actions.actionEntrys['ɋ']);
        toReturn.add(Actions.actionEntrys['Ɍ']);
      }
      else
      {
        toReturn.add(new ActionEntry((short)587, "View", "viewing", emptyIntArr));
        toReturn.add(Actions.actionEntrys['ɕ']);
      }
    }
    else if (player.mayHearMgmtTalk())
    {
      if ((ticket.isOpen()) && (player.mayMute()))
      {
        toReturn.add(new ActionEntry((short)-2, "Forward", "forward", emptyIntArr));
        toReturn.add(Actions.actionEntrys['ɔ']);
        toReturn.add(Actions.actionEntrys['ɏ']);
        if (ticket.getResponderName().equalsIgnoreCase(performer.getName())) {
          toReturn.add(Actions.actionEntrys['ɒ']);
        }
        toReturn.add(Actions.actionEntrys['ɍ']);
        toReturn.add(Actions.actionEntrys['Ɏ']);
      }
      toReturn.add(Actions.actionEntrys['ɋ']);
    }
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, int ticketId, short action, float counter)
  {
    Ticket ticket = Tickets.getTicket(ticketId);
    Player player = (Player)performer;
    if (player.mayHearDevTalk())
    {
      if (ticket.isOpen())
      {
        if (action == 596)
        {
          updateTicket(performer, ticketId, action);
        }
        else if (action == 591)
        {
          updateTicket(performer, ticketId, action);
        }
        else if (action == 592)
        {
          updateTicket(performer, ticketId, action);
        }
        else if (action == 593)
        {
          updateTicket(performer, ticketId, action);
        }
        else if ((ticket.getResponderName().equalsIgnoreCase(performer.getName())) && (action == 594))
        {
          updateTicket(performer, ticketId, action);
        }
        else if (action == 589)
        {
          player.respondGMTab(ticket.getPlayerName(), String.valueOf(ticket.getTicketId()));
          if (performer.getPower() >= 2) {
            ticket.addNewTicketAction((byte)3, performer.getName(), "GM " + performer.getName() + " responded.", (byte)0);
          } else {
            ticket.addNewTicketAction((byte)2, performer.getName(), "CM " + performer.getName() + " responded.", (byte)0);
          }
        }
        else if (action == 590)
        {
          updateTicket(performer, ticketId, action);
        }
        else if (action == 595)
        {
          ticket.addNewTicketAction((byte)11, performer.getName(), performer.getName() + " took ticket.", (byte)1);
        }
      }
      else if (action == 597) {
        updateTicket(performer, ticketId, action);
      } else if ((action == 599) && (ticket.getStateCode() == 2)) {
        updateTicket(performer, ticketId, action);
      }
      if (action == 587) {
        updateTicket(performer, ticketId, action);
      }
    }
    else if (ticket.getPlayerId() == player.getWurmId())
    {
      if ((player.mayHearMgmtTalk()) && (ticket.getLevelCode() == 1)) {
        updateTicket(performer, ticketId, action);
      } else if ((ticket.isOpen()) && (action == 588)) {
        updateTicket(performer, ticketId, action);
      } else if (action == 587) {
        updateTicket(performer, ticketId, action);
      } else if (action == 597) {
        updateTicket(performer, ticketId, action);
      }
    }
    else if (player.mayHearMgmtTalk())
    {
      if (ticket.isOpen()) {
        if (action == 596)
        {
          updateTicket(performer, ticketId, action);
        }
        else if (action == 591)
        {
          updateTicket(performer, ticketId, action);
        }
        else if ((ticket.getResponderName().equalsIgnoreCase(performer.getName())) && (action == 594))
        {
          updateTicket(performer, ticketId, action);
        }
        else if (action == 589)
        {
          player.respondGMTab(ticket.getPlayerName(), String.valueOf(ticket.getTicketId()));
          ticket.addNewTicketAction((byte)2, performer.getName(), "CM " + performer.getName() + " responded.", (byte)0);
        }
        else if (action == 590)
        {
          updateTicket(performer, ticketId, action);
        }
      }
      if (action == 587) {
        updateTicket(performer, ticketId, action);
      }
    }
    return true;
  }
  
  public boolean action(Action act, Creature performer, Item source, int ticketId, short action, float counter)
  {
    return action(act, performer, ticketId, action, counter);
  }
  
  private void updateTicket(Creature performer, int ticketId, short action)
  {
    TicketUpdateQuestion tuq = new TicketUpdateQuestion(performer, ticketId, action);
    tuq.sendQuestion();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\TicketBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */