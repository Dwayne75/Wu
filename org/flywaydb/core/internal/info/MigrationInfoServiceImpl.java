package org.flywaydb.core.internal.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.flywaydb.core.internal.util.Pair;

public class MigrationInfoServiceImpl
  implements MigrationInfoService
{
  private final MigrationResolver migrationResolver;
  private final MetaDataTable metaDataTable;
  private MigrationVersion target;
  private boolean outOfOrder;
  private final boolean pending;
  private final boolean future;
  private List<MigrationInfoImpl> migrationInfos;
  
  public MigrationInfoServiceImpl(MigrationResolver migrationResolver, MetaDataTable metaDataTable, MigrationVersion target, boolean outOfOrder, boolean pending, boolean future)
  {
    this.migrationResolver = migrationResolver;
    this.metaDataTable = metaDataTable;
    this.target = target;
    this.outOfOrder = outOfOrder;
    this.pending = pending;
    this.future = future;
  }
  
  public void refresh()
  {
    Collection<ResolvedMigration> availableMigrations = this.migrationResolver.resolveMigrations();
    List<AppliedMigration> appliedMigrations = this.metaDataTable.allAppliedMigrations();
    
    this.migrationInfos = mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);
    if (MigrationVersion.CURRENT == this.target)
    {
      MigrationInfo current = current();
      if (current == null) {
        this.target = MigrationVersion.EMPTY;
      } else {
        this.target = current.getVersion();
      }
    }
  }
  
  private List<MigrationInfoImpl> mergeAvailableAndAppliedMigrations(Collection<ResolvedMigration> resolvedMigrations, List<AppliedMigration> appliedMigrations)
  {
    MigrationInfoContext context = new MigrationInfoContext();
    context.outOfOrder = this.outOfOrder;
    context.pending = this.pending;
    context.future = this.future;
    context.target = this.target;
    
    Map<MigrationVersion, ResolvedMigration> resolvedMigrationsMap = new TreeMap();
    Map<String, ResolvedMigration> resolvedRepeatableMigrationsMap = new TreeMap();
    for (ResolvedMigration resolvedMigration : resolvedMigrations)
    {
      version = resolvedMigration.getVersion();
      if (version != null)
      {
        if (version.compareTo(context.lastResolved) > 0) {
          context.lastResolved = version;
        }
        resolvedMigrationsMap.put(version, resolvedMigration);
      }
      else
      {
        resolvedRepeatableMigrationsMap.put(resolvedMigration.getDescription(), resolvedMigration);
      }
    }
    MigrationVersion version;
    Object appliedMigrationsMap = new TreeMap();
    
    List<AppliedMigration> appliedRepeatableMigrations = new ArrayList();
    for (AppliedMigration appliedMigration : appliedMigrations)
    {
      version = appliedMigration.getVersion();
      boolean outOfOrder = false;
      if (version != null) {
        if (version.compareTo(context.lastApplied) > 0) {
          context.lastApplied = version;
        } else {
          outOfOrder = true;
        }
      }
      if (appliedMigration.getType() == MigrationType.SCHEMA) {
        context.schema = version;
      }
      if (appliedMigration.getType() == MigrationType.BASELINE) {
        context.baseline = version;
      }
      if (version != null) {
        ((Map)appliedMigrationsMap).put(version, Pair.of(appliedMigration, Boolean.valueOf(outOfOrder)));
      } else {
        appliedRepeatableMigrations.add(appliedMigration);
      }
    }
    Set<MigrationVersion> allVersions = new HashSet();
    allVersions.addAll(resolvedMigrationsMap.keySet());
    allVersions.addAll(((Map)appliedMigrationsMap).keySet());
    
    List<MigrationInfoImpl> migrationInfos = new ArrayList();
    for (MigrationVersion version = allVersions.iterator(); version.hasNext();)
    {
      version = (MigrationVersion)version.next();
      ResolvedMigration resolvedMigration = (ResolvedMigration)resolvedMigrationsMap.get(version);
      Pair<AppliedMigration, Boolean> appliedMigrationInfo = (Pair)((Map)appliedMigrationsMap).get(version);
      if (appliedMigrationInfo == null) {
        migrationInfos.add(new MigrationInfoImpl(resolvedMigration, null, context, false));
      } else {
        migrationInfos.add(new MigrationInfoImpl(resolvedMigration, (AppliedMigration)appliedMigrationInfo.getLeft(), context, ((Boolean)appliedMigrationInfo.getRight()).booleanValue()));
      }
    }
    MigrationVersion version;
    Set<ResolvedMigration> pendingResolvedRepeatableMigrations = new HashSet(resolvedRepeatableMigrationsMap.values());
    for (AppliedMigration appliedRepeatableMigration : appliedRepeatableMigrations)
    {
      ResolvedMigration resolvedMigration = (ResolvedMigration)resolvedRepeatableMigrationsMap.get(appliedRepeatableMigration.getDescription());
      if ((resolvedMigration != null) && (ObjectUtils.nullSafeEquals(appliedRepeatableMigration.getChecksum(), resolvedMigration.getChecksum()))) {
        pendingResolvedRepeatableMigrations.remove(resolvedMigration);
      }
      if ((!context.latestRepeatableRuns.containsKey(appliedRepeatableMigration.getDescription())) || 
        (appliedRepeatableMigration.getInstalledRank() > ((Integer)context.latestRepeatableRuns.get(appliedRepeatableMigration.getDescription())).intValue())) {
        context.latestRepeatableRuns.put(appliedRepeatableMigration.getDescription(), Integer.valueOf(appliedRepeatableMigration.getInstalledRank()));
      }
      migrationInfos.add(new MigrationInfoImpl(resolvedMigration, appliedRepeatableMigration, context, false));
    }
    for (ResolvedMigration pendingResolvedRepeatableMigration : pendingResolvedRepeatableMigrations) {
      migrationInfos.add(new MigrationInfoImpl(pendingResolvedRepeatableMigration, null, context, false));
    }
    Collections.sort(migrationInfos);
    
    return migrationInfos;
  }
  
  public MigrationInfo[] all()
  {
    return (MigrationInfo[])this.migrationInfos.toArray(new MigrationInfoImpl[this.migrationInfos.size()]);
  }
  
  public MigrationInfo current()
  {
    for (int i = this.migrationInfos.size() - 1; i >= 0; i--)
    {
      MigrationInfo migrationInfo = (MigrationInfo)this.migrationInfos.get(i);
      if ((migrationInfo.getState().isApplied()) && (migrationInfo.getVersion() != null)) {
        return migrationInfo;
      }
    }
    return null;
  }
  
  public MigrationInfoImpl[] pending()
  {
    List<MigrationInfoImpl> pendingMigrations = new ArrayList();
    for (MigrationInfoImpl migrationInfo : this.migrationInfos) {
      if (MigrationState.PENDING == migrationInfo.getState()) {
        pendingMigrations.add(migrationInfo);
      }
    }
    return (MigrationInfoImpl[])pendingMigrations.toArray(new MigrationInfoImpl[pendingMigrations.size()]);
  }
  
  public MigrationInfo[] applied()
  {
    List<MigrationInfo> appliedMigrations = new ArrayList();
    for (MigrationInfo migrationInfo : this.migrationInfos) {
      if (migrationInfo.getState().isApplied()) {
        appliedMigrations.add(migrationInfo);
      }
    }
    return (MigrationInfo[])appliedMigrations.toArray(new MigrationInfo[appliedMigrations.size()]);
  }
  
  public MigrationInfo[] resolved()
  {
    List<MigrationInfo> resolvedMigrations = new ArrayList();
    for (MigrationInfo migrationInfo : this.migrationInfos) {
      if (migrationInfo.getState().isResolved()) {
        resolvedMigrations.add(migrationInfo);
      }
    }
    return (MigrationInfo[])resolvedMigrations.toArray(new MigrationInfo[resolvedMigrations.size()]);
  }
  
  public MigrationInfo[] failed()
  {
    List<MigrationInfo> failedMigrations = new ArrayList();
    for (MigrationInfo migrationInfo : this.migrationInfos) {
      if (migrationInfo.getState().isFailed()) {
        failedMigrations.add(migrationInfo);
      }
    }
    return (MigrationInfo[])failedMigrations.toArray(new MigrationInfo[failedMigrations.size()]);
  }
  
  public MigrationInfo[] future()
  {
    List<MigrationInfo> futureMigrations = new ArrayList();
    for (MigrationInfo migrationInfo : this.migrationInfos) {
      if ((migrationInfo.getState() == MigrationState.FUTURE_SUCCESS) || 
        (migrationInfo.getState() == MigrationState.FUTURE_FAILED)) {
        futureMigrations.add(migrationInfo);
      }
    }
    return (MigrationInfo[])futureMigrations.toArray(new MigrationInfo[futureMigrations.size()]);
  }
  
  public MigrationInfo[] outOfOrder()
  {
    List<MigrationInfo> outOfOrderMigrations = new ArrayList();
    for (MigrationInfo migrationInfo : this.migrationInfos) {
      if (migrationInfo.getState() == MigrationState.OUT_OF_ORDER) {
        outOfOrderMigrations.add(migrationInfo);
      }
    }
    return (MigrationInfo[])outOfOrderMigrations.toArray(new MigrationInfo[outOfOrderMigrations.size()]);
  }
  
  public String validate()
  {
    for (MigrationInfoImpl migrationInfo : this.migrationInfos)
    {
      String message = migrationInfo.validate();
      if (message != null) {
        return message;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\info\MigrationInfoServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */