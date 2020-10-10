package com.google.common.jimfs;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.nio.file.InvalidPathException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

final class WindowsPathType
  extends PathType
{
  static final WindowsPathType INSTANCE = new WindowsPathType();
  private static final Pattern WORKING_DIR_WITH_DRIVE = Pattern.compile("^[a-zA-Z]:([^\\\\].*)?$");
  private static final Pattern TRAILING_SPACES = Pattern.compile("[ ]+(\\\\|$)");
  
  private WindowsPathType()
  {
    super(true, '\\', new char[] { '/' });
  }
  
  public PathType.ParseResult parsePath(String path)
  {
    String original = path;
    path = path.replace('/', '\\');
    if (WORKING_DIR_WITH_DRIVE.matcher(path).matches()) {
      throw new InvalidPathException(original, "Jimfs does not currently support the Windows syntax for a relative path on a specific drive (e.g. \"C:foo\\bar\"");
    }
    String root;
    String root;
    if (path.startsWith("\\\\"))
    {
      root = parseUncRoot(path, original);
    }
    else
    {
      if (path.startsWith("\\")) {
        throw new InvalidPathException(original, "Jimfs does not currently support the Windows syntax for an absolute path on the current drive (e.g. \"\\foo\\bar\"");
      }
      root = parseDriveRoot(path);
    }
    int startIndex = (root == null) || (root.length() > 3) ? 0 : root.length();
    for (int i = startIndex; i < path.length(); i++)
    {
      char c = path.charAt(i);
      if (isReserved(c)) {
        throw new InvalidPathException(original, "Illegal char <" + c + ">", i);
      }
    }
    Matcher trailingSpaceMatcher = TRAILING_SPACES.matcher(path);
    if (trailingSpaceMatcher.find()) {
      throw new InvalidPathException(original, "Trailing char < >", trailingSpaceMatcher.start());
    }
    if (root != null)
    {
      path = path.substring(root.length());
      if (!root.endsWith("\\")) {
        root = root + "\\";
      }
    }
    return new PathType.ParseResult(root, splitter().split(path));
  }
  
  private static final Pattern UNC_ROOT = Pattern.compile("^(\\\\\\\\)([^\\\\]+)?(\\\\[^\\\\]+)?");
  
  private String parseUncRoot(String path, String original)
  {
    Matcher uncMatcher = UNC_ROOT.matcher(path);
    if (uncMatcher.find())
    {
      String host = uncMatcher.group(2);
      if (host == null) {
        throw new InvalidPathException(original, "UNC path is missing hostname");
      }
      String share = uncMatcher.group(3);
      if (share == null) {
        throw new InvalidPathException(original, "UNC path is missing sharename");
      }
      return path.substring(uncMatcher.start(), uncMatcher.end());
    }
    throw new InvalidPathException(original, "Invalid UNC path");
  }
  
  private static final Pattern DRIVE_LETTER_ROOT = Pattern.compile("^[a-zA-Z]:\\\\");
  
  @Nullable
  private String parseDriveRoot(String path)
  {
    Matcher drivePathMatcher = DRIVE_LETTER_ROOT.matcher(path);
    if (drivePathMatcher.find()) {
      return path.substring(drivePathMatcher.start(), drivePathMatcher.end());
    }
    return null;
  }
  
  private static boolean isReserved(char c)
  {
    switch (c)
    {
    case '"': 
    case '*': 
    case ':': 
    case '<': 
    case '>': 
    case '?': 
    case '|': 
      return true;
    }
    return c <= '\037';
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
    if (root.startsWith("\\\\")) {
      root = root.replace('\\', '/');
    } else {
      root = "/" + root.replace('\\', '/');
    }
    StringBuilder builder = new StringBuilder();
    builder.append(root);
    
    Iterator<String> iter = names.iterator();
    if (iter.hasNext())
    {
      builder.append((String)iter.next());
      while (iter.hasNext()) {
        builder.append('/').append((String)iter.next());
      }
    }
    if ((directory) && (builder.charAt(builder.length() - 1) != '/')) {
      builder.append('/');
    }
    return builder.toString();
  }
  
  public PathType.ParseResult parseUriPath(String uriPath)
  {
    uriPath = uriPath.replace('/', '\\');
    if ((uriPath.charAt(0) == '\\') && (uriPath.charAt(1) != '\\')) {
      uriPath = uriPath.substring(1);
    }
    return parsePath(uriPath);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\WindowsPathType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */