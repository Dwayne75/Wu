package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class CultQuestion
  extends Question
  implements TimeConstants
{
  private Cultist cultist;
  private final boolean leavePath;
  private final boolean askStatus;
  private final byte path;
  private static final Logger logger = Logger.getLogger(CultQuestion.class.getName());
  
  public CultQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, @Nullable Cultist _cultist, byte _path, boolean leave, boolean _askStatus)
  {
    super(aResponder, aTitle, aQuestion, 78, aTarget);
    this.cultist = _cultist;
    this.path = _path;
    this.leavePath = leave;
    this.askStatus = _askStatus;
  }
  
  public void answer(Properties aAnswers)
  {
    if (this.askStatus) {
      return;
    }
    String prop = aAnswers.getProperty("quit");
    if (prop != null)
    {
      try
      {
        int num = Integer.parseInt(prop);
        if (num == 1) {
          if (this.cultist == null)
          {
            getResponder().getCommunicator().sendNormalServerMessage("You are not following a philosophical path!");
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("You decide to stop pursuing the insights of " + 
              Cults.getPathNameFor(this.path) + ".");
            try
            {
              this.cultist.deleteCultist();
              if (getResponder().isPlayer()) {
                ((Player)getResponder()).setLastChangedPath(System.currentTimeMillis());
              }
              return;
            }
            catch (IOException iox)
            {
              logger.log(Level.WARNING, getResponder().getName() + ":" + iox.getMessage(), iox);
            }
          }
        }
      }
      catch (NumberFormatException nsf)
      {
        getResponder().getCommunicator().sendNormalServerMessage("The answer you provided was impossible to understand. You are sorry.");
        
        return;
      }
      getResponder().getCommunicator().sendNormalServerMessage("You decide to keep pursuing the insights of " + 
        Cults.getPathNameFor(this.path) + ".");
      return;
    }
    prop = aAnswers.getProperty("answer");
    if (prop != null) {
      try
      {
        int num = Integer.parseInt(prop);
        if (this.cultist == null)
        {
          if (num == 1)
          {
            if (getResponder().isPlayer()) {
              if (System.currentTimeMillis() - ((Player)getResponder()).getLastChangedPath() < 86400000L)
              {
                getResponder().getCommunicator().sendNormalServerMessage("You recently left a cult and need to contemplate the changes for another " + 
                
                  Server.getTimeFor(((Player)getResponder()).getLastChangedPath() + 86400000L - 
                  System.currentTimeMillis()) + " before embarking on a new philosophical journey.");
                
                return;
              }
            }
            getResponder().getCommunicator().sendNormalServerMessage("You decide to start pursuing the insights of " + 
              Cults.getPathNameFor(this.path) + ".");
            this.cultist = new Cultist(getResponder().getWurmId(), this.path);
            
            getResponder().achievement(548);
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("You decide not to follow " + 
              Cults.getPathNameFor(this.path) + ".");
          }
        }
        else if (num == Cults.getCorrectAnswerForNextLevel(this.cultist.getPath(), this.cultist.getLevel()))
        {
          if (this.cultist == null) {
            this.cultist = new Cultist(getResponder().getWurmId(), this.path);
          }
          getResponder().getCommunicator().sendSafeServerMessage(
            Cults.getCorrectAnswerStringForNextLevel(this.path, this.cultist.getLevel()));
          this.cultist.increaseLevel();
          try
          {
            this.cultist.saveCultist(false);
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, "Failed to set " + 
              getResponder().getName() + " to level " + iox.getMessage(), iox);
          }
        }
        else if (this.cultist == null)
        {
          getResponder().getCommunicator().sendNormalServerMessage(
            Cults.getWrongAnswerStringForLevel(this.path, (byte)0));
        }
        else
        {
          this.cultist.failedToLevel();
          getResponder().getCommunicator().sendNormalServerMessage(
            Cults.getWrongAnswerStringForLevel(this.path, this.cultist.getLevel()));
        }
      }
      catch (NumberFormatException nsf)
      {
        getResponder().getCommunicator().sendNormalServerMessage("The answer you provided was impossible to understand. You are sorry.");
        
        return;
      }
    }
    getResponder().getCommunicator().sendNormalServerMessage("You decide not to answer the question right now and instead meditate more.");
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    int width = 300;
    int height = 330;
    Map<Integer, Set<Cultist>> treemap;
    boolean showedLevel;
    int localServer;
    if (this.askStatus)
    {
      buf.append("text{text='You consider the local leaders of the path:'}");
      treemap = Cultist.getCultistLeaders(this.cultist.getPath(), getResponder()
        .getKingdomId());
      
      showedLevel = false;
      localServer = Servers.localServer.id;
      for (Integer level : treemap.keySet())
      {
        Set<Cultist> subset = (Set)treemap.get(level);
        buf.append("text{text='" + Cults.getNameForLevel(this.cultist.getPath(), level.byteValue()) + ":'}");
        for (Cultist cist : subset)
        {
          PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(cist.getWurmId());
          if ((pinf != null) && (pinf.currentServer == localServer)) {
            if (pinf.wurmId == this.cultist.getWurmId())
            {
              buf.append("text{type=\"bold\";text='" + pinf.getName() + " '}");
            }
            else
            {
              if (!showedLevel) {
                if (level.byteValue() - this.cultist.getLevel() == 3)
                {
                  buf.append("text{type='bold';text='Those on this level may help you advance the path:'}");
                  showedLevel = true;
                }
              }
              buf.append("label{text=\"" + pinf.getName() + " \"}");
            }
          }
        }
        buf.append("text{text=''}");
        width = 500;
        height = 400;
      }
    }
    else if (this.leavePath)
    {
      buf.append("text{text='Select quit to stop following this path. The result is immediate and dramatic:'}");
      buf.append("radio{ group='quit'; id='0';text='Stay';selected='true'}");
      buf.append("radio{ group='quit'; id='1';text='Quit'}");
      buf.append("text{text=''}");
    }
    else if (this.cultist == null)
    {
      buf.append("text{text=\"As you meditate upon these things you realize that there is a pattern of thinking that you can try to follow.\"}");
      buf.append("text{text=\"If this path contains the truth or simply the figment of someones imagination, you do not know.\"}");
      buf.append("text{text=\"Nonetheless, it may pose an interesting challenge.\"}");
      buf.append("text{text=\"Do you wish to embark on the philosophical journey of " + Cults.getPathNameFor(this.path) + "?\"}");
      
      buf.append("text{text=\"If you choose yes, know that you join the Cult of " + Cults.getPathNameFor(this.path) + " with secrets supposed to lead to enlightenment. Divulging those secrets may lead to expulsion.\"}");
      
      buf.append("text{type='bold';text=\"If you decide to join, you will be challenged by the selected path as you visit more places like this one and meditate.\"}");
      buf.append("text{text=''}");
      buf.append("radio{ group='answer'; id='0';text='No';selected='true'}");
      buf.append("radio{ group='answer'; id='1';text='Yes'}");
    }
    else
    {
      buf.append("text{text=\"If " + Cults.getPathNameFor(this.cultist.getPath()) + " contains the truth or simply is the figment of someones imagination, you do not know.\"}");
      
      buf.append("text{text=\"Nonetheless, it poses an interesting challenge.\"}");
      buf.append("text{text=\"The following question springs to mind:\"}");
      buf.append("text{type='bold';text=\"" + Cults.getQuestionForLevel(this.path, this.cultist.getLevel()) + "\"}");
      
      buf.append("text{text=''}");
      String[] answers = Cults.getAnswerAlternativesForLevel(this.path, this.cultist.getLevel());
      for (int x = 0; x < answers.length; x++) {
        buf.append("radio{ group='answer'; id='" + x + "';text=\"" + answers[x] + "\"}");
      }
      buf.append("text{text=\"Know that you are part of the Cult of " + Cults.getPathNameFor(this.path) + " with secrets supposed to lead to enlightenment. Divulging those secrets may lead to expulsion.\"}");
      
      buf.append("text{text=''}");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(width, height, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\CultQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */