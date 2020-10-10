package com.wurmonline.server.players;

import com.wurmonline.server.steam.SteamId;

public class SteamIdBan
  implements Ban
{
  private SteamId identifier;
  private String reason;
  private long expiry;
  private static final String ADD_BANNED_STEAMID = "insert into BANNED_STEAM_IDS (STEAM_ID,BANREASON,BANEXPIRY) values(?,?,?)";
  private static final String UPDATE_BANNED_STEAMID = "UPDATE BANNED_STEAM_IDS SET BANREASON=?,BANEXPIRY=? WHERE STEAM_ID=?";
  private static final String GET_BANNED_STEAMIDS = "select * from BANNED_STEAM_IDS";
  private static final String REMOVE_BANNED_STEAMID = "delete from BANNED_STEAM_IDS where STEAM_ID=?";
  
  public SteamIdBan(SteamId identifier, String reason, long expiry)
  {
    this.identifier = identifier;
    this.reason = reason;
    this.expiry = expiry;
  }
  
  public boolean isExpired()
  {
    return System.currentTimeMillis() > getExpiry();
  }
  
  public String getIdentifier()
  {
    return this.identifier.toString();
  }
  
  public String getReason()
  {
    return this.reason;
  }
  
  public void setReason(String reason)
  {
    this.reason = reason;
  }
  
  public long getExpiry()
  {
    return this.expiry;
  }
  
  public void setExpiry(long expiry)
  {
    this.expiry = expiry;
  }
  
  public String getUpdateSql()
  {
    return "UPDATE BANNED_STEAM_IDS SET BANREASON=?,BANEXPIRY=? WHERE STEAM_ID=?";
  }
  
  public String getInsertSql()
  {
    return "insert into BANNED_STEAM_IDS (STEAM_ID,BANREASON,BANEXPIRY) values(?,?,?)";
  }
  
  public String getDeleteSql()
  {
    return "delete from BANNED_STEAM_IDS where STEAM_ID=?";
  }
  
  public static String getSelectSql()
  {
    return "select * from BANNED_STEAM_IDS";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\SteamIdBan.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */