package com.google.common.jimfs;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.nio.file.InvalidPathException;
import javax.annotation.Nullable;

final class UnixPathType
  extends PathType
{
  static final PathType INSTANCE = new UnixPathType();
  
  private UnixPathType()
  {
    super(false, '/', new char[0]);
  }
  
  public PathType.ParseResult parsePath(String path)
  {
    if (path.isEmpty()) {
      return emptyPath();
    }
    checkValid(path);
    
    String root = path.startsWith("/") ? "/" : null;
    return new PathType.ParseResult(root, splitter().split(path));
  }
  
  private static void checkValid(String path)
  {
    int nulIndex = path.indexOf(0);
    if (nulIndex != -1) {
      throw new InvalidPathException(path, "nul character not allowed", nulIndex);
    }
  }
  
  public String toString(@Nullable String root, Iterable<String> names)
  {
    StringBuilder builder = new StringBuilder();
    if (root != null) {
      builder.append(root);
    }
    joiner().appendTo(builder, names);
    return builder.toString();
  }
  
  public String toUriPath(String root, Iterable<String> names, boolean directory)
  {
    StringBuilder builder = new StringBuilder();
    for (String name : names) {
      builder.append('/').append(name);
    }
    if ((directory) || (builder.length() == 0)) {
      builder.append('/');
    }
    return builder.toString();
  }
  
  public PathType.ParseResult parseUriPath(String uriPath)
  {
    Preconditions.checkArgument(uriPath.startsWith("/"), "uriPath (%s) must start with /", new Object[] { uriPath });
    return parsePath(uriPath);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\UnixPathType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */