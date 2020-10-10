package com.sun.xml.xsom;

import java.util.Iterator;

public abstract interface XSAttContainer
  extends XSDeclaration
{
  public abstract XSWildcard getAttributeWildcard();
  
  public abstract XSAttributeUse getAttributeUse(String paramString1, String paramString2);
  
  public abstract Iterator iterateAttributeUses();
  
  public abstract XSAttributeUse getDeclaredAttributeUse(String paramString1, String paramString2);
  
  public abstract Iterator iterateDeclaredAttributeUses();
  
  public abstract Iterator iterateAttGroups();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSAttContainer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */