package com.sun.xml.xsom;

public abstract interface XSDeclaration
  extends XSComponent
{
  public abstract String getTargetNamespace();
  
  public abstract String getName();
  
  /**
   * @deprecated
   */
  public abstract boolean isAnonymous();
  
  public abstract boolean isGlobal();
  
  public abstract boolean isLocal();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\XSDeclaration.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */