package org.flywaydb.core.api.resolver;

import java.util.Collection;

public abstract interface MigrationResolver
{
  public abstract Collection<ResolvedMigration> resolveMigrations();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\resolver\MigrationResolver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */