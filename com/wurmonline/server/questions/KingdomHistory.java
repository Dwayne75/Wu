package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public final class KingdomHistory
  extends Question
{
  public KingdomHistory(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 66, aTarget);
  }
  
  public void answer(Properties answers) {}
  
  public void sendQuestion()
  {
    String lHtml = getBmlHeaderWithScroll();
    
    StringBuilder buf = new StringBuilder(lHtml);
    Map<Integer, King> kings = King.eras;
    Map<String, LinkedList<King>> counters = new HashMap();
    for (King k : kings.values())
    {
      kinglist = (LinkedList)counters.get(k.kingdomName);
      if (kinglist == null) {
        kinglist = new LinkedList();
      }
      kinglist.add(k);
      counters.put(k.kingdomName, kinglist);
    }
    LinkedList<King> kinglist;
    for (Object it = counters.entrySet().iterator(); ((Iterator)it).hasNext();)
    {
      entry = (Map.Entry)((Iterator)it).next();
      addKing((Collection)entry.getValue(), (String)entry.getKey(), buf);
    }
    Map.Entry<String, LinkedList<King>> entry;
    if (Servers.localServer.isChallengeServer())
    {
      it = Kingdoms.getAllKingdoms();entry = it.length;
      for (kinglist = 0; kinglist < entry; kinglist++)
      {
        Kingdom kingdom = it[kinglist];
        if (kingdom.existsHere())
        {
          buf.append("label{text=\"" + kingdom.getName() + " points:\"};");
          buf.append("label{text=\"" + kingdom.getWinpoints() + "\"};text{text=''};");
        }
      }
    }
    buf.append(createAnswerButton3());
    
    getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public void addKing(Collection<King> kings, String kingdomName, StringBuilder buf)
  {
    buf.append("text{type=\"bold\";text=\"History of " + kingdomName + ":\"}text{text=''}");
    buf.append("table{rows='" + (kings
      .size() + 1) + "'; cols='10';label{text='Ruler'};label{text='Capital'};label{text='Start Land'};label{text='End Land'};label{text='Land Difference'};label{text='Levels Killed'};label{text='Levels Lost'};label{text='Levels Appointed'};label{text='Start Date'};label{text='End Date'};");
    for (King k : kings)
    {
      buf.append("label{text=\"" + k.getFullTitle() + "\"};");
      buf.append("label{text=\"" + k.capital + "\"};");
      buf.append("label{text=\"" + String.format("%.2f%%", new Object[] { Float.valueOf(k.startLand) }) + "\"};");
      buf.append("label{text=\"" + String.format("%.2f%%", new Object[] { Float.valueOf(k.currentLand) }) + "\"};");
      buf.append("label{text=\"" + String.format("%.2f%%", new Object[] { Float.valueOf(k.currentLand - k.startLand) }) + "\"};");
      buf.append("label{text=\"" + k.levelskilled + "\"};");
      buf.append("label{text=\"" + k.levelslost + "\"};");
      buf.append("label{text=\"" + k.appointed + "\"};");
      buf.append("label{text=\"" + WurmCalendar.getDateFor(k.startWurmTime) + "\"};");
      if (k.endWurmTime > 0L) {
        buf.append("label{text=\"" + WurmCalendar.getDateFor(k.endWurmTime) + "\"};");
      } else {
        buf.append("label{text=\"N/A\"};");
      }
    }
    buf.append("}");
    buf.append("text{text=\"\"}");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\KingdomHistory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */