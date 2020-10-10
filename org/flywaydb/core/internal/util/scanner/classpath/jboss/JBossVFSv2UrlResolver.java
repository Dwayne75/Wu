package org.flywaydb.core.internal.util.scanner.classpath.jboss;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.scanner.classpath.UrlResolver;

public class JBossVFSv2UrlResolver
  implements UrlResolver
{
  public URL toStandardJavaUrl(URL url)
    throws IOException
  {
    try
    {
      Class<?> vfsClass = Class.forName("org.jboss.virtual.VFS");
      Class<?> vfsUtilsClass = Class.forName("org.jboss.virtual.VFSUtils");
      Class<?> virtualFileClass = Class.forName("org.jboss.virtual.VirtualFile");
      
      Method getRootMethod = vfsClass.getMethod("getRoot", new Class[] { URL.class });
      Method getRealURLMethod = vfsUtilsClass.getMethod("getRealURL", new Class[] { virtualFileClass });
      
      Object root = getRootMethod.invoke(null, new Object[] { url });
      return (URL)getRealURLMethod.invoke(null, new Object[] { root });
    }
    catch (Exception e)
    {
      throw new FlywayException("JBoss VFS v2 call failed", e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\jboss\JBossVFSv2UrlResolver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */