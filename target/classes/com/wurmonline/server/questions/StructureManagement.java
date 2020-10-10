package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StructureManagement
  extends Question
{
  private static final Logger logger = Logger.getLogger(StructureManagement.class.getName());
  private Player player;
  private Structure structure;
  private Friend[] friends = new Friend[0];
  private PermissionsByPlayer[] guests;
  
  public StructureManagement(Creature aResponder, String aTitle, String aQuestion, int aType, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, aType, aTarget);
    this.player = ((Player)getResponder());
    try
    {
      this.structure = Structures.getStructure(this.target);
      this.friends = this.player.getFriends();
      this.guests = this.structure.getPermissionsPlayerList().getPermissionsByPlayer();
    }
    catch (NoSuchStructureException e)
    {
      logger.log(Level.INFO, getResponder().getWurmId() + " tried to manage structure with id " + this.target + " but no structure was found.");
      
      return;
    }
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseStructureManagement(this);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("text{type='bold';text='Add guest:'}");
    Iterator<Map.Entry<String, Long>> it;
    if (this.friends.length > 0)
    {
      Map<String, Long> fset = new TreeMap();
      for (int x = 0; x < this.friends.length; x++) {
        try
        {
          boolean add = true;
          for (int g = 0; g < this.guests.length; g++) {
            if (this.friends[x].getFriendId() == this.guests[g].getPlayerId())
            {
              add = false;
              break;
            }
          }
          if (add) {
            fset.put(Players.getInstance().getNameFor(this.friends[x].getFriendId()), new Long(this.friends[x].getFriendId()));
          }
        }
        catch (NoSuchPlayerException localNoSuchPlayerException) {}catch (IOException iox)
        {
          getResponder().getCommunicator().sendAlertServerMessage("There was a problem when handling your request. Please contact an administrator.");
          
          logger.log(Level.WARNING, "Got ioexception when looking for player with id " + this.friends[x], iox);
        }
      }
      Set<Map.Entry<String, Long>> entries = fset.entrySet();
      for (it = entries.iterator(); it.hasNext();)
      {
        Map.Entry<String, Long> e = (Map.Entry)it.next();
        buf.append("checkbox{id='f" + ((Long)e.getValue()).longValue() + "';text='" + (String)e.getKey() + "'}");
      }
    }
    else
    {
      buf.append("text{type='bold';text='No friends to add.'}");
    }
    buf.append("text{type='bold';text='Remove guest:'}");
    
    Map<String, Long> fset = new TreeMap();
    if (this.guests.length > 0) {
      for (int x = 0; x < this.guests.length; x++) {
        try
        {
          if ((this.guests[x].getPlayerId() != -20L) && 
            (this.guests[x].getPlayerId() != -30L) && 
            (this.guests[x].getPlayerId() != -40L)) {
            fset.put(Players.getInstance().getNameFor(this.guests[x].getPlayerId()), Long.valueOf(this.guests[x].getPlayerId()));
          }
        }
        catch (NoSuchPlayerException localNoSuchPlayerException1) {}catch (IOException iox)
        {
          getResponder().getCommunicator().sendAlertServerMessage("There was a problem when handling your request. Please contact an administrator.");
          
          logger.log(Level.WARNING, "Got ioexception when looking for player with id " + this.guests[x], iox);
        }
      }
    }
    Iterator<Map.Entry<String, Long>> it;
    if (fset.isEmpty())
    {
      buf.append("text{type='bold';text='No guests to remove.'}");
    }
    else
    {
      Set<Map.Entry<String, Long>> entries = fset.entrySet();
      for (it = entries.iterator(); it.hasNext();)
      {
        Map.Entry<String, Long> e = (Map.Entry)it.next();
        buf.append("checkbox{id='g" + ((Long)e.getValue()).longValue() + "';text='" + (String)e.getKey() + "'}");
      }
    }
    buf.append("text{type='bold';text='Lock structure:'}");
    if (!this.structure.isLockable()) {
      buf.append("text{type='bold';text='WARNING! Not all doors have locks.'}");
    }
    if (this.structure.isLocked()) {
      buf.append("checkbox{id='unlock';text='Unlock all doors'}");
    } else {
      buf.append("checkbox{id='lock';text='Lock all doors'}");
    }
    if (getResponder().getCitizenVillage() != null)
    {
      buf.append("checkbox{id='allowVillagers';text='Allow citizens to enter';selected=\"" + (this.structure
        .allowsCitizens()) + "\"}");
      buf.append("checkbox{id='allowAllies';text='Allow allies to enter';selected=\"" + (this.structure
        .allowsAllies()) + "\"}");
      buf.append("checkbox{id='allowKingdom';text='Allow kingdom to enter';selected=\"" + (this.structure
        .allowsKingdom()) + "\"}");
    }
    buf.append("text{type='bold';text='Change name:'}");
    buf.append("input{maxchars='40'; id='sname'; text=\"" + this.structure.getName() + "\"}");
    buf.append("text{type='italic';text='Note! The name may contain the following letters: '}");
    buf.append("text{type='italic';text=\"a-z,A-Z,', and -\"}");
    buf.append("text{text=''}checkbox{id='demolish';text='Destroy this structure';selected='false';confirm=\"You are about to demolish this building" + (this.structure
    
      .hasBridgeEntrance() ? " and connected bridge(s)" : "") + ".\";question=\"Are you sure you want to do that?\"}text{text=''}text{text=''}");
    
    buf.append(createAnswerButton2());
    
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\StructureManagement.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */