package org.flywaydb.core.internal.dbsupport;

public abstract class Function
  extends SchemaObject
{
  protected String[] args;
  
  public Function(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name, String... args)
  {
    super(jdbcTemplate, dbSupport, schema, name);
    this.args = args;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\Function.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */