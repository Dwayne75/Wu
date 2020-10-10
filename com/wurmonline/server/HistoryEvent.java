package com.wurmonline.server;

import java.text.DateFormat;
import java.util.Date;
import net.jcip.annotations.Immutable;

@Immutable
public final class HistoryEvent
  implements Comparable<HistoryEvent>
{
  private final DateFormat df = DateFormat.getDateTimeInstance();
  public final long time;
  public final String performer;
  public final String event;
  public final int identifier;
  
  public HistoryEvent(long aTime, String aPerformer, String aEvent, int aIdentifier)
  {
    this.time = aTime;
    this.performer = aPerformer;
    this.event = aEvent;
    this.identifier = aIdentifier;
  }
  
  public String getDate()
  {
    return this.df.format(new Date(this.time));
  }
  
  public String getLongDesc()
  {
    if ((this.performer == null) || (this.performer.length() == 0)) {
      return getDate() + "  " + this.event;
    }
    return getDate() + "  " + this.performer + " " + this.event;
  }
  
  public int compareTo(HistoryEvent he)
  {
    return Long.compare(this.time, he.time);
  }
  
  public String toString()
  {
    return "HistoryEvent [" + getLongDesc() + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\HistoryEvent.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */