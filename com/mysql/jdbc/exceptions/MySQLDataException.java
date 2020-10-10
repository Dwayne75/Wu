package com.mysql.jdbc.exceptions;

public class MySQLDataException
  extends MySQLNonTransientException
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLDataException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */