package org.flywaydb.core.internal.dbsupport.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class MySQLDbSupport
  extends DbSupport
{
  private static final Log LOG = LogFactory.getLog(MySQLDbSupport.class);
  
  public MySQLDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 12));
  }
  
  public String getDbName()
  {
    return "mysql";
  }
  
  public String getCurrentUserFunction()
  {
    return "SUBSTRING_INDEX(USER(),'@',1)";
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    return this.jdbcTemplate.getConnection().getCatalog();
  }
  
  public void changeCurrentSchemaTo(Schema schema)
  {
    if ((schema.getName().equals(this.originalSchema)) || (!schema.exists())) {
      return;
    }
    try
    {
      doChangeCurrentSchemaTo(schema.getName());
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error setting current schema to " + schema, e);
    }
  }
  
  protected void doChangeCurrentSchemaTo(String schema)
    throws SQLException
  {
    if (!StringUtils.hasLength(schema)) {
      try
      {
        String newDb = quote(new String[] { UUID.randomUUID().toString() });
        this.jdbcTemplate.execute("CREATE SCHEMA " + newDb, new Object[0]);
        this.jdbcTemplate.execute("USE " + newDb, new Object[0]);
        this.jdbcTemplate.execute("DROP SCHEMA " + newDb, new Object[0]);
      }
      catch (Exception e)
      {
        LOG.warn("Unable to restore connection to having no default schema: " + e.getMessage());
      }
    } else {
      this.jdbcTemplate.getConnection().setCatalog(schema);
    }
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
  
  public SqlStatementBuilder createSqlStatementBuilder()
  {
    return new MySQLSqlStatementBuilder();
  }
  
  public String doQuote(String identifier)
  {
    return "`" + identifier + "`";
  }
  
  public Schema getSchema(String name)
  {
    return new MySQLSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\mysql\MySQLDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */