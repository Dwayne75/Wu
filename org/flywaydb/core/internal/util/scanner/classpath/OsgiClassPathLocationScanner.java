package org.flywaydb.core.internal.util.scanner.classpath;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class OsgiClassPathLocationScanner
  implements ClassPathLocationScanner
{
  private static final Pattern bundleIdPattern = Pattern.compile("^\\d+");
  
  public Set<String> findResourceNames(String location, URL locationUrl)
    throws IOException
  {
    Set<String> resourceNames = new TreeSet();
    
    Bundle bundle = getTargetBundleOrCurrent(FrameworkUtil.getBundle(getClass()), locationUrl);
    
    Enumeration<URL> entries = bundle.findEntries(locationUrl.getPath(), "*", true);
    if (entries != null) {
      while (entries.hasMoreElements())
      {
        URL entry = (URL)entries.nextElement();
        String resourceName = getPathWithoutLeadingSlash(entry);
        
        resourceNames.add(resourceName);
      }
    }
    return resourceNames;
  }
  
  private Bundle getTargetBundleOrCurrent(Bundle currentBundle, URL locationUrl)
  {
    try
    {
      Bundle targetBundle = currentBundle.getBundleContext().getBundle(getBundleId(locationUrl.getHost()));
      return targetBundle != null ? targetBundle : currentBundle;
    }
    catch (Exception e) {}
    return currentBundle;
  }
  
  private long getBundleId(String host)
  {
    Matcher matcher = bundleIdPattern.matcher(host);
    if (matcher.find()) {
      return Double.valueOf(matcher.group()).longValue();
    }
    throw new IllegalArgumentException("There's no bundleId in passed URL");
  }
  
  private String getPathWithoutLeadingSlash(URL entry)
  {
    String path = entry.getPath();
    
    return path.startsWith("/") ? path.substring(1) : path;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\OsgiClassPathLocationScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */