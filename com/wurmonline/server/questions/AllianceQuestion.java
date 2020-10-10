package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AllianceQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(AllianceQuestion.class.getName());
  
  public AllianceQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 18, aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parsePvPAllianceQuestion(this);
  }
  
  public final String getAllianceName()
  {
    try
    {
      Creature sender = Server.getInstance().getCreature(this.target);
      if (sender.getCitizenVillage() != null)
      {
        PvPAlliance all = PvPAlliance.getPvPAlliance(sender.getCitizenVillage().getAllianceNumber());
        if (all != null) {
          return all.getName();
        }
        if (getResponder().getCitizenVillage() != null) {
          return 
          
            sender.getCitizenVillage().getName().substring(0, Math.min(8, sender.getCitizenVillage().getName().length())) + "and" + getResponder().getCitizenVillage().getName().substring(0, Math.min(8, getResponder().getCitizenVillage().getName().length()));
        }
        return sender.getCitizenVillage().getName();
      }
    }
    catch (Exception nsc)
    {
      logger.log(Level.WARNING, nsc.getMessage(), nsc);
    }
    return "unknown";
  }
  
  public void sendQuestion()
  {
    try
    {
      Creature sender = Server.getInstance().getCreature(this.target);
      Village senderVillage = sender.getCitizenVillage();
      Village responderVillage = getResponder().getCitizenVillage();
      if (senderVillage.equals(responderVillage))
      {
        sender.getCommunicator().sendNormalServerMessage("You cannot form an alliance within a settlement.");
      }
      else
      {
        PvPAlliance respAlliance = PvPAlliance.getPvPAlliance(responderVillage.getAllianceNumber());
        if (respAlliance != null)
        {
          sender.getCommunicator().sendNormalServerMessage(
            getResponder().getName() + " is already in the " + respAlliance.getName() + " alliance.");
          return;
        }
        StringBuilder buf = new StringBuilder();
        VillageRole role = sender.getCitizenVillage().getCitizen(sender.getWurmId()).getRole();
        buf.append(getBmlHeader());
        buf.append("header{text=\"Citizens of " + responderVillage.getName() + "!\"}");
        buf.append("text{text=\"We, the citizens of " + senderVillage
          .getName() + ", under the wise leadership of " + sender
          
          .getName() + ", wish to declare our sincere intentions to invite you to the " + 
          getAllianceName() + " alliance.\"}");
        
        buf.append("text{text='This union would stand forever, strengthening both our positions in this unfriendly world against common foes.'}");
        buf.append("text{text=''}");
        buf.append("text{text='We hope you see the possibilities in this, and return with a positive answer'}");
        buf.append("text{text=''}");
        buf.append("text{type='italic';text=\"" + sender.getName() + ", " + role.getName() + ", " + senderVillage
          .getName() + "\"}");
        
        buf.append("radio{ group='join'; id='accept';text='Accept'}");
        buf.append("radio{ group='join'; id='decline';text='Decline';selected='true'}");
        
        buf.append(createAnswerButton2());
        getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
      }
    }
    catch (NoSuchCreatureException nsc)
    {
      logger.log(Level.WARNING, nsc.getMessage(), nsc);
    }
    catch (NoSuchPlayerException nsp)
    {
      logger.log(Level.WARNING, nsp.getMessage(), nsp);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\AllianceQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */