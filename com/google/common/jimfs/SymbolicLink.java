package com.google.common.jimfs;

import com.google.common.base.Preconditions;

final class SymbolicLink
  extends File
{
  private final JimfsPath target;
  
  public static SymbolicLink create(int id, JimfsPath target)
  {
    return new SymbolicLink(id, target);
  }
  
  private SymbolicLink(int id, JimfsPath target)
  {
    super(id);
    this.target = ((JimfsPath)Preconditions.checkNotNull(target));
  }
  
  JimfsPath target()
  {
    return this.target;
  }
  
  File copyWithoutContent(int id)
  {
    return create(id, this.target);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\SymbolicLink.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */