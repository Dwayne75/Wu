package org.flywaydb.core.internal.util;

import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

public class VersionPrinter
{
  private static final Log LOG = LogFactory.getLog(VersionPrinter.class);
  private static boolean printed;
  
  public static void printVersion()
  {
    if (printed) {
      return;
    }
    printed = true;
    String version = new ClassPathResource("org/flywaydb/core/internal/version.txt", VersionPrinter.class.getClassLoader()).loadAsString("UTF-8");
    LOG.info("Flyway " + version + " by Boxfuse");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\VersionPrinter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */