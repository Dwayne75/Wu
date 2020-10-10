package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;

final class JimfsFileStore
  extends FileStore
{
  private final FileTree tree;
  private final HeapDisk disk;
  private final AttributeService attributes;
  private final FileFactory factory;
  private final ImmutableSet<Feature> supportedFeatures;
  private final FileSystemState state;
  private final Lock readLock;
  private final Lock writeLock;
  
  public JimfsFileStore(FileTree tree, FileFactory factory, HeapDisk disk, AttributeService attributes, ImmutableSet<Feature> supportedFeatures, FileSystemState state)
  {
    this.tree = ((FileTree)Preconditions.checkNotNull(tree));
    this.factory = ((FileFactory)Preconditions.checkNotNull(factory));
    this.disk = ((HeapDisk)Preconditions.checkNotNull(disk));
    this.attributes = ((AttributeService)Preconditions.checkNotNull(attributes));
    this.supportedFeatures = ((ImmutableSet)Preconditions.checkNotNull(supportedFeatures));
    this.state = ((FileSystemState)Preconditions.checkNotNull(state));
    
    ReadWriteLock lock = new ReentrantReadWriteLock();
    this.readLock = lock.readLock();
    this.writeLock = lock.writeLock();
  }
  
  FileSystemState state()
  {
    return this.state;
  }
  
  Lock readLock()
  {
    return this.readLock;
  }
  
  Lock writeLock()
  {
    return this.writeLock;
  }
  
  ImmutableSortedSet<Name> getRootDirectoryNames()
  {
    this.state.checkOpen();
    return this.tree.getRootDirectoryNames();
  }
  
  @Nullable
  Directory getRoot(Name name)
  {
    DirectoryEntry entry = this.tree.getRoot(name);
    return entry == null ? null : (Directory)entry.file();
  }
  
  boolean supportsFeature(Feature feature)
  {
    return this.supportedFeatures.contains(feature);
  }
  
  DirectoryEntry lookUp(File workingDirectory, JimfsPath path, Set<? super LinkOption> options)
    throws IOException
  {
    this.state.checkOpen();
    return this.tree.lookUp(workingDirectory, path, options);
  }
  
  Supplier<RegularFile> regularFileCreator()
  {
    this.state.checkOpen();
    return this.factory.regularFileCreator();
  }
  
  Supplier<Directory> directoryCreator()
  {
    this.state.checkOpen();
    return this.factory.directoryCreator();
  }
  
  Supplier<SymbolicLink> symbolicLinkCreator(JimfsPath target)
  {
    this.state.checkOpen();
    return this.factory.symbolicLinkCreator(target);
  }
  
  File copyWithoutContent(File file, AttributeCopyOption attributeCopyOption)
    throws IOException
  {
    File copy = this.factory.copyWithoutContent(file);
    setInitialAttributes(copy, new FileAttribute[0]);
    this.attributes.copyAttributes(file, copy, attributeCopyOption);
    return copy;
  }
  
  void setInitialAttributes(File file, FileAttribute<?>... attrs)
  {
    this.state.checkOpen();
    this.attributes.setInitialAttributes(file, attrs);
  }
  
  @Nullable
  <V extends FileAttributeView> V getFileAttributeView(FileLookup lookup, Class<V> type)
  {
    this.state.checkOpen();
    return this.attributes.getFileAttributeView(lookup, type);
  }
  
  ImmutableMap<String, Object> readAttributes(File file, String attributes)
  {
    this.state.checkOpen();
    return this.attributes.readAttributes(file, attributes);
  }
  
  <A extends BasicFileAttributes> A readAttributes(File file, Class<A> type)
  {
    this.state.checkOpen();
    return this.attributes.readAttributes(file, type);
  }
  
  void setAttribute(File file, String attribute, Object value)
  {
    this.state.checkOpen();
    
    this.attributes.setAttribute(file, attribute, value, false);
  }
  
  ImmutableSet<String> supportedFileAttributeViews()
  {
    this.state.checkOpen();
    return this.attributes.supportedFileAttributeViews();
  }
  
  public String name()
  {
    return "jimfs";
  }
  
  public String type()
  {
    return "jimfs";
  }
  
  public boolean isReadOnly()
  {
    return false;
  }
  
  public long getTotalSpace()
    throws IOException
  {
    this.state.checkOpen();
    return this.disk.getTotalSpace();
  }
  
  public long getUsableSpace()
    throws IOException
  {
    this.state.checkOpen();
    return getUnallocatedSpace();
  }
  
  public long getUnallocatedSpace()
    throws IOException
  {
    this.state.checkOpen();
    return this.disk.getUnallocatedSpace();
  }
  
  public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type)
  {
    this.state.checkOpen();
    return this.attributes.supportsFileAttributeView(type);
  }
  
  public boolean supportsFileAttributeView(String name)
  {
    this.state.checkOpen();
    return this.attributes.supportedFileAttributeViews().contains(name);
  }
  
  public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type)
  {
    this.state.checkOpen();
    return null;
  }
  
  public Object getAttribute(String attribute)
    throws IOException
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsFileStore.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */