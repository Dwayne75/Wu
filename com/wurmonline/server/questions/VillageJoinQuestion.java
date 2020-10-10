package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.villages.Village;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageJoinQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(VillageJoinQuestion.class.getName());
  private final Creature invited;
  
  public VillageJoinQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
    throws NoSuchCreatureException, NoSuchPlayerException
  {
    super(aResponder, aTitle, aQuestion, 11, aTarget);
    this.invited = Server.getInstance().getCreature(aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseVillageJoinQuestion(this);
  }
  
  public Creature getInvited()
  {
    return this.invited;
  }
  
  public void sendQuestion()
  {
    Village village = getResponder().getCitizenVillage();
    if (village != null)
    {
      StringBuilder buf = new StringBuilder();
      buf.append(getBmlHeader());
      buf.append("text{type=\"bold\";text=\"Joining settlement " + village.getName() + ":\"}");
      buf.append("text{text=\"You have been invited by " + getResponder().getName() + " to join " + village.getName() + ". \"}");
      if ((getInvited().isPlayer()) && (getInvited().mayChangeVillageInMillis() > 0L))
      {
        buf.append("text{text=\"You may not change settlement in " + 
          Server.getTimeFor(getInvited().mayChangeVillageInMillis()) + ". \"}");
      }
      else
      {
        Village currvill = getInvited().getCitizenVillage();
        if (currvill != null) {
          buf.append("text{text=\"Your " + currvill.getName() + " citizenship will be revoked. \"}");
        } else {
          buf.append("text{text=\"You are currently not citizen in any settlement. \"}");
        }
        if (village.isDemocracy()) {
          buf.append("text{text=\"" + village
            .getName() + " is a democracy. This means your citizenship cannot be revoked by any city officials such as the mayor. \"}");
        } else {
          buf.append("text{text=\"" + village
            .getName() + " is a non-democracy. This means your citizenship can be revoked by any city officials such as the mayor. \"}");
        }
        buf.append("text{text=\"Do you want to join " + village.getName() + "?\"}");
        
        buf.append("radio{ group=\"join\"; id=\"true\";text=\"Yes\"}");
        buf.append("radio{ group=\"join\"; id=\"false\";text=\"No\";selected=\"true\"}");
      }
      buf.append(createAnswerButton2());
      getInvited().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
    else
    {
      logger.log(Level.WARNING, getResponder().getName() + " tried to invite to null settlement!");
      getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that invitation. Please contact administration.");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\VillageJoinQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */