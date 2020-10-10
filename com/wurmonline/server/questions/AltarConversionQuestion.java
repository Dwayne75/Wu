package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import java.util.Properties;

public final class AltarConversionQuestion
  extends Question
{
  private final Deity deity;
  
  public AltarConversionQuestion(Creature aResponder, String aTitle, String aQuestion, long aAltar, Deity aDeity)
  {
    super(aResponder, aTitle, aQuestion, 31, aAltar);
    this.deity = aDeity;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseAltarConvertQuestion(this);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("text{text='The inscription talks about " + this.deity.name + ".'}");
    buf.append("text{text=''}");
    for (int x = 0; x < this.deity.altarConvertText1.length; x++)
    {
      buf.append("text{text='" + this.deity.altarConvertText1[x] + "'}");
      buf.append("text{text=''}");
    }
    if ((getResponder().isChampion()) && (getResponder().getDeity() != null))
    {
      buf.append("text{text='You are already the devoted follower of " + getResponder().getDeity().name + ". " + this.deity.name + " would never accept you.'}");
      
      buf.append("text{text=''}");
    }
    else if (!QuestionParser.doesKingdomTemplateAcceptDeity(getResponder().getKingdomTemplateId(), this.deity))
    {
      buf.append("text{text='" + getResponder().getKingdomName() + " would never accept a follower of " + this.deity.name + ".'}");
      buf.append("text{text=''}");
    }
    else if ((getResponder().getDeity() == null) || (getResponder().getDeity() != this.deity))
    {
      buf.append("text{type='italic';text='Do you want to become a follower of " + this.deity.name + "?'}");
      buf.append("text{text=''}");
      if (getResponder().getDeity() != null) {
        buf.append("text{type='bold';text='If you answer yes, your faith and all your abilities granted by " + 
          getResponder().getDeity().name + " will be lost!'}");
      }
      if (!Servers.localServer.PVPSERVER) {
        buf.append("text{type='bold';text='Warning: If you transfer to Chaos and are already a member of a WL based kingdom, your faith and all your abilities granted by " + this.deity.name + " will be lost!'}");
      }
      buf.append("text{text=''}");
      
      buf.append("radio{ group='conv'; id='true';text='Accept'}");
      buf.append("radio{ group='conv'; id='false';text='Decline';selected='true'}");
    }
    else
    {
      buf.append("text{text='You are already a follower of " + getResponder().getDeity().name + ".'}");
      buf.append("text{text=''}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  Deity getDeity()
  {
    return this.deity;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\AltarConversionQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */