package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import java.util.HashSet;
import java.util.Properties;

public class PersonalGoalsListQuestion
  extends Question
{
  public PersonalGoalsListQuestion(Creature aResponder, long aTarget)
  {
    super(aResponder, "Personal Goals", "Personal Goals", 152, aTarget);
  }
  
  public void answer(Properties answers) {}
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    if (getResponder().getPower() >= 4)
    {
      Achievements achs = Achievements.getAchievementObject(getTarget());
      HashSet<AchievementTemplate> goals = (HashSet)Achievements.getPersonalGoals(getTarget(), false);
      HashSet<AchievementTemplate> oldGoals = (HashSet)Achievements.getOldPersonalGoals(getTarget());
      
      buf.append("text{text='Current Personal Goals for WurmId " + getTarget() + "'}");
      buf.append("text{text=''}");
      buf.append("table{rows='" + goals.size() + "';cols='2';");
      for (AchievementTemplate t : goals)
      {
        boolean done = false;
        if (achs.getAchievement(t.getNumber()) != null) {
          done = true;
        }
        buf.append("label{color=\"" + (done ? "20,255,20" : "200,200,200") + "\";text=\"" + t.getName() + "\"};");
        buf.append("label{color=\"" + (done ? "20,255,20" : "200,200,200") + "\";text=\"" + t.getRequirement() + "\"}");
      }
      buf.append("}");
      buf.append("text{text=''}");
      
      buf.append("text{text='Pre June 5 2018 Personal Goals for WurmId " + getTarget() + "'}");
      buf.append("text{text=''}");
      buf.append("table{rows='" + oldGoals.size() + "';cols='2';");
      for (AchievementTemplate t : oldGoals)
      {
        buf.append("label{text=\"" + t.getName() + "\"};");
        buf.append("label{text=\"" + t.getRequirement() + "\"}");
      }
      buf.append("}");
      
      buf.append("}};null;null;}");
      getResponder().getCommunicator().sendBml(300, 600, true, true, buf.toString(), 200, 200, 200, this.title);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\PersonalGoalsListQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */