package com.mysql.jdbc;

import java.sql.SQLException;

public abstract interface ExceptionInterceptor
  extends Extension
{
  public abstract SQLException interceptException(SQLException paramSQLException);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\ExceptionInterceptor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */