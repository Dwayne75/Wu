package com.google.common.jimfs;

import com.google.common.base.Supplier;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

final class JimfsFileSystems
{
  private static final FileSystemProvider systemJimfsProvider = ;
  
  private static FileSystemProvider getSystemJimfsProvider()
  {
    for (FileSystemProvider provider : ) {
      if (provider.getScheme().equals("jimfs")) {
        return provider;
      }
    }
    return null;
  }
  
  private static final Runnable DO_NOTHING = new Runnable()
  {
    public void run() {}
  };
  
  private static Runnable removeFileSystemRunnable(URI uri)
  {
    if (systemJimfsProvider == null) {
      return DO_NOTHING;
    }
    try
    {
      Method method = systemJimfsProvider.getClass().getDeclaredMethod("removeFileSystemRunnable", new Class[] { URI.class });
      
      return (Runnable)method.invoke(systemJimfsProvider, new Object[] { uri });
    }
    catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException e)
    {
      throw new RuntimeException("Unable to get Runnable for removing the FileSystem from the cache when it is closed", e);
    }
  }
  
  public static JimfsFileSystem newFileSystem(JimfsFileSystemProvider provider, URI uri, Configuration config)
    throws IOException
  {
    PathService pathService = new PathService(config);
    FileSystemState state = new FileSystemState(removeFileSystemRunnable(uri));
    
    JimfsFileStore fileStore = createFileStore(config, pathService, state);
    FileSystemView defaultView = createDefaultView(config, fileStore, pathService);
    WatchServiceConfiguration watchServiceConfig = config.watchServiceConfig;
    
    JimfsFileSystem fileSystem = new JimfsFileSystem(provider, uri, fileStore, pathService, defaultView, watchServiceConfig);
    
    pathService.setFileSystem(fileSystem);
    return fileSystem;
  }
  
  private static JimfsFileStore createFileStore(Configuration config, PathService pathService, FileSystemState state)
  {
    AttributeService attributeService = new AttributeService(config);
    
    HeapDisk disk = new HeapDisk(config);
    FileFactory fileFactory = new FileFactory(disk);
    
    Map<Name, Directory> roots = new HashMap();
    for (String root : config.roots)
    {
      JimfsPath path = pathService.parsePath(root, new String[0]);
      if ((!path.isAbsolute()) && (path.getNameCount() == 0)) {
        throw new IllegalArgumentException("Invalid root path: " + root);
      }
      Name rootName = path.root();
      
      Directory rootDir = fileFactory.createRootDirectory(rootName);
      attributeService.setInitialAttributes(rootDir, new FileAttribute[0]);
      roots.put(rootName, rootDir);
    }
    return new JimfsFileStore(new FileTree(roots), fileFactory, disk, attributeService, config.supportedFeatures, state);
  }
  
  private static FileSystemView createDefaultView(Configuration config, JimfsFileStore fileStore, PathService pathService)
    throws IOException
  {
    JimfsPath workingDirPath = pathService.parsePath(config.workingDirectory, new String[0]);
    
    Directory dir = fileStore.getRoot(workingDirPath.root());
    if (dir == null) {
      throw new IllegalArgumentException("Invalid working dir path: " + workingDirPath);
    }
    for (Name name : workingDirPath.names())
    {
      Directory newDir = (Directory)fileStore.directoryCreator().get();
      fileStore.setInitialAttributes(newDir, new FileAttribute[0]);
      dir.link(name, newDir);
      
      dir = newDir;
    }
    return new FileSystemView(fileStore, dir, workingDirPath);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsFileSystems.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */