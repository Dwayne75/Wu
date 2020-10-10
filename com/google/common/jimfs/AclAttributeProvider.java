package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class AclAttributeProvider
  extends AttributeProvider
{
  private static final ImmutableSet<String> ATTRIBUTES = ImmutableSet.of("acl");
  private static final ImmutableSet<String> INHERITED_VIEWS = ImmutableSet.of("owner");
  private static final ImmutableList<AclEntry> DEFAULT_ACL = ImmutableList.of();
  
  public String name()
  {
    return "acl";
  }
  
  public ImmutableSet<String> inherits()
  {
    return INHERITED_VIEWS;
  }
  
  public ImmutableSet<String> fixedAttributes()
  {
    return ATTRIBUTES;
  }
  
  public ImmutableMap<String, ?> defaultValues(Map<String, ?> userProvidedDefaults)
  {
    Object userProvidedAcl = userProvidedDefaults.get("acl:acl");
    
    ImmutableList<AclEntry> acl = DEFAULT_ACL;
    if (userProvidedAcl != null) {
      acl = toAcl((List)checkType("acl", "acl", userProvidedAcl, List.class));
    }
    return ImmutableMap.of("acl:acl", acl);
  }
  
  @Nullable
  public Object get(File file, String attribute)
  {
    if (attribute.equals("acl")) {
      return file.getAttribute("acl", "acl");
    }
    return null;
  }
  
  public void set(File file, String view, String attribute, Object value, boolean create)
  {
    if (attribute.equals("acl"))
    {
      checkNotCreate(view, attribute, create);
      file.setAttribute("acl", "acl", toAcl((List)checkType(view, attribute, value, List.class)));
    }
  }
  
  private static ImmutableList<AclEntry> toAcl(List<?> list)
  {
    ImmutableList<?> copy = ImmutableList.copyOf(list);
    for (Object obj : copy) {
      if (!(obj instanceof AclEntry)) {
        throw new IllegalArgumentException("invalid element for attribute 'acl:acl': should be List<AclEntry>, found element of type " + obj.getClass());
      }
    }
    return copy;
  }
  
  public Class<AclFileAttributeView> viewType()
  {
    return AclFileAttributeView.class;
  }
  
  public AclFileAttributeView view(FileLookup lookup, ImmutableMap<String, FileAttributeView> inheritedViews)
  {
    return new View(lookup, (FileOwnerAttributeView)inheritedViews.get("owner"));
  }
  
  private static final class View
    extends AbstractAttributeView
    implements AclFileAttributeView
  {
    private final FileOwnerAttributeView ownerView;
    
    public View(FileLookup lookup, FileOwnerAttributeView ownerView)
    {
      super();
      this.ownerView = ((FileOwnerAttributeView)Preconditions.checkNotNull(ownerView));
    }
    
    public String name()
    {
      return "acl";
    }
    
    public List<AclEntry> getAcl()
      throws IOException
    {
      return (List)lookupFile().getAttribute("acl", "acl");
    }
    
    public void setAcl(List<AclEntry> acl)
      throws IOException
    {
      Preconditions.checkNotNull(acl);
      lookupFile().setAttribute("acl", "acl", ImmutableList.copyOf(acl));
    }
    
    public UserPrincipal getOwner()
      throws IOException
    {
      return this.ownerView.getOwner();
    }
    
    public void setOwner(UserPrincipal owner)
      throws IOException
    {
      this.ownerView.setOwner(owner);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\AclAttributeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */