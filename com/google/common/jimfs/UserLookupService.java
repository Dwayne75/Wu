package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.UserPrincipalNotFoundException;

final class UserLookupService
  extends UserPrincipalLookupService
{
  private final boolean supportsGroups;
  
  public UserLookupService(boolean supportsGroups)
  {
    this.supportsGroups = supportsGroups;
  }
  
  public UserPrincipal lookupPrincipalByName(String name)
  {
    return createUserPrincipal(name);
  }
  
  public GroupPrincipal lookupPrincipalByGroupName(String group)
    throws IOException
  {
    if (!this.supportsGroups) {
      throw new UserPrincipalNotFoundException(group);
    }
    return createGroupPrincipal(group);
  }
  
  static UserPrincipal createUserPrincipal(String name)
  {
    return new JimfsUserPrincipal(name, null);
  }
  
  static GroupPrincipal createGroupPrincipal(String name)
  {
    return new JimfsGroupPrincipal(name, null);
  }
  
  private static abstract class NamedPrincipal
    implements UserPrincipal
  {
    protected final String name;
    
    private NamedPrincipal(String name)
    {
      this.name = ((String)Preconditions.checkNotNull(name));
    }
    
    public final String getName()
    {
      return this.name;
    }
    
    public final int hashCode()
    {
      return this.name.hashCode();
    }
    
    public final String toString()
    {
      return this.name;
    }
  }
  
  static final class JimfsUserPrincipal
    extends UserLookupService.NamedPrincipal
  {
    private JimfsUserPrincipal(String name)
    {
      super(null);
    }
    
    public boolean equals(Object obj)
    {
      return ((obj instanceof JimfsUserPrincipal)) && (getName().equals(((JimfsUserPrincipal)obj).getName()));
    }
  }
  
  static final class JimfsGroupPrincipal
    extends UserLookupService.NamedPrincipal
    implements GroupPrincipal
  {
    private JimfsGroupPrincipal(String name)
    {
      super(null);
    }
    
    public boolean equals(Object obj)
    {
      return ((obj instanceof JimfsGroupPrincipal)) && (((JimfsGroupPrincipal)obj).name.equals(this.name));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\UserLookupService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */