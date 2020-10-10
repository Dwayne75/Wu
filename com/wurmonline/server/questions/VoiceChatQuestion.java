package com.wurmonline.server.questions;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.MonetaryConstants;
import java.util.Properties;

public class VoiceChatQuestion
  extends Question
  implements TimeConstants, MonetaryConstants
{
  public VoiceChatQuestion(Creature aResponder)
  {
    super(aResponder, "Voice chat options", "Select voice chat subscription", 95, aResponder.getWurmId());
  }
  
  public void answer(Properties aAnswers) {}
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("text{text=\"The Ericsson ingame voice chat service is being shut down as of 2013-01-31 due to low interest. We can no longer offer these packages.\"};text{text=\"\"}");
    
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\VoiceChatQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */