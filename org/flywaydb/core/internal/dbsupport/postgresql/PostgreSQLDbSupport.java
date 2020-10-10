package org.flywaydb.core.internal.dbsupport.postgresql;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class PostgreSQLDbSupport
  extends DbSupport
{
  public PostgreSQLDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 0));
  }
  
  public String getDbName()
  {
    return "postgresql";
  }
  
  public String getCurrentUserFunction()
  {
    return "current_user";
  }
  
  public Schema getOriginalSchema()
  {
    if (this.originalSchema == null) {
      return null;
    }
    return getSchema(getFirstSchemaFromSearchPath(this.originalSchema));
  }
  
  String getFirstSchemaFromSearchPath(String searchPath)
  {
    String result = searchPath.replace(doQuote("$user"), "").trim();
    if (result.startsWith(",")) {
      result = result.substring(1);
    }
    if (result.contains(",")) {
      result = result.substring(0, result.indexOf(","));
    }
    result = result.trim();
    if ((result.startsWith("\"")) && (result.endsWith("\"")) && (!result.endsWith("\\\"")) && (result.length() > 1)) {
      result = result.substring(1, result.length() - 1);
    }
    return result;
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("SHOW search_path", new String[0]);
  }
  
  public void changeCurrentSchemaTo(Schema schema)
  {
    if ((schema.getName().equals(this.originalSchema)) || (this.originalSchema.startsWith(schema.getName() + ",")) || (!schema.exists())) {
      return;
    }
    try
    {
      if (StringUtils.hasText(this.originalSchema)) {
        doChangeCurrentSchemaTo(schema.toString() + "," + this.originalSchema);
      } else {
        doChangeCurrentSchemaTo(schema.toString());
      }
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error setting current schema to " + schema, e);
    }
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    if (!StringUtils.hasLength(schema))
    {
      this.jdbcTemplate.execute("SELECT set_config('search_path', '', false)", new Object[0]);
      return;
    }
    this.jdbcTemplate.execute("SET search_path = " + schema, new Object[0]);
  }
  
  public boolean supportsDdlTransactions()
  {
    return true;
  }
  
  public String getBooleanTrue()
  {
    return "TRUE";
  }
  
  public String getBooleanFalse()
  {
    return "FALSE";
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new PostgreSQLSqlStatementBuilder();
  }
  
  public String doQuote(String identifier)
  {
    return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new PostgreSQLSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
  
  public void executePgCopy(Connection connection, String sql)
    throws SQLException
  {
    int split = sql.indexOf(";");
    String statement = sql.substring(0, split);
    String data = sql.substring(split + 1).trim();
    
    CopyManager copyManager = new CopyManager((BaseConnection)connection.unwrap(BaseConnection.class));
    try
    {
      copyManager.copyIn(statement, new StringReader(data));
    }
    catch (IOException e)
    {
      throw new SQLException("Unable to execute COPY operation", e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\postgresql\PostgreSQLDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */