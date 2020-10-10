package com.wurmonline.server.questions;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.util.Properties;

public final class AppointmentsQuestion
  extends Question
{
  public AppointmentsQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 63, aTarget);
  }
  
  public void answer(Properties answers)
  {
    King k = King.getKing(getResponder().getKingdomId());
    if ((k != null) && (k.kingid == getResponder().getWurmId()))
    {
      Appointments a = Appointments.getAppointments(k.era);
      if (a != null) {
        addAppointments(a, k, answers);
      }
    }
  }
  
  public void addAppointments(Appointments a, King k, Properties answers)
  {
    for (int x = 0; x < a.availableOrders.length; x++)
    {
      String val = answers.getProperty("order" + x);
      if ((val != null) && (val.length() > 0))
      {
        Player p = Players.getInstance().getPlayerOrNull(LoginHandler.raiseFirstLetter(val));
        if (p == null) {
          getResponder().getCommunicator().sendNormalServerMessage("There is no person with the name " + val + " present in your kingdom.");
        } else {
          p.addAppointment(a.getAppointment(x + 30), getResponder());
        }
      }
    }
    for (int x = 0; x < a.availableTitles.length; x++)
    {
      String val = answers.getProperty("title" + x);
      if ((val != null) && (val.length() > 0))
      {
        Player p = Players.getInstance().getPlayerOrNull(LoginHandler.raiseFirstLetter(val));
        if (p == null) {
          getResponder().getCommunicator().sendNormalServerMessage("There is no person with the name " + val + " present in your kingdom.");
        } else {
          p.addAppointment(a.getAppointment(x), getResponder());
        }
      }
    }
    for (int x = 0; x < a.officials.length; x++)
    {
      String val = answers.getProperty("official" + x);
      if ((val == null) || (val.length() <= 0))
      {
        Appointment app = a.getAppointment(x + 1500);
        if ((app != null) && (a.officials[x] > 0L))
        {
          Player oldp = Players.getInstance().getPlayerOrNull(a.officials[x]);
          if (oldp != null)
          {
            oldp.getCommunicator().sendNormalServerMessage("You are hereby notified that you have been removed of the office as " + app
            
              .getNameForGender(oldp.getSex()) + ".", (byte)2);
          }
          else
          {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(a.officials[x]);
            if (pinf != null) {
              getResponder().getCommunicator().sendNormalServerMessage("Failed to notify " + pinf
                .getName() + " that they have been removed from the office of " + app
                .getNameForGender((byte)0) + ".", (byte)3);
            }
          }
          getResponder().getCommunicator().sendNormalServerMessage("You vacate the office of " + app
            .getNameForGender((byte)0) + ".", (byte)2);
          a.setOfficial(x + 1500, 0L);
        }
      }
      else if (val.compareToIgnoreCase(PlayerInfoFactory.getPlayerName(a.officials[x])) != 0)
      {
        Player p = Players.getInstance().getPlayerOrNull(LoginHandler.raiseFirstLetter(val));
        if (p == null) {
          getResponder().getCommunicator().sendNormalServerMessage("There is no person with the name " + val + " present in your kingdom.");
        } else {
          p.addAppointment(a.getAppointment(x + 1500), getResponder());
        }
      }
    }
  }
  
  private void addTitleStrings(Appointments a, King k, StringBuilder buf)
  {
    buf.append("text{type='italic';text='Titles'}");
    for (int x = 0; x < a.availableTitles.length; x++)
    {
      String key = "title" + x;
      if (a.getAvailTitlesForId(x) > 0)
      {
        Appointment app = a.getAppointment(x);
        if (app != null) {
          buf.append("harray{label{text='" + app.getNameForGender((byte)0) + " (" + a.getAvailTitlesForId(x) + ")'}};input{id='" + key + "'; maxchars='40'; text=''}");
        }
      }
    }
    buf.append("text{text=''}");
  }
  
  private void addOrderStrings(Appointments a, King k, StringBuilder buf)
  {
    buf.append("text{type='italic';text='Orders and decorations'}");
    for (int x = 0; x < a.availableOrders.length; x++)
    {
      String key = "order" + x;
      if (a.getAvailOrdersForId(x + 30) > 0)
      {
        Appointment app = a.getAppointment(x + 30);
        if (app != null) {
          buf.append("harray{label{text='" + app.getNameForGender((byte)0) + " (" + a
            .getAvailOrdersForId(x + 30) + ")'}};input{id='" + key + "'; maxchars='40'; text=''}");
        }
      }
    }
    buf.append("text{text=''}");
  }
  
  private void addOfficeStrings(Appointments a, King k, StringBuilder buf)
  {
    buf.append("text{type='italic';text='Offices. Note: You can only set these once per week and only to players who are online.'}");
    for (int x = 0; x < a.officials.length; x++)
    {
      String key = "official" + x;
      String oldval = "";
      long current = a.getOfficialForId(x + 1500);
      if (current > 0L)
      {
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(current);
        if (pinf != null) {
          oldval = pinf.getName();
        }
      }
      Appointment app = a.getAppointment(x + 1500);
      if (app != null)
      {
        String set = "(available)";
        if (a.isOfficeSet(x + 1500)) {
          set = "(not available)";
        }
        String aname = app.getNameForGender((byte)0);
        if ((getResponder().getSex() == 0) && (app.getId() == 1507)) {
          aname = app.getNameForGender((byte)1);
        }
        buf.append("harray{label{text='" + aname + " " + set + "'}};input{id='" + key + "'; maxchars='40'; text='" + oldval + "'}");
      }
    }
    buf.append("text{text=''}");
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("header{text='Kingdom appointments:'}text{text=''}");
    King k = King.getKing(getResponder().getKingdomId());
    if ((k != null) && (k.kingid == getResponder().getWurmId()))
    {
      Appointments a = Appointments.getAppointments(k.era);
      if (a == null) {
        return;
      }
      long timeLeft = a.getResetTimeRemaining();
      if (timeLeft <= 0L) {
        buf.append("text{text='Titles and orders will refresh shortly.'}");
      } else {
        buf.append("text{text='Titles and orders will refresh in " + Server.getTimeFor(timeLeft) + ".'}");
      }
      buf.append("text{text=''}");
      addTitleStrings(a, k, buf);
      addOrderStrings(a, k, buf);
      addOfficeStrings(a, k, buf);
    }
    else
    {
      buf.append("text{text='You are not the current ruler.'}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(600, 600, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\AppointmentsQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */