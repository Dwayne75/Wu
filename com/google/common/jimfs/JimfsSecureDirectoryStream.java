package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

final class JimfsSecureDirectoryStream
  implements SecureDirectoryStream<Path>
{
  private final FileSystemView view;
  private final DirectoryStream.Filter<? super Path> filter;
  private final FileSystemState fileSystemState;
  private boolean open = true;
  private Iterator<Path> iterator = new DirectoryIterator(null);
  
  public JimfsSecureDirectoryStream(FileSystemView view, DirectoryStream.Filter<? super Path> filter, FileSystemState fileSystemState)
  {
    this.view = ((FileSystemView)Preconditions.checkNotNull(view));
    this.filter = ((DirectoryStream.Filter)Preconditions.checkNotNull(filter));
    this.fileSystemState = fileSystemState;
    fileSystemState.register(this);
  }
  
  private JimfsPath path()
  {
    return this.view.getWorkingDirectoryPath();
  }
  
  public synchronized Iterator<Path> iterator()
  {
    checkOpen();
    Iterator<Path> result = this.iterator;
    Preconditions.checkState(result != null, "iterator() has already been called once");
    this.iterator = null;
    return result;
  }
  
  public synchronized void close()
  {
    this.open = false;
    this.fileSystemState.unregister(this);
  }
  
  protected synchronized void checkOpen()
  {
    if (!this.open) {
      throw new ClosedDirectoryStreamException();
    }
  }
  
  private final class DirectoryIterator
    extends AbstractIterator<Path>
  {
    @Nullable
    private Iterator<Name> fileNames;
    
    private DirectoryIterator() {}
    
    protected synchronized Path computeNext()
    {
      JimfsSecureDirectoryStream.this.checkOpen();
      try
      {
        if (this.fileNames == null) {
          this.fileNames = JimfsSecureDirectoryStream.this.view.snapshotWorkingDirectoryEntries().iterator();
        }
        while (this.fileNames.hasNext())
        {
          Name name = (Name)this.fileNames.next();
          Path path = JimfsSecureDirectoryStream.this.view.getWorkingDirectoryPath().resolve(name);
          if (JimfsSecureDirectoryStream.this.filter.accept(path)) {
            return path;
          }
        }
        return (Path)endOfData();
      }
      catch (IOException e)
      {
        throw new DirectoryIteratorException(e);
      }
    }
  }
  
  public static final DirectoryStream.Filter<Object> ALWAYS_TRUE_FILTER = new DirectoryStream.Filter()
  {
    public boolean accept(Object entry)
      throws IOException
    {
      return true;
    }
  };
  
  public SecureDirectoryStream<Path> newDirectoryStream(Path path, LinkOption... options)
    throws IOException
  {
    checkOpen();
    JimfsPath checkedPath = checkPath(path);
    
    return (SecureDirectoryStream)this.view.newDirectoryStream(checkedPath, ALWAYS_TRUE_FILTER, Options.getLinkOptions(options), path().resolve(checkedPath));
  }
  
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
    throws IOException
  {
    checkOpen();
    JimfsPath checkedPath = checkPath(path);
    ImmutableSet<OpenOption> opts = Options.getOptionsForChannel(options);
    return new JimfsFileChannel(this.view.getOrCreateRegularFile(checkedPath, opts, new FileAttribute[0]), opts, this.fileSystemState);
  }
  
  public void deleteFile(Path path)
    throws IOException
  {
    checkOpen();
    JimfsPath checkedPath = checkPath(path);
    this.view.deleteFile(checkedPath, FileSystemView.DeleteMode.NON_DIRECTORY_ONLY);
  }
  
  public void deleteDirectory(Path path)
    throws IOException
  {
    checkOpen();
    JimfsPath checkedPath = checkPath(path);
    this.view.deleteFile(checkedPath, FileSystemView.DeleteMode.DIRECTORY_ONLY);
  }
  
  public void move(Path srcPath, SecureDirectoryStream<Path> targetDir, Path targetPath)
    throws IOException
  {
    checkOpen();
    JimfsPath checkedSrcPath = checkPath(srcPath);
    JimfsPath checkedTargetPath = checkPath(targetPath);
    if (!(targetDir instanceof JimfsSecureDirectoryStream)) {
      throw new ProviderMismatchException("targetDir isn't a secure directory stream associated with this file system");
    }
    JimfsSecureDirectoryStream checkedTargetDir = (JimfsSecureDirectoryStream)targetDir;
    
    this.view.copy(checkedSrcPath, checkedTargetDir.view, checkedTargetPath, ImmutableSet.of(), true);
  }
  
  public <V extends FileAttributeView> V getFileAttributeView(Class<V> type)
  {
    return getFileAttributeView(path().getFileSystem().getPath(".", new String[0]), type, new LinkOption[0]);
  }
  
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
  {
    checkOpen();
    final JimfsPath checkedPath = checkPath(path);
    final ImmutableSet<LinkOption> optionsSet = Options.getLinkOptions(options);
    this.view.getFileAttributeView(new FileLookup()
    {
      public File lookup()
        throws IOException
      {
        JimfsSecureDirectoryStream.this.checkOpen();
        return JimfsSecureDirectoryStream.this.view.lookUpWithLock(checkedPath, optionsSet).requireExists(checkedPath).file();
      }
    }, type);
  }
  
  private static JimfsPath checkPath(Path path)
  {
    if ((path instanceof JimfsPath)) {
      return (JimfsPath)path;
    }
    throw new ProviderMismatchException("path " + path + " is not associated with a Jimfs file system");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsSecureDirectoryStream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */