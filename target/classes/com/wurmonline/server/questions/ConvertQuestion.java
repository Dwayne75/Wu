package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import java.util.Properties;

public final class ConvertQuestion
  extends Question
{
  private final Item holyItem;
  private float skillcounter = 0.0F;
  
  public ConvertQuestion(Creature aResponder, String aTitle, String aQuestion, long aAsker, Item aHolyItem)
  {
    super(aResponder, aTitle, aQuestion, 28, aAsker);
    this.holyItem = aHolyItem;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseConvertQuestion(this);
  }
  
  public void sendQuestion()
  {
    try
    {
      Creature asker = Server.getInstance().getCreature(this.target);
      Deity deity = asker.getDeity();
      
      StringBuilder buf = new StringBuilder();
      buf.append(getBmlHeader());
      if (!QuestionParser.doesKingdomTemplateAcceptDeity(getResponder().getKingdomTemplateId(), deity))
      {
        buf.append("text{text='" + getResponder().getKingdomName() + " would never accept a follower of " + deity.name + ".'}");
        buf.append("text{text=''}");
      }
      else if (deity != getResponder().getDeity())
      {
        buf.append("text{text='" + asker.getName() + " asks if you wish to become a follower of " + deity.name + ".'}");
        buf.append("text{text=''}");
        
        buf.append("text{text=''}");
        if (getResponder().getDeity() != null) {
          buf.append("text{type='bold';text='If you answer yes, your faith and all your abilities granted by " + 
            getResponder().getDeity().name + " will be lost!'}");
        }
        if (!Servers.localServer.PVPSERVER) {
          buf.append("text{type='bold';text='Warning: If you transfer to Chaos and are already a member of a WL based kingdom, your faith and all your abilities granted by " + deity.name + " will be lost!'}");
        }
        buf.append("text{type='italic';text='Do you want to become a follower of " + deity.name + "?'}");
        buf.append("text{text=''}");
        
        buf.append("radio{ group='conv'; id='true';text='Yes'}");
        buf.append("radio{ group='conv'; id='false';text='No';selected='true'}");
      }
      else
      {
        buf.append("text{text='You are already a follower of " + deity.name + ".'}");
      }
      buf.append(createAnswerButton2());
      getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
    catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
  }
  
  public float getSkillcounter()
  {
    return this.skillcounter;
  }
  
  public void setSkillcounter(float aSkillcounter)
  {
    this.skillcounter = aSkillcounter;
  }
  
  Item getHolyItem()
  {
    return this.holyItem;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\ConvertQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */