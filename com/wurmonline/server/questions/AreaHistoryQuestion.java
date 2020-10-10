package com.wurmonline.server.questions;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import java.util.Properties;

public final class AreaHistoryQuestion
  extends Question
  implements TimeConstants
{
  private static final long waittime = 21600000L;
  
  public AreaHistoryQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 41, aTarget);
  }
  
  public void answer(Properties answers)
  {
    String newevent = answers.getProperty("newevent");
    if ((newevent != null) && (newevent.length() > 0))
    {
      Appointments app = King.getCurrentAppointments(getResponder().getKingdomId());
      if (app != null) {
        if (app.getOfficialForId(1510) == getResponder().getWurmId()) {
          if (System.currentTimeMillis() - 21600000L < ((Player)getResponder()).getSaveFile().lastCreatedHistoryEvent)
          {
            getResponder().getCommunicator().sendNormalServerMessage("You need to wait " + 
            
              Server.getTimeFor(21600000L + 
              ((Player)getResponder()).getSaveFile().lastCreatedHistoryEvent - 
              System.currentTimeMillis()) + " before writing history again.");
          }
          else
          {
            newevent = newevent.replace("\"", "'");
            newevent = newevent.replace("\\", "");
            newevent = newevent.replace("/", "");
            newevent = newevent.replace(";", "");
            newevent = newevent.replace("#", "");
            
            getResponder().getCommunicator().sendNormalServerMessage("You write some history.");
            HistoryManager.addHistory(getResponder().getName() + " note:", newevent);
            ((Player)getResponder()).getSaveFile().lastCreatedHistoryEvent = System.currentTimeMillis();
          }
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("header{text='Area Events:'}text{text=''}");
    Appointments app = King.getCurrentAppointments(getResponder().getKingdomId());
    if (app != null) {
      if (app.getOfficialForId(1510) == getResponder().getWurmId())
      {
        buf.append("text{text='Your office allows you to record historic events:'}");
        if (System.currentTimeMillis() - 21600000L < ((Player)getResponder()).getSaveFile().lastCreatedHistoryEvent)
        {
          buf.append("text{text='You need to wait " + 
            Server.getTimeFor(21600000L + ((Player)getResponder()).getSaveFile().lastCreatedHistoryEvent - 
            System.currentTimeMillis()) + " before writing history again.'}");
        }
        else
        {
          buf.append("input{id='newevent';maxchars='200';text=''}");
          buf.append("text{type=\"italic\";text=\"Please note that event texts are governed by game conduct rules so make sure they are not inappropriate.\"}");
        }
      }
    }
    String[] list = HistoryManager.getHistory(200);
    if (list.length > 0) {
      for (int x = 0; x < list.length; x++) {
        buf.append("text{text=\"" + list[x] + "\"}");
      }
    } else {
      buf.append("text{text='No events recorded.'}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\AreaHistoryQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */