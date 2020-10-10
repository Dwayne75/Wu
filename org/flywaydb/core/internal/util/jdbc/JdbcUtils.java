package org.flywaydb.core.internal.util.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class JdbcUtils
{
  private static final Log LOG = LogFactory.getLog(JdbcUtils.class);
  
  public static Connection openConnection(DataSource dataSource)
    throws FlywayException
  {
    try
    {
      Connection connection = dataSource.getConnection();
      if (connection == null) {
        throw new FlywayException("Unable to obtain Jdbc connection from DataSource");
      }
      return connection;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to obtain Jdbc connection from DataSource", e);
    }
  }
  
  public static void closeConnection(Connection connection)
  {
    if (connection == null) {
      return;
    }
    try
    {
      connection.close();
    }
    catch (SQLException e)
    {
      LOG.error("Error while closing Jdbc connection", e);
    }
  }
  
  public static void closeStatement(Statement statement)
  {
    if (statement == null) {
      return;
    }
    try
    {
      statement.close();
    }
    catch (SQLException e)
    {
      LOG.error("Error while closing Jdbc statement", e);
    }
  }
  
  public static void closeResultSet(ResultSet resultSet)
  {
    if (resultSet == null) {
      return;
    }
    try
    {
      resultSet.close();
    }
    catch (SQLException e)
    {
      LOG.error("Error while closing Jdbc resultSet", e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\jdbc\JdbcUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */