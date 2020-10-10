package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;
import javax.annotation.Nullable;

public final class BankManagementQuestion
  extends Question
  implements MiscConstants, TimeConstants
{
  private final Bank bank;
  
  public BankManagementQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, @Nullable Bank aBank)
  {
    super(aResponder, aTitle, aQuestion, 33, aTarget);
    this.bank = aBank;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    if (!getResponder().isGuest()) {
      try
      {
        Item token = Items.getItem(this.target);
        Village village = Villages.getVillage(token.getData2());
        String key = "open";
        String val = getAnswer().getProperty(key);
        if ((val != null) && (val.equals("true"))) {
          ((Player)getResponder()).startBank(village);
        } else if (this.bank != null) {
          if (!this.bank.open)
          {
            key = "move";
            val = getAnswer().getProperty(key);
            if ((val != null) && (val.equals("true"))) {
              if (this.bank.targetVillage > 0)
              {
                try
                {
                  Village localVillage1 = Villages.getVillage(this.bank.targetVillage);
                }
                catch (NoSuchVillageException nsv)
                {
                  this.bank.stopMoving();
                  getResponder().getCommunicator()
                    .sendNormalServerMessage("The bank account has moved here.");
                }
              }
              else if (this.bank.targetVillage != village.getId())
              {
                boolean disbanded = false;
                try
                {
                  this.bank.getCurrentVillage();
                }
                catch (BankUnavailableException nub)
                {
                  disbanded = true;
                }
                this.bank.startMoving(village.getId());
                if (getResponder().getPower() > 0)
                {
                  this.bank.stopMoving();
                  getResponder()
                    .getCommunicator()
                    .sendNormalServerMessage("The bank account has moved here because you're a cool person with some extra powers.");
                }
                else if (disbanded)
                {
                  this.bank.stopMoving();
                  getResponder().getCommunicator().sendNormalServerMessage("The bank account has moved here.");
                }
              }
              else
              {
                getResponder().getCommunicator().sendNormalServerMessage("Your bank is already moving here.");
              }
            }
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("The bank account is open. You cannot manage it now.");
          }
        }
      }
      catch (NoSuchItemException nsi)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Failed to localize the village token for that request.");
      }
      catch (NoSuchVillageException nsv)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Failed to localize the village for that request.");
      }
    } else {
      getResponder().getCommunicator().sendNormalServerMessage("Guests may not open bank accounts.");
    }
  }
  
  public void sendQuestion()
  {
    Village vill = null;
    if ((this.bank != null) && (this.bank.targetVillage >= 0)) {
      try
      {
        vill = Villages.getVillage(this.bank.targetVillage);
      }
      catch (NoSuchVillageException localNoSuchVillageException) {}
    }
    StringBuilder buf = new StringBuilder(getBmlHeader());
    if (!getResponder().isGuest()) {
      if (this.bank == null)
      {
        buf.append("text{text='You may open a bank account here.'}");
        buf.append("text{text='You can only have one bank account, but you may move it.'}");
        buf
          .append("text{text='A bank account will currently not move items between servers, but your money will be available if you open a new bank account on another server.'}");
        buf.append("text{text='It will take 24 hours to move it to another settlement.'}");
        buf.append("text{text='You have to start the move from that settlement token.'}");
        buf
          .append("text{text='Note that some items decay and may disappear inside the bank account, although slower than outside.'}");
        buf
          .append("text{text='It will however be possible to rent a stasis spell to be cast upon the item in the future that will prevent decay.'}");
        
        buf.append("text{text='Do you wish to open a bank account here?'}");
        
        buf.append("radio{ group='open'; id='true';text='Yes'}");
        buf.append("radio{ group='open'; id='false';text='No';selected='true'}");
      }
      else
      {
        this.bank.poll(System.currentTimeMillis());
        if (this.bank.startedMoving > 0L)
        {
          if (vill != null)
          {
            buf.append("text{text=\"Your bank is currently moving to " + vill.getName() + ".\"}");
            buf.append("text{text='It will arrive in approximately " + 
              Server.getTimeFor(this.bank.startedMoving + 86400000L - System.currentTimeMillis()) + ".'}");
            if (vill != getResponder().getCurrentVillage()) {
              buf.append("text{text='It will take 24 hours to move your bank account here instead.'}");
            }
          }
          else
          {
            buf.append("text{text='Do you wish to move your bank account here?'}");
            buf.append("radio{ group='move'; id='true';text='Yes'}");
            buf.append("radio{ group='move'; id='false';text='No';selected='true'}");
          }
        }
        else
        {
          try
          {
            Village village = this.bank.getCurrentVillage();
            buf.append("text{text=\"Your bank is currently situated in " + village.getName() + ".\"}");
            
            buf.append("text{text='It will take 24 hours to move your bank account here.'}");
          }
          catch (BankUnavailableException bu)
          {
            buf.append("text{text='Your bank is not currently located in a village as its previous location has been disbanded.'}");
          }
          buf.append("text{text='Do you wish to move your bank account here?'}");
          buf.append("radio{ group='move'; id='true';text='Yes'}");
          buf.append("radio{ group='move'; id='false';text='No';selected='true'}");
        }
      }
    }
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\BankManagementQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */