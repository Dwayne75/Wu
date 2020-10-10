package com.mysql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class NonRegisteringReplicationDriver
  extends NonRegisteringDriver
{
  public NonRegisteringReplicationDriver()
    throws SQLException
  {}
  
  public Connection connect(String url, Properties info)
    throws SQLException
  {
    return connectReplicationConnection(url, info);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\NonRegisteringReplicationDriver.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */