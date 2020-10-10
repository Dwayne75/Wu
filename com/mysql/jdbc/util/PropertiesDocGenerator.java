package com.mysql.jdbc.util;

import com.mysql.jdbc.ConnectionPropertiesImpl;
import java.io.PrintStream;
import java.sql.SQLException;

public class PropertiesDocGenerator
  extends ConnectionPropertiesImpl
{
  public static void main(String[] args)
    throws SQLException
  {
    System.out.println(new PropertiesDocGenerator().exposeAsXml());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\util\PropertiesDocGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */