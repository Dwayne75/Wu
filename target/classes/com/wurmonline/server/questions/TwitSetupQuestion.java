package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitSetupQuestion
  extends Question
{
  private static Logger logger = Logger.getLogger(TwitSetupQuestion.class.getName());
  private final boolean isVillage;
  
  public TwitSetupQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, boolean village)
  {
    super(aResponder, aTitle, aQuestion, 90, aTarget);
    this.isVillage = village;
  }
  
  public void answer(Properties aAnswers)
  {
    String _consumerKeyToUse = "";
    String _consumerSecretToUse = "";
    String _applicationToken = "";
    String _applicationSecret = "";
    String key = "consumerKeyToUse";
    boolean twitChat = false;
    String val = aAnswers.getProperty(key);
    if (val != null) {
      _consumerKeyToUse = val.trim();
    }
    key = "consumerSecretToUse";
    val = aAnswers.getProperty(key);
    if (val != null) {
      _consumerSecretToUse = val.trim();
    }
    key = "applicationToken";
    val = aAnswers.getProperty(key);
    if (val != null) {
      _applicationToken = val.trim();
    }
    key = "applicationSecret";
    val = aAnswers.getProperty(key);
    if (val != null) {
      _applicationSecret = val.trim();
    }
    key = "twitChat";
    val = aAnswers.getProperty(key);
    if (val != null) {
      twitChat = val.equals("true");
    }
    boolean champtwits = false;
    key = "champtwit";
    val = aAnswers.getProperty(key);
    if (val != null) {
      champtwits = val.equals("true");
    }
    if (this.isVillage)
    {
      try
      {
        Village village;
        if (this.target == -10L)
        {
          Village village = getResponder().getCitizenVillage();
          if (village == null) {
            throw new NoSuchVillageException("You are not a citizen of any village (on this server).");
          }
        }
        else
        {
          village = Villages.getVillage((int)this.target);
        }
        boolean twitEnabled = village.isTwitEnabled();
        if (getResponder().getPower() > 0)
        {
          key = "twitEnabled";
          val = aAnswers.getProperty(key);
          if (val != null) {
            twitEnabled = val.equals("true");
          }
          getResponder().getLogger().log(Level.INFO, "Setting " + village
            .getName() + " twitter enable to " + twitEnabled + ".");
        }
        if ((_consumerKeyToUse == null) || (_consumerSecretToUse == null) || (_applicationToken == null) || (_applicationSecret == null))
        {
          logger.info(getResponder() + " has cleared the Twitter credentials for Settlement: " + village);
          village.setTwitCredentials("", "", "", "", false, twitEnabled);
          getResponder().getCommunicator().sendNormalServerMessage("No twitting will occur now.");
        }
        else
        {
          village.setTwitCredentials(_consumerKeyToUse, _consumerSecretToUse, _applicationToken, _applicationSecret, twitChat, twitEnabled);
          if (village.canTwit())
          {
            logger.info(getResponder() + " has set the Twitter credentials for Settlement: " + village);
            getResponder().getCommunicator().sendNormalServerMessage("Allright, twit away!");
          }
          else
          {
            logger.info(getResponder() + " has set invalid Twitter credentials for Settlement: " + village + ", so Twitter is now disabled.");
            
            getResponder().getCommunicator().sendNormalServerMessage("You won't be twittin' with those keys.");
          }
        }
      }
      catch (NoSuchVillageException nsv)
      {
        getResponder().getCommunicator().sendAlertServerMessage("No such settlement.");
      }
    }
    else if (getResponder().getPower() >= 3)
    {
      if ((_consumerKeyToUse == null) || (_consumerSecretToUse == null) || (_applicationToken == null) || (_applicationSecret == null))
      {
        logger.info(getResponder() + " has cleared the Twitter credentials for this server, whose ID is: " + Servers.localServer.id);
        if (champtwits) {
          Servers.localServer.setChampTwitter("", "", "", "");
        } else {
          Servers.setTwitCredentials(Servers.localServer.id, "", "", "", "");
        }
        getResponder().getCommunicator().sendNormalServerMessage("No twitting will occur now.");
      }
      else if (champtwits)
      {
        Servers.localServer.setChampTwitter(_consumerKeyToUse, _consumerSecretToUse, _applicationToken, _applicationSecret);
        
        Servers.localServer.canTwit();
        if (Servers.localServer.canTwitChamps)
        {
          logger.info(getResponder() + " has set the Champion Twitter credentials for this server, whose ID is: " + Servers.localServer.id);
          
          getResponder().getCommunicator().sendNormalServerMessage("Allright, twit away!");
        }
        else
        {
          logger.info(getResponder() + " has set invalid Champion Twitter credentials for this server, whose ID is: " + Servers.localServer.id + ", so Champion Twitter is now disabled.");
          
          getResponder().getCommunicator().sendNormalServerMessage("You won't be twittin' with those keys.");
        }
      }
      else
      {
        Servers.setTwitCredentials(Servers.localServer.id, _consumerKeyToUse, _consumerSecretToUse, _applicationToken, _applicationSecret);
        if (Servers.localServer.canTwit())
        {
          logger.info(getResponder() + " has set the Twitter credentials for this server, whose ID is: " + Servers.localServer.id);
          
          getResponder().getCommunicator().sendNormalServerMessage("Allright, twit away!");
        }
        else
        {
          logger.info(getResponder() + " has set invalid Twitter credentials for this server, whose ID is: " + Servers.localServer.id + ", so Twitter is now disabled.");
          
          getResponder().getCommunicator().sendNormalServerMessage("You won't be twittin' with those keys.");
        }
      }
    }
    else
    {
      logger.info(getResponder() + " tried to set the Twitter credentials but was not allowed.");
      getResponder().getCommunicator().sendAlertServerMessage("You are not allowed to edit this information.");
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    String _consumerKeyToUse = "";
    String _consumerSecretToUse = "";
    String _applicationToken = "";
    String _applicationSecret = "";
    boolean twitChat = false;
    boolean enabled = true;
    String vilname = "";
    if (this.isVillage)
    {
      try
      {
        Village village;
        if (this.target == -10L)
        {
          Village village = getResponder().getCitizenVillage();
          if (village == null) {
            throw new NoSuchVillageException("You are not a citizen of any village (on this server).");
          }
        }
        else
        {
          village = Villages.getVillage((int)this.target);
        }
        _consumerKeyToUse = village.getConsumerKey();
        _consumerSecretToUse = village.getConsumerSecret();
        _applicationToken = village.getApplicationToken();
        _applicationSecret = village.getApplicationSecret();
        twitChat = village.twitChat();
        enabled = village.isTwitEnabled();
        vilname = village.getName();
      }
      catch (NoSuchVillageException nsv)
      {
        buf.append("text{text=\"settlement not found.\"};");
        buf.append("text{text=\"\"};");
      }
    }
    else if (getResponder().getPower() >= 3)
    {
      _consumerKeyToUse = Servers.localServer.getConsumerKey();
      _consumerSecretToUse = Servers.localServer.getConsumerSecret();
      _applicationToken = Servers.localServer.getApplicationToken();
      _applicationSecret = Servers.localServer.getApplicationSecret();
    }
    buf.append("text{text=\"In order to use this functionality you need to perform the following steps:\"};");
    buf.append("text{text=\"1. You need to create a twitter account.\"}");
    buf.append("text{text=\"2. You need to register an application (use the developers link down to the right in twitter).\"}");
    buf.append("text{text=\"3. Insert the consumer key and secret, as well as the access token information for the application here.\"}");
    buf.append("harray{label{text=\"Consumer key \"};input{id=\"consumerKeyToUse\"; maxchars=\"70\"; text=\"" + _consumerKeyToUse + "\"}}");
    
    buf.append("harray{label{text=\"Consumer secret \"};input{id=\"consumerSecretToUse\"; maxchars=\"70\"; text=\"" + _consumerSecretToUse + "\"}}");
    
    buf.append("harray{label{text=\"Application key (oauth token) \"}input{id=\"applicationToken\"; maxchars=\"70\"; text=\"" + _applicationToken + "\"}}");
    
    buf.append("harray{label{text=\"Application secret (oauth token secret) \"}input{id=\"applicationSecret\"; maxchars=\"70\"; text=\"" + _applicationSecret + "\"}}");
    if (this.isVillage)
    {
      buf.append("checkbox{id=\"twitChat\";text=\"Twit all settlement chat? \";selected=\"" + twitChat + "\"};");
      if (getResponder().getPower() > 0)
      {
        getResponder().getLogger().log(Level.INFO, "Editing " + vilname + " twitter settings.");
        buf.append("checkbox{id=\"twitEnabled\";text=\"Enable twit? \";selected=\"" + enabled + "\"};");
      }
    }
    else
    {
      buf.append("checkbox{id=\"champtwit\";text=\"Setting for Champion tweets? \";selected=\"false\"};");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\TwitSetupQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */