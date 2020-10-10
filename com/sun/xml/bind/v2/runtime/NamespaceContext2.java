package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import javax.xml.namespace.NamespaceContext;

public abstract interface NamespaceContext2
  extends NamespaceContext
{
  public abstract String declareNamespace(String paramString1, String paramString2, boolean paramBoolean);
  
  public abstract int force(@NotNull String paramString1, @NotNull String paramString2);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\NamespaceContext2.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */