package org.flywaydb.core.internal.dbsupport.vertica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

public class VerticaDbSupport
  extends DbSupport
{
  public VerticaDbSupport(Connection connection)
  {
    super(new JdbcTemplate(connection, 0));
  }
  
  public String getDbName()
  {
    return "vertica";
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
      result = result.substring(1);
    }
    if (result.contains(",")) {
      return getSchema(result.substring(0, result.indexOf(",")));
    }
    return getSchema(result);
  }
  
  protected String doGetCurrentSchemaName()
    throws SQLException
  {
    (String)this.jdbcTemplate.query("SHOW search_path", new RowMapper()
    {
      public String mapRow(ResultSet rs)
        throws SQLException
      {
        return rs.getString("setting");
      }
    }
    
      ).get(0);
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
      this.jdbcTemplate.execute("SET search_path = v_catalog", new Object[0]);
      return;
    }
    this.jdbcTemplate.execute("SET search_path = " + schema, new Object[0]);
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
    return new VerticaStatementBuilder();
  }
  
  public String doQuote(String identifier)
  {
    return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
  }
  
  public Schema getSchema(String name)
  {
    return new VerticaSchema(this.jdbcTemplate, this, name);
  }
  
  public boolean catalogIsSchema()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\vertica\VerticaDbSupport.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */