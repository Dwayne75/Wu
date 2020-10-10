package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.epic.MissionHelper;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.util.Map;
import java.util.Properties;

public final class SimplePopup
  extends Question
{
  private String toSend = "";
  private long missionId = -10L;
  private float maxHelps = -10.0F;
  private InscriptionData papyrusData = null;
  
  public SimplePopup(Creature _responder, String _title, String _question)
  {
    super(_responder, _title, _question, 62, -10L);
    this.windowSizeX = 300;
    this.windowSizeY = 300;
  }
  
  public SimplePopup(Creature _responder, String _title, String _question, String _toSend)
  {
    super(_responder, _title, _question, 62, -10L);
    this.windowSizeX = 300;
    this.windowSizeY = 300;
    this.toSend = _toSend;
  }
  
  public SimplePopup(Creature _responder, String _title, InscriptionData thePapyrusData)
  {
    super(_responder, _title, "You find a message inscribed on the papyrus.", 62, -10L);
    this.windowSizeX = 350;
    this.windowSizeY = 300;
    this.papyrusData = thePapyrusData;
  }
  
  public SimplePopup(Creature _responder, String _title, String _question, long _missionId, float _maxHelps)
  {
    super(_responder, _title, _question, 62, -10L);
    this.windowSizeX = 300;
    this.windowSizeY = 300;
    this.missionId = _missionId;
    this.maxHelps = _maxHelps;
  }
  
  public void answer(Properties answers) {}
  
  public void sendQuestion(String sendButtonText)
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    buf.append("text{text=\"\"}");
    if (this.missionId > 0L)
    {
      for (MissionHelper helper : MissionHelper.getHelpers().values())
      {
        int i = helper.getHelps(this.missionId);
        if (i > 0)
        {
          PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(helper.getPlayerId());
          if (pinf != null) {
            buf.append("text{text=\"" + pinf.getName() + ": " + i + " (" + i * 100 / this.maxHelps + "%) \"}");
          } else {
            buf.append("text{text=\"Unknown: " + i + " (" + i * 100 / this.maxHelps + "%) \"}");
          }
        }
      }
      buf.append("text{text=\"\"}");
    }
    else if (this.papyrusData != null)
    {
      buf.append("input{id=\"answer\";enabled=\"false\";maxchars=\"" + this.papyrusData
        .getInscription().length() + "\";maxlines=\"-1\";bgcolor=\"200,200,200\";color=\"" + 
        
        WurmColor.getColorRed(this.papyrusData.getPenColour()) + "," + 
        WurmColor.getColorGreen(this.papyrusData.getPenColour()) + "," + 
        WurmColor.getColorBlue(this.papyrusData.getPenColour()) + "\";text=\"" + this.papyrusData
        .getInscription() + "\"}");
      buf.append("text{text=\"Signed " + this.papyrusData.getInscriber() + "\"};");
    }
    else if (this.toSend.length() > 0)
    {
      if ((this.toSend.contains("{")) && (this.toSend.contains("}"))) {
        buf.append(this.toSend);
      } else {
        buf.append("text{text=\"" + this.toSend + "\"}");
      }
      buf.append("text{text=\"\"}");
    }
    buf.append(createAnswerButton2(sendButtonText));
    getResponder().getCommunicator().sendBml(this.windowSizeX, this.windowSizeY, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public void sendQuestion()
  {
    sendQuestion("Send");
  }
  
  public String getToSend()
  {
    return this.toSend;
  }
  
  public void setToSend(String aToSend)
  {
    this.toSend = aToSend;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\SimplePopup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */