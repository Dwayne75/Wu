package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.OldMission;
import java.util.Properties;

public final class MissionQuestion
  extends Question
{
  private int missionNumber;
  
  public MissionQuestion(int aMissionNum, Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 61, aTarget);
    this.missionNumber = aMissionNum;
  }
  
  public void answer(Properties answers)
  {
    Creature guide = null;
    try
    {
      guide = Server.getInstance().getCreature(this.target);
    }
    catch (NoSuchCreatureException nsc)
    {
      getResponder().getCommunicator().sendNormalServerMessage("Your guide has left!");
      return;
    }
    catch (NoSuchPlayerException nsp)
    {
      getResponder().getCommunicator().sendNormalServerMessage("Your guide has left!");
      return;
    }
    OldMission cm = OldMission.getMission(this.missionNumber, getResponder().getKingdomId());
    boolean ok = false;
    boolean skip = false;
    if (cm.hasCheckBox())
    {
      boolean done = answers.getProperty("check").equals("true");
      if (done)
      {
        skip = true;
        if (this.missionNumber == 9999)
        {
          this.missionNumber = getResponder().getTutorialLevel();
          if (this.missionNumber != 9999) {
            getResponder().getCommunicator().sendNormalServerMessage("You decide to continue following the instructions from the " + guide
              .getName() + ".");
          }
        }
        else
        {
          this.missionNumber += 1;
          getResponder().missionFinished(true, false);
        }
        if (this.missionNumber != 9999)
        {
          OldMission m = OldMission.getMission(this.missionNumber, getResponder().getKingdomId());
          if (m != null)
          {
            MissionQuestion ms = new MissionQuestion(m.number, getResponder(), m.title, m.missionDescription, this.target);
            
            ms.sendQuestion();
          }
        }
        else
        {
          ((Player)getResponder()).setTutorialLevel(9999);
          
          SimplePopup popup = new SimplePopup(getResponder(), "Tutorial done!", "That concludes the tutorial! The " + guide.getName() + " is most pleased. Congratulations and good luck!");
          
          popup.sendQuestion();
        }
      }
      else
      {
        SimplePopup popup = new SimplePopup(getResponder(), "Wait for now.", "You decide to take a pause and maybe come back later.");
        
        popup.sendQuestion();
      }
    }
    else if (answers.getProperty("mission") != null)
    {
      ok = answers.getProperty("mission").equals("do");
      if (ok)
      {
        OldMission m = OldMission.getMission(this.missionNumber, getResponder().getKingdomId());
        if (m != null)
        {
          SimplePopup popup = new SimplePopup(getResponder(), this.title, "You accept the mission.");
          popup.sendQuestion();
        }
        else
        {
          ((Player)getResponder()).setTutorialLevel(9999);
          
          SimplePopup popup = new SimplePopup(getResponder(), "Tutorial done!", "That concludes the tutorial! The " + guide.getName() + " is most pleased. Congratulations and good luck!");
          
          popup.sendQuestion();
        }
      }
    }
    if (answers.getProperty("mission") != null)
    {
      ok = answers.getProperty("mission").equals("wait");
      if ((!skip) && (ok))
      {
        SimplePopup popup = new SimplePopup(getResponder(), "Wait for now.", "You decide to take a pause and maybe come back later.");
        
        popup.sendQuestion();
      }
      ok = answers.getProperty("mission").equals("skip");
      if (ok)
      {
        OldMission m = OldMission.getMission(this.missionNumber + 1, getResponder().getKingdomId());
        if (m != null)
        {
          ((Player)getResponder()).setTutorialLevel(this.missionNumber + 1);
          MissionQuestion ms = new MissionQuestion(m.number, getResponder(), m.title, m.missionDescription, this.target);
          
          ms.sendQuestion();
        }
        else
        {
          ((Player)getResponder()).setTutorialLevel(9999);
          
          SimplePopup popup = new SimplePopup(getResponder(), "Tutorial done!", "That concludes the tutorial! The " + guide.getName() + " is most pleased. Congratulations and good luck!");
          
          popup.sendQuestion();
        }
      }
      ok = answers.getProperty("mission").equals("skipall");
      if (ok)
      {
        ((Player)getResponder()).setTutorialLevel(9999);
        SimplePopup popup = new SimplePopup(getResponder(), "Tutorial done!", "You decide to skip all the missions for now. You may return later. Good luck!");
        
        popup.sendQuestion();
      }
    }
  }
  
  public void sendQuestion()
  {
    Creature guide = null;
    try
    {
      guide = Server.getInstance().getCreature(this.target);
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
    OldMission m = OldMission.getMission(this.missionNumber, getResponder().getKingdomId());
    
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeaderNoQuestion());
    if (guide != null) {
      buf.append("text{text=\"The " + guide.getName() + " looks at you sternly and says:\"}");
    }
    buf.append("text{text=''}");
    buf.append("text{text=\"" + getQuestion() + "\"}");
    buf.append("text{text=''}");
    if (m.missionDescription2.length() > 0)
    {
      buf.append("text{text=\"" + m.missionDescription2 + "\"}");
      buf.append("text{text=''}");
    }
    if (m.missionDescription3.length() > 0)
    {
      buf.append("text{text=\"" + m.missionDescription3 + "\"}");
      buf.append("text{text=''}");
    }
    if (m.hasCheckBox())
    {
      buf.append("checkbox{id='check';selected='false';text=\"" + m.checkBoxString + "\"}");
    }
    else
    {
      buf.append("radio{ group='mission'; id='do';text='I will do this';selected='true'}");
      buf.append("radio{ group='mission'; id='wait';text='I want to wait a while'}");
    }
    buf.append("text{text=''}");
    buf.append("text{type='italic';text='You may see your current instructions by typing /mission in a chat window.'}");
    
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\MissionQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */