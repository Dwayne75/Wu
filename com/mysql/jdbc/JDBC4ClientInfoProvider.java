package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Properties;

public abstract interface JDBC4ClientInfoProvider
{
  public abstract void initialize(Connection paramConnection, Properties paramProperties)
    throws SQLException;
  
  public abstract void destroy()
    throws SQLException;
  
  public abstract Properties getClientInfo(Connection paramConnection)
    throws SQLException;
  
  public abstract String getClientInfo(Connection paramConnection, String paramString)
    throws SQLException;
  
  public abstract void setClientInfo(Connection paramConnection, Properties paramProperties)
    throws SQLClientInfoException;
  
  public abstract void setClientInfo(Connection paramConnection, String paramString1, String paramString2)
    throws SQLClientInfoException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\JDBC4ClientInfoProvider.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */