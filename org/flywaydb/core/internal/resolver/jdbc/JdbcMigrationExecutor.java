package org.flywaydb.core.internal.resolver.jdbc;

import java.sql.Connection;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;

public class JdbcMigrationExecutor
  implements MigrationExecutor
{
  private final JdbcMigration jdbcMigration;
  
  public JdbcMigrationExecutor(JdbcMigration jdbcMigration)
  {
    this.jdbcMigration = jdbcMigration;
  }
  
  public void execute(Connection connection)
  {
    try
    {
      this.jdbcMigration.migrate(connection);
    }
    catch (Exception e)
    {
      throw new FlywayException("Migration failed !", e);
    }
  }
  
  public boolean executeInTransaction()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\jdbc\JdbcMigrationExecutor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */