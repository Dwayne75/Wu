package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLSyntaxErrorException;

public class MySQLSyntaxErrorException
  extends SQLSyntaxErrorException
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLSyntaxErrorException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */