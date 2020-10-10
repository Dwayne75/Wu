package com.wurmonline.server.questions;

import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class ReimbursementQuestion
  extends Question
{
  private String[] nameArr = new String[0];
  
  public ReimbursementQuestion(Creature aResponder, long aTarget)
  {
    super(aResponder, "Reimbursements", "These are your available reimbursements:", 50, aTarget);
  }
  
  public void answer(Properties answers)
  {
    String key = "";
    String value = "";
    for (int x = 0; x < this.nameArr.length; x++)
    {
      int days = 0;
      int trinkets = 0;
      int silver = 0;
      boolean boktitle = false;
      boolean mbok = false;
      key = "silver" + this.nameArr[x];
      value = answers.getProperty(key);
      if (value != null) {
        try
        {
          silver = Integer.parseInt(value);
        }
        catch (Exception ex)
        {
          getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of silver for " + this.nameArr[x]);
          return;
        }
      }
      key = "days" + this.nameArr[x];
      value = answers.getProperty(key);
      if (value != null) {
        try
        {
          days = Integer.parseInt(value);
        }
        catch (Exception ex)
        {
          getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of days for " + this.nameArr[x]);
          return;
        }
      }
      key = "trinket" + this.nameArr[x];
      value = answers.getProperty(key);
      if (value != null) {
        try
        {
          trinkets = Integer.parseInt(value);
        }
        catch (Exception ex)
        {
          getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of trinkets for " + this.nameArr[x]);
          return;
        }
      }
      key = "mbok" + this.nameArr[x];
      value = answers.getProperty(key);
      if (value != null) {
        try
        {
          boktitle = Boolean.parseBoolean(value);
          if (boktitle) {
            mbok = true;
          }
        }
        catch (Exception ex)
        {
          getResponder().getCommunicator().sendAlertServerMessage("Unable to parse the MBoK/Title answer for " + this.nameArr[x]);
          return;
        }
      }
      if (!boktitle)
      {
        key = "bok" + this.nameArr[x];
        value = answers.getProperty(key);
        if (value != null) {
          try
          {
            boktitle = Boolean.parseBoolean(value);
          }
          catch (Exception ex)
          {
            getResponder().getCommunicator().sendAlertServerMessage("Unable to parse the BoK/Title answer for " + this.nameArr[x]);
            return;
          }
        }
      }
      if ((days > 0) || (trinkets > 0) || (silver > 0) || (boktitle)) {
        if ((days < 0) || (trinkets < 0) || (silver < 0))
        {
          getResponder().getCommunicator().sendAlertServerMessage("Less than 0 value entered for " + this.nameArr[x]);
        }
        else
        {
          LoginServerWebConnection lsw = new LoginServerWebConnection();
          getResponder().getCommunicator().sendNormalServerMessage(lsw
            .withDraw((Player)getResponder(), this.nameArr[x], ((Player)getResponder()).getSaveFile().emailAddress, trinkets, silver, boktitle, mbok, days));
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    LoginServerWebConnection lsw = new LoginServerWebConnection();
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    String s = lsw.getReimburseInfo((Player)getResponder());
    if (s.equals("text{text='You have no reimbursements pending.'}"))
    {
      ((Player)getResponder()).getSaveFile().setHasNoReimbursementLeft(true);
    }
    else
    {
      String ttext = s;
      String newName = "";
      Set<String> names = new HashSet();
      boolean keepGoing = true;
      while (keepGoing)
      {
        newName = getNextName(ttext);
        if (newName.equals(""))
        {
          keepGoing = false;
        }
        else
        {
          names.add(newName);
          
          ttext = ttext.substring(ttext.indexOf(" - '}") + 5, ttext.length());
        }
      }
      this.nameArr = ((String[])names.toArray(new String[names.size()]));
    }
    buf.append(s);
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(400, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private String getNextName(String ttext)
  {
    int place = ttext.indexOf("Name=");
    if (place > 0) {
      return ttext.substring(place + 5, ttext.indexOf(" - '}"));
    }
    return "";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\ReimbursementQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */