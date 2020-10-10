package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.webinterface.WcCAHelpGroupMessage;
import java.util.Properties;
import java.util.logging.Logger;

public final class GroupCAHelpQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(GroupCAHelpQuestion.class.getName());
  
  public GroupCAHelpQuestion(Creature aResponder)
  {
    super(aResponder, "Group CA Helps", "Setup Grouping of CA Helps?", 115, aResponder.getWurmId());
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
    Creature responder = getResponder();
    for (ServerEntry se : Servers.getAllServers())
    {
      byte newCAHelpGroup = Byte.parseByte(aAnswer.getProperty(se.getAbbreviation()));
      if (se.getCAHelpGroup() != newCAHelpGroup)
      {
        if (se.getId() != Servers.getLocalServerId())
        {
          WcCAHelpGroupMessage wchgm = new WcCAHelpGroupMessage(newCAHelpGroup);
          wchgm.sendToServer(se.getId());
        }
        se.updateCAHelpGroup(newCAHelpGroup);
      }
    }
    responder.getCommunicator().sendNormalServerMessage("CA Help Groups Updated.");
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("text{text=\"This allows you to combine CA Help tabs across servers.\"}");
    buf.append("text{text=\"\"}");
    
    buf.append("table{rows=\"" + (Servers.getAllServers().length + 1) + "\";cols=\"9\";text{type=\"bold\";text=\"Server\"};text{type=\"bold\";text=\"Single\"};text{type=\"bold\";text=\"Group0\"};text{type=\"bold\";text=\"Group1\"};text{type=\"bold\";text=\"Group2\"};text{type=\"bold\";text=\"Group3\"};text{type=\"bold\";text=\"Group4\"};text{type=\"bold\";text=\"Group5\"};text{type=\"bold\";text=\"Group6\"};");
    for (ServerEntry se : Servers.getAllServers())
    {
      String sea = se.getAbbreviation();
      byte seg = se.getCAHelpGroup();
      buf.append("label{text=\"" + sea + "\"};radio{group=\"" + sea + "\";id=\"-1\"" + (seg == -1 ? ";selected=\"true\"" : "") + "};radio{group=\"" + sea + "\";id=\"0\"" + (seg == 0 ? ";selected=\"true\"" : "") + "};radio{group=\"" + sea + "\";id=\"1\"" + (seg == 1 ? ";selected=\"true\"" : "") + "};radio{group=\"" + sea + "\";id=\"2\"" + (seg == 2 ? ";selected=\"true\"" : "") + "};radio{group=\"" + sea + "\";id=\"3\"" + (seg == 3 ? ";selected=\"true\"" : "") + "};radio{group=\"" + sea + "\";id=\"4\"" + (seg == 4 ? ";selected=\"true\"" : "") + "};radio{group=\"" + sea + "\";id=\"5\"" + (seg == 5 ? ";selected=\"true\"" : "") + "};radio{group=\"" + sea + "\";id=\"6\"" + (seg == 6 ? ";selected=\"true\"" : "") + "};");
    }
    buf.append("}");
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(450, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\GroupCAHelpQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */