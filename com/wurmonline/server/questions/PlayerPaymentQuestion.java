package com.wurmonline.server.questions;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerPaymentQuestion
  extends Question
  implements MonetaryConstants
{
  public static final long silverCost = 10L;
  public static final long silverCostFirstTime = 2L;
  public static final long silverCost15Day = 5L;
  
  public PlayerPaymentQuestion(Creature aResponder)
  {
    super(aResponder, "Purchase Premium Time", "Choose an option from the below:", 20, aResponder.getWurmId());
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    
    long money = getResponder().getMoney();
    if ((((Player)getResponder()).getSaveFile().getPaymentExpire() <= 0L) || (getResponder().hasFlag(63)))
    {
      if (money < 20000L)
      {
        getResponder().getCommunicator().sendNormalServerMessage("You need at least 2 silver in your account to purchase premium game time.");
      }
      else
      {
        boolean purchaseFirstTime = Boolean.parseBoolean(answers.getProperty("purchaseFirstTime"));
        long referredBy = ((Player)getResponder()).getSaveFile().referrer;
        if ((purchaseFirstTime) && (referredBy == 0L)) {
          try
          {
            if (getResponder().chargeMoney(20000L))
            {
              LoginServerWebConnection lsw = new LoginServerWebConnection();
              
              lsw.addPlayingTime(getResponder(), getResponder().getName(), 0, 30, "firstBuy" + (
                System.currentTimeMillis() - 1400000000000L) + Servers.localServer.name);
              getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time is being processed. It may take up to half an hour until the system is fully updated.");
              
              ((Player)getResponder()).getSaveFile().setReferedby(getResponder().getWurmId());
              
              getResponder().setFlag(63, false);
            }
            else
            {
              getResponder().getCommunicator().sendAlertServerMessage("Failed to charge you 2 silvers. Please try later.");
            }
          }
          catch (IOException ex)
          {
            getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time could not be processed.");
          }
        } else if ((purchaseFirstTime) && (referredBy != 0L)) {
          getResponder().getCommunicator().sendNormalServerMessage("You have already purchased this option once, if you still have not received your play time after 30 minutes, please contact /support.");
        } else {
          getResponder().getCommunicator().sendNormalServerMessage("You decide not to buy any premium game time for now.");
        }
      }
    }
    else
    {
      String purchaseStr = answers.getProperty("purchase");
      if ("30day".equals(purchaseStr))
      {
        if (money >= 100000L) {
          try
          {
            if (getResponder().chargeMoney(100000L))
            {
              LoginServerWebConnection lsw = new LoginServerWebConnection();
              lsw.addPlayingTime(getResponder(), getResponder().getName(), 0, 30, System.currentTimeMillis() + Servers.localServer.name);
              getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time is being processed. It may take up to half an hour until the system is fully updated.");
              
              Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + 30000L);
              logger.log(Level.INFO, getResponder().getName() + " purchased 1 month premium time for " + 10L + " silver coins. " + 30000L + " iron added to king.");
            }
            else
            {
              getResponder().getCommunicator().sendAlertServerMessage("Failed to charge you 10 silvers. Please try later.");
            }
          }
          catch (IOException ex)
          {
            getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time could not be processed.");
          }
        } else {
          getResponder().getCommunicator().sendNormalServerMessage("You need at least 10 silver in your account to purchase 30 days of premium game time.");
        }
      }
      else if ("15day".equals(purchaseStr))
      {
        if (money >= 50000L) {
          try
          {
            if (getResponder().chargeMoney(50000L))
            {
              LoginServerWebConnection lsw = new LoginServerWebConnection();
              lsw.addPlayingTime(getResponder(), getResponder().getName(), 0, 15, System.currentTimeMillis() + Servers.localServer.name);
              getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time is being processed. It may take up to half an hour until the system is fully updated.");
              
              Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + -20000L);
              logger.log(Level.INFO, getResponder().getName() + " purchased 1 month premium time for " + 5L + " silver coins. " + -20000L + " iron added to king.");
            }
            else
            {
              getResponder().getCommunicator().sendAlertServerMessage("Failed to charge you 10 silvers. Please try later.");
            }
          }
          catch (IOException ex)
          {
            getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time could not be processed.");
          }
        } else {
          getResponder().getCommunicator().sendNormalServerMessage("You need at least 5 silver in your account to purchase 15 days of premium game time.");
        }
      }
      else {
        getResponder().getCommunicator().sendNormalServerMessage("You decide not to buy any premium game time for now.");
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    long money = getResponder().getMoney();
    Change change = Economy.getEconomy().getChangeFor(money);
    buf.append(getBmlHeader());
    if (Features.Feature.RETURNER_PACK_REGISTRATION.isEnabled()) {
      if (getResponder().hasFlag(47)) {
        buf.append("text{text='You are successfully registered for the returner pack!'}");
      }
    }
    if ((((Player)getResponder()).getSaveFile().getPaymentExpire() <= 0L) || (getResponder().hasFlag(63)))
    {
      if (money < 20000L)
      {
        buf.append("text{text='As this is your first time purchasing premium game time, you will need at least 2 silver in your bank account.'}");
        
        buf.append("text{text=''}");
        buf.append("text{text='You currently only have " + change.getChangeString() + " in your account.'}");
      }
      else
      {
        buf.append("text{text='As this is your first time purchasing premium game time, you may purchase 30 days for 2 silver. After this first time the price will become 5 silver for 15 days, or 10 silver for 30 days.'}");
        
        buf.append("text{text=''}");
        buf.append("text{text='You currently have " + change.getChangeString() + " in your account.'}");
        buf.append("text{text=''}");
        buf.append("checkbox{id='purchaseFirstTime'; selected='true'; text='Purchase 30 days of premium playing time for 2 silver.'}");
      }
    }
    else if (money < 50000L)
    {
      buf.append("text{text='To purchase more premium game time you will need at least 5 silver in your bank account.'}");
      buf.append("text{text=''}");
      buf.append("text{text='You currently only have " + change.getChangeString() + " in your account.'}");
    }
    else
    {
      buf.append("text{text='You may purchase another 30 days of premium playing time for 10 silver, or 15 days of premium playing time for 5 silver.'}");
      
      buf.append("text{text=''}");
      buf.append("text{text='You currently have " + change.getChangeString() + " in your account.'}");
      buf.append("text{text=''}");
      buf.append("label{text=\"Purchase Premium Time?\"};");
      if (money >= 100000L) {
        buf.append("radio{group='purchase';id='30day';selected='false';text='30 days for 10 silver'};");
      }
      buf.append("radio{group='purchase';id='15day';selected='false';text='15 days for 5 silver'};");
      buf.append("radio{group='purchase';id='none';selected='true';text='Nothing'};");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(400, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\PlayerPaymentQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */