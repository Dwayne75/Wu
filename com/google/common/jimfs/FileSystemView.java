package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import javax.annotation.Nullable;

final class FileSystemView
{
  private final JimfsFileStore store;
  private final Directory workingDirectory;
  private final JimfsPath workingDirectoryPath;
  
  public FileSystemView(JimfsFileStore store, Directory workingDirectory, JimfsPath workingDirectoryPath)
  {
    this.store = ((JimfsFileStore)Preconditions.checkNotNull(store));
    this.workingDirectory = ((Directory)Preconditions.checkNotNull(workingDirectory));
    this.workingDirectoryPath = ((JimfsPath)Preconditions.checkNotNull(workingDirectoryPath));
  }
  
  private boolean isSameFileSystem(FileSystemView other)
  {
    return this.store == other.store;
  }
  
  public FileSystemState state()
  {
    return this.store.state();
  }
  
  public JimfsPath getWorkingDirectoryPath()
  {
    return this.workingDirectoryPath;
  }
  
  DirectoryEntry lookUpWithLock(JimfsPath path, Set<? super LinkOption> options)
    throws IOException
  {
    this.store.readLock().lock();
    try
    {
      return lookUp(path, options);
    }
    finally
    {
      this.store.readLock().unlock();
    }
  }
  
  private DirectoryEntry lookUp(JimfsPath path, Set<? super LinkOption> options)
    throws IOException
  {
    return this.store.lookUp(this.workingDirectory, path, options);
  }
  
  public DirectoryStream<Path> newDirectoryStream(JimfsPath dir, DirectoryStream.Filter<? super Path> filter, Set<? super LinkOption> options, JimfsPath basePathForStream)
    throws IOException
  {
    Directory file = (Directory)lookUpWithLock(dir, options).requireDirectory(dir).file();
    
    FileSystemView view = new FileSystemView(this.store, file, basePathForStream);
    JimfsSecureDirectoryStream stream = new JimfsSecureDirectoryStream(view, filter, state());
    return this.store.supportsFeature(Feature.SECURE_DIRECTORY_STREAM) ? stream : new DowngradedDirectoryStream(stream);
  }
  
  public ImmutableSortedSet<Name> snapshotWorkingDirectoryEntries()
  {
    this.store.readLock().lock();
    try
    {
      ImmutableSortedSet<Name> names = this.workingDirectory.snapshot();
      this.workingDirectory.updateAccessTime();
      return names;
    }
    finally
    {
      this.store.readLock().unlock();
    }
  }
  
  public ImmutableMap<Name, Long> snapshotModifiedTimes(JimfsPath path)
    throws IOException
  {
    ImmutableMap.Builder<Name, Long> modifiedTimes = ImmutableMap.builder();
    
    this.store.readLock().lock();
    try
    {
      Directory dir = (Directory)lookUp(path, Options.FOLLOW_LINKS).requireDirectory(path).file();
      for (DirectoryEntry entry : dir) {
        if ((!entry.name().equals(Name.SELF)) && (!entry.name().equals(Name.PARENT))) {
          modifiedTimes.put(entry.name(), Long.valueOf(entry.file().getLastModifiedTime()));
        }
      }
      return modifiedTimes.build();
    }
    finally
    {
      this.store.readLock().unlock();
    }
  }
  
  public boolean isSameFile(JimfsPath path, FileSystemView view2, JimfsPath path2)
    throws IOException
  {
    if (!isSameFileSystem(view2)) {
      return false;
    }
    this.store.readLock().lock();
    try
    {
      File file = lookUp(path, Options.FOLLOW_LINKS).fileOrNull();
      File file2 = view2.lookUp(path2, Options.FOLLOW_LINKS).fileOrNull();
      return (file != null) && (Objects.equals(file, file2));
    }
    finally
    {
      this.store.readLock().unlock();
    }
  }
  
  public JimfsPath toRealPath(JimfsPath path, PathService pathService, Set<? super LinkOption> options)
    throws IOException
  {
    Preconditions.checkNotNull(path);
    Preconditions.checkNotNull(options);
    
    this.store.readLock().lock();
    try
    {
      DirectoryEntry entry = lookUp(path, options).requireExists(path);
      
      List<Name> names = new ArrayList();
      names.add(entry.name());
      while (!entry.file().isRootDirectory())
      {
        entry = entry.directory().entryInParent();
        names.add(entry.name());
      }
      List<Name> reversed = Lists.reverse(names);
      Name root = (Name)reversed.remove(0);
      return pathService.createPath(root, reversed);
    }
    finally
    {
      this.store.readLock().unlock();
    }
  }
  
  public Directory createDirectory(JimfsPath path, FileAttribute<?>... attrs)
    throws IOException
  {
    return (Directory)createFile(path, this.store.directoryCreator(), true, attrs);
  }
  
  public SymbolicLink createSymbolicLink(JimfsPath path, JimfsPath target, FileAttribute<?>... attrs)
    throws IOException
  {
    if (!this.store.supportsFeature(Feature.SYMBOLIC_LINKS)) {
      throw new UnsupportedOperationException();
    }
    return (SymbolicLink)createFile(path, this.store.symbolicLinkCreator(target), true, attrs);
  }
  
  private File createFile(JimfsPath path, Supplier<? extends File> fileCreator, boolean failIfExists, FileAttribute<?>... attrs)
    throws IOException
  {
    Preconditions.checkNotNull(path);
    Preconditions.checkNotNull(fileCreator);
    
    this.store.writeLock().lock();
    try
    {
      DirectoryEntry entry = lookUp(path, Options.NOFOLLOW_LINKS);
      if (entry.exists())
      {
        if (failIfExists) {
          throw new FileAlreadyExistsException(path.toString());
        }
        return entry.file();
      }
      Directory parent = entry.directory();
      
      File newFile = (File)fileCreator.get();
      this.store.setInitialAttributes(newFile, attrs);
      parent.link(path.name(), newFile);
      parent.updateModifiedTime();
      return newFile;
    }
    finally
    {
      this.store.writeLock().unlock();
    }
  }
  
  public RegularFile getOrCreateRegularFile(JimfsPath path, Set<OpenOption> options, FileAttribute<?>... attrs)
    throws IOException
  {
    Preconditions.checkNotNull(path);
    if (!options.contains(StandardOpenOption.CREATE_NEW))
    {
      RegularFile file = lookUpRegularFile(path, options);
      if (file != null) {
        return file;
      }
    }
    if ((options.contains(StandardOpenOption.CREATE)) || (options.contains(StandardOpenOption.CREATE_NEW))) {
      return getOrCreateRegularFileWithWriteLock(path, options, attrs);
    }
    throw new NoSuchFileException(path.toString());
  }
  
  @Nullable
  private RegularFile lookUpRegularFile(JimfsPath path, Set<OpenOption> options)
    throws IOException
  {
    this.store.readLock().lock();
    try
    {
      DirectoryEntry entry = lookUp(path, options);
      File file;
      if (entry.exists())
      {
        file = entry.file();
        if (!file.isRegularFile()) {
          throw new FileSystemException(path.toString(), null, "not a regular file");
        }
        return open((RegularFile)file, options);
      }
      return null;
    }
    finally
    {
      this.store.readLock().unlock();
    }
  }
  
  private RegularFile getOrCreateRegularFileWithWriteLock(JimfsPath path, Set<OpenOption> options, FileAttribute<?>[] attrs)
    throws IOException
  {
    this.store.writeLock().lock();
    try
    {
      File file = createFile(path, this.store.regularFileCreator(), options.contains(StandardOpenOption.CREATE_NEW), attrs);
      if (!file.isRegularFile()) {
        throw new FileSystemException(path.toString(), null, "not a regular file");
      }
      return open((RegularFile)file, options);
    }
    finally
    {
      this.store.writeLock().unlock();
    }
  }
  
  private static RegularFile open(RegularFile file, Set<OpenOption> options)
  {
    if ((options.contains(StandardOpenOption.TRUNCATE_EXISTING)) && (options.contains(StandardOpenOption.WRITE)))
    {
      file.writeLock().lock();
      try
      {
        file.truncate(0L);
      }
      finally
      {
        file.writeLock().unlock();
      }
    }
    file.opened();
    
    return file;
  }
  
  public JimfsPath readSymbolicLink(JimfsPath path)
    throws IOException
  {
    if (!this.store.supportsFeature(Feature.SYMBOLIC_LINKS)) {
      throw new UnsupportedOperationException();
    }
    SymbolicLink symbolicLink = (SymbolicLink)lookUpWithLock(path, Options.NOFOLLOW_LINKS).requireSymbolicLink(path).file();
    
    return symbolicLink.target();
  }
  
  public void checkAccess(JimfsPath path)
    throws IOException
  {
    lookUpWithLock(path, Options.FOLLOW_LINKS).requireExists(path);
  }
  
  public void link(JimfsPath link, FileSystemView existingView, JimfsPath existing)
    throws IOException
  {
    Preconditions.checkNotNull(link);
    Preconditions.checkNotNull(existingView);
    Preconditions.checkNotNull(existing);
    if (!this.store.supportsFeature(Feature.LINKS)) {
      throw new UnsupportedOperationException();
    }
    if (!isSameFileSystem(existingView)) {
      throw new FileSystemException(link.toString(), existing.toString(), "can't link: source and target are in different file system instances");
    }
    Name linkName = link.name();
    
    this.store.writeLock().lock();
    try
    {
      File existingFile = existingView.lookUp(existing, Options.FOLLOW_LINKS).requireExists(existing).file();
      if (!existingFile.isRegularFile()) {
        throw new FileSystemException(link.toString(), existing.toString(), "can't link: not a regular file");
      }
      Directory linkParent = lookUp(link, Options.NOFOLLOW_LINKS).requireDoesNotExist(link).directory();
      
      linkParent.link(linkName, existingFile);
      linkParent.updateModifiedTime();
    }
    finally
    {
      this.store.writeLock().unlock();
    }
  }
  
  public void deleteFile(JimfsPath path, DeleteMode deleteMode)
    throws IOException
  {
    this.store.writeLock().lock();
    try
    {
      DirectoryEntry entry = lookUp(path, Options.NOFOLLOW_LINKS).requireExists(path);
      delete(entry, deleteMode, path);
    }
    finally
    {
      this.store.writeLock().unlock();
    }
  }
  
  private void delete(DirectoryEntry entry, DeleteMode deleteMode, JimfsPath pathForException)
    throws IOException
  {
    Directory parent = entry.directory();
    File file = entry.file();
    
    checkDeletable(file, deleteMode, pathForException);
    parent.unlink(entry.name());
    parent.updateModifiedTime();
    
    file.deleted();
  }
  
  public static enum DeleteMode
  {
    ANY,  NON_DIRECTORY_ONLY,  DIRECTORY_ONLY;
    
    private DeleteMode() {}
  }
  
  private void checkDeletable(File file, DeleteMode mode, Path path)
    throws IOException
  {
    if (file.isRootDirectory()) {
      throw new FileSystemException(path.toString(), null, "can't delete root directory");
    }
    if (file.isDirectory())
    {
      if (mode == DeleteMode.NON_DIRECTORY_ONLY) {
        throw new FileSystemException(path.toString(), null, "can't delete: is a directory");
      }
      checkEmpty((Directory)file, path);
    }
    else if (mode == DeleteMode.DIRECTORY_ONLY)
    {
      throw new FileSystemException(path.toString(), null, "can't delete: is not a directory");
    }
    if ((file == this.workingDirectory) && (!path.isAbsolute())) {
      throw new FileSystemException(path.toString(), null, "invalid argument");
    }
  }
  
  private void checkEmpty(Directory dir, Path pathForException)
    throws FileSystemException
  {
    if (!dir.isEmpty()) {
      throw new DirectoryNotEmptyException(pathForException.toString());
    }
  }
  
  public void copy(JimfsPath source, FileSystemView destView, JimfsPath dest, Set<CopyOption> options, boolean move)
    throws IOException
  {
    Preconditions.checkNotNull(source);
    Preconditions.checkNotNull(destView);
    Preconditions.checkNotNull(dest);
    Preconditions.checkNotNull(options);
    
    boolean sameFileSystem = isSameFileSystem(destView);
    
    File copyFile = null;
    lockBoth(this.store.writeLock(), destView.store.writeLock());
    File sourceFile;
    try
    {
      DirectoryEntry sourceEntry = lookUp(source, options).requireExists(source);
      DirectoryEntry destEntry = destView.lookUp(dest, Options.NOFOLLOW_LINKS);
      
      Directory sourceParent = sourceEntry.directory();
      sourceFile = sourceEntry.file();
      
      Directory destParent = destEntry.directory();
      if ((move) && (sourceFile.isDirectory())) {
        if (sameFileSystem)
        {
          checkMovable(sourceFile, source);
          checkNotAncestor(sourceFile, destParent, destView);
        }
        else
        {
          checkDeletable(sourceFile, DeleteMode.ANY, source);
        }
      }
      if (destEntry.exists())
      {
        if (destEntry.file().equals(sourceFile)) {
          return;
        }
        if (options.contains(StandardCopyOption.REPLACE_EXISTING)) {
          destView.delete(destEntry, DeleteMode.ANY, dest);
        } else {
          throw new FileAlreadyExistsException(dest.toString());
        }
      }
      if ((move) && (sameFileSystem))
      {
        sourceParent.unlink(source.name());
        sourceParent.updateModifiedTime();
        
        destParent.link(dest.name(), sourceFile);
        destParent.updateModifiedTime();
      }
      else
      {
        AttributeCopyOption attributeCopyOption = AttributeCopyOption.NONE;
        if (move) {
          attributeCopyOption = AttributeCopyOption.BASIC;
        } else if (options.contains(StandardCopyOption.COPY_ATTRIBUTES)) {
          attributeCopyOption = sameFileSystem ? AttributeCopyOption.ALL : AttributeCopyOption.BASIC;
        }
        copyFile = destView.store.copyWithoutContent(sourceFile, attributeCopyOption);
        destParent.link(dest.name(), copyFile);
        destParent.updateModifiedTime();
        
        lockSourceAndCopy(sourceFile, copyFile);
        if (move) {
          delete(sourceEntry, DeleteMode.ANY, source);
        }
      }
    }
    finally
    {
      destView.store.writeLock().unlock();
      this.store.writeLock().unlock();
    }
    if (copyFile != null) {
      try
      {
        sourceFile.copyContentTo(copyFile);
      }
      finally
      {
        unlockSourceAndCopy(sourceFile, copyFile);
      }
    }
  }
  
  private void checkMovable(File file, JimfsPath path)
    throws FileSystemException
  {
    if (file.isRootDirectory()) {
      throw new FileSystemException(path.toString(), null, "can't move root directory");
    }
  }
  
  private static void lockBoth(Lock sourceWriteLock, Lock destWriteLock)
  {
    for (;;)
    {
      sourceWriteLock.lock();
      if (destWriteLock.tryLock()) {
        return;
      }
      sourceWriteLock.unlock();
      
      destWriteLock.lock();
      if (sourceWriteLock.tryLock()) {
        return;
      }
      destWriteLock.unlock();
    }
  }
  
  private void checkNotAncestor(File source, Directory destParent, FileSystemView destView)
    throws IOException
  {
    if (!isSameFileSystem(destView)) {
      return;
    }
    Directory current = destParent;
    for (;;)
    {
      if (current.equals(source)) {
        throw new IOException("invalid argument: can't move directory into a subdirectory of itself");
      }
      if (current.isRootDirectory()) {
        return;
      }
      current = current.parent();
    }
  }
  
  private void lockSourceAndCopy(File sourceFile, File copyFile)
  {
    sourceFile.opened();
    ReadWriteLock sourceLock = sourceFile.contentLock();
    if (sourceLock != null) {
      sourceLock.readLock().lock();
    }
    ReadWriteLock copyLock = copyFile.contentLock();
    if (copyLock != null) {
      copyLock.writeLock().lock();
    }
  }
  
  private void unlockSourceAndCopy(File sourceFile, File copyFile)
  {
    ReadWriteLock sourceLock = sourceFile.contentLock();
    if (sourceLock != null) {
      sourceLock.readLock().unlock();
    }
    ReadWriteLock copyLock = copyFile.contentLock();
    if (copyLock != null) {
      copyLock.writeLock().unlock();
    }
    sourceFile.closed();
  }
  
  @Nullable
  public <V extends FileAttributeView> V getFileAttributeView(FileLookup lookup, Class<V> type)
  {
    return this.store.getFileAttributeView(lookup, type);
  }
  
  @Nullable
  public <V extends FileAttributeView> V getFileAttributeView(final JimfsPath path, Class<V> type, final Set<? super LinkOption> options)
  {
    this.store.getFileAttributeView(new FileLookup()
    {
      public File lookup()
        throws IOException
      {
        return FileSystemView.this.lookUpWithLock(path, options).requireExists(path).file();
      }
    }, type);
  }
  
  public <A extends BasicFileAttributes> A readAttributes(JimfsPath path, Class<A> type, Set<? super LinkOption> options)
    throws IOException
  {
    File file = lookUpWithLock(path, options).requireExists(path).file();
    
    return this.store.readAttributes(file, type);
  }
  
  public ImmutableMap<String, Object> readAttributes(JimfsPath path, String attributes, Set<? super LinkOption> options)
    throws IOException
  {
    File file = lookUpWithLock(path, options).requireExists(path).file();
    
    return this.store.readAttributes(file, attributes);
  }
  
  public void setAttribute(JimfsPath path, String attribute, Object value, Set<? super LinkOption> options)
    throws IOException
  {
    File file = lookUpWithLock(path, options).requireExists(path).file();
    
    this.store.setAttribute(file, attribute, value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\FileSystemView.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */