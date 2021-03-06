package org.flywaydb.core.api.resolver;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

public abstract class BaseMigrationResolver
  implements MigrationResolver, ConfigurationAware
{
  protected FlywayConfiguration flywayConfiguration;
  
  public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration)
  {
    this.flywayConfiguration = flywayConfiguration;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\api\resolver\BaseMigrationResolver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */