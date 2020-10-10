package org.flywaydb.core.internal.resolver.spring;

import java.sql.Connection;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class SpringJdbcMigrationExecutor
  implements MigrationExecutor
{
  private final SpringJdbcMigration springJdbcMigration;
  
  public SpringJdbcMigrationExecutor(SpringJdbcMigration springJdbcMigration)
  {
    this.springJdbcMigration = springJdbcMigration;
  }
  
  public void execute(Connection connection)
  {
    try
    {
      this.springJdbcMigration.migrate(new JdbcTemplate(new SingleConnectionDataSource(connection, true)));
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\spring\SpringJdbcMigrationExecutor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */