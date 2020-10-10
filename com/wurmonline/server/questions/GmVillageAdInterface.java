package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.villages.RecruitmentAd;
import com.wurmonline.server.villages.RecruitmentAds;
import java.util.Properties;

public class GmVillageAdInterface
  extends Question
{
  public GmVillageAdInterface(Creature aResponder, long aTarget)
  {
    super(aResponder, "Manage Village Recruitment Ads", "", 101, aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseGmVillageAdQuestion(this);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    RecruitmentAd[] ads = RecruitmentAds.getAllRecruitmentAds();
    
    buf.append("table{rows=\"" + ads.length + 1 + "\";cols=\"3\";label{text=\"Remove\"};label{text=\"Village\"};label{text=\"Contact\"};");
    for (int i = 0; i < ads.length; i++)
    {
      buf.append("checkbox{id=\"" + ads[i].getVillageId() + "remove\";selected=\"false\";text=\" \"}");
      buf.append("label{text=\"" + ads[i].getVillageName() + "\"};");
      buf.append("label{text=\"" + ads[i].getContactName() + "\"};");
    }
    buf.append("}");
    buf.append(createAnswerButton2("Remove"));
    getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\GmVillageAdInterface.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */