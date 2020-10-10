package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.players.Titles.Title;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PortalQuestion
  extends Question
  implements MonetaryConstants, TimeConstants
{
  private final Item portal;
  private static final Logger logger = Logger.getLogger(PortalQuestion.class.getName());
  private static final int maxItems = 200;
  private static final int standardBodyInventoryItems = 12;
  public static final int PORTAL_FREEDOM_ID = 100000;
  public static final int PORTAL_EPIC_ID = 100001;
  public static final int PORTAL_CHALLENGE_ID = 100002;
  public static final boolean allowPortalToLatestServer = true;
  private int step = 0;
  private int selectedServer = 100000;
  private byte selectedKingdom = 0;
  private int selectedSpawn = -1;
  public static boolean epicPortalsEnabled = true;
  private String cyan = "66,200,200";
  private String green = "66,225,66";
  private String orange = "255,156,66";
  private String purple = "166,166,66";
  private String red = "255,66,66";
  
  public PortalQuestion(Creature aResponder, String aTitle, String aQuestion, Item _portal)
  {
    super(aResponder, aTitle, aQuestion, 76, _portal.getWurmId());
    this.portal = _portal;
  }
  
  public void answer(Properties aAnswers)
  {
    String val = aAnswers.getProperty("portalling");
    
    getResponder().sendToLoggers(" at A: " + val + " selectedServer=" + this.selectedServer);
    int numitems;
    int stayBehind;
    Item[] inventoryItems;
    if ((val != null) && (val.equals("true")))
    {
      if (this.portal != null)
      {
        byte targetKingdom = 0;
        int data1 = this.portal.getData1();
        if (this.step == 1) {
          data1 = this.selectedServer;
        }
        getResponder().sendToLoggers(" at A: " + val + " selectedServer=" + data1);
        ServerEntry entry = Servers.getServerWithId(data1);
        if (entry != null)
        {
          getResponder().sendToLoggers(" at 1: " + data1);
          if (entry.id == Servers.loginServer.id) {
            entry = Servers.loginServer;
          }
          boolean changingCluster = false;
          boolean newTutorial = this.portal.getTemplateId() == 855;
          if ((Servers.localServer.EPIC != entry.EPIC) && (!newTutorial))
          {
            changingCluster = true;
            if (!this.portal.isEpicPortal())
            {
              getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. This is not an epic portal.");
              
              return;
            }
            if ((!epicPortalsEnabled) && (getResponder().getPower() == 0)) {
              getResponder().getCommunicator().sendNormalServerMessage("The portal won't let you just yet.");
            }
          }
          else if ((Servers.localServer.EPIC) && (getResponder().isChampion()))
          {
            if (!this.portal.isEpicPortal())
            {
              getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You could not use this portal since you are a champion.");
              
              return;
            }
          }
          if (getResponder().getEnemyPresense() > 0)
          {
            getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You sense a disturbance.");
            
            return;
          }
          if (getResponder().hasBeenAttackedWithin(300))
          {
            getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You sense a disturbance - maybe your are not calm enough yet.");
            
            return;
          }
          if (Servers.localServer.isChallengeServer()) {
            changingCluster = true;
          }
          if (Servers.localServer.entryServer) {
            changingCluster = false;
          }
          if (changingCluster) {
            if ((getResponder().isChampion()) && (!Servers.localServer.EPIC)) {
              if (!this.portal.isEpicPortal())
              {
                getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. You could not use this portal since you are a champion.");
                
                return;
              }
            }
          }
          if ((getResponder().getPower() == 0) && (entry.entryServer) && (!Servers.localServer.testServer))
          {
            getResponder().getCommunicator().sendNormalServerMessage("Nothing happens.");
            return;
          }
          if (this.portal.isEpicPortal())
          {
            if ((!changingCluster) && (!Servers.localServer.entryServer))
            {
              getResponder().getCommunicator().sendNormalServerMessage("Nothing happens. Actually this shouldn't be possible.");
              
              return;
            }
            long time = System.currentTimeMillis() - ((Player)getResponder()).getSaveFile().lastUsedEpicPortal;
            if (getResponder().getEpicServerKingdom() == 0)
            {
              String kingdomid = "kingdid";
              String kval = aAnswers.getProperty("kingdid");
              if (kval != null) {
                try
                {
                  targetKingdom = Byte.parseByte(kval);
                }
                catch (NumberFormatException nfe)
                {
                  logger.log(Level.WARNING, "Failed to parse " + kval + " to a valid byte.");
                  getResponder().getCommunicator().sendAlertServerMessage("An error occured with the target kingdom. You can't select that kingdom.");
                  
                  return;
                }
              }
            }
            else
            {
              targetKingdom = getResponder().getEpicServerKingdom();
              if (Servers.isThisAChaosServer()) {
                logger.log(Level.INFO, getResponder().getName() + " joining " + targetKingdom);
              }
            }
            ((Player)getResponder()).getSaveFile().setEpicLocation(targetKingdom, entry.id);
            getResponder().setRotation(270.0F);
            int targetTileX = entry.SPAWNPOINTJENNX;
            int targetTileY = entry.SPAWNPOINTJENNY;
            if (targetKingdom == 2)
            {
              getResponder().setRotation(90.0F);
              targetTileX = entry.SPAWNPOINTMOLX;
              targetTileY = entry.SPAWNPOINTMOLY;
            }
            else if (targetKingdom == 3)
            {
              getResponder().setRotation(1.0F);
              targetTileX = entry.SPAWNPOINTLIBX;
              targetTileY = entry.SPAWNPOINTLIBY;
            }
            if (Servers.localServer.entryServer) {
              if (getResponder().isPlayer()) {
                ((Player)getResponder()).addTitle(Titles.Title.Educated);
              }
            }
            if (getResponder().isPlayer()) {
              ((Player)getResponder()).getSaveFile().setBed(this.portal.getWurmId());
            }
            getResponder().sendTransfer(Server.getInstance(), entry.INTRASERVERADDRESS, 
              Integer.parseInt(entry.INTRASERVERPORT), entry.INTRASERVERPASSWORD, entry.id, targetTileX, targetTileY, true, 
              getResponder().getPower() <= 0, targetKingdom);
            
            return;
          }
          if (entry.HOMESERVER)
          {
            if (entry.KINGDOM != 0) {
              targetKingdom = entry.KINGDOM;
            } else {
              targetKingdom = this.selectedKingdom == 0 ? getResponder().getKingdomId() : this.selectedKingdom;
            }
          }
          else
          {
            String kingdomid = "kingdid";
            String kval = aAnswers.getProperty("kingdid");
            if (kval != null) {
              try
              {
                targetKingdom = Byte.parseByte(kval);
                getResponder().sendToLoggers(" at kingdid: " + entry
                  .getName() + " selected kingdom " + targetKingdom);
              }
              catch (NumberFormatException nfe)
              {
                targetKingdom = getResponder().getKingdomId();
              }
            } else {
              targetKingdom = this.selectedKingdom == 0 ? getResponder().getKingdomId() : this.selectedKingdom;
            }
          }
          getResponder().sendToLoggers(" at 1: " + entry.getName() + " target kingdom " + targetKingdom);
          if (entry.isAvailable(getResponder().getPower(), getResponder().isReallyPaying()))
          {
            if ((!entry.ISPAYMENT) || (getResponder().isReallyPaying()))
            {
              numitems = 0;
              stayBehind = 0;
              inventoryItems = getResponder().getInventory().getAllItems(true);
              for (int x = 0; x < inventoryItems.length; x++) {
                if (!inventoryItems[x].willLeaveServer(true, changingCluster, 
                  getResponder().getPower() > 0))
                {
                  stayBehind++;
                  getResponder().getCommunicator().sendNormalServerMessage("The " + inventoryItems[x]
                    .getName() + " stays behind.");
                }
              }
              Item[] bodyItems = getResponder().getBody().getAllItems();
              for (int x = 0; x < bodyItems.length; x++) {
                if (!bodyItems[x].willLeaveServer(true, changingCluster, getResponder().getPower() > 0))
                {
                  stayBehind++;
                  getResponder().getCommunicator().sendNormalServerMessage("The " + bodyItems[x]
                    .getName() + " stays behind.");
                }
              }
              if (getResponder().getPower() == 0) {
                numitems = inventoryItems.length + bodyItems.length - stayBehind - 12;
              }
              if (numitems < 200)
              {
                getResponder().getCommunicator().sendNormalServerMessage("You step through the portal. Will you ever return?");
                if ((getResponder().getPower() == 0) && (changingCluster)) {
                  try
                  {
                    getResponder().setLastKingdom();
                    getResponder().getStatus().setKingdom(targetKingdom);
                  }
                  catch (IOException iox)
                  {
                    getResponder().getCommunicator().sendNormalServerMessage("A sudden strong wind blows through the portal, throwing you back!");
                    
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                    return;
                  }
                }
                if (changingCluster)
                {
                  if (getResponder().getPower() <= 0)
                  {
                    try
                    {
                      Skill fs = getResponder().getSkills().getSkill(1023);
                      if (fs.getKnowledge() > 50.0D)
                      {
                        double x = 100.0D - fs.getKnowledge();
                        x -= x * 0.95D;
                        double newskill = fs.getKnowledge() - x;
                        fs.setKnowledge(newskill, false);
                        getResponder().getCommunicator().sendAlertServerMessage("Your group fighting skill has been set to " + fs
                          .getKnowledge(0.0D) + "!");
                      }
                    }
                    catch (NoSuchSkillException localNoSuchSkillException) {}
                    try
                    {
                      Skill as = getResponder().getSkills().getSkill(1030);
                      if (as.getKnowledge() > 50.0D)
                      {
                        double x = 100.0D - as.getKnowledge();
                        x -= x * 0.95D;
                        double newskill = as.getKnowledge() - x;
                        as.setKnowledge(newskill, false);
                        getResponder().getCommunicator().sendAlertServerMessage("Your archery skill has been set to " + as
                          .getKnowledge(0.0D) + "!");
                      }
                    }
                    catch (NoSuchSkillException localNoSuchSkillException1) {}
                  }
                  getResponder().setLastChangedCluster();
                }
                int targetTileX = entry.SPAWNPOINTJENNX;
                int targetTileY = entry.SPAWNPOINTJENNY;
                if (targetKingdom == 2)
                {
                  targetTileX = entry.SPAWNPOINTMOLX;
                  targetTileY = entry.SPAWNPOINTMOLY;
                }
                else if (targetKingdom == 3)
                {
                  targetTileX = entry.SPAWNPOINTLIBX;
                  targetTileY = entry.SPAWNPOINTLIBY;
                }
                getResponder().sendToLoggers("Before spawnpoints: " + this.selectedSpawn + ", server=" + this.selectedServer + ",kingdom=" + this.selectedKingdom + " entry name=" + entry
                
                  .getName());
                Spawnpoint[] spawns = entry.getSpawns();
                if (spawns != null)
                {
                  String kval = aAnswers.getProperty("spawnpoint");
                  getResponder().sendToLoggers("Inside spawns. Length is " + spawns.length + " kval=" + kval);
                  
                  int spnum = -1;
                  if (kval != null)
                  {
                    kval = kval.replace("spawn", "");
                    try
                    {
                      spnum = Integer.parseInt(kval);
                    }
                    catch (NumberFormatException nfe)
                    {
                      spnum = this.selectedSpawn;
                    }
                  }
                  else
                  {
                    spnum = this.selectedSpawn;
                  }
                  getResponder().sendToLoggers("Before loop. " + spnum);
                  for (Spawnpoint sp : spawns)
                  {
                    if ((!entry.HOMESERVER) && (spnum < 0)) {
                      if (sp.kingdom == targetKingdom)
                      {
                        this.selectedSpawn = sp.number;
                        getResponder().sendToLoggers("Inside spawnpoints. Just selected " + this.selectedSpawn + " AT RANDOM, server=" + this.selectedServer + ",kingdom=" + this.selectedKingdom);
                        
                        targetTileX = sp.tilex - 2 + Server.rand.nextInt(5);
                        targetTileY = sp.tiley - 2 + Server.rand.nextInt(5);
                        break;
                      }
                    }
                    if (sp.number == this.selectedSpawn)
                    {
                      getResponder().sendToLoggers("Using selected spawn " + this.selectedSpawn);
                      
                      targetTileX = sp.tilex - 2 + Server.rand.nextInt(5);
                      targetTileY = sp.tiley - 2 + Server.rand.nextInt(5);
                      break;
                    }
                    if (spnum == sp.number)
                    {
                      this.selectedSpawn = sp.number;
                      getResponder().sendToLoggers("Inside spawnpoints. Just selected " + this.selectedSpawn + ", server=" + this.selectedServer + ",kingdom=" + this.selectedKingdom);
                      if ((getResponder().getPower() <= 0) && (targetKingdom == 0)) {
                        targetKingdom = sp.kingdom;
                      }
                      targetTileX = sp.tilex - 2 + Server.rand.nextInt(5);
                      targetTileY = sp.tiley - 2 + Server.rand.nextInt(5);
                      break;
                    }
                  }
                }
                getResponder().sendToLoggers(" at 4: " + entry
                  .getName() + " target kingdom " + targetKingdom + "tx=" + targetTileX + ", ty=" + targetTileY);
                if (Servers.localServer.entryServer)
                {
                  getResponder().setRotation(270.0F);
                  if (getResponder().isPlayer()) {
                    ((Player)getResponder()).addTitle(Titles.Title.Educated);
                  }
                }
                if (newTutorial) {
                  getResponder().setFlag(76, false);
                }
                getResponder().sendTransfer(Server.getInstance(), entry.INTRASERVERADDRESS, 
                  Integer.parseInt(entry.INTRASERVERPORT), entry.INTRASERVERPASSWORD, entry.id, targetTileX, targetTileY, true, entry
                  .isChallengeServer(), targetKingdom);
              }
              else
              {
                getResponder().getCommunicator().sendNormalServerMessage("The portal does not work. You are probably carrying too much. Try 200 items on body and in inventory.");
              }
            }
            else
            {
              getResponder().getCommunicator().sendNormalServerMessage("Alas! A trifle stops you from entering the portal. You need to purchase some nice premium time in order to enter the portal.");
            }
          }
          else if (entry.maintaining) {
            getResponder().getCommunicator().sendNormalServerMessage("The portal is shut but a flicker indicates that it may open soon. You may try later.");
          } else if (entry.isFull()) {
            getResponder().getCommunicator().sendNormalServerMessage("The portal is shut. " + entry.currentPlayers + " people are on the other side of the portal but only " + entry.pLimit + " are allowed. Please note that we are adding new servers as soon as possible when all available servers are full.");
          } else {
            getResponder().getCommunicator().sendNormalServerMessage("The portal is shut. The lands beyond are not available at the moment.");
          }
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("The portal is shut. No matter what you try nothing happens.");
        }
      }
      else
      {
        getResponder().getCommunicator().sendNormalServerMessage("You decide not to step through the portal.");
      }
    }
    else if (this.step == 1)
    {
      String val2 = aAnswers.getProperty("sid");
      if ((val2 != null) || (val == null)) {
        try
        {
          int spnum = this.selectedSpawn;
          getResponder()
            .sendToLoggers("At 1: " + this.selectedSpawn + ", server=" + this.selectedServer + ", val2=" + val2 + " kingdom=" + this.selectedKingdom);
          if (val2 != null)
          {
            this.selectedServer = Integer.parseInt(val2);
            getResponder().sendToLoggers("At 2: val 2 is not null server=" + this.selectedServer + ", val2=" + val2);
          }
          ServerEntry entry = Servers.getServerWithId(this.selectedServer);
          if (entry != null)
          {
            Spawnpoint[] spawns = entry.getSpawns();
            if (spawns != null)
            {
              getResponder().sendToLoggers("At 2.5: server=" + this.selectedServer + " spawn " + spnum);
              
              String kval = aAnswers.getProperty("spawnpoint");
              if (kval != null)
              {
                getResponder().sendToLoggers("At 2.6: server=" + this.selectedServer + " spawn kval " + kval);
                
                kval = kval.replace("spawn", "");
                try
                {
                  spnum = Integer.parseInt(kval);
                  getResponder().sendToLoggers("At 2.7: server=" + this.selectedServer + " spawn spnum " + spnum);
                  
                  numitems = spawns;stayBehind = numitems.length;
                  for (inventoryItems = 0; inventoryItems < stayBehind; inventoryItems++)
                  {
                    Spawnpoint sp = numitems[inventoryItems];
                    if (sp.number == spnum)
                    {
                      getResponder().sendToLoggers("At 2.8: spawn " + sp.name);
                      this.selectedKingdom = sp.kingdom;
                      break;
                    }
                  }
                }
                catch (NumberFormatException localNumberFormatException1) {}
              }
            }
          }
          String kingdomid = "kingdid";
          String kval = aAnswers.getProperty("kingdid");
          if (kval != null) {
            try
            {
              this.selectedKingdom = Byte.parseByte(kval);
              getResponder().sendToLoggers("At 3: " + spnum + ", server=" + this.selectedServer + ", val2=" + val2 + " selected kingdom=" + this.selectedKingdom);
            }
            catch (NumberFormatException nfe)
            {
              this.selectedKingdom = getResponder().getKingdomId();
            }
          }
          PortalQuestion pq = new PortalQuestion(getResponder(), "Entering portal", "Go ahead!", this.portal);
          
          pq.step = 1;
          pq.selectedServer = this.selectedServer;
          pq.selectedSpawn = spnum;
          pq.selectedKingdom = this.selectedKingdom;
          pq.sendQuestion();
        }
        catch (NumberFormatException nfe)
        {
          logger.log(Level.WARNING, nfe.getMessage() + ": " + val2);
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    if (this.portal != null)
    {
      byte targetKingdom = this.selectedKingdom;
      int data1 = this.portal.getData1();
      
      int epicServerId = getResponder().getEpicServerId();
      if (this.step == 1) {
        data1 = this.selectedServer;
      } else if (this.portal.isEpicPortal()) {
        if ((epicServerId > 0) && (epicServerId != Servers.localServer.id))
        {
          data1 = epicServerId;
          
          ServerEntry entry = Servers.getServerWithId(data1);
          if (entry != null) {
            if (entry.EPIC == Servers.localServer.EPIC) {
              data1 = 100001;
            }
          }
        }
        else
        {
          data1 = 100001;
        }
      }
      ServerEntry entry = Servers.getServerWithId(data1);
      if (entry != null)
      {
        if (entry.id == Servers.loginServer.id) {
          entry = Servers.loginServer;
        }
        if ((getResponder().getPower() == 0) && (!Servers.isThisATestServer()) && ((entry.entryServer) || 
          (Servers.localServer.isChallengeServer()) || (
          (entry.isChallengeServer()) && (!Servers.localServer.entryServer))))
        {
          buf.append("text{type='bold';text=\"The portal looks dormant.\"};");
        }
        else if (this.portal.isEpicPortal())
        {
          if (epicServerId == entry.id)
          {
            if (entry.isAvailable(getResponder().getPower(), getResponder().isReallyPaying()))
            {
              this.step = 1;
              this.selectedServer = entry.id;
              if (entry.EPIC) {
                buf.append("text{text=\"This portal leads to the Epic server " + entry.name + " where you last left it.\"}");
              } else if (entry.isChallengeServer()) {
                buf.append("text{text=\"This portal leads to the Challenge server '" + entry.name + "'.\"}");
              } else if (entry.PVPSERVER) {
                buf.append("text{text=\"This portal leads back to the Wild server " + entry.name + " where you last left it.\"}");
              } else {
                buf.append("text{text=\"This portal leads to back the Freedom server " + entry.name + " where you last left it.\"}");
              }
            }
            else
            {
              buf.append("text{text=\"The " + entry.name + " server is currently unavailable to you.\"}");
            }
          }
          else if (entry.isAvailable(getResponder().getPower(), getResponder().isReallyPaying()))
          {
            if (entry.EPIC)
            {
              buf.append("text{text=\"This portal leads to the Epic server " + entry.name + ". Please select a kingdom to join:\"}");
              
              addKingdoms(entry, buf);
            }
            else if (entry.PVPSERVER)
            {
              buf.append("text{text=\"This portal leads to the Wild server " + entry.name + ". Please select a kingdom to join:\"}");
              
              addKingdoms(entry, buf);
            }
            else
            {
              buf.append("text{text=\"This portal leads to the Freedom server " + entry.name + ". You will join:\"}");
              
              addKingdoms(entry, buf);
            }
          }
          else {
            buf.append("text{text=\"The " + entry.name + " server is currently unavailable to you.\"}");
          }
          if ((!entry.ISPAYMENT) || (getResponder().isReallyPaying()))
          {
            if ((Servers.localServer.entryServer) && (getResponder().getPower() == 0)) {
              buf.append("text{text=\"Do you wish to enter this portal never to return?\"};");
            } else {
              buf.append("text{text=\"Do you wish to enter this portal?\"};");
            }
            buf.append("radio{ group='portalling'; id='true';text='Yes'}");
            buf.append("radio{ group='portalling'; id='false';text='No';selected='true'}");
          }
          else
          {
            buf.append("text{text=\"Alas! A trifle stops you from entering the portal. You need to purchase some nice premium time in order to enter the portal.\"}");
          }
        }
        else
        {
          if (!entry.PVPSERVER)
          {
            buf.append("text{text='This portal leads to the safe lands of " + 
              Kingdoms.getNameFor(entry.KINGDOM) + ".'}");
            if ((!entry.PVPSERVER) && (getResponder().getDeity() != null) && 
              (getResponder().getDeity().number == 4)) {
              buf.append("text{text=\"You will lose connection with " + getResponder().getDeity().name + " if you enter the portal.\"}");
            }
            if (entry.KINGDOM != 0) {
              targetKingdom = entry.KINGDOM;
            } else {
              targetKingdom = getResponder().getKingdomId();
            }
          }
          else if ((entry.KINGDOM != 0) && (getResponder().getPower() == 0) && (Servers.localServer.entryServer) && (targetKingdom == 0))
          {
            targetKingdom = entry.KINGDOM;
          }
          else if (targetKingdom == 0)
          {
            getResponder().sendToLoggers("Not setting kingdom at 12");
            targetKingdom = getResponder().getKingdomId();
          }
          else
          {
            getResponder().sendToLoggers("Keeping kingdom at 12:" + targetKingdom);
          }
          if (entry.isAvailable(getResponder().getPower(), getResponder().isReallyPaying()))
          {
            boolean changingCluster = false;
            boolean changingEpicCluster = false;
            if (Servers.localServer.PVPSERVER != entry.PVPSERVER)
            {
              changingCluster = true;
            }
            else if (Servers.localServer.EPIC != entry.EPIC)
            {
              changingCluster = true;
              changingEpicCluster = true;
              buf.append("text{text=\"You will not be able to use this portal. You must use an Epic Portal which you can build yourself using stones and logs.\"};");
            }
            else if (targetKingdom == 3)
            {
              buf.append("text{text=\"The portal comes to life! You may pass to " + 
                Kingdoms.getNameFor((byte)3) + "!\"}");
            }
            if (Servers.localServer.entryServer) {
              changingCluster = false;
            }
            if ((changingCluster) && (!changingEpicCluster))
            {
              if ((getResponder().isChampion()) && (!Servers.localServer.EPIC)) {
                buf.append("text{text=\"You will not be able to use this portal since you are a champion.\"};");
              }
              if (getResponder().getLastChangedCluster() + 3600000L > System.currentTimeMillis()) {
                buf.append("text{text=\"You will not be able to use this portal since you may only change cluster once per hour.\"};");
              }
              if (getResponder().getPower() <= 0)
              {
                try
                {
                  Skill fs = getResponder().getSkills().getSkill(1023);
                  if (fs.getKnowledge(0.0D) > 50.0D) {
                    buf.append("text{text=\"Your new group fighting skill will become " + fs
                      .getKnowledge(0.0D) * 0.949999988079071D + "!\"};");
                  }
                }
                catch (NoSuchSkillException localNoSuchSkillException) {}
                try
                {
                  Skill as = getResponder().getSkills().getSkill(1030);
                  if (as.getKnowledge(0.0D) > 50.0D) {
                    buf.append("text{text=\"Your new group archery skill will become " + as
                      .getKnowledge(0.0D) * 0.949999988079071D + "!\"};");
                  }
                }
                catch (NoSuchSkillException localNoSuchSkillException1) {}
              }
            }
            int numitems = 0;
            if (!changingEpicCluster)
            {
              int stayBehind = 0;
              Item[] inventoryItems = getResponder().getInventory().getAllItems(true);
              for (int x = 0; x < inventoryItems.length; x++) {
                if (!inventoryItems[x].willLeaveServer(false, changingCluster, 
                  getResponder().getPower() > 0))
                {
                  stayBehind++;
                  buf.append("text{text=\"The " + inventoryItems[x].getName() + " will stay behind.\"};");
                  if ((Servers.localServer.entryServer) && 
                    (inventoryItems[x].getTemplateId() == 166)) {
                    buf.append("text{text=\"The structure will be destroyed.\"};");
                  }
                }
              }
              Item[] bodyItems = getResponder().getBody().getAllItems();
              for (int x = 0; x < bodyItems.length; x++) {
                if (!bodyItems[x].willLeaveServer(false, changingCluster, 
                  getResponder().getPower() > 0))
                {
                  stayBehind++;
                  buf.append("text{text=\"The " + bodyItems[x].getName() + " will stay behind.\"};");
                  if ((Servers.localServer.entryServer) && 
                    (bodyItems[x].getTemplateId() == 166)) {
                    buf.append("text{text=\"The structure will be destroyed.\"};");
                  }
                }
              }
              if (stayBehind > 0) {
                buf.append("text{text=\"Items that stay behind will normally be available again when you return here.\"};");
              }
              if (getResponder().getPower() == 0) {
                numitems = inventoryItems.length + bodyItems.length - stayBehind - 12;
              }
            }
            if (numitems > 200)
            {
              buf.append("text{text=\"The portal seems to become unresponsive as you approach. You are carrying too much. Try removing " + (numitems - 200) + " items from body and inventory.\"};");
            }
            else if ((!entry.ISPAYMENT) || (getResponder().isReallyPaying()))
            {
              if ((Servers.localServer.entryServer) && (getResponder().getPower() == 0)) {
                buf.append("text{text=\"Do you wish to enter this portal never to return?\"};");
              } else {
                buf.append("text{text=\"Do you wish to enter this portal?\"};");
              }
              if ((getResponder().getPower() == 0) && (Servers.localServer.entryServer)) {
                buf.append("text{type='bold';text=\"Note that you will automatically convert to a " + 
                  Kingdoms.getNameFor(targetKingdom) + "!\"};");
              }
              buf.append("radio{ group='portalling'; id='true';text='Yes'}");
              buf.append("radio{ group='portalling'; id='false';text='No';selected='true'}");
            }
            else
            {
              buf.append("text{text=\"Alas! A trifle stops you from entering the portal. You need to purchase some nice premium time in order to enter the portal.\"}");
            }
          }
          else if (entry.maintaining)
          {
            buf.append("text{text=\"The portal is shut but a flicker indicates that it may open soon. You may try later.\"}");
          }
          else if (entry.isFull())
          {
            buf.append("text{text=\"The portal is shut. " + entry.currentPlayers + " people are on the other side of the portal but only " + entry.pLimit + " are allowed.\"}");
          }
          else
          {
            buf.append("text{text=\"The portal is shut. The lands beyond are not available at the moment.\"}");
          }
        }
      }
      else
      {
        if ((data1 == 100000) || (data1 == 100001) || (data1 == 100002))
        {
          buf.setLength(0);
          sendQuestion2(data1);
          return;
        }
        buf.append("text{text=\"The portal is shut. No matter what you try nothing happens.\"}");
      }
    }
    else
    {
      buf.append("text{text=\"The portal fades from view and becomes immaterial. No matter what you try nothing happens.\"}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(700, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public final void sendQuestion2(int portalNumber)
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    this.step = 1;
    boolean selected = true;
    if ((portalNumber != 100000) && (portalNumber != 100001) && (portalNumber != 100002)) {
      this.selectedServer = portalNumber;
    }
    List<ServerEntry> entries = Servers.getServerList(portalNumber);
    if ((this.portal.isEpicPortal()) && (!epicPortalsEnabled) && (getResponder().getPower() == 0)) {
      entries.clear();
    }
    if (entries.size() == 0)
    {
      buf.append("text{text=\"The portal is shut. No matter what you try nothing happens.\"}");
    }
    else
    {
      ServerEntry[] entryArr = (ServerEntry[])entries.toArray(new ServerEntry[entries.size()]);
      Arrays.sort(entryArr);
      for (ServerEntry sentry : entryArr) {
        if ((getResponder().getPower() > 0) || (!sentry.entryServer) || (Servers.localServer.testServer))
        {
          String desc = "";
          String colour = "";
          switch (sentry.id)
          {
          case 1: 
            desc = " - This is the tutorial server.";
            colour = this.purple;
            break;
          case 3: 
            desc = " - This is an old and large PvP server in the Freedom cluster. Custom kingdoms can be formed here.";
            colour = this.orange;
            break;
          case 5: 
            desc = " - This is the oldest large PvE server in the Freedom cluster.";
            colour = this.green;
            break;
          case 6: 
          case 7: 
          case 8: 
            desc = " - This is a standard sized, well developed PvE server in the Freedom cluster.";
            colour = this.green;
            break;
          case 9: 
            desc = " - This is the Jenn-Kellon Home PvP server in the Epic cluster. Home servers have large bonuses against attackers.";
            colour = this.orange;
            break;
          case 10: 
            desc = " - This is the Mol Rehan Home PvP server in the Epic cluster. Home servers have large bonuses against attackers.";
            colour = this.orange;
            break;
          case 11: 
            desc = " - This is the Horde of The Summoned Home PvP server in the Epic cluster. Home servers have large bonuses against attackers.";
            colour = this.orange;
            break;
          case 12: 
            desc = " - This is the central PvP server in the Epic cluster. This is where the kingdoms clash, and custom kingdoms are formed.";
            colour = this.red;
            break;
          case 13: 
          case 14: 
            desc = " - This is a standard sized, fairly well developed PvE server in the Freedom cluster.";
            colour = this.green;
            break;
          case 15: 
            desc = " - The most recent Land Rush server. It is bigger than all the other servers together.";
            colour = this.green;
            break;
          case 20: 
            desc = " - This is the Challenge server. Very quick skillgain, small and compact providing lots of action. Full loot PvP with highscore lists and prizes. Resets after a while.";
            colour = this.cyan;
            break;
          case 2: 
          case 4: 
          case 16: 
          case 17: 
          case 18: 
          case 19: 
          default: 
            String kingdomname = Kingdoms.getNameFor(sentry.KINGDOM);
            String pvp = " Pvp Kingdoms ";
            String kingdoms = " (" + kingdomname + "): ";
            if (!sentry.PVPSERVER) {
              pvp = " Non-Pvp";
            } else if (sentry.HOMESERVER) {
              pvp = " Pvp Home";
            } else {
              kingdoms = ": ";
            }
            desc = " - Test Server. " + pvp + kingdoms;
            colour = this.cyan;
          }
          if (sentry.id != Servers.localServer.id)
          {
            boolean full = sentry.isFull();
            if (sentry.isAvailable(getResponder().getPower(), getResponder().isReallyPaying()))
            {
              if (entryArr.length == 1)
              {
                buf.append("harray{radio{group='sid';id='" + sentry.id + "';selected='true'}label{color='" + colour + "';text='" + sentry.name + desc + (full ? " (Full)" : "") + "'}}");
                
                buf.append("text{text=''}");
                buf.append("text{text='You will join the following kingdom:'}");
                addKingdoms(sentry, buf);
              }
              else
              {
                buf.append("harray{radio{group='sid';id='" + sentry.id + "';selected='" + selected + "'}label{color='" + colour + "';text='" + sentry.name + desc + (full ? " (Full)" : "") + "'}}");
              }
              selected = false;
            }
            else
            {
              String reason = "unavailable";
              if ((full) && (sentry.isConnected())) {
                reason = "full";
              }
              if (sentry.maintaining) {
                reason = "maintenance";
              }
              buf.append("label{color=\"" + colour + "\";text=\"    " + sentry.name + desc + " Unavailable: " + reason + ".\"}");
            }
          }
        }
      }
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(700, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private static final void addVillages(ServerEntry entry, StringBuilder buf, byte selectedKingdom)
  {
    Spawnpoint[] spawns = entry.getSpawns();
    if ((spawns != null) && (spawns.length > 0))
    {
      buf.append("text{text=\"Also, please select a start village:\"}");
      
      int numSelected = Server.rand.nextInt(spawns.length);
      int curr = 0;
      for (Spawnpoint spawn : spawns) {
        if ((selectedKingdom != 0) && (spawn.kingdom == selectedKingdom)) {
          buf.append("radio{group=\"spawnpoint\";id=\"spawn" + spawn.number + "\"; text=\"" + spawn.name + " (" + spawn.description + ")\";selected=\"" + (numSelected == curr++) + "\"}");
        }
      }
    }
  }
  
  private static final void addKingdoms(ServerEntry entry, StringBuilder buf)
  {
    Set<Byte> kingdoms = entry.getExistingKingdoms();
    int numSelected;
    boolean selected;
    if (entry.HOMESERVER)
    {
      Kingdom kingd = Kingdoms.getKingdom(entry.KINGDOM);
      if (kingd != null) {
        buf.append("radio{group=\"kingdid\";id=\"" + entry.KINGDOM + "\"; text=\"" + kingd.getName() + "\";selected=\"" + true + "\"}");
      }
      buf.append("text{text=\"\"}");
      addVillages(entry, buf, entry.KINGDOM);
    }
    else if (entry.isChallengeServer())
    {
      Spawnpoint[] spawns = entry.getSpawns();
      if ((spawns != null) && (spawns.length > 0))
      {
        numSelected = Server.rand.nextInt(spawns.length);
        int curr = 0;
        for (Spawnpoint spawn : spawns)
        {
          Kingdom kingd = Kingdoms.getKingdom(spawn.kingdom);
          if ((kingd != null) && (kingd.acceptsTransfers()))
          {
            buf.append("radio{group=\"spawnpoint\";id=\"spawn" + spawn.number + "\"; text=\"" + spawn.name + " in " + kingd
              .getName() + " (" + spawn.description + ")\";selected=\"" + (numSelected == curr) + "\"}");
            
            curr++;
          }
        }
      }
      buf.append("text{text=\"\"}");
    }
    else
    {
      selected = true;
      for (Byte k : kingdoms)
      {
        Kingdom kingd = Kingdoms.getKingdom(k.byteValue());
        if ((kingd != null) && (kingd.acceptsTransfers()))
        {
          buf.append("radio{group=\"kingdid\";id=\"" + k.byteValue() + "\"; text=\"" + kingd.getName() + " '" + kingd
            .getFirstMotto() + " " + kingd.getSecondMotto() + "'\";selected=\"" + selected + "\"}");
          selected = false;
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\PortalQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */