package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public final class VillageHistoryQuestion
  extends Question
{
  public VillageHistoryQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 40, aTarget);
  }
  
  public void answer(Properties answers) {}
  
  public void sendQuestion()
  {
    Village citizenVillage = getResponder().getCitizenVillage();
    StringBuilder sb = new StringBuilder();
    sb.append(getBmlHeader());
    if (citizenVillage != null)
    {
      sb.append("header{text=\"Latest events in " + citizenVillage.getName() + ":\"}");
      String[] list = citizenVillage.getHistoryAsStrings(50);
      for (int x = 0; x < list.length; x++) {
        sb.append("text{text=\"" + list[x] + "\"}");
      }
    }
    else
    {
      sb.append("text{text='You are not citizen of a village.'}");
    }
    sb.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(500, 300, true, true, sb.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\VillageHistoryQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */