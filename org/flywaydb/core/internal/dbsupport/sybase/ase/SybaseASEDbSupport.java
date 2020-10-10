package org.flywaydb.core.internal.dbsupport.sybase.ase;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class SybaseASEDbSupport
  extends DbSupport
{
  private static final Log LOG = LogFactory.getLog(SybaseASEDbSupport.class);
  
  public SybaseASEDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 0));
  }
  
  public Schema getSchema(String name)
  {
    Schema schema = new SybaseASESchema(this.jdbcTemplate, this, name)
    {
      protected boolean doExists()
        throws SQLException
      {
        return false;
      }
    };
    try
    {
      String currentName = doGetCurrentSchemaName();
      if (currentName.equals(name)) {
        schema = new SybaseASESchema(this.jdbcTemplate, this, name);
      }
    }
    catch (SQLException e)
    {
      LOG.error("Unable to obtain current schema, return non-existing schema", e);
    }
    return schema;
  }
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new SybaseASESqlStatementBuilder();
  }
  
  public String getDbName()
  {
    return "sybaseASE";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.queryForString("select USER_NAME()", new String[0]);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    LOG.info("Sybase does not support setting the schema for the current session. Default schema NOT changed to " + schema);
  }
  
  public String getCurrentUserFunction()
  {
    return "user_name()";
  }
  
  public boolean supportsDdlTransactions()
  {
    return false;
  }
  
  public String getBooleanTrue()
  {
    return "1";
  }
  
  public String getBooleanFalse()
  {
    return "0";
  }
  
  protected String doQuote(String identifier)
  {
    return identifier;
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sybase\ase\SybaseASEDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */