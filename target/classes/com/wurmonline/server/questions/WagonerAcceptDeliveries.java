package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.highways.PathToCalculate;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WagonerAcceptDeliveries
  extends Question
  implements MonetaryConstants
{
  private static final Logger logger = Logger.getLogger(WagonerAcceptDeliveries.class.getName());
  private static final String red = "color=\"255,127,127\";";
  private final Item waystone;
  private long deliveryId = -10L;
  private int sortBy = 1;
  private int pageNo = 1;
  
  public WagonerAcceptDeliveries(Creature aResponder, Item waystone)
  {
    super(aResponder, "Wagoner Accept Delivery Management", "Wagoner Accept Delivery Management", 146, waystone.getWurmId());
    this.waystone = waystone;
  }
  
  public WagonerAcceptDeliveries(Creature aResponder, Item waystone, long deliveryId, int sortBy, int pageNo)
  {
    super(aResponder, "Wagoner Accept Delivery Management", "Wagoner Accept Delivery Management", 146, waystone.getWurmId());
    this.waystone = waystone;
    this.deliveryId = deliveryId;
    this.sortBy = sortBy;
    this.pageNo = pageNo;
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
    switch (this.pageNo)
    {
    case 1: 
      boolean close = getBooleanProp("close");
      if (close) {
        return;
      }
      boolean next = getBooleanProp("next");
      String sel;
      if (next)
      {
        sel = aAnswer.getProperty("sel");
        this.deliveryId = Long.parseLong(sel);
        if (this.deliveryId == -10L)
        {
          getResponder().getCommunicator().sendNormalServerMessage("You decide to do nothing.");
          return;
        }
        this.pageNo = 2;
        this.sortBy = 1;
      }
      else
      {
        for (String key : getAnswer().stringPropertyNames()) {
          if (key.startsWith("sort"))
          {
            String sid = key.substring(4);
            this.sortBy = Integer.parseInt(sid);
            break;
          }
        }
      }
      WagonerAcceptDeliveries wad = new WagonerAcceptDeliveries(getResponder(), this.waystone, this.deliveryId, this.sortBy, this.pageNo);
      switch (this.pageNo)
      {
      case 1: 
        wad.sendQuestion();
        break;
      case 2: 
        wad.sendQuestion2();
      }
      return;
    case 2: 
      boolean back = getBooleanProp("back");
      if (back)
      {
        WagonerAcceptDeliveries wad = new WagonerAcceptDeliveries(getResponder(), this.waystone, this.deliveryId, this.sortBy, 1);
        
        wad.sendQuestion();
        return;
      }
      boolean accept = getBooleanProp("accept");
      if (accept)
      {
        Delivery delivery = Delivery.getDelivery(this.deliveryId);
        
        long rmoney = getResponder().getMoney();
        if (rmoney < delivery.getReceiverCost())
        {
          getResponder().getCommunicator().sendServerMessage("You cannot afford that delivery.", 255, 127, 127);
          return;
        }
        boolean passed = true;
        if (delivery.getReceiverCost() > 0L) {
          try
          {
            passed = getResponder().chargeMoney(delivery.getReceiverCost());
            if (passed)
            {
              Change change = Economy.getEconomy().getChangeFor(getResponder().getMoney());
              getResponder().getCommunicator().sendNormalServerMessage("You now have " + change.getChangeString() + " in the bank.");
              getResponder().getCommunicator().sendNormalServerMessage("If this amount is incorrect, please wait a while since the information may not immediately be updated.");
            }
          }
          catch (IOException e)
          {
            passed = false;
            getResponder().getCommunicator().sendServerMessage("Something went wrong!", 255, 127, 127);
            logger.log(Level.WARNING, e.getMessage(), e);
          }
        }
        if (passed)
        {
          delivery.setAccepted(this.waystone.getWurmId());
          getResponder().getCommunicator().sendServerMessage("Delivery accepted from " + delivery
            .getSenderName() + ".", 127, 255, 127);
          try
          {
            Player player = Players.getInstance().getPlayer(delivery.getSenderId());
            player.getCommunicator().sendServerMessage("Delivery accepted by " + delivery
              .getReceiverName() + ".", 127, 255, 127);
          }
          catch (NoSuchPlayerException localNoSuchPlayerException) {}
        }
        return;
      }
      boolean reject = getBooleanProp("reject");
      if (reject)
      {
        Delivery delivery = Delivery.getDelivery(this.deliveryId);
        delivery.setRejected();
        
        getResponder().getCommunicator().sendNormalServerMessage("Delivery rejected from " + delivery
          .getSenderName());
        try
        {
          Player player = Players.getInstance().getPlayer(delivery.getSenderId());
          player.getCommunicator().sendNormalServerMessage("Delivery rejected by " + delivery
            .getReceiverName());
        }
        catch (NoSuchPlayerException localNoSuchPlayerException1) {}
      }
      break;
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("text{text=\"\"}");
    
    buf.append("text{text=\"The following bulk deliveries are waiting, but you need to accept them first before their delivery can start.\"}");
    buf.append("text{text=\"If an delivery has a Cash On Delivery (C.O.D) cost associated with it, you have to pay upfront before the delivery will start, the monies will be held by the wagoner and will only be paid to the supplier when the delivery is complete.\"}");
    
    long money = getResponder().getMoney();
    if (money <= 0L) {
      buf.append("text{text='You have no money in the bank.'}");
    } else {
      buf.append("text{text='You have " + new Change(money).getChangeString() + " in the bank.'}");
    }
    Delivery[] deliveries = Delivery.getWaitingDeliveries(getResponder().getWurmId());
    int absSortBy = Math.abs(this.sortBy);
    int upDown = Integer.signum(this.sortBy);
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(deliveries, new WagonerAcceptDeliveries.1(this, upDown));
      
      break;
    case 2: 
      Arrays.sort(deliveries, new WagonerAcceptDeliveries.2(this, upDown));
      
      break;
    case 3: 
      Arrays.sort(deliveries, new WagonerAcceptDeliveries.3(this, upDown));
      
      break;
    case 4: 
      Arrays.sort(deliveries, new WagonerAcceptDeliveries.4(this, upDown));
      
      break;
    case 5: 
      Arrays.sort(deliveries, new WagonerAcceptDeliveries.5(this, upDown));
      
      break;
    case 6: 
      Arrays.sort(deliveries, new WagonerAcceptDeliveries.6(this, upDown));
      
      break;
    case 7: 
      Arrays.sort(deliveries, new WagonerAcceptDeliveries.7(this, upDown));
    }
    buf.append("label{text=\"Select which delivery to view \"};");
    buf.append("table{rows=\"1\";cols=\"9\";label{text=\"\"};" + 
    
      colHeader("Sender", 1, this.sortBy) + 
      colHeader("Receiver", 2, this.sortBy) + 
      colHeader("Delivery State", 3, this.sortBy) + 
      colHeader("# Crates", 4, this.sortBy) + 
      colHeader("Wagoner", 5, this.sortBy) + 
      colHeader("Wagoner State", 6, this.sortBy) + 
      colHeader("Cost", 7, this.sortBy) + "label{type=\"bold\";text=\"\"};");
    
    String noneSelected = "selected=\"true\";";
    for (Delivery delivery : deliveries)
    {
      boolean sameWaystone = delivery.getCollectionWaystoneId() == this.waystone.getWurmId();
      boolean connected = (!sameWaystone) && (PathToCalculate.isWaystoneConnected(delivery.getCollectionWaystoneId(), this.waystone.getWurmId()));
      String selected = "";
      if (this.deliveryId == delivery.getDeliveryId())
      {
        selected = "selected=\"true\";";
        noneSelected = "";
      }
      buf.append((connected ? "radio{group=\"sel\";id=\"" + delivery.getDeliveryId() + "\";" + selected + "text=\"\"};" : "label{text=\"  \"};") + "label{text=\"" + delivery
      
        .getSenderName() + "\"};label{text=\"" + delivery
        .getReceiverName() + "\"};label{text=\"" + delivery
        .getStateName() + "\"};label{text=\"" + delivery
        .getCrates() + "\"};label{text=\"" + delivery
        .getWagonerName() + "\"};label{text=\"" + delivery
        .getWagonerState() + "\"};label{text=\"" + new Change(delivery
        .getReceiverCost()).getChangeShortString() + "\"};");
      if (sameWaystone) {
        buf.append("label{color=\"255,127,127\";text=\"same waystone\";hover=\"Waystone is the collection one, no need for a wagoner.\"}");
      } else if (!connected) {
        buf.append("label{color=\"255,127,127\";text=\"no route!\";hover=\"No route found from collection waystone to this waystone.\"}");
      } else {
        buf.append("label{text=\"\"}");
      }
    }
    buf.append("}");
    buf.append("radio{group=\"sel\";id=\"-10\";" + noneSelected + "text=\" None\"}");
    buf.append("text{text=\"\"}");
    if (this.waystone.getData() != -1L)
    {
      Wagoner wagoner = Wagoner.getWagoner(this.waystone.getData());
      if (wagoner == null)
      {
        logger.log(Level.WARNING, "wagoner (" + this.waystone.getData() + ") not found that was associated with waystone " + this.waystone.getWurmId() + " @" + this.waystone
          .getTileX() + "," + this.waystone.getTileY() + "," + this.waystone.isOnSurface());
        this.waystone.setData(-1L);
      }
      else
      {
        buf.append("label{color=\"255,127,127\";text=\"This waystone is the home of " + wagoner.getName() + " and they wont allow deliveries here.\"};");
        buf.append("harray{button{text=\"Close\";id=\"close\"}}");
      }
    }
    if (this.waystone.getData() == -1L)
    {
      VolaTile vt = Zones.getTileOrNull(this.waystone.getTileX(), this.waystone.getTileY(), this.waystone.isOnSurface());
      Village village = vt != null ? vt.getVillage() : null;
      if ((village != null) && (!village.isActionAllowed((short)605, getResponder())))
      {
        buf.append("label{color=\"255,127,127\";text=\"You need Load permissions to be able to accept a delivery here!\"};");
        buf.append("harray{button{text=\"Close\";id=\"close\"}}");
      }
      else
      {
        buf.append("harray{label{text=\"Continue to \"};button{text=\"Next\";id=\"next\"}label{text=\" screen to view selected delivery.\"};}");
      }
    }
    buf.append("}};null;null;}");
    
    getResponder().getCommunicator().sendBml(600, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public void sendQuestion2()
  {
    Delivery delivery = Delivery.getDelivery(this.deliveryId);
    if (delivery == null)
    {
      getResponder().getCommunicator().sendNormalServerMessage("Delivery not found!");
      this.pageNo = 1;
      sendQuestion();
      return;
    }
    this.pageNo = 2;
    
    long money = getResponder().getMoney();
    boolean connected = PathToCalculate.isWaystoneConnected(delivery.getCollectionWaystoneId(), this.waystone.getWurmId());
    boolean hasAccept = (money >= delivery.getReceiverCost()) && (connected);
    String buffer = WagonerDeliveriesQuestion.showDelivery(delivery, getId(), getResponder(), true, !connected, hasAccept, true, false);
    
    getResponder().getCommunicator().sendBml(400, 400, true, true, buffer, 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\WagonerAcceptDeliveries.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */