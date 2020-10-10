package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.Zones;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageTeleportQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(VillageTeleportQuestion.class.getName());
  private int floorLevel;
  
  public VillageTeleportQuestion(Creature aResponder, String aTitle, String aQuestion, int aType, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 111, aTarget);
    
    this.floorLevel = 0;
  }
  
  public VillageTeleportQuestion(Creature responder)
  {
    super(responder, "Village Teleport", "", 111, responder.getWurmId());
    this.floorLevel = 0;
  }
  
  public void answer(Properties aAnswers)
  {
    boolean teleport = (aAnswers.getProperty("teleport") != null) && (aAnswers.getProperty("teleport").equals("true"));
    this.floorLevel = getResponder().getFloorLevel();
    if (getResponder().isDead())
    {
      getResponder().getCommunicator().sendNormalServerMessage("The dead can't teleport.");
      return;
    }
    if (this.floorLevel != 0)
    {
      getResponder().getCommunicator().sendNormalServerMessage("You need to be on ground level to teleport.");
      return;
    }
    Item[] inventoryItems = getResponder().getInventory().getAllItems(true);
    Item[] arrayOfItem1 = inventoryItems;int i = arrayOfItem1.length;
    for (Item localItem1 = 0; localItem1 < i; localItem1++)
    {
      lInventoryItem = arrayOfItem1[localItem1];
      if (lInventoryItem.isArtifact())
      {
        getResponder().getCommunicator().sendNormalServerMessage("The " + lInventoryItem
          .getName() + " hums and disturbs the weave. You can not teleport right now.");
        
        return;
      }
    }
    Item[] bodyItems = getResponder().getBody().getBodyItem().getAllItems(true);
    Item[] arrayOfItem2 = bodyItems;localItem1 = arrayOfItem2.length;
    for (Item lInventoryItem = 0; lInventoryItem < localItem1; lInventoryItem++)
    {
      Item lInventoryItem = arrayOfItem2[lInventoryItem];
      if (lInventoryItem.isArtifact())
      {
        getResponder().getCommunicator().sendNormalServerMessage("The " + lInventoryItem
          .getName() + " hums and disturbs the weave. You can not teleport right now.");
        
        return;
      }
    }
    if ((teleport) && (this.floorLevel == 0))
    {
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
      Village village = getResponder().getCitizenVillage();
      if (village != null)
      {
        getResponder().setTeleportPoints((short)village.getTokenX(), (short)village.getTokenY(), 0, 0);
        if (getResponder().startTeleporting())
        {
          getResponder().getCommunicator().sendTeleport(false);
          getResponder().teleport();
          ((Player)getResponder()).setUsedFreeVillageTeleport();
        }
      }
      else
      {
        logger.log(Level.WARNING, getResponder().getName() + " tried to teleport to null settlement!");
      }
    }
  }
  
  public void sendQuestion()
  {
    this.floorLevel = getResponder().getFloorLevel();
    if (this.floorLevel == 0)
    {
      Village village = getResponder().getCitizenVillage();
      if (village != null)
      {
        String villageName = village.getName();
        StringBuilder buf = new StringBuilder();
        buf.append(getBmlHeader());
        
        buf.append("text{type=\"bold\";text=\"Teleport to settlement " + villageName + ":\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"You have to option to teleport directly to the village token of your new village.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"You can only do this once per character.\"}");
        buf.append("text{text=\"\"}");
        
        buf.append("text{text=\"Do you want to teleport to " + villageName + "?\"}");
        
        buf.append("radio{ group=\"teleport\"; id=\"true\";text=\"Yes\"}");
        buf.append("radio{ group=\"teleport\"; id=\"false\";text=\"No\";selected=\"true\"}");
        
        buf.append(createOkAnswerButton());
        getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
      }
      else
      {
        logger.log(Level.WARNING, getResponder().getName() + " tried to teleport to null settlement!");
        getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for the teleportation. Please contact administration.");
      }
    }
    else
    {
      Village village = getResponder().getCitizenVillage();
      if (village != null)
      {
        String villageName = village.getName();
        StringBuilder buf = new StringBuilder();
        buf.append(getBmlHeader());
        
        buf.append("text{type=\"bold\";text=\"Teleport to settlement " + villageName + ":\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"You have to option to teleport directly to the village token of your new village.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"You can only do this once per character.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{type=\"bold\";text=\"You need to be on ground level in order to teleport to your village.\"}");
        buf.append("text{type=\"bold\";text=\"Once on ground level write /vteleport in the chat to teleport.\"}");
        
        buf.append(createOkAnswerButton());
        getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
      }
      else
      {
        logger.log(Level.WARNING, getResponder().getName() + " tried to teleport to null settlement!");
        getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for the teleportation. Please contact administration.");
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\VillageTeleportQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */