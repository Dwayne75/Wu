package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public abstract interface Extension
{
  public abstract void init(Connection paramConnection, Properties paramProperties)
    throws SQLException;
  
  public abstract void destroy();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\Extension.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */