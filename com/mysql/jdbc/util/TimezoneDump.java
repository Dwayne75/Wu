package com.mysql.jdbc.util;

import com.mysql.jdbc.TimeUtil;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TimezoneDump
{
  private static final String DEFAULT_URL = "jdbc:mysql:///test";
  
  public static void main(String[] args)
    throws Exception
  {
    String jdbcUrl = "jdbc:mysql:///test";
    if ((args.length == 1) && (args[0] != null)) {
      jdbcUrl = args[0];
    }
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    ResultSet rs = DriverManager.getConnection(jdbcUrl).createStatement().executeQuery("SHOW VARIABLES LIKE 'timezone'");
    while (rs.next())
    {
      String timezoneFromServer = rs.getString(2);
      System.out.println("MySQL timezone name: " + timezoneFromServer);
      
      String canonicalTimezone = TimeUtil.getCanoncialTimezone(timezoneFromServer, null);
      
      System.out.println("Java timezone name: " + canonicalTimezone);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\util\TimezoneDump.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */