package com.mysql.jdbc.exceptions;

import java.sql.SQLException;

public class MySQLTransientException
  extends SQLException
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLTransientException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */