package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import java.util.Properties;

public class ChangeStaffSkillQuestion
  extends Question
{
  public ChangeStaffSkillQuestion(Creature aResponder)
  {
    super(aResponder, "Switch skills", "Do you want to switch your spear and staff skills?", 110, aResponder
      .getWurmId());
  }
  
  public void answer(Properties answers)
  {
    String key = "rd";
    String val = answers.getProperty("rd");
    if (Boolean.parseBoolean(val))
    {
      if ((getResponder().hasFlag(11)) && (getResponder().getPower() <= 0))
      {
        getResponder().getCommunicator().sendNormalServerMessage("You have already switched those skills.");
        
        return;
      }
      Skill staff = null;
      try
      {
        staff = getResponder().getSkills().getSkill(10090);
      }
      catch (NoSuchSkillException nss)
      {
        staff = getResponder().getSkills().learn(10090, 1.0F);
      }
      Skill spear = null;
      try
      {
        spear = getResponder().getSkills().getSkill(10088);
      }
      catch (NoSuchSkillException nsss)
      {
        spear = getResponder().getSkills().learn(10088, 1.0F);
      }
      if ((spear != null) && (staff != null))
      {
        getResponder().getSkills().switchSkillNumbers(spear, staff);
        
        getResponder().setFlag(11, true);
      }
      else
      {
        getResponder().getCommunicator().sendNormalServerMessage("You lack one of the skills.");
      }
    }
    else
    {
      getResponder().getCommunicator().sendNormalServerMessage("You decide not to switch those skills.");
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    
    buf.append("text{text='Steel Spears had the stats Steel Staff should have had so you have the option to switch those skills once.'}");
    buf.append("text{text='Do you wish to switch your Staff skill with your Long Spear skill?'}");
    buf.append("radio{ group='rd'; id='true';text='Yes'}");
    buf.append("radio{ group='rd'; id='false';text='No';selected='true'}");
    
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\ChangeStaffSkillQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */