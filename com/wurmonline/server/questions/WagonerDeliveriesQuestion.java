package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.highways.PathToCalculate;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Village;
import com.wurmonline.shared.util.MaterialUtilities;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class WagonerDeliveriesQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(WagonerDeliveriesQuestion.class.getName());
  private static final DecimalFormat df = new DecimalFormat("#0.00");
  private long deliveryId;
  private int sortBy = 1;
  private int pageNo = 1;
  private boolean hasBack = false;
  private final boolean hasList;
  
  public WagonerDeliveriesQuestion(Creature aResponder, long deliveryId, boolean hasList)
  {
    super(aResponder, getTitle(deliveryId), getTitle(deliveryId), 147, deliveryId);
    this.deliveryId = deliveryId;
    this.hasList = hasList;
  }
  
  public WagonerDeliveriesQuestion(Creature aResponder, long deliveryId, boolean hasList, int sortBy, int pageNo, boolean hasBack)
  {
    super(aResponder, getTitle(deliveryId), getTitle(deliveryId), 147, deliveryId);
    this.deliveryId = deliveryId;
    this.hasList = hasList;
    this.sortBy = sortBy;
    this.pageNo = pageNo;
    this.hasBack = hasBack;
  }
  
  private static String getTitle(long deliveryId)
  {
    if (deliveryId == -10L) {
      return "Wagoner Deliveries Management";
    }
    return "Wagoner Delivery Management";
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
    switch (this.pageNo)
    {
    case 1: 
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
        this.hasBack = true;
      }
      else
      {
        for (String key : getAnswer().stringPropertyNames())
        {
          if (key.startsWith("sort"))
          {
            String sid = key.substring(4);
            this.sortBy = Integer.parseInt(sid);
            break;
          }
          if (key.startsWith("assign"))
          {
            String sid = key.substring(6);
            this.deliveryId = Integer.parseInt(sid);
            WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
            
            wdq.sendQuestion3();
            return;
          }
        }
      }
      WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
      switch (this.pageNo)
      {
      case 1: 
        wdq.sendQuestion();
        break;
      case 2: 
        wdq.sendQuestion2();
      }
      return;
    case 2: 
      boolean cancel = getBooleanProp("cancel");
      if (cancel)
      {
        Delivery delivery = Delivery.getDelivery(this.deliveryId);
        if ((delivery != null) && (delivery.getState() == 0))
        {
          delivery.setCancelled();
          getResponder().getCommunicator().sendServerMessage("You have cancelled your delivery to " + delivery
            .getReceiverName() + ".", 255, 127, 127);
        }
        else if (delivery == null)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Delivery cannot be found!");
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("Delivery may not be cancelled as it has been accepted.");
        }
        return;
      }
      boolean assign = getBooleanProp("assign");
      if (assign)
      {
        WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
        
        wdq.sendQuestion3();
        return;
      }
      boolean back = getBooleanProp("back");
      if (back)
      {
        WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(getResponder(), -10L, true, 1, 1, false);
        wdq.sendQuestion();
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
        catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
      break;
    case 3: 
      boolean close = getBooleanProp("close");
      if (close) {
        return;
      }
      boolean cancel = getBooleanProp("cancel");
      if (cancel)
      {
        Delivery delivery = Delivery.getDelivery(this.deliveryId);
        delivery.setCancelledNoWagoner();
      }
      else
      {
        String sel = aAnswer.getProperty("sel");
        long selId = Long.parseLong(sel);
        if (selId == -10L)
        {
          getResponder().getCommunicator().sendNormalServerMessage("No wagoner selected.");
        }
        else
        {
          Delivery delivery = Delivery.getDelivery(this.deliveryId);
          delivery.setWagonerId(selId);
        }
      }
      WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
      if (this.hasList) {
        wdq.sendQuestion();
      } else {
        wdq.sendQuestion2();
      }
      break;
    }
  }
  
  public void sendQuestion()
  {
    this.pageNo = 1;
    
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("text{text=\"\"}");
    
    Delivery[] deliveries = Delivery.getPendingDeliveries(getResponder().getWurmId());
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    switch (absSortBy)
    {
    case 7: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getSenderName().compareTo(param2.getSenderName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getReceiverName().compareTo(param2.getReceiverName()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getStateName().compareTo(param2.getStateName()) * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          if (param1.getCrates() < param2.getCrates()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 5: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getWagonerName().compareTo(param2.getWagonerName()) * upDown;
        }
      });
      break;
    case 6: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          return param1.getWagonerState().compareTo(param2.getWagonerState()) * upDown;
        }
      });
      break;
    case 1: 
      Arrays.sort(deliveries, new Comparator()
      {
        public int compare(Delivery param1, Delivery param2)
        {
          if (param1.getWhen() < param2.getWhen()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
    }
    buf.append("label{text=\"Select which delivery to view \"};");
    buf.append("table{rows=\"1\";cols=\"8\";label{text=\"\"};" + 
    
      colHeader("Sender", 7, this.sortBy) + 
      colHeader("Receiver", 2, this.sortBy) + 
      colHeader("Delivery State", 3, this.sortBy) + 
      colHeader("# Crates", 4, this.sortBy) + 
      colHeader("Wagoner", 5, this.sortBy) + 
      colHeader("Wagoner State", 6, this.sortBy) + 
      colHeader("Last state change", 1, this.sortBy));
    
    String noneSelected = "selected=\"true\";";
    for (Delivery delivery : deliveries)
    {
      String rad;
      String rad;
      if (delivery.canSeeCrates()) {
        rad = "radio{group=\"sel\";id=\"" + delivery.getDeliveryId() + "\"text=\"\"}";
      } else {
        rad = "label{text=\"  \"};";
      }
      String wagonerState;
      String wagonerName;
      String wagonerState;
      if ((delivery.getWagonerId() == -10L) && (delivery.isQueued()))
      {
        String wagonerName = "button{text=\"Assign wagoner\";id=\"assign" + delivery.getDeliveryId() + "\"}";
        wagonerState = "label{text=\"\"};";
      }
      else
      {
        wagonerName = "label{text=\"" + delivery.getWagonerName() + "\"};";
        wagonerState = "label{text=\"" + delivery.getWagonerState() + "\"};";
      }
      buf.append(rad + "label{text=\"" + delivery
        .getSenderName() + "\"};label{text=\"" + delivery
        .getReceiverName() + "\"};label{text=\"" + delivery
        .getStateName() + "\"};label{text=\"" + delivery
        .getCrates() + "\"};" + wagonerName + wagonerState + "label{text=\"" + delivery
        
        .getStringWhen() + "\"}");
    }
    buf.append("}");
    buf.append("radio{group=\"sel\";id=\"-10\";" + noneSelected + "text=\" None\"}");
    buf.append("text{text=\"\"}");
    
    buf.append("harray{label{text=\"Continue to \"};button{text=\"Next\";id=\"next\"}label{text=\" screen to view selected delivery.\"};}");
    
    buf.append("}};null;null;}");
    
    getResponder().getCommunicator().sendBml(600, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public void sendQuestion2()
  {
    Delivery delivery = Delivery.getDelivery(this.deliveryId);
    if (delivery == null)
    {
      getResponder().getCommunicator().sendNormalServerMessage("Delivery not found!");
      if (this.hasBack)
      {
        this.pageNo = 1;
        sendQuestion();
      }
      return;
    }
    this.pageNo = 2;
    boolean hasCancel = (delivery.getSenderId() == getResponder().getWurmId()) && (delivery.getState() == 0);
    boolean hasReject = (delivery.getReceiverId() == getResponder().getWurmId()) && (delivery.getState() == 0);
    Creature creature = delivery.getReceiverId() == getResponder().getWurmId() ? getResponder() : null;
    String buffer = showDelivery(delivery, getId(), creature, this.hasBack, false, false, hasReject, hasCancel);
    getResponder().getCommunicator().sendBml(400, 400, true, true, buffer, 200, 200, 200, this.title);
  }
  
  public void sendQuestion3()
  {
    Delivery delivery = Delivery.getDelivery(this.deliveryId);
    if (delivery == null)
    {
      getResponder().getCommunicator().sendNormalServerMessage("Delivery not found!");
      if (this.hasBack)
      {
        this.pageNo = 1;
        sendQuestion();
      }
      return;
    }
    this.pageNo = 3;
    
    Set<Creature> creatureSet = Creatures.getMayUseWagonersFor(getResponder());
    
    Set<Distanced> wagonerSet = new HashSet();
    float dist = PathToCalculate.getRouteDistance(delivery.getCollectionWaystoneId(), delivery.getDeliveryWaystoneId());
    if (dist != 99999.0F) {
      for (Creature creature : creatureSet)
      {
        Wagoner wagoner = Wagoner.getWagoner(creature.getWurmId());
        if (wagoner != null)
        {
          dist = PathToCalculate.getRouteDistance(wagoner.getHomeWaystoneId(), delivery.getCollectionWaystoneId());
          if (dist != 99999.0F)
          {
            boolean isPublic = creature.publicMayUse(getResponder());
            String villName = "";
            Village vill = creature.getCitizenVillage();
            if (vill != null) {
              villName = vill.getName();
            }
            wagonerSet.add(new Distanced(wagoner, isPublic, villName, dist));
          }
        }
      }
    }
    Distanced[] wagonerArr = (Distanced[])wagonerSet.toArray(new Distanced[wagonerSet.size()]);
    int absSortBy = Math.abs(this.sortBy);
    final int upDown = Integer.signum(this.sortBy);
    switch (absSortBy)
    {
    case 1: 
      Arrays.sort(wagonerArr, new Comparator()
      {
        public int compare(WagonerDeliveriesQuestion.Distanced param1, WagonerDeliveriesQuestion.Distanced param2)
        {
          return param1.getWagoner().getName().compareTo(param2.getWagoner().getName()) * upDown;
        }
      });
      break;
    case 2: 
      Arrays.sort(wagonerArr, new Comparator()
      {
        public int compare(WagonerDeliveriesQuestion.Distanced param1, WagonerDeliveriesQuestion.Distanced param2)
        {
          return param1.getType().compareTo(param2.getType()) * upDown;
        }
      });
      break;
    case 3: 
      Arrays.sort(wagonerArr, new Comparator()
      {
        public int compare(WagonerDeliveriesQuestion.Distanced param1, WagonerDeliveriesQuestion.Distanced param2)
        {
          return param1.getVillageName().compareTo(param2.getVillageName()) * upDown;
        }
      });
      break;
    case 4: 
      Arrays.sort(wagonerArr, new Comparator()
      {
        public int compare(WagonerDeliveriesQuestion.Distanced param1, WagonerDeliveriesQuestion.Distanced param2)
        {
          if (param1.getDistance() < param2.getDistance()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
      break;
    case 5: 
      Arrays.sort(wagonerArr, new Comparator()
      {
        public int compare(WagonerDeliveriesQuestion.Distanced param1, WagonerDeliveriesQuestion.Distanced param2)
        {
          return param1.getWagoner().getStateName().compareTo(param2.getWagoner().getStateName()) * upDown;
        }
      });
      break;
    case 6: 
      Arrays.sort(wagonerArr, new Comparator()
      {
        public int compare(WagonerDeliveriesQuestion.Distanced param1, WagonerDeliveriesQuestion.Distanced param2)
        {
          if (param1.getQueueLength() < param2.getQueueLength()) {
            return 1 * upDown;
          }
          return -1 * upDown;
        }
      });
    }
    int height = 300;
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("closebutton{id=\"close\"};");
    buf.append("label{text=\"Select which wagoner to use \"};");
    buf.append("table{rows=\"1\";cols=\"8\";label{text=\"\"};" + 
    
      colHeader("Name", 1, this.sortBy) + 
      colHeader("Type", 2, this.sortBy) + 
      colHeader("Village", 3, this.sortBy) + 
      colHeader("Distance", 4, this.sortBy) + 
      colHeader("State", 5, this.sortBy) + 
      colHeader("Queue", 6, this.sortBy) + "label{type=\"bold\";text=\"\"};");
    
    String noneSelected = "selected=\"true\";";
    for (Distanced distanced : wagonerArr) {
      buf.append("radio{group=\"sel\";id=\"" + distanced
        .getWagoner().getWurmId() + "\";text=\"\"}label{text=\"" + distanced
        .getWagoner().getName() + "\"};label{text=\"" + distanced
        .getType() + "\"};label{text=\"" + distanced
        .getVillageName() + "\"};label{text=\"" + 
        (int)distanced.getDistance() + "\"};label{text=\"" + distanced
        .getWagoner().getStateName() + "\"};label{text=\"" + distanced
        .getQueueLength() + "\"};label{text=\"\"}");
    }
    buf.append("}");
    buf.append("radio{group=\"sel\";id=\"-10\";" + noneSelected + "text=\" None\"}");
    buf.append("text{text=\"\"}");
    String assignButton;
    String assignButton;
    if (wagonerArr.length > 0) {
      assignButton = "button{text=\"Assign\";id=\"assign\"};label{text=\" \"};";
    } else {
      assignButton = "";
    }
    buf.append("harray{" + assignButton + "button{text=\"Cancel Delivery\";id=\"cancel\";hover=\"This will remove this delivery from the wagoner queue\";confirm=\"You are about to cancel a delivery to " + delivery
    
      .getReceiverName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"};button{text=\"Back to Delivery\";id=\"back\"}label{text=\" \"};button{text=\"Close\";id=\"close\"}}");
    
    buf.append("}};null;null;}");
    height += wagonerArr.length * 20;
    
    getResponder().getCommunicator().sendBml(420, height, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  static String showDelivery(Delivery delivery, int questionId, @Nullable Creature creature, boolean hasBack, boolean isNotConnected, boolean hasAccept, boolean hasReject, boolean hasCancel)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("border{scroll{vertical='true';horizontal='false';varray{rescale='true';passthrough{id='id';text='" + questionId + "'}");
    
    buf.append("text{text=\"Delivery of " + delivery.getCrates() + " crate" + (delivery
      .getCrates() == 1 ? "" : "s") + " from " + delivery
      .getSenderName() + " to " + delivery.getReceiverName() + ".\"}");
    String wagonerName;
    String wagonerName;
    if ((delivery.getWagonerId() == -10L) && (delivery.isQueued())) {
      wagonerName = "harray{label{text=\"Using \"};button{text=\"Assign wagoner\";id=\"assign\"};label{text=\" \"};}";
    } else {
      wagonerName = "text{text=\"Using " + delivery.getWagonerName() + ".\"}";
    }
    buf.append(wagonerName);
    if (isNotConnected)
    {
      buf.append("label{text=\"This waystone is not connected to the collection waystone, so cannot accept it here.\"}");
    }
    else if (creature != null)
    {
      long money = creature.getMoney();
      if (money <= 0L) {
        buf.append("text{text='You have no money in the bank.'}");
      } else {
        buf.append("text{text='You have " + new Change(money).getChangeString() + " in the bank.'}");
      }
    }
    buf.append("}};null;");
    
    buf.append("tree{id=\"t1\";cols=\"3\";showheader=\"true\";height=\"300\"col{text=\"QL\";width=\"50\"};col{text=\"DMG\";width=\"50\"};col{text=\"Weight\";width=\"50\"};");
    
    Item crateContainer = delivery.getCrateContainer();
    if (crateContainer != null)
    {
      Item[] crates = crateContainer.getItemsAsArray();
      for (Item crate : crates) {
        buf.append(addCrate(crate));
      }
    }
    buf.append("}");
    
    buf.append("null;varray{");
    if (delivery.getSenderCost() > 0L)
    {
      buf.append("label{text=\"The delivery fees of " + new Change(delivery.getSenderCost()).getChangeString() + " have been paid for by " + delivery
        .getSenderName() + ".\"}");
    }
    else
    {
      int deliveryFees = delivery.getCrates() * 100;
      buf.append("label{text=\"The delivery fees of " + new Change(deliveryFees).getChangeString() + " has been added into the goods cost.\"}");
    }
    if (delivery.getReceiverCost() == 0L) {
      buf.append("label{text=\"The goods have already been paid for by " + delivery.getSenderName() + ".\"}");
    } else if (delivery.getState() == 0) {
      buf.append("label{text=\"The goods cost of " + new Change(delivery.getReceiverCost()).getChangeString() + " have yet to be agreed by " + delivery
        .getReceiverName() + ".\"}");
    } else {
      buf.append("label{text=\"The goods cost of " + new Change(delivery.getReceiverCost()).getChangeString() + " has been paid for by " + delivery
        .getReceiverName() + ".\"}");
    }
    buf.append("label{text=\"All monies are held by the wagoner until the delivery is complete.\"}");
    
    buf.append("harray{" + (hasAccept ? "button{text=\"Accept\";id=\"accept\"};label{text=\" \"};" : "") + (hasReject ? "button{text=\"Reject\";id=\"reject\"};label{text=\" \"};" : "") + ((!hasAccept) && (!hasReject) ? "button{text=\"Close\";id=\"submit\"};label{text=\" \"};" : "") + (hasCancel ? "button{text=\"Cancel Delivery\";id=\"cancel\";hover=\"This will remove this delivery from the wagoner queue\";confirm=\"You are about to cancel a delivery to " + delivery
    
      .getReceiverName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"};" : "") + (hasBack ? "button{text=\"Back to list\";id=\"back\"};" : "") + "}");
    
    buf.append("text=\"\"}}");
    
    return buf.toString();
  }
  
  private static String addCrate(Item crate)
  {
    StringBuilder buf = new StringBuilder();
    
    String sQL = "" + df.format(crate.getQualityLevel());
    String sDMG = "" + df.format(crate.getDamage());
    String sWeight = "" + df.format(crate.getFullWeight(true) / 1000.0F);
    String itemName = longItemName(crate);
    String hover = itemName;
    Item[] contained = crate.getItemsAsArray();
    int children = contained.length;
    
    buf.append("row{id=\"" + crate.getWurmId() + "\";hover=\"" + hover + "\";name=\"" + itemName + "\";rarity=\"" + crate
      .getRarity() + "\";children=\"" + children + "\";col{text=\"" + sQL + "\"};col{text=\"" + sDMG + "\"};col{text=\"" + sWeight + "\"}}");
    for (Item bulkItem : contained) {
      buf.append(addBulkItem(bulkItem));
    }
    return buf.toString();
  }
  
  private static String addBulkItem(Item bulkItem)
  {
    StringBuilder buf = new StringBuilder();
    
    String sQL = "" + df.format(bulkItem.getQualityLevel());
    String sWeight = "" + df.format(bulkItem.getFullWeight(true) / 1000.0F);
    String itemName = longItemName(bulkItem);
    String hover = itemName;
    
    buf.append("row{id=\"" + bulkItem.getWurmId() + "\";hover=\"" + hover + "\";name=\"" + itemName + "\";rarity=\"0\";children=\"0\";col{text=\"" + sQL + "\"};col{text=\"0.00\"};col{text=\"" + sWeight + "\"}}");
    
    return buf.toString();
  }
  
  public static String longItemName(Item litem)
  {
    StringBuilder sb = new StringBuilder();
    if (litem.getRarity() == 1) {
      sb.append("rare ");
    } else if (litem.getRarity() == 2) {
      sb.append("supreme ");
    } else if (litem.getRarity() == 3) {
      sb.append("fantastic ");
    }
    String name = litem.getName().length() == 0 ? litem.getTemplate().getName() : litem.getName();
    MaterialUtilities.appendNameWithMaterialSuffix(sb, name.replace("\"", "''"), litem.getMaterial());
    if (litem.getDescription().length() > 0) {
      sb.append(" (" + litem.getDescription() + ")");
    }
    return sb.toString();
  }
  
  class Distanced
  {
    private final Wagoner wagoner;
    private final boolean isPublic;
    private final String villageName;
    private final float distance;
    private final int queueLength;
    
    Distanced(Wagoner wagoner, boolean isPublic, String villageName, float distance)
    {
      this.wagoner = wagoner;
      this.isPublic = isPublic;
      this.villageName = villageName;
      this.distance = distance;
      this.queueLength = Delivery.getQueueLength(wagoner.getWurmId());
    }
    
    Wagoner getWagoner()
    {
      return this.wagoner;
    }
    
    float getDistance()
    {
      return this.distance;
    }
    
    boolean isPublic()
    {
      return this.isPublic;
    }
    
    String getType()
    {
      if (this.isPublic) {
        return "Public";
      }
      return "Private";
    }
    
    String getVillageName()
    {
      return this.villageName;
    }
    
    int getQueueLength()
    {
      return this.queueLength;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\WagonerDeliveriesQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */