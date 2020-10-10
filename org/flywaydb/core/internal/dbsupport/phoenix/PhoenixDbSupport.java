package org.flywaydb.core.internal.dbsupport.phoenix;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class PhoenixDbSupport
  extends DbSupport
{
  private static final Log LOG = LogFactory.getLog(PhoenixDbSupport.class);
  
  public PhoenixDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public String getDbName()
  {
    return "phoenix";
  }
  
  public Schema getOriginalSchema()
  {
    return getSchema(this.originalSchema);
  }
  
  public String quote(String... identifiers)
  {
    String result = "";
    
    boolean first = true;
    boolean lastNull = false;
    for (String identifier : identifiers)
    {
      if ((!first) && (!lastNull)) {
        result = result + ".";
      }
      first = false;
      if (identifier == null)
      {
        lastNull = true;
      }
      else
      {
        result = result + doQuote(identifier);
        lastNull = false;
      }
    }
    return result;
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return null;
  }
  
  public void changeCurrentSchemaTo(Schema schema)
  {
    LOG.info("Phoenix does not support setting the schema. Default schema NOT changed to " + schema);
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    LOG.info("Phoenix does not support setting the schema. Default schema NOT changed to " + schema);
  }
  
  public String getCurrentUserFunction()
  {
    String userName = null;
    try
    {
      userName = this.jdbcTemplate.getMetaData().getUserName();
    }
    catch (SQLException localSQLException) {}
    return userName;
  }
  
  public boolean supportsDdlTransactions()
  {
    return false;
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
    return new SqlStatementBuilder();
  }
  
  public String doQuote(String identifier)
  {
    return "\"" + identifier + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new PhoenixSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\phoenix\PhoenixDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */