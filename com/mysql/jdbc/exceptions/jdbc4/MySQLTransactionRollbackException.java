package com.mysql.jdbc.exceptions.jdbc4;

import com.mysql.jdbc.exceptions.DeadlockTimeoutRollbackMarker;
import java.sql.SQLTransactionRollbackException;

public class MySQLTransactionRollbackException
  extends SQLTransactionRollbackException
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLTransactionRollbackException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */