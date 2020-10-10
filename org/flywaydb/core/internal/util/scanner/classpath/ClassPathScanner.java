package org.flywaydb.core.internal.util.scanner.classpath;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.UrlUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.flywaydb.core.internal.util.scanner.classpath.jboss.JBossVFSv3ClassPathLocationScanner;

public class ClassPathScanner
  implements ResourceAndClassScanner
{
  private static final Log LOG = LogFactory.getLog(ClassPathScanner.class);
  private final ClassLoader classLoader;
  private final Map<Location, List<URL>> locationUrlCache = new HashMap();
  private final Map<String, ClassPathLocationScanner> locationScannerCache = new HashMap();
  private final Map<ClassPathLocationScanner, Map<URL, Set<String>>> resourceNameCache = new HashMap();
  
  public ClassPathScanner(ClassLoader classLoader)
  {
    this.classLoader = classLoader;
  }
  
  public Resource[] scanForResources(Location path, String prefix, String suffix)
    throws IOException
  {
    LOG.debug("Scanning for classpath resources at '" + path + "' (Prefix: '" + prefix + "', Suffix: '" + suffix + "')");
    
    Set<Resource> resources = new TreeSet();
    
    Set<String> resourceNames = findResourceNames(path, prefix, suffix);
    for (String resourceName : resourceNames)
    {
      resources.add(new ClassPathResource(resourceName, this.classLoader));
      LOG.debug("Found resource: " + resourceName);
    }
    return (Resource[])resources.toArray(new Resource[resources.size()]);
  }
  
  public Class<?>[] scanForClasses(Location location, Class<?> implementedInterface)
    throws Exception
  {
    LOG.debug("Scanning for classes at '" + location + "' (Implementing: '" + implementedInterface.getName() + "')");
    
    List<Class<?>> classes = new ArrayList();
    
    Set<String> resourceNames = findResourceNames(location, "", ".class");
    for (String resourceName : resourceNames)
    {
      String className = toClassName(resourceName);
      Class<?> clazz = this.classLoader.loadClass(className);
      if ((Modifier.isAbstract(clazz.getModifiers())) || (clazz.isEnum()) || (clazz.isAnonymousClass()))
      {
        LOG.debug("Skipping non-instantiable class: " + className);
      }
      else if (implementedInterface.isAssignableFrom(clazz))
      {
        try
        {
          ClassUtils.instantiate(className, this.classLoader);
        }
        catch (Exception e)
        {
          throw new FlywayException("Unable to instantiate class: " + className, e);
        }
        classes.add(clazz);
        LOG.debug("Found class: " + className);
      }
    }
    return (Class[])classes.toArray(new Class[classes.size()]);
  }
  
  private String toClassName(String resourceName)
  {
    String nameWithDots = resourceName.replace("/", ".");
    return nameWithDots.substring(0, nameWithDots.length() - ".class".length());
  }
  
  private Set<String> findResourceNames(Location location, String prefix, String suffix)
    throws IOException
  {
    Set<String> resourceNames = new TreeSet();
    
    List<URL> locationsUrls = getLocationUrlsForPath(location);
    for (URL locationUrl : locationsUrls)
    {
      LOG.debug("Scanning URL: " + locationUrl.toExternalForm());
      
      UrlResolver urlResolver = createUrlResolver(locationUrl.getProtocol());
      URL resolvedUrl = urlResolver.toStandardJavaUrl(locationUrl);
      
      String protocol = resolvedUrl.getProtocol();
      ClassPathLocationScanner classPathLocationScanner = createLocationScanner(protocol);
      if (classPathLocationScanner == null)
      {
        String scanRoot = UrlUtils.toFilePath(resolvedUrl);
        LOG.warn("Unable to scan location: " + scanRoot + " (unsupported protocol: " + protocol + ")");
      }
      else
      {
        Set<String> names = (Set)((Map)this.resourceNameCache.get(classPathLocationScanner)).get(resolvedUrl);
        if (names == null)
        {
          names = classPathLocationScanner.findResourceNames(location.getPath(), resolvedUrl);
          ((Map)this.resourceNameCache.get(classPathLocationScanner)).put(resolvedUrl, names);
        }
        resourceNames.addAll(names);
      }
    }
    return filterResourceNames(resourceNames, prefix, suffix);
  }
  
  private List<URL> getLocationUrlsForPath(Location location)
    throws IOException
  {
    if (this.locationUrlCache.containsKey(location)) {
      return (List)this.locationUrlCache.get(location);
    }
    LOG.debug("Determining location urls for " + location + " using ClassLoader " + this.classLoader + " ...");
    
    List<URL> locationUrls = new ArrayList();
    if (this.classLoader.getClass().getName().startsWith("com.ibm"))
    {
      Enumeration<URL> urls = this.classLoader.getResources(location.getPath() + "/flyway.location");
      if (!urls.hasMoreElements()) {
        LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + this.classLoader + ")" + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!");
      }
      while (urls.hasMoreElements())
      {
        URL url = (URL)urls.nextElement();
        locationUrls.add(new URL(URLDecoder.decode(url.toExternalForm(), "UTF-8").replace("/flyway.location", "")));
      }
    }
    else
    {
      Enumeration<URL> urls = this.classLoader.getResources(location.getPath());
      if (!urls.hasMoreElements()) {
        LOG.warn("Unable to resolve location " + location);
      }
      while (urls.hasMoreElements()) {
        locationUrls.add(urls.nextElement());
      }
    }
    this.locationUrlCache.put(location, locationUrls);
    
    return locationUrls;
  }
  
  private UrlResolver createUrlResolver(String protocol)
  {
    if ((new FeatureDetector(this.classLoader).isJBossVFSv2Available()) && (protocol.startsWith("vfs"))) {
      return new JBossVFSv2UrlResolver();
    }
    return new DefaultUrlResolver();
  }
  
  private ClassPathLocationScanner createLocationScanner(String protocol)
  {
    if (this.locationScannerCache.containsKey(protocol)) {
      return (ClassPathLocationScanner)this.locationScannerCache.get(protocol);
    }
    if ("file".equals(protocol))
    {
      FileSystemClassPathLocationScanner locationScanner = new FileSystemClassPathLocationScanner();
      this.locationScannerCache.put(protocol, locationScanner);
      this.resourceNameCache.put(locationScanner, new HashMap());
      return locationScanner;
    }
    if (("jar".equals(protocol)) || 
      ("zip".equals(protocol)) || 
      ("wsjar".equals(protocol)))
    {
      JarFileClassPathLocationScanner locationScanner = new JarFileClassPathLocationScanner();
      this.locationScannerCache.put(protocol, locationScanner);
      this.resourceNameCache.put(locationScanner, new HashMap());
      return locationScanner;
    }
    FeatureDetector featureDetector = new FeatureDetector(this.classLoader);
    if ((featureDetector.isJBossVFSv3Available()) && ("vfs".equals(protocol)))
    {
      JBossVFSv3ClassPathLocationScanner locationScanner = new JBossVFSv3ClassPathLocationScanner();
      this.locationScannerCache.put(protocol, locationScanner);
      this.resourceNameCache.put(locationScanner, new HashMap());
      return locationScanner;
    }
    if ((featureDetector.isOsgiFrameworkAvailable()) && (
      ("bundle".equals(protocol)) || 
      ("bundleresource".equals(protocol))))
    {
      OsgiClassPathLocationScanner locationScanner = new OsgiClassPathLocationScanner();
      this.locationScannerCache.put(protocol, locationScanner);
      this.resourceNameCache.put(locationScanner, new HashMap());
      return locationScanner;
    }
    return null;
  }
  
  private Set<String> filterResourceNames(Set<String> resourceNames, String prefix, String suffix)
  {
    Set<String> filteredResourceNames = new TreeSet();
    for (String resourceName : resourceNames)
    {
      String fileName = resourceName.substring(resourceName.lastIndexOf("/") + 1);
      if ((fileName.startsWith(prefix)) && (fileName.endsWith(suffix)) && 
        (fileName.length() > (prefix + suffix).length())) {
        filteredResourceNames.add(resourceName);
      } else {
        LOG.debug("Filtering out resource: " + resourceName + " (filename: " + fileName + ")");
      }
    }
    return filteredResourceNames;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\ClassPathScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */