package com.wurmonline.server.villages;

import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.text.DateFormat;
import java.util.Date;

public final class VillageMessage
  implements Comparable<VillageMessage>
{
  private static final DateFormat df = ;
  private int villageId;
  private long posterId;
  private long toId;
  private String message;
  private int penColour;
  private long posted;
  private boolean everyone;
  
  public VillageMessage(int aVillageId, long aPosterId, long aToId, String aMessage, int thePenColour, long aPosted, boolean everyone)
  {
    this.villageId = aVillageId;
    this.posterId = aPosterId;
    this.toId = aToId;
    this.message = aMessage;
    this.penColour = thePenColour;
    this.posted = aPosted;
    this.everyone = everyone;
  }
  
  public int compareTo(VillageMessage villageMsg)
  {
    if (getVillageId() == villageMsg.getVillageId())
    {
      if (getToId() < villageMsg.getToId()) {
        return -1;
      }
      if (getToId() > villageMsg.getToId()) {
        return 1;
      }
      if (getPostedTime() < villageMsg.getPostedTime()) {
        return 1;
      }
      if (getPostedTime() > villageMsg.getPostedTime()) {
        return -1;
      }
      return 0;
    }
    if (getVillageId() < villageMsg.getVillageId()) {
      return -1;
    }
    return 1;
  }
  
  public int getPenColour()
  {
    return this.penColour;
  }
  
  public final long getPosterId()
  {
    return this.posterId;
  }
  
  public final String getPosterName()
  {
    return getPlayerName(this.posterId);
  }
  
  public final long getToId()
  {
    return this.toId;
  }
  
  public final String getToNmae()
  {
    return getPlayerName(this.toId);
  }
  
  private final String getPlayerName(long id)
  {
    PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(id);
    if (info == null) {
      return "";
    }
    return info.getName();
  }
  
  public final long getPostedTime()
  {
    return this.posted;
  }
  
  public String getDate()
  {
    return df.format(new Date(this.posted));
  }
  
  public final String getMessage()
  {
    return this.message;
  }
  
  public final int getVillageId()
  {
    return this.villageId;
  }
  
  public final boolean isForEveryone()
  {
    return this.everyone;
  }
  
  public final String getVillageName()
  {
    try
    {
      Village village = Villages.getVillage(this.villageId);
      return village.getName();
    }
    catch (NoSuchVillageException nsv) {}
    return "";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\villages\VillageMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */