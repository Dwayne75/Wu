package org.flywaydb.core.internal.util.scanner.classpath;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileClassPathLocationScanner
  implements ClassPathLocationScanner
{
  public Set<String> findResourceNames(String location, URL locationUrl)
    throws IOException
  {
    JarFile jarFile = getJarFromUrl(locationUrl);
    try
    {
      String prefix = jarFile.getName().toLowerCase().endsWith(".war") ? "WEB-INF/classes/" : "";
      return findResourceNamesFromJarFile(jarFile, prefix, location);
    }
    finally
    {
      jarFile.close();
    }
  }
  
  private JarFile getJarFromUrl(URL locationUrl)
    throws IOException
  {
    URLConnection con = locationUrl.openConnection();
    if ((con instanceof JarURLConnection))
    {
      JarURLConnection jarCon = (JarURLConnection)con;
      jarCon.setUseCaches(false);
      return jarCon.getJarFile();
    }
    String urlFile = locationUrl.getFile();
    
    int separatorIndex = urlFile.indexOf("!/");
    if (separatorIndex != -1)
    {
      String jarFileUrl = urlFile.substring(0, separatorIndex);
      if (jarFileUrl.startsWith("file:")) {
        try
        {
          return new JarFile(new URL(jarFileUrl).toURI().getSchemeSpecificPart());
        }
        catch (URISyntaxException ex)
        {
          return new JarFile(jarFileUrl.substring("file:".length()));
        }
      }
      return new JarFile(jarFileUrl);
    }
    return new JarFile(urlFile);
  }
  
  private Set<String> findResourceNamesFromJarFile(JarFile jarFile, String prefix, String location)
    throws IOException
  {
    String toScan = prefix + location + (location.endsWith("/") ? "" : "/");
    Set<String> resourceNames = new TreeSet();
    
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements())
    {
      String entryName = ((JarEntry)entries.nextElement()).getName();
      if (entryName.startsWith(toScan)) {
        resourceNames.add(entryName.substring(prefix.length()));
      }
    }
    return resourceNames;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\JarFileClassPathLocationScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */