package com.mysql.jdbc.exceptions;

public class MySQLTransactionRollbackException
  extends MySQLTransientException
  implements DeadlockTimeoutRollbackMarker
{
  public MySQLTransactionRollbackException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLTransactionRollbackException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLTransactionRollbackException(String reason)
  {
    super(reason);
  }
  
  public MySQLTransactionRollbackException() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\MySQLTransactionRollbackException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */