package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

final class JimfsPath
  implements Path
{
  @Nullable
  private final Name root;
  private final ImmutableList<Name> names;
  private final PathService pathService;
  
  public JimfsPath(PathService pathService, @Nullable Name root, Iterable<Name> names)
  {
    this.pathService = ((PathService)Preconditions.checkNotNull(pathService));
    this.root = root;
    this.names = ImmutableList.copyOf(names);
  }
  
  @Nullable
  public Name root()
  {
    return this.root;
  }
  
  public ImmutableList<Name> names()
  {
    return this.names;
  }
  
  @Nullable
  public Name name()
  {
    if (!this.names.isEmpty()) {
      return (Name)Iterables.getLast(this.names);
    }
    return this.root;
  }
  
  public boolean isEmptyPath()
  {
    return (this.root == null) && (this.names.size() == 1) && (((Name)this.names.get(0)).toString().isEmpty());
  }
  
  public FileSystem getFileSystem()
  {
    return this.pathService.getFileSystem();
  }
  
  public JimfsFileSystem getJimfsFileSystem()
  {
    return (JimfsFileSystem)this.pathService.getFileSystem();
  }
  
  public boolean isAbsolute()
  {
    return this.root != null;
  }
  
  public JimfsPath getRoot()
  {
    if (this.root == null) {
      return null;
    }
    return this.pathService.createRoot(this.root);
  }
  
  public JimfsPath getFileName()
  {
    return this.names.isEmpty() ? null : getName(this.names.size() - 1);
  }
  
  public JimfsPath getParent()
  {
    if ((this.names.isEmpty()) || ((this.names.size() == 1) && (this.root == null))) {
      return null;
    }
    return this.pathService.createPath(this.root, this.names.subList(0, this.names.size() - 1));
  }
  
  public int getNameCount()
  {
    return this.names.size();
  }
  
  public JimfsPath getName(int index)
  {
    Preconditions.checkArgument((index >= 0) && (index < this.names.size()), "index (%s) must be >= 0 and < name count (%s)", new Object[] { Integer.valueOf(index), Integer.valueOf(this.names.size()) });
    
    return this.pathService.createFileName((Name)this.names.get(index));
  }
  
  public JimfsPath subpath(int beginIndex, int endIndex)
  {
    Preconditions.checkArgument((beginIndex >= 0) && (endIndex <= this.names.size()) && (endIndex > beginIndex), "beginIndex (%s) must be >= 0; endIndex (%s) must be <= name count (%s) and > beginIndex", new Object[] { Integer.valueOf(beginIndex), Integer.valueOf(endIndex), Integer.valueOf(this.names.size()) });
    
    return this.pathService.createRelativePath(this.names.subList(beginIndex, endIndex));
  }
  
  private static boolean startsWith(List<?> list, List<?> other)
  {
    return (list.size() >= other.size()) && (list.subList(0, other.size()).equals(other));
  }
  
  public boolean startsWith(Path other)
  {
    JimfsPath otherPath = checkPath(other);
    return (otherPath != null) && (getFileSystem().equals(otherPath.getFileSystem())) && (Objects.equals(this.root, otherPath.root)) && (startsWith(this.names, otherPath.names));
  }
  
  public boolean startsWith(String other)
  {
    return startsWith(this.pathService.parsePath(other, new String[0]));
  }
  
  public boolean endsWith(Path other)
  {
    JimfsPath otherPath = checkPath(other);
    if (otherPath == null) {
      return false;
    }
    if (otherPath.isAbsolute()) {
      return compareTo(otherPath) == 0;
    }
    return startsWith(this.names.reverse(), otherPath.names.reverse());
  }
  
  public boolean endsWith(String other)
  {
    return endsWith(this.pathService.parsePath(other, new String[0]));
  }
  
  public JimfsPath normalize()
  {
    if (isNormal()) {
      return this;
    }
    Deque<Name> newNames = new ArrayDeque();
    for (Name name : this.names) {
      if (name.equals(Name.PARENT))
      {
        Name lastName = (Name)newNames.peekLast();
        if ((lastName != null) && (!lastName.equals(Name.PARENT))) {
          newNames.removeLast();
        } else if (!isAbsolute()) {
          newNames.add(name);
        }
      }
      else if (!name.equals(Name.SELF))
      {
        newNames.add(name);
      }
    }
    return newNames.equals(this.names) ? this : this.pathService.createPath(this.root, newNames);
  }
  
  private boolean isNormal()
  {
    if ((getNameCount() == 0) || ((getNameCount() == 1) && (!isAbsolute()))) {
      return true;
    }
    boolean foundNonParentName = isAbsolute();
    boolean normal = true;
    for (Name name : this.names) {
      if (name.equals(Name.PARENT))
      {
        if (foundNonParentName)
        {
          normal = false;
          break;
        }
      }
      else
      {
        if (name.equals(Name.SELF))
        {
          normal = false;
          break;
        }
        foundNonParentName = true;
      }
    }
    return normal;
  }
  
  JimfsPath resolve(Name name)
  {
    if (name.toString().isEmpty()) {
      return this;
    }
    return this.pathService.createPathInternal(this.root, ImmutableList.builder().addAll(this.names).add(name).build());
  }
  
  public JimfsPath resolve(Path other)
  {
    JimfsPath otherPath = checkPath(other);
    if (otherPath == null) {
      throw new ProviderMismatchException(other.toString());
    }
    if ((isEmptyPath()) || (otherPath.isAbsolute())) {
      return otherPath;
    }
    if (otherPath.isEmptyPath()) {
      return this;
    }
    return this.pathService.createPath(this.root, ImmutableList.builder().addAll(this.names).addAll(otherPath.names).build());
  }
  
  public JimfsPath resolve(String other)
  {
    return resolve(this.pathService.parsePath(other, new String[0]));
  }
  
  public JimfsPath resolveSibling(Path other)
  {
    JimfsPath otherPath = checkPath(other);
    if (otherPath == null) {
      throw new ProviderMismatchException(other.toString());
    }
    if (otherPath.isAbsolute()) {
      return otherPath;
    }
    JimfsPath parent = getParent();
    if (parent == null) {
      return otherPath;
    }
    return parent.resolve(other);
  }
  
  public JimfsPath resolveSibling(String other)
  {
    return resolveSibling(this.pathService.parsePath(other, new String[0]));
  }
  
  public JimfsPath relativize(Path other)
  {
    JimfsPath otherPath = checkPath(other);
    if (otherPath == null) {
      throw new ProviderMismatchException(other.toString());
    }
    Preconditions.checkArgument(Objects.equals(this.root, otherPath.root), "Paths have different roots: %s, %s", new Object[] { this, other });
    if (equals(other)) {
      return this.pathService.emptyPath();
    }
    if (isEmptyPath()) {
      return otherPath;
    }
    ImmutableList<Name> otherNames = otherPath.names;
    int sharedSubsequenceLength = 0;
    for (int i = 0; i < Math.min(getNameCount(), otherNames.size()); i++)
    {
      if (!((Name)this.names.get(i)).equals(otherNames.get(i))) {
        break;
      }
      sharedSubsequenceLength++;
    }
    int extraNamesInThis = Math.max(0, getNameCount() - sharedSubsequenceLength);
    
    ImmutableList<Name> extraNamesInOther = otherNames.size() <= sharedSubsequenceLength ? ImmutableList.of() : otherNames.subList(sharedSubsequenceLength, otherNames.size());
    
    List<Name> parts = new ArrayList(extraNamesInThis + extraNamesInOther.size());
    
    parts.addAll(Collections.nCopies(extraNamesInThis, Name.PARENT));
    
    parts.addAll(extraNamesInOther);
    
    return this.pathService.createRelativePath(parts);
  }
  
  public JimfsPath toAbsolutePath()
  {
    return isAbsolute() ? this : getJimfsFileSystem().getWorkingDirectory().resolve(this);
  }
  
  public JimfsPath toRealPath(LinkOption... options)
    throws IOException
  {
    return getJimfsFileSystem().getDefaultView().toRealPath(this, this.pathService, Options.getLinkOptions(options));
  }
  
  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers)
    throws IOException
  {
    Preconditions.checkNotNull(modifiers);
    return register(watcher, events);
  }
  
  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events)
    throws IOException
  {
    Preconditions.checkNotNull(watcher);
    Preconditions.checkNotNull(events);
    if (!(watcher instanceof AbstractWatchService)) {
      throw new IllegalArgumentException("watcher (" + watcher + ") is not associated with this file system");
    }
    AbstractWatchService service = (AbstractWatchService)watcher;
    return service.register(this, Arrays.asList(events));
  }
  
  public URI toUri()
  {
    return getJimfsFileSystem().toUri(this);
  }
  
  public File toFile()
  {
    throw new UnsupportedOperationException();
  }
  
  public Iterator<Path> iterator()
  {
    return asList().iterator();
  }
  
  private List<Path> asList()
  {
    new AbstractList()
    {
      public Path get(int index)
      {
        return JimfsPath.this.getName(index);
      }
      
      public int size()
      {
        return JimfsPath.this.getNameCount();
      }
    };
  }
  
  public int compareTo(Path other)
  {
    JimfsPath otherPath = (JimfsPath)other;
    return ComparisonChain.start().compare(getJimfsFileSystem().getUri(), ((JimfsPath)other).getJimfsFileSystem().getUri()).compare(this, otherPath, this.pathService).result();
  }
  
  public boolean equals(@Nullable Object obj)
  {
    return ((obj instanceof JimfsPath)) && (compareTo((JimfsPath)obj) == 0);
  }
  
  public int hashCode()
  {
    return this.pathService.hash(this);
  }
  
  public String toString()
  {
    return this.pathService.toString(this);
  }
  
  @Nullable
  private JimfsPath checkPath(Path other)
  {
    if (((Preconditions.checkNotNull(other) instanceof JimfsPath)) && (other.getFileSystem().equals(getFileSystem()))) {
      return (JimfsPath)other;
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsPath.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */