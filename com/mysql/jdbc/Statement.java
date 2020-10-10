package com.mysql.jdbc;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract interface Statement
  extends java.sql.Statement
{
  public abstract void enableStreamingResults()
    throws SQLException;
  
  public abstract void disableStreamingResults()
    throws SQLException;
  
  public abstract void setLocalInfileInputStream(InputStream paramInputStream);
  
  public abstract InputStream getLocalInfileInputStream();
  
  public abstract void setPingTarget(PingTarget paramPingTarget);
  
  public abstract ExceptionInterceptor getExceptionInterceptor();
  
  public abstract void removeOpenResultSet(ResultSet paramResultSet);
  
  public abstract int getOpenResultSetCount();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\Statement.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */