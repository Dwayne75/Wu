package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangeMedPathQuestion
  extends Question
{
  private final Cultist cultist;
  
  public ChangeMedPathQuestion(Creature aResponder, Cultist cultist, Item target)
  {
    super(aResponder, "Meditation Path", aResponder.getName() + " Meditation Path", 722, target.getWurmId());
    this.cultist = cultist;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    if (this.type == 0)
    {
      logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      return;
    }
    if (this.type == 722) {
      if (this.cultist.getPath() == 4)
      {
        String prop = answers.getProperty("newcult");
        if (prop != null)
        {
          byte cultId = Byte.parseByte(prop);
          this.cultist.setPath(cultId);
          
          getResponder().getCommunicator().sendNormalServerMessage("You are now " + this.cultist.getCultistTitle());
          
          getResponder().refreshVisible();
          getResponder().getCommunicator().sendOwnTitles();
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("Your path was not changed.");
        }
      }
      else
      {
        getResponder().getCommunicator().sendNormalServerMessage("You are not currently on the correct path to be able to change.");
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    if (this.cultist.getPath() == 4)
    {
      buf.append("text{type=\"bold\";text=\"Choose which path you want to change to. This cannot be undone. This change is only available once, and only from the path of insanity.\"}");
      
      int cId = this.cultist.getPath();
      for (int i = 1; i < 6; i++) {
        if (i != cId) {
          buf.append("radio{ group='newcult'; id='" + i + "'; text='" + Cults.getPathNameFor((byte)i) + "'}");
        }
      }
      buf.append(createAnswerButton2());
      getResponder().getCommunicator().sendBml(350, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\ChangeMedPathQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */