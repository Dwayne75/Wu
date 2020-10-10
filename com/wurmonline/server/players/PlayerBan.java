package com.wurmonline.server.players;

public class PlayerBan
  implements Ban
{
  private String playerName;
  private String reason;
  private long expiry;
  
  public PlayerBan(String playerName, String reason, long expiry)
  {
    this.playerName = playerName;
    this.reason = reason;
    this.expiry = expiry;
  }
  
  public boolean isExpired()
  {
    return System.currentTimeMillis() > getExpiry();
  }
  
  public String getIdentifier()
  {
    return this.playerName;
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\PlayerBan.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */