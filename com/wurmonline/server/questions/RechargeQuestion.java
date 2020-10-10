package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import java.util.Properties;

public final class RechargeQuestion
  extends Question
  implements MonetaryConstants
{
  public RechargeQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 73, aTarget);
  }
  
  public void answer(Properties aAnswers)
  {
    try
    {
      Item wand = Items.getItem(this.target);
      if (wand.getOwnerId() == getResponder().getWurmId())
      {
        String key = "recharge";
        String val = aAnswers.getProperty("recharge");
        if ((val != null) && (val.equals("true")))
        {
          if (wand.getQualityLevel() < 90.0F) {
            try
            {
              if (QuestionParser.charge(getResponder(), 50000L, "Recharging", 0.3F))
              {
                getResponder().getCommunicator().sendNormalServerMessage("Something rummages through your pockets. The " + wand.getName() + " hums softly.");
                wand.setQualityLevel(wand.getQualityLevel() + 10.0F);
              }
              else
              {
                getResponder().getCommunicator().sendNormalServerMessage("The spirits demand that you carry 5 silver in your inventory in coinage.");
              }
            }
            catch (Exception ex)
            {
              getResponder().getCommunicator().sendNormalServerMessage(ex.getMessage());
            }
          } else {
            getResponder().getCommunicator().sendNormalServerMessage("You may not recharge the " + wand.getName() + " while it has a quality level above 89.");
          }
        }
        else {
          getResponder().getCommunicator().sendNormalServerMessage("You decide not to recharge the " + wand.getName() + " for now.");
        }
      }
      else
      {
        getResponder().getCommunicator().sendNormalServerMessage("You are not in possession of the " + wand.getName() + " any longer and may not recharge it.");
      }
    }
    catch (NoSuchItemException nsi)
    {
      getResponder().getCommunicator().sendNormalServerMessage("You can not recharge the item now.");
      return;
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    try
    {
      Item item = Items.getItem(this.target);
      
      buf.append("label{text=\"Recharge the " + item.getName() + "?\"};");
      buf.append("text{text='Recharging will cost 5 silver coins and increase the quality level by 10 to a maximum of 99.'}text{text=''}");
      buf.append("text{text='You need to have the coins in your inventory.'}text{text=''}");
      buf.append("text{text=\"The current quality level is " + item.getQualityLevel() + ".\"}text{text=''}");
      buf.append("text{text=\"\"}");
      if (item.getQualityLevel() > 89.0F)
      {
        buf.append("text{text=\"You may not recharge the item yet.\"}");
      }
      else
      {
        buf.append("text{type='italic';text=\"Do you want to recharge the " + item.getName() + "?\"}");
        
        buf.append("radio{ group='recharge'; id='true';text='Yes'}");
        buf.append("radio{ group='recharge'; id='false';text='No';selected='true'}");
      }
    }
    catch (NoSuchItemException nsi)
    {
      buf.append("text{text='The item can not be found.'}text{text=''}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\RechargeQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */