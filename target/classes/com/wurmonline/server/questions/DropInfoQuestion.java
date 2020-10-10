package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import java.util.Properties;

public final class DropInfoQuestion
  extends Question
{
  public DropInfoQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 49, aTarget);
  }
  
  public void answer(Properties answers)
  {
    String val = answers.getProperty("okaycb");
    if (val != null) {
      if (val.equals("true")) {
        getResponder().setTheftWarned(true);
      }
    }
    ((Player)getResponder()).setQuestion(null);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("header{text=\"Theft warning:\"}");
    buf.append("text{text=''}");
    buf.append("text{text=\"You are dropping an item.\"}");
    if (!Servers.localServer.PVPSERVER) {
      buf.append("text{text=\"Usually, if you stay within one tile of the item nobody else may pick them up unless you team up with them.\"}");
    }
    buf.append("text{text=\"\"}");
    buf
      .append("text{text=\"Otherwise, if this area is not on a settlement deed it may be stolen. Anyone may pass by and steal this unless you pick it up first. You need to build a house for your things to be protected.\"}");
    
    buf.append("text{text=''}");
    buf
      .append("checkbox{id='okaycb';selected='false';text='I have understood this message and do not need to see it ever again'}");
    
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\DropInfoQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */