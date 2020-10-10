package org.flywaydb.core.internal.resolver.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

public class SqlMigrationResolver
  implements MigrationResolver
{
  private final DbSupport dbSupport;
  private final Scanner scanner;
  private final Location location;
  private final PlaceholderReplacer placeholderReplacer;
  private final String encoding;
  private final String sqlMigrationPrefix;
  private final String repeatableSqlMigrationPrefix;
  private final String sqlMigrationSeparator;
  private final String sqlMigrationSuffix;
  
  public SqlMigrationResolver(DbSupport dbSupport, Scanner scanner, Location location, PlaceholderReplacer placeholderReplacer, String encoding, String sqlMigrationPrefix, String repeatableSqlMigrationPrefix, String sqlMigrationSeparator, String sqlMigrationSuffix)
  {
    this.dbSupport = dbSupport;
    this.scanner = scanner;
    this.location = location;
    this.placeholderReplacer = placeholderReplacer;
    this.encoding = encoding;
    this.sqlMigrationPrefix = sqlMigrationPrefix;
    this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
    this.sqlMigrationSeparator = sqlMigrationSeparator;
    this.sqlMigrationSuffix = sqlMigrationSuffix;
  }
  
  public List<ResolvedMigration> resolveMigrations()
  {
    List<ResolvedMigration> migrations = new ArrayList();
    
    scanForMigrations(migrations, this.sqlMigrationPrefix, this.sqlMigrationSeparator, this.sqlMigrationSuffix);
    scanForMigrations(migrations, this.repeatableSqlMigrationPrefix, this.sqlMigrationSeparator, this.sqlMigrationSuffix);
    
    Collections.sort(migrations, new ResolvedMigrationComparator());
    return migrations;
  }
  
  public void scanForMigrations(List<ResolvedMigration> migrations, String prefix, String separator, String suffix)
  {
    for (Resource resource : this.scanner.scanForResources(this.location, prefix, suffix))
    {
      String filename = resource.getFilename();
      if (!isSqlCallback(filename, suffix))
      {
        Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(filename, prefix, separator, suffix);
        
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setVersion((MigrationVersion)info.getLeft());
        migration.setDescription((String)info.getRight());
        migration.setScript(extractScriptName(resource));
        migration.setChecksum(Integer.valueOf(calculateChecksum(resource, resource.loadAsString(this.encoding))));
        migration.setType(MigrationType.SQL);
        migration.setPhysicalLocation(resource.getLocationOnDisk());
        migration.setExecutor(new SqlMigrationExecutor(this.dbSupport, resource, this.placeholderReplacer, this.encoding));
        migrations.add(migration);
      }
    }
  }
  
  static boolean isSqlCallback(String filename, String suffix)
  {
    String baseName = filename.substring(0, filename.length() - suffix.length());
    return SqlScriptFlywayCallback.ALL_CALLBACKS.contains(baseName);
  }
  
  String extractScriptName(Resource resource)
  {
    if (this.location.getPath().isEmpty()) {
      return resource.getLocation();
    }
    return resource.getLocation().substring(this.location.getPath().length() + 1);
  }
  
  static int calculateChecksum(Resource resource, String str)
  {
    CRC32 crc32 = new CRC32();
    
    BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
    try
    {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        crc32.update(line.getBytes("UTF-8"));
      }
    }
    catch (IOException e)
    {
      String message = "Unable to calculate checksum";
      if (resource != null) {
        message = message + " for " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")";
      }
      throw new FlywayException(message, e);
    }
    return (int)crc32.getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\sql\SqlMigrationResolver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */