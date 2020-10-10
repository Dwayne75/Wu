package com.mysql.jdbc.exceptions;

public class MySQLSyntaxErrorException
  extends MySQLNonTransientException
{
  public MySQLSyntaxErrorException() {}
  
  public MySQLSyntaxErrorException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLSyntaxErrorException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLSyntaxErrorException(String reason)
  {
    super(reason);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLSyntaxErrorException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */