package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.utils.BMLBuilder;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;

public class AffinityQuestion
  extends Question
{
  private final Item targetItem;
  private String[] allOptions;
  
  public AffinityQuestion(Creature aResponder, Item target)
  {
    super(aResponder, "Claim Affinity Token", "Which affinity should you gain?", 155, target.getWurmId());
    this.targetItem = target;
  }
  
  public void answer(Properties answers)
  {
    System.out.println("??");
    
    boolean accepted = Boolean.parseBoolean(answers.getProperty("send"));
    if ((this.targetItem == null) || (this.targetItem.deleted))
    {
      getResponder().getCommunicator().sendNormalServerMessage("Something went wrong when claiming this token.");
      return;
    }
    if (accepted)
    {
      int ddVal = Integer.parseInt(answers.getProperty("affdrop"));
      if (ddVal == 0)
      {
        getResponder().getCommunicator().sendNormalServerMessage("You decide against gaining a new skill affinity.");
        return;
      }
      int skillNum = SkillSystem.getSkillByName(this.allOptions[ddVal]);
      Skill s = getResponder().getSkills().getSkillOrLearn(skillNum);
      if (s.affinity >= 5)
      {
        getResponder().getCommunicator().sendNormalServerMessage("You cannot gain any more affinities in " + s.getName() + ".");
        return;
      }
      Affinities.setAffinity(getResponder().getWurmId(), skillNum, s.affinity + 1, false);
      Items.destroyItem(this.targetItem.getWurmId());
      getResponder().getCommunicator().sendNormalServerMessage("You successfully gain a new affinity in " + s.getName() + ".");
    }
    else
    {
      getResponder().getCommunicator().sendNormalServerMessage("You decide against gaining a new skill affinity.");
      return;
    }
  }
  
  public void sendQuestion()
  {
    ArrayList<String> options = new ArrayList();
    options.add("None");
    for (Skill s : getResponder().getSkills().getSkills()) {
      if (s.affinity < 5) {
        options.add(s.getName());
      }
    }
    this.allOptions = new String[options.size()];
    for (int i = 0; i < this.allOptions.length; i++) {
      this.allOptions[i] = ((String)options.get(i));
    }
    BMLBuilder toSend = BMLBuilder.createBMLBorderPanel(null, 
    
      BMLBuilder.createHorizArrayNode(false)
      .addPassthrough("id", Integer.toString(getId()))
      .addLabel(""), 
      BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false)
      .addText("\r\nChoose an affinity from the list below:\r\n\r\n", null, null, null, 200, 50)
      .addDropdown("affdrop", "0", this.allOptions)), null, 
      
      BMLBuilder.createCenteredNode(BMLBuilder.createHorizArrayNode(false)
      .addButton("close", "Cancel", 80, 20, true)
      .addLabel("", null, null, null, 20, 20)
      .addButton("send", "Accept", 80, 20, true)));
    
    getResponder().getCommunicator().sendBml(270, 150, true, false, toSend.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\AffinityQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */