package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedeemQuestion
  extends Question
  implements TimeConstants
{
  private static final Logger logger = Logger.getLogger(RedeemQuestion.class.getName());
  
  public RedeemQuestion(Creature aResponder, String aTitle, String aQuestion)
  {
    super(aResponder, aTitle, aQuestion, 87, aResponder.getWurmId());
  }
  
  public void answer(Properties aAnswers)
  {
    String key = "plays";
    String val = aAnswers.getProperty("plays");
    try
    {
      long wid = Long.parseLong(val);
      if (wid > 0L)
      {
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid);
        if (pinf != null)
        {
          if (pinf.hasMovedInventory()) {
            Items.returnItemsFromFreezerFor(wid);
          }
          Set<Item> items = Items.loadAllItemsForCreatureWithId(wid, false);
          if (items.size() > 0) {
            try
            {
              Item chest = ItemFactory.createItem(192, 50 + Server.rand
                .nextInt(30), pinf.getName());
              for (Item i : items) {
                if ((!i.isInventory()) && (!i.isBodyPart()) && (!i.isHomesteadDeed()) && 
                  (!i.isNewDeed()) && (!i.isVillageDeed()) && 
                  (i.getTemplateId() != 166)) {
                  chest.insertItem(i, true);
                }
              }
              getResponder().getInventory().insertItem(chest, true);
              getResponder().getCommunicator().sendNormalServerMessage("You redeem the items for " + pinf
                .getName() + " and put them in a nice chest.");
              getResponder().getLogger().info(
                getResponder().getName() + " redeems the items of " + pinf.getName() + ".");
              logger.log(Level.INFO, getResponder().getName() + " redeems the items of " + pinf.getName() + ".");
            }
            catch (Exception ex)
            {
              logger.log(Level.INFO, ex.getMessage(), ex);
            }
          } else {
            getResponder().getCommunicator().sendNormalServerMessage("There were no items for " + pinf
              .getName() + ".");
          }
        }
      }
    }
    catch (NumberFormatException nfn)
    {
      getResponder().getCommunicator().sendNormalServerMessage("Unknown player " + val);
    }
  }
  
  public void sendQuestion()
  {
    PlayerInfo[] plays = PlayerInfoFactory.getPlayerInfos();
    String lHtml = getBmlHeader();
    StringBuilder buf = new StringBuilder(lHtml);
    buf
      .append("text{text=\"This functionality will retrieve all non-deed and non-writ items from a banned player and put them in a chest in your inventory.\"}");
    buf
      .append("text{text=\"The suggestion is to use the hide functionality to get a pair of random coordinates to put it at or simply hide it at the suggested location.\"}");
    buf.append("text{text=\"Only players with at least a year of bannination left will be listed here.\"}");
    buf.append("text{text=\"Select a banned player from which to redeem items:\"}");
    buf.append("radio{group=\"plays\";id=\"-1\";text=\"None\";selected=\"true\"};");
    for (int x = 0; x < plays.length; x++) {
      if ((plays[x].isBanned()) && (plays[x].getCurrentServer() == Servers.localServer.id)) {
        if (plays[x].banexpiry - System.currentTimeMillis() > 29030400000L) {
          buf.append("radio{group=\"plays\";id=\"" + plays[x].wurmId + "\";text=\"" + plays[x].getName() + "\"};");
        }
      }
    }
    buf.append(createAnswerButton3());
    getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\RedeemQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */