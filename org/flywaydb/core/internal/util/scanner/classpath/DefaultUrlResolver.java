package org.flywaydb.core.internal.util.scanner.classpath;

import java.io.IOException;
import java.net.URL;

public class DefaultUrlResolver
  implements UrlResolver
{
  public URL toStandardJavaUrl(URL url)
    throws IOException
  {
    return url;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\DefaultUrlResolver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */