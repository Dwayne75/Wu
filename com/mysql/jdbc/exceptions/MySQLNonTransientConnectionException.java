package com.mysql.jdbc.exceptions;

public class MySQLNonTransientConnectionException
  extends MySQLNonTransientException
{
  public MySQLNonTransientConnectionException() {}
  
  public MySQLNonTransientConnectionException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLNonTransientConnectionException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLNonTransientConnectionException(String reason)
  {
    super(reason);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLNonTransientConnectionException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */