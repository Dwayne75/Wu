package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GmSetMedPath
  extends Question
{
  private static final Logger logger = Logger.getLogger(GmSetMedPath.class.getName());
  private final Creature creature;
  
  public GmSetMedPath(Creature aResponder, Creature aTarget)
  {
    super(aResponder, "Meditation Path", aTarget.getName() + " Meditation Path", 132, aTarget.getWurmId());
    this.creature = aTarget;
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
    if (this.type == 0)
    {
      logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      return;
    }
    if (this.type == 132) {
      if (getResponder().getPower() >= 4) {
        try
        {
          String prop = aAnswer.getProperty("newcult");
          if (prop != null)
          {
            getResponder().getLogger().log(Level.INFO, "Setting meditation path and level of " + this.creature.getName());
            
            byte cultId = Byte.parseByte(prop);
            Cultist c = this.creature.getCultist();
            if ((c == null) && (cultId > 0)) {
              c = new Cultist(this.creature.getWurmId(), cultId);
            } else if (c != null)
            {
              if (cultId > 0) {
                c.setPath(cultId);
              } else {
                try
                {
                  c.deleteCultist();
                  if (this.creature != getResponder()) {
                    this.creature.getCommunicator().sendNormalServerMessage("You are no longer following any path.");
                  }
                  getResponder().getCommunicator().sendNormalServerMessage(this.creature.getName() + " is no longer following any path.");
                  
                  this.creature.refreshVisible();
                  this.creature.getCommunicator().sendOwnTitles();
                  
                  return;
                }
                catch (IOException localIOException) {}
              }
            }
            else {
              return;
            }
          }
          prop = aAnswer.getProperty("cultlevel");
          if (prop != null)
          {
            byte cultLvl = Byte.parseByte(prop);
            Cultist c = this.creature.getCultist();
            if (c != null) {
              c.setLevel(cultLvl);
            }
          }
          if (this.creature != getResponder()) {
            this.creature.getCommunicator().sendNormalServerMessage("You are now " + this.creature.getCultist().getCultistTitle());
          }
          getResponder().getCommunicator().sendNormalServerMessage(this.creature.getName() + " is now " + this.creature.getCultist().getCultistTitle());
          
          this.creature.refreshVisible();
          this.creature.getCommunicator().sendOwnTitles();
        }
        catch (NumberFormatException nsf)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Unable to set meditation path and level with those answers.");
          
          return;
        }
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    if (getResponder().getPower() >= 4)
    {
      buf.append("text{type=\"bold\";text=\"Choose path to set the target to:\"}");
      
      Cultist c = this.creature.getCultist();
      
      int cId = c == null ? 0 : c.getPath();
      boolean isSelected = false;
      for (int i = 0; i < 6; i++)
      {
        if (i == cId) {
          isSelected = true;
        } else {
          isSelected = false;
        }
        buf.append("radio{ group='newcult'; id='" + i + "'; text='" + Cults.getPathNameFor((byte)i) + "'" + (isSelected ? ";selected='true'" : "") + "}");
      }
      int cLvl = c == null ? 0 : c.getLevel();
      buf.append("harray{input{id='cultlevel'; maxchars='2'; text='" + cLvl + "'}label{text='Path Level'}}");
      buf.append(createAnswerButton2());
      getResponder().getCommunicator().sendBml(250, 250, true, true, buf.toString(), 200, 200, 200, this.title);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\GmSetMedPath.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */