package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KingdomFoundationQuestion
  extends Question
{
  public static final int playersNeeded = 1;
  private int playersFound = 0;
  private final Set<Creature> creaturesToConvert = new HashSet();
  private static Logger logger = Logger.getLogger(KingdomFoundationQuestion.class.getName());
  
  public KingdomFoundationQuestion(Creature aResponder, long aTarget)
  {
    super(aResponder, "Declaring independence", "Do you wish to found a new kingdom?", 89, aTarget);
  }
  
  public boolean checkMovingPlayers(Creature resp, int x, int y, boolean surfaced)
  {
    VolaTile t = Zones.getTileOrNull(x, y, surfaced);
    if (t != null)
    {
      Creature[] crets = t.getCreatures();
      if (crets.length > 0) {
        for (int c = 0; c < crets.length; c++) {
          if (crets[c].isPlayer())
          {
            if (crets[c].getPower() == 0)
            {
              if (crets[c].isFighting())
              {
                resp.getCommunicator().sendNormalServerMessage(crets[c]
                  .getName() + " was moving or fighting. Everyone has to stand still and honor the moment.");
                
                return false;
              }
              if ((crets[c].getCitizenVillage() != null) && 
                (crets[c].getCitizenVillage().getMayor().wurmId == crets[c].getWurmId())) {
                if (crets[c].getWurmId() != resp.getWurmId())
                {
                  resp.getCommunicator().sendNormalServerMessage(crets[c]
                    .getName() + " is the mayor of another settlement. You have to ask " + crets[c]
                    
                    .getHimHerItString() + " to leave the area and be converted later.");
                  
                  return false;
                }
              }
              if (crets[c].isChampion())
              {
                resp.getCommunicator().sendNormalServerMessage(crets[c]
                  .getName() + " is champion of a deity. You have to ask " + crets[c]
                  
                  .getHimHerItString() + " to leave the area and be converted later.");
                
                return false;
              }
              if (crets[c].isPaying()) {
                this.playersFound += 1;
              }
              this.creaturesToConvert.add(crets[c]);
            }
          }
          else if ((crets[c].isSpiritGuard()) || (crets[c].isKingdomGuard()) || (crets[c].getLoyalty() > 0.0F)) {
            this.creaturesToConvert.add(crets[c]);
          }
        }
      }
    }
    return true;
  }
  
  public void answer(Properties aAnswers)
  {
    setAnswer(aAnswers);
    Creature resp = getResponder();
    if (resp.isChampion())
    {
      resp.getCommunicator().sendAlertServerMessage("Champions are not able to rule kingdoms.");
      return;
    }
    if (!Kingdoms.mayCreateKingdom())
    {
      resp.getCommunicator().sendAlertServerMessage("There are too many kingdoms already.");
      return;
    }
    try
    {
      Item declaration = Items.getItem(this.target);
      if (declaration.deleted)
      {
        resp.getCommunicator().sendAlertServerMessage("The declaration is gone!");
        return;
      }
      if ((declaration.isTraded()) || (declaration.getOwnerId() != resp.getWurmId()))
      {
        resp.getCommunicator().sendAlertServerMessage("The declaration is not under your control any longer!");
        return;
      }
    }
    catch (NoSuchItemException nsi)
    {
      resp.getCommunicator().sendAlertServerMessage("The declaration is gone!");
      return;
    }
    if (resp.getCitizenVillage() == null)
    {
      resp.getCommunicator().sendNormalServerMessage("You need to be mayor of a settlement.");
      return;
    }
    if (resp.getCitizenVillage() != resp.getCurrentVillage())
    {
      resp.getCommunicator().sendNormalServerMessage("You need to be standing in your settlement.");
      return;
    }
    int sx = Zones.safeTileX(resp.getCitizenVillage().getStartX() - resp.getCitizenVillage().getPerimeterSize() - 5);
    
    int ex = Zones.safeTileX(resp.getCitizenVillage().getEndX() + resp.getCitizenVillage().getPerimeterSize() + 5);
    
    int sy = Zones.safeTileY(resp.getCitizenVillage().getStartY() - resp.getCitizenVillage().getPerimeterSize() - 5);
    
    int ey = Zones.safeTileY(resp.getCitizenVillage().getEndY() + resp.getCitizenVillage().getPerimeterSize() + 5);
    int y;
    for (int x = sx; x <= ex; x++) {
      for (y = sy; y <= ey; y++)
      {
        if (!checkMovingPlayers(resp, x, y, true)) {
          return;
        }
        if (!checkMovingPlayers(resp, x, y, false)) {
          return;
        }
      }
    }
    if (resp.getCitizenVillage() != null)
    {
      Player[] players = Players.getInstance().getPlayers();
      for (Player p : players) {
        if (p.getCitizenVillage() == resp.getCitizenVillage())
        {
          if (!this.creaturesToConvert.contains(p)) {
            if (p.isPaying()) {
              this.playersFound += 1;
            }
          }
          this.creaturesToConvert.add(p);
        }
      }
    }
    if ((this.playersFound < 1) && (resp.getPower() < 3))
    {
      getResponder().getCommunicator().sendNormalServerMessage("Only " + this.playersFound + " premium players were found in the village, on deed and in perimeter. You need " + 1 + ".");
      
      return;
    }
    boolean created = false;
    String kingdomName = "";
    String password = "";
    byte templateId = 0;
    String key = "kingdomName";
    String val = aAnswers.getProperty(key);
    if ((val != null) && (val.length() > 0))
    {
      val = val.trim();
      if (val.length() < 2)
      {
        getResponder().getCommunicator().sendNormalServerMessage("The name is too short.");
        return;
      }
      if (val.length() > 20)
      {
        getResponder().getCommunicator().sendNormalServerMessage("The name is too long.");
        return;
      }
      if (QuestionParser.containsIllegalVillageCharacters(val))
      {
        getResponder().getCommunicator().sendNormalServerMessage("The name contains illegal characters.");
        return;
      }
      kingdomName = val;
      
      key = "passw";
      val = aAnswers.getProperty(key);
      if ((val != null) && (val.length() > 0))
      {
        if (val.length() < 5)
        {
          getResponder().getCommunicator().sendNormalServerMessage("The password is too short.");
          return;
        }
        password = val;
      }
      key = "templateid";
      val = aAnswers.getProperty(key);
      if ((val != null) && (val.length() > 0)) {
        if (val.equals("0"))
        {
          templateId = 1;
        }
        else if (val.equals("1"))
        {
          templateId = 2;
        }
        else if (val.equals("2"))
        {
          templateId = 3;
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("Illegal template: " + val);
          return;
        }
      }
      Kingdom k = Kingdoms.getKingdomWithName(kingdomName);
      if (k != null)
      {
        if (k.existsHere())
        {
          King existingRuler = King.getKing(k.getId());
          if (existingRuler != null)
          {
            getResponder().getCommunicator().sendNormalServerMessage("A kingdom with that name already exists in these lands ruled by " + existingRuler.kingName + ".");
            
            return;
          }
          boolean crownExists = false;
          Item[] _items = Items.getAllItems();
          for (Item lItem : _items) {
            if (lItem.isRoyal()) {
              if (lItem.getKingdom() == k.getId()) {
                if ((lItem.getTemplateId() == 536) || 
                  (lItem.getTemplateId() == 530) || 
                  (lItem.getTemplateId() == 533)) {
                  crownExists = true;
                }
              }
            }
          }
          if (crownExists)
          {
            getResponder().getCommunicator().sendNormalServerMessage("A kingdom with that name already exists in these lands. You need to find the crown.");
            
            return;
          }
        }
        if (!k.getPassword().equals(password))
        {
          getResponder().getCommunicator().sendNormalServerMessage("The password you provided was wrong.");
          return;
        }
        if (templateId != k.getTemplate())
        {
          getResponder().getCommunicator().sendNormalServerMessage("You can not use that template for this kingdom since it already exists. Change template");
          
          return;
        }
      }
      String mottoOne = "Friendly";
      String mottoTwo = "Peasants";
      
      val = aAnswers.getProperty("mottoone");
      if ((val != null) && (val.length() > 0))
      {
        val = val.trim();
        if (LoginHandler.containsIllegalCharacters(val))
        {
          getResponder().getCommunicator().sendNormalServerMessage("The first motto contains illegal characters.");
          return;
        }
        mottoOne = val;
      }
      val = aAnswers.getProperty("mottotwo");
      if ((val != null) && (val.length() > 0))
      {
        val = val.trim();
        if (LoginHandler.containsIllegalCharacters(val))
        {
          getResponder().getCommunicator().sendNormalServerMessage("The second motto contains illegal characters.");
          return;
        }
        mottoTwo = val;
      }
      boolean allowPortal = true;
      val = aAnswers.getProperty("allowPortal");
      if ((val != null) && (val.equals("true"))) {
        allowPortal = true;
      } else {
        allowPortal = false;
      }
      String chatName = kingdomName.substring(0, Math.min(11, kingdomName.length()));
      if (Kingdoms.getKingdomWithChatTitle(chatName) != null) {
        if (chatName.contains(" "))
        {
          chatName = kingdomName.replace(" ", "").substring(0, Math.min(11, kingdomName.length()));
          Kingdom kc = Kingdoms.getKingdomWithChatTitle(chatName);
          if (kc != null)
          {
            getResponder().getCommunicator().sendNormalServerMessage("That name is too similar to the kingdom " + kc
              .getName() + ".");
            return;
          }
        }
      }
      String suffix = kingdomName.replace(" ", "").substring(0, Math.min(4, kingdomName.length())) + ".";
      suffix = suffix.toLowerCase();
      if (Kingdoms.getKingdomWithSuffix(suffix) != null)
      {
        suffix = (Server.rand.nextBoolean() ? "z" : "y") + kingdomName.substring(0, Math.min(3, kingdomName.length())) + ".";
        
        suffix = suffix.toLowerCase();
        Kingdom kc = Kingdoms.getKingdomWithSuffix(suffix);
        if (kc != null)
        {
          getResponder().getCommunicator().sendNormalServerMessage("That name is too similar to the kingdom " + kc
            .getName() + ".");
          return;
        }
      }
      int allnum = resp.getCitizenVillage().getAllianceNumber();
      String aname = resp.getCitizenVillage().getAllianceName();
      if (allnum > 0) {
        if (allnum == resp.getCitizenVillage().getAllianceNumber())
        {
          PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(allnum);
          for (Village v : pvpAll.getVillages())
          {
            v.setAllianceNumber(0);
            v.broadCastAlert(aname + " alliance has been disbanded.");
          }
          if (pvpAll.exists())
          {
            pvpAll.delete();
            pvpAll.sendClearAllianceAnnotations();
            pvpAll.deleteAllianceMapAnnotations();
          }
        }
        else
        {
          PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(allnum);
          resp.getCitizenVillage().broadCastAlert(resp
            .getCitizenVillage().getName() + " leaves the " + aname + " alliance.");
          resp.getCitizenVillage().setAllianceNumber(0);
          if (!pvpAll.exists()) {
            pvpAll.delete();
          }
        }
      }
      Kingdom newkingdom = new Kingdom(Kingdoms.getNextAvailableKingdomId(), templateId, kingdomName, password, chatName, suffix, mottoOne, mottoTwo, allowPortal);
      
      Kingdoms.addKingdom(newkingdom);
      
      LoginServerWebConnection lsw = new LoginServerWebConnection();
      lsw.setKingdomInfo(newkingdom.getId(), templateId, kingdomName, password, chatName, suffix, mottoOne, mottoTwo, allowPortal);
      
      lsw.kingdomExists(Servers.localServer.id, newkingdom.getId(), true);
      try
      {
        getResponder().setKingdomId(newkingdom.getId(), true);
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, getResponder().getName() + ": " + ((IOException)iox).getMessage(), (Throwable)iox);
      }
      try
      {
        resp.getCitizenVillage().setKingdom(newkingdom.getId());
        resp.getCitizenVillage().setKingdomInfluence();
        for (iox = this.creaturesToConvert.iterator(); ((Iterator)iox).hasNext();)
        {
          Creature c = (Creature)((Iterator)iox).next();
          try
          {
            c.setKingdomId(newkingdom.getId(), true);
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, iox.getMessage(), iox);
          }
        }
        this.creaturesToConvert.clear();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, iox.getMessage(), iox);
      }
      Kingdoms.convertTowersWithin(sx, sy, ex, ey, newkingdom.getId());
      created = true;
      King king = King.createKing(newkingdom.getId(), resp.getName(), resp.getWurmId(), resp.getSex());
      king.setCapital(resp.getCitizenVillage().getName(), true);
      Methods.rewardRegalia(resp);
      NewKingQuestion nk = new NewKingQuestion(resp, "New ruler!", "Congratulations!", resp.getWurmId());
      
      nk.sendQuestion();
      Items.destroyItem(this.target);
      try
      {
        Item contract = ItemFactory.createItem(299, 50.0F + Server.rand.nextFloat() * 50.0F, 
          getResponder().getName());
        getResponder().getInventory().insertItem(contract);
      }
      catch (Exception ex2)
      {
        logger.log(Level.INFO, ex2.getMessage(), ex2);
      }
    }
    if (!created) {
      getResponder().getCommunicator().sendNormalServerMessage("You decide to do nothing.");
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    if (!Servers.localServer.PVPSERVER)
    {
      buf.append("text{text=\"You may not use this here.\"}");
    }
    else if (getResponder().isKing())
    {
      buf.append("text{text=\"What a foolish idea. You are already the king! Imagine the laughter if your loyal subjects knew!\"}");
    }
    else
    {
      if (getResponder().getCitizenVillage() != null)
      {
        Player[] players = Players.getInstance().getPlayers();
        for (Player p : players) {
          if (p.getCitizenVillage() == getResponder().getCitizenVillage()) {
            p.getCommunicator().sendAlertServerMessage(
              getResponder().getName() + " may be forming a new kingdom with your village in it! Make sure you are outside of enemy areas.");
          }
        }
      }
      buf.append("text{text=\"You are ready to declare your independence from " + 
        Kingdoms.getNameFor(getResponder().getKingdomId()) + "!\"}");
      buf.append("text{text=\"\"}");
      buf.append("text{text=\"In order to succeed with this, you need to be mayor of and stand in your future capital.\"}");
      
      buf.append("text{text=\"Any alliances you have with other village will be disbanded.\"}");
      
      buf.append("text{text=\"Everyone has to stand still to honor this event.\"}");
      buf.append("text{text=\"\"}");
      buf.append("text{text=\"In case the name of the kingdom exists on other servers, you have to provide the password for that kingdom.\"}");
      buf.append("harray{label{text='Name your new kingdom: '};input{id='kingdomName'; text='';maxchars='20'}}");
      buf.append("text{text=\"\"}");
      buf.append("harray{label{text='Provide a password for multiple servers (min 6 letters): '};input{id='passw'; text='';maxchars='10'}}");
      buf.append("text{text=\"\"}");
      buf.append("text{text=\"You have to select the kingdom that will serve as your example when it comes to special titles, combat moves, creatures and deities.\"}");
      buf.append("text{text=\"If your new kingdom is dissolved for any reason, any remaining people will revert to this template kingdom.\"}");
      buf.append("text{text=\"Note that the king must stay premium at all time.\"}");
      buf.append("harray{label{text='Template kingdom: '};dropdown{id='templateid';options=\"Jenn-Kellon,Mol Rehan,Horde of the Summoned\"}}");
      
      buf.append("text{text=\"\"}");
      buf.append("checkbox{id='allowPortal';text='Allow people to join the kingdom via portals?';selected=\"true\"}");
      buf.append("text{text=\"Finally, provide two words that you think will describe your kingdom. You can change these later.\"}");
      buf.append("harray{input{id='mottoone'; maxchars='10'; text=\"Friendly\"}label{text=\" Description one\"}}");
      buf.append("harray{input{id='mottotwo'; maxchars='10'; text=\"Peasants\"}label{text=\" Description two\"}}");
      buf.append("text{text=\"\"}");
    }
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(700, 530, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\KingdomFoundationQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */