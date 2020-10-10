package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import java.util.Properties;

public class LCMManagementQuestion
  extends Question
{
  private short actionType;
  
  public LCMManagementQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, short actionType)
  {
    super(aResponder, aTitle, aQuestion, 128, aTarget);
    this.actionType = actionType;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseLCMManagementQuestion(this);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("text{text='Who do you want to " + getActionVerb() + "?'};");
    buf.append("label{text'Name:'};input{id='name';maxchars='40';text=\"\"};");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private String getActionVerb()
  {
    if (this.actionType == 698) {
      return "add or remove their CA status from";
    }
    if (this.actionType == 699) {
      return "add or remove their CM status from";
    }
    if (this.actionType == 700) {
      return "see their info of";
    }
    return "";
  }
  
  public short getActionType()
  {
    return this.actionType;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\LCMManagementQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */