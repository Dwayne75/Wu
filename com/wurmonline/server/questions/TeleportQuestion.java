package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class TeleportQuestion
  extends Question
{
  private final List<Player> playerlist = new LinkedList();
  private final List<Village> villagelist = new LinkedList();
  private String filter = "";
  private boolean filterPlayers = false;
  private boolean filterVillages = false;
  
  public TeleportQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 17, aTarget);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    
    buf.append("harray{label{text='Tile x'};input{id='data1'; text='-1'}}");
    buf.append("harray{label{text='Tile y'};input{id='data2'; text='-1'}}");
    buf.append("harray{label{text='Surfaced: '};dropdown{id='layer';options='true,false'}}");
    
    Player[] players = Players.getInstance().getPlayers();
    
    Arrays.sort(players);
    for (int x = 0; x < players.length; x++) {
      if ((!this.filterPlayers) || 
        (PlayerInfoFactory.wildCardMatch(players[x].getName().toLowerCase(), this.filter.toLowerCase()))) {
        this.playerlist.add(players[x]);
      }
    }
    buf.append("text{text=''};");
    buf.append("harray{label{text=\"Filter by: \"};input{maxchars=\"20\";id=\"filtertext\";text=\"" + this.filter + "\"; onenter='filterboth'};label{text=' (Use * as a wildcard)'};}");
    
    buf.append("harray{label{text='Player:    '}; dropdown{id='wurmid';options='");
    for (int x = 0; x < this.playerlist.size(); x++)
    {
      if (x > 0) {
        buf.append(",");
      }
      buf.append(((Player)this.playerlist.get(x)).getName());
    }
    buf.append("'};button{text='Filter'; id='filterplayer'}}");
    buf.append("harray{label{text='Village:   '}; dropdown{id='villid';default='0';options=\"none,");
    Village[] vills = Villages.getVillages();
    
    Arrays.sort(vills);
    int lastPerm = 0;
    for (int x = 0; x < vills.length; x++) {
      if ((!this.filterVillages) || 
        (PlayerInfoFactory.wildCardMatch(vills[x].getName().toLowerCase(), this.filter.toLowerCase()))) {
        if (vills[x].isPermanent)
        {
          this.villagelist.add(lastPerm, vills[x]);
          lastPerm++;
        }
        else
        {
          this.villagelist.add(vills[x]);
        }
      }
    }
    for (int x = 0; x < this.villagelist.size(); x++)
    {
      if (x > 0) {
        buf.append(",");
      }
      if (((Village)this.villagelist.get(x)).isPermanent) {
        buf.append("#");
      }
      buf.append(((Village)this.villagelist.get(x)).getName());
    }
    buf.append("\"};button{text='Filter'; id='filtervillage'}}");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    boolean filterP = false;
    boolean filterV = false;
    String val = getAnswer().getProperty("filterplayer");
    if ((val != null) && (val.equals("true"))) {
      filterP = true;
    }
    val = getAnswer().getProperty("filtervillage");
    if ((val != null) && (val.equals("true"))) {
      filterV = true;
    }
    val = getAnswer().getProperty("filterboth");
    if ((val != null) && (val.equals("true"))) {
      filterV = filterP = 1;
    }
    if ((filterP) || (filterV))
    {
      val = getAnswer().getProperty("filtertext");
      if ((val == null) || (val.length() == 0)) {
        val = "*";
      }
      TeleportQuestion tq = new TeleportQuestion(getResponder(), this.title, this.question, this.target);
      tq.filter = val;
      tq.filterPlayers = filterP;
      tq.filterVillages = filterV;
      tq.sendQuestion();
    }
    else
    {
      QuestionParser.parseTeleportQuestion(this);
    }
  }
  
  Player getPlayer(int aPosition)
  {
    return (Player)this.playerlist.get(aPosition);
  }
  
  Village getVillage(int aPosition)
  {
    return (Village)this.villagelist.get(aPosition);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\TeleportQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */