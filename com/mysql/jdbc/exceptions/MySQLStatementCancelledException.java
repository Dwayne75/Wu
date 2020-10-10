package com.mysql.jdbc.exceptions;

public class MySQLStatementCancelledException
  extends MySQLNonTransientException
{
  public MySQLStatementCancelledException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLStatementCancelledException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLStatementCancelledException(String reason)
  {
    super(reason);
  }
  
  public MySQLStatementCancelledException()
  {
    super("Statement cancelled due to client request");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLStatementCancelledException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */