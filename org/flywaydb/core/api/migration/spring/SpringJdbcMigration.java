package org.flywaydb.core.api.migration.spring;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract interface SpringJdbcMigration
{
  public abstract void migrate(JdbcTemplate paramJdbcTemplate)
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\migration\spring\SpringJdbcMigration.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */