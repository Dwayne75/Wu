package com.wurmonline.server.database.migrations;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.flywaydb.core.api.MigrationVersion;

@ParametersAreNonnullByDefault
@Immutable
public abstract class MigrationResult
{
  public abstract boolean isSuccess();
  
  public boolean isError()
  {
    return !isSuccess();
  }
  
  public MigrationResult.MigrationError asError()
  {
    throw new IllegalArgumentException("This migration is not in error");
  }
  
  public MigrationResult.MigrationSuccess asSuccess()
  {
    throw new IllegalArgumentException("This migration is not a success");
  }
  
  static MigrationResult.MigrationError newError(String message)
  {
    return new MigrationResult.MigrationError(message, null);
  }
  
  static MigrationResult.MigrationSuccess newSuccess(MigrationVersion versionBeforeMigration, MigrationVersion versionAfterMigration, int numMigrations)
  {
    return new MigrationResult.MigrationSuccess(versionBeforeMigration, versionAfterMigration, numMigrations, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\database\migrations\MigrationResult.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */