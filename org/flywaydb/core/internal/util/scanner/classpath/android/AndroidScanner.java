package org.flywaydb.core.internal.util.scanner.classpath.android;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.android.ContextHolder;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.ResourceAndClassScanner;

public class AndroidScanner
  implements ResourceAndClassScanner
{
  private static final Log LOG = LogFactory.getLog(AndroidScanner.class);
  private final Context context;
  private final PathClassLoader classLoader;
  
  public AndroidScanner(ClassLoader classLoader)
  {
    this.classLoader = ((PathClassLoader)classLoader);
    this.context = ContextHolder.getContext();
    if (this.context == null) {
      throw new FlywayException("Unable to scan for Migrations! Context not set. Within an activity you can fix this with org.flywaydb.core.api.android.ContextHolder.setContext(this);");
    }
  }
  
  public Resource[] scanForResources(Location location, String prefix, String suffix)
    throws Exception
  {
    List<Resource> resources = new ArrayList();
    
    String path = location.getPath();
    for (String asset : this.context.getAssets().list(path)) {
      if ((asset.startsWith(prefix)) && (asset.endsWith(suffix)) && 
        (asset.length() > (prefix + suffix).length())) {
        resources.add(new AndroidResource(this.context.getAssets(), path, asset));
      } else {
        LOG.debug("Filtering out asset: " + asset);
      }
    }
    return (Resource[])resources.toArray(new Resource[resources.size()]);
  }
  
  public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface)
    throws Exception
  {
    String pkg = location.getPath().replace("/", ".");
    
    List<Class> classes = new ArrayList();
    
    DexFile dex = new DexFile(this.context.getApplicationInfo().sourceDir);
    Enumeration<String> entries = dex.entries();
    while (entries.hasMoreElements())
    {
      String className = (String)entries.nextElement();
      if (className.startsWith(pkg))
      {
        Class<?> clazz = this.classLoader.loadClass(className);
        if (Modifier.isAbstract(clazz.getModifiers()))
        {
          LOG.debug("Skipping abstract class: " + className);
        }
        else if (implementedInterface.isAssignableFrom(clazz))
        {
          try
          {
            ClassUtils.instantiate(className, this.classLoader);
          }
          catch (Exception e)
          {
            throw new FlywayException("Unable to instantiate class: " + className);
          }
          classes.add(clazz);
          LOG.debug("Found class: " + className);
        }
      }
    }
    return (Class[])classes.toArray(new Class[classes.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\android\AndroidScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */