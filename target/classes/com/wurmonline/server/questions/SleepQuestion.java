package com.wurmonline.server.questions;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.structures.NoSuchWallException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SleepQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(SleepQuestion.class.getName());
  
  public SleepQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 47, aTarget);
  }
  
  public void answer(Properties answers)
  {
    String key = "sleep";
    String val = answers.getProperty("sleep");
    if ((val != null) && (val.equals("true"))) {
      try
      {
        getResponder().getCurrentAction();
        getResponder().getCommunicator().sendNormalServerMessage("You are too busy to sleep right now.");
      }
      catch (NoSuchActionException nsa)
      {
        try
        {
          BehaviourDispatcher.action(getResponder(), getResponder().getCommunicator(), -1L, this.target, (short)140);
        }
        catch (FailedException localFailedException) {}catch (NoSuchBehaviourException nsb)
        {
          logger.log(Level.WARNING, nsb.getMessage(), nsb);
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, nsi.getMessage(), nsi);
        }
        catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (NoSuchWallException nsw)
        {
          logger.log(Level.WARNING, nsw.getMessage(), nsw);
        }
      }
    } else {
      getResponder().getCommunicator().sendNormalServerMessage("You decide not to go to sleep right now.");
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    
    buf.append("text{text='Do you want to go to sleep? You will log off Wurm.'}text{text=''}");
    
    buf.append("radio{ group='sleep'; id='true';text='Yes';selected='true'}");
    buf.append("radio{ group='sleep'; id='false';text='No'}");
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\SleepQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */