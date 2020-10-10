package com.wurmonline.server.questions;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangeEmailQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(ChangeEmailQuestion.class.getName());
  boolean providedPassword = false;
  String passProvided = "unknown";
  String passProvidedHashed = "unknown";
  String alertMessage = "";
  
  public ChangeEmailQuestion(Creature aResponder)
  {
    super(aResponder, "Email address for " + aResponder.getName(), "Changing email address", 112, -10L);
  }
  
  public void answer(Properties answers)
  {
    if (!this.providedPassword)
    {
      String oldpw = answers.getProperty("pwinput");
      if ((oldpw == null) || (oldpw.length() < 6))
      {
        getResponder().getCommunicator().sendNormalServerMessage("The old password contains at least 6 characters.");
        return;
      }
      String hashedpw = "";
      try
      {
        hashedpw = LoginHandler.hashPassword(oldpw, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(getResponder().getName())));
      }
      catch (Exception e)
      {
        logger.log(Level.WARNING, "Failed to encrypt pw for " + getResponder().getName() + " with " + oldpw);
      }
      if (hashedpw.equals(((Player)getResponder()).getSaveFile().getPassword()))
      {
        this.providedPassword = true;
        this.passProvided = oldpw;
        this.passProvidedHashed = hashedpw;
        ChangeEmailQuestion ceq = new ChangeEmailQuestion(getResponder());
        ceq.providedPassword = true;
        ceq.passProvided = this.passProvided;
        ceq.passProvidedHashed = this.passProvidedHashed;
        ceq.sendQuestion();
      }
      else
      {
        getResponder().getCommunicator().sendNormalServerMessage("You provided the wrong password.");
        return;
      }
    }
    else
    {
      boolean resend = false;
      this.alertMessage = "";
      String newEmail = answers.getProperty("emailAddress");
      String ppassword = answers.getProperty("pwinput2");
      if ((ppassword == null) || (ppassword.length() < 2)) {
        ppassword = this.passProvided;
      }
      String pwQuestion = answers.getProperty("pwQuestion");
      if ((pwQuestion == null) || (pwQuestion.length() < 5))
      {
        getResponder().getCommunicator().sendAlertServerMessage("You need to provide a password retrieval question at least 5 characters long. This is used on the website.");
        
        this.alertMessage = "You need to provide a password retrieval question at least 5 characters long. This is used on the website.";
        resend = true;
      }
      String pwAnswer = answers.getProperty("pwAnswer");
      if ((pwAnswer == null) || (pwAnswer.length() < 3))
      {
        getResponder().getCommunicator().sendAlertServerMessage("You need to provide a password retrieval answer at least 3 characters long. This is used on the website.");
        
        this.alertMessage = "You need to provide a password retrieval answer at least 3 characters long. This is used on the website.";
        resend = true;
      }
      if (!resend) {
        if ((newEmail != null) && (WebInterfaceImpl.isEmailValid(newEmail)))
        {
          if (!newEmail.equalsIgnoreCase(((Player)getResponder()).getSaveFile().emailAddress))
          {
            resend = false;
            getResponder().getCommunicator().sendNormalServerMessage("You try to change the email to '" + newEmail + "' - result:");
            
            LoginServerWebConnection lsw = new LoginServerWebConnection();
            String isok = lsw.changeEmail(getResponder().getName(), getResponder().getName(), newEmail, ppassword, 
              getResponder().getPower(), pwQuestion, pwAnswer);
            getResponder().getCommunicator().sendNormalServerMessage(isok);
            if (isok.contains("- ok")) {
              try
              {
                PlayerInfoFactory.changeEmail(getResponder().getName(), getResponder().getName(), newEmail, ppassword, 
                  getResponder().getPower(), pwQuestion, pwAnswer);
                logger.log(Level.INFO, getResponder().getName() + " changed the email to " + newEmail);
                getResponder().getCommunicator().lastChangedEmail = System.currentTimeMillis();
              }
              catch (IOException iox)
              {
                logger.log(Level.INFO, getResponder().getName() + " FAILED changed the email to " + newEmail, new Exception());
                
                getResponder().getCommunicator().sendAlertServerMessage("The email was successfully changed on the login server, but not changed locally!");
              }
            }
          }
          else
          {
            getResponder().getCommunicator().sendAlertServerMessage("No change was made.");
          }
        }
        else
        {
          getResponder().getCommunicator().sendAlertServerMessage("The email " + newEmail + " is not a valid email address.");
          
          this.alertMessage = ("The email " + newEmail + " is not a valid email address.");
          resend = true;
        }
      }
      if (resend)
      {
        ChangeEmailQuestion ceq = new ChangeEmailQuestion(getResponder());
        ceq.providedPassword = true;
        ceq.passProvided = this.passProvided;
        ceq.passProvidedHashed = this.passProvidedHashed;
        ceq.alertMessage = this.alertMessage;
        ceq.sendQuestion();
      }
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("text{text=\"Current email:\"}");
    buf.append("text{text=\"" + ((Player)getResponder()).getSaveFile().emailAddress + "\"}");
    if (!this.providedPassword)
    {
      buf.append("text{text=\"Provide the account password in order to update email information:\"}");
      buf.append("input{id=\"pwinput\";maxchars=\"32\"};");
    }
    else
    {
      if (this.alertMessage != null) {
        buf.append("label{color=\"255,40,40\";text=\"" + this.alertMessage + "\"}");
      }
      buf.append("text{text=\"Desired email:\"}");
      buf.append("input{id=\"emailAddress\";maxchars=\"127\";text=\"" + 
        ((Player)getResponder()).getSaveFile().emailAddress + "\"};");
      buf.append("text{text=\"If you want to change the email to one already in use, you need to provide the password for an account using that email.\"}");
      buf.append("text{text=\"If you want to change the email to one that is not in use, you need to leave this empty instead:\"}");
      buf.append("input{id=\"pwinput2\";maxchars=\"30\"};");
      buf.append("text{text=\"Question for password retrieval via website:\"}");
      
      buf.append("input{id=\"pwQuestion\";maxchars=\"127\";text=\"" + 
        ((Player)getResponder()).getSaveFile().pwQuestion + "\"};");
      buf.append("text{text=\"Answer to that question:\"}");
      buf.append("input{id=\"pwAnswer\";maxchars=\"20\";text=\"" + 
        ((Player)getResponder()).getSaveFile().pwAnswer + "\"};");
    }
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\ChangeEmailQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */