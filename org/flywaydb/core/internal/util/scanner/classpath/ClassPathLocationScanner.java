package org.flywaydb.core.internal.util.scanner.classpath;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

public abstract interface ClassPathLocationScanner
{
  public abstract Set<String> findResourceNames(String paramString, URL paramURL)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\ClassPathLocationScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */