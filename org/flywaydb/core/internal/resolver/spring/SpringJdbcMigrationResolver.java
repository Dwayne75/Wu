package org.flywaydb.core.internal.resolver.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.ConfigurationInjectionUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.Scanner;

public class SpringJdbcMigrationResolver
  implements MigrationResolver
{
  private final Location location;
  private Scanner scanner;
  private FlywayConfiguration configuration;
  
  public SpringJdbcMigrationResolver(Scanner scanner, Location location, FlywayConfiguration configuration)
  {
    this.location = location;
    this.scanner = scanner;
    this.configuration = configuration;
  }
  
  public Collection<ResolvedMigration> resolveMigrations()
  {
    List<ResolvedMigration> migrations = new ArrayList();
    if (!this.location.isClassPath()) {
      return migrations;
    }
    try
    {
      Class<?>[] classes = this.scanner.scanForClasses(this.location, SpringJdbcMigration.class);
      for (Class<?> clazz : classes)
      {
        SpringJdbcMigration springJdbcMigration = (SpringJdbcMigration)ClassUtils.instantiate(clazz.getName(), this.scanner.getClassLoader());
        ConfigurationInjectionUtils.injectFlywayConfiguration(springJdbcMigration, this.configuration);
        
        ResolvedMigrationImpl migrationInfo = extractMigrationInfo(springJdbcMigration);
        migrationInfo.setPhysicalLocation(ClassUtils.getLocationOnDisk(clazz));
        migrationInfo.setExecutor(new SpringJdbcMigrationExecutor(springJdbcMigration));
        
        migrations.add(migrationInfo);
      }
    }
    catch (Exception e)
    {
      throw new FlywayException("Unable to resolve Spring Jdbc Java migrations in location: " + this.location, e);
    }
    Collections.sort(migrations, new ResolvedMigrationComparator());
    return migrations;
  }
  
  ResolvedMigrationImpl extractMigrationInfo(SpringJdbcMigration springJdbcMigration)
  {
    Integer checksum = null;
    if ((springJdbcMigration instanceof MigrationChecksumProvider))
    {
      MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider)springJdbcMigration;
      checksum = checksumProvider.getChecksum();
    }
    MigrationVersion version;
    String description;
    if ((springJdbcMigration instanceof MigrationInfoProvider))
    {
      MigrationInfoProvider infoProvider = (MigrationInfoProvider)springJdbcMigration;
      MigrationVersion version = infoProvider.getVersion();
      String description = infoProvider.getDescription();
      if (!StringUtils.hasText(description)) {
        throw new FlywayException("Missing description for migration " + version);
      }
    }
    else
    {
      String shortName = ClassUtils.getShortName(springJdbcMigration.getClass());
      String prefix;
      if ((shortName.startsWith("V")) || (shortName.startsWith("R"))) {
        prefix = shortName.substring(0, 1);
      } else {
        throw new FlywayException("Invalid Jdbc migration class name: " + springJdbcMigration.getClass().getName() + " => ensure it starts with V or R," + " or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming");
      }
      String prefix;
      Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", "");
      version = (MigrationVersion)info.getLeft();
      description = (String)info.getRight();
    }
    ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
    resolvedMigration.setVersion(version);
    resolvedMigration.setDescription(description);
    resolvedMigration.setScript(springJdbcMigration.getClass().getName());
    resolvedMigration.setChecksum(checksum);
    resolvedMigration.setType(MigrationType.SPRING_JDBC);
    return resolvedMigration;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\spring\SpringJdbcMigrationResolver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */