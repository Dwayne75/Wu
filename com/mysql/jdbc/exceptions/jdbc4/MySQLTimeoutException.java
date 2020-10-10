package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLTimeoutException;

public class MySQLTimeoutException
  extends SQLTimeoutException
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
  
  public int getErrorCode()
  {
    return super.getErrorCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLTimeoutException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */