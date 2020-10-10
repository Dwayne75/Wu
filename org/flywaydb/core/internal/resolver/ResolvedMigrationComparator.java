package org.flywaydb.core.internal.resolver;

import java.util.Comparator;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;

public class ResolvedMigrationComparator
  implements Comparator<ResolvedMigration>
{
  public int compare(ResolvedMigration o1, ResolvedMigration o2)
  {
    if ((o1.getVersion() != null) && (o2.getVersion() != null)) {
      return o1.getVersion().compareTo(o2.getVersion());
    }
    if (o1.getVersion() != null) {
      return Integer.MIN_VALUE;
    }
    if (o2.getVersion() != null) {
      return Integer.MAX_VALUE;
    }
    return o1.getDescription().compareTo(o2.getDescription());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\ResolvedMigrationComparator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */