package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import java.util.Properties;

public class MissionPopup
  extends Question
{
  private String top = "";
  private String toSend = "";
  private MissionManager root = null;
  
  public MissionPopup(Creature _responder, String _title, String _question)
  {
    super(_responder, _title, _question, 92, -10L);
    this.windowSizeX = 300;
    this.windowSizeY = 300;
  }
  
  public void answer(Properties aAnswers)
  {
    if (this.root != null) {
      this.root.cloneAndSendManageEffect(null);
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    if (this.top.length() == 0)
    {
      buf.append("border{null;");
    }
    else if ((this.top.contains("{")) && (this.toSend.contains("}")))
    {
      buf.append("border{" + this.top);
      if (!this.top.endsWith(";")) {
        buf.append(";");
      }
    }
    else
    {
      buf.append("border{center{text{type='bold';text=\"" + this.top + "\"}};");
    }
    buf.append("null;scroll{vertical='true';horizontal='true';varray{rescale='false';passthrough{id='id';text='" + getId() + "'}");
    
    buf.append("closebutton{id=\"submit\"};");
    buf.append("text{text=\"\"}");
    if (this.toSend.length() > 0)
    {
      if ((this.toSend.contains("{")) && (this.toSend.contains("}"))) {
        buf.append(this.toSend);
      } else {
        buf.append("text{size=\"" + this.windowSizeX + ",10\";text=\"" + this.toSend + "\"}");
      }
      buf.append("text{text=\"\"}");
    }
    buf.append("harray{button{text='Ok';id='submit'}}}};null;null;}");
    
    getResponder().getCommunicator().sendBml(this.windowSizeX, this.windowSizeY, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  void setRoot(MissionManager aRoot)
  {
    this.root = aRoot;
  }
  
  public String getToSend()
  {
    return this.toSend;
  }
  
  public void setToSend(String aToSend)
  {
    this.toSend = aToSend;
  }
  
  public String getTop()
  {
    return this.top;
  }
  
  public void setTop(String topString)
  {
    this.top = topString;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\MissionPopup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */