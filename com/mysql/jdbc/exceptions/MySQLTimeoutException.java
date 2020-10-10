package com.mysql.jdbc.exceptions;

public class MySQLTimeoutException
  extends MySQLTransientException
{
  public MySQLTimeoutException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLTimeoutException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLTimeoutException(String reason)
  {
    super(reason);
  }
  
  public MySQLTimeoutException()
  {
    super("Statement cancelled due to timeout or client request");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLTimeoutException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */