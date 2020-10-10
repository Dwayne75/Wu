package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public final class PeaceQuestion
  extends Question
{
  private final Creature invited;
  
  public PeaceQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    super(aResponder, aTitle, aQuestion, 30, aTarget);
    this.invited = Server.getInstance().getCreature(aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseVillagePeaceQuestion(this);
  }
  
  public Creature getInvited()
  {
    return this.invited;
  }
  
  public void sendQuestion()
  {
    Village village = getResponder().getCitizenVillage();
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("header{text=\"Peace offer by " + village.getName() + ":\"}");
    buf.append("text{text=\"You have been offered peace by " + getResponder().getName() + " and the village of " + village
      .getName() + ". \"}");
    
    buf.append("text{text='Do you accept?'}");
    buf.append("radio{ group='peace'; id='true';text='Yes'}");
    buf.append("radio{ group='peace'; id='false';text='No';selected='true'}");
    buf.append(createAnswerButton2());
    getInvited().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    getResponder().getCommunicator().sendNormalServerMessage("You send a peace offer to " + getInvited().getName() + ".");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\PeaceQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */