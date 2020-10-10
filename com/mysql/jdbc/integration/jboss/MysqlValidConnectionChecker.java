package com.mysql.jdbc.integration.jboss;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jboss.resource.adapter.jdbc.ValidConnectionChecker;

public final class MysqlValidConnectionChecker
  implements ValidConnectionChecker, Serializable
{
  private static final long serialVersionUID = 8909421133577519177L;
  
  public SQLException isValidConnection(Connection conn)
  {
    Statement pingStatement = null;
    try
    {
      pingStatement = conn.createStatement();
      
      pingStatement.executeQuery("/* ping */ SELECT 1").close();
      
      return null;
    }
    catch (SQLException sqlEx)
    {
      return sqlEx;
    }
    finally
    {
      if (pingStatement != null) {
        try
        {
          pingStatement.close();
        }
        catch (SQLException sqlEx) {}
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\integration\jboss\MysqlValidConnectionChecker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */