package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

final class FileTree
{
  private static final int MAX_SYMBOLIC_LINK_DEPTH = 40;
  private static final ImmutableList<Name> EMPTY_PATH_NAMES = ImmutableList.of(Name.SELF);
  private final ImmutableSortedMap<Name, Directory> roots;
  
  FileTree(Map<Name, Directory> roots)
  {
    this.roots = ImmutableSortedMap.copyOf(roots, Name.canonicalOrdering());
  }
  
  public ImmutableSortedSet<Name> getRootDirectoryNames()
  {
    return this.roots.keySet();
  }
  
  @Nullable
  public DirectoryEntry getRoot(Name name)
  {
    Directory dir = (Directory)this.roots.get(name);
    return dir == null ? null : dir.entryInParent();
  }
  
  public DirectoryEntry lookUp(File workingDirectory, JimfsPath path, Set<? super LinkOption> options)
    throws IOException
  {
    Preconditions.checkNotNull(path);
    Preconditions.checkNotNull(options);
    
    DirectoryEntry result = lookUp(workingDirectory, path, options, 0);
    if (result == null) {
      throw new NoSuchFileException(path.toString());
    }
    return result;
  }
  
  @Nullable
  private DirectoryEntry lookUp(File dir, JimfsPath path, Set<? super LinkOption> options, int linkDepth)
    throws IOException
  {
    ImmutableList<Name> names = path.names();
    if (path.isAbsolute())
    {
      DirectoryEntry entry = getRoot(path.root());
      if (entry == null) {
        return null;
      }
      if (names.isEmpty()) {
        return entry;
      }
      dir = entry.file();
    }
    else if (isEmpty(names))
    {
      names = EMPTY_PATH_NAMES;
    }
    return lookUp(dir, names, options, linkDepth);
  }
  
  @Nullable
  private DirectoryEntry lookUp(File dir, Iterable<Name> names, Set<? super LinkOption> options, int linkDepth)
    throws IOException
  {
    Iterator<Name> nameIterator = names.iterator();
    Name name = (Name)nameIterator.next();
    while (nameIterator.hasNext())
    {
      Directory directory = toDirectory(dir);
      if (directory == null) {
        return null;
      }
      DirectoryEntry entry = directory.get(name);
      if (entry == null) {
        return null;
      }
      File file = entry.file();
      if (file.isSymbolicLink())
      {
        DirectoryEntry linkResult = followSymbolicLink(dir, (SymbolicLink)file, linkDepth);
        if (linkResult == null) {
          return null;
        }
        dir = linkResult.fileOrNull();
      }
      else
      {
        dir = file;
      }
      name = (Name)nameIterator.next();
    }
    return lookUpLast(dir, name, options, linkDepth);
  }
  
  @Nullable
  private DirectoryEntry lookUpLast(@Nullable File dir, Name name, Set<? super LinkOption> options, int linkDepth)
    throws IOException
  {
    Directory directory = toDirectory(dir);
    if (directory == null) {
      return null;
    }
    DirectoryEntry entry = directory.get(name);
    if (entry == null) {
      return new DirectoryEntry(directory, name, null);
    }
    File file = entry.file();
    if ((!options.contains(LinkOption.NOFOLLOW_LINKS)) && (file.isSymbolicLink())) {
      return followSymbolicLink(dir, (SymbolicLink)file, linkDepth);
    }
    return getRealEntry(entry);
  }
  
  @Nullable
  private DirectoryEntry followSymbolicLink(File dir, SymbolicLink link, int linkDepth)
    throws IOException
  {
    if (linkDepth >= 40) {
      throw new IOException("too many levels of symbolic links");
    }
    return lookUp(dir, link.target(), Options.FOLLOW_LINKS, linkDepth + 1);
  }
  
  @Nullable
  private DirectoryEntry getRealEntry(DirectoryEntry entry)
  {
    Name name = entry.name();
    if ((name.equals(Name.SELF)) || (name.equals(Name.PARENT)))
    {
      Directory dir = toDirectory(entry.file());
      assert (dir != null);
      return dir.entryInParent();
    }
    return entry;
  }
  
  @Nullable
  private Directory toDirectory(@Nullable File file)
  {
    return (file == null) || (!file.isDirectory()) ? null : (Directory)file;
  }
  
  private static boolean isEmpty(ImmutableList<Name> names)
  {
    return (names.isEmpty()) || ((names.size() == 1) && (((Name)names.get(0)).toString().isEmpty()));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\FileTree.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */