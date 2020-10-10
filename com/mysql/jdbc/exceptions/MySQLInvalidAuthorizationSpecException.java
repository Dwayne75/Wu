package com.mysql.jdbc.exceptions;

public class MySQLInvalidAuthorizationSpecException
  extends MySQLNonTransientException
{
  public MySQLInvalidAuthorizationSpecException() {}
  
  public MySQLInvalidAuthorizationSpecException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLInvalidAuthorizationSpecException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLInvalidAuthorizationSpecException(String reason)
  {
    super(reason);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLInvalidAuthorizationSpecException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */