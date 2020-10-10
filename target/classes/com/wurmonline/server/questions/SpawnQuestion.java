package com.wurmonline.server.questions;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Spawnpoint;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class SpawnQuestion
  extends Question
{
  private final List<Spawnpoint> spawnpoints = new LinkedList();
  private final Map<Integer, Integer> servers = new HashMap();
  
  public SpawnQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 34, aTarget);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    
    Set<Spawnpoint> spawnPoints = ((Player)getResponder()).spawnpoints;
    if ((spawnPoints == null) || (spawnPoints.isEmpty()))
    {
      ((Player)getResponder()).calculateSpawnPoints();
      spawnPoints = ((Player)getResponder()).spawnpoints;
    }
    Object localObject;
    Spawnpoint sp;
    if ((spawnPoints != null) && (!spawnPoints.isEmpty()))
    {
      int x = 0;
      buf.append("dropdown{id='spawnpoint';options=\"");
      for (localObject = spawnPoints.iterator(); ((Iterator)localObject).hasNext();)
      {
        sp = (Spawnpoint)((Iterator)localObject).next();
        if (x > 0) {
          buf.append(",");
        }
        this.spawnpoints.add(sp);
        buf.append(sp.description);
        x++;
      }
      buf.append("\"};");
    }
    else
    {
      buf.append("label{text=\"No valid spawn points found. Wait and try again using the /respawn command or send to start at the startpoint.\"};");
    }
    if (Servers.localServer.EPIC) {
      if (Servers.localServer.getKingdom() != getResponder().getKingdomId())
      {
        buf.append("label{text=\"You may also select to spawn on another Epic server.\"};");
        int x = 0;
        this.servers.put(Integer.valueOf(x), Integer.valueOf(0));
        buf.append("dropdown{id='eserver';options=\"None");
        localObject = Servers.getAllServers();sp = localObject.length;
        for (Spawnpoint localSpawnpoint1 = 0; localSpawnpoint1 < sp; localSpawnpoint1++)
        {
          ServerEntry s = localObject[localSpawnpoint1];
          if ((s.EPIC) && (s.isAvailable(0, getResponder().isPaying()))) {
            if (s.getId() != Servers.localServer.id) {
              if ((s.getKingdom() == 0) || (s.getKingdom() == getResponder().getKingdomId())) {
                if ((getResponder().isPaying()) || (!s.ISPAYMENT))
                {
                  x++;
                  buf.append(",");
                  buf.append(s.getName());
                  this.servers.put(Integer.valueOf(x), Integer.valueOf(s.getId()));
                }
              }
            }
          }
        }
        buf.append("\"};");
      }
    }
    if (Features.Feature.FREE_ITEMS.isEnabled())
    {
      buf.append("text{text=''};text{text=''};");
      buf.append("label{text=\"Do you require a weapon QL 40 or a rope?\"};");
      buf.append("dropdown{id='weapon';options=\"No");
      buf.append(",Long Sword + Shield,Two Handed Sword, Large Axe + Shield, Huge Axe, Medium Maul + Shield, Large Maul, Halberd, Long Spear");
      buf.append("\"};");
      
      buf.append("text{text=''};text{text=''};");
      buf.append("label{text=\"You may also select to spawn with some armour.\"};");
      buf.append("dropdown{id='armour';options=\"None");
      buf.append(",Chain (QL 40), Leather (QL 60), Plate (QL 20)");
      buf.append("\"};");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, Servers.localServer.isChallengeServer() ? 500 : 300, true, true, buf
      .toString(), 200, 200, 200, this.title);
  }
  
  final Map<Integer, Integer> getServerEntries()
  {
    return this.servers;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseSpawnQuestion(this);
  }
  
  Spawnpoint getSpawnpoint(int aIndex)
  {
    if (this.spawnpoints.isEmpty()) {
      return null;
    }
    return (Spawnpoint)this.spawnpoints.get(aIndex);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\SpawnQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */