package com.google.common.jimfs;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.MapMaker;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@AutoService(FileSystemProvider.class)
public final class SystemJimfsFileSystemProvider
  extends FileSystemProvider
{
  static final String FILE_SYSTEM_KEY = "fileSystem";
  private static final ConcurrentMap<URI, FileSystem> fileSystems = new MapMaker().weakValues().makeMap();
  
  public String getScheme()
  {
    return "jimfs";
  }
  
  public FileSystem newFileSystem(URI uri, Map<String, ?> env)
    throws IOException
  {
    Preconditions.checkArgument(uri.getScheme().equalsIgnoreCase("jimfs"), "uri (%s) scheme must be '%s'", new Object[] { uri, "jimfs" });
    
    Preconditions.checkArgument(isValidFileSystemUri(uri), "uri (%s) may not have a path, query or fragment", new Object[] { uri });
    
    Preconditions.checkArgument(env.get("fileSystem") instanceof FileSystem, "env map (%s) must contain key '%s' mapped to an instance of %s", new Object[] { env, "fileSystem", FileSystem.class });
    
    FileSystem fileSystem = (FileSystem)env.get("fileSystem");
    if (fileSystems.putIfAbsent(uri, fileSystem) != null) {
      throw new FileSystemAlreadyExistsException(uri.toString());
    }
    return fileSystem;
  }
  
  public FileSystem getFileSystem(URI uri)
  {
    FileSystem fileSystem = (FileSystem)fileSystems.get(uri);
    if (fileSystem == null) {
      throw new FileSystemNotFoundException(uri.toString());
    }
    return fileSystem;
  }
  
  public Path getPath(URI uri)
  {
    Preconditions.checkArgument("jimfs".equalsIgnoreCase(uri.getScheme()), "uri scheme does not match this provider: %s", new Object[] { uri });
    
    String path = uri.getPath();
    Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "uri must have a path: %s", new Object[] { uri });
    
    return toPath(getFileSystem(toFileSystemUri(uri)), uri);
  }
  
  private static boolean isValidFileSystemUri(URI uri)
  {
    return (Strings.isNullOrEmpty(uri.getPath())) && (Strings.isNullOrEmpty(uri.getQuery())) && (Strings.isNullOrEmpty(uri.getFragment()));
  }
  
  private static URI toFileSystemUri(URI uri)
  {
    try
    {
      return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
    }
    catch (URISyntaxException e)
    {
      throw new AssertionError(e);
    }
  }
  
  private static Path toPath(FileSystem fileSystem, URI uri)
  {
    try
    {
      Method toPath = fileSystem.getClass().getDeclaredMethod("toPath", new Class[] { URI.class });
      return (Path)toPath.invoke(fileSystem, new Object[] { uri });
    }
    catch (NoSuchMethodException e)
    {
      throw new IllegalArgumentException("invalid file system: " + fileSystem);
    }
    catch (InvocationTargetException|IllegalAccessException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public FileSystem newFileSystem(Path path, Map<String, ?> env)
    throws IOException
  {
    FileSystemProvider realProvider = path.getFileSystem().provider();
    return realProvider.newFileSystem(path, env);
  }
  
  public static Runnable removeFileSystemRunnable(URI uri)
  {
    new Runnable()
    {
      public void run()
      {
        SystemJimfsFileSystemProvider.fileSystems.remove(this.val$uri);
      }
    };
  }
  
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public void createDirectory(Path dir, FileAttribute<?>... attrs)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public void delete(Path path)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public void copy(Path source, Path target, CopyOption... options)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public void move(Path source, Path target, CopyOption... options)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isSameFile(Path path, Path path2)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isHidden(Path path)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public FileStore getFileStore(Path path)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public void checkAccess(Path path, AccessMode... modes)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
  {
    throw new UnsupportedOperationException();
  }
  
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
  
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\SystemJimfsFileSystemProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */