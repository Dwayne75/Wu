package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public class KosWarningInfo
  extends Question
{
  private final Village village;
  
  public KosWarningInfo(Creature aResponder, long aTarget, Village vill)
  {
    super(aResponder, "Kos Warning", "You have been put on the KOS list", 97, -10L);
    this.windowSizeX = 300;
    this.windowSizeY = 300;
    this.village = vill;
  }
  
  public void answer(Properties answers)
  {
    String val = answers.getProperty("okaycb");
    if (val != null) {
      if (val.equals("true")) {
        ((Player)getResponder()).disableKosPopups(this.village.getId());
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("text{text=\"\"}");
    buf.append("text{text=\"You have been deemed a criminal by " + this.village.getName() + ".\"}");
    buf.append("text{text=\"This means that you have to leave " + this.village.getName() + " within 2 minutes.\"}");
    buf.append("text{text=\"\"}");
    buf.append("text{text=\"If you fail to leave the area during this time you will be killed on sight by its guards.\"}");
    buf.append("text{text=\"\"}");
    buf
      .append("checkbox{id='okaycb';selected='false';text=\"I do not want to receive these warnings from " + this.village
      .getName() + " any more until server restart.\"}");
    buf.append(createOkAnswerButton());
    getResponder().getCommunicator().sendBml(this.windowSizeX, this.windowSizeY, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\KosWarningInfo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */