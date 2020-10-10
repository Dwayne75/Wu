package com.wurmonline.server.villages;

import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.util.Date;

public final class RecruitmentAd
  implements Comparable<RecruitmentAd>
{
  private int villageId;
  private long contactId;
  private String description;
  private Date created;
  private int kingdom;
  
  public RecruitmentAd(int _villageId, long _contactId, String _description, Date _created, int _kingdom)
  {
    this.villageId = _villageId;
    this.contactId = _contactId;
    this.description = _description;
    this.created = _created;
    this.kingdom = _kingdom;
  }
  
  public int compareTo(RecruitmentAd ad1)
  {
    if (ad1.getVillageId() == getVillageId()) {
      return 1;
    }
    return 0;
  }
  
  public final long getContactId()
  {
    return this.contactId;
  }
  
  public final String getContactName()
  {
    PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(this.contactId);
    if (info == null) {
      return "";
    }
    return info.getName();
  }
  
  public final Date getCreated()
  {
    return this.created;
  }
  
  public final String getDescription()
  {
    return this.description;
  }
  
  public final int getKingdom()
  {
    return this.kingdom;
  }
  
  public final int getVillageId()
  {
    return this.villageId;
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
  
  public void setContactId(long _contactId)
  {
    this.contactId = _contactId;
  }
  
  public void setCreated(Date date)
  {
    this.created = date;
  }
  
  public void setDescription(String text)
  {
    this.description = text;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\villages\RecruitmentAd.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */