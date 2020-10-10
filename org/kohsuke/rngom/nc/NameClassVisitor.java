package org.kohsuke.rngom.nc;

import javax.xml.namespace.QName;

public abstract interface NameClassVisitor<V>
{
  public abstract V visitChoice(NameClass paramNameClass1, NameClass paramNameClass2);
  
  public abstract V visitNsName(String paramString);
  
  public abstract V visitNsNameExcept(String paramString, NameClass paramNameClass);
  
  public abstract V visitAnyName();
  
  public abstract V visitAnyNameExcept(NameClass paramNameClass);
  
  public abstract V visitName(QName paramQName);
  
  public abstract V visitNull();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\nc\NameClassVisitor.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */