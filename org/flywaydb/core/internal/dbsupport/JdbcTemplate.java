package org.flywaydb.core.internal.dbsupport;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class JdbcTemplate
{
  private static final Log LOG = LogFactory.getLog(JdbcTemplate.class);
  private final Connection connection;
  private final int nullType;
  
  public JdbcTemplate(Connection connection, int nullType)
  {
    this.connection = connection;
    this.nullType = nullType;
  }
  
  public Connection getConnection()
  {
    return this.connection;
  }
  
  public List<Map<String, String>> queryForList(String query, String... params)
    throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try
    {
      statement = this.connection.prepareStatement(query);
      for (int i = 0; i < params.length; i++) {
        statement.setString(i + 1, params[i]);
      }
      resultSet = statement.executeQuery();
      
      List<Map<String, String>> result = new ArrayList();
      while (resultSet.next())
      {
        Map<String, String> rowMap = new HashMap();
        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
          rowMap.put(resultSet.getMetaData().getColumnLabel(i), resultSet.getString(i));
        }
        result.add(rowMap);
      }
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);
    }
    List<Map<String, String>> result;
    return result;
  }
  
  public List<String> queryForStringList(String query, String... params)
    throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try
    {
      statement = this.connection.prepareStatement(query);
      for (int i = 0; i < params.length; i++) {
        statement.setString(i + 1, params[i]);
      }
      resultSet = statement.executeQuery();
      
      List<String> result = new ArrayList();
      while (resultSet.next()) {
        result.add(resultSet.getString(1));
      }
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);
    }
    List<String> result;
    return result;
  }
  
  public int queryForInt(String query, String... params)
    throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try
    {
      statement = this.connection.prepareStatement(query);
      for (int i = 0; i < params.length; i++) {
        statement.setString(i + 1, params[i]);
      }
      resultSet = statement.executeQuery();
      resultSet.next();
      result = resultSet.getInt(1);
    }
    finally
    {
      int result;
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);
    }
    int result;
    return result;
  }
  
  public String queryForString(String query, String... params)
    throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try
    {
      statement = this.connection.prepareStatement(query);
      for (int i = 0; i < params.length; i++) {
        statement.setString(i + 1, params[i]);
      }
      resultSet = statement.executeQuery();
      String result = null;
      if (resultSet.next()) {
        result = resultSet.getString(1);
      }
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);
    }
    String result;
    return result;
  }
  
  public DatabaseMetaData getMetaData()
    throws SQLException
  {
    return this.connection.getMetaData();
  }
  
  public void execute(String sql, Object... params)
    throws SQLException
  {
    PreparedStatement statement = null;
    try
    {
      statement = prepareStatement(sql, params);
      statement.execute();
    }
    finally
    {
      JdbcUtils.closeStatement(statement);
    }
  }
  
  public void executeStatement(String sql)
    throws SQLException
  {
    Statement statement = null;
    try
    {
      statement = this.connection.createStatement();
      statement.setEscapeProcessing(false);
      boolean hasResults = false;
      try
      {
        hasResults = statement.execute(sql);
      }
      finally
      {
        SQLWarning warning;
        int updateCount;
        SQLWarning warning = statement.getWarnings();
        while (warning != null)
        {
          if ("00000".equals(warning.getSQLState())) {
            LOG.info("DB: " + warning.getMessage());
          } else {
            LOG.warn("DB: " + warning.getMessage() + " (SQL State: " + warning
              .getSQLState() + " - Error Code: " + warning.getErrorCode() + ")");
          }
          warning = warning.getNextWarning();
        }
        int updateCount = -1;
        while ((hasResults) || ((updateCount = statement.getUpdateCount()) != -1))
        {
          if (updateCount != -1) {
            LOG.debug("Update Count: " + updateCount);
          }
          hasResults = statement.getMoreResults();
        }
      }
    }
    finally
    {
      JdbcUtils.closeStatement(statement);
    }
  }
  
  public void update(String sql, Object... params)
    throws SQLException
  {
    PreparedStatement statement = null;
    try
    {
      statement = prepareStatement(sql, params);
      statement.executeUpdate();
    }
    finally
    {
      JdbcUtils.closeStatement(statement);
    }
  }
  
  private PreparedStatement prepareStatement(String sql, Object[] params)
    throws SQLException
  {
    PreparedStatement statement = this.connection.prepareStatement(sql);
    for (int i = 0; i < params.length; i++) {
      if (params[i] == null) {
        statement.setNull(i + 1, this.nullType);
      } else if ((params[i] instanceof Integer)) {
        statement.setInt(i + 1, ((Integer)params[i]).intValue());
      } else if ((params[i] instanceof Boolean)) {
        statement.setBoolean(i + 1, ((Boolean)params[i]).booleanValue());
      } else {
        statement.setString(i + 1, (String)params[i]);
      }
    }
    return statement;
  }
  
  public <T> List<T> query(String query, RowMapper<T> rowMapper)
    throws SQLException
  {
    Statement statement = null;
    ResultSet resultSet = null;
    try
    {
      statement = this.connection.createStatement();
      resultSet = statement.executeQuery(query);
      
      List<T> results = new ArrayList();
      while (resultSet.next()) {
        results.add(rowMapper.mapRow(resultSet));
      }
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
      JdbcUtils.closeStatement(statement);
    }
    List<T> results;
    return results;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\JdbcTemplate.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */