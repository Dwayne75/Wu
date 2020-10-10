package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.util.MaterialUtilities;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MailReceiveQuestion
  extends Question
  implements MonetaryConstants, TimeConstants
{
  private static final Logger logger = Logger.getLogger(MailReceiveQuestion.class.getName());
  private final Item mbox;
  private Set<WurmMail> mailset = null;
  
  public MailReceiveQuestion(Creature aResponder, String aTitle, String aQuestion, Item aMailbox)
  {
    super(aResponder, aTitle, aQuestion, 53, aMailbox.getWurmId());
    this.mbox = aMailbox;
  }
  
  public void answer(Properties answers)
  {
    if (!this.mbox.isEmpty(false))
    {
      getResponder().getCommunicator().sendNormalServerMessage("Empty the mailbox first.");
    }
    else
    {
      if (this.mailset.isEmpty()) {
        return;
      }
      if (!Servers.loginServer.isAvailable(5, true))
      {
        getResponder().getCommunicator().sendNormalServerMessage("You may not receive mail right now. Please try later.");
        
        return;
      }
      int x = 0;
      float priceMod = 1.0F;
      int pcost = 0;
      long fullcost = 0L;
      Map<Long, Long> moneyToSend = new HashMap();
      WurmMail m = null;
      String val = "";
      Set<Item> itemset = new HashSet();
      Map<Long, WurmMail> toReturn = null;
      for (Iterator<WurmMail> it = this.mailset.iterator(); it.hasNext();)
      {
        x++;
        m = (WurmMail)it.next();
        priceMod = 1.0F;
        
        receiver = WurmMail.getReceiverForItem(m.itemId);
        if (receiver == getResponder().getWurmId())
        {
          val = answers.getProperty(x + "receive");
          if ((val != null) && (val.equals("true")))
          {
            try
            {
              Item item = Items.getItem(m.itemId);
              if (m.rejected)
              {
                pcost = 100;
                if ((item.getTemplateId() == 748) || (item.getTemplateId() == 1272))
                {
                  InscriptionData insData = item.getInscription();
                  if (insData != null) {
                    if (insData.hasBeenInscribed()) {
                      pcost = 1;
                    }
                  }
                }
                fullcost += pcost;
              }
              else if (m.type == 1)
              {
                pcost = MailSendConfirmQuestion.getCostForItem(item, priceMod);
                fullcost += pcost;
                fullcost += m.price;
                if (m.price > 0L)
                {
                  Long msend = (Long)moneyToSend.get(Long.valueOf(m.sender));
                  if (msend == null) {
                    msend = Long.valueOf(m.price);
                  } else {
                    msend = Long.valueOf(msend.longValue() + m.price);
                  }
                  moneyToSend.put(Long.valueOf(m.sender), msend);
                }
                Item[] contained = item.getAllItems(true);
                for (int c = 0; c < contained.length; c++)
                {
                  pcost = MailSendConfirmQuestion.getCostForItem(contained[c], priceMod);
                  fullcost += pcost;
                }
              }
              itemset.add(item);
            }
            catch (NoSuchItemException nsi)
            {
              logger.log(Level.INFO, " NO SUCH ITEM");
              WurmMail.deleteMail(m.itemId);
            }
          }
          else
          {
            val = answers.getProperty(x + "return");
            if ((val != null) && (val.equals("true")))
            {
              if (toReturn == null) {
                toReturn = new HashMap();
              }
              toReturn.put(Long.valueOf(m.itemId), m);
            }
            else if (m.isExpired())
            {
              if (toReturn == null) {
                toReturn = new HashMap();
              }
              toReturn.put(Long.valueOf(m.itemId), m);
            }
          }
        }
      }
      long receiver;
      if (toReturn != null)
      {
        Map<Integer, Set<WurmMail>> serverReturns = new HashMap();
        for (WurmMail retm : toReturn.values())
        {
          long timeavail = System.currentTimeMillis() + (101 - (int)this.mbox.getSpellCourierBonus()) * 60000L;
          if (getResponder().getPower() > 0) {
            timeavail = System.currentTimeMillis() + 60000L;
          }
          WurmMail mail = new WurmMail((byte)1, retm.itemId, getResponder().getWurmId(), retm.sender, 0L, timeavail, System.currentTimeMillis() + (Servers.localServer.testServer ? 3600000L : 604800000L), Servers.localServer.id, true, false);
          if (retm.sourceserver == Servers.localServer.id)
          {
            WurmMail.removeMail(retm.itemId);
            WurmMail.addWurmMail(mail);
            mail.createInDatabase();
          }
          else
          {
            Set<WurmMail> returnSet = (Set)serverReturns.get(Integer.valueOf(retm.sourceserver));
            if (returnSet == null) {
              returnSet = new HashSet();
            }
            returnSet.add(mail);
            serverReturns.put(Integer.valueOf(retm.sourceserver), returnSet);
          }
        }
        if (!serverReturns.isEmpty())
        {
          Map<Long, ReiceverReturnMails> returnsPerReceiver = new HashMap();
          for (Iterator<Map.Entry<Integer, Set<WurmMail>>> it = serverReturns.entrySet().iterator(); it.hasNext();)
          {
            Map.Entry<Integer, Set<WurmMail>> entry = (Map.Entry)it.next();
            sid = (Integer)entry.getKey();
            Set<WurmMail> mails = (Set)entry.getValue();
            for (it2 = mails.iterator(); it2.hasNext();)
            {
              WurmMail newmail = (WurmMail)it2.next();
              try
              {
                Item i = Items.getItem(newmail.itemId);
                ReiceverReturnMails returnSetReceiver = (ReiceverReturnMails)returnsPerReceiver.get(Long.valueOf(newmail.receiver));
                if (returnSetReceiver == null)
                {
                  returnSetReceiver = new ReiceverReturnMails();
                  returnSetReceiver.setReceiverId(newmail.receiver);
                  returnSetReceiver.setServerId(sid.intValue());
                }
                returnSetReceiver.addMail(newmail, i);
                Item[] contained = i.getAllItems(true);
                for (int c = 0; c < contained.length; c++) {
                  returnSetReceiver.addMail(newmail, contained[c]);
                }
                returnsPerReceiver.put(Long.valueOf(newmail.receiver), returnSetReceiver);
              }
              catch (NoSuchItemException nsi)
              {
                logger.log(Level.WARNING, "The item that should be returned is gone!");
              }
            }
          }
          Integer sid;
          Iterator<WurmMail> it2;
          if (!returnsPerReceiver.isEmpty())
          {
            boolean problem = false;
            Iterator<Map.Entry<Long, ReiceverReturnMails>> it = returnsPerReceiver.entrySet().iterator();
            while (it.hasNext())
            {
              Map.Entry<Long, ReiceverReturnMails> entry = (Map.Entry)it.next();
              Long rid = (Long)entry.getKey();
              ReiceverReturnMails returnSetReceiver = (ReiceverReturnMails)entry.getValue();
              Item[] items = returnSetReceiver.getReturnItemSetAsArray();
              problem = MailSendConfirmQuestion.sendMailSetToServer(getResponder().getWurmId(), getResponder(), returnSetReceiver
                .getServerId(), returnSetReceiver.getReturnWurmMailSet(), rid.longValue(), items);
              if (!problem) {
                for (int a = 0; a < items.length; a++)
                {
                  Item[] contained = items[a].getAllItems(true);
                  for (int c = 0; c < contained.length; c++) {
                    Items.destroyItem(contained[c].getWurmId());
                  }
                  Items.destroyItem(items[a].getWurmId());
                  WurmMail.removeMail(items[a].getWurmId());
                }
              }
            }
          }
        }
      }
      long money = getResponder().getMoney();
      if (fullcost > money)
      {
        Change change = new Change(fullcost - money);
        getResponder().getCommunicator().sendNormalServerMessage("You need " + change
          .getChangeString() + " in order to receive the selected items.");
      }
      else if (fullcost > 0L)
      {
        LoginServerWebConnection lsw = new LoginServerWebConnection();
        try
        {
          int xx;
          Iterator<Map.Entry<Long, Long>> it;
          if (getResponder().chargeMoney(fullcost))
          {
            for (Iterator<Item> it = itemset.iterator(); it.hasNext();)
            {
              Item item = (Item)it.next();
              Item[] contained = item.getAllItems(true);
              for (int c = 0; c < contained.length; c++)
              {
                contained[c].setMailed(false);
                contained[c].setLastOwnerId(getResponder().getWurmId());
              }
              WurmMail.removeMail(item.getWurmId());
              this.mbox.insertItem(item, true);
              item.setLastOwnerId(getResponder().getWurmId());
              item.setMailed(false);
              logger.log(Level.INFO, 
                getResponder().getName() + " received " + item.getName() + " " + item.getWurmId());
            }
            Change change = new Change(fullcost);
            getResponder().getCommunicator().sendNormalServerMessage("The items are now available and you have been charged " + change
              .getChangeString() + ".");
            
            xx = 0;
            for (it = moneyToSend.entrySet().iterator(); it.hasNext();)
            {
              xx++;
              Map.Entry<Long, Long> entry = (Map.Entry)it.next();
              PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(((Long)entry.getKey()).longValue());
              if (pinf != null)
              {
                if (((Long)entry.getValue()).longValue() > 0L)
                {
                  logger.log(Level.INFO, 
                    getResponder().getName() + " adding COD " + ((Long)entry.getValue()).longValue() + " to " + pinf
                    
                    .getName() + " via server " + lsw
                    .getServerId());
                  lsw.addMoney(pinf.wurmId, pinf.getName(), ((Long)entry.getValue()).longValue(), "Mail " + 
                  
                    getResponder().getName() + 
                    DateFormat.getInstance().format(new Date()).replace(" ", "") + xx);
                }
              }
              else if (((Long)entry.getValue()).longValue() > 0L)
              {
                logger.log(Level.INFO, "Adding COD " + ((Long)entry.getValue()).longValue() + " to " + 
                  ((Long)entry.getKey()).longValue() + " (no name) via server " + lsw.getServerId());
                lsw.addMoney(((Long)entry.getKey()).longValue(), null, ((Long)entry.getValue()).longValue(), "Mail " + 
                
                  getResponder().getName() + 
                  DateFormat.getInstance().format(new Date()).replace(" ", "") + xx);
              }
              else
              {
                getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the receiver of some money.");
                
                logger.log(Level.WARNING, "failed to locate receiver " + 
                  ((Long)entry.getKey()).longValue() + " of amount " + 
                  ((Long)entry.getValue()).longValue() + " from " + getResponder().getName() + ".");
              }
            }
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("Failed to charge you the money. The bank may not be available. No mail received.");
          }
        }
        catch (IOException iox)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Failed to charge you the money. The bank may not be available. No mail received.");
        }
      }
      else
      {
        for (Iterator<Item> it = itemset.iterator(); it.hasNext();)
        {
          Item item = (Item)it.next();
          Item[] contained = item.getAllItems(true);
          for (int c = 0; c < contained.length; c++)
          {
            contained[c].setMailed(false);
            contained[c].setLastOwnerId(getResponder().getWurmId());
          }
          WurmMail.removeMail(item.getWurmId());
          this.mbox.insertItem(item, true);
          item.setLastOwnerId(getResponder().getWurmId());
          item.setMailed(false);
        }
        if (itemset.size() > 0) {
          getResponder().getCommunicator().sendNormalServerMessage("The items are now available in the " + this.mbox
            .getName() + ".");
        }
        if ((toReturn != null) && (toReturn.size() > 0)) {
          getResponder().getCommunicator().sendNormalServerMessage("The spirits will return the unwanted items.");
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    String lHtml = "border{scroll{vertical='true';horizontal='false';varray{rescale='true';passthrough{id='id';text='" + getId() + "'}";
    StringBuilder buf = new StringBuilder(lHtml);
    if (!this.mbox.isEmpty(false))
    {
      buf.append("label{text=\"Empty the mailbox first.\"}");
      buf.append("text{text=\"\"};}null;varray{");
    }
    else
    {
      this.mailset = WurmMail.getSentMailsFor(getResponder().getWurmId(), 100);
      if (this.mailset.isEmpty())
      {
        buf.append("text{text='You have no pending mail.'}");
        buf.append("text{text=\"\"};}null;");
      }
      else
      {
        buf.append("text{text='Use the checkboxes to select which items you wish to receive in your mailbox, and which to return to the sender.'}");
        buf.append("text{text='If an item has a Cash On Delivery (C.O.D) cost, you have to have that money in the bank.'}");
        long money = getResponder().getMoney();
        if (money <= 0L) {
          buf.append("text{text='You have no money in the bank.'}");
        } else {
          buf.append("text{text='You have " + new Change(money).getChangeString() + " in the bank.'}");
        }
        buf.append("}};null;");
        
        int rowNumb = 0;
        buf.append("tree{id=\"t1\";cols=\"10\";showheader=\"true\";height=\"300\"col{text=\"QL\";width=\"45\"};col{text=\"DAM\";width=\"45\"};col{text=\"Receive\";width=\"50\"};col{text=\"Return\";width=\"50\"};col{text=\"G\";width=\"25\"};col{text=\"S\";width=\"25\"};col{text=\"C\";width=\"25\"};col{text=\"I\";width=\"25\"};col{text=\"Sender\";width=\"75\"};col{text=\"Expiry\";width=\"220\"};");
        for (Iterator<WurmMail> it = this.mailset.iterator(); it.hasNext();)
        {
          WurmMail m = (WurmMail)it.next();
          try
          {
            rowNumb++;
            Item item = Items.getItem(m.itemId);
            buf.append(addItem("" + rowNumb, item, m, true));
          }
          catch (NoSuchItemException e)
          {
            buf.append("row{id=\"e" + rowNumb + "\";hover=\"Item gone.\";name=\"Item gone.\";rarity=\"0\";children=\"0\";col{text=\"n/a\"};col{text=\"n/a\"};col{text=\"n/a\"};col{text=\"n/a\"};col{text=\"\"};col{text=\"\"};col{text=\"\"};col{text=\"\"};col{text=\"n/a\"};col{text=\"n/a\"}}");
          }
        }
        buf.append("}");
        buf.append("null;varray{");
        if (this.mailset.size() < 100) {
          buf.append("label{text='You have no more mail.'}");
        } else {
          buf.append("text{text='You may have more mail than these. Manage these then check again.'}");
        }
      }
    }
    buf.append("harray{button{text=\"Send\";id=\"submit\"}}text=\"\"}}");
    getResponder().getCommunicator().sendBml(700, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private String addItem(String id, Item item, WurmMail m, boolean isTopLevel)
  {
    StringBuilder buf = new StringBuilder();
    Change change = null;
    float priceMod = 1.0F;
    int pcost = 0;
    long fullcost = 0L;
    
    Item[] contained = item.getItemsAsArray();
    int children = contained.length;
    if ((m.rejected) && (isTopLevel))
    {
      pcost = 100;
      if ((item.getTemplateId() == 748) || (item.getTemplateId() == 1272))
      {
        InscriptionData insData = item.getInscription();
        if (insData != null) {
          if (insData.hasBeenInscribed()) {
            pcost = 1;
          }
        }
      }
      change = new Change(pcost);
    }
    else if ((m.price > 0L) && (isTopLevel))
    {
      fullcost = MailSendConfirmQuestion.getCostForItem(item, priceMod);
      fullcost += m.price;
      for (int c = 0; c < contained.length; c++)
      {
        pcost = MailSendConfirmQuestion.getCostForItem(contained[c], priceMod);
        fullcost += pcost;
      }
      change = new Change(fullcost);
    }
    String itemName = longItemName(item);
    String sQL = String.format("%.2f", new Object[] { Float.valueOf(item.getQualityLevel()) });
    String sDMG = String.format("%.2f", new Object[] { Float.valueOf(item.getDamage()) });
    String receive = "text=\"\"";
    String ret = "text=\"\"";
    String gold = "";
    String silver = "";
    String copper = "";
    String iron = "";
    String sender = "";
    String expire = "";
    if (isTopLevel)
    {
      receive = "checkbox=\"true\";id=\"" + id + "receive\"";
      if (m.rejected) {
        ret = "text=\"n/a\"";
      } else {
        ret = "checkbox=\"true\";id=\"" + id + "return\"";
      }
      if (change != null)
      {
        gold = "" + change.getGoldCoins();
        silver = "" + change.getSilverCoins();
        copper = "" + change.getCopperCoins();
        iron = "" + change.getIronCoins();
      }
      PlayerState ps = PlayerInfoFactory.getPlayerState(m.getSender());
      sender = ps != null ? ps.getPlayerName() : "Unknown";
      expire = "" + Server.getTimeFor(Math.max(0L, m.expiration - System.currentTimeMillis()));
    }
    String spells = "";
    ItemSpellEffects eff = item.getSpellEffects();
    if (eff != null)
    {
      SpellEffect[] speffs = eff.getEffects();
      for (int z = 0; z < speffs.length; z++)
      {
        if (spells.length() > 0) {
          spells = spells + ",";
        }
        spells = spells + speffs[z].getName() + " [" + (int)speffs[z].power + "]";
      }
    }
    String extra = "";
    if (item.getColor() != -1) {
      extra = " [" + WurmColor.getRGBDescription(item.getColor()) + "]";
    }
    if (item.getTemplateId() == 866) {
      try
      {
        extra = extra + " [" + CreatureTemplateFactory.getInstance().getTemplate(item.getData2()).getName() + "]";
      }
      catch (NoSuchCreatureTemplateException e)
      {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }
    if (spells.length() == 0) {
      spells = "no enchants";
    }
    String hover = itemName + " - " + spells + extra;
    buf.append("row{id=\"" + id + "\";hover=\"" + hover + "\";name=\"" + itemName + "\";rarity=\"" + item
      .getRarity() + "\";children=\"" + children + "\";col{text=\"" + sQL + "\"};col{text=\"" + sDMG + "\"};col{" + receive + "};col{" + ret + "};col{text=\"" + gold + "\"};col{text=\"" + silver + "\"};col{text=\"" + copper + "\"};col{text=\"" + iron + "\"};col{text=\"" + sender + "\"};col{text=\"" + expire
      
      .replace(",", " ") + "\"}}");
    for (int c = 0; c < contained.length; c++) {
      buf.append(addItem(id + "c" + c, contained[c], m, false));
    }
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\MailReceiveQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */