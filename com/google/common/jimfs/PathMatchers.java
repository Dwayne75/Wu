package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ascii;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PathMatchers
{
  public static PathMatcher getPathMatcher(String syntaxAndPattern, String separators, ImmutableSet<PathNormalization> normalizations)
  {
    int syntaxSeparator = syntaxAndPattern.indexOf(':');
    Preconditions.checkArgument(syntaxSeparator > 0, "Must be of the form 'syntax:pattern': %s", new Object[] { syntaxAndPattern });
    
    String syntax = Ascii.toLowerCase(syntaxAndPattern.substring(0, syntaxSeparator));
    String pattern = syntaxAndPattern.substring(syntaxSeparator + 1);
    switch (syntax)
    {
    case "glob": 
      pattern = GlobToRegex.toRegex(pattern, separators);
    case "regex": 
      return fromRegex(pattern, normalizations);
    }
    throw new UnsupportedOperationException("Invalid syntax: " + syntaxAndPattern);
  }
  
  private static PathMatcher fromRegex(String regex, Iterable<PathNormalization> normalizations)
  {
    return new RegexPathMatcher(PathNormalization.compilePattern(regex, normalizations), null);
  }
  
  @VisibleForTesting
  static final class RegexPathMatcher
    implements PathMatcher
  {
    private final Pattern pattern;
    
    private RegexPathMatcher(Pattern pattern)
    {
      this.pattern = ((Pattern)Preconditions.checkNotNull(pattern));
    }
    
    public boolean matches(Path path)
    {
      return this.pattern.matcher(path.toString()).matches();
    }
    
    public String toString()
    {
      return MoreObjects.toStringHelper(this).addValue(this.pattern).toString();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\PathMatchers.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */