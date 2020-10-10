package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLTransientConnectionException;

public class MySQLTransientConnectionException
  extends SQLTransientConnectionException
{
  public MySQLTransientConnectionException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLTransientConnectionException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLTransientConnectionException(String reason)
  {
    super(reason);
  }
  
  public MySQLTransientConnectionException() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLTransientConnectionException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */