package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Player;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class SetDeityQuestion
  extends Question
{
  private final List<Player> playlist = new LinkedList();
  private final Map<Integer, Integer> deityMap = new ConcurrentHashMap();
  
  public SetDeityQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 26, aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseSetDeityQuestion(this);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    
    buf.append("harray{label{text='Player: '};dropdown{id='wurmid';options='");
    Player[] players = Players.getInstance().getPlayers();
    
    Arrays.sort(players);
    for (int x = 0; x < players.length; x++)
    {
      if (x > 0) {
        buf.append(",");
      }
      buf.append(players[x].getName());
      this.playlist.add(players[x]);
    }
    buf.append("'}}");
    Deity[] deitys = Deities.getDeities();
    int counter = 0;
    
    buf.append("harray{label{text=\"Deity\"};dropdown{id=\"deityid\";options='None");
    for (Deity d : deitys)
    {
      counter++;
      this.deityMap.put(Integer.valueOf(counter), Integer.valueOf(d.getNumber()));
      buf.append(",");
      buf.append(d.getName());
    }
    buf.append("'}}");
    
    buf.append("harray{label{text=\"Faith\"};input{maxchars=\"3\";id=\"faith\";text=\"1\"}label{text=\".\"}input{maxchars=\"6\"; id=\"faithdec\"; text=\"000000\"}}");
    
    buf.append("harray{label{text=\"Favor\"};input{maxchars='3';id=\"favor\";text=\"1\"}}");
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public final int getDeityNumberFromArrayPos(int arrayPos)
  {
    if (arrayPos == 0) {
      return 0;
    }
    return ((Integer)this.deityMap.get(Integer.valueOf(arrayPos))).intValue();
  }
  
  Player getPlayer(int aPosition)
  {
    return (Player)this.playlist.get(aPosition);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\SetDeityQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */