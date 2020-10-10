package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SuicideQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(SuicideQuestion.class.getName());
  
  public SuicideQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 48, aTarget);
  }
  
  public void answer(Properties answers)
  {
    String key = "suicide";
    String val = answers.getProperty("suicide");
    if ((val != null) && (val.equals("true")))
    {
      if (getResponder().isDead()) {
        getResponder().getCommunicator().sendNormalServerMessage("You are already dead.");
      }
      if (getResponder().isTeleporting())
      {
        getResponder().getCommunicator().sendAlertServerMessage("You are too confused to kill yourself right now.");
      }
      else if (getResponder().getBattle() != null)
      {
        getResponder().getCommunicator().sendAlertServerMessage("You are too full of adrenaline from the battle to kill yourself right now.");
      }
      else
      {
        if (((Player)getResponder()).getSaveFile().realdeath > 2)
        {
          getResponder().getCommunicator().sendAlertServerMessage("You cannot force yourself to suicide this time.");
          return;
        }
        logger.log(Level.INFO, getResponder().getName() + " SUICIDE " + getResponder().getName() + " at coords: " + 
          getResponder().getTileX() + ", " + getResponder().getTileY());
        getResponder()
          .getCommunicator()
          .sendAlertServerMessage("Using an old Kelatchka Nomad-trick you once heard of, you swallow your tongue and quickly fall down dead.");
        
        Server.getInstance().broadCastAction(
          getResponder().getName() + " falls down dead, having swallowed " + getResponder().getHisHerItsString() + " tongue.", 
          getResponder(), 5);
        ((Player)getResponder()).suiciding = true;
        ((Player)getResponder()).lastSuicide = System.currentTimeMillis();
        
        getResponder().die(false, "Suicide");
      }
    }
    else
    {
      getResponder().getCommunicator().sendNormalServerMessage("You decide not to commit suicide for now.");
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    
    buf
      .append("text{text='Committing suicide may be helpful when you are stuck in a mine or fenced in. At low levels you will quickly regain the skill you lose when doing so, and more experienced players should know better than to risk becoming stuck anyways.'}");
    buf.append("text{text='If you are stuck in a fence, using the command /stuck may help as well.'}");
    buf
      .append("text{text='You may not commit suicide when you are already dead, teleporting, or have just been engaged in battle.'}");
    boolean lastDeath = false;
    try
    {
      Skill body = getResponder().getSkills().getSkill(102);
      if ((getResponder().isPlayer()) && (!getResponder().isPaying())) {
        if (body.getKnowledge() < 20.0D) {
          if ((body.minimum - body.getKnowledge() > 0.05000000074505806D) && (body.minimum - body.getKnowledge() <= 0.06D)) {
            lastDeath = true;
          }
        }
      }
    }
    catch (NoSuchSkillException nss)
    {
      getResponder().getSkills().learn(102, 1.0F);
      logger.log(Level.WARNING, getResponder().getName() + " learnt body strength.");
    }
    if (lastDeath)
    {
      buf.append("text{type='italic';text='You may not suicide now. You need more strength.'}");
    }
    else
    {
      buf.append("text{type='italic';text='Do you wish to commit suicide?'}");
      buf.append("text{text=''}");
      
      buf.append("radio{ group='suicide'; id='true';text='Yes'}");
      buf.append("radio{ group='suicide'; id='false';text='No';selected='true'}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\SuicideQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */