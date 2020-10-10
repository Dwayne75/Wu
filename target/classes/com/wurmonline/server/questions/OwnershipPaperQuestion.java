package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.Properties;

public class OwnershipPaperQuestion
  extends Question
{
  public OwnershipPaperQuestion(Creature aResponder, Item aTarget)
  {
    super(aResponder, "Ownership Papers", "", 123, aTarget.getWurmId());
  }
  
  public void answer(Properties aAnswers) {}
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeaderWithScroll());
    
    buf.append("label{type=\"bold\";text=\"I herby declare that YYY is the current owner of XXX.\"}");
    buf.append("text{type=\"bolditalic\";text=\"lock info?\"}");
    
    buf.append("label{text=\"These papers can be traded to another player to transfer the ownership of XXX\"}");
    buf.append("text{text=\"\"}");
    buf.append("text{type=\"bold\";text=\"The King\"}");
    
    buf.append(createAnswerButton2("Close"));
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\OwnershipPaperQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */