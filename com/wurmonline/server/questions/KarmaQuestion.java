package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KarmaQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(Question.class.getName());
  
  public KarmaQuestion(Creature aResponder)
  {
    super(aResponder, "Using your Karma", "Decide how you wish to use your Karma", 100, aResponder.getWurmId());
  }
  
  public void answer(Properties answers)
  {
    String key = "karma";
    String val = answers.getProperty("karma");
    if (val != null) {
      if (val.equals("light50"))
      {
        if (getResponder().getKarma() >= 500)
        {
          int sx = Zones.safeTileX(getResponder().getTileX() - 50);
          int ex = Zones.safeTileX(getResponder().getTileX() + 50);
          int sy = Zones.safeTileY(getResponder().getTileY() - 50);
          int ey = Zones.safeTileY(getResponder().getTileY() + 50);
          Item[] items = Items.getAllItems();
          for (Item item : items) {
            if (item.getZoneId() > 0) {
              if (item.isStreetLamp()) {
                if ((item.isWithin(sx, ex, sy, ey)) && 
                  (item.isPlanted()))
                {
                  item.setAuxData((byte)120);
                  item.setTemperature((short)10000);
                }
              }
            }
          }
          Server.getInstance().broadCastAction(
            getResponder().getName() + " convinces the fire spirits to light up the area!", getResponder(), 50);
          getResponder().getCommunicator().sendSafeServerMessage("The fire spirits light up the area!");
          getResponder().modifyKarma(65036);
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("You need 500 karma for this.");
        }
      }
      else if (val.equals("light100"))
      {
        if (getResponder().getKarma() >= 1500)
        {
          int sx = Zones.safeTileX(getResponder().getTileX() - 100);
          int ex = Zones.safeTileX(getResponder().getTileX() + 100);
          int sy = Zones.safeTileY(getResponder().getTileY() - 100);
          int ey = Zones.safeTileY(getResponder().getTileY() + 100);
          Item[] items = Items.getAllItems();
          for (Item item : items) {
            if (item.getZoneId() > 0) {
              if (item.isStreetLamp()) {
                if ((item.isWithin(sx, ex, sy, ey)) && 
                  (item.isPlanted()))
                {
                  item.setAuxData((byte)120);
                  item.setTemperature((short)10000);
                }
              }
            }
          }
          Server.getInstance().broadCastAction(getResponder().getName() + " convinces the fire spirits to light up the area!", 
            getResponder(), 100);
          getResponder().getCommunicator().sendSafeServerMessage("The fire spirits light up the area!");
          getResponder().modifyKarma(64036);
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("You need 1500 karma for this.");
        }
      }
      else
      {
        int sy;
        int ey;
        Item[] items;
        if (val.equals("light200"))
        {
          if (getResponder().getKarma() >= 3000)
          {
            int sx = Zones.safeTileX(getResponder().getTileX() - 200);
            int ex = Zones.safeTileX(getResponder().getTileX() + 200);
            sy = Zones.safeTileY(getResponder().getTileY() - 200);
            ey = Zones.safeTileY(getResponder().getTileY() + 200);
            items = Items.getAllItems();
            for (Item item : items) {
              if (item.getZoneId() > 0) {
                if (item.isStreetLamp()) {
                  if ((item.isWithin(sx, ex, sy, ey)) && 
                    (item.isPlanted()))
                  {
                    item.setAuxData((byte)120);
                    item.setTemperature((short)10000);
                  }
                }
              }
            }
            Server.getInstance().broadCastAction(getResponder().getName() + " convinces the fire spirits to light up the area!", 
              getResponder(), 200);
            getResponder().getCommunicator().sendSafeServerMessage("The fire spirits light up the area!");
            getResponder().modifyKarma(62536);
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("You need 3000 karma for this.");
          }
        }
        else
        {
          int maxContained;
          if (val.equals("corpse"))
          {
            if (getResponder().getKarma() > 3000)
            {
              if (getResponder().maySummonCorpse())
              {
                Item toSummon = null;
                maxContained = 0;
                sy = Items.getAllItems();ey = sy.length;
                for (items = 0; items < ey; items++)
                {
                  Item i = sy[items];
                  if (i.getOwnerId() <= -10L) {
                    if (i.getName().equals("corpse of " + getResponder().getName()))
                    {
                      int nums = i.getItems().size();
                      if (nums >= maxContained)
                      {
                        toSummon = i;
                        maxContained = nums;
                      }
                    }
                  }
                }
                if (toSummon != null)
                {
                  if (toSummon.getZoneId() >= 0) {
                    try
                    {
                      Zone z = Zones.getZone((int)toSummon.getPosX() >> 2, 
                        (int)toSummon.getPosY() >> 2, toSummon.isOnSurface());
                      z.removeItem(toSummon);
                      logger.log(Level.INFO, toSummon.getName() + " was removed from " + (
                        (int)toSummon.getPosX() >> 2) + ',' + (
                        (int)toSummon.getPosY() >> 2) + ", surf=" + toSummon.isOnSurface());
                    }
                    catch (NoSuchZoneException nsz)
                    {
                      logger.log(Level.INFO, toSummon.getName() + " was not on " + (
                        (int)toSummon.getPosX() >> 2) + ',' + (
                        (int)toSummon.getPosY() >> 2) + ", surf=" + toSummon.isOnSurface());
                    }
                  }
                  try
                  {
                    Item parent = toSummon.getParent();
                    parent.dropItem(toSummon.getWurmId(), true);
                    logger.log(Level.INFO, toSummon.getName() + " was removed from " + parent.getName() + '.');
                  }
                  catch (NoSuchItemException localNoSuchItemException1) {}
                  getResponder().getInventory().insertItem(toSummon);
                  getResponder().getCommunicator().sendSafeServerMessage("The spirits summon your corpse!");
                  getResponder().modifyKarma(62536);
                }
                else
                {
                  getResponder().getCommunicator().sendSafeServerMessage("The spirits fail to locate your corpse!");
                }
              }
              else
              {
                long timeToNext = getResponder().getTimeToSummonCorpse();
                getResponder().getCommunicator().sendNormalServerMessage("You have to wait " + 
                  Server.getTimeFor(timeToNext) + " until you can summon your corpse.");
              }
            }
            else {
              getResponder().getCommunicator().sendNormalServerMessage("You need 3000 karma for this.");
            }
          }
          else if (val.equals("townportal"))
          {
            Item[] inventoryItems = getResponder().getInventory().getAllItems(true);
            for (lInventoryItem : inventoryItems) {
              if (lInventoryItem.isArtifact())
              {
                getResponder().getCommunicator().sendNormalServerMessage("The " + lInventoryItem
                  .getName() + " hums and disturbs the weave. You can not teleport right now.");
                
                return;
              }
            }
            Item[] bodyItems = getResponder().getBody().getBodyItem().getAllItems(true);
            localNoSuchItemException1 = bodyItems;ey = localNoSuchItemException1.length;
            for (Item lInventoryItem = 0; lInventoryItem < ey; lInventoryItem++)
            {
              Item lInventoryItem = localNoSuchItemException1[lInventoryItem];
              if (lInventoryItem.isArtifact())
              {
                getResponder().getCommunicator().sendNormalServerMessage("The " + lInventoryItem
                  .getName() + " hums and disturbs the weave. You can not teleport right now.");
                
                return;
              }
            }
            if ((getResponder().getEnemyPresense() > 0) || (getResponder().isFighting()))
            {
              getResponder().getCommunicator().sendNormalServerMessage("There are enemies in the vicinity. You fail to focus.");
              
              return;
            }
            if (getResponder().getCitizenVillage() == null)
            {
              getResponder().getCommunicator().sendNormalServerMessage("You need to be citizen in a village to teleport home.");
              
              return;
            }
            if (getResponder().mayChangeVillageInMillis() > 0L)
            {
              getResponder().getCommunicator().sendNormalServerMessage("You are still too new to this village to teleport home.");
              
              return;
            }
            if (getResponder().getKarma() < 1000)
            {
              getResponder().getCommunicator().sendNormalServerMessage("You need 1000 karma to perform this feat.");
              
              return;
            }
            if ((getResponder().isOnPvPServer()) && 
              (Zones.isWithinDuelRing(getResponder().getTileX(), getResponder().getTileY(), true) != null))
            {
              getResponder().getCommunicator().sendNormalServerMessage("The magic of the duelling ring interferes. You can not teleport here.");
              
              return;
            }
            if (getResponder().isInPvPZone())
            {
              getResponder().getCommunicator().sendNormalServerMessage("The magic of the pvp zone interferes. You can not teleport here.");
              
              return;
            }
            if ((Servers.localServer.PVPSERVER) && (EndGameItems.getEvilAltar() != null))
            {
              EndGameItem egi = EndGameItems.getEvilAltar();
              if (getResponder().isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi
                .getItem().getPosZ(), 50.0F))
              {
                getResponder().getCommunicator().sendNormalServerMessage("The magic of this place interferes. You can not teleport here.");
                
                return;
              }
            }
            else if ((Servers.localServer.PVPSERVER) && (EndGameItems.getGoodAltar() != null))
            {
              EndGameItem egi = EndGameItems.getGoodAltar();
              if (getResponder().isWithinDistanceTo(egi.getItem().getPosX(), egi.getItem().getPosY(), egi
                .getItem().getPosZ(), 50.0F))
              {
                getResponder().getCommunicator().sendNormalServerMessage("The magic of this place interferes. You can not teleport here.");
                
                return;
              }
            }
            try
            {
              short[] tokenCoords;
              try
              {
                tokenCoords = getResponder().getCitizenVillage().getTokenCoords();
              }
              catch (NoSuchItemException nsi)
              {
                short[] tokenCoords;
                tokenCoords = getResponder().getCitizenVillage().getSpawnPoint();
              }
              getResponder().setTeleportPoints(tokenCoords[0], tokenCoords[1], 0, 0);
              if (getResponder().startTeleporting())
              {
                getResponder().modifyKarma(64536);
                getResponder().getCommunicator().sendNormalServerMessage("You feel a slight tingle in your spine.");
                getResponder().getCommunicator().sendTeleport(false);
              }
            }
            catch (Exception ex)
            {
              getResponder().getCommunicator().sendNormalServerMessage("The weave does not contain a proper teleport spot.");
              
              return;
            }
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("You decide to bide your time.");
          }
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    
    buf.append("text{text=\"You have " + getResponder().getKarma() + " karma, how would you like to spend it?\"}");
    buf.append("text{text=''}");
    buf.append("radio{ group='karma'; id='light50';text='Light up 50 tiles radius (500 karma)'}");
    buf.append("radio{ group='karma'; id='light100';text='Light up 100 tiles radius (1500 karma)'}");
    buf.append("radio{ group='karma'; id='light200';text='Light up 200 tiles radius (3000 karma)'}");
    buf.append("radio{ group='karma'; id='corpse';text='Summon corpse (3000 karma, 5 minutes delay)'}");
    buf.append("radio{ group='karma'; id='townportal';text='Town Portal (1000 karma, enemies block)'}");
    buf.append("radio{ group='karma'; id='false';text='Do nothing';selected='true'}");
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\KarmaQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */