package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.AllianceWar;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.villages.WarDeclaration;
import com.wurmonline.server.zones.FocusZone;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageInfo
  extends Question
  implements VillageStatus, TimeConstants
{
  private static final Logger logger = Logger.getLogger(VillageInfo.class.getName());
  private static final NumberFormat nf = NumberFormat.getInstance();
  private VillageRole playerRole = null;
  
  public VillageInfo(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 14, aTarget);
    nf.setMaximumFractionDigits(6);
  }
  
  public VillageInfo(Creature aResponder, VillageRole vRole)
  {
    super(aResponder, "", "", 14, -10L);
    this.playerRole = vRole;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    if (Boolean.parseBoolean(getAnswer().getProperty("showPlayerRole")))
    {
      VillageInfo vi = new VillageInfo(getResponder(), this.playerRole);
      vi.sendQuestion();
    }
  }
  
  public void sendQuestion()
  {
    if (this.playerRole != null)
    {
      VillageRolesManageQuestion.roleShow(getResponder(), getId(), null, this.playerRole, "");
      return;
    }
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
        Item deed = Items.getItem(this.target);
        int villageId = deed.getData2();
        village = Villages.getVillage(villageId);
      }
      if ((village.getMayor() != null) && (village.getMayor().getId() == getResponder().getWurmId())) {
        try
        {
          Item deed = Items.getItem(village.getDeedId());
          if (deed.getOwnerId() < 0L)
          {
            logger.log(Level.INFO, 
              getResponder().getName() + " retrieving and inserting deed " + village.getDeedId() + " for " + village
              .getName() + ".");
            deed.setTransferred(false);
            deed.setMailed(false);
            getResponder().getInventory().insertItem(deed);
            getResponder().getCommunicator().sendNormalServerMessage("You have retrieved your settlement deed.");
          }
        }
        catch (NoSuchItemException iox)
        {
          logger.log(Level.WARNING, "No deed available for " + village.getName() + ". Creating new. Exception was " + iox
            .getMessage(), iox);
          village.replaceNoDeed(getResponder());
          getResponder().getCommunicator().sendNormalServerMessage("You have received a new settlement deed.");
        }
      }
      StringBuilder buf = new StringBuilder();
      buf.append(getBmlHeader());
      buf.append("header{text=\"" + village.getName() + "\"}");
      buf.append("text{type=\"italic\";text=\"" + village.getMotto() + "\"};text{text=\"\"}");
      if (village.isCapital()) {
        buf.append("text{type=\"bold\";text=\"Welcome to the capital of " + Kingdoms.getNameFor(village.kingdom) + "!\"};text{text=\"\"}");
      }
      if (village.isDisbanding())
      {
        long timeleft = village.getDisbanding() - System.currentTimeMillis();
        String times = Server.getTimeFor(timeleft);
        buf.append("text{type=\"bold\";text=\"This settlement is disbanding\"}");
        if (timeleft > 0L) {
          buf.append("text{type=\"bold\";text=\"Eta: " + times + ".\"};text{text=\"\"}");
        } else {
          buf.append("text{type=\"bold\";text=\"Eta: any minute now.\"};text{text=\"\"}");
        }
      }
      if ((village.isCitizen(getResponder())) || (getResponder().getPower() >= 2))
      {
        buf.append("text{text=\"The size of " + village.getName() + " is " + village.getDiameterX() + " by " + village
          .getDiameterY() + ".\"}");
        buf.append("text{text=\"The perimeter is " + (5 + village.getPerimeterSize()) + " and it has " + village.plan
          .getNumHiredGuards() + " guards hired.\"}");
        if (Servers.localServer.testServer) {
          buf.append("text{text='[TEST] Number of current guards in guardPlan: " + village.getGuards().length + "'}");
        }
        long money = village.plan.getMoneyLeft();
        Change ca = new Change(money);
        if (Servers.localServer.isUpkeep())
        {
          buf.append("text{text=\"The settlement has " + ca.getChangeString() + " in its coffers.\"}");
          if (getResponder().getPower() >= 2)
          {
            logger.log(Level.INFO, getResponder().getName() + " checking " + village.getName() + " financial info.");
            getResponder().getLogger().log(Level.INFO, 
              getResponder().getName() + " checking " + village.getName() + " financial info.");
            long moneyTick = village.plan.calculateUpkeep(false);
            Change cu = new Change(moneyTick);
            buf.append("text{text=\"Every tick (~8 mins) will drain " + cu.getChangeString() + ".\"}");
          }
          long monthly = village.plan.getMonthlyCost();
          Change c = new Change(monthly);
          buf.append("text{text=\"The monthly cost is " + c.getChangeString() + ".\"}");
          long timeleft = village.plan.getTimeLeft();
          buf.append("text{text=\"The upkeep will last approximately " + Server.getTimeFor(timeleft) + " more.\"}");
        }
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The settlement is granted the following faith bonuses:\"}");
        buf.append("text{text=\"War (" + nf.format(village.getFaithWarValue()) + ") damage: " + nf
          .format(village.getFaithWarBonus()) + "% CR: " + nf
          .format(village.getFaithWarBonus()) + "%, Healing (" + nf.format(village.getFaithHealValue()) + "): " + nf
          
          .format(village.getFaithHealBonus()) + "%, Enchanting (" + nf
          .format(village.getFaithCreateValue()) + "): " + nf
          
          .format(village.getFaithCreateBonus()) + "%, Rarity window: " + 
          (int)Math.min(10.0F, village.getFaithCreateValue()) + " bonus seconds\"}");
        
        buf.append("text{text=\"These bonuses will decrease by 15% per day.\"}");
        buf.append("text{text=\"\"}");
        float ratio = village.getCreatureRatio();
        buf.append("text{text=\"The tile per creature ratio of this deed is " + ratio + ". Optimal is " + Village.OPTIMUMCRETRATIO + " or more.");
        if (ratio < Village.OPTIMUMCRETRATIO) {
          buf.append(" This means that you will see more disease and miscarriage.");
        } else {
          buf.append(" This is a good figure.");
        }
        int brandedCreatures = Creatures.getInstance().getBranded(village.getId()).length;
        if (brandedCreatures > 1) {
          buf.append(String.format(" There are %d creatures currently branded.", new Object[] { Integer.valueOf(brandedCreatures) }));
        } else if (brandedCreatures == 1) {
          buf.append(String.format(" There is %d creature currently branded.", new Object[] { Integer.valueOf(brandedCreatures) }));
        }
        buf.append("\"};text{text=\"\"}");
        if (village.isDemocracy()) {
          buf.append("text{text=\"" + village
            .getName() + " is a democracy. This means your citizenship cannot be revoked by any city officials such as the mayor. \"}");
        } else {
          buf.append("text{text=\"" + village
            .getName() + " is a non-democracy. This means your citizenship can be revoked by any city officials such as the mayor. \"}");
        }
        buf.append("");
        buf.append("text{text=\"\"}");
      }
      String visitor = "Visitor";
      
      this.playerRole = village.getRoleForPlayer(getResponder().getWurmId());
      if (this.playerRole != null)
      {
        if (village.isCitizen(getResponder())) {
          visitor = this.playerRole.getName() + " of " + village.getName();
        } else {
          visitor = "Individual (" + this.playerRole.getName() + ") role";
        }
      }
      else {
        try
        {
          if (getResponder().getCitizenVillage() == null)
          {
            visitor = "visitor";
            this.playerRole = village.getRoleForStatus((byte)1);
          }
          else
          {
            this.playerRole = village.getRoleForVillage(getResponder().getCitizenVillage().getId());
            if (this.playerRole != null)
            {
              visitor = "Citizen of " + getResponder().getCitizenVillage().getName();
            }
            else if (getResponder().getCitizenVillage().isAlly(village))
            {
              visitor = "Ally";
              this.playerRole = village.getRoleForStatus((byte)5);
            }
            else
            {
              visitor = "visitor";
              this.playerRole = village.getRoleForStatus((byte)1);
            }
          }
        }
        catch (NoSuchRoleException e)
        {
          logger.log(Level.WARNING, e.getMessage(), e);
          visitor = "problem";
        }
      }
      buf.append("harray{button{text=\"Show role for " + visitor + "\";id=\"showPlayerRole\"}}");
      buf.append("text{text=\"\"}");
      if (FocusZone.getHotaZone() != null)
      {
        buf.append("text{text=\"" + village.getName() + " has won the Hunt of the Ancients " + village
          .getHotaWins() + " times.\"}");
        if (Servers.localServer.getNextHota() == Long.MAX_VALUE)
        {
          buf.append("text{text=\"The Hunt of the Ancients is afoot!\"}");
        }
        else
        {
          long timeLeft = Servers.localServer.getNextHota() - System.currentTimeMillis();
          buf.append("text{text=\"The next Hunt of the Ancients is in " + Server.getTimeFor(timeLeft) + ".\"}");
        }
      }
      Village[] allies = village.getAllies();
      if (allies.length > 0)
      {
        PvPAlliance alliance = PvPAlliance.getPvPAlliance(village.getAllianceNumber());
        if (alliance != null)
        {
          Village capital = alliance.getAllianceCapital();
          buf.append("text{text=\"We are in the " + alliance.getName() + ". ");
          buf.append("The capital is " + capital.getName() + ".\"}");
          if (FocusZone.getHotaZone() != null) {
            buf.append("text{text=\"" + alliance.getName() + " has won the Hunt of the Ancients " + alliance
              .getNumberOfWins() + " times.\"}");
          }
        }
        buf.append("label{text=\"The alliance consists of: \"};text{text=\"");
        
        Arrays.sort(allies);
        for (int x = 0; x < allies.length; x++) {
          if (x == allies.length - 1) {
            buf.append(allies[x].getName());
          } else if (x == allies.length - 2) {
            buf.append(allies[x].getName() + " and ");
          } else {
            buf.append(allies[x].getName() + ", ");
          }
        }
        buf.append(".\"}");
        buf.append("text{text=\"\"}");
        if (alliance != null)
        {
          AllianceWar[] wars = alliance.getWars();
          if (wars.length > 0)
          {
            buf.append("text{type=\"bold\";text=\"We are at war with the following alliances: \"};text{text=\"");
            for (int x = 0; x < wars.length; x++)
            {
              PvPAlliance enemy = null;
              if (wars[x].hasEnded())
              {
                wars[x].delete();
              }
              else
              {
                if (wars[x].getAggressor() != alliance.getId()) {
                  enemy = PvPAlliance.getPvPAlliance(wars[x].getAggressor());
                } else {
                  enemy = PvPAlliance.getPvPAlliance(wars[x].getDefender());
                }
                if (enemy != null) {
                  if (x == wars.length - 1) {
                    buf.append(enemy.getName());
                  } else if (x == wars.length - 2) {
                    buf.append(enemy.getName() + " and ");
                  } else {
                    buf.append(enemy.getName() + ", ");
                  }
                }
              }
            }
            buf.append(".\"}");
          }
        }
      }
      if ((Servers.localServer.HOMESERVER) && (Servers.localServer.EPIC))
      {
        buf.append("text{type=\"bold\";text=\"Our notoriety is " + village.getVillageReputation() + ".\"};");
        if (village.getVillageReputation() >= 50) {
          buf.append("text{text=\" Over 50 - other settlements may declare war on us. \"};text{text=\"\"}");
        } else {
          buf.append("text{text=\" Below 50 - other settlements may not declare war on us. \"};text{text=\"\"}");
        }
      }
      Village[] enemies = village.getEnemies();
      if (enemies.length > 0)
      {
        buf.append("text{type=\"bold\";text=\"We are at war with the following settlements: \"};text{text=\"");
        
        Arrays.sort(enemies);
        for (int x = 0; x < enemies.length; x++) {
          if (x == enemies.length - 1) {
            buf.append(enemies[x].getName());
          } else if (x == enemies.length - 2) {
            buf.append(enemies[x].getName() + " and ");
          } else {
            buf.append(enemies[x].getName() + ", ");
          }
        }
        buf.append(".\"}");
        buf.append("text{text=\"\"}");
      }
      if (village.warDeclarations != null)
      {
        buf.append("label{text=\"The current settlement war declarations are: \"}");
        for (Iterator<WarDeclaration> it = village.warDeclarations.values().iterator(); it.hasNext();)
        {
          WarDeclaration declaration = (WarDeclaration)it.next();
          buf.append("text{text=\"" + declaration.receiver.getName() + " must answer the challenge from " + declaration.declarer
            .getName() + " within " + 
            Server.getTimeFor(declaration.time + 86400000L - System.currentTimeMillis()) + ".\"}");
        }
        buf.append("text{text=\"\"}");
      }
      if (village.getSkillModifier() == 0.0D) {
        buf.append("text{text=\"This settlement has no acquired knowledge.\"}");
      } else {
        buf.append("text{text=\"This settlement has acquired knowledge that increases the productivity bonus of its citizens by " + village
          .getSkillModifier() + "%.\"}");
      }
      buf.append("text{text=\"\"}");
      
      Citizen mayor = village.getMayor();
      if (mayor != null) {
        buf.append("text{type=\"italic\";text=\"" + mayor.getName() + ", " + mayor.getRole().getName() + ", " + village
          .getName() + "\"};text{text=\"\"}");
      } else {
        buf.append("text{type=\"italic\";text=\"The Citizens, " + village.getName() + "\"};text{text=\"\"}");
      }
      buf.append(createAnswerButton2());
      getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }
    catch (NoSuchItemException nsi)
    {
      logger.log(Level.WARNING, getResponder().getName() + " tried to get info for null token with id " + this.target, nsi);
      getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
    }
    catch (NoSuchVillageException nsv)
    {
      logger.log(Level.WARNING, getResponder().getName() + " tried to get info for null settlement for token with id " + this.target);
      
      getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\VillageInfo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */