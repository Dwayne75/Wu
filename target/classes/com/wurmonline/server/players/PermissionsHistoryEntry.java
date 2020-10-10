package com.wurmonline.server.players;

import java.text.DateFormat;
import java.util.Date;

public class PermissionsHistoryEntry
  implements Comparable<PermissionsHistoryEntry>
{
  private static final DateFormat df = ;
  private final long eventDate;
  private final long playerId;
  public final String performer;
  public final String event;
  
  public PermissionsHistoryEntry(long eventDate, long playerId, String performer, String event)
  {
    this.eventDate = eventDate;
    this.playerId = playerId;
    this.performer = performer;
    this.event = event;
  }
  
  long getEventDate()
  {
    return this.eventDate;
  }
  
  String getPlayerName()
  {
    return this.performer;
  }
  
  String getEvent()
  {
    return this.event;
  }
  
  public String getDate()
  {
    return df.format(new Date(this.eventDate));
  }
  
  public long getPlayerId()
  {
    return this.playerId;
  }
  
  public String getLongDesc()
  {
    if ((this.performer == null) || (this.performer.length() == 0)) {
      return getDate() + "  " + this.event;
    }
    return getDate() + "  " + this.performer + " " + this.event;
  }
  
  public int compareTo(PermissionsHistoryEntry he)
  {
    return Long.compare(this.eventDate, he.eventDate);
  }
  
  public String toString()
  {
    return "PermissionsHistoryEntry [" + getLongDesc() + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\PermissionsHistoryEntry.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */