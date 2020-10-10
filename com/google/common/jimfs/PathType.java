package com.google.common.jimfs;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import javax.annotation.Nullable;

public abstract class PathType
{
  private final boolean allowsMultipleRoots;
  private final String separator;
  private final String otherSeparators;
  private final Joiner joiner;
  private final Splitter splitter;
  
  public static PathType unix()
  {
    return UnixPathType.INSTANCE;
  }
  
  public static PathType windows()
  {
    return WindowsPathType.INSTANCE;
  }
  
  protected PathType(boolean allowsMultipleRoots, char separator, char... otherSeparators)
  {
    this.separator = String.valueOf(separator);
    this.allowsMultipleRoots = allowsMultipleRoots;
    this.otherSeparators = String.valueOf(otherSeparators);
    this.joiner = Joiner.on(separator);
    this.splitter = createSplitter(separator, otherSeparators);
  }
  
  private static final char[] regexReservedChars = "^$.?+*\\[]{}()".toCharArray();
  
  static
  {
    Arrays.sort(regexReservedChars);
  }
  
  private static boolean isRegexReserved(char c)
  {
    return Arrays.binarySearch(regexReservedChars, c) >= 0;
  }
  
  private static Splitter createSplitter(char separator, char... otherSeparators)
  {
    if (otherSeparators.length == 0) {
      return Splitter.on(separator).omitEmptyStrings();
    }
    StringBuilder patternBuilder = new StringBuilder();
    patternBuilder.append("[");
    appendToRegex(separator, patternBuilder);
    for (char other : otherSeparators) {
      appendToRegex(other, patternBuilder);
    }
    patternBuilder.append("]");
    return Splitter.onPattern(patternBuilder.toString()).omitEmptyStrings();
  }
  
  private static void appendToRegex(char separator, StringBuilder patternBuilder)
  {
    if (isRegexReserved(separator)) {
      patternBuilder.append("\\");
    }
    patternBuilder.append(separator);
  }
  
  public final boolean allowsMultipleRoots()
  {
    return this.allowsMultipleRoots;
  }
  
  public final String getSeparator()
  {
    return this.separator;
  }
  
  public final String getOtherSeparators()
  {
    return this.otherSeparators;
  }
  
  public final Joiner joiner()
  {
    return this.joiner;
  }
  
  public final Splitter splitter()
  {
    return this.splitter;
  }
  
  protected final ParseResult emptyPath()
  {
    return new ParseResult(null, ImmutableList.of(""));
  }
  
  public final URI toUri(URI fileSystemUri, String root, Iterable<String> names, boolean directory)
  {
    String path = toUriPath(root, names, directory);
    try
    {
      return new URI(fileSystemUri.getScheme(), fileSystemUri.getUserInfo(), fileSystemUri.getHost(), fileSystemUri.getPort(), path, null, null);
    }
    catch (URISyntaxException e)
    {
      throw new AssertionError(e);
    }
  }
  
  public final ParseResult fromUri(URI uri)
  {
    return parseUriPath(uri.getPath());
  }
  
  public abstract ParseResult parsePath(String paramString);
  
  public abstract String toString(@Nullable String paramString, Iterable<String> paramIterable);
  
  protected abstract String toUriPath(String paramString, Iterable<String> paramIterable, boolean paramBoolean);
  
  protected abstract ParseResult parseUriPath(String paramString);
  
  public static final class ParseResult
  {
    @Nullable
    private final String root;
    private final Iterable<String> names;
    
    public ParseResult(@Nullable String root, Iterable<String> names)
    {
      this.root = root;
      this.names = ((Iterable)Preconditions.checkNotNull(names));
    }
    
    public boolean isAbsolute()
    {
      return this.root != null;
    }
    
    public boolean isRoot()
    {
      return (this.root != null) && (Iterables.isEmpty(this.names));
    }
    
    @Nullable
    public String root()
    {
      return this.root;
    }
    
    public Iterable<String> names()
    {
      return this.names;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\PathType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */