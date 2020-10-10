package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

final class UnixAttributeProvider
  extends AttributeProvider
{
  private static final ImmutableSet<String> ATTRIBUTES = ImmutableSet.of("uid", "ino", "dev", "nlink", "rdev", "ctime", new String[] { "mode", "gid" });
  private static final ImmutableSet<String> INHERITED_VIEWS = ImmutableSet.of("basic", "owner", "posix");
  private final AtomicInteger uidGenerator = new AtomicInteger();
  private final ConcurrentMap<Object, Integer> idCache = new ConcurrentHashMap();
  
  public String name()
  {
    return "unix";
  }
  
  public ImmutableSet<String> inherits()
  {
    return INHERITED_VIEWS;
  }
  
  public ImmutableSet<String> fixedAttributes()
  {
    return ATTRIBUTES;
  }
  
  public Class<UnixFileAttributeView> viewType()
  {
    return UnixFileAttributeView.class;
  }
  
  public UnixFileAttributeView view(FileLookup lookup, ImmutableMap<String, FileAttributeView> inheritedViews)
  {
    throw new UnsupportedOperationException();
  }
  
  private Integer getUniqueId(Object object)
  {
    Integer id = (Integer)this.idCache.get(object);
    if (id == null)
    {
      id = Integer.valueOf(this.uidGenerator.incrementAndGet());
      Integer existing = (Integer)this.idCache.putIfAbsent(object, id);
      if (existing != null) {
        return existing;
      }
    }
    return id;
  }
  
  public Object get(File file, String attribute)
  {
    switch (attribute)
    {
    case "uid": 
      UserPrincipal user = (UserPrincipal)file.getAttribute("owner", "owner");
      return getUniqueId(user);
    case "gid": 
      GroupPrincipal group = (GroupPrincipal)file.getAttribute("posix", "group");
      return getUniqueId(group);
    case "mode": 
      Set<PosixFilePermission> permissions = (Set)file.getAttribute("posix", "permissions");
      
      return Integer.valueOf(toMode(permissions));
    case "ctime": 
      return FileTime.fromMillis(file.getCreationTime());
    case "rdev": 
      return Long.valueOf(0L);
    case "dev": 
      return Long.valueOf(1L);
    case "ino": 
      return Integer.valueOf(file.id());
    case "nlink": 
      return Integer.valueOf(file.links());
    }
    return null;
  }
  
  public void set(File file, String view, String attribute, Object value, boolean create)
  {
    throw unsettable(view, attribute);
  }
  
  private static int toMode(Set<PosixFilePermission> permissions)
  {
    int result = 0;
    for (PosixFilePermission permission : permissions)
    {
      Preconditions.checkNotNull(permission);
      switch (permission)
      {
      case OWNER_READ: 
        result |= 0x100;
        break;
      case OWNER_WRITE: 
        result |= 0x80;
        break;
      case OWNER_EXECUTE: 
        result |= 0x40;
        break;
      case GROUP_READ: 
        result |= 0x20;
        break;
      case GROUP_WRITE: 
        result |= 0x10;
        break;
      case GROUP_EXECUTE: 
        result |= 0x8;
        break;
      case OTHERS_READ: 
        result |= 0x4;
        break;
      case OTHERS_WRITE: 
        result |= 0x2;
        break;
      case OTHERS_EXECUTE: 
        result |= 0x1;
        break;
      default: 
        throw new AssertionError();
      }
    }
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\UnixAttributeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */