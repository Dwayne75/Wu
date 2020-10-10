package com.wurmonline.server.questions;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReferralQuestion
  extends Question
  implements MonetaryConstants
{
  private static final Logger logger = Logger.getLogger(ReferralQuestion.class.getName());
  private Map<String, Byte> referrals;
  
  public ReferralQuestion(Creature aResponder, long aTarget)
  {
    super(aResponder, "Referrals", "These are your referrals:", 46, aTarget);
  }
  
  public void addReferrer(String receiver)
  {
    try
    {
      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(receiver);
      try
      {
        pinf.load();
      }
      catch (IOException iox)
      {
        getResponder().getCommunicator().sendNormalServerMessage(receiver + " - no such player exists. Please check the spelling.");
        
        return;
      }
      if (pinf.wurmId == getResponder().getWurmId())
      {
        getResponder().getCommunicator().sendNormalServerMessage("You may not refer yourself.");
        return;
      }
      if (pinf.getPaymentExpire() <= 0L)
      {
        getResponder().getCommunicator().sendNormalServerMessage(pinf
          .getName() + " has never had a premium account and may not receive referrals.");
        return;
      }
      if (PlayerInfoFactory.addReferrer(pinf.wurmId, getResponder().getWurmId()))
      {
        ((Player)getResponder()).getSaveFile().setReferedby(pinf.wurmId);
        getResponder().getCommunicator().sendNormalServerMessage("Okay, you have set " + receiver + " as your referrer.");
        
        return;
      }
      getResponder().getCommunicator().sendNormalServerMessage("You have already awarded referral to that player.");
    }
    catch (Exception e)
    {
      logger.log(Level.WARNING, e.getMessage() + " " + receiver + " from " + getResponder().getName(), e);
      getResponder().getCommunicator()
        .sendNormalServerMessage("An error occurred. Please write a bug report about this.");
    }
  }
  
  public void acceptReferrer(long wurmid, String awarderName, boolean money)
  {
    String name = awarderName;
    PlayerInfo pinf = null;
    try
    {
      long l = Long.parseLong(awarderName);
      pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(l);
    }
    catch (NumberFormatException nfe)
    {
      pinf = PlayerInfoFactory.createPlayerInfo(name);
      try
      {
        pinf.load();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage());
        getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the player " + awarderName + " in the database.");
        
        return;
      }
    }
    if (pinf != null) {
      try
      {
        if (PlayerInfoFactory.acceptReferer(wurmid, pinf.wurmId, money)) {
          try
          {
            if (money) {
              PlayerInfoFactory.addMoneyToBank(wurmid, 30000L, "Refby " + pinf.getName());
            } else {
              PlayerInfoFactory.addPlayingTime(wurmid, 0, 20, "Refby " + pinf.getName());
            }
            getResponder().getCommunicator().sendNormalServerMessage("Okay, accepted the referral from " + awarderName + ". The reward will arrive soon if it has not already.");
          }
          catch (Exception ex)
          {
            logger.log(Level.WARNING, "An error occurred wurmid: " + wurmid + ", awarderName: " + awarderName + ", money: " + money + " - " + ex
              .getMessage(), ex);
            PlayerInfoFactory.revertReferer(wurmid, pinf.wurmId);
            getResponder().getCommunicator().sendNormalServerMessage("An error occured. Please try later or post a bug report.");
          }
        } else {
          getResponder().getCommunicator().sendNormalServerMessage("Failed to match " + awarderName + " to any existing referral.");
        }
      }
      catch (Exception ex)
      {
        getResponder().getCommunicator().sendNormalServerMessage("An error occured. Please try later or post a bug report.");
        
        logger.log(Level.WARNING, "An error occurred wurmid: " + wurmid + ", awarderName: " + awarderName + ", money: " + money + " - " + ex
          .getMessage(), ex);
      }
    } else {
      getResponder().getCommunicator().sendNormalServerMessage("Failed to locate " + awarderName + " in the database.");
    }
  }
  
  public void answer(Properties answers)
  {
    LoginServerWebConnection lsw;
    Iterator<Map.Entry<String, Byte>> it;
    if ((((Player)getResponder()).getPaymentExpire() > 0L) && (!getResponder().hasFlag(63)))
    {
      lsw = null;
      String referrer = answers.getProperty("awarder");
      if ((referrer != null) && (referrer.length() > 0))
      {
        referrer = LoginHandler.raiseFirstLetter(referrer);
        if (((Player)getResponder()).getSaveFile().referrer <= 0L)
        {
          if (referrer.length() > 2)
          {
            if (Servers.isRealLoginServer())
            {
              addReferrer(referrer);
            }
            else
            {
              lsw = new LoginServerWebConnection();
              lsw.addReferrer((Player)getResponder(), referrer);
            }
          }
          else {
            getResponder().getCommunicator().sendNormalServerMessage("The name " + referrer + " is too short.");
          }
        }
        else {
          getResponder().getCommunicator().sendNormalServerMessage("Our records tell us that you have already referred someone.");
        }
      }
      if (this.referrals.size() > 0) {
        for (it = this.referrals.entrySet().iterator(); it.hasNext();)
        {
          Map.Entry<String, Byte> entry = (Map.Entry)it.next();
          String name = (String)entry.getKey();
          byte referralType = ((Byte)entry.getValue()).byteValue();
          if (referralType == 0)
          {
            String a = answers.getProperty(name + "group");
            if (a != null) {
              if (a.equals(name + "silver"))
              {
                if (Servers.isRealLoginServer())
                {
                  acceptReferrer(getResponder().getWurmId(), name, true);
                }
                else
                {
                  if (lsw == null) {
                    lsw = new LoginServerWebConnection();
                  }
                  lsw.acceptReferrer(getResponder(), name, true);
                }
              }
              else if (a.equals(name + "time")) {
                if (Servers.isRealLoginServer())
                {
                  acceptReferrer(getResponder().getWurmId(), name, false);
                }
                else
                {
                  if (lsw == null) {
                    lsw = new LoginServerWebConnection();
                  }
                  lsw.acceptReferrer(getResponder(), name, false);
                }
              }
            }
          }
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    ((Player)getResponder()).lastReferralQuestion = System.currentTimeMillis();
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    if (Servers.isRealLoginServer()) {
      this.referrals = PlayerInfoFactory.getReferrers(this.target);
    } else {
      this.referrals = new LoginServerWebConnection().getReferrers(getResponder(), this.target);
    }
    if (this.referrals == null)
    {
      buf.append("text{text='An error occurred. Please try later.'}");
    }
    else
    {
      if ((((Player)getResponder()).getPaymentExpire() > 0L) && (!getResponder().hasFlag(63)))
      {
        if (((Player)getResponder()).getSaveFile().referrer <= 0L)
        {
          buf.append("text{text='You may reward a player for directing you to Wurm Online.'}");
          buf.append("text{text='That player must have or have had a premium account once.'}");
          buf.append("text{text='You may only do this once, and you may not refer to yourself.'}");
          
          buf.append("text{text='The player you type in the box will see that you rewarded him, and may opt to receive 20 days premium playing time or the amount of 3 silver coins in his or her bank account, plus an hour of skillgain speed bonus.'}");
          buf.append("text{text='This reward is therefor valuable, and should not be given away unless you have given it proper consideration.'}");
          buf.append("text{text='You may currently not change referrer once you select one, even if he/she does not collect the reward.'}");
          buf.append("harray{label{text='Who do you wish to award?'};input{id='awarder'; text='';maxchars='40'}}");
        }
        else
        {
          buf.append("text{text='You have already used up your referral award.'};text{text=''}");
        }
      }
      else {
        buf.append("text{text='You are not playing a premier account, so you can not refer to anyone.'};text{text=''}");
      }
      Iterator<Map.Entry<String, Byte>> it;
      if (this.referrals.size() > 0)
      {
        buf.append("text{type='bold';text='Here are your referrals:'}");
        for (it = this.referrals.entrySet().iterator(); it.hasNext();)
        {
          Map.Entry<String, Byte> entry = (Map.Entry)it.next();
          String name = (String)entry.getKey();
          byte referralType = ((Byte)entry.getValue()).byteValue();
          if (referralType == 0) {
            buf.append("harray{label{text=\"" + name + "\"};radio{id=\"" + name + "none\";text='Decide later';group=\"" + name + "group\";selected='true'};radio{id=\"" + name + "silver\";text='3 silver';group=\"" + name + "group\"};radio{id=\"" + name + "time\";text='20 days and 1 hour sleep bonus';group=\"" + name + "group\"}}");
          } else if (referralType == 1) {
            buf.append("harray{label{text=\"" + name + "\"};label{text='   3 silver'}}");
          } else {
            buf.append("harray{label{text=\"" + name + "\"};label{text='   20 days'}}");
          }
        }
      }
      else
      {
        buf.append("text{text='Nobody has referred to you, so you have no award to collect right now.'}");
      }
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\ReferralQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */