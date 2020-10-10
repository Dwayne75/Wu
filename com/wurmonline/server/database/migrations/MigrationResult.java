package com.wurmonline.server.database.migrations;

import javax.annotation.Nonnull;
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
  
  public MigrationError asError()
  {
    throw new IllegalArgumentException("This migration is not in error");
  }
  
  public MigrationSuccess asSuccess()
  {
    throw new IllegalArgumentException("This migration is not a success");
  }
  
  @ParametersAreNonnullByDefault
  @Immutable
  public static final class MigrationError
    extends MigrationResult
  {
    private final String message;
    
    private MigrationError(String message)
    {
      super();
      this.message = message;
    }
    
    public boolean isSuccess()
    {
      return false;
    }
    
    @Nonnull
    public final String getMessage()
    {
      return this.message;
    }
    
    public MigrationError asError()
    {
      return this;
    }
  }
  
  @ParametersAreNonnullByDefault
  @Immutable
  public static final class MigrationSuccess
    extends MigrationResult
  {
    private final MigrationVersion versionBeforeMigration;
    private final MigrationVersion versionAfterMigration;
    private final int numMigrations;
    
    private MigrationSuccess(MigrationVersion versionBeforeMigration, MigrationVersion versionAfterMigration, int numMigrations)
    {
      super();
      this.versionBeforeMigration = versionBeforeMigration;
      this.versionAfterMigration = versionAfterMigration;
      this.numMigrations = numMigrations;
    }
    
    public boolean isSuccess()
    {
      return true;
    }
    
    public MigrationVersion getVersionBefore()
    {
      return this.versionBeforeMigration;
    }
    
    public MigrationVersion getVersionAfter()
    {
      return this.versionAfterMigration;
    }
    
    public int getNumMigrations()
    {
      return this.numMigrations;
    }
    
    public MigrationSuccess asSuccess()
    {
      return this;
    }
  }
  
  static MigrationError newError(String message)
  {
    return new MigrationError(message, null);
  }
  
  static MigrationSuccess newSuccess(MigrationVersion versionBeforeMigration, MigrationVersion versionAfterMigration, int numMigrations)
  {
    return new MigrationSuccess(versionBeforeMigration, versionAfterMigration, numMigrations, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\database\migrations\MigrationResult.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */