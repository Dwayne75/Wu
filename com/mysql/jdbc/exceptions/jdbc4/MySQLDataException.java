package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLDataException;

public class MySQLDataException
  extends SQLDataException
{
  public MySQLDataException() {}
  
  public MySQLDataException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLDataException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLDataException(String reason)
  {
    super(reason);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLDataException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */