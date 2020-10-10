package org.flywaydb.core.internal.util.scanner.classpath;

import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Resource;

public abstract interface ResourceAndClassScanner
{
  public abstract Resource[] scanForResources(Location paramLocation, String paramString1, String paramString2)
    throws Exception;
  
  public abstract Class<?>[] scanForClasses(Location paramLocation, Class<?> paramClass)
    throws Exception;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\ResourceAndClassScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */