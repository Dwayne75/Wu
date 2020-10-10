package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageMessages;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class VillageMessagePopup
  extends Question
{
  private static final Logger logger = Logger.getLogger(VillageMessagePopup.class.getName());
  private Village village;
  private InscriptionData papyrusData;
  private String message = null;
  private final Item messageBoard;
  private final Map<Integer, Long> idMap = new HashMap();
  private static final String red = "color=\"255,127,127\"";
  
  public VillageMessagePopup(Creature aResponder, Village aVillage, InscriptionData ins, long aSource, Item noticeBoard)
  {
    super(aResponder, getTitle(aVillage), getQuestion(aVillage), 137, aSource);
    this.messageBoard = noticeBoard;
    this.village = aVillage;
    this.papyrusData = ins;
  }
  
  private static String getTitle(Village village)
  {
    return village.getName() + " notice board";
  }
  
  private static String getQuestion(Village village)
  {
    return "Add Note";
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
    
    String selected = aAnswer.getProperty("select");
    int select = Integer.parseInt(selected);
    if (select > 0)
    {
      long cit = ((Long)this.idMap.get(Integer.valueOf(select))).longValue();
      VillageMessages.create(this.village.getId(), getResponder().getWurmId(), cit, this.message, this.papyrusData
        .getPenColour(), cit == -1L);
      if (cit == -1L) {
        getResponder().getCommunicator().sendNormalServerMessage("You posted a public notice.");
      } else if (cit == -10L) {
        getResponder().getCommunicator().sendNormalServerMessage("You posted a notice.");
      } else {
        getResponder().getCommunicator().sendNormalServerMessage("You posted a note to " + getPlayerName(cit) + ".");
      }
      Items.destroyItem(this.target);
    }
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    
    buf.append(getBmlHeader());
    
    int msglen = this.papyrusData.getInscription().length();
    int mlen = Math.min(msglen, 500);
    this.message = this.papyrusData.getInscription().substring(0, mlen);
    buf.append("input{id=\"answer\";enabled=\"false\";maxchars=\"" + mlen + "\";maxlines=\"-1\";bgcolor=\"200,200,200\";color=\"" + 
    
      WurmColor.getColorRed(this.papyrusData.getPenColour()) + "," + 
      WurmColor.getColorGreen(this.papyrusData.getPenColour()) + "," + 
      WurmColor.getColorBlue(this.papyrusData.getPenColour()) + "\";text=\"" + this.message + "\"}");
    
    buf.append("text{text=\"\"}");
    if (mlen < msglen) {
      buf.append("label{color=\"255,127,127\"text=\"Message is too long, so will be truncated.\"};");
    }
    buf.append("harray{text{type=\"bold\";text=\"Post\"};dropdown{id=\"select\";options=\"");
    buf.append("no where");
    this.idMap.put(Integer.valueOf(0), Long.valueOf(-10L));
    if (this.messageBoard.mayPostNotices(getResponder()))
    {
      if (getResponder().getCitizenVillage() == this.village)
      {
        this.idMap.put(Integer.valueOf(this.idMap.size()), Long.valueOf(-10L));
        buf.append(",as village notice");
      }
      this.idMap.put(Integer.valueOf(this.idMap.size()), Long.valueOf(-1L));
      buf.append(",as public notice");
    }
    if (this.messageBoard.mayAddPMs(getResponder()))
    {
      Citizen[] citizens = this.village.getCitizens();
      Arrays.sort(citizens);
      for (Citizen c : citizens) {
        if ((c.isPlayer()) && (c.getId() != getResponder().getWurmId())) {
          if (getPlayerName(c.getId()).length() > 0)
          {
            this.idMap.put(Integer.valueOf(this.idMap.size()), Long.valueOf(c.getId()));
            buf.append(",to " + c.getName());
          }
        }
      }
    }
    buf.append("\"}}");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(400, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private final String getPlayerName(long id)
  {
    PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(id);
    if (info == null) {
      return "";
    }
    return info.getName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\VillageMessagePopup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */