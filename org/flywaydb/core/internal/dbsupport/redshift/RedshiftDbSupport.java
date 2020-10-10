package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLSqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public abstract class RedshiftDbSupport
  extends DbSupport
{
  private static final Log LOG = LogFactory.getLog(RedshiftDbSupport.class);
  
  public RedshiftDbSupport(JdbcTemplate jdbcTemplate)
  {
    super(jdbcTemplate);
  }
  
  public String getDbName()
  {
    return "redshift";
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
    String result = this.originalSchema.replace(doQuote("$user"), "").trim();
    if (result.startsWith(",")) {
      result = result.substring(2);
    }
    if (result.contains(",")) {
      return getSchema(result.substring(0, result.indexOf(",")));
    }
    return getSchema(result);
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    String searchPath = this.jdbcTemplate.queryForString("SHOW search_path", new String[0]);
    if ((StringUtils.hasText(searchPath)) && (!searchPath.equals("unset"))) {
      if ((searchPath.contains("$user")) && (!searchPath.contains(doQuote("$user")))) {
        searchPath = searchPath.replace("$user", doQuote("$user"));
      }
    }
    return searchPath;
  }
  
  public void changeCurrentSchemaTo(Schema schema)
  {
    if ((schema.getName().equals(this.originalSchema)) || (this.originalSchema.startsWith(schema.getName() + ",")) || (!schema.exists())) {
      return;
    }
    try
    {
      if ((StringUtils.hasText(this.originalSchema)) && (!this.originalSchema.equals("unset"))) {
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
    if ((!StringUtils.hasLength(schema)) || (schema.equals("unset")))
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
    return new RedshiftSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
  
  public boolean detect()
  {
    try
    {
      return this.jdbcTemplate.queryForInt("select count(*) from information_schema.tables where table_schema = 'pg_catalog' and table_name = 'stl_s3client'", new String[0]) > 0;
    }
    catch (SQLException e)
    {
      LOG.error("Unable to check whether this is a Redshift database", e);
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\redshift\RedshiftDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */