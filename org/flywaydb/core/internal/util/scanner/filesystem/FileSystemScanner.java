package org.flywaydb.core.internal.util.scanner.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;

public class FileSystemScanner
{
  private static final Log LOG = LogFactory.getLog(FileSystemScanner.class);
  
  public Resource[] scanForResources(Location location, String prefix, String suffix)
    throws IOException
  {
    String path = location.getPath();
    LOG.debug("Scanning for filesystem resources at '" + path + "' (Prefix: '" + prefix + "', Suffix: '" + suffix + "')");
    
    File dir = new File(path);
    if ((!dir.isDirectory()) || (!dir.canRead()))
    {
      LOG.warn("Unable to resolve location filesystem:" + path);
      return new Resource[0];
    }
    Set<Resource> resources = new TreeSet();
    
    Set<String> resourceNames = findResourceNames(path, prefix, suffix);
    for (String resourceName : resourceNames)
    {
      resources.add(new FileSystemResource(resourceName));
      LOG.debug("Found filesystem resource: " + resourceName);
    }
    return (Resource[])resources.toArray(new Resource[resources.size()]);
  }
  
  private Set<String> findResourceNames(String path, String prefix, String suffix)
    throws IOException
  {
    Set<String> resourceNames = findResourceNamesFromFileSystem(path, new File(path));
    return filterResourceNames(resourceNames, prefix, suffix);
  }
  
  private Set<String> findResourceNamesFromFileSystem(String scanRootLocation, File folder)
    throws IOException
  {
    LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");
    
    Set<String> resourceNames = new TreeSet();
    
    File[] files = folder.listFiles();
    for (File file : files) {
      if (file.canRead()) {
        if (file.isDirectory()) {
          resourceNames.addAll(findResourceNamesFromFileSystem(scanRootLocation, file));
        } else {
          resourceNames.add(file.getPath());
        }
      }
    }
    return resourceNames;
  }
  
  private Set<String> filterResourceNames(Set<String> resourceNames, String prefix, String suffix)
  {
    Set<String> filteredResourceNames = new TreeSet();
    for (String resourceName : resourceNames)
    {
      String fileName = resourceName.substring(resourceName.lastIndexOf(File.separator) + 1);
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\filesystem\FileSystemScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */