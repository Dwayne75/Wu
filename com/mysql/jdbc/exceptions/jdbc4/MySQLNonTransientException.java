package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLNonTransientException;

public class MySQLNonTransientException
  extends SQLNonTransientException
{
  public MySQLNonTransientException() {}
  
  public MySQLNonTransientException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLNonTransientException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLNonTransientException(String reason)
  {
    super(reason);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLNonTransientException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */