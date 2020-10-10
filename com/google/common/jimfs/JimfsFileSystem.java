package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableSortedSet.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;

final class JimfsFileSystem
  extends FileSystem
{
  private final JimfsFileSystemProvider provider;
  private final URI uri;
  private final JimfsFileStore fileStore;
  private final PathService pathService;
  private final UserPrincipalLookupService userLookupService = new UserLookupService(true);
  private final FileSystemView defaultView;
  private final WatchServiceConfiguration watchServiceConfig;
  @Nullable
  private ExecutorService defaultThreadPool;
  
  JimfsFileSystem(JimfsFileSystemProvider provider, URI uri, JimfsFileStore fileStore, PathService pathService, FileSystemView defaultView, WatchServiceConfiguration watchServiceConfig)
  {
    this.provider = ((JimfsFileSystemProvider)Preconditions.checkNotNull(provider));
    this.uri = ((URI)Preconditions.checkNotNull(uri));
    this.fileStore = ((JimfsFileStore)Preconditions.checkNotNull(fileStore));
    this.pathService = ((PathService)Preconditions.checkNotNull(pathService));
    this.defaultView = ((FileSystemView)Preconditions.checkNotNull(defaultView));
    this.watchServiceConfig = ((WatchServiceConfiguration)Preconditions.checkNotNull(watchServiceConfig));
  }
  
  public JimfsFileSystemProvider provider()
  {
    return this.provider;
  }
  
  public URI getUri()
  {
    return this.uri;
  }
  
  public FileSystemView getDefaultView()
  {
    return this.defaultView;
  }
  
  public String getSeparator()
  {
    return this.pathService.getSeparator();
  }
  
  public ImmutableSortedSet<Path> getRootDirectories()
  {
    ImmutableSortedSet.Builder<JimfsPath> builder = ImmutableSortedSet.orderedBy(this.pathService);
    for (Name name : this.fileStore.getRootDirectoryNames()) {
      builder.add(this.pathService.createRoot(name));
    }
    return builder.build();
  }
  
  public JimfsPath getWorkingDirectory()
  {
    return this.defaultView.getWorkingDirectoryPath();
  }
  
  @VisibleForTesting
  PathService getPathService()
  {
    return this.pathService;
  }
  
  public JimfsFileStore getFileStore()
  {
    return this.fileStore;
  }
  
  public ImmutableSet<FileStore> getFileStores()
  {
    this.fileStore.state().checkOpen();
    return ImmutableSet.of(this.fileStore);
  }
  
  public ImmutableSet<String> supportedFileAttributeViews()
  {
    return this.fileStore.supportedFileAttributeViews();
  }
  
  public JimfsPath getPath(String first, String... more)
  {
    this.fileStore.state().checkOpen();
    return this.pathService.parsePath(first, more);
  }
  
  public URI toUri(JimfsPath path)
  {
    this.fileStore.state().checkOpen();
    return this.pathService.toUri(this.uri, path.toAbsolutePath());
  }
  
  public JimfsPath toPath(URI uri)
  {
    this.fileStore.state().checkOpen();
    return this.pathService.fromUri(uri);
  }
  
  public PathMatcher getPathMatcher(String syntaxAndPattern)
  {
    this.fileStore.state().checkOpen();
    return this.pathService.createPathMatcher(syntaxAndPattern);
  }
  
  public UserPrincipalLookupService getUserPrincipalLookupService()
  {
    this.fileStore.state().checkOpen();
    return this.userLookupService;
  }
  
  public WatchService newWatchService()
    throws IOException
  {
    return this.watchServiceConfig.newWatchService(this.defaultView, this.pathService);
  }
  
  public synchronized ExecutorService getDefaultThreadPool()
  {
    if (this.defaultThreadPool == null)
    {
      this.defaultThreadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("JimfsFileSystem-" + this.uri.getHost() + "-defaultThreadPool-%s").build());
      
      this.fileStore.state().register(new Closeable()
      {
        public void close()
        {
          JimfsFileSystem.this.defaultThreadPool.shutdown();
        }
      });
    }
    return this.defaultThreadPool;
  }
  
  public boolean isReadOnly()
  {
    return false;
  }
  
  public boolean isOpen()
  {
    return this.fileStore.state().isOpen();
  }
  
  public void close()
    throws IOException
  {
    this.fileStore.state().close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsFileSystem.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */