package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.shared.util.StringUtilities;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SummonSoulQuestion
  extends Question
{
  private boolean properlySent = false;
  private static final Logger logger = Logger.getLogger(SummonSoulQuestion.class.getName());
  
  public SummonSoulQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 79, aTarget);
  }
  
  public void answer(Properties aAnswers)
  {
    if (!this.properlySent) {
      return;
    }
    String name = aAnswers.getProperty("name");
    Creature soul = null;
    if ((name != null) && (name.length() > 1)) {
      soul = acquireSoul(StringUtilities.raiseFirstLetter(name));
    }
    if ((soul == null) || (soul.getPower() > getResponder().getPower()))
    {
      getResponder().getCommunicator().sendNormalServerMessage("No such soul found.");
    }
    else
    {
      SummonSoulAcceptQuestion ssaq = new SummonSoulAcceptQuestion(soul, "Accept Summon?", "Would you like to accept a summon from " + getResponder().getName() + "?", getResponder().getWurmId(), getResponder());
      ssaq.sendQuestion();
    }
  }
  
  private static Creature acquireSoul(String name)
  {
    PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
    if ((pinf != null) && (pinf.loaded)) {
      try
      {
        return Server.getInstance().getCreature(pinf.wurmId);
      }
      catch (NoSuchPlayerException|NoSuchCreatureException ex)
      {
        logger.log(Level.WARNING, ex.getMessage());
      }
    }
    return null;
  }
  
  public void sendQuestion()
  {
    this.properlySent = true;
    
    String sb = getBmlHeader() + "text{text='Which soul do you wish to summon?'};label{text='Name:'};input{id='name';maxchars='40';text=\"\"};" + createAnswerButton2();
    getResponder().getCommunicator().sendBml(300, 300, true, true, sb, 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\SummonSoulQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */