package org.flywaydb.core.internal.dbsupport;

import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;

public abstract class SchemaObject
{
  protected final JdbcTemplate jdbcTemplate;
  protected final DbSupport dbSupport;
  protected final Schema schema;
  protected final String name;
  
  public SchemaObject(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    this.name = name;
    this.jdbcTemplate = jdbcTemplate;
    this.dbSupport = dbSupport;
    this.schema = schema;
  }
  
  public final Schema getSchema()
  {
    return this.schema;
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final void drop()
  {
    try
    {
      doDrop();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to drop " + this, e);
    }
  }
  
  protected abstract void doDrop()
    throws SQLException;
  
  public String toString()
  {
    return this.dbSupport.quote(new String[] { this.schema.getName(), this.name });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\SchemaObject.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */