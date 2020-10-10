package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLable;

abstract interface ResourceType
  extends XMLable
{
  public abstract void visit(ResourceVisitor paramResourceVisitor);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\ResourceType.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */