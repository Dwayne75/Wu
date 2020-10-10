package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

public class ConfigurationInjectionUtils
{
  public static void injectFlywayConfiguration(Object target, FlywayConfiguration configuration)
  {
    if ((target instanceof ConfigurationAware)) {
      ((ConfigurationAware)target).setFlywayConfiguration(configuration);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\ConfigurationInjectionUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */