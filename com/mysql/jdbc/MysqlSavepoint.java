package com.mysql.jdbc;

import java.rmi.server.UID;
import java.sql.SQLException;
import java.sql.Savepoint;

public class MysqlSavepoint
  implements Savepoint
{
  private String savepointName;
  private ExceptionInterceptor exceptionInterceptor;
  
  private static String getUniqueId()
  {
    String uidStr = new UID().toString();
    
    int uidLength = uidStr.length();
    
    StringBuffer safeString = new StringBuffer(uidLength);
    for (int i = 0; i < uidLength; i++)
    {
      char c = uidStr.charAt(i);
      if ((Character.isLetter(c)) || (Character.isDigit(c))) {
        safeString.append(c);
      } else {
        safeString.append('_');
      }
    }
    return safeString.toString();
  }
  
  MysqlSavepoint(ExceptionInterceptor exceptionInterceptor)
    throws SQLException
  {
    this(getUniqueId(), exceptionInterceptor);
  }
  
  MysqlSavepoint(String name, ExceptionInterceptor exceptionInterceptor)
    throws SQLException
  {
    if ((name == null) || (name.length() == 0)) {
      throw SQLError.createSQLException("Savepoint name can not be NULL or empty", "S1009", exceptionInterceptor);
    }
    this.savepointName = name;
    
    this.exceptionInterceptor = exceptionInterceptor;
  }
  
  public int getSavepointId()
    throws SQLException
  {
    throw SQLError.createSQLException("Only named savepoints are supported.", "S1C00", this.exceptionInterceptor);
  }
  
  public String getSavepointName()
    throws SQLException
  {
    return this.savepointName;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\MysqlSavepoint.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */