package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

public class MigrationInfoHelper
{
  public static Pair<MigrationVersion, String> extractVersionAndDescription(String migrationName, String prefix, String separator, String suffix)
  {
    String cleanMigrationName = migrationName.substring(prefix.length(), migrationName.length() - suffix.length());
    
    int descriptionPos = cleanMigrationName.indexOf(separator);
    if (descriptionPos < 0) {
      throw new FlywayException("Wrong migration name format: " + migrationName + "(It should look like this: " + prefix + "1_2" + separator + "Description" + suffix + ")");
    }
    String version = cleanMigrationName.substring(0, descriptionPos);
    String description = cleanMigrationName.substring(descriptionPos + separator.length()).replaceAll("_", " ");
    if (StringUtils.hasText(version)) {
      return Pair.of(MigrationVersion.fromVersion(version), description);
    }
    return Pair.of(null, description);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\MigrationInfoHelper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */