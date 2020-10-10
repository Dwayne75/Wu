package com.wurmonline.server.players;

import com.wurmonline.server.steam.SteamId;

public abstract interface Ban
{
  public abstract boolean isExpired();
  
  public abstract String getIdentifier();
  
  public abstract String getReason();
  
  public abstract void setReason(String paramString);
  
  public abstract long getExpiry();
  
  public abstract void setExpiry(long paramLong);
  
  public String getUpdateSql()
  {
    return "";
  }
  
  public String getInsertSql()
  {
    return "";
  }
  
  public String getDeleteSql()
  {
    return "";
  }
  
  public static Ban fromString(String identifier)
  {
    return fromString(identifier, "", 0L);
  }
  
  public static Ban fromString(String identifier, String reason, long expiry)
  {
    SteamId id = SteamId.fromAnyString(identifier);
    if (id != null) {
      return new SteamIdBan(id, reason, expiry);
    }
    return new IPBan(identifier, reason, expiry);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\Ban.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */