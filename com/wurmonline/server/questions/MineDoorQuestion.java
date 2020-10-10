package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.MineDoorSettings.MinedoorPermissions;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.io.IOException;
import java.util.Properties;

public final class MineDoorQuestion
  extends Question
{
  private PermissionsByPlayer[] permitted = null;
  private final int tx;
  private final int ty;
  
  public MineDoorQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, int tilex, int tiley)
  {
    super(aResponder, aTitle, aQuestion, 72, aTarget);
    this.tx = tilex;
    this.ty = tiley;
  }
  
  public void answer(Properties aAnswers)
  {
    MineDoorPermission md = MineDoorPermission.getPermission(this.tx, this.ty);
    this.permitted = md.getPermissionsPlayerList().getPermissionsByPlayer();
    boolean changed = false;
    int villid = -1;
    if (getResponder().getCitizenVillage() != null) {
      villid = getResponder().getCitizenVillage().id;
    }
    int removed = 0;
    for (int x = 0; x < this.permitted.length; x++)
    {
      String key = "nperm" + x;
      String val = aAnswers.getProperty(key);
      if ((val != null) && (val.equals("true")))
      {
        removed++;
        md.removeMDPerm(this.permitted[x].getPlayerId());
      }
    }
    if (removed > 0) {
      getResponder().getCommunicator().sendNormalServerMessage("Removed " + removed + " existing permissions.");
    }
    String key = "vperm";
    String val = aAnswers.getProperty(key);
    if ((val != null) && (val.equals("true")))
    {
      changed = (md.setVillageId(villid)) || (changed);
      getResponder().getCommunicator().sendNormalServerMessage("Added the right for your village to use the door.");
    }
    else if ((md.getVillageId() == villid) && (villid >= 0))
    {
      changed = (md.setVillageId(-1)) || (changed);
      getResponder().getCommunicator().sendNormalServerMessage("Removed the right for your village to use the door.");
    }
    key = "alliedperm";
    val = aAnswers.getProperty(key);
    if ((val != null) && (val.equals("true")))
    {
      if (!md.isAllowAllies())
      {
        changed = (md.setAllowAllies(true)) || (changed);
        getResponder().getCommunicator().sendNormalServerMessage("Added the right for your alliance to use the door.");
      }
    }
    else if (md.isAllowAllies())
    {
      changed = (md.setAllowAllies(false)) || (changed);
      getResponder().getCommunicator().sendNormalServerMessage("Removed the right for your alliance to use the door.");
    }
    key = "allperm";
    val = aAnswers.getProperty(key);
    if ((val != null) && (val.equals("true")))
    {
      if (!md.isAllowAll())
      {
        changed = (md.setAllowAll(true)) || (changed);
        getResponder().getCommunicator().sendNormalServerMessage("Added the right for anyone to use the door.");
      }
    }
    else if (md.isAllowAll())
    {
      changed = (md.setAllowAll(false)) || (changed);
      getResponder().getCommunicator().sendNormalServerMessage("Removed the right for anyone to use the door.");
    }
    key = "newperm";
    val = aAnswers.getProperty(key);
    if ((val != null) && (val.length() > 0)) {
      if (md.getPermissionsPlayerList().size() < md.getMaxAllowed())
      {
        long mdid = Players.getInstance().getWurmIdByPlayerName(val);
        if (mdid > 0L)
        {
          byte kingdom = Players.getInstance().getKingdomForPlayer(mdid);
          if (kingdom == getResponder().getKingdomId())
          {
            md.addMDPerm(mdid, MineDoorSettings.MinedoorPermissions.PASS.getValue());
            getResponder().getCommunicator().sendNormalServerMessage("Gave permission to " + val + ".");
          }
          else
          {
            getResponder().getCommunicator().sendNormalServerMessage("You may not permit the enemy " + val + " to enter the mine door.");
          }
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("There is no known player with the name " + val + ".");
        }
      }
      else
      {
        getResponder().getCommunicator().sendNormalServerMessage("May not add new permissions at the moment. You only have 100 keys.");
      }
    }
    key = "newcont";
    val = aAnswers.getProperty(key);
    if ((val != null) && (val.length() > 0))
    {
      long mdid = Players.getInstance().getWurmIdByPlayerName(val);
      if (mdid > 0L)
      {
        byte kingdom = Players.getInstance().getKingdomForPlayer(mdid);
        if (kingdom == getResponder().getKingdomId())
        {
          changed = (md.setController(mdid)) || (changed);
          getResponder().getCommunicator().sendNormalServerMessage("You gave control of the mine door to " + val + ".");
        }
        else
        {
          getResponder().getCommunicator().sendNormalServerMessage("You may not permit the enemy " + val + " to enter the mine door.");
        }
      }
      else
      {
        getResponder().getCommunicator().sendNormalServerMessage("There is no known player with the name " + val + ".");
      }
    }
    key = "newname";
    val = aAnswers.getProperty(key);
    if (val != null) {
      changed = (md.setObjectName(val, getResponder())) || (changed);
    }
    if (changed) {
      try
      {
        md.save();
      }
      catch (IOException localIOException) {}
    }
  }
  
  public void sendQuestion()
  {
    MineDoorPermission md = MineDoorPermission.getPermission(this.tx, this.ty);
    this.permitted = md.getPermissionsPlayerList().getPermissionsByPlayer();
    int villid = -1;
    if (getResponder().getCitizenVillage() != null) {
      villid = getResponder().getCitizenVillage().id;
    }
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("text{type='bold';text=\"Permissions to use mine door:\"}text{text=''}");
    String rvname = "none";
    if (villid >= 0) {
      try
      {
        rvname = Villages.getVillage(villid).getName();
      }
      catch (NoSuchVillageException localNoSuchVillageException1) {}
    }
    if (md.getVillageId() >= 0)
    {
      try
      {
        Village cvill = Villages.getVillage(md.getVillageId());
        buf.append("text{text='Currently the village of " + cvill.getName() + " may use and manage the mine door.'}");
      }
      catch (NoSuchVillageException nsv)
      {
        md.setVillageId(-1);
        try
        {
          md.save();
        }
        catch (IOException localIOException) {}
      }
      buf.append("checkbox{id='vperm';selected='" + (md.getVillageId() == villid) + "';text='Check here if you want everyone in your village (" + rvname + ") to be able to use and manage the mine door.'};");
    }
    else if (villid >= 0)
    {
      buf.append("checkbox{id='vperm';selected='" + (md.getVillageId() == villid) + "';text='Check here if you want everyone in your village (" + rvname + ") to be able to use and manage the mine door.'};");
    }
    buf.append("checkbox{id='allperm';selected='" + md.isAllowAll() + "';text='Check here if you want everyone to be able to use the mine door.'};");
    
    buf.append("checkbox{id='alliedperm';selected='" + md.isAllowAllies() + "';text='Check here if you want your allies to be able to use the mine door.'};");
    if ((getResponder().getCitizenVillage() == null) || (getResponder().getCitizenVillage().getAllianceNumber() <= 0)) {
      buf.append("text{text=\"Note that the alliance setting only has effect if your settlement actually is in an alliance.\"}");
    }
    if (this.permitted.length > 0)
    {
      buf.append("text{text='These are the people who may use and manage the mine door:'}");
      buf.append("table{rows='" + (this.permitted.length + 1) + "';cols='2';label{text='Remove'};label{text='Name'};");
      for (int x = 0; x < this.permitted.length; x++)
      {
        String name = "unknown";
        try
        {
          name = Players.getInstance().getNameFor(this.permitted[x].getPlayerId());
        }
        catch (Exception localException) {}
        buf.append("checkbox{id='nperm" + x + "';selected='false';text=''};label{text='" + name + "'};");
      }
      buf.append("};");
    }
    else if (!md.isAllowAll())
    {
      buf.append("text{text='No other people may use the mine door.'}");
    }
    if (md.getPermissionsPlayerList().size() < md.getMaxAllowed()) {
      buf.append("text{text='Add new person:'};input{maxchars='40'; id='newperm'; text=''};");
    } else {
      buf.append("text{text='You have no more keys for the door to give away.'}");
    }
    if (md.getController() == getResponder().getWurmId())
    {
      buf.append("text{text='Only you may change the controller of this door.'}");
      buf.append("text{text='Change controller of this door to:'};input{maxchars='40'; id='newcont'; text=''};");
      buf.append("text{text=\"Rename this door\"};input{maxchars=\"40\"; id=\"newname\"; text=\"" + md.getObjectName() + "\"};");
    }
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\MineDoorQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */