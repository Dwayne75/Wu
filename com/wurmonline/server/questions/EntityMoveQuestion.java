package com.wurmonline.server.questions;

import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.MapHex;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EntityMoveQuestion
  extends Question
{
  private Integer[] neighbours;
  private MapHex currentHex;
  private int deityToGuide = -1;
  private boolean secondStep = false;
  private static final Logger logger = Logger.getLogger(EntityMoveQuestion.class.getName());
  
  public EntityMoveQuestion(Creature aResponder)
  {
    super(aResponder, "Guide the deities", "Whereto will you guide your deity?", 113, -10L);
  }
  
  public void answer(Properties answers)
  {
    if (getResponder().getKarma() < 5000)
    {
      getResponder().getCommunicator().sendNormalServerMessage("You do not have enough karma to commune with " + 
        getResponder().getDeity().getName() + ".");
      return;
    }
    String deityString = answers.getProperty("deityId");
    if (!this.secondStep)
    {
      if ((deityString != null) && (deityString.length() > 0)) {
        try
        {
          int deityId = Integer.parseInt(deityString);
          if (deityId < 0)
          {
            getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
            
            return;
          }
          Deity deity = Deities.getDeity(deityId);
          if ((getResponder().getDeity() != null) && (deity != null))
          {
            EntityMoveQuestion nem = new EntityMoveQuestion(getResponder());
            nem.secondStep = true;
            nem.deityToGuide = deityId;
            nem.sendHexQuestion();
            return;
          }
          getResponder().getCommunicator().sendAlertServerMessage("You fail to commune with the gods...");
        }
        catch (NumberFormatException nfre)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Not a number for the desired deity...");
          logger.log(Level.INFO, "Not a number " + deityString);
        }
      }
      getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
    }
    else if (getResponder().getDeity() != null)
    {
      final Deity deity = Deities.getDeity(this.deityToGuide);
      if (deity == null)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Not a number for the desired deity...");
        
        return;
      }
      String val = answers.getProperty("sethex");
      if ((val != null) && (val.length() > 0)) {
        try
        {
          final int hexnum = Integer.parseInt(val);
          if (hexnum < 0)
          {
            getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
            
            return;
          }
          boolean ok = false;
          for (Integer hexes : this.neighbours) {
            if (hexes.intValue() == hexnum)
            {
              ok = true;
              break;
            }
          }
          if (ok)
          {
            MapHex hex = EpicServerStatus.getValrei().getMapHex(hexnum);
            if (hex != null)
            {
              getResponder().getCommunicator().sendNormalServerMessage("You attempt to guide your deity..");
              
              new Thread(getResponder().getName() + "-guides-" + deity.getName() + "-Thread")
              {
                public final void run()
                {
                  boolean success = Server.rand.nextFloat() < 0.7F;
                  if (success)
                  {
                    LoginServerWebConnection lsw = new LoginServerWebConnection();
                    
                    success = lsw.requestDeityMove(EntityMoveQuestion.this.deityToGuide, hexnum, EntityMoveQuestion.this
                      .getResponder().getName());
                    try
                    {
                      Thread.sleep(2000L);
                    }
                    catch (InterruptedException localInterruptedException) {}
                    if (success)
                    {
                      EntityMoveQuestion.logger.log(Level.INFO, EntityMoveQuestion.this.getResponder().getName() + " guides " + deity
                        .getName());
                      EntityMoveQuestion.this.getResponder().getCommunicator().sendSafeServerMessage("... and " + deity
                        .getName() + " heeds your advice!");
                      
                      EntityMoveQuestion.this.getResponder().modifyKarma(60536);
                    }
                    else
                    {
                      EntityMoveQuestion.this.getResponder().getCommunicator().sendNormalServerMessage("... but fail to penetrate the ether to Valrei.");
                      
                      EntityMoveQuestion.logger.log(Level.INFO, EntityMoveQuestion.this.getResponder().getName() + " guiding but connection to " + deity
                      
                        .getName() + " broken.");
                    }
                  }
                  else
                  {
                    try
                    {
                      Thread.sleep(3000L);
                    }
                    catch (InterruptedException localInterruptedException1) {}
                    EntityMoveQuestion.this.getResponder().getCommunicator().sendNormalServerMessage("... but you are ignored.");
                    
                    EntityMoveQuestion.this.getResponder().modifyKarma(63036);
                    EntityMoveQuestion.logger.log(Level.INFO, EntityMoveQuestion.this.getResponder().getName() + " guiding ignored by " + deity
                      .getName() + ".");
                  }
                }
              }.start();
            }
          }
        }
        catch (NumberFormatException nfre)
        {
          getResponder().getCommunicator().sendNormalServerMessage("Not a number for the desired position...");
          logger.log(Level.INFO, "Not a number " + val);
        }
      }
      getResponder().getCommunicator().sendNormalServerMessage("You refrain from disturbing the gods at this time.");
    }
    else
    {
      getResponder().getCommunicator().sendNormalServerMessage("You no longer pray to a deity.");
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    if (getResponder().getDeity() != null)
    {
      buf.append("text{text=\"You may spend karma in order to envision Valrei and attempt to guide your deity.\"}text{text=\"\"}");
      buf.append("text{text=\"There is 70% chance that you succed in getting your deities attention, and the cost will be 5000 karma if you do.\"}text{text=\"\"}");
      buf.append("text{text=\"If the request fails, you will only lose 2500 karma.\"}text{text=\"\"}");
      
      buf.append("radio{ group='deityId'; id='0';text='Do not Guide';selected='true'}");
      if (getResponder().getKingdomTemplateId() == 3) {
        buf.append("radio{ group='deityId'; id='4';text='Guide Libila'}");
      } else if (getResponder().getKingdomTemplateId() == 2) {
        buf.append("radio{ group='deityId'; id='2';text='Guide Magranon'}");
      } else if (getResponder().getKingdomTemplateId() == 1) {
        if (getResponder().getDeity().number == 3) {
          buf.append("radio{ group='deityId'; id='1';text='Guide Fo'}");
        } else {
          buf.append("radio{ group='deityId'; id='1';text='Guide Fo'}");
        }
      }
    }
    else
    {
      buf.append("text{text=\"You no longer pray to a deity.\"}text{text=\"\"}");
    }
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public final void sendHexQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    Integer currentInt = Deities.getPosition(this.deityToGuide);
    Deity deity = Deities.getDeity(this.deityToGuide);
    buf.append("text{text=\"Where do you want " + deity.getName() + " to go?\"}text{text=\"\"}");
    buf.append("radio{ group='sethex'; id=\"-1\";text=\"Never mind...\";selected=\"true\"};");
    if (currentInt != null)
    {
      this.currentHex = EpicServerStatus.getValrei().getMapHex(currentInt.intValue());
      if (this.currentHex != null)
      {
        this.neighbours = this.currentHex.getNearMapHexes();
        for (Integer i : this.neighbours)
        {
          MapHex maphex = EpicServerStatus.getValrei().getMapHex(i.intValue());
          if (maphex != null)
          {
            String trap = maphex.isTrap() ? " (trap)" : "";
            String slow = maphex.isSlow() ? " (slow)" : "";
            String teleport = maphex.isTeleport() ? " (shift)" : "";
            String strength = maphex.isStrength() ? " (strength)" : "";
            String vitality = maphex.isVitality() ? " (vitality)" : "";
            buf.append("radio{ group='sethex'; id=\"" + i.intValue() + "\";text=\"" + maphex.getName() + trap + slow + teleport + strength + vitality + "\"};");
          }
          else
          {
            logger.log(Level.WARNING, "NO HEX ON VALREI FOR " + i.intValue());
          }
        }
        if ((this.neighbours == null) || (this.neighbours.length == 0)) {
          buf.append("text{text=\"" + deity.getName() + " is not available for guidance now.\"}text{text=\"\"}");
        }
      }
      else
      {
        buf.append("text{text=\"" + deity.getName() + " is not available for guidance now.\"}text{text=\"\"}");
      }
    }
    else
    {
      buf.append("text{text=\"" + deity.getName() + " is not available for guidance now.\"}text{text=\"\"}");
    }
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\EntityMoveQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */