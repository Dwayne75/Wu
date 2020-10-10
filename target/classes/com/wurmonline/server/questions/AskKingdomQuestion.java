package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public final class AskKingdomQuestion
  extends Question
{
  public AskKingdomQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 38, aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseAskKingdomQuestion(this);
  }
  
  public void sendQuestion()
  {
    try
    {
      Creature asker = Server.getInstance().getCreature(this.target);
      String kname = Kingdoms.getNameFor(asker.getKingdomId());
      StringBuilder buf = new StringBuilder();
      buf.append(getBmlHeader());
      if (asker.getKingdomId() != getResponder().getKingdomId())
      {
        buf.append("text{text='" + asker.getName() + " asks if you wish to join " + kname + ".'}");
        if (getResponder().getAppointments() != 0L) {
          buf.append("text{type='bold';text='If you accept, you will loose all your royal orders, titles and appointments!'}");
        }
        if ((!Servers.localServer.HOMESERVER) && (Kingdoms.getKingdom(asker.getKingdomId()).isCustomKingdom()) && 
          (getResponder().getCitizenVillage() != null)) {
          if (getResponder().getCitizenVillage().getMayor().wurmId == getResponder().getWurmId()) {
            buf.append("text{type='bold';text='If you accept, your whole village will convert!'}");
          }
        }
        buf.append("text{text='Do you want to join " + kname + "?'}");
        buf.append("radio{ group='conv'; id='true';text='Yes'}");
        buf.append("radio{ group='conv'; id='false';text='No';selected='true'}");
      }
      else
      {
        buf.append("text{text='You are already in " + kname + ".'}");
      }
      buf.append(createAnswerButton2());
      getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
      asker.getCommunicator().sendNormalServerMessage("You ask " + getResponder().getName() + " to join " + kname + ".");
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\AskKingdomQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */