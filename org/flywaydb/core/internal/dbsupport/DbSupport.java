package org.flywaydb.core.internal.dbsupport;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;

public abstract class DbSupport
{
  protected final JdbcTemplate jdbcTemplate;
  protected final String originalSchema;
  
  public DbSupport(JdbcTemplate jdbcTemplate)
  {
    this.jdbcTemplate = jdbcTemplate;
    this.originalSchema = (jdbcTemplate.getConnection() == null ? null : getCurrentSchemaName());
  }
  
  public JdbcTemplate getJdbcTemplate()
  {
    return this.jdbcTemplate;
  }
  
  public abstract Schema getSchema(String paramString);
  
  public abstract SqlStatementBuilder createSqlStatementBuilder();
  
  public abstract String getDbName();
  
  public Schema getOriginalSchema()
  {
    if (this.originalSchema == null) {
      return null;
    }
    return getSchema(this.originalSchema);
  }
  
  public String getCurrentSchemaName()
  {
    try
    {
      return doGetCurrentSchemaName();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to retrieve the current schema for the connection", e);
    }
  }
  
  protected abstract String doGetCurrentSchemaName()
    throws SQLException;
  
  public void changeCurrentSchemaTo(Schema schema)
  {
    if ((schema.getName().equals(this.originalSchema)) || (!schema.exists())) {
      return;
    }
    try
    {
      doChangeCurrentSchemaTo(schema.toString());
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error setting current schema to " + schema, e);
    }
  }
  
  public void restoreCurrentSchema()
  {
    try
    {
      doChangeCurrentSchemaTo(this.originalSchema);
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error restoring current schema to its original setting", e);
    }
  }
  
  protected abstract void doChangeCurrentSchemaTo(String paramString)
    throws SQLException;
  
  public abstract String getCurrentUserFunction();
  
  public abstract boolean supportsDdlTransactions();
  
  public abstract String getBooleanTrue();
  
  public abstract String getBooleanFalse();
  
  public String quote(String... identifiers)
  {
    String result = "";
    
    boolean first = true;
    for (String identifier : identifiers)
    {
      if (!first) {
        result = result + ".";
      }
      first = false;
      result = result + doQuote(identifier);
    }
    return result;
  }
  
  protected abstract String doQuote(String paramString);
  
  public abstract boolean catalogIsSchema();
  
  public void executePgCopy(Connection connection, String sql)
    throws SQLException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\DbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */