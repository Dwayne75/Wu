package org.flywaydb.core.internal.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.resolver.jdbc.JdbcMigrationResolver;
import org.flywaydb.core.internal.resolver.spring.SpringJdbcMigrationResolver;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Scanner;

public class CompositeMigrationResolver
  implements MigrationResolver
{
  private Collection<MigrationResolver> migrationResolvers = new ArrayList();
  private List<ResolvedMigration> availableMigrations;
  
  public CompositeMigrationResolver(DbSupport dbSupport, Scanner scanner, FlywayConfiguration config, Locations locations, String encoding, String sqlMigrationPrefix, String repeatableSqlMigrationPrefix, String sqlMigrationSeparator, String sqlMigrationSuffix, PlaceholderReplacer placeholderReplacer, MigrationResolver... customMigrationResolvers)
  {
    if (!config.isSkipDefaultResolvers()) {
      for (Location location : locations.getLocations())
      {
        this.migrationResolvers.add(new SqlMigrationResolver(dbSupport, scanner, location, placeholderReplacer, encoding, sqlMigrationPrefix, repeatableSqlMigrationPrefix, sqlMigrationSeparator, sqlMigrationSuffix));
        
        this.migrationResolvers.add(new JdbcMigrationResolver(scanner, location, config));
        if (new FeatureDetector(scanner.getClassLoader()).isSpringJdbcAvailable()) {
          this.migrationResolvers.add(new SpringJdbcMigrationResolver(scanner, location, config));
        }
      }
    }
    this.migrationResolvers.addAll(Arrays.asList(customMigrationResolvers));
  }
  
  public List<ResolvedMigration> resolveMigrations()
  {
    if (this.availableMigrations == null) {
      this.availableMigrations = doFindAvailableMigrations();
    }
    return this.availableMigrations;
  }
  
  private List<ResolvedMigration> doFindAvailableMigrations()
    throws FlywayException
  {
    List<ResolvedMigration> migrations = new ArrayList(collectMigrations(this.migrationResolvers));
    Collections.sort(migrations, new ResolvedMigrationComparator());
    
    checkForIncompatibilities(migrations);
    
    return migrations;
  }
  
  static Collection<ResolvedMigration> collectMigrations(Collection<MigrationResolver> migrationResolvers)
  {
    Set<ResolvedMigration> migrations = new HashSet();
    for (MigrationResolver migrationResolver : migrationResolvers) {
      migrations.addAll(migrationResolver.resolveMigrations());
    }
    return migrations;
  }
  
  static void checkForIncompatibilities(List<ResolvedMigration> migrations)
  {
    for (int i = 0; i < migrations.size() - 1; i++)
    {
      ResolvedMigration current = (ResolvedMigration)migrations.get(i);
      ResolvedMigration next = (ResolvedMigration)migrations.get(i + 1);
      if (new ResolvedMigrationComparator().compare(current, next) == 0)
      {
        if (current.getVersion() != null) {
          throw new FlywayException(String.format("Found more than one migration with version %s\nOffenders:\n-> %s (%s)\n-> %s (%s)", new Object[] {current
            .getVersion(), current
            .getPhysicalLocation(), current
            .getType(), next
            .getPhysicalLocation(), next
            .getType() }));
        }
        throw new FlywayException(String.format("Found more than one repeatable migration with description %s\nOffenders:\n-> %s (%s)\n-> %s (%s)", new Object[] {current
          .getDescription(), current
          .getPhysicalLocation(), current
          .getType(), next
          .getPhysicalLocation(), next
          .getType() }));
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\CompositeMigrationResolver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */