package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableSortedSet.Builder;
import java.util.Iterator;
import javax.annotation.Nullable;

final class Directory
  extends File
  implements Iterable<DirectoryEntry>
{
  private DirectoryEntry entryInParent;
  private static final int INITIAL_CAPACITY = 16;
  private static final int INITIAL_RESIZE_THRESHOLD = 12;
  
  public static Directory create(int id)
  {
    return new Directory(id);
  }
  
  public static Directory createRoot(int id, Name name)
  {
    return new Directory(id, name);
  }
  
  private Directory(int id)
  {
    super(id);
    put(new DirectoryEntry(this, Name.SELF, this));
  }
  
  private Directory(int id, Name rootName)
  {
    this(id);
    linked(new DirectoryEntry(this, rootName, this));
  }
  
  Directory copyWithoutContent(int id)
  {
    return create(id);
  }
  
  public DirectoryEntry entryInParent()
  {
    return this.entryInParent;
  }
  
  public Directory parent()
  {
    return this.entryInParent.directory();
  }
  
  void linked(DirectoryEntry entry)
  {
    File parent = entry.directory();
    this.entryInParent = entry;
    forcePut(new DirectoryEntry(this, Name.PARENT, parent));
  }
  
  void unlinked()
  {
    parent().decrementLinkCount();
  }
  
  @VisibleForTesting
  int entryCount()
  {
    return this.entryCount;
  }
  
  public boolean isEmpty()
  {
    return entryCount() == 2;
  }
  
  @Nullable
  public DirectoryEntry get(Name name)
  {
    int index = bucketIndex(name, this.table.length);
    
    DirectoryEntry entry = this.table[index];
    while (entry != null)
    {
      if (name.equals(entry.name())) {
        return entry;
      }
      entry = entry.next;
    }
    return null;
  }
  
  public void link(Name name, File file)
  {
    DirectoryEntry entry = new DirectoryEntry(this, checkNotReserved(name, "link"), file);
    put(entry);
    file.linked(entry);
  }
  
  public void unlink(Name name)
  {
    DirectoryEntry entry = remove(checkNotReserved(name, "unlink"));
    entry.file().unlinked();
  }
  
  public ImmutableSortedSet<Name> snapshot()
  {
    ImmutableSortedSet.Builder<Name> builder = new ImmutableSortedSet.Builder(Name.displayOrdering());
    for (DirectoryEntry entry : this) {
      if (!isReserved(entry.name())) {
        builder.add(entry.name());
      }
    }
    return builder.build();
  }
  
  private static Name checkNotReserved(Name name, String action)
  {
    if (isReserved(name)) {
      throw new IllegalArgumentException("cannot " + action + ": " + name);
    }
    return name;
  }
  
  private static boolean isReserved(Name name)
  {
    return (name == Name.SELF) || (name == Name.PARENT);
  }
  
  private DirectoryEntry[] table = new DirectoryEntry[16];
  private int resizeThreshold = 12;
  private int entryCount;
  
  private static int bucketIndex(Name name, int tableLength)
  {
    return name.hashCode() & tableLength - 1;
  }
  
  @VisibleForTesting
  void put(DirectoryEntry entry)
  {
    put(entry, false);
  }
  
  private void forcePut(DirectoryEntry entry)
  {
    put(entry, true);
  }
  
  private void put(DirectoryEntry entry, boolean overwriteExisting)
  {
    int index = bucketIndex(entry.name(), this.table.length);
    
    DirectoryEntry prev = null;
    DirectoryEntry curr = this.table[index];
    while (curr != null)
    {
      if (curr.name().equals(entry.name()))
      {
        if (overwriteExisting)
        {
          if (prev != null) {
            prev.next = entry;
          } else {
            this.table[index] = entry;
          }
          entry.next = curr.next;
          curr.next = null;
          entry.file().incrementLinkCount();
          return;
        }
        throw new IllegalArgumentException("entry '" + entry.name() + "' already exists");
      }
      prev = curr;
      curr = curr.next;
    }
    this.entryCount += 1;
    if (expandIfNeeded())
    {
      index = bucketIndex(entry.name(), this.table.length);
      addToBucket(index, this.table, entry);
    }
    else if (prev != null)
    {
      prev.next = entry;
    }
    else
    {
      this.table[index] = entry;
    }
    entry.file().incrementLinkCount();
  }
  
  private boolean expandIfNeeded()
  {
    if (this.entryCount <= this.resizeThreshold) {
      return false;
    }
    DirectoryEntry[] newTable = new DirectoryEntry[this.table.length << 1];
    for (DirectoryEntry entry : this.table) {
      while (entry != null)
      {
        int index = bucketIndex(entry.name(), newTable.length);
        addToBucket(index, newTable, entry);
        DirectoryEntry next = entry.next;
        
        entry.next = null;
        entry = next;
      }
    }
    this.table = newTable;
    this.resizeThreshold <<= 1;
    return true;
  }
  
  private static void addToBucket(int bucketIndex, DirectoryEntry[] table, DirectoryEntry entryToAdd)
  {
    DirectoryEntry prev = null;
    DirectoryEntry existing = table[bucketIndex];
    while (existing != null)
    {
      prev = existing;
      existing = existing.next;
    }
    if (prev != null) {
      prev.next = entryToAdd;
    } else {
      table[bucketIndex] = entryToAdd;
    }
  }
  
  @VisibleForTesting
  DirectoryEntry remove(Name name)
  {
    int index = bucketIndex(name, this.table.length);
    
    DirectoryEntry prev = null;
    DirectoryEntry entry = this.table[index];
    while (entry != null)
    {
      if (name.equals(entry.name()))
      {
        if (prev != null) {
          prev.next = entry.next;
        } else {
          this.table[index] = entry.next;
        }
        entry.next = null;
        this.entryCount -= 1;
        entry.file().decrementLinkCount();
        return entry;
      }
      prev = entry;
      entry = entry.next;
    }
    throw new IllegalArgumentException("no entry matching '" + name + "' in this directory");
  }
  
  public Iterator<DirectoryEntry> iterator()
  {
    new AbstractIterator()
    {
      int index;
      @Nullable
      DirectoryEntry entry;
      
      protected DirectoryEntry computeNext()
      {
        if (this.entry != null) {
          this.entry = this.entry.next;
        }
        while ((this.entry == null) && (this.index < Directory.this.table.length)) {
          this.entry = Directory.this.table[(this.index++)];
        }
        return this.entry != null ? this.entry : (DirectoryEntry)endOfData();
      }
    };
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\Directory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */