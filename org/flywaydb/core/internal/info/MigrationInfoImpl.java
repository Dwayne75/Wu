package org.flywaydb.core.internal.info;

import java.util.Date;
import java.util.Map;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.util.ObjectUtils;

public class MigrationInfoImpl
  implements MigrationInfo
{
  private final ResolvedMigration resolvedMigration;
  private final AppliedMigration appliedMigration;
  private final MigrationInfoContext context;
  private final boolean outOfOrder;
  
  public MigrationInfoImpl(ResolvedMigration resolvedMigration, AppliedMigration appliedMigration, MigrationInfoContext context, boolean outOfOrder)
  {
    this.resolvedMigration = resolvedMigration;
    this.appliedMigration = appliedMigration;
    this.context = context;
    this.outOfOrder = outOfOrder;
  }
  
  public ResolvedMigration getResolvedMigration()
  {
    return this.resolvedMigration;
  }
  
  public AppliedMigration getAppliedMigration()
  {
    return this.appliedMigration;
  }
  
  public MigrationType getType()
  {
    if (this.appliedMigration != null) {
      return this.appliedMigration.getType();
    }
    return this.resolvedMigration.getType();
  }
  
  public Integer getChecksum()
  {
    if (this.appliedMigration != null) {
      return this.appliedMigration.getChecksum();
    }
    return this.resolvedMigration.getChecksum();
  }
  
  public MigrationVersion getVersion()
  {
    if (this.appliedMigration != null) {
      return this.appliedMigration.getVersion();
    }
    return this.resolvedMigration.getVersion();
  }
  
  public String getDescription()
  {
    if (this.appliedMigration != null) {
      return this.appliedMigration.getDescription();
    }
    return this.resolvedMigration.getDescription();
  }
  
  public String getScript()
  {
    if (this.appliedMigration != null) {
      return this.appliedMigration.getScript();
    }
    return this.resolvedMigration.getScript();
  }
  
  public MigrationState getState()
  {
    if (this.appliedMigration == null)
    {
      if (this.resolvedMigration.getVersion() != null)
      {
        if (this.resolvedMigration.getVersion().compareTo(this.context.baseline) < 0) {
          return MigrationState.BELOW_BASELINE;
        }
        if (this.resolvedMigration.getVersion().compareTo(this.context.target) > 0) {
          return MigrationState.ABOVE_TARGET;
        }
        if ((this.resolvedMigration.getVersion().compareTo(this.context.lastApplied) < 0) && (!this.context.outOfOrder)) {
          return MigrationState.IGNORED;
        }
      }
      return MigrationState.PENDING;
    }
    if (this.resolvedMigration == null)
    {
      if (MigrationType.SCHEMA == this.appliedMigration.getType()) {
        return MigrationState.SUCCESS;
      }
      if (MigrationType.BASELINE == this.appliedMigration.getType()) {
        return MigrationState.BASELINE;
      }
      if ((this.appliedMigration.getVersion() == null) || (getVersion().compareTo(this.context.lastResolved) < 0))
      {
        if (this.appliedMigration.isSuccess()) {
          return MigrationState.MISSING_SUCCESS;
        }
        return MigrationState.MISSING_FAILED;
      }
      if (this.appliedMigration.isSuccess()) {
        return MigrationState.FUTURE_SUCCESS;
      }
      return MigrationState.FUTURE_FAILED;
    }
    if (!this.appliedMigration.isSuccess()) {
      return MigrationState.FAILED;
    }
    if (this.appliedMigration.getVersion() == null)
    {
      if (ObjectUtils.nullSafeEquals(this.appliedMigration.getChecksum(), this.resolvedMigration.getChecksum())) {
        return MigrationState.SUCCESS;
      }
      if (this.appliedMigration.getInstalledRank() == ((Integer)this.context.latestRepeatableRuns.get(this.appliedMigration.getDescription())).intValue()) {
        return MigrationState.OUTDATED;
      }
      return MigrationState.SUPERSEEDED;
    }
    if (this.outOfOrder) {
      return MigrationState.OUT_OF_ORDER;
    }
    return MigrationState.SUCCESS;
  }
  
  public Date getInstalledOn()
  {
    if (this.appliedMigration != null) {
      return this.appliedMigration.getInstalledOn();
    }
    return null;
  }
  
  public String getInstalledBy()
  {
    if (this.appliedMigration != null) {
      return this.appliedMigration.getInstalledBy();
    }
    return null;
  }
  
  public Integer getInstalledRank()
  {
    if (this.appliedMigration != null) {
      return Integer.valueOf(this.appliedMigration.getInstalledRank());
    }
    return null;
  }
  
  public Integer getExecutionTime()
  {
    if (this.appliedMigration != null) {
      return Integer.valueOf(this.appliedMigration.getExecutionTime());
    }
    return null;
  }
  
  public String validate()
  {
    if ((this.resolvedMigration == null) && 
      (this.appliedMigration.getType() != MigrationType.SCHEMA) && 
      (this.appliedMigration.getType() != MigrationType.BASELINE) && 
      (this.appliedMigration.getVersion() != null) && ((!this.context.future) || (
      
      (MigrationState.FUTURE_SUCCESS != getState()) && (MigrationState.FUTURE_FAILED != getState())))) {
      return "Detected applied migration not resolved locally: " + getVersion();
    }
    if (!this.context.pending)
    {
      if ((MigrationState.PENDING == getState()) || (MigrationState.IGNORED == getState()))
      {
        if (getVersion() != null) {
          return "Detected resolved migration not applied to database: " + getVersion();
        }
        return "Detected resolved repeatable migration not applied to database: " + getDescription();
      }
      if (MigrationState.OUTDATED == getState()) {
        return "Detected outdated resolved repeatable migration that should be re-applied to database: " + getDescription();
      }
    }
    if ((this.resolvedMigration != null) && (this.appliedMigration != null))
    {
      Object migrationIdentifier = this.appliedMigration.getVersion();
      if (migrationIdentifier == null) {
        migrationIdentifier = this.appliedMigration.getScript();
      }
      if ((getVersion() == null) || (getVersion().compareTo(this.context.baseline) > 0))
      {
        if (this.resolvedMigration.getType() != this.appliedMigration.getType()) {
          return createMismatchMessage("type", migrationIdentifier, this.appliedMigration
            .getType(), this.resolvedMigration.getType());
        }
        if (this.resolvedMigration.getVersion() == null)
        {
          if (this.context.pending) {
            if ((MigrationState.OUTDATED == getState()) || (MigrationState.SUPERSEEDED == getState())) {}
          }
        }
        else if (!ObjectUtils.nullSafeEquals(this.resolvedMigration.getChecksum(), this.appliedMigration.getChecksum())) {
          return createMismatchMessage("checksum", migrationIdentifier, this.appliedMigration
            .getChecksum(), this.resolvedMigration.getChecksum());
        }
        if (!this.resolvedMigration.getDescription().equals(this.appliedMigration.getDescription())) {
          return createMismatchMessage("description", migrationIdentifier, this.appliedMigration
            .getDescription(), this.resolvedMigration.getDescription());
        }
      }
    }
    return null;
  }
  
  private String createMismatchMessage(String mismatch, Object migrationIdentifier, Object applied, Object resolved)
  {
    return String.format("Migration " + mismatch + " mismatch for migration %s\n" + "-> Applied to database : %s\n" + "-> Resolved locally    : %s", new Object[] { migrationIdentifier, applied, resolved });
  }
  
  public int compareTo(MigrationInfo o)
  {
    if ((getInstalledRank() != null) && (o.getInstalledRank() != null)) {
      return getInstalledRank().intValue() - o.getInstalledRank().intValue();
    }
    MigrationState state = getState();
    MigrationState oState = o.getState();
    if (((getInstalledRank() != null) || (o.getInstalledRank() != null)) && (state != MigrationState.BELOW_BASELINE) && (oState != MigrationState.BELOW_BASELINE) && (state != MigrationState.IGNORED) && (oState != MigrationState.IGNORED))
    {
      if (getInstalledRank() != null) {
        return Integer.MIN_VALUE;
      }
      if (o.getInstalledRank() != null) {
        return Integer.MAX_VALUE;
      }
    }
    if ((getVersion() != null) && (o.getVersion() != null)) {
      return getVersion().compareTo(o.getVersion());
    }
    if (getVersion() != null) {
      return Integer.MIN_VALUE;
    }
    if (o.getVersion() != null) {
      return Integer.MAX_VALUE;
    }
    return getDescription().compareTo(o.getDescription());
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    MigrationInfoImpl that = (MigrationInfoImpl)o;
    if (this.appliedMigration != null ? !this.appliedMigration.equals(that.appliedMigration) : that.appliedMigration != null) {
      return false;
    }
    if (!this.context.equals(that.context)) {
      return false;
    }
    return this.resolvedMigration != null ? this.resolvedMigration.equals(that.resolvedMigration) : that.resolvedMigration == null;
  }
  
  public int hashCode()
  {
    int result = this.resolvedMigration != null ? this.resolvedMigration.hashCode() : 0;
    result = 31 * result + (this.appliedMigration != null ? this.appliedMigration.hashCode() : 0);
    result = 31 * result + this.context.hashCode();
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\info\MigrationInfoImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */