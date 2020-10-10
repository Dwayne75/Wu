package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

final class PathService
  implements Comparator<JimfsPath>
{
  private static final Ordering<Name> DISPLAY_ROOT_ORDERING = Name.displayOrdering().nullsLast();
  private static final Ordering<Iterable<Name>> DISPLAY_NAMES_ORDERING = Name.displayOrdering().lexicographical();
  private static final Ordering<Name> CANONICAL_ROOT_ORDERING = Name.canonicalOrdering().nullsLast();
  private static final Ordering<Iterable<Name>> CANONICAL_NAMES_ORDERING = Name.canonicalOrdering().lexicographical();
  private final PathType type;
  private final ImmutableSet<PathNormalization> displayNormalizations;
  private final ImmutableSet<PathNormalization> canonicalNormalizations;
  private final boolean equalityUsesCanonicalForm;
  private final Ordering<Name> rootOrdering;
  private final Ordering<Iterable<Name>> namesOrdering;
  private volatile FileSystem fileSystem;
  private volatile JimfsPath emptyPath;
  
  PathService(Configuration config)
  {
    this(config.pathType, config.nameDisplayNormalization, config.nameCanonicalNormalization, config.pathEqualityUsesCanonicalForm);
  }
  
  PathService(PathType type, Iterable<PathNormalization> displayNormalizations, Iterable<PathNormalization> canonicalNormalizations, boolean equalityUsesCanonicalForm)
  {
    this.type = ((PathType)Preconditions.checkNotNull(type));
    this.displayNormalizations = ImmutableSet.copyOf(displayNormalizations);
    this.canonicalNormalizations = ImmutableSet.copyOf(canonicalNormalizations);
    this.equalityUsesCanonicalForm = equalityUsesCanonicalForm;
    
    this.rootOrdering = (equalityUsesCanonicalForm ? CANONICAL_ROOT_ORDERING : DISPLAY_ROOT_ORDERING);
    this.namesOrdering = (equalityUsesCanonicalForm ? CANONICAL_NAMES_ORDERING : DISPLAY_NAMES_ORDERING);
  }
  
  public void setFileSystem(FileSystem fileSystem)
  {
    Preconditions.checkState(this.fileSystem == null, "may not set fileSystem twice");
    this.fileSystem = ((FileSystem)Preconditions.checkNotNull(fileSystem));
  }
  
  public FileSystem getFileSystem()
  {
    return this.fileSystem;
  }
  
  public String getSeparator()
  {
    return this.type.getSeparator();
  }
  
  public JimfsPath emptyPath()
  {
    JimfsPath result = this.emptyPath;
    if (result == null)
    {
      result = createPathInternal(null, ImmutableList.of(Name.EMPTY));
      this.emptyPath = result;
      return result;
    }
    return result;
  }
  
  public Name name(String name)
  {
    switch (name)
    {
    case "": 
      return Name.EMPTY;
    case ".": 
      return Name.SELF;
    case "..": 
      return Name.PARENT;
    }
    String display = PathNormalization.normalize(name, this.displayNormalizations);
    String canonical = PathNormalization.normalize(name, this.canonicalNormalizations);
    return Name.create(display, canonical);
  }
  
  @VisibleForTesting
  List<Name> names(Iterable<String> names)
  {
    List<Name> result = new ArrayList();
    for (String name : names) {
      result.add(name(name));
    }
    return result;
  }
  
  public JimfsPath createRoot(Name root)
  {
    return createPath((Name)Preconditions.checkNotNull(root), ImmutableList.of());
  }
  
  public JimfsPath createFileName(Name name)
  {
    return createPath(null, ImmutableList.of(name));
  }
  
  public JimfsPath createRelativePath(Iterable<Name> names)
  {
    return createPath(null, ImmutableList.copyOf(names));
  }
  
  public JimfsPath createPath(@Nullable Name root, Iterable<Name> names)
  {
    ImmutableList<Name> nameList = ImmutableList.copyOf(Iterables.filter(names, NOT_EMPTY));
    if ((root == null) && (nameList.isEmpty())) {
      return emptyPath();
    }
    return createPathInternal(root, nameList);
  }
  
  protected final JimfsPath createPathInternal(@Nullable Name root, Iterable<Name> names)
  {
    return new JimfsPath(this, root, names);
  }
  
  public JimfsPath parsePath(String first, String... more)
  {
    String joined = this.type.joiner().join(Iterables.filter(Lists.asList(first, more), NOT_EMPTY));
    return toPath(this.type.parsePath(joined));
  }
  
  private JimfsPath toPath(PathType.ParseResult parsed)
  {
    Name root = parsed.root() == null ? null : name(parsed.root());
    Iterable<Name> names = names(parsed.names());
    return createPath(root, names);
  }
  
  public String toString(JimfsPath path)
  {
    Name root = path.root();
    String rootString = root == null ? null : root.toString();
    Iterable<String> names = Iterables.transform(path.names(), Functions.toStringFunction());
    return this.type.toString(rootString, names);
  }
  
  public int hash(JimfsPath path)
  {
    int hash = 31;
    hash = 31 * hash + getFileSystem().hashCode();
    
    Name root = path.root();
    ImmutableList<Name> names = path.names();
    if (this.equalityUsesCanonicalForm)
    {
      hash = 31 * hash + (root == null ? 0 : root.hashCode());
      for (Name name : names) {
        hash = 31 * hash + name.hashCode();
      }
    }
    else
    {
      hash = 31 * hash + (root == null ? 0 : root.toString().hashCode());
      for (Name name : names) {
        hash = 31 * hash + name.toString().hashCode();
      }
    }
    return hash;
  }
  
  public int compare(JimfsPath a, JimfsPath b)
  {
    return ComparisonChain.start().compare(a.root(), b.root(), this.rootOrdering).compare(a.names(), b.names(), this.namesOrdering).result();
  }
  
  public URI toUri(URI fileSystemUri, JimfsPath path)
  {
    Preconditions.checkArgument(path.isAbsolute(), "path (%s) must be absolute", new Object[] { path });
    String root = String.valueOf(path.root());
    Iterable<String> names = Iterables.transform(path.names(), Functions.toStringFunction());
    return this.type.toUri(fileSystemUri, root, names, Files.isDirectory(path, new LinkOption[] { LinkOption.NOFOLLOW_LINKS }));
  }
  
  public JimfsPath fromUri(URI uri)
  {
    return toPath(this.type.fromUri(uri));
  }
  
  public PathMatcher createPathMatcher(String syntaxAndPattern)
  {
    return PathMatchers.getPathMatcher(syntaxAndPattern, this.type.getSeparator() + this.type.getOtherSeparators(), this.displayNormalizations);
  }
  
  private static final Predicate<Object> NOT_EMPTY = new Predicate()
  {
    public boolean apply(Object input)
    {
      return !input.toString().isEmpty();
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\PathService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */