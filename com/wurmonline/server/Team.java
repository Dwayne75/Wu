package com.wurmonline.server;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Team
  extends Group
{
  private Creature leader = null;
  private final Map<Long, Boolean> offlineMembers = new HashMap();
  
  public Team(String aName, Creature _leader)
  {
    super(aName);
    this.leader = _leader;
  }
  
  public boolean isTeam()
  {
    return true;
  }
  
  public boolean isTeamLeader(Creature c)
  {
    return c == this.leader;
  }
  
  public Creature[] getMembers()
  {
    return (Creature[])this.members.values().toArray(new Creature[this.members.size()]);
  }
  
  public final void setNewLeader(Creature newLeader)
  {
    this.leader = newLeader;
    
    Message m = new Message(newLeader, (byte)(newLeader == this.leader ? 14 : 13), "Team", newLeader.getName() + " has been appointed new leader.");
    for (Creature c : this.members.values())
    {
      c.getCommunicator().sendRemoveTeam(newLeader.getName());
      c.getCommunicator().sendAddTeam(newLeader.getName(), newLeader.getWurmId());
      c.getCommunicator().sendMessage(m);
    }
  }
  
  public final void creatureJoinedTeam(Creature joined)
  {
    addMember(joined.getName(), joined);
    Message m = new Message(joined, (byte)(joined == this.leader ? 14 : 13), "Team", "Welcome to team chat.");
    joined.getCommunicator().sendMessage(m);
    for (Creature c : this.members.values())
    {
      c.getCommunicator().sendAddTeam(joined.getName(), joined.getWurmId());
      joined.getCommunicator().sendAddTeam(c.getName(), c.getWurmId());
    }
    if (this.offlineMembers.containsKey(Long.valueOf(joined.getWurmId())))
    {
      Boolean mayInvite = (Boolean)this.offlineMembers.remove(Long.valueOf(joined.getWurmId()));
      joined.setMayInviteTeam(mayInvite.booleanValue());
    }
  }
  
  public final void creatureReconnectedTeam(Creature joined)
  {
    Message m = new Message(joined, (byte)(joined == this.leader ? 14 : 13), "Team", "Welcome to team chat.");
    joined.getCommunicator().sendMessage(m);
    for (Creature c : this.members.values()) {
      joined.getCommunicator().sendAddTeam(c.getName(), c.getWurmId());
    }
  }
  
  public final void creaturePartedTeam(Creature parted, boolean sendRemove)
  {
    for (Creature c : this.members.values())
    {
      c.getCommunicator().sendRemoveTeam(parted.getName());
      if (sendRemove) {
        parted.getCommunicator().sendRemoveTeam(c.getName());
      }
    }
    dropMember(parted.getName());
    if (this.members.size() == 1)
    {
      Creature[] s = getMembers();
      s[0].getCommunicator().sendNormalServerMessage("The team has dissolved.");
      s[0].setTeam(null, true);
    }
    else if (this.members.size() > 1)
    {
      if (parted == this.leader)
      {
        Creature[] s = getMembers();
        setNewLeader(s[0]);
        if (!sendRemove) {
          this.offlineMembers.put(Long.valueOf(parted.getWurmId()), Boolean.valueOf(parted.mayInviteTeam()));
        }
      }
    }
    else
    {
      Groups.removeGroup(this.name);
    }
  }
  
  public final void sendTeamMessage(Creature sender, Message message)
  {
    for (Creature c : this.members.values()) {
      if (!c.isIgnored(message.getSender().getWurmId())) {
        c.getCommunicator().sendMessage(message);
      }
    }
  }
  
  public boolean containsOfflineMember(long wurmid)
  {
    return this.offlineMembers.keySet().contains(Long.valueOf(wurmid));
  }
  
  public final void sendTeamMessage(Creature sender, String message)
  {
    Message m = new Message(sender, (byte)(sender == this.leader ? 14 : 13), "Team", "<" + sender.getName() + "> " + message);
    for (Creature c : this.members.values()) {
      if (!c.isIgnored(m.getSender().getWurmId())) {
        c.getCommunicator().sendMessage(m);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\Team.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */