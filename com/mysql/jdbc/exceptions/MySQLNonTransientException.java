package com.mysql.jdbc.exceptions;

import java.sql.SQLException;

public class MySQLNonTransientException
  extends SQLException
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLNonTransientException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */