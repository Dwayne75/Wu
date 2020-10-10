package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;

public final class Location
  implements Comparable<Location>
{
  private static final String CLASSPATH_PREFIX = "classpath:";
  public static final String FILESYSTEM_PREFIX = "filesystem:";
  private String prefix;
  private String path;
  
  public Location(String descriptor)
  {
    String normalizedDescriptor = descriptor.trim().replace("\\", "/");
    if (normalizedDescriptor.contains(":"))
    {
      this.prefix = normalizedDescriptor.substring(0, normalizedDescriptor.indexOf(":") + 1);
      this.path = normalizedDescriptor.substring(normalizedDescriptor.indexOf(":") + 1);
    }
    else
    {
      this.prefix = "classpath:";
      this.path = normalizedDescriptor;
    }
    if (isClassPath())
    {
      this.path = this.path.replace(".", "/");
      if (this.path.startsWith("/")) {
        this.path = this.path.substring(1);
      }
    }
    else if (!isFileSystem())
    {
      throw new FlywayException("Unknown prefix for location (should be either filesystem: or classpath:): " + normalizedDescriptor);
    }
    if (this.path.endsWith("/")) {
      this.path = this.path.substring(0, this.path.length() - 1);
    }
  }
  
  public boolean isClassPath()
  {
    return "classpath:".equals(this.prefix);
  }
  
  public boolean isFileSystem()
  {
    return "filesystem:".equals(this.prefix);
  }
  
  public boolean isParentOf(Location other)
  {
    return (other.getDescriptor() + "/").startsWith(getDescriptor() + "/");
  }
  
  public String getPrefix()
  {
    return this.prefix;
  }
  
  public String getPath()
  {
    return this.path;
  }
  
  public String getDescriptor()
  {
    return this.prefix + this.path;
  }
  
  public int compareTo(Location o)
  {
    return getDescriptor().compareTo(o.getDescriptor());
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Location location = (Location)o;
    
    return getDescriptor().equals(location.getDescriptor());
  }
  
  public int hashCode()
  {
    return getDescriptor().hashCode();
  }
  
  public String toString()
  {
    return getDescriptor();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\Location.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */