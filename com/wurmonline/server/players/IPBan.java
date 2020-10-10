package com.wurmonline.server.players;

public final class IPBan
  implements Ban
{
  private final String identifier;
  private String reason;
  private long expiry;
  private static final String ADD_BANNED_IP = "insert into BANNEDIPS (IPADDRESS,BANREASON,BANEXPIRY) values(?,?,?)";
  private static final String UPDATE_BANNED_IP = "UPDATE BANNEDIPS SET BANREASON=?,BANEXPIRY=? WHERE IPADDRESS=?";
  private static final String GET_BANNED_IPS = "select * from BANNEDIPS";
  private static final String REMOVE_BANNED_IP = "delete from BANNEDIPS where IPADDRESS=?";
  
  public IPBan(String _identifier, String _reason, long _expiry)
  {
    this.identifier = _identifier;
    setReason(_reason);
    setExpiry(_expiry);
  }
  
  public boolean isExpired()
  {
    return System.currentTimeMillis() > getExpiry();
  }
  
  public String getIdentifier()
  {
    return this.identifier;
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
    return "UPDATE BANNEDIPS SET BANREASON=?,BANEXPIRY=? WHERE IPADDRESS=?";
  }
  
  public String getInsertSql()
  {
    return "insert into BANNEDIPS (IPADDRESS,BANREASON,BANEXPIRY) values(?,?,?)";
  }
  
  public String getDeleteSql()
  {
    return "delete from BANNEDIPS where IPADDRESS=?";
  }
  
  public static String getSelectSql()
  {
    return "select * from BANNEDIPS";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\IPBan.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */