package org.flywaydb.core.internal.dbsupport.sqlserver;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class SQLServerDbSupport
  extends DbSupport
{
  private static final Log LOG = LogFactory.getLog(SQLServerDbSupport.class);
  private static boolean schemaMessagePrinted;
  
  public SQLServerDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public String getDbName()
  {
    return "sqlserver";
  }
  
  public String getCurrentUserFunction()
  {
    return "SUSER_SNAME()";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("SELECT SCHEMA_NAME()", new String[0]);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    if (!schemaMessagePrinted)
    {
      LOG.info("SQLServer does not support setting the schema for the current session. Default schema NOT changed to " + schema);
      
      schemaMessagePrinted = true;
    }
  }
  
  public boolean supportsDdlTransactions()
  {
    return true;
  }
  
  public String getBooleanTrue()
  {
    return "1";
  }
  
  public String getBooleanFalse()
  {
    return "0";
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new SQLServerSqlStatementBuilder();
  }
  
  private String escapeIdentifier(String identifier)
  {
    return StringUtils.replaceAll(identifier, "]", "]]");
  }
  
  public String doQuote(String identifier)
  {
    return "[" + escapeIdentifier(identifier) + "]";
  }
  
  public Schema getSchema(String name)
  {
    return new SQLServerSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sqlserver\SQLServerDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */