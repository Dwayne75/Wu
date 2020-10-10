package org.flywaydb.core.internal.dbsupport;

public abstract class Type
  extends SchemaObject
{
  public Type(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    super(jdbcTemplate, dbSupport, schema, name);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\Type.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */