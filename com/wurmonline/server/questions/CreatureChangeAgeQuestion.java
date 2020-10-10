package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import java.io.IOException;
import java.util.Properties;

public final class CreatureChangeAgeQuestion
  extends Question
{
  public CreatureChangeAgeQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 153, aTarget);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    int width = 150;
    int height = 150;
    try
    {
      Creature target = Creatures.getInstance().getCreature(this.target);
      int age = target.getStatus().age;
      buf.append("harray{input{id='newAge'; maxchars='3'; text='").append(age).append("'}label{text='Age'}}");
    }
    catch (NoSuchCreatureException ex)
    {
      ex.printStackTrace();
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(width, height, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    init(this);
  }
  
  private void init(CreatureChangeAgeQuestion question)
  {
    Creature responder = question.getResponder();
    int newAge = 0;
    long target = question.getTarget();
    try
    {
      Creature creature = Creatures.getInstance().getCreature(target);
      String age = question.getAnswer().getProperty("newAge");
      newAge = Integer.parseInt(age);
      ((DbCreatureStatus)creature.getStatus()).updateAge(newAge);
      
      creature.getStatus().lastPolledAge = 0L;
      creature.pollAge();
      
      creature.refreshVisible();
    }
    catch (NoSuchCreatureException|IOException ex)
    {
      ex.printStackTrace();
    }
    responder.getCommunicator().sendNormalServerMessage("Age = " + newAge + ".");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\CreatureChangeAgeQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */