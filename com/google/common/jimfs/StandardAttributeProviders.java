package com.google.common.jimfs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import javax.annotation.Nullable;

final class StandardAttributeProviders
{
  private static final ImmutableMap<String, AttributeProvider> PROVIDERS = new ImmutableMap.Builder().put("basic", new BasicAttributeProvider()).put("owner", new OwnerAttributeProvider()).put("posix", new PosixAttributeProvider()).put("dos", new DosAttributeProvider()).put("acl", new AclAttributeProvider()).put("user", new UserDefinedAttributeProvider()).build();
  
  @Nullable
  public static AttributeProvider get(String view)
  {
    AttributeProvider provider = (AttributeProvider)PROVIDERS.get(view);
    if ((provider == null) && (view.equals("unix"))) {
      return new UnixAttributeProvider();
    }
    return provider;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\StandardAttributeProviders.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */