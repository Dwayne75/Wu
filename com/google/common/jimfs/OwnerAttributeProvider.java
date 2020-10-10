package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Map;
import javax.annotation.Nullable;

final class OwnerAttributeProvider
  extends AttributeProvider
{
  private static final ImmutableSet<String> ATTRIBUTES = ImmutableSet.of("owner");
  private static final UserPrincipal DEFAULT_OWNER = UserLookupService.createUserPrincipal("user");
  
  public String name()
  {
    return "owner";
  }
  
  public ImmutableSet<String> fixedAttributes()
  {
    return ATTRIBUTES;
  }
  
  public ImmutableMap<String, ?> defaultValues(Map<String, ?> userProvidedDefaults)
  {
    Object userProvidedOwner = userProvidedDefaults.get("owner:owner");
    
    UserPrincipal owner = DEFAULT_OWNER;
    if (userProvidedOwner != null) {
      if ((userProvidedOwner instanceof String)) {
        owner = UserLookupService.createUserPrincipal((String)userProvidedOwner);
      } else {
        throw invalidType("owner", "owner", userProvidedOwner, new Class[] { String.class, UserPrincipal.class });
      }
    }
    return ImmutableMap.of("owner:owner", owner);
  }
  
  @Nullable
  public Object get(File file, String attribute)
  {
    if (attribute.equals("owner")) {
      return file.getAttribute("owner", "owner");
    }
    return null;
  }
  
  public void set(File file, String view, String attribute, Object value, boolean create)
  {
    if (attribute.equals("owner"))
    {
      UserPrincipal user = (UserPrincipal)checkType(view, attribute, value, UserPrincipal.class);
      if (!(user instanceof UserLookupService.JimfsUserPrincipal)) {
        user = UserLookupService.createUserPrincipal(user.getName());
      }
      file.setAttribute("owner", "owner", user);
    }
  }
  
  public Class<FileOwnerAttributeView> viewType()
  {
    return FileOwnerAttributeView.class;
  }
  
  public FileOwnerAttributeView view(FileLookup lookup, ImmutableMap<String, FileAttributeView> inheritedViews)
  {
    return new View(lookup);
  }
  
  private static final class View
    extends AbstractAttributeView
    implements FileOwnerAttributeView
  {
    public View(FileLookup lookup)
    {
      super();
    }
    
    public String name()
    {
      return "owner";
    }
    
    public UserPrincipal getOwner()
      throws IOException
    {
      return (UserPrincipal)lookupFile().getAttribute("owner", "owner");
    }
    
    public void setOwner(UserPrincipal owner)
      throws IOException
    {
      lookupFile().setAttribute("owner", "owner", Preconditions.checkNotNull(owner));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\OwnerAttributeProvider.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */