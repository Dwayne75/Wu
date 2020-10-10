package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLIntegrityConstraintViolationException;

public class MySQLIntegrityConstraintViolationException
  extends SQLIntegrityConstraintViolationException
{
  public MySQLIntegrityConstraintViolationException() {}
  
  public MySQLIntegrityConstraintViolationException(String reason, String SQLState, int vendorCode)
  {
    super(reason, SQLState, vendorCode);
  }
  
  public MySQLIntegrityConstraintViolationException(String reason, String SQLState)
  {
    super(reason, SQLState);
  }
  
  public MySQLIntegrityConstraintViolationException(String reason)
  {
    super(reason);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\exceptions\jdbc4\MySQLIntegrityConstraintViolationException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */