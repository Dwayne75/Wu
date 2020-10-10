package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.UUID;

public final class Jimfs
{
  public static final String URI_SCHEME = "jimfs";
  
  public static FileSystem newFileSystem()
  {
    return newFileSystem(newRandomFileSystemName());
  }
  
  public static FileSystem newFileSystem(String name)
  {
    return newFileSystem(name, Configuration.forCurrentPlatform());
  }
  
  public static FileSystem newFileSystem(Configuration configuration)
  {
    return newFileSystem(newRandomFileSystemName(), configuration);
  }
  
  public static FileSystem newFileSystem(String name, Configuration configuration)
  {
    try
    {
      URI uri = new URI("jimfs", name, null, null);
      return newFileSystem(uri, configuration);
    }
    catch (URISyntaxException e)
    {
      throw new IllegalArgumentException(e);
    }
  }
  
  @VisibleForTesting
  static FileSystem newFileSystem(URI uri, Configuration config)
  {
    Preconditions.checkArgument("jimfs".equals(uri.getScheme()), "uri (%s) must have scheme %s", new Object[] { uri, "jimfs" });
    try
    {
      JimfsFileSystem fileSystem = JimfsFileSystems.newFileSystem(JimfsFileSystemProvider.instance(), uri, config);
      
      ImmutableMap<String, ?> env = ImmutableMap.of("fileSystem", fileSystem);
      FileSystems.newFileSystem(uri, env, SystemJimfsFileSystemProvider.class.getClassLoader());
      
      return fileSystem;
    }
    catch (IOException e)
    {
      throw new AssertionError(e);
    }
  }
  
  private static String newRandomFileSystemName()
  {
    return UUID.randomUUID().toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\Jimfs.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */