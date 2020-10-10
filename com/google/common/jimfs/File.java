package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import javax.annotation.Nullable;

public abstract class File
{
  private final int id;
  private int links;
  private long creationTime;
  private long lastAccessTime;
  private long lastModifiedTime;
  @Nullable
  private Table<String, String, Object> attributes;
  
  File(int id)
  {
    this.id = id;
    
    long now = System.currentTimeMillis();
    this.creationTime = now;
    this.lastAccessTime = now;
    this.lastModifiedTime = now;
  }
  
  public int id()
  {
    return this.id;
  }
  
  public long size()
  {
    return 0L;
  }
  
  public final boolean isDirectory()
  {
    return this instanceof Directory;
  }
  
  public final boolean isRegularFile()
  {
    return this instanceof RegularFile;
  }
  
  public final boolean isSymbolicLink()
  {
    return this instanceof SymbolicLink;
  }
  
  abstract File copyWithoutContent(int paramInt);
  
  void copyContentTo(File file)
    throws IOException
  {}
  
  @Nullable
  ReadWriteLock contentLock()
  {
    return null;
  }
  
  void opened() {}
  
  void closed() {}
  
  void deleted() {}
  
  final boolean isRootDirectory()
  {
    return (isDirectory()) && (equals(((Directory)this).parent()));
  }
  
  public final synchronized int links()
  {
    return this.links;
  }
  
  void linked(DirectoryEntry entry)
  {
    Preconditions.checkNotNull(entry);
  }
  
  void unlinked() {}
  
  final synchronized void incrementLinkCount()
  {
    this.links += 1;
  }
  
  final synchronized void decrementLinkCount()
  {
    this.links -= 1;
  }
  
  public final synchronized long getCreationTime()
  {
    return this.creationTime;
  }
  
  public final synchronized long getLastAccessTime()
  {
    return this.lastAccessTime;
  }
  
  public final synchronized long getLastModifiedTime()
  {
    return this.lastModifiedTime;
  }
  
  final synchronized void setCreationTime(long creationTime)
  {
    this.creationTime = creationTime;
  }
  
  final synchronized void setLastAccessTime(long lastAccessTime)
  {
    this.lastAccessTime = lastAccessTime;
  }
  
  final synchronized void setLastModifiedTime(long lastModifiedTime)
  {
    this.lastModifiedTime = lastModifiedTime;
  }
  
  final void updateAccessTime()
  {
    setLastAccessTime(System.currentTimeMillis());
  }
  
  final void updateModifiedTime()
  {
    setLastModifiedTime(System.currentTimeMillis());
  }
  
  public final synchronized ImmutableSet<String> getAttributeNames(String view)
  {
    if (this.attributes == null) {
      return ImmutableSet.of();
    }
    return ImmutableSet.copyOf(this.attributes.row(view).keySet());
  }
  
  @VisibleForTesting
  final synchronized ImmutableSet<String> getAttributeKeys()
  {
    if (this.attributes == null) {
      return ImmutableSet.of();
    }
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (Table.Cell<String, String, Object> cell : this.attributes.cellSet()) {
      builder.add((String)cell.getRowKey() + ':' + (String)cell.getColumnKey());
    }
    return builder.build();
  }
  
  @Nullable
  public final synchronized Object getAttribute(String view, String attribute)
  {
    if (this.attributes == null) {
      return null;
    }
    return this.attributes.get(view, attribute);
  }
  
  public final synchronized void setAttribute(String view, String attribute, Object value)
  {
    if (this.attributes == null) {
      this.attributes = HashBasedTable.create();
    }
    this.attributes.put(view, attribute, value);
  }
  
  public final synchronized void deleteAttribute(String view, String attribute)
  {
    if (this.attributes != null) {
      this.attributes.remove(view, attribute);
    }
  }
  
  final synchronized void copyBasicAttributes(File target)
  {
    target.setFileTimes(this.creationTime, this.lastModifiedTime, this.lastAccessTime);
  }
  
  private synchronized void setFileTimes(long creationTime, long lastModifiedTime, long lastAccessTime)
  {
    this.creationTime = creationTime;
    this.lastModifiedTime = lastModifiedTime;
    this.lastAccessTime = lastAccessTime;
  }
  
  final synchronized void copyAttributes(File target)
  {
    copyBasicAttributes(target);
    target.putAll(this.attributes);
  }
  
  private synchronized void putAll(@Nullable Table<String, String, Object> attributes)
  {
    if ((attributes != null) && (this.attributes != attributes))
    {
      if (this.attributes == null) {
        this.attributes = HashBasedTable.create();
      }
      this.attributes.putAll(attributes);
    }
  }
  
  public final String toString()
  {
    return MoreObjects.toStringHelper(this).add("id", id()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\File.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */