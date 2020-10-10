package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.utils.BMLBuilder;
import com.wurmonline.server.utils.BMLBuilder.TextType;
import java.awt.Color;
import java.io.IOException;
import java.util.Properties;

public class TransferQuestion
  extends Question
{
  public TransferQuestion(Creature aResponder, String aTitle, String aQuestion)
  {
    super(aResponder, aTitle, aQuestion, 88, -10L);
  }
  
  public void answer(Properties answers)
  {
    if (getResponder().isPlayer())
    {
      Player rp = (Player)getResponder();
      if (rp.hasFlag(74))
      {
        rp.getCommunicator().sendNormalServerMessage("You do not have a free faith transfer available.");
        return;
      }
      if (rp.getDeity() == null)
      {
        rp.getCommunicator().sendNormalServerMessage("You currently pray to no deity and cannot transfer to a new one.");
        return;
      }
      if (rp.isChampion())
      {
        rp.getCommunicator().sendNormalServerMessage("Champions cannot convert faith with this command.");
        return;
      }
      String key = "deityid";
      String val = answers.getProperty("deityid");
      if (val != null) {
        try
        {
          int index = Integer.parseInt(val);
          int newDeity = Deities.getDeities()[index].getNumber();
          if ((newDeity == 0) || (newDeity == rp.getDeity().getNumber()))
          {
            rp.getCommunicator().sendNormalServerMessage("You decide not to change deity for now.");
            return;
          }
          Deity newd = Deities.getDeity(newDeity);
          if (!QuestionParser.doesKingdomTemplateAcceptDeity(rp.getKingdomTemplateId(), newd))
          {
            rp.getCommunicator().sendNormalServerMessage("Your kingdom does not allow following that god.");
            return;
          }
          try
          {
            rp.getSaveFile().transferDeity(newd);
            
            rp.getCommunicator().sendNormalServerMessage("You decide to use your transfer and change deity to " + newd.getName() + ".");
            rp.setFlag(74, true);
          }
          catch (IOException iox)
          {
            rp.getCommunicator().sendNormalServerMessage("An exception occurred when changing deity. Please try again later or use /support if this persists.");
          }
        }
        catch (NumberFormatException nfe)
        {
          rp.getCommunicator().sendNormalServerMessage("Failed to parse index " + val);
        }
      }
      getResponder().getCommunicator().sendNormalServerMessage("You decide not to change deity for now.");
    }
  }
  
  public void sendQuestion()
  {
    Deity[] deities = Deities.getDeities();
    String[] deityNames = new String[deities.length];
    int defaultId = 0;
    for (int i = 0; i < deities.length; i++)
    {
      deityNames[i] = deities[i].getName();
      if (deities[i] == getResponder().getDeity()) {
        defaultId = i;
      }
    }
    BMLBuilder bml = BMLBuilder.createNormalWindow(Integer.toString(getId()), "Transfer your faith to which deity?", 
      BMLBuilder.createGenericBuilder()
      .addText("You currently have a single use free faith transfer from your current deity of " + getResponder().getDeity().getName() + " to another of your choice from the list below.")
      
      .addText("")
      .addText("Once you select which deity to transfer to and click accept, you will not be able to transfer back. Choose wisely.", null, BMLBuilder.TextType.BOLD, null)
      
      .addText("")
      .addText("Warning: Converting to Libila while on a Freedom server then transferring to Chaos while already in a WL based kingdom will remove your faith and any bonuses gained. Transferring to Chaos into a BL based kingdom will keep your Libila faith and bonuses.", null, BMLBuilder.TextType.BOLD, Color.RED)
      
      .addText("")
      .addLabel("Choose a deity to transfer your faith to:")
      .addDropdown("deityid", Integer.toString(defaultId), deityNames)
      .addText("")
      .addButton("submit", "Accept", null, null, null, true));
    
    getResponder().getCommunicator().sendBml(350, 330, true, true, bml.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\TransferQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */