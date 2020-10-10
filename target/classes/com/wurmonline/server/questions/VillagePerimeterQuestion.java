package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;

public final class VillagePerimeterQuestion
  extends Question
{
  private final int villageId;
  
  public VillagePerimeterQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, int villid)
  {
    super(aResponder, aTitle, aQuestion, 75, aTarget);
    this.villageId = villid;
  }
  
  public void answer(Properties props) {}
  
  public void sendQuestion()
  {
    if (!Servers.localServer.HOMESERVER)
    {
      StringBuilder buf = new StringBuilder(getBmlHeaderWithScroll());
      buf.append("text{type='bold';text='Perimeters are not active on wild servers.'}");
      buf.append(createAnswerButton2());
      getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }
    else
    {
      try
      {
        Village village = Villages.getVillage(this.villageId);
        String deedType = "settlement";
        
        StringBuilder buf = new StringBuilder(getBmlHeaderWithScroll());
        buf.append("text{type='bold';text='Set the permissions for the settlement perimeter.'}");
        
        buf.append("text{text=''}");
        int max = (village.getEndX() - village.getStartX()) / 2;
        buf.append("text{text=\"Perimeter friends. These may do all these things in your perimeter. " + village
          .getName() + " may have up to " + max + " friends.\"}");
        
        buf.append(createAnswerButton3());
        getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
      }
      catch (NoSuchVillageException nsv)
      {
        getResponder().getCommunicator().sendNormalServerMessage("Failed to update perimeter settings. No such village could be located.");
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\VillagePerimeterQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */