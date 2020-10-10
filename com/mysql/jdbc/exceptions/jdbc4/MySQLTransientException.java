package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLTransientException;

public class MySQLTransientException
  extends SQLTransientException
{
  public MySQLTransientException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLTransientException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLTransientException(String reason)
  {
    super(reason);
  }
  
  public MySQLTransientException() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLTransientException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */