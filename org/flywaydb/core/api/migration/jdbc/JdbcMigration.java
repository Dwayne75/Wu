package org.flywaydb.core.api.migration.jdbc;

import java.sql.Connection;

public abstract interface JdbcMigration
{
  public abstract void migrate(Connection paramConnection)
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\migration\jdbc\JdbcMigration.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */