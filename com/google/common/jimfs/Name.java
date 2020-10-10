package com.google.common.jimfs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import javax.annotation.Nullable;

final class Name
{
  static final Name EMPTY = new Name("", "");
  public static final Name SELF = new Name(".", ".");
  public static final Name PARENT = new Name("..", "..");
  private final String display;
  private final String canonical;
  
  @VisibleForTesting
  static Name simple(String name)
  {
    switch (name)
    {
    case ".": 
      return SELF;
    case "..": 
      return PARENT;
    }
    return new Name(name, name);
  }
  
  public static Name create(String display, String canonical)
  {
    return new Name(display, canonical);
  }
  
  private Name(String display, String canonical)
  {
    this.display = ((String)Preconditions.checkNotNull(display));
    this.canonical = ((String)Preconditions.checkNotNull(canonical));
  }
  
  public boolean equals(@Nullable Object obj)
  {
    if ((obj instanceof Name))
    {
      Name other = (Name)obj;
      return this.canonical.equals(other.canonical);
    }
    return false;
  }
  
  public int hashCode()
  {
    return Util.smearHash(this.canonical.hashCode());
  }
  
  public String toString()
  {
    return this.display;
  }
  
  public static Ordering<Name> displayOrdering()
  {
    return DISPLAY_ORDERING;
  }
  
  public static Ordering<Name> canonicalOrdering()
  {
    return CANONICAL_ORDERING;
  }
  
  private static final Ordering<Name> DISPLAY_ORDERING = Ordering.natural().onResultOf(new Function()
  {
    public String apply(Name name)
    {
      return name.display;
    }
  });
  private static final Ordering<Name> CANONICAL_ORDERING = Ordering.natural().onResultOf(new Function()
  {
    public String apply(Name name)
    {
      return name.canonical;
    }
  });
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\Name.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */