package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;

final class JimfsFileSystemProvider
  extends FileSystemProvider
{
  private static final JimfsFileSystemProvider INSTANCE = new JimfsFileSystemProvider();
  
  static
  {
    try
    {
      Handler.register();
    }
    catch (Throwable e) {}
  }
  
  static JimfsFileSystemProvider instance()
  {
    return INSTANCE;
  }
  
  public String getScheme()
  {
    return "jimfs";
  }
  
  public FileSystem newFileSystem(URI uri, Map<String, ?> env)
    throws IOException
  {
    throw new UnsupportedOperationException("This method should not be called directly;use an overload of Jimfs.newFileSystem() to create a FileSystem.");
  }
  
  public FileSystem getFileSystem(URI uri)
  {
    throw new UnsupportedOperationException("This method should not be called directly; use FileSystems.getFileSystem(URI) instead.");
  }
  
  public FileSystem newFileSystem(Path path, Map<String, ?> env)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    Preconditions.checkNotNull(env);
    
    URI pathUri = checkedPath.toUri();
    URI jarUri = URI.create("jar:" + pathUri);
    try
    {
      return FileSystems.newFileSystem(jarUri, env);
    }
    catch (Exception e)
    {
      throw new UnsupportedOperationException(e);
    }
  }
  
  public Path getPath(URI uri)
  {
    throw new UnsupportedOperationException("This method should not be called directly; use Paths.get(URI) instead.");
  }
  
  private static JimfsPath checkPath(Path path)
  {
    if ((path instanceof JimfsPath)) {
      return (JimfsPath)path;
    }
    throw new ProviderMismatchException("path " + path + " is not associated with a Jimfs file system");
  }
  
  private static JimfsFileSystem getFileSystem(Path path)
  {
    return (JimfsFileSystem)checkPath(path).getFileSystem();
  }
  
  private static FileSystemView getDefaultView(JimfsPath path)
  {
    return getFileSystem(path).getDefaultView();
  }
  
  public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    if (!checkedPath.getJimfsFileSystem().getFileStore().supportsFeature(Feature.FILE_CHANNEL)) {
      throw new UnsupportedOperationException();
    }
    return newJimfsFileChannel(checkedPath, options, attrs);
  }
  
  private JimfsFileChannel newJimfsFileChannel(JimfsPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
    throws IOException
  {
    ImmutableSet<OpenOption> opts = Options.getOptionsForChannel(options);
    FileSystemView view = getDefaultView(path);
    RegularFile file = view.getOrCreateRegularFile(path, opts, attrs);
    return new JimfsFileChannel(file, opts, view.state());
  }
  
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    JimfsFileChannel channel = newJimfsFileChannel(checkedPath, options, attrs);
    return checkedPath.getJimfsFileSystem().getFileStore().supportsFeature(Feature.FILE_CHANNEL) ? channel : new DowngradedSeekableByteChannel(channel);
  }
  
  public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options, @Nullable ExecutorService executor, FileAttribute<?>... attrs)
    throws IOException
  {
    JimfsFileChannel channel = (JimfsFileChannel)newFileChannel(path, options, attrs);
    if (executor == null)
    {
      JimfsFileSystem fileSystem = (JimfsFileSystem)path.getFileSystem();
      executor = fileSystem.getDefaultThreadPool();
    }
    return channel.asAsynchronousFileChannel(executor);
  }
  
  public InputStream newInputStream(Path path, OpenOption... options)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    ImmutableSet<OpenOption> opts = Options.getOptionsForInputStream(options);
    FileSystemView view = getDefaultView(checkedPath);
    RegularFile file = view.getOrCreateRegularFile(checkedPath, opts, NO_ATTRS);
    return new JimfsInputStream(file, view.state());
  }
  
  private static final FileAttribute<?>[] NO_ATTRS = new FileAttribute[0];
  
  public OutputStream newOutputStream(Path path, OpenOption... options)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    ImmutableSet<OpenOption> opts = Options.getOptionsForOutputStream(options);
    FileSystemView view = getDefaultView(checkedPath);
    RegularFile file = view.getOrCreateRegularFile(checkedPath, opts, NO_ATTRS);
    return new JimfsOutputStream(file, opts.contains(StandardOpenOption.APPEND), view.state());
  }
  
  public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(dir);
    return getDefaultView(checkedPath).newDirectoryStream(checkedPath, filter, Options.FOLLOW_LINKS, checkedPath);
  }
  
  public void createDirectory(Path dir, FileAttribute<?>... attrs)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(dir);
    FileSystemView view = getDefaultView(checkedPath);
    view.createDirectory(checkedPath, attrs);
  }
  
  public void createLink(Path link, Path existing)
    throws IOException
  {
    JimfsPath linkPath = checkPath(link);
    JimfsPath existingPath = checkPath(existing);
    Preconditions.checkArgument(linkPath.getFileSystem().equals(existingPath.getFileSystem()), "link and existing paths must belong to the same file system instance");
    
    FileSystemView view = getDefaultView(linkPath);
    view.link(linkPath, getDefaultView(existingPath), existingPath);
  }
  
  public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs)
    throws IOException
  {
    JimfsPath linkPath = checkPath(link);
    JimfsPath targetPath = checkPath(target);
    Preconditions.checkArgument(linkPath.getFileSystem().equals(targetPath.getFileSystem()), "link and target paths must belong to the same file system instance");
    
    FileSystemView view = getDefaultView(linkPath);
    view.createSymbolicLink(linkPath, targetPath, attrs);
  }
  
  public Path readSymbolicLink(Path link)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(link);
    return getDefaultView(checkedPath).readSymbolicLink(checkedPath);
  }
  
  public void delete(Path path)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    FileSystemView view = getDefaultView(checkedPath);
    view.deleteFile(checkedPath, FileSystemView.DeleteMode.ANY);
  }
  
  public void copy(Path source, Path target, CopyOption... options)
    throws IOException
  {
    copy(source, target, Options.getCopyOptions(options), false);
  }
  
  public void move(Path source, Path target, CopyOption... options)
    throws IOException
  {
    copy(source, target, Options.getMoveOptions(options), true);
  }
  
  private void copy(Path source, Path target, ImmutableSet<CopyOption> options, boolean move)
    throws IOException
  {
    JimfsPath sourcePath = checkPath(source);
    JimfsPath targetPath = checkPath(target);
    
    FileSystemView sourceView = getDefaultView(sourcePath);
    FileSystemView targetView = getDefaultView(targetPath);
    sourceView.copy(sourcePath, targetView, targetPath, options, move);
  }
  
  public boolean isSameFile(Path path, Path path2)
    throws IOException
  {
    if (path.equals(path2)) {
      return true;
    }
    if ((!(path instanceof JimfsPath)) || (!(path2 instanceof JimfsPath))) {
      return false;
    }
    JimfsPath checkedPath = (JimfsPath)path;
    JimfsPath checkedPath2 = (JimfsPath)path2;
    
    FileSystemView view = getDefaultView(checkedPath);
    FileSystemView view2 = getDefaultView(checkedPath2);
    
    return view.isSameFile(checkedPath, view2, checkedPath2);
  }
  
  public boolean isHidden(Path path)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    FileSystemView view = getDefaultView(checkedPath);
    if (getFileStore(path).supportsFileAttributeView("dos")) {
      return ((DosFileAttributes)view.readAttributes(checkedPath, DosFileAttributes.class, Options.NOFOLLOW_LINKS)).isHidden();
    }
    return (path.getNameCount() > 0) && (path.getFileName().toString().startsWith("."));
  }
  
  public FileStore getFileStore(Path path)
    throws IOException
  {
    return getFileSystem(path).getFileStore();
  }
  
  public void checkAccess(Path path, AccessMode... modes)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    getDefaultView(checkedPath).checkAccess(checkedPath);
  }
  
  @Nullable
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
  {
    JimfsPath checkedPath = checkPath(path);
    return getDefaultView(checkedPath).getFileAttributeView(checkedPath, type, Options.getLinkOptions(options));
  }
  
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    return getDefaultView(checkedPath).readAttributes(checkedPath, type, Options.getLinkOptions(options));
  }
  
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    return getDefaultView(checkedPath).readAttributes(checkedPath, attributes, Options.getLinkOptions(options));
  }
  
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
    throws IOException
  {
    JimfsPath checkedPath = checkPath(path);
    getDefaultView(checkedPath).setAttribute(checkedPath, attribute, value, Options.getLinkOptions(options));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsFileSystemProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */