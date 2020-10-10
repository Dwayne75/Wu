package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.MysqlConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;

public class MysqlMigrationStrategy
  implements MigrationStrategy
{
  public static final WurmDatabaseSchema MIGRATION_SCHEMA = WurmDatabaseSchema.LOGIN;
  private final MysqlMigrator migrator;
  
  public MysqlMigrationStrategy(MysqlConnectionFactory connectionFactory)
  {
    this.migrator = new MysqlMigrator(MIGRATION_SCHEMA, connectionFactory);
  }
  
  public MigrationResult migrate()
  {
    return this.migrator.migrate();
  }
  
  public boolean hasPendingMigrations()
  {
    return this.migrator.hasPendingMigrations();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\database\migrations\MysqlMigrationStrategy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */