package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.TradeHandler;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MultiPriceManageQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(MultiPriceManageQuestion.class.getName());
  private final Map<Long, Integer> itemMap = new HashMap();
  
  public MultiPriceManageQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 23, aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseMultiPriceQuestion(this);
  }
  
  public Map<Long, Integer> getItemMap()
  {
    return this.itemMap;
  }
  
  public void sendQuestion()
  {
    try
    {
      int idx = 0;
      Creature trader = Server.getInstance().getCreature(this.target);
      if (trader.isNpcTrader())
      {
        Shop shop = Economy.getEconomy().getShop(trader);
        if (shop == null)
        {
          getResponder().getCommunicator().sendNormalServerMessage("No shop registered for that creature.");
        }
        else if (shop.getOwnerId() == getResponder().getWurmId())
        {
          Item[] items = trader.getInventory().getAllItems(false);
          Arrays.sort(items);
          int removed = 0;
          for (int x = 0; x < items.length; x++) {
            if (items[x].isFullprice()) {
              removed++;
            }
          }
          String lHtml = getBmlHeader();
          StringBuilder buf = new StringBuilder(lHtml);
          DecimalFormat df = new DecimalFormat("#.##");
          buf.append("text{text=\"" + trader.getName() + " may put up " + (
            TradeHandler.getMaxNumPersonalItems() - trader.getNumberOfShopItems()) + " more items for sale.\"}");
          
          buf.append("text{type=\"bold\";text=\"Prices for " + trader.getName() + "\"}text{text=''}");
          buf.append("table{rows=\"" + (items.length - removed + 1) + "\"; cols=\"7\";label{text=\"Item name\"};label{text=\"QL\"};label{text=\"DMG\"};label{text=\"Gold\"};label{text=\"Silver\"};label{text=\"Copper\"};label{text=\"Iron\"}");
          for (int x = 0; x < items.length; x++) {
            if (!items[x].isFullprice())
            {
              long wid = items[x].getWurmId();
              idx++;
              Change change = Economy.getEconomy().getChangeFor(items[x].getPrice());
              buf.append(itemNameWithColorByRarity(items[x]));
              buf.append("label{text=\"" + df.format(items[x].getQualityLevel()) + "\"};");
              buf.append("label{text=\"" + df.format(items[x].getDamage()) + "\"};");
              buf.append("harray{input{maxchars=\"3\"; id=\"" + idx + "g\";text=\"" + change.getGoldCoins() + "\"};label{text=\" \"}};");
              buf.append("harray{input{maxchars=\"2\"; id=\"" + idx + "s\";text=\"" + change.getSilverCoins() + "\"};label{text=\" \"}};");
              buf.append("harray{input{maxchars=\"2\"; id=\"" + idx + "c\";text=\"" + change.getCopperCoins() + "\"};label{text=\" \"}};");
              buf.append("harray{input{maxchars=\"2\"; id=\"" + idx + "i\";text=\"" + change.getIronCoins() + "\"};label{text=\" \"}}");
              this.itemMap.put(new Long(wid), Integer.valueOf(idx));
            }
          }
          buf.append("}");
          buf.append(createAnswerButton2());
          getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("You don't own that shop.");
        }
      }
    }
    catch (NoSuchCreatureException nsc)
    {
      getResponder().getCommunicator().sendNormalServerMessage("No such creature.");
      logger.log(Level.WARNING, getResponder().getName(), nsc);
    }
    catch (NoSuchPlayerException nsp)
    {
      getResponder().getCommunicator().sendNormalServerMessage("No such creature.");
      logger.log(Level.WARNING, getResponder().getName(), nsp);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\MultiPriceManageQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */