package com.mysql.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Driver
  extends NonRegisteringDriver
  implements java.sql.Driver
{
  public Driver()
    throws SQLException
  {}
  
  static
  {
    try
    {
      DriverManager.registerDriver(new Driver());
    }
    catch (SQLException E)
    {
      throw new RuntimeException("Can't register driver!");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\Driver.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */