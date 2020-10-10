package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import java.util.Properties;

public final class NewKingQuestion
  extends Question
{
  public NewKingQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 69, aTarget);
  }
  
  public void answer(Properties answers) {}
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    
    buf.append("text{type='bold';text='Welcome as the " + 
      King.getRulerTitle(getResponder().isNotFemale(), getResponder().getKingdomId()) + " of " + 
      Kingdoms.getNameFor(getResponder().getKingdomId()) + "!'}");
    buf.append("text{type='';text='There are some things you should be aware of:'}");
    buf.append("text{type='';text='As the " + 
      King.getRulerTitle(getResponder().isNotFemale(), getResponder().getKingdomId()) + ", you have one general goal, apart from your personal ones.'}");
    
    buf.append("text{type='';text='This is to gain land.'}");
    buf.append("text{type='';text='The more land you gain, the better your public title will become.'}");
    buf.append("text{type='';text='The more land you control, the more and finer titles and orders you may bestow upon your subjects.'}");
    buf.append("text{type='';text='Therefor a good idea is to reward those who gain land for you.'}");
    buf.append("text{type='';text='Land also has the benefit of yielding more coins to traders from the pool.'}");
    buf.append("text{text=''}");
    buf.append("text{type='';text='Good luck in your rulership!'}");
    buf.append("text{text=''}");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(400, 250, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\NewKingQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */