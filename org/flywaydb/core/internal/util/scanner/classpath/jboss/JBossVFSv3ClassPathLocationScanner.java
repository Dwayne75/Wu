package org.flywaydb.core.internal.util.scanner.classpath.jboss;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.internal.util.UrlUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathLocationScanner;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

public class JBossVFSv3ClassPathLocationScanner
  implements ClassPathLocationScanner
{
  private static final Log LOG = LogFactory.getLog(JBossVFSv3ClassPathLocationScanner.class);
  
  public Set<String> findResourceNames(String location, URL locationUrl)
    throws IOException
  {
    String filePath = UrlUtils.toFilePath(locationUrl);
    String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
    if (!classPathRootOnDisk.endsWith("/")) {
      classPathRootOnDisk = classPathRootOnDisk + "/";
    }
    LOG.debug("Scanning starting at classpath root on JBoss VFS: " + classPathRootOnDisk);
    
    Set<String> resourceNames = new TreeSet();
    
    List<VirtualFile> files = VFS.getChild(filePath).getChildrenRecursively(new VirtualFileFilter()
    {
      public boolean accepts(VirtualFile file)
      {
        return file.isFile();
      }
    });
    for (VirtualFile file : files) {
      resourceNames.add(file.getPathName().substring(classPathRootOnDisk.length()));
    }
    return resourceNames;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\scanner\classpath\jboss\JBossVFSv3ClassPathLocationScanner.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */