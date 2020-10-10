package com.sun.xml.xsom;

import java.util.Collection;
import java.util.Iterator;

public abstract interface XSAttContainer
  extends XSDeclaration
{
  public abstract XSWildcard getAttributeWildcard();
  
  public abstract XSAttributeUse getAttributeUse(String paramString1, String paramString2);
  
  public abstract Iterator<? extends XSAttributeUse> iterateAttributeUses();
  
  public abstract Collection<? extends XSAttributeUse> getAttributeUses();
  
  public abstract XSAttributeUse getDeclaredAttributeUse(String paramString1, String paramString2);
  
  public abstract Iterator<? extends XSAttributeUse> iterateDeclaredAttributeUses();
  
  public abstract Collection<? extends XSAttributeUse> getDeclaredAttributeUses();
  
  public abstract Iterator<? extends XSAttGroupDecl> iterateAttGroups();
  
  public abstract Collection<? extends XSAttGroupDecl> getAttGroups();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\XSAttContainer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */